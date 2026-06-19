/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * various mathematics utilities. The functions do not exist in the basic math package Math.*
 *
 * @author Charles Prud'homme, Arnaud Malapert
 * @version 2.0.1
 * @since 8 dec. 2008 version 2.0.1</br>
 */
public final class MathUtils {

    public static final int MIN_VALUE = Integer.MIN_VALUE + 1;
    public static final int MAX_VALUE = Integer.MAX_VALUE - 1;

    /**
     * Precision for rounded logarithm.
     */
    public static final double ROUNDED_LOG_PRECISION = 10000;

    public static final double LOG10_2 = Math.log10(2);

    private MathUtils() {
        //do nothing
    }

    /**
     * simple recursive version of factorial
     *
     * @param n size of the suite
     * @return n!
     */
    public static long factorial(int n) {
        return n < 2 ? 1 : n * factorial(n - 1);
    }

    /**
     * it computes the number of combinaison C_n^p.
     * The function is only recursive and do not use an array to store temporary results
     *
     * @param n max cardinality
     * @param p sub cardinality
     * @return n among k combinations
     */
    public static int combinaison(int n, int p) {
        if (n == p) {
            return 1;
        } else if (p == 0) {
            return 1;
        } else if (p == 1) {
            return n;
        } else {
            return combinaison(n - 1, p) + combinaison(n - 1, p - 1);
        }
    }

    /**
     * @param x a value
     * @return <tt>true</tt> if <i>x</i> is power of 2.
     */
    public static boolean isPowerOfTwo(int x) {
        return (x & (x - 1)) == 0;
    }

    /**
     * Returns the value of the first argument raised to the power of the
     * second argument. See {@link Math#pow(double, double)} for special cases.
     *
     * @param value value
     * @param exp   exponent
     * @return the power result
     */
    public static int pow(int value, int exp) {
        return value == 2 && exp >= 0 ? 1 << exp : (int) Math.pow(value, exp);
    }

    public static double log(double value, double exponent) {
        return Math.log(value) / Math.log(exponent);
    }

    /**
     * a rounded logarithm to avoid issues with jvm dependant math functions
     */
    public static double roundedLog(double value, double exponent) {
        return Math.round(log(value, exponent) * ROUNDED_LOG_PRECISION) / ROUNDED_LOG_PRECISION;
    }

    /**
     * Returns the sum of elements in <i>values</i> from position <i>begin</i> (inclusive) to position <i>end</i> (exclusive).
     *
     * @param values array of ints
     * @param begin  starting position (inclusive)
     * @param end    ending position (exclusive)
     * @return the sum of elements in <i>values</i> from position <i>begin</i> (inclusive) to position <i>end</i> (exclusive).
     */
    public static int sum(int[] values, int begin, int end) {
        int s = 0;
        for (int i = begin; i < end; i++) {
            s += values[i];
        }
        return s;
    }

    /**
     * Returns the sum of elements in <i>values</i> from position <i>begin</i> (inclusive) to values.length.
     *
     * @param values array of ints
     * @param begin  starting position (inclusive)
     * @return the sum of elements in <i>values</i> from position <i>begin</i> (inclusive) to values.length.
     */
    public static int sumFrom(int[] values, int begin) {
        return sum(values, begin, values.length);
    }

    /**
     * Returns the sum of elements in <i>values</i> from position <i>0</i> (inclusive) to position <i>end</i> (exclusive).
     *
     * @param values array of ints
     * @param end    ending position (exclusive)
     * @return the sum of elements in <i>values</i> from position <i>0</i> (inclusive) to position <i>end</i> (exclusive).
     */
    public static int sumTo(int[] values, int end) {
        return sum(values, 0, end);
    }

    /**
     * Returns the sum of elements in <i>values</i>.
     *
     * @param values array of ints
     * @return the sum of elements in <i>values</i>.
     */
    public static int sum(int[] values) {
        return sum(values, 0, values.length);
    }

    /**
     * Retuns the sum of elements in <i>values</i>.
     *
     * @param values matrix of ints
     * @return the sum of elements in <i>values</i>.
     */
    public static int sum(int[][] values) {
        int s = 0;
        for (int[] value : values) {
            for (int j = 0; j < value.length; j++) {
                s += value[j];
            }
        }
        return s;
    }

