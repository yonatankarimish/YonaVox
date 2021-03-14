package org.yonavox.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.yonavox.util.ArrayUtils;
import org.yonavox.util.Constants;
import org.yonavox.util.PreprocessUtils;
import org.yonavox.util.SensiboUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class TranscriptionService extends Service {
    private Module voxEncoder;
    private Module voxDecoder;

    private Map<String, Long> syllableToToken;
    private Map<Long, String> tokenToSyllable;

    @Override
    public void onCreate(){
        try {
            voxEncoder = Module.load(assetFilePath(this, "vox_encoder.pt"));
            voxDecoder = Module.load(assetFilePath(this, "vox_decoder.pt"));
        } catch (IOException e) {
            System.err.println("Failed to init recognition model. Caused by: " + e.getMessage());
            throw new RuntimeException(e);
        }

        populateTokenMaps();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread transcriptionThread = new Thread(() -> actOnIntent(intent));
        transcriptionThread.start();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //We never bind to this service. The docs say we should return null in this case.
        return null;
    }

    //Transcribe the raw pcm data, invoking the sensor if anything meaningful was said
    private void actOnIntent(Intent intent){
        float[] pcmData = intent.getFloatArrayExtra(Constants.RAW_PCM_DATA);
        String transcription = transcribe(pcmData);
        invokeSensor(transcription);
    }

    //Converts speech to text; The core of the entire app.
    private String transcribe(float[] pcmData){
        float[][] spectrogram = PreprocessUtils.preprocess(pcmData);
        long[] tensorShape = getDimensions(spectrogram);

        float[] flatSpectro = ArrayUtils.concat(spectrogram);
        IValue spectroTensor = IValue.from(Tensor.fromBlob(flatSpectro, tensorShape));
        //IValue spectroTensor = IValue.from(Tensor.fromBlob(new float[(int)(dim0 * dim1 * dim2 * dim3)], tensorShape));
        IValue[] encoderResults = voxEncoder.forward(spectroTensor).toTuple();

        IValue encoderOutput = encoderResults[0];
        IValue hiddenState = encoderResults[1];
        IValue inputTokens = IValue.from(Tensor.fromBlob(
                new long[]{syllableToToken.get("BEG")}, // The token representing "BEG" (i.e. the start token)
                new long[]{1L, 1L} //expanded as a 2d array
        ));

        int maxDecoderRounds = 20;
        StringJoiner predictedSyllables = new StringJoiner(" ", "", "");
        for(int i=0; i<maxDecoderRounds; i++){
            IValue[] decoderResults = voxDecoder.forward(encoderOutput, hiddenState, inputTokens).toTuple();
            IValue outputLogits = decoderResults[0];
            hiddenState = decoderResults[1];

            float[] logitArray = outputLogits.toTensor().getDataAsFloatArray();
            int predictedToken = ArrayUtils.indexOfMax(logitArray);

            String predictedSyllable = tokenToSyllable.get((long)predictedToken).toLowerCase();
            if(predictedSyllable.equals("end")){
                break;
            }else{
                if(!predictedSyllable.equals("bkg") && !predictedSyllable.equals("noise")){
                    predictedSyllables.add(predictedSyllable);
                }

                inputTokens = IValue.from(Tensor.fromBlob(
                        new long[]{predictedToken},
                        new long[]{1L, 1L}
                ));
            }
        }

        return predictedSyllables.toString();
    }

    //Create a shape array based on the dimensions of the given spectrogram
    private long[] getDimensions(float[][] spectrogram){
        long dim0 = 1; //Batch dimension
        long dim1 = 1; //Channel dimension
        long dim2 = spectrogram.length; //Spectrogram height
        long dim3 = spectrogram[0].length; //spectrogram width
        return new long[]{dim0, dim1, dim2, dim3};
    }

    //Really just a long copy-paste of the token encodings from the model training code
    private void populateTokenMaps(){
        long tokenIdx = 0;
        syllableToToken = new HashMap<>();
        syllableToToken.put("END", tokenIdx++);
        syllableToToken.put("BEG", tokenIdx++);
        syllableToToken.put("BKG", tokenIdx++);
        syllableToToken.put("NOISE", tokenIdx++);
        syllableToToken.put("Ba", tokenIdx++);
        syllableToToken.put("Be", tokenIdx++);
        syllableToToken.put("Dle", tokenIdx++);
        syllableToToken.put("Dli", tokenIdx++);
        syllableToToken.put("E", tokenIdx++);
        syllableToToken.put("Et", tokenIdx++);
        syllableToToken.put("F", tokenIdx++);
        syllableToToken.put("Gan", tokenIdx++);
        syllableToToken.put("Ha", tokenIdx++);
        syllableToToken.put("Hei", tokenIdx++);
        syllableToToken.put("I", tokenIdx++);
        syllableToToken.put("K", tokenIdx++);
        syllableToToken.put("Ka", tokenIdx++);
        syllableToToken.put("L", tokenIdx++);
        syllableToToken.put("Lon", tokenIdx++);
        syllableToToken.put("Ma", tokenIdx++);
        syllableToToken.put("Mem", tokenIdx++);
        syllableToToken.put("Po", tokenIdx++);
        syllableToToken.put("R", tokenIdx++);
        syllableToToken.put("Re", tokenIdx++);
        syllableToToken.put("Sa", tokenIdx++);
        syllableToToken.put("T", tokenIdx++);
        syllableToToken.put("Ta", tokenIdx++);
        syllableToToken.put("Te", tokenIdx++);
        syllableToToken.put("UNK", tokenIdx++);
        syllableToToken.put("Xa", tokenIdx++);
        syllableToToken.put("Xam", tokenIdx++);
        syllableToToken.put("Z", tokenIdx++);

        tokenToSyllable = new HashMap<>();
        for(Map.Entry<String, Long> entry : syllableToToken.entrySet()){
            tokenToSyllable.put(entry.getValue(), entry.getKey());
        }
    }

    //Invokes predetermined methods in the sensor's API, based on the predicted phrase.
    private void invokeSensor(String predictedPhrase) {
        if(predictedPhrase.contains("ha dle k ma z gan")
                || predictedPhrase.contains("ha f e l ma z gan")
                || predictedPhrase.contains("ta f i l ma z gan")
                || predictedPhrase.contains("ha dle k et ha ma z gan")
                || predictedPhrase.contains("ha f e l et ha ma z gan")
                || predictedPhrase.contains("ta f i l et ha ma z gan")){
            //These are all variations of "Turn on the AC" in Hebrew
            SensiboUtils.turnOn();
        }else if(predictedPhrase.contains("ka be ma z gan")
                || predictedPhrase.contains("te xa be ma z gan")
                || predictedPhrase.contains("ka be et ha ma z gan")
                || predictedPhrase.contains("te xa be et ha ma z gan")){
            //These are all variations of "Turn off the AC" in Hebrew
            SensiboUtils.turnOff();
        }else if(predictedPhrase.contains("kar po") //"It's cold here"
                || predictedPhrase.contains("te xa mem po") //"Warm this place up"
                || predictedPhrase.contains("te xa mem et ha sa lon")){ //"Warm the lounge up"
            SensiboUtils.heat();
        }else if(predictedPhrase.contains("xam po") //"It's hot here"
                || predictedPhrase.contains("te ka re r po") //"Cool this place down"
                || predictedPhrase.contains("te ka re r et ha sa lon")){ //"Cool the lounge down"
            SensiboUtils.cool();
        }
    }

    //Shamelessly copied from the pytorch demo app at https://github.com/pytorch/android-demo-app/blob/79c4a74aad2045b8dc16947c7b7f85490fa1cfef/HelloWorldApp/app/src/main/java/org/pytorch/helloworld/MainActivity.java
    private static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
}
