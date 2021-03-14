package org.yonavox.util;

import android.graphics.Bitmap;
import org.yonavox.transforms.LowPassFilter;
import org.yonavox.transforms.MelScaleConverter;
import org.yonavox.transforms.SpectrogramMaker;

import java.util.Date;

public class PreprocessUtils {
    private static final LowPassFilter lowPassFilter;
    private static final SpectrogramMaker spectroMaker;
    private static final MelScaleConverter melScaleConverter;

    static {
        lowPassFilter = LowPassFilter.build(
            Constants.SAMPLE_RATE,
            Constants.CUTOFF_FREQUENCY,
            Constants.FILTER_ORDER
        );
        spectroMaker = SpectrogramMaker.build(
            Constants.FRAME_LENGTH,
            Constants.HOP_LENGTH
        );
        melScaleConverter = MelScaleConverter.build(
            Constants.DOWNSAMPLE_RATE,
            Constants.FRAME_LENGTH,
            Constants.MEL_BINS,
            Constants.LOWER_EDGE_HERTZ,
            Constants.UPPER_EDGE_HERTZ
        );
    }

    public static float[][] preprocess(float[] rawPcmData){
        long start = new Date().getTime();
        float[] downsampledAudio = lowPassFilter.apply(rawPcmData);
        long dsTime = new Date().getTime();
        System.out.println("Low-pass + Downsample took " + (dsTime - start) + " ms");
        //WavUtils.playRecording(downsampledAudio, Constants.DOWNSAMPLE_RATE);

        float[][] spectrogram = spectroMaker.transform(downsampledAudio);
        long spTime = new Date().getTime();
        System.out.println("Spectrogram creation took " + (spTime - dsTime) + " ms");

        float[][] melSpectrogram = melScaleConverter.convert(spectrogram);
        long mlTime = new Date().getTime();
        System.out.println("Log-mel conversion took " + (mlTime - spTime) + " ms");
        //Bitmap melImg = toImage(melSpectrogram);

        System.out.println("Finished converting audio => spectrogram");
        return melSpectrogram;
    }

    public static Bitmap toImage(float[][] spectrogram){
        float pixelMin = Float.MAX_VALUE;
        float pixelMax = Float.MIN_VALUE;
        for(int i=0; i<spectrogram.length; i++){
            for(int j=0; j<spectrogram[0].length; j++){
                float pixel = spectrogram[i][j];
                if(pixel < pixelMin){
                    pixelMin = pixel;
                }
                if(pixel > pixelMax){
                    pixelMax = pixel;
                }
            }
        }

        float invMaxMinusMin = 1 / (pixelMax - pixelMin);
        float[][] normalizedSpectrogram = new float[spectrogram.length][spectrogram[0].length];
        for(int i=0; i<spectrogram.length; i++){
            for(int j=0; j<spectrogram[0].length; j++){
                float pixel = spectrogram[i][j];
                float normPixel = 255.0f * (pixel - pixelMin) * invMaxMinusMin;
                normalizedSpectrogram[i][j] = normPixel;
            }
        }
        float[] as1dArray = ArrayUtils.concat(normalizedSpectrogram);
        int[] pixels = new int[as1dArray.length];
        for(int i=0; i<as1dArray.length; i++){
            // int color = (A & 0xff) << 24 | (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff)
            byte subpixel = (byte)as1dArray[i];
            int alpha = (255 & 0xff) << 24;
            int blue = (subpixel & 0xff) << 16;
            int green = (subpixel & 0xff) << 8;
            int red = (subpixel & 0xff);
            pixels[i] = alpha | blue | green | red;
        }

        return Bitmap.createBitmap(
            pixels,
            normalizedSpectrogram[0].length,
            normalizedSpectrogram.length,
            Bitmap.Config.ARGB_8888
        );
    }
}