    /**
     * Returns the element with the greatest value in <i>values</i>.
     *
     * @param values array of ints
     * @return the element with the greatest value in <i>values</i>.
     */
    public static int max(int[] values) {
        int max = Integer.MIN_VALUE;
        for (int value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /**
     * Returns the element with the greatest value in <i>values</i>.
     *
     * @param values array of ints
     * @return the element with the greatest value in <i>values</i>.
     */
    public static int max(int[][] values) {
        int max = Integer.MIN_VALUE;
        for (int[] value : values) {
            for (int j = 0; j < value.length; j++) {
                if (value[j] > max) {
                    max = value[j];
                }
            }
        }
        return max;
    }

    /**
     * Returns the element with the smallest value in <i>values</i>.
     *
     * @param values array of ints
     * @return the element with the smallest value in <i>values</i>.
     */
    public static int min(int[] values) {
        int min = Integer.MAX_VALUE;
        for (int value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    /**
     * Returns the element with the smallest value in <i>values</i>.
     *
     * @param values array of ints
     * @return the element with the smallest value in <i>values</i>.
     */
    public static int min(int[][] values) {
        int min = Integer.MAX_VALUE;
        for (int[] value : values) {
            for (int j = 0; j < value.length; j++) {
                if (value[j] < min) {
                    min = value[j];
                }
            }
        }
        return min;
    }

    /**
     * Returns the largest (closest to positive infinity) {@code int} value that is less than or equal to a/b.
     * Adapted from {@link Math#floorDiv(int, int)}.
     *
     * @param x the dividend
     * @param y the divisor
     * @return the largest (closest to positive infinity) {@code int} value that is less than or equal to a/b.
     */
    public static int divFloor(int x, int y) {
        if (y == 0) {
            return Integer.MAX_VALUE;
        } else {
            return Math.floorDiv(x, y);
        }
    }

    /**
     * Returns the smallest (closest to positive infinity) {@code int} value that is greater or equal to a/b.
     * Adapted from {@link Math#floorDiv(int, int)}.
     *
     * @param x the dividend
     * @param y the divisor
     * @return the smallest (closest to positive infinity) {@code int} value that is greater or equal to a/b.
     */
    public static int divCeil(int x, int y) {
        if (y == 0) {
            return Integer.MIN_VALUE;
        } else {
            int r = x / y;
//            // if the signs are the same and modulo not zero, round up
            if ((x ^ y) > 0 && (r * y != x)) {
                r++;
            }
            return r;
        }
    }

    /**
     * Returns the sum of its arguments,
     * returning either {@link Integer#MAX_VALUE} if the result overflows an {@code int},
     * or {@link Integer#MIN_VALUE} if the result underflows an {@code int}, .
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     */
    public static int safeAdd(int x, int y) {
        return safeCast((long) x + (long) y);
    }

    /**
     * Returns the difference of its arguments,
     * returning either {@link Integer#MAX_VALUE} if the result overflows an {@code int},
     * or {@link Integer#MIN_VALUE} if the result underflows an {@code int}, .
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     */
    public static int safeSubstract(int x, int y) {
        return safeCast((long) x - (long) y);
    }

    /**
     * Returns the product of its arguments,
     * returning either {@link Integer#MAX_VALUE} if the result overflows an {@code int},
     * or {@link Integer#MIN_VALUE} if the result underflows an {@code int}, .
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     */
    public static int safeMultiply(int x, int y) {
        return safeCast((long) x * (long) y);
    }

    /**
     * @param x long to cast
     * @return the closest int value when safe casting a long into an int
     */
    public static int safeCast(long x) {
        if (x > MAX_VALUE) {
            return MAX_VALUE;
        }
        if (x < MIN_VALUE) {
            return MIN_VALUE;
        }
        return (int) x;
    }

    /**
     * Compute a safe sum of integers
     *
     * @param values integers to sum
     * @return the sum of values bounded to [MIN_VALUE, MAX_VALUE] to avoid integer overflows
     */
    public static int safeSum(int[] values) {
        return safeSum(Arrays.stream(values));
    }

    /**
     * Compute a safe sum of integers
     *
     * @param stream integers to sum
     * @return the sum of stream bounded to [MIN_VALUE, MAX_VALUE] to avoid integer overflows
     */
    public static int safeSum(IntStream stream) {
        return safeSum(stream.mapToLong(i -> (long) i));
    }

    /**
     * Compute a safe sum of longs with an integer result
     *
     * @param stream long values to sum
     * @return the sum of stream bounded to [MIN_VALUE, MAX_VALUE] to avoid integer overflows
     */
    public static int safeSum(LongStream stream) {
        return safeCast(stream.sum());
    }

    public static double log2(double a) {
        return Math.log10(a) / LOG10_2;
    }

    /**
     * @param n a long value
     * @return <code>true</code> if <code>n</code> is a perfect square, <code>false</code> otherwise.
     */
    public static boolean isPerfectSquare(long n) {
        if (n < 0) {
            return false;
        }
		return switch ((int) (n & 0xF)) {
		    case 0, 1, 4, 9 -> {
				long tst = (long) Math.sqrt(n);
				yield tst * tst == n;
			}
		    default -> false;
	    };
    }

}
