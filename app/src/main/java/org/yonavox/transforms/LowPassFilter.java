package org.yonavox.transforms;

import org.yonavox.util.ArrayUtils;
import org.yonavox.util.ComplexUtils;
import org.yonavox.util.Constants;

import java.util.Arrays;

public class LowPassFilter {
    //Filter polynomials
    private final float denominator_0; //first denominator coefficient
    private final float denom0_reciprocal;
    private final float[] denominator_1; //all denominator coefficients except the first
    private final float[] numerator;

    //Pre-allocated arrays
    private final float[] paddedSignal;
    private final float[] output;
    private final float[] signalSlice;
    private final float[] outputSlice;

    //The polynomials are flipped for faster real-time convolutions (this is a deviation from the text-book implementation, but is faster)
    //For most use cases, it is better to invoke LowPassFilter.build();
    private LowPassFilter(float[] numerator, float[] denominator) {
        this.denominator_0 = denominator[0];
        this.denom0_reciprocal = denominator_0 != 0? 1 / denominator_0 : 1000000000000f;
        this.denominator_1 = ArrayUtils.flip(Arrays.copyOfRange(denominator, 1, denominator.length));
        this.numerator = ArrayUtils.flip(numerator);

        this.paddedSignal = new float[Constants.DOWNSAMPLE_COEFFICIENT * Constants.SAMPLE_RATE + numerator.length - 1];
        this.output = new float[paddedSignal.length];
        this.signalSlice = new float[numerator.length];
        this.outputSlice = new float[numerator.length - 1];
    }

    //Create a new low pass filter. Each step is explained in the annotations above the function declaration for that step
    public static LowPassFilter build(int sampleRate, int cutoffFreq, int filterOrder) {
        float[] angles = makeAngles(filterOrder);
        float[][] unitPoles = makeUnitPoles(angles);
        float[][] scaledPoles = scaleUnitPoles(unitPoles, sampleRate, cutoffFreq);

        float[][] poles = castToZPlane(scaledPoles, sampleRate);
        float[][] zeros = createFilterZeros(filterOrder);
        float gain = calculateGain(unitPoles, sampleRate, cutoffFreq);

        return fromZPK(zeros, poles, gain);
    }

    //Strictly non-concurrent! concurrent invocations will corrupt all running calculations
    //Filter the provided signal, returning the filtered and downsampled result
    public synchronized float[] apply(float[] signal) {
        System.arraycopy(signal, 0, paddedSignal, numerator.length - 1, signal.length);

        for(int n=0; n<signal.length; n++){
            System.arraycopy(paddedSignal, n, signalSlice, 0, numerator.length);
            System.arraycopy(output, n, outputSlice, 0, numerator.length - 1);

            float signalDot = ArrayUtils.dotProduct(numerator, signalSlice);
            float outputDot = ArrayUtils.dotProduct(denominator_1, outputSlice);
            float convolution = signalDot - outputDot;
            output[n + numerator.length - 1] = denom0_reciprocal * convolution;
        }

        return ArrayUtils.downsample(output, Constants.DOWNSAMPLE_COEFFICIENT, numerator.length - 1);
    }

    //Creates an array of angles (in radians), evenly spaced along the unit circle
    private static float[] makeAngles(int filterOrder) {
        float[] angles = new float[filterOrder];
        for(int k=0; k<angles.length; k++){
            angles[k] = (float)((2*k + 1) * Math.PI) / (2*filterOrder);
        }

        return angles;
    }

    //Converts an array of angles to their matching complex numbers on the unit circle
    //Each pole is a 2-scalar array where pole = pole[0] + i*pole[1]
    private static float[][] makeUnitPoles(float[] angles) {
        float[][] unitPoles = new float[angles.length][2];
        for(int k=0; k<angles.length; k++){
            float angle = angles[k];
            unitPoles[k][0] = -(float)Math.sin(angle);
            unitPoles[k][1] = +(float)Math.cos(angle);
        }

        return unitPoles;
    }

