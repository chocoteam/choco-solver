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
import solver.constraints.propagators.nary.nValue.PropAtLeastNValues_AC;
import solver.variables.IntVar;
import solver.variables.Variable;

import java.util.BitSet;

/**
 * AtLeastNValues Constraint
 * The number of distinct values in the set of variables vars is at least equal to nValues
 *
 * @author Jean-Guillaume Fages
 */
public class AtLeastNValues extends IntConstraint<IntVar> {

	/**
	 * AtLeastNValues Constraint (similar to SoftAllDiff)
	 * The number of distinct values in vars is at least nValues
	 * Performs Generalized Arc Consistency based on Maximum Bipartite Matching
	 * The worst case time complexity is O(nm) but this is very pessimistic
	 * In practice it is more like O(m) where m is the number of variable-value pairs
	 *
	 * @param vars
	 * @param nValues
	 * @param solver
	 */
	public AtLeastNValues(IntVar[] vars, IntVar nValues, Solver solver) {
		super(ArrayUtils.append(vars, new IntVar[]{nValues}), solver);
		setPropagators(new PropAtLeastNValues_AC(vars,nValues,this,solver));
	}

	/**
	 * Checks if the constraint is satisfied when all variables are instantiated.
	 *
	 * @param tuple an complete instantiation
	 * @return true iff a solution
	 */
	@Override
	public ESat isSatisfied(int[] tuple) {
		int n = tuple.length-1;
		BitSet values = new BitSet(n);
		for (int i = 0; i < n; i++) {
			values.set(tuple[i]);
		}
		if(values.cardinality()>=tuple[n]){
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
		if(values.cardinality()<vars[n].getLB()){
			return ESat.FALSE;
		}
		if(mandatoryValues.cardinality()>=vars[n].getUB()){
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append("AtLeastNValue({");
		for (int i = 0; i < vars.length-1; i++) {
			if (i > 0) sb.append(", ");
			Variable var = vars[i];
			sb.append(var);
		}
		sb.append(" >= "+vars[vars.length-1]);
		sb.append("})");
		return sb.toString();
	}
}
