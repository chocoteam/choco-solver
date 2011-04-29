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
 */
public class ValueProbabilisticComputation implements IProbabilisticComputation {

    protected static final boolean debug = false;
    protected static final double prec = Math.pow(10,6);


    /////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////// Consistency ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    double pIn[];
    double pNotIn[];
    double pOut[];

    public final void updateProbaCons(int[] domSize, int unionSize, int l) {
        int n = domSize.length;
        this.pIn = new double[n];
        this.pNotIn = new double[n];
        this.pOut = new double[n];
        if (debug) System.out.println("\t\ttaille intervalle : " + l);
        for (int i = 0; i < n; i++) {
            double di = (double) domSize[i];
            pIn[i] = Math.max(0, (l - di + 1) / (unionSize - di + 1));
            pNotIn[i] = 1.0 - pIn[i];
            pOut[i] = Math.max(0, (unionSize - l - di + 1) / (unionSize - di + 1));
            if (debug) {
                System.out.print("\t\tpIn[" + i + "] = " + pIn[i] + ", ");
                System.out.print("pNotIn[" + i + "] = " + pNotIn[i] + ", ");
                System.out.println("pOut[" + i + "] = " + pOut[i]);
            }
        }
    }

    public final boolean allNullPin() {
        for (double aPIn : pIn) {
            if (aPIn != 0) return false;
        }
        return true;
    }

    @Override
    public final double consistency(int[] domSize, int unionSize, int minDomSize) {
        double res = 1;
        int n = domSize.length;
        if (debug) {
            System.out.println("\tunionSize = " + unionSize);
            System.out.println("\tminDomSize = " + minDomSize);
            System.out.println("\tn = " + n);
            System.out.println(printTab(domSize, "dom"));
        }
        for (int c = minDomSize; c <= unionSize; c++) {
            updateProbaCons(domSize, unionSize, c);
            double val1 = probConsistHall(n, c);
            double val2 = probConsistI(n, c);
            if (debug) {
                System.out.println("\tn = " + n + ", union = " + unionSize);
                System.out.println("\tsize(" + c + ") :: Hall(" + val1 + "), Include(" + val2 + ')');
            }
            val1 = Math.floor(val1 * prec) / prec;
            val2 = Math.floor(val2 * prec) / prec;
            double todo = (val1 + val2);
            if (debug) System.out.println("\ton ajoute : " + todo);
            res *= todo;
            //res *= Math.pow(val1 + val2,unionSize - c + 1);
        }
        if (debug) {
            System.out.println("\t-*-*-*-*-*-*-");
        }
        //return 1+Math.log(1-res);
        return res;
    }

    public final double probConsistHall(int n, int size) {
        double[][] t = ProbaUtils.generalizedNewton(pIn, pOut, n, size);
        //double[][] t = ProbaUtils.generalizedNewtonBis(pIn, pOut, n, size);
        return t[n-1][size] - t[n-1][size-1];
    }

    public final double probConsistI(int n, int size) {
        double[][] t = ProbaUtils.generalizedNewton(pIn, pNotIn, n, size);
        return t[n-1][size-1];
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// Satisfiability ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    double p[];

    public void updateProbaSat(int n, int unionSize, int[] domSize) {
        this.p = new double[n];
        for (int i = 0; i < n; i++) {
            this.p[i] = (double) domSize[i] / (double) unionSize;
        }
    }

    @Override
    public double satisfiability(int n, int unionSize, int[] domSize) {
        updateProbaSat(n, unionSize, domSize);
        double res = 1;
        BitSet current;
        for (int i = 1; i < Math.pow(2, n); i++) {
            current = ProbaUtils.convertToBitSet(i);
            double val = probSatJ(unionSize, current);
            //System.out.println("\t\t val = " + val + " --- " + current);
            res *= val;
        }
        //System.out.println("\t\t-*-*-*-*-*-*-\n");
        return res;
    }

    public double probSatJ(int unionSize, BitSet setJ) {
        double res = 0;
        double qJ = qSet(setJ);
        for (int k = setJ.cardinality(); k <= unionSize; k++) {
            //System.out.println("\t\t\tqJ = " + qJ +", k = " + k);
            double c = ProbaUtils.coefBinome(unionSize, k);
            double d = Math.pow(qJ, k);
            double e = Math.pow(1 - qJ, unionSize - k);
            //System.out.println("\t\t\tc = " + c + "; d = " + d + "; e = " + e);
            res += c * d * e;
        }
        return res;
    }


    public double qSet(BitSet set) {
        double res = 1.0;
        for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
            res *= (1.0 - p[i]);
        }
        return 1.0 - res;
    }

    public static String printTab(double[] tab, String s) {
        StringBuilder res = new StringBuilder(32);
        for (int i = 0; i < tab.length; i++) {
            res.append(s).append('[').append(i).append("] = ").append(tab[i]).append(", ");
        }
        return res.toString();
    }

    public static String printTab(int[] tab, String s) {
        StringBuilder res = new StringBuilder(32).append('\t');
        for (int i = 0; i < tab.length; i++) {
            res.append(s).append('[').append(i).append("] = ").append(tab[i]).append(", ");
        }
        return res.toString();
    }

}