    //Perform pre-warping to find the analog cutoff frequency, based on the digital cutoff frequency:
    //Then scale the unit poles to our actual cutoff frequency, by multiplying by 2pi * analogCuttoffFreq
    private static float[][] scaleUnitPoles(float[][] unitPoles, int sampleRate, int cuttoffFreq) {
        float[][] scaledPoles = new float[unitPoles.length][2];
        float analogCuttoffFreq = (float)((sampleRate / Math.PI) * Math.tan((Math.PI * cuttoffFreq) / sampleRate));
        for(int k=0; k<unitPoles.length; k++){
            scaledPoles[k][0] = 2 * (float)Math.PI * analogCuttoffFreq * unitPoles[k][0];
            scaledPoles[k][1] = 2 * (float)Math.PI * analogCuttoffFreq * unitPoles[k][1];
        }

        return scaledPoles;
    }

    //The poles we obtain are expressed as coordinates in the s-plane (https://en.wikipedia.org/wiki/S-plane)
    //Cast them to coordinates in the z-plane (the standard complex plane)
    private static float[][] castToZPlane(float[][] sPlanePoles, int sampleRate) {
        float[][] zPlanePoles = new float[sPlanePoles.length][2];
        float[] one = ComplexUtils.complex(1.0f);
        float[] doubleSampleRate = ComplexUtils.complex(2*sampleRate);
        for(int k=0; k<sPlanePoles.length; k++){
            //Calculates (1 + sPlanePoles[k] / (2*sample_rate)) / (1 - sPlanePoles[k] / (2*sample_rate))
            float[] quotient = ComplexUtils.divide(sPlanePoles[k], doubleSampleRate);
            float[] numerator = ComplexUtils.add(one, quotient);
            float[] denominator = ComplexUtils.subtract(one, quotient);
            ComplexUtils.divide(numerator, denominator, zPlanePoles[k]);
        }

        return zPlanePoles;
    }

    //The zero values are given as -1 + 0j for all zeros (as in poles and zeros of the transfer function, not zero scalars)
    //It's as simple as initializing an array
    private static float[][] createFilterZeros(int filterOrder) {
        float[][] zeros = new float[filterOrder][2];
        for(int k=0; k<zeros.length; k++){
            zeros[k][0] = -1;
            //zeros[k][1] = 0 is implied;
        }

        return zeros;
    }

    //Define the gain scalar for the transfer function
    //(honestly, couldn't figure out these equations. copied from scipy.signal.iirfilter, best guess is by the names I gave the cryptic letters there...)
    private static float calculateGain(float[][] unitPoles, int sampleRate, int cutoffFreq) {
        float prewarpGain = (float)(4.0f * Math.tan(Math.PI * cutoffFreq / sampleRate));
        float[][] gainPoles = ArrayUtils.multiply(unitPoles, prewarpGain);
        float exponentialGain = (float)Math.pow(prewarpGain, unitPoles.length);

        //np.real(1 / np.prod(4.0 - gain_poles))
        float[][] negGainPoles = ArrayUtils.multiply(gainPoles, -1);
        float[] reducedProd = ArrayUtils.reduceProd(ArrayUtils.add(negGainPoles, 4.0f));
        float[] prodReciprocal = ComplexUtils.divide(ComplexUtils.complex(1.0f), reducedProd);
        return exponentialGain * ComplexUtils.real(prodReciprocal);
    }

    //Creates an array storing the coefficients of polynomial
    //By providing an array containing it's roots
    //The algorithm is explained on https://stackoverflow.com/questions/32932245
    public static float[][] rootsToCoefficients(float polynomialRoots[][]){
        float[][] curriedConvolve = new float[][] {{1.0f, 0.0f}};

        for (float[] root : polynomialRoots){
            float[][] rootKernel = new float[][] {{1.0f, 0.0f}, {-root[0], -root[1]}};
            curriedConvolve = ArrayUtils.fullConvolve(curriedConvolve, rootKernel);
        }

        return curriedConvolve;
    }

    //Construct a low pass filter from zeros and poles, scaled by the filter's gain
    private static LowPassFilter fromZPK(float[][] zeros, float[][] poles, float gain){
        float[][] zeroCoefficients = rootsToCoefficients(zeros);
        float[][] poleCoefficients = rootsToCoefficients(poles);

        float[] realZeros = ArrayUtils.real(zeroCoefficients);
        float[] realPoles = ArrayUtils.real(poleCoefficients);

        return new LowPassFilter(
            ArrayUtils.multiply(realZeros, gain),
            realPoles
        );
    }
}
