/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.util.tools;

import java.awt.*;


/**
 * various mathematics utilities. The functions do not exist in the basic math package Math.*
 *
 * @author Charles Prud'homme, Arnaud Malapert
 * @version 2.0.1
 * @since 8 dec. 2008 version 2.0.1</br>
 */
public final class MathUtils {

    /**
     * Precision for rounded logarithm.
     */
    public final static double ROUNDED_LOG_PRECISION = 10000;

    private MathUtils() {
        //do nothing
    }

    /**
     * simple recursive version of factorial
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
     * @param value
     * @param exp
     * @return
     */
    public static int pow(int value, int exp) {
        return value == 2 ? 1 << exp : (int) Math.pow(value, exp);
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
     * @param values array of ints
     * @param begin starting position (inclusive)
     * @param end ending position (exclusive)
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
     * @param values array of ints
     * @param begin starting position (inclusive)
     * @return the sum of elements in <i>values</i> from position <i>begin</i> (inclusive) to values.length.
     */
    public static int sumFrom(int[] values, int begin) {
        return sum(values, begin, values.length);
    }

    /**
     * Returns the sum of elements in <i>values</i> from position <i>0</i> (inclusive) to position <i>end</i> (exclusive).
     * @param values array of ints
     * @param end ending position (exclusive)
     * @return the sum of elements in <i>values</i> from position <i>0</i> (inclusive) to position <i>end</i> (exclusive).
     */
    public static int sumTo(int[] values, int end) {
        return sum(values, 0, end);
    }

    /**
     * Returns the sum of elements in <i>values</i>.
     * @param values array of ints
     * @return the sum of elements in <i>values</i>.
     */
    public static int sum(int[] values) {
        return sum(values, 0, values.length);
    }

    /**
     * Retuns the sum of elements in <i>values</i>.
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

    public static Point bounds(int[] values) {
        if (values == null || values.length == 0) {
            return new Point(Integer.MAX_VALUE, Integer.MIN_VALUE);
        } else {
            final Point b = new Point(values[0], values[0]);
            for (int i = 1; i < values.length; i++) {
                if (values[i] < b.x) {
                    b.x = values[i];
                } else if (values[i] > b.y) {
                    b.y = values[i];
                }
            }
            return b;
        }
    }

    /**
     * Returns the largest (closest to positive infinity) {@code int} value that is less than or equal to a/b.
     * Adapted from {@link Math#floorDiv(int, int)}.
     * @param x the dividend
     * @param y the divisor
     * @return the largest (closest to positive infinity) {@code int} value that is less than or equal to a/b.
     */
    public static int divFloor(int x, int y) {
        if (y == 0) {
            return Integer.MAX_VALUE;
        } else {
            int r = x / y;
            // if the signs are different and modulo not zero, round down
            if ((x ^ y) < 0 && (r * y != x)) {
                r--;
            }
            return r;
        }
    }

    /**
     * Returns the smallest (closest to positive infinity) {@code int} value that is greater or equal to a/b.
     * Adapted from {@link Math#floorDiv(int, int)}.
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
    public static int safeAdd(int x, int y){
        int r = x + y;
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if (((x ^ r) & (y ^ r)) < 0) {
            long rr = (long)x + y;
            return rr > 0 ? Integer.MAX_VALUE:Integer.MIN_VALUE;
        }
        return r;
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
    public static int safeSubstract(int x, int y){
        int r = x - y;
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if (((x ^ y) & (x ^ r)) < 0) {
            long rr = (long)x - y;
            return rr > 0 ? Integer.MAX_VALUE:Integer.MIN_VALUE;
        }
        return r;
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
    public static int safeMultiply(int x, int y){
        long r = (long)x * (long)y;
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if ((int)r != r) {
            return r > 0 ? Integer.MAX_VALUE:Integer.MIN_VALUE;
        }
        return (int)r;
    }

    /**
     * @param x long to cast
     * @return the closest int value when safe casting a long into an int
     */
    public static int safeCast(long x){
        if(x > Integer.MAX_VALUE)return Integer.MAX_VALUE;
        if(x < Integer.MIN_VALUE)return Integer.MIN_VALUE;
        return (int) x;
    }

}
