package org.yonavox.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import org.yonavox.R;
import org.yonavox.activities.MainActivity;
import org.yonavox.util.Constants;

public class RecordingService extends Service {
    private static Context appContext;
    private static AudioRecord audioRecorder;
    private static final int SERVICE_MESSAGE_ID = 42;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(Constants.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT);
    private static final float[] cyclicPcmData = new float[Constants.DOWNSAMPLE_COEFFICIENT * Constants.SPECTROGRAM_TIMESTAMPS]; //That's about 2.25 seconds worth of audio
    private static int cyclicIdx = 0;

    private static int loudSamples = 0;
    private static int silentSamples = 0;
    private static int trackedSamples = 0;
    private static boolean currentlySpeaking = false;

    @Override
    public void onCreate(){
        appContext = getApplicationContext();
        audioRecorder = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                Constants.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                BUFFER_SIZE
        );

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification recordNotification = new NotificationCompat.Builder(this, "Recording Service Notification")
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.recording_notification))
                .setSmallIcon(R.drawable.ic_signal_wave)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(SERVICE_MESSAGE_ID, recordNotification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
            audioRecorder.startRecording();
            Thread recordingThread = new Thread(() -> readAudio(audioRecorder), "AudioRecorder Thread");
            recordingThread.start();
        }else{
            throw new RuntimeException("Failed to initialize audio recorder");
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //We never bind to this service. The docs say we should return null in this case.
        return null;
    }

    @Override
    public void onDestroy(){
        if(audioRecorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
            audioRecorder.stop();
            audioRecorder.release();
        }
    }

    //Stores the latest signals from the device microphone in cyclicPcmData
    //When out of space, overrides previously recorded floats
    private static void readAudio(AudioRecord audioRecorder){
        float[] buffer = new float[(int) Math.ceil(0.25 * BUFFER_SIZE)]; //getMinBufferSize() returns the minimum buffer size in bytes; Each float is represented using 4 bytes
        while(audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
            int floatsRead = audioRecorder.read(buffer, 0, 256, AudioRecord.READ_NON_BLOCKING);

            //Write each new float to the cyclic buffer
            for (int i = 0; i < floatsRead; i++) {
                cyclicPcmData[cyclicIdx] = buffer[i];
                cyclicIdx = (cyclicIdx + 1) % cyclicPcmData.length;
                handleRecordedFloat(buffer[i], i);
            }
        }
    }

    /*Every recorded float is factored in a calculation determining if speech was detected.
    * When done speaking, pass the raw data to the transcription service.*/
    private static void handleRecordedFloat(float recordedFloat, int currentIdx){
        //Check if the new float is a significant utterance (by amplitude)
        if(recordedFloat < -Constants.SPEECH_AMPLITUDE || Constants.SPEECH_AMPLITUDE < recordedFloat){
            currentlySpeaking = true;
            loudSamples++;
            trackedSamples++;
        }else if (currentlySpeaking){
            silentSamples++;
            trackedSamples++;
        }

        //Decrement counters for samples outside the silence window
        if(trackedSamples > Constants.SILENCE_WINDOW_SIZE) {
            int laggedIndex =  getLaggingIndex(currentIdx, Constants.SILENCE_WINDOW_SIZE);
            float laggedAmplitude = cyclicPcmData[laggedIndex];
            if(silentSamples > 0 && -Constants.SPEECH_AMPLITUDE <= laggedAmplitude && laggedAmplitude <= Constants.SPEECH_AMPLITUDE){
                silentSamples--;
            }
        }

        //Decrement counters for samples outside the utter window
        if(trackedSamples > Constants.UTTER_WINDOW_SIZE && loudSamples < Constants.MIN_UTTERED_SAMPLES) {
            int laggedIndex =  getLaggingIndex(currentIdx, Constants.UTTER_WINDOW_SIZE);
            float laggedAmplitude = cyclicPcmData[laggedIndex];
            trackedSamples--;

            if(loudSamples > 0 && (laggedAmplitude < -Constants.SPEECH_AMPLITUDE || Constants.SPEECH_AMPLITUDE < laggedAmplitude)){
                loudSamples--;
            }
            if(silentSamples > 0 && -Constants.SPEECH_AMPLITUDE <= laggedAmplitude && laggedAmplitude <= Constants.SPEECH_AMPLITUDE){
                silentSamples--;
            }
        }

        //When enough floats are no longer significant, handle the recorded data
        if(silentSamples > Constants.MAX_SILENT_SAMPLES){
            //trigger the preprocessing chain if enough samples were spoken
            if(loudSamples > Constants.MIN_UTTERED_SAMPLES){
                System.out.println("Speech detected!");
                invokeTranscription();
            }

            //reset significance counters
            loudSamples = 0;
            silentSamples = 0;
            trackedSamples = 0;
            currentlySpeaking = false;
        }
    }

    //Returns (currentIdx - lag) mod cyclicPcmData.length
    private static int getLaggingIndex(int currentIdx, int lag){
        int laggedIndex = currentIdx - lag;
        if (laggedIndex < 0) {
            laggedIndex += cyclicPcmData.length;
        }

        return laggedIndex;
    }

    //Returns the data from the cyclic array in sequential form (i.e. time-ordered)
    public static float[] getRecordingData() {
        float[] copyOfData = new float[cyclicPcmData.length];
        System.arraycopy(cyclicPcmData, cyclicIdx, copyOfData, 0, cyclicPcmData.length - cyclicIdx);
        System.arraycopy(cyclicPcmData, 0, copyOfData, cyclicPcmData.length - cyclicIdx, cyclicIdx);
        return copyOfData;
    }

    //Passes the current sound recording to the transcription service
    public static void invokeTranscription(){
        float[] latestPcmData = getRecordingData();
        Intent transcriptionIntent = new Intent(appContext, TranscriptionService.class);
        transcriptionIntent.putExtra(Constants.RAW_PCM_DATA, latestPcmData);
        appContext.startService(transcriptionIntent);
    }

    //Return the size of the buffer used by the audio recorder
    public static int getBufferSize() {
        return BUFFER_SIZE;
    }
}
