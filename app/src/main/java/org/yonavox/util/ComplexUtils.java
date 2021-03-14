package org.yonavox.util;

/*Complex numbers are most commonly implemented in Java using a class (OOP language, after all).
* However, creating and destroying (i.e. Garbage collecting) hundreds of thousands of class instances every second
* as if they were primitive types would cause a big overhead.
*
* For live audio data streaming every millisecond counts.
* Therefore I opted for an array-based implementation:
* each complex number of the form a + bi is represented with the array [a, b]
*
* pros: speed (!)
* cons: no type safety or checks*/
public class ComplexUtils {
    //Casts the real number f to the complex number f + 0i
    public static float[] complex(float f) {
        return new float[] {f, 0.0f};
    }

    //Calculate the sum a + b, and returns the result as a new complex number
    public static float[] add(float[] a, float b) {
        return add(a, b, new float[2]);
    }

    //Calculate the sum a + b, and saves the sum over the first number
    public static float[] ipAdd(float[] a, float b) {
        return add(a, b, a);
    }

    //Calculate the sum a + b, and saves the sum to {result}
    public static float[] add(float[] a, float b, float[] result) {
        result[0] = a[0] + b;
        result[1] = a[1];
        return result;
    }

    //Calculate the sum a + b, and returns the result as a new complex number
    public static float[] add(float[] a, float[] b) {
        return add(a, b, new float[2]);
    }

    //Calculate the sum a + b, and saves the sum over the first number
    public static float[] ipAdd(float[] a, float[] b) {
        return add(a, b, a);
    }

    //Calculate the sum a + b, and saves the sum to {result}
    public static float[] add(float[] a, float[] b, float[] result) {
        result[0] = a[0] + b[0];
        result[1] = a[1] + b[1];
        return result;
    }

    //Calculate the difference a - b, and returns the result as a new complex number
    public static float[] subtract(float[] a, float b) {
        return subtract(a, b, new float[2]);
    }

    //Calculate the difference a - b, and saves the difference over the first number
    public static float[] ipSubtract(float[] a, float b) {
        return subtract(a, b, a);
    }

    //Calculate the difference a - b, and saves the difference to {result}
    public static float[] subtract(float[] a, float b, float[] result) {
        result[0] = a[0] - b;
        result[1] = a[1];
        return result;
    }

    //Calculate the difference a - b, and returns the result as a new complex number
    public static float[] subtract(float[] a, float[] b) {
        return subtract(a, b, new float[2]);
    }

    //Calculate the difference a - b, and saves the difference over the first number
    public static float[] ipSubtract(float[] a, float[] b) {
        return subtract(a, b, a);
    }

    //Calculate the difference a - b, and saves the difference to {result}
    public static float[] subtract(float[] a, float[] b, float[] result) {
        result[0] = a[0] - b[0];
        result[1] = a[1] - b[1];
        return result;
    }

    //Calculate the product a * b, and returns the result as a new complex number
    public static float[] multiply(float[] a, float b) {
        return multiply(a, b, new float[2]);
    }

    //Calculate the product a * b, and saves the product over the first number
    public static float[] ipMultiply(float[] a, float b) {
        return multiply(a, b, a);
    }

    //Calculate the product a * b, and saves the product to {result}
    public static float[] multiply(float[] a, float b, float[] result) {
        result[0] = a[0] * b;
        result[1] = a[1] * b;
        return result;
    }

    //Calculate the product a * b, and returns the result as a new complex number
    public static float[] multiply(float[] a, float[] b) {
        return multiply(a, b, new float[2]);
    }

    //Calculate the product a * b, and saves the product over the first number
    public static float[] ipMultiply(float[] a, float[] b) {
        return multiply(a, b, a);
    }

    //Calculate the product a * b, and saves the product to {result}
    public static float[] multiply(float[] a, float[] b, float[] result) {
        float r0 = a[0] * b[0] - a[1] * b[1];
        float r1 = a[0] * b[1] + a[1] * b[0];
        result[0] = r0;
        result[1] = r1;
        return result;
    }

    //Calculate the quotient a / b, and returns the result as a new complex number
    public static float[] divide(float[] a, float b) {
        return divide(a, b, new float[2]);
    }

    //Calculate the quotient a / b, and saves the product over the first number
    public static float[] ipDivide(float[] a, float b) {
        return divide(a, b, a);
    }

    //Calculate the quotient a / b, and saves the product to {result}
    public static float[] divide(float[] a, float b, float[] result) {
        result[0] = a[0] / b;
        result[1] = a[1] / b;
        return result;
    }

    //Calculate the quotient a / b, and returns the result as a new complex number
    public static float[] divide(float[] a, float[] b) {
        return divide(a, b, new float[2]);
    }

    //Calculate the quotient a / b, and saves the product over the first number
    public static float[] ipDivide(float[] a, float[] b) {
        return divide(a, b, a);
    }

    //Calculate the quotient a / b, and saves the product to {result}
    public static float[] divide(float[] a, float[] b, float[] result) {
        float partialReciprocal = 1 / (b[0] * b[0] + b[1] * b[1]);

        float r0 = partialReciprocal * (a[0]*b[0] + a[1]*b[1]);
        float r1 = partialReciprocal * (a[1]*b[0] - a[0]*b[1]);
        result[0] = r0;
        result[1] = r1;
        return result;
    }

    //Performs the exponentiation e^z using Euler's identity
    public static float[] exponent(float[] z) {
        double realExponent = Math.pow(Math.E, z[0]);
        float cos = (float)(realExponent * Math.cos(z[1]));
        float sin = (float)(realExponent * Math.sin(z[1]));
        return new float[] {cos, sin};
    }

    //Returns the real part of the complex number z
    public static float real(float[] z) {
        return z[0];
    }

    //Returns the imaginary part of the complex number z
    public static float imag(float[] z) {
        return z[1];
    }

    public static float abs(float[] z) {
        double selfNorm = Math.pow(z[0], 2) + Math.pow(z[1], 2);
        return (float)Math.sqrt(selfNorm);
    }
}
