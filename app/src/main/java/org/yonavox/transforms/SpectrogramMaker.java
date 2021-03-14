package org.yonavox.transforms;

import org.yonavox.util.ArrayUtils;
import org.yonavox.util.ComplexUtils;
import org.yonavox.util.Constants;

import java.util.ArrayDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//Handles anything to do with spectrogram generation
public class SpectrogramMaker {
    //Lookup tables
    private final int[] shuffledIndices;
    private final int[][][] pairIndexLookup;
    private final float[][] eCoefficients;
    private final float[] fftWindow;

    //Constants
    private final int frameLength;
    private final int hopLength;
    private final int fftIterationLimit;

    //Pre-allocated arrays
    private final float[] kIndices; //linearly interpolated start+end indices, splitting the frames into batches
    private final float[][] frames; //The signal frames, after extraction, used in-place for windowing
    private final float[][] fftMultiplications; //holds the result of the computation eCoef * rightArg (in the fft method)
    private final float[][][] fftComputations; //holds the result of the computation leftArg + eCoef * rightArg (in the fft method)
    private final float[][][] fftStepResults; //holds the results of the previous fft step
    private final float[][] spectrogram; //The holy grail

    //Concurrency settings
    private static final int threadCount = 8;
    private final Executor threadPool = Executors.newFixedThreadPool(threadCount);

    private SpectrogramMaker(int frameLength, int hopLength, int[] shuffledIndices, int[][][] pairIndexLookup, float[][] eCoefficients, float[] fftWindow) {
        this.frameLength = frameLength;
        this.hopLength = hopLength;
        this.fftIterationLimit = (int)(Math.log(frameLength) / Math.log(2));

        this.shuffledIndices = shuffledIndices;
        this.pairIndexLookup = pairIndexLookup;
        this.eCoefficients = eCoefficients;
        this.fftWindow = fftWindow;

        int hopCount = 1 + (Constants.SPECTROGRAM_TIMESTAMPS - this.frameLength) / this.hopLength;
        this.kIndices = ArrayUtils.linspace(0, hopCount, threadCount + 1);
        this.frames = new float[hopCount][this.frameLength];
        this.fftMultiplications = new float[threadCount][2];
        this.fftComputations = new float[threadCount][this.frameLength][2];
        this.fftStepResults = new float[threadCount][this.frameLength][2];
        this.spectrogram = new float[hopCount][this.frameLength / 2 + 1];
    }

    public static SpectrogramMaker build(int frameLength, int hopLength){
        int maxOrder = (int)(Math.log(frameLength) / Math.log(2));

        float[] fftWindow = createHannWindow(frameLength);
        int[] shuffledIndices = createShuffledIndices(frameLength);
        int[][][] pairIndexLookup = generateIndexPairs(frameLength, maxOrder);
        float[][] eCoefficients = getECoefficients(frameLength);

        return new SpectrogramMaker(frameLength, hopLength, shuffledIndices, pairIndexLookup, eCoefficients, fftWindow);
    }

