package org.yonavox.util;

import android.media.*;
import org.yonavox.services.RecordingService;

public class WavUtils {
    public static void playRecording(float[] pcmData){
        playRecording(pcmData, Constants.SAMPLE_RATE);
    }

    public static void playRecording(float[] pcmData, int sampleRate){
        AudioTrack player = new AudioTrack.Builder()
            .setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build())
            .setAudioFormat(new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(RecordingService.getBufferSize())
            .build();

        player.play();

        int totalFloatsWritten = 0;
        while (totalFloatsWritten < pcmData.length){
            int floatsWritten = player.write(pcmData, totalFloatsWritten, RecordingService.getBufferSize(), AudioTrack.WRITE_NON_BLOCKING);
            if (floatsWritten >= 0){
                totalFloatsWritten += floatsWritten;
            }else if(floatsWritten == AudioTrack.ERROR_INVALID_OPERATION){
                System.err.println("Invalid operation error while writing to audio track");
                break;
            }else if(floatsWritten == AudioTrack.ERROR_BAD_VALUE){
                System.err.println("Bad value error while writing to audio track");
                break;
            }else if(floatsWritten == AudioTrack.ERROR_DEAD_OBJECT){
                System.err.println("Dead object error while writing to audio track");
                break;
            }
            else if(floatsWritten == AudioTrack.ERROR){
                System.err.println("Unspecified error while writing to audio track");
                break;
            }
        }

        player.stop();
    }
}
