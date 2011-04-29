/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package solver.probabilities;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: xlorca
 *
 * TODO duplicate with {@code choco.kernel.common.util.tools.StatisticUtils}
 */
public class ProbaUtils {

    protected ProbaUtils() {}

    public static int sum(int[] data) {
        int sum = 0;
        for (int i = data.length - 1; i > -1; i--) {
            sum += data[i];
        }
        return sum;
    }

    public static int prod(int[] data) {
        int prod = 1;
        for (int i = data.length - 1; i > -1; i--) {
            prod *= data[i];
        }
        return prod;
    }

    public static double sum(double[] data) {
        double sum = 0;
        for (int i = data.length - 1; i > -1; i--) {
            sum += data[i];
        }
        return sum;
    }

    public static double prod(double[] data) {
        double prod = 1.0;
        for (int i = data.length - 1; i > -1; i--) {
            prod *= data[i];
        }
        return prod;
    }

    public static double fact(long n) {
        double res = 1;
        while (n > 0) {
            res *= n;
            n = n - 1;
        }
        return res;
    }

    public static double coefBinome(long n, long p) {
        if (p == 0) {
            return 1;
        }
        if ((n < 0) || (p < 0) || (p > n)) {
            return 0;
        }
        double res = 1;
        for (long i = n - p + 1; i <= n; i++) {
            res *= (double) i;
        }
        res /= fact(p);
        return res;
    }

    // m nombre totale de boules dans l'urne
    // m1 (nombre de boules gagnantes)
    // n nombre de tirages
    // k parametre de la loi
    public static double hyperGeometric(long m, long m1, long n, int k) {
        return (coefBinome(m1, k) * coefBinome(m - m1, n - k)) / coefBinome(m, n);
    }

    // p1 probabilitŽs de gagner pour chaque loi de Bernoulli (|p1| = n)
    // p2 probabilitŽs de perdre pour chaque loi de Bernoulli (|p2| = n)
    // n nombre de loi de Bernoulli, n > 0
    // k parametre de la loi
    public static double[][] generalizedNewton(double[] p1, double[] p2, int n, int k) {
        double[][] p = new double[n][k + 1];
        p[0][0] = p2[0];
        for (int j = 0; j < n; j++) {
            if (j == 0) {
                for (int i = 1; i <= k; i++) {
                    //p[j][i] = 1;
                    p[j][i] = p1[0] + p2[0];
                }
            } else {
                for (int i = 0; i <= k; i++) {
                    if (i == 0) {
                        p[j][i] = p2[j - 1] * p[j - 1][i];
                    } else {
                        p[j][i] = p1[j - 1] * p[j - 1][i - 1] + p2[j - 1] * p[j - 1][i];
                    }
                }
            }
        }
        return p;//p[n - 1][k];
    }


    public static double[][] generalizedNewtonBis(double[] p1, double[] p2, int n, int k) {
        // TODO : ici p[][] devrait etre donnee en parametre (incrementale), lors du premier appel p[][] est remplie avec des 1
        double[][] p = new double[n][k + 1];
        for (int j = 0; j < n; j++) {
            for (int i = 1; i <= k; i++) {
                p[j][i] = 1;
            }
        }
        p[0][0] = p2[0];
        for (int j = 0; j < n; j++) {
            if (j == 0) {
                for (int i = 1; i <= k; i++) {
                    //p[j][i] = 1;
                    p[j][i] = p1[0] + p2[0];
                    if (p[j][i] == 1) {
                        break;
                    }
                }
            } else {
                for (int i = 0; i <= k; i++) {
                    if (i == 0) {
                        p[j][i] = p2[j - 1] * p[j - 1][i];
                    } else {
                        p[j][i] = p1[j - 1] * p[j - 1][i - 1] + p2[j - 1] * p[j - 1][i];
                    }
                    if (p[j][i] == 1) {
                        break;
                    }
                }
            }
        }
        return p;//p[n - 1][k];
    }

    public static BitSet convertToBitSet(int n) {
        BitSet nSet = new BitSet(32);
        int i = 0;
        while (n > 0) {
            if (n % 2 == 1) {
                nSet.set(i, true);
            }
            i++;
            n /= 2;
        }
        return nSet;
    }

    public static String print(double[] tab) {
        StringBuilder res = new StringBuilder(32);
        for (double aTab : tab) {
            res.append(aTab).append('\t');
        }
        return res.toString();
    }

}