    //Strictly non-concurrent! concurrent invocations will corrupt all running calculations
    //Creates a spectrogram from the given signal using the Short-time Fourier Transform
    //The spectrogram returned is the transpose of the standard shape, as the recognition model input is a transposed spectrogram
    public synchronized float[][] transform(float[] signal){
        float[] paddedSignal = ArrayUtils.reflectivePad(signal, this.frameLength / 2);

        CountDownLatch latch = new CountDownLatch(threadCount);
        for(int threadId=0; threadId<threadCount; threadId++) {
            int finalThreadId = threadId;
            threadPool.execute(() -> {
                //Define the range of frames the current TPE worker is responsible for
                int startFrameIdx = (int)Math.floor(kIndices[finalThreadId]);
                int endFrameIdx = (int)Math.floor(kIndices[finalThreadId+1]);
                long start, end;
                long paddedSignalCopies = 0;
                long windowing = 0;
                long frameShuffling = 0;
                long fftCalls = 0;
                long absValues = 0;

                for(int k=startFrameIdx; k<endFrameIdx; k++){
                    //Extract the k'th frame from the padded signal
                    System.arraycopy(paddedSignal, k*this.hopLength, frames[k], 0, this.frameLength);

                    //Window the frame by multiplying with the window array
                    ArrayUtils.ipMultiply(frames[k], fftWindow);

                    //Shuffle the windowed frame to reorder the frame for the FFT invocation
                    float[] shuffledFrame = shuffleFrame(frames[k]);

                    //Obtain the FFT coefficients of the frame and save their absolute values (complex numbers)
                    fastFourierTransform(shuffledFrame, finalThreadId);

                    //The results are mirrored around the center, meaning we can discard the latter half of the FFT coefficients
                    //The shorter output array spectrogram[k] will ensure ArrayUtils.abs() does that automatically
                    ArrayUtils.abs(fftStepResults[finalThreadId], spectrogram[k]);
                }

                //Signal the main thread this batch has finished
                latch.countDown();
            });
        }

        try {
            latch.await();
            return spectrogram;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //Shuffles the given frame using the index order provided
    private float[] shuffleFrame(float[] frame){
        float[] shuffledFrame = new float[frame.length];
        for(int i=0; i<frame.length; i++){
            shuffledFrame[i] = frame[this.shuffledIndices[i]];
        }

        return shuffledFrame;
    }


    /* Performs the fast fourier transform on a single frame vector
     * The standard textbook implementations of FFT use recursion, but the call stack can get quite large for long signals which reduces performance.
     * That's why I opted for a non recursive implementation (breadth-first, using a for-loop)
     * A non-recursive implementation can be made (as written here, for example).
     * The recursive version of this algorithm is explained in https://en.wikipedia.org/wiki/Cooley%E2%80%93Tukey_FFT_algorithm*/
    private void fastFourierTransform (float[] shuffledFrame, int workerId){
        //Cast frame to complex numbers
        ArrayUtils.complex(shuffledFrame, fftStepResults[workerId]);
        int eSpacing = frameLength;

        //Iterate over the signal log2(n) times, where n = len(shuffledFrame)
        for(int order = 0; order< fftIterationLimit; order++){
            //Extract the constants for the current iteration of the outer loop
            int[][] currentLookup = this.pairIndexLookup[order];
            eSpacing *= 0.5;

            //Iterate over the index pairs in currentLookup
            for(int pairIdx=0; pairIdx<currentLookup.length; pairIdx++){
                //Extract the required indices from the index lookup and from the e lookup
                int[] currentPair = currentLookup[pairIdx];
                int leftIdx = currentPair[0];
                int rightIdx = currentPair[1];
                int eIdx = (pairIdx*eSpacing) % shuffledFrame.length;

                //And use them to obtain the correct arguments for the calculation
                float[] leftArg = fftStepResults[workerId][leftIdx];
                float[] rightArg = fftStepResults[workerId][rightIdx];
                float[] eCoefficient = this.eCoefficients[eIdx];

                //The computation itself is quite simple: left + e*right
                ComplexUtils.add(
                    leftArg,
                    ComplexUtils.multiply(eCoefficient, rightArg, fftMultiplications[workerId]),
                    fftComputations[workerId][pairIdx]
                );
            }

            //Set the results for the current step (of the outer loop)
            for(int i = 0; i< fftComputations[workerId].length; i++){
                System.arraycopy(fftComputations[workerId][i], 0, fftStepResults[workerId][i], 0, 2);
            }
        }
    }

    private static float[] createHannWindow(int frameLength){
        float[] window = new float[frameLength];
        for(int i=0; i<window.length; i++){
            window[i] = 0.5f - 0.5f * (float)(Math.cos((2*Math.PI * i) / frameLength));
        }

        return window;
    }

    /* One of the steps for implementing a non-recursive FFT is an index shuffle of the input.
     * The shuffle can be achieved using bit-level hacking, or the algorithm below.
     * Because the index shuffle is only calculated once (all signals will be of the same length)
     * It can be stored somewhere in the mobile app, without needing to calculate it again.
     * That's why I chose this algorithm for clarity (over the bit-level equivalent)*/
    private static int[] createShuffledIndices(int frameLength){
        //Initialize the index orderng to the natural ordering [0, 1, 2, ...]
        int[] naturalOrdering = new int[frameLength];
        for(int i=0; i<frameLength; i++){
            naturalOrdering[i] = i;
        }

        //Create a queue to hold the current order, starting with the natural index order
        //The queue mimics a jagged array, with arrays getting shorter all the time
        int iterationLimit = (int)(Math.log(frameLength) / Math.log(2)) - 1;
        ArrayDeque<int[]> shuffledFrame = new ArrayDeque<>();
        shuffledFrame.addLast(naturalOrdering);

        //Run for {iterationLimit} iterations
        for(int iter=0; iter<iterationLimit; iter++){
            int shuffledCoefficients = 0;
            while(shuffledCoefficients < frameLength){
                //In each iteration we pop the first element, and split it by the even and odd indices of the subrame (frame idx, not frame value)
                int[] currentSubframe = shuffledFrame.removeFirst();
                int[] evenIndices = ArrayUtils.downsample(currentSubframe, 2, 0);
                int[] oddIndices = ArrayUtils.downsample(currentSubframe, 2, 1);

                //We then push both index arrays to the queue
                shuffledCoefficients += currentSubframe.length;
                shuffledFrame.addLast(evenIndices);
                shuffledFrame.addLast(oddIndices);
            }
        }

        //By this stage the array has been fully shuffled
        //(when the arrays in the queue are of size 2, additional shuffles do not change the order of the values across all arrays)
        //We can concat the arrays and return them
        return ArrayUtils.concat(shuffledFrame.toArray(new int[0][0]));
    }

    /* In the non-recursive FFT, we perform multiplications between elements at certain indices at every iteration of the main loop body
     * Since the signals are of the same length, we can pre-calculate the index lookup
     *
     * when n = windowLength
     * Dimension 0 corresponds to the k-th iteration of the main loop (of which there are log(n) iterations)
     * Dimension 1 corresponds to the l-th  lookup call (of which there are n calls every loop iteration)
     * Dimension 2 corresponds to the signal indices to retrieve (two indices per lookup invocation)

     * This generation method uses four nested loops, but is still O(n) in time complexity*/
    private static int[][][] generateIndexPairs(int frameLength, int maxOrder){
        int[][][] indexPairs = new int[maxOrder][frameLength][2];

        for(int order=0; order<maxOrder; order++){
            int pairsPerGroup = (int)Math.pow(2, order);
            int groupsPerIdxTable = (int)(0.5 * frameLength / pairsPerGroup);
            int pairIdx = 0;

            for(int groupIdx=0; groupIdx<groupsPerIdxTable; groupIdx++){
                for(int foo=0; foo<2; foo++){
                    for(int ppgIdx=0; ppgIdx<pairsPerGroup; pairIdx++, ppgIdx++){
                        indexPairs[order][pairIdx][0] = 2*pairsPerGroup*groupIdx + ppgIdx;
                        indexPairs[order][pairIdx][1] = 2*pairsPerGroup*groupIdx + pairsPerGroup + ppgIdx;
                    }
                }
            }
        }

        return indexPairs;
    }

    //Generates {amount} complex numbers, evenly spaced along the unit circle in the complex plane
    private static float[][] getECoefficients(int amount){
        float[][] exponents = new float[amount][2];
        for(int i=0; i<amount; i++){
            exponents[i] = new float[]{0, -2*(float)Math.PI*i/amount};
        }

        return ArrayUtils.ipExp(exponents);
    }
}
