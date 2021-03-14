package org.yonavox.util;

import java.util.Arrays;

public class ArrayUtils {
    public static float[] add(float[] a, float b){
        return add(a, b, new float[a.length]);
    }

    public static float[] ipAdd(float[] a, float b){
        return add(a, b, a);
    }

    public static float[] add(float[] a, float b, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = a[i] + b;
        }

        return output;
    }

    public static float[] add(float[] a, float[] b){
        return add(a, b, new float[a.length]);
    }

    public static float[] ipAdd(float[] a, float[] b){
        return add(a, b, a);
    }

    public static float[] add(float[] a, float[] b, float[] output){
        if(a.length != b.length || b.length != output.length){
            throw new ArithmeticException("Cannot perform element-wise addtion between arrays of different sizes");
        }

        for(int i=0; i<output.length; i++){
            output[i] = a[i] + b[i];
        }

        return output;
    }

    public static float[][] add(float[][] a, float b){
        return add(a, b, new float[a.length][2]);
    }

    public static float[][] ipAdd(float[][] a, float b){
        return add(a, b, a);
    }

    public static float[][] add(float[][] a, float b, float[][] output){
        for(int i=0; i<output.length; i++){
            ComplexUtils.add(a[i], b, output[i]);
        }

        return output;
    }

    public static float[][] add(float[][] a, float[][] b){
        return add(a, b, new float[a.length][2]);
    }

    public static float[][] ipAdd(float[][] a, float[][] b){
        return add(a, b, a);
    }

    public static float[][] add(float[][] a, float[][] b, float[][] output){
        if(a.length != b.length || b.length != output.length){
            throw new ArithmeticException("Cannot perform element-wise addtion between arrays of different sizes");
        }

        for(int i=0; i<output.length; i++){
            ComplexUtils.add(a[i], b[i], output[i]);
        }

        return output;
    }

    public static float[] subtract(float[] a, float b){
        return subtract(a, b, new float[a.length]);
    }

    public static float[] ipSubtract(float[] a, float b){
        return subtract(a, b, a);
    }

    public static float[] subtract(float[] a, float b, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = a[i] - b;
        }

        return output;
    }

    public static float[] subtract(float[] a, float[] b){
        return subtract(a, b, new float[a.length]);
    }

    public static float[] ipSubtract(float[] a, float[] b){
        return subtract(a, b, a);
    }

    public static float[] subtract(float[] a, float[] b, float[] output){
        if(a.length != b.length || b.length != output.length){
            throw new ArithmeticException("Cannot perform element-wise subtraction between arrays of different sizes");
        }

        for(int i=0; i<output.length; i++){
            output[i] = a[i] - b[i];
        }

        return output;
    }

    public static float[][] subtract(float[][] a, float b){
        return subtract(a, b, new float[a.length][2]);
    }

    public static float[][] ipSubtract(float[][] a, float b){
        return subtract(a, b, a);
    }

    public static float[][] subtract(float[][] a, float b, float[][] output){
        for(int i=0; i<output.length; i++){
            ComplexUtils.subtract(a[i], b, output[i]);
        }

        return output;
    }

    public static float[][] subtract(float[][] a, float[][] b){
        return subtract(a, b, new float[a.length][2]);
    }

    public static float[][] ipSubtract(float[][] a, float[][] b){
        return subtract(a, b, a);
    }

    public static float[][] subtract(float[][] a, float[][] b, float[][] output){
        if(a.length != b.length || b.length != output.length){
            throw new ArithmeticException("Cannot perform element-wise subtraction between arrays of different sizes");
        }

        for(int i=0; i<output.length; i++){
            ComplexUtils.subtract(a[i], b[i], output[i]);
        }

        return output;
    }

    public static float[] multiply(float[] a, float b){
        return multiply(a, b, new float[a.length]);
    }

    public static float[] ipMultiply(float[] a, float b){
        return multiply(a, b, a);
    }

    public static float[] multiply(float[] a, float b, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = a[i] * b;
        }

        return output;
    }

    public static float[] multiply(float[] a, float[] b){
        return multiply(a, b, new float[a.length]);
    }

    public static float[] ipMultiply(float[] a, float[] b){
        return multiply(a, b, a);
    }

    public static float[] multiply(float[] a, float[] b, float[] output){
        if(a.length != b.length || b.length != output.length){
            throw new ArithmeticException("Cannot perform element-wise multiplication between arrays of different sizes");
        }

        for(int i=0; i<output.length; i++){
            output[i] = a[i] * b[i];
        }

        return output;
    }

    public static float[][] multiply(float[][] a, float b){
        return multiply(a, b, new float[a.length][2]);
    }

    public static float[][] ipMultiply(float[][] a, float b){
        return multiply(a, b, a);
    }

    public static float[][] multiply(float[][] a, float b, float[][] output){
        for(int i=0; i<output.length; i++){
            ComplexUtils.multiply(a[i], b, output[i]);
        }

        return output;
    }

    public static float[][] multiply(float[][] a, float[][] b){
        return multiply(a, b, new float[a.length][2]);
    }

    public static float[][] ipMultiply(float[][] a, float[][] b){
        return multiply(a, b, a);
    }

    public static float[][] multiply(float[][] a, float[][] b, float[][] output){
        if(a.length != b.length || b.length != output.length){
            throw new ArithmeticException("Cannot perform element-wise multiplication between arrays of different sizes");
        }

        for(int i=0; i<output.length; i++){
            ComplexUtils.multiply(a[i], b[i], output[i]);
        }

        return output;
    }

    public static float[] divide(float[] a, float b){
        return divide(a, b, new float[a.length]);
    }

    public static float[] ipDivide(float[] a, float b){
        return divide(a, b, a);
    }

    public static float[] divide(float[] a, float b, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = a[i] / b;
        }

        return output;
    }

    public static float[] divide(float[] a, float[] b){
        return divide(a, b, new float[a.length]);
    }

    public static float[] ipDivide(float[] a, float[] b){
        return divide(a, b, a);
    }

    public static float[] divide(float[] a, float[] b, float[] output){
        if(a.length != b.length || b.length != output.length){
            throw new ArithmeticException("Cannot perform element-wise division between arrays of different sizes");
        }

        for(int i=0; i<output.length; i++){
            output[i] = a[i] / b[i];
        }

        return output;
    }

    public static float[] sin(float[] f){
        return sin(f, new float[f.length]);
    }

    public static float[] ipSin(float[] f){
        return sin(f, f);
    }

    public static float[] sin(float[] f, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = (float)Math.sin(f[i]);
        }

        return output;
    }

    public static float[] cos(float[] f){
        return cos(f, new float[f.length]);
    }

    public static float[] ipCos(float[] f){
        return cos(f, f);
    }

    public static float[] cos(float[] f, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = (float)Math.cos(f[i]);
        }

        return output;
    }

    public static float[] tan(float[] f){
        return tan(f, new float[f.length]);
    }

    public static float[] ipTan(float[] f){
        return tan(f, f);
    }

    public static float[] tan(float[] f, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = (float)Math.tan(f[i]);
        }

        return output;
    }

    public static float[][] complex(float[] f){
        return complex(f, new float[f.length][2]);
    }

    public static float[][] complex(float[] f, float[][] output){
        for(int i=0; i<output.length; i++){
            output[i] = ComplexUtils.complex(f[i]);
        }

        return output;
    }

    public static float[] abs(float[] f){
        return abs(f, new float[f.length]);
    }

    public static float[] ipAbs(float[] f){
        return abs(f, f);
    }

    public static float[] abs(float[] f, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = Math.abs(f[i]);
        }

        return output;
    }

    public static float[] abs(float[][] f){
        return abs(f, new float[f.length]);
    }

    public static float[] abs(float[][] f, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = ComplexUtils.abs(f[i]);
        }

        return output;
    }

    public static float[] real(float[][] f){
        return real(f, new float[f.length]);
    }

    public static float[] real(float[][] f, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = ComplexUtils.real(f[i]);
        }

        return output;
    }

    public static float[] imag(float[][] f){
        return imag(f, new float[f.length]);
    }

    public static float[] imag(float[][] f, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = ComplexUtils.imag(f[i]);
        }

        return output;
    }

    public static float[] exp(float[] f){
        return exp(f, new float[f.length]);
    }

    public static float[] ipExp(float[] f){
        return exp(f, f);
    }

    //Performs the exponentiation e^x for every x = f[i]
    public static float[] exp(float[] f, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = (float)Math.pow(Math.E, f[i]);
        }

        return output;
    }

    public static float[][] exp(float[][] f){
        return exp(f, new float[f.length][2]);
    }

    public static float[][] ipExp(float[][] f){
        return exp(f, f);
    }

    //Performs the exponentiation e^x for every x = f[i], where x is a complex number
    public static float[][] exp(float[][] f, float[][] output){
        for(int i=0; i<output.length; i++){
            output[i] = ComplexUtils.exponent(f[i]);
        }

        return output;
    }

    //Calculates the dot product between the two provided arrays
    public static float dotProduct(float[] a, float[] b){
        float dotProduct = 0;
        for(int i=0; i<a.length; i++){
            dotProduct += a[i] * b[i];
        }

        return dotProduct;
    }

    //Calculates the dot product between the two provided complex arrays
    public static float[] dotProduct(float[][] a, float[][] b){
        float[] tempProducts = new float[2];
        float[] dotProduct = ComplexUtils.complex(0);
        for(int i=0; i<a.length; i++){
            ComplexUtils.multiply(a[i], b[i], tempProducts);
            ComplexUtils.ipAdd(dotProduct, tempProducts);
        }

        return dotProduct;
    }

    //Returns the sum of all real numbers in the given array
    public static float reduceSum(float[] f){
        float sum = 0;
        for(float scalar : f){
            sum += scalar;
        }

        return sum;
    }

    //Returns the sum of all complex numbers in the given array
    public static float[] reduceSum(float[][] z){
        float[] sum = new float[] {0.0f, 0.0f};
        for(float[] scalar : z){
            ComplexUtils.ipAdd(sum, scalar);
        }

        return sum;
    }

    //Returns the product of all real numbers in the given array
    public static float reduceProd(float[] f){
        float product = 0;
        for(float scalar : f){
            product *= scalar;
        }

        return product;
    }

    //Returns the product of all complex numbers in the given array
    public static float[] reduceProd(float[][] z){
        float[] product = new float[] {1.0f, 0.0f};
        for(float[] scalar : z){
            ComplexUtils.ipMultiply(product, scalar);
        }

        return product;
    }

    public static int[] concat(int[] ...arrays){
        int totalLength = 0;
        for(int[] array : arrays){
            totalLength += array.length;
        }

        int copyIdx = 0;
        int[] output = new int[totalLength];
        for(int[] array : arrays){
            System.arraycopy(array, 0, output, copyIdx, array.length);
            copyIdx += array.length;
        }

        return output;
    }

    public static float[] concat(float[] ...arrays){
        int totalLength = 0;
        for(float[] array : arrays){
            totalLength += array.length;
        }

        int copyIdx = 0;
        float[] output = new float[totalLength];
        for(float[] array : arrays){
            System.arraycopy(array, 0, output, copyIdx, array.length);
            copyIdx += array.length;
        }

        return output;
    }

    //Pad the given array with zeros; totalPadding is halved to count the zeros on each side
    public static float[] zeroPad(float[] f, int totalPadding){
        float[] output = new float[f.length + totalPadding];
        int startCopy = (int)Math.floor(totalPadding / 2.0);
        System.arraycopy(f, 0, output, startCopy, f.length);
        return output;
    }

    //Pad the given array with zeros; totalPadding is halved to count the zeros on each side
    public static float[][] zeroPad(float[][] f, int totalPadding){
        float[][] output = new float[f.length + totalPadding][2];
        int startCopy = (int)Math.floor(totalPadding / 2.0);
        System.arraycopy(f, 0, output, startCopy, f.length);
        return output;
    }

    public static float[] flip(float[] f){
        float[] output = new float[f.length];
        for(int i=1; i<=f.length; i++){
            output[output.length-i] = f[i-1];
        }

        return output;
    }

    public static float[][] flip(float[][] f){
        float[][] output = new float[f.length][2];
        for(int i=1; i<=f.length; i++){
            output[output.length-i][0] = f[i-1][0];
            output[output.length-i][1] = f[i-1][1];
        }

        return output;
    }

    //pad the given array with it's own reflection
    //i.e. pad([1,2,3,4,5], 2) = [3,2,1,2,3,4,5,4,3]
    public static float[] reflectivePad(float[] f, int totalPadding){
        float[] output = zeroPad(f, 2 * totalPadding);
        float[] left = Arrays.copyOfRange(f, 1, totalPadding + 1);
        float[] right = Arrays.copyOfRange(f, f.length-totalPadding-2, f.length-2);

        float[] leftFlip = flip(left);
        float[] rightFlip = flip(right);
        System.arraycopy(leftFlip, 0, output, 0, leftFlip.length);
        System.arraycopy(rightFlip, 0, output, f.length-rightFlip.length-1, rightFlip.length);
        return output;
    }

    //Performs a full outer padded convolution between the given signal and kernel
    //(i.e. signal is padded by nearly twice the kernel length before convolving)
    public static float[] fullConvolve(float[] signal, float[] kernel){
        float[] paddedSignal = zeroPad(signal, 2 * kernel.length - 1);
        float[] reverseKernel = flip(kernel);

        int outputLength = signal.length + kernel.length - 1;
        float[] output = new float[outputLength];
        for(int i=0; i<outputLength; i++){
            float[] signalSlice = Arrays.copyOfRange(paddedSignal, i, i+kernel.length);
            float[] convMul = multiply(signalSlice, reverseKernel);
            float convSum = reduceSum(convMul);
            output[i] = convSum;
        }

        return output;
    }

    //Performs a full outer padded convolution between the given signal and kernel (both are arrays of complex numbers)
    //(i.e. signal is padded by nearly twice the kernel length before convolving)
    public static float[][] fullConvolve(float[][] signal, float[][] kernel){
        float[][] paddedSignal = zeroPad(signal, 2 * kernel.length - 1);
        float[][] reverseKernel = flip(kernel);

        int outputLength = signal.length + kernel.length - 1;
        float[][] output = new float[outputLength][2];
        for(int i=0; i<outputLength; i++){
            float[][] signalSlice = Arrays.copyOfRange(paddedSignal, i, i+kernel.length);
            float[][] convMul = multiply(signalSlice, reverseKernel);
            float[] convSum = reduceSum(convMul);
            output[i] = convSum;
        }

        return output;
    }

    public static float[] fill(float[] f, float value){
        Arrays.fill(f, value);
        return f;
    }

    public static long[] fill(long[] f, long value){
        Arrays.fill(f, value);
        return f;
    }

    public static int[] downsample(int[] f, int factor){
        return downsample(f, factor, 0);
    }

    public static int[] downsample(int[] f, int factor, int startIdx){
        int[] downsample = new int[f.length / factor - startIdx / factor]; //the size calculation relies on java integer division (i.e. 2/3=0)
        for(int i=0; i<downsample.length; i++){
            downsample[i] = f[startIdx + i*factor];
        }

        return downsample;
    }

    public static float[] downsample(float[] f, int factor){
        return downsample(f, factor, 0);
    }

    public static float[] downsample(float[] f, int factor, int startIdx){
        float[] downsample = new float[f.length / factor - startIdx / factor]; //the size calculation relies on java integer division (i.e. 2/3=0)
        for(int i=0; i<downsample.length; i++){
            downsample[i] = f[startIdx + i*factor];
        }

        return downsample;
    }

    //Applying a row to an array is as easy as running an array copy
    //Applying a column has no built in alternative, so this method handles it.
    public static float[][] applyToColumn(float[][] f, float[] column, int columnIdx){
        if(f.length != column.length){
            throw new IllegalArgumentException ("Array and column must have the same length when calling applyColumn");
        }

        for(int i=0; i<column.length; i++){
            f[i][columnIdx] = column[i];
        }

        return f;
    }

    //Interpolates {numElements - 2} values between {start} and {end}
    public static float[] linspace(float start, float end, int numElements){
        float[] output = new float[numElements];
        float increment = (end - start) / (numElements - 1);

        for(int i=0; i<numElements; i++){
            output[i] = i*increment + start;
        }

        return output;
    }

    //First order difference of the given array. Hope the code is self-explanatory
    public static float[] neighbourDiff(float[] f){
        float[] diff = new float[f.length - 1];
        for(int i=1; i<f.length; i++){
            diff[i-1] = f[i] - f[i-1];
        }

        return diff;
    }

    // Constructs all ordered pairs (a,b) => a in array a and b in array b
    // Then stores the product a-b in an output matrix, and returns it
    public static float[][] outerDiff(float[] a, float[] b){
        float[][] output = new float[a.length][b.length];
        for(int i=0; i<a.length; i++){
            for(int j=0; j<b.length; j++){
                output[i][j] = a[i] - b[j];
            }
        }

        return output;
    }

    //Return the element-wise maximum between the array a and the scalar b, as if the scalar were an array filled with the value b at every index
    public static float[] maximum(float[] a, float b){
        return maximum(a, b, new float[a.length]);
    }

    public static float[] ipMaximum(float[] a, float b){
        return maximum(a, b, a);
    }

    public static float[] maximum(float[] a, float b, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = Math.max(a[i], b);
        }

        return output;
    }

    //Return the element-wise maximum between the two arrays
    public static float[] maximum(float[] a, float[] b){
        return maximum(a, b, new float[a.length]);
    }

    public static float[] ipMaximum(float[] a, float[] b){
        return maximum(a, b, a);
    }

    public static float[] maximum(float[] a, float[] b, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = Math.max(a[i], b[i]);
        }

        return output;
    }

    //Return the element-wise maximum between the array a and the scalar b, as if the scalar were an array filled with the value b at every index
    public static float[][] maximum(float[][] a, float b){
        return maximum(a, b, new float[a.length][a[0].length]);
    }

    public static float[][] ipMaximum(float[][] a, float b){
        return maximum(a, b, a);
    }

    public static float[][] maximum(float[][] a, float b, float[][] output){
        for(int i=0; i<output.length; i++){
            for(int j=0; j<output[0].length; j++){
                output[i][j] = Math.max(a[i][j], b);
            }
        }

        return output;
    }


    //Return the element-wise maximum between the two arrays
    public static float[][] maximum(float[][] a, float[][] b){
        return maximum(a, b, new float[a.length][a[0].length]);
    }

    public static float[][] ipMaximum(float[][] a, float[][] b){
        return maximum(a, b, a);
    }

    public static float[][] maximum(float[][] a, float[][] b, float[][] output){
        for(int i=0; i<output.length; i++){
            for(int j=0; j<output[0].length; j++){
                output[i][j] = Math.max(a[i][j], b[i][j]);
            }
        }

        return output;
    }

    //Return the element-wise minimum between the array a and the scalar b, as if the scalar were an array filled with the value b at every index
    public static float[] minimum(float[] a, float b){
        return minimum(a, b, new float[a.length]);
    }

    public static float[] ipMinimum(float[] a, float b){
        return minimum(a, b, a);
    }

    public static float[] minimum(float[] a, float b, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = Math.min(a[i], b);
        }

        return output;
    }

    //Return the element-wise minimum between the two arrays
    public static float[] minimum(float[] a, float[] b){
        return minimum(a, b, new float[a.length]);
    }

    public static float[] ipMinimum(float[] a, float[] b){
        return minimum(a, b, a);
    }

    public static float[] minimum(float[] a, float[] b, float[] output){
        for(int i=0; i<output.length; i++){
            output[i] = Math.min(a[i], b[i]);
        }

        return output;
    }

    //Return the element-wise minimum between the array a and the scalar b, as if the scalar were an array filled with the value b at every index
    public static float[][] minimum(float[][] a, float b){
        return minimum(a, b, new float[a.length][a[0].length]);
    }

    public static float[][] ipMinimum(float[][] a, float b){
        return minimum(a, b, a);
    }

    public static float[][] minimum(float[][] a, float b, float[][] output){
        for(int i=0; i<output.length; i++){
            for(int j=0; j<output[0].length; j++){
                output[i][j] = Math.min(a[i][j], b);
            }
        }

        return output;
    }

    //Return the element-wise minimum between the two arrays
    public static float[][] minimum(float[][] a, float[][] b){
        return minimum(a, b,  new float[a.length][a[0].length]);
    }

    public static float[][] ipMinimum(float[][] a, float[][] b){
        return minimum(a, b,  a);
    }

    public static float[][] minimum(float[][] a, float[][] b, float[][] output){
        for(int i=0; i<output.length; i++){
            for(int j=0; j<output[0].length; j++){
                output[i][j] = Math.min(a[i][j], b[i][j]);
            }
        }

        return output;
    }

    //Returns the maximum value of the given array
    public static float max(float[] f){
        float max = Float.MIN_VALUE;
        for (float val : f) {
            max = Math.max(val, max);
        }

        return max;
    }

    //Returns the maximum value of the given array
    public static float max(float[][] f){
        float max = Float.MIN_VALUE;
        for (float[] row : f) {
            for (float val : row) {
                max = Math.max(val, max);
            }
        }

        return max;
    }

    //Returns the maximum value of the given array
    public static float min(float[] f){
        float max = Float.MAX_VALUE;
        for (float val : f) {
            max = Math.min(val, max);
        }

        return max;
    }

    //Returns the maximum value of the given array
    public static float min(float[][] f){
        float max = Float.MAX_VALUE;
        for (float[] row : f) {
            for (float val : row) {
                max = Math.min(val, max);
            }
        }

        return max;
    }

    //Returns the index of the maximum value in a given array
    public static int indexOfMax(float[] f){
        float max = -Float.MAX_VALUE; //Why on earth use MAX_VALUE? https://stackoverflow.com/questions/3884793/why-is-double-min-value-in-not-negative
        int idx = -1;

        for(int i=0; i<f.length; i++){
            if(f[i] > max){
                max = f[i];
                idx = i;
            }
        }

        return idx;
    }

    public static float[] meanOfRows(float[][] f){
        float[] means = new float[f.length];
        for(int i=0; i<f.length; i++){
            float[] arr = f[i];
            means[i] = reduceSum(arr) / arr.length;
        }

        return means;
    }

    public static float[] meanOfCols(float[][] f){
        float[] means = new float[f[0].length];
        for(int i=0; i<f[0].length; i++){
            float verticalSum = 0;
            for (float[] samples : f) {
                verticalSum += samples[i];
            }

            means[i] = verticalSum / f.length;
        }

        return means;
    }

    public static float[] stdOfRows(float[][] f){
        return stdOfRows(f, meanOfRows(f));
    }

    public static float[] stdOfRows(float[][] f, float[] means){
        float[] deviations = new float[f.length];
        for(int i=0; i<f.length; i++){
            float[] arr = f[i];
            float arrMean = means[i];

            float[] minusMean = subtract(arr, arrMean);
            float squaredMean = dotProduct(minusMean, minusMean);
            float dividedByLen = squaredMean / (arr.length - 1); //applying Bessel's correction
            deviations[i] = (float)Math.sqrt(dividedByLen);
        }

        return deviations;
    }

    public static float[] stdOfCols(float[][] f){
        return stdOfCols(f, meanOfCols(f));
    }

    public static float[] stdOfCols(float[][] f, float[] means){
        float[] deviations = new float[f[0].length];
        for(int i=0; i<f[0].length; i++){
            float squaredMean = 0;
            for (float[] samples : f) {
                float minusMean = (samples[i] - means[i]);
                squaredMean += minusMean * minusMean;
            }

            float dividedByLen = squaredMean / (f.length - 1); //applying Bessel's correction
            deviations[i] = (float)Math.sqrt(dividedByLen);
        }

        return deviations;
    }
}
