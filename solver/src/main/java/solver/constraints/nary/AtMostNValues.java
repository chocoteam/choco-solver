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

package solver.constraints.nary;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.nary.nValue.PropAtMostNValues_BC;
import solver.constraints.propagators.nary.nValue.PropAtMostNValues_Greedy;
import solver.variables.IntVar;
import solver.variables.Variable;
import java.util.BitSet;

/**
 * AtMostNValues constraint
 * The number of distinct values in the set of variables vars is at most equal to nValues
 *
 * @author Jean-Guillaume Fages
 */
public class AtMostNValues extends IntConstraint<IntVar> {

    public static enum Algo {
		BC {
			@Override
			public void addPropagators(IntVar[] vars, IntVar nValues,IntConstraint cons, Solver sol) {
				cons.setPropagators(new PropAtMostNValues_BC(vars,nValues,cons,sol));
			}
		},
//		BC_wr { //TODO
//			@Override
//			public void addPropagators(IntVar[] vars, IntVar nValues,IntConstraint cons, Solver sol) {
//				for(IntVar v:vars){
//					assert v.hasEnumeratedDomain();
//				}
//				throw new UnsupportedOperationException("not implemented yet");
//			}
//		},
		Greedy {
			@Override
			public void addPropagators(IntVar[] vars, IntVar nValues,IntConstraint cons, Solver sol) {
				for(IntVar v:vars){
					assert v.hasEnumeratedDomain();
				}
				cons.setPropagators(new PropAtMostNValues_Greedy(vars,nValues,cons,sol));
			}
		};

		public abstract void addPropagators(IntVar[] vars, IntVar nValues,IntConstraint cons, Solver sol);
	}

	/**
	 * AtMostNValues constraint
	 * The number of distinct values in vars is at most nValues
	 * Performs Bound Consistency in O(n+d) with
	 * 		n = |vars|
	 * 		d = maxValue - minValue (from initial domains)
	 *
	 * 	=> very appropriate when d <= n It is indeed much better than the usual time complexity of O(n.log(n))
	 * 	=>  not appropriate when d >> n (you should encode another data structure and a quick sort algorithm)
	 *
	 * @param vars
	 * @param nValues
	 * @param solver
	 */
    public AtMostNValues(IntVar[] vars, IntVar nValues, Solver solver) {
        this(vars, nValues, solver, Algo.BC);
    }

	/**The number of distinct values in vars is at most nValues
	 *
	 * @param vars
	 * @param nValues
	 * @param solver
	 * @param algos propagator(s) for the constraint
	 * (BC, BC_wr and/or Greedy)
	 *  Note that if domains are not enumerated, only the BC makes sense
	 */
    public AtMostNValues(IntVar[] vars, IntVar nValues, Solver solver, Algo... algos) {
        super(ArrayUtils.append(vars,new IntVar[]{nValues}), solver);
		for(Algo a:algos){
			a.addPropagators(vars, nValues, this, solver);
		}
    }

    /**
     * Checks if the constraint is satisfied when all variables are instantiated.
     *
     * @param tuple an complete instantiation
     * @return true iff a solution
     */
    @Override
    public ESat isSatisfied(int[] tuple) {
		BitSet values = new BitSet(tuple.length-1);
        for (int i = 0; i < tuple.length-1; i++) {
			values.set(tuple[i]);
        }
		if(values.cardinality()<=tuple[tuple.length-1]){
			return ESat.TRUE;
		}
        return ESat.FALSE;
    }

    @Override
    public ESat isSatisfied() {
		int n = vars.length-1;
		BitSet values = new BitSet();
		BitSet mandatoryValues = new BitSet();
		IntVar v;
		int ub;
		for(int i=0;i<n;i++){
			v = vars[i];
			ub = v.getUB();
			if(v.instantiated()){
				mandatoryValues.set(ub);
			}
			for(int j=v.getLB();j<=ub;j++){
				values.set(j);
			}
		}
		if(values.cardinality()<=vars[n].getLB()){
			return ESat.TRUE;
		}
		if(mandatoryValues.cardinality()>vars[n].getUB()){
			return ESat.FALSE;
		}
		return ESat.UNDEFINED;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("AtMostNValue({");
        for (int i = 0; i < vars.length-1; i++) {
            if (i > 0) sb.append(", ");
            Variable var = vars[i];
            sb.append(var);
        }
		sb.append(" <= "+vars[vars.length-1]);
        sb.append("})");
        return sb.toString();
    }
}
