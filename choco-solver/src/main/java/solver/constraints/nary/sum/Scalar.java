/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.nary.sum;

import gnu.trove.map.hash.TObjectIntHashMap;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.variables.IntVar;
import util.ESat;

/**
 * <br/>
 * Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public class Scalar extends IntConstraint<IntVar> {

    final int[] coeffs;
    final int b;


    protected Scalar(IntVar[] vars, int[] coeffs, int pos, int b, Solver solver) {
        super(vars, solver);
        this.coeffs = coeffs.clone();
        this.b = b;
        if (vars.length > PropBigSum.BIG_SUM_SIZE) {
            setPropagators(new PropBigSum(vars, coeffs, pos, b));
        } else {
            setPropagators(new PropScalarEq(vars, coeffs, pos, b));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// GENERIC /////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Scalar build(IntVar[] vars, int[] coeffs, Solver solver) {
        TObjectIntHashMap<IntVar> map = new TObjectIntHashMap<IntVar>();
        for (int i = 0; i < vars.length; i++) {
            map.adjustOrPutValue(vars[i], coeffs[i], coeffs[i]);
            if (map.get(vars[i]) == 0) {
                map.remove(vars[i]);
            }
        }
        int b = 0, e = map.size();
        IntVar[] tmpV = new IntVar[e];
        int[] tmpC = new int[e];
        // to fix determinism in the construction, we iterate over the original array of variables
        for (int i = 0; i < vars.length; i++) {
            IntVar key = vars[i];
            int coeff = map.get(key);
            if (coeff > 0) {
                tmpV[b] = key;
                tmpC[b++] = coeff;
            } else if (coeff < 0) {
                tmpV[--e] = key;
                tmpC[e] = coeff;
            }
            map.adjustValue(key, -coeff); // to avoid multiple occurrence of the variable
        }
        return new Scalar(tmpV, tmpC, b, 0, solver);
    }

    /**
     * Ensures that sum{vars[i]*coreffs[i]} = b*c
     *
     * @param vars
     * @param coeffs
     * @param b
     * @param c
     * @param solver
     * @return a scalar product constraint
     */
    public static Scalar buildScalar(IntVar[] vars, int[] coeffs, IntVar b, int c, Solver solver) {
        IntVar[] x = new IntVar[vars.length + 1];
        System.arraycopy(vars, 0, x, 0, vars.length);
        x[x.length - 1] = b;
        int[] cs = new int[coeffs.length + 1];
        System.arraycopy(coeffs, 0, cs, 0, coeffs.length);
        cs[cs.length - 1] = -c;
        return build(x, cs, solver);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ESat isSatisfied(int[] tuple) {
        int sum = 0;
        for (int i = 0; i < tuple.length; i++) {
            sum += coeffs[i] * tuple[i];
        }
        return ESat.eval(sum == b);
    }

    @Override
    public String toString() {
        StringBuilder linComb = new StringBuilder(20);
        for (int i = 0; i < coeffs.length; i++) {
            linComb.append(coeffs[i]).append('*').append(vars[i].getName()).append(coeffs[i] < coeffs.length ? " +" : " ");
        }
        linComb.append(" = ");
        linComb.append(b);
        return linComb.toString();
    }

    public static int[] getScalarBounds(IntVar[] vars, int[] coefs) {
        int[] ext = new int[2];
        for (int i = 0; i < vars.length; i++) {
            int min = Math.min(0, vars[i].getLB() * coefs[i]);
            min = Math.min(min, vars[i].getUB() * coefs[i]);
            int max = Math.max(0, vars[i].getLB() * coefs[i]);
            max = Math.max(max, vars[i].getUB() * coefs[i]);
            ext[0] += min;
            ext[1] += max;
        }
        return ext;
    }
    ;
}
