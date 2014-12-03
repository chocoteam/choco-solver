/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.util.tools;

import java.awt.*;


/**
 * various mathematics utilities. The functions do not exist in the basic math package Math.*
 *
 * @author Arnaud Malapert</br>
 * @version 2.0.1</br>
 * @since 8 dec. 2008 version 2.0.1</br>
 */
public final class MathUtils {

    public final static double ROUNDED_LOG_PRECISION = 10000;

    /**
     *
     */
    private MathUtils() {
        //do nothing
    }

    /**
     * simple recursive version of factorielle
     */
    public static long factoriel(int n) {
        return n < 2 ? 1 : n * factoriel(n - 1);
    }

    /**
     * it computes the number of combinaison C_n^p.
     * The function is oonly recursive and do not use an array to store temporary results
     *
     * @param n
     * @param p
     * @return
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

    public static boolean isPowerOfTwo(int x) {
        return (x & (x - 1)) == 0;
    }

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

    public static int sum(int[] values, int begin, int end) {
        int s = 0;
        for (int i = begin; i < end; i++) {
            s += values[i];
        }
        return s;
    }

    public static int sumFrom(int[] values, int begin) {
        return sum(values, begin, values.length);
    }

    public static int sumTo(int[] values, int end) {
        return sum(values, 0, end);
    }

    public static int sum(int[] values) {
        return sum(values, 0, values.length);
    }

    public static int sum(int[][] values) {
        int s = 0;
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                s += values[i][j];
            }
        }
        return s;
    }

    public static int max(int[] values) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    public static int max(int[][] values) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                if (values[i][j] > max) {
                    max = values[i][j];
                }
            }
        }
        return max;
    }

    public static int min(int[] values) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    public static int min(int[][] values) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                if (values[i][j] < min) {
                    min = values[i][j];
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

    public static int divFloor(int a, int b) {
        if (b < 0) {
            return divFloor(-a, -b);
        } else if (b > 0) {
            if (a >= 0) {
                return (a / b);
            } else {
                return (a - b + 1) / b;
            }
        } else {
            assert (b == 0);
        }
        return Integer.MAX_VALUE;
    }

    public static int divCeil(int a, int b) {
        if (b < 0) {
            return divCeil(-a, -b);
        } else if (b > 0) {
            if (a >= 0) {
                return ((a + b - 1) / b);
            } else {
                return a / b;
            }
        } else {
            assert (b == 0);
        }
        return Integer.MIN_VALUE;
    }
}
