package org.yonavox.transforms;

import org.yonavox.util.ArrayUtils;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.floatmatrix.impl.ArrayDenseFloatMatrix2D;

//Converts spectrograms from linear hertz scale to logarithmic mel-scale
public class MelScaleConverter {
    private static final float MAX_AMPLITUDE = 80.0f; //in decibels
    public final Matrix filterBank;

    private MelScaleConverter(Matrix filterBank){
        this.filterBank = filterBank;
    }

    public static MelScaleConverter build(int sampleRate, int frameLength, int numBins, int minFreq, int maxFreq){
        float[][] filterBankArray = makeFilterBank(sampleRate, frameLength, numBins, minFreq, maxFreq);
        Matrix filterBank = DenseMatrix.Factory.importFromArray(filterBankArray);
        return new MelScaleConverter(filterBank.transpose());
    }

    public synchronized float[][] convert(float[][] raw){
        Matrix spectrogram = new ArrayDenseFloatMatrix2D(raw);
        Matrix melScale = spectrogram.mtimes(this.filterBank);
        Matrix sqMel = melScale.power(Calculation.Ret.ORIG, 2);
        Matrix decibels = sqMel.log10(Calculation.Ret.ORIG)
            .minus(Math.log10(MAX_AMPLITUDE))
            .times(10.0f);

        float maxDecibel = (float)decibels.getMaxValue();
        float clip = Math.max(-100.0f - (float)Math.log10(MAX_AMPLITUDE), maxDecibel - MAX_AMPLITUDE);
        return ArrayUtils.ipMaximum(decibels.toFloatArray(), clip);
    }

    /* There are two common implementations for mel-scale:
     * HTK uses a conversion formula for the entire frequency range
     * Slaney splits the frequency range in two, and uses a different formula for each.
     * This implementation (and the librosa default) use the Slaney method.*/
    public static float hzToMel(float hzFrequency){
        //Init constants (according to the research done by Slaney)
        float logStep = 27.0f / (float)Math.log(6.4);
        float three200 = 3.0f / 200.0f;
        float hzBoundary = 1000.0f;
        float melBoundary = hzBoundary * three200;

        //Do the actual conversion
        if(hzFrequency > hzBoundary){
            return melBoundary + (float)Math.log(hzFrequency / hzBoundary) * logStep;
        }else{
            return hzFrequency * three200;
        }
    }

    public static float[] hzToMel(float[] hzFrequencies){
        //Init constants (according to the research done by Slaney)
        float logStep = 27.0f / (float)Math.log(6.4);
        float three200 = 3.0f / 200.0f;
        float hzBoundary = 1000.0f;
        float melBoundary = hzBoundary * three200;

        //Do the actual conversion
        float[] melFrequencies = new float[hzFrequencies.length];
        for(int i=0; i<hzFrequencies.length; i++){
            float hzFreq = hzFrequencies[i];
            if(hzFreq > hzBoundary){
                melFrequencies[i] = melBoundary + (float)Math.log(hzFreq / hzBoundary) * logStep;
            }else{
                melFrequencies[i] = hzFreq * three200;
            }
        }

        return melFrequencies;
    }

    //The inverse of hzToMel
    public static float melToHz(float melFrequency){
        //Init constants (according to the research done by Slaney)
        float logStep = (float)Math.log(6.4) / 27.0f;
        float twoHundredThirds = 200.0f / 3.0f;
        float hzBoundary = 1000.0f;
        float melBoundary = hzBoundary * 3.0f / 200.0f;

        //Do the actual conversion
        if(melFrequency >= melBoundary){
            return hzBoundary * (float)Math.exp(logStep * (melFrequency - melBoundary));
        }else {
            return melFrequency * twoHundredThirds;
        }
    }

    public static float[] melToHz(float[] melFrequencies){
        //Init constants (according to the research done by Slaney)
        float logStep = (float)Math.log(6.4) / 27.0f;
        float twoHundredThirds = 200.0f / 3.0f;
        float hzBoundary = 1000.0f;
        float melBoundary = hzBoundary * 3.0f / 200.0f;

        //Do the actual conversion
        float[] hzFrequencies = new float[melFrequencies.length];
        for(int i=0; i<melFrequencies.length; i++) {
            float melFreq = melFrequencies[i];
            if(melFreq >= melBoundary){
                hzFrequencies[i] = hzBoundary * (float)Math.exp(logStep * (melFreq - melBoundary));
            }else {
                hzFrequencies[i] = melFreq * twoHundredThirds;
            }
        }

        return hzFrequencies;
    }

    //Create the mel-scale filter bank for scaling the spectrograms
    private static float[][] makeFilterBank(int sampleRate, int frameLength, int numBins, int minFreq, int maxFreq){
        //Interpolate all frequencies between 0 and half the sample rate, in hz
        //Interpolate all frequencies between min_freq and half the min_freq rate, in mel
        //The min and max frequencies are first converted to mel, interpolated, then the result is converted back to hz
        float minMel = hzToMel(minFreq);
        float maxMel = hzToMel(maxFreq);
        float[] melLinspace = ArrayUtils.linspace(minMel, maxMel, numBins + 2);
        float[] melFrequencies = melToHz(melLinspace);
        float[] hzFrequencies = ArrayUtils.linspace(0.0f, 0.5f * sampleRate, 1 + frameLength/2);

        // Compute the difference between each two neighbouring mel frequencies
        // and the outer difference between the mel and hz frequencies
        float[] melDiff = ArrayUtils.neighbourDiff(melFrequencies);
        float[][] filterRamps = ArrayUtils.outerDiff(melFrequencies, hzFrequencies);

        //The energy norm is used to normalize the filter bank
        //so that each channel has equal energy (i.e. equal integral under it's mel filter)
        float[] energyNorm = new float[numBins];
        for(int i=0; i<numBins; i++){
            energyNorm[i] = 2.0f / (melFrequencies[i + 2] - melFrequencies[i]);
        }

        //Hard to explain w.o. a graphic, but constructs a "window" for each mel filter
        //This link might help: http://practicalcryptography.com/miscellaneous/machine-learning/guide-mel-frequency-cepstral-coefficients-mfccs/
        float[][] filterBank = new float[numBins][1 + frameLength/2];
        for(int binIdx=0; binIdx<numBins; binIdx++){
            float[] lower = ArrayUtils.divide(filterRamps[binIdx], - melDiff[binIdx]);
            float[] upper = ArrayUtils.divide(filterRamps[binIdx+2], melDiff[binIdx+1]);
            float[] filter = ArrayUtils.maximum(ArrayUtils.minimum(lower, upper), 0);
            filterBank[binIdx] = ArrayUtils.multiply(filter, energyNorm[binIdx]);
        }

        return filterBank;
    }
}
