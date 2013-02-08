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

package solver.constraints.nary;

import common.ESat;
import common.util.iterators.DisposableRangeIterator;
import common.util.tools.StringUtils;
import gnu.trove.map.hash.TObjectIntHashMap;
import memory.IStateBitSet;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.Operator;
import solver.constraints.propagators.nary.sum.*;
import solver.exception.SolverException;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.fast.BitsetIntVarImpl;
import solver.variables.fast.IntervalIntVarImpl;

import java.util.Arrays;

/**
 * <br/>
 * Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public class Sum extends IntConstraint<IntVar> {

//    public static final String
//            VAR_DECRCOEFFS = "var_decrcoeffs",
//            VAR_DOMOVERCOEFFS = "var_domovercoeffs",
//            VAL_TOTO = "domovercoeffs",
//            METRIC_COEFFS = "met_coeffs";

    public static int BIG_SUM_SIZE = 160;
    public static int BIG_SUM_GROUP = 20;

    final int[] coeffs;
    final int b;


    protected Sum(IntVar[] vars, int[] coeffs, int pos, int b, Solver solver) {
        super(vars, solver);
        this.coeffs = coeffs.clone();
        this.b = b;
        if (vars.length > BIG_SUM_SIZE) {
            setPropagators(new PropBigSum(vars, coeffs, pos, b));
        } else {
			setPropagators(new PropSumEq(vars, coeffs, pos, b));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// GENERIC /////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Sum build(IntVar[] vars, int[] coeffs, int r, Solver solver) {
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
        return new Sum(tmpV, tmpC, b, r, solver);
    }

	/**
	 * Ensures that sum{vars[i]} = b
	 * @param vars
	 * @param b
	 * @param solver
	 * @return a sum constraint
	 */
    public static Sum buildSum(IntVar[] vars, IntVar b, Solver solver) {
        int[] cs = new int[vars.length + 1];
        Arrays.fill(cs, 1);
        cs[vars.length] = -1;
        IntVar[] x = new IntVar[vars.length + 1];
        System.arraycopy(vars, 0, x, 0, vars.length);
        x[vars.length] = b;
        return build(x, cs, 0, solver);
    }

	/**
	 * Ensures that sum{vars[i]*coreffs[i]} = b*c
	 * @param vars
	 * @param coeffs
	 * @param b
	 * @param c
	 * @param solver
	 * @return a scalar product constraint
	 */
    public static Sum buildScalar(IntVar[] vars, int[] coeffs, IntVar b, int c, Solver solver) {
        IntVar[] x = new IntVar[vars.length + 1];
        System.arraycopy(vars, 0, x, 0, vars.length);
        x[x.length - 1] = b;
        int[] cs = new int[coeffs.length + 1];
        System.arraycopy(coeffs, 0, cs, 0, coeffs.length);
        cs[cs.length - 1] = -c;
        return build(x, cs, 0, solver);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static IntVar var(IntVar a, IntVar b) {
        if (a.instantiated()) {
            if (b.instantiated()) {
                return VariableFactory.fixed(a.getValue() + b.getValue(), a.getSolver());
            } else {
                return VariableFactory.offset(b, a.getValue());
            }
        } else if (b.instantiated()) {
            return VariableFactory.offset(a, b.getValue());
        } else {
            Solver solver = a.getSolver();
            IntVar z;
            //TODO: add a more complex analysis of the build domain
            if (a.hasEnumeratedDomain() || b.hasEnumeratedDomain()) {
                int lbA = a.getLB();
                int ubA = a.getUB();
                int lbB = b.getLB();
                int ubB = b.getUB();
                int OFFSET = lbA + lbB;
                IStateBitSet VALUES = solver.getEnvironment().makeBitSet((ubA + ubB) - (lbA + lbB) + 1);
                DisposableRangeIterator itA = a.getRangeIterator(true);
                DisposableRangeIterator itB = b.getRangeIterator(true);
                while (itA.hasNext()) {
                    itB.bottomUpInit();
                    while (itB.hasNext()) {
                        VALUES.set(itA.min() + itB.min() - OFFSET, itA.max() + itB.max() - OFFSET + 1);
                        itB.next();
                    }
                    itB.dispose();
                    itA.next();
                }
                itA.dispose();
                z = new BitsetIntVarImpl(StringUtils.randomName(), OFFSET, VALUES, solver);
            } else {
                z = new IntervalIntVarImpl(StringUtils.randomName(), a.getLB() + b.getLB(), a.getUB() + b.getUB(), solver);
            }
            solver.post(IntConstraintFactory.sum(new IntVar[]{a, b},z));
            return z;
        }
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

	public static int[] getScalarBounds(IntVar[] vars, int[] coefs){
		int[] ext = new int[2];
		int n = vars.length;
		for(int i=0;i<n;i++){
			int min = Math.min(0,vars[i].getLB()*coefs[i]);
				min = Math.min(min,vars[i].getUB()*coefs[i]);
			int max = Math.max(0,vars[i].getLB()*coefs[i]);
				max = Math.max(max,vars[i].getUB()*coefs[i]);
			ext[0] += min;
			ext[1] += max;
		}
		return ext;
	};
}
