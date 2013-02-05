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
package solver.constraints.propagators.nary.globalcardinality;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateInt;
import gnu.trove.map.hash.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;

/**
 * Propagator for Global Cardinality Constraint (GCC) for integer variables
 * Incremental checker and basic filter: no particular consistency
 * Filter cardinality variables only
 *
 * @author Jean-Guillaume Fages
 */
public class PropFastGCC_card extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n, n2;
	private IStateInt[] min,max;
	private IIntDeltaMonitor[] idms;
	private IntProcedure valRem;
	private int[] values;
	private TIntIntHashMap map;
	private IntVar[] cards;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Propagator for Global Cardinality Constraint (GCC) for integer variables
	 * Incremental checker and basic filter: no particular consistency
	 * Filter cardinality variables only
	 * @param vars
	 * @param restrictedValues
	 * @param valueCardinalities
	 * @param constraint
	 * @param sol
	 */
	public PropFastGCC_card(IntVar[] vars, int[] restrictedValues, IntVar[] valueCardinalities, Constraint constraint, Solver sol) {
		super(vars, sol, constraint, PropagatorPriority.LINEAR, false);
		if (restrictedValues.length != valueCardinalities.length) {
			throw new UnsupportedOperationException();
		}
		this.values = restrictedValues;
		this.cards = valueCardinalities;
		this.n = vars.length;
		this.n2 = values.length;
		this.map = new TIntIntHashMap();
		this.idms = new IIntDeltaMonitor[n];
		this.min = new IStateInt[n2];
		this.max = new IStateInt[n2];
		int idx = 0;
		for (int v:values) {
			if (!map.containsKey(v)) {
				min[idx] = environment.makeInt();
				max[idx] = environment.makeInt();
				map.put(v, idx);
				idx++;
			}else{
				throw new UnsupportedOperationException("multiple occurrence of value: "+v);
			}
		}
		for(int i=0;i<n;i++){
			idms[i] = vars[i].monitorDelta(this);
		}
		this.valRem = new IntProcedure() {
			@Override
			public void execute(int value) throws ContradictionException {
				if(map.containsKey(value)){
					int index = map.get(value);
					max[index].add(-1);
					cards[index].updateUpperBound(max[index].get(), aCause);
				}
			}
		};
	}

	@Override
	public String toString() {
		StringBuilder st = new StringBuilder();
		st.append("PropFastGCC_cardFilter(");
		int i = 0;
		for (; i < Math.min(4, vars.length); i++) {
			st.append(vars[i].getName()).append(", ");
		}
		if (i < vars.length - 2) {
			st.append("...,");
		}
		st.append(vars[vars.length - 1].getName()).append(")");
		return st.toString();
	}


	//***********************************************************************************
	// PROPAGATION
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for (int i = 0; i < n2; i++) {
			min[i].set(0);
			max[i].set(0);
		}
		int j, k, ub;
		IntVar v;
		for (int i = 0; i < n; i++) {
			v = vars[i];
			ub = v.getUB();
			if (v.instantiated()) {
				if(map.containsKey(v.getValue())){
					j = map.get(v.getValue());
					min[j].add(1);
					max[j].add(1);
				}
			} else {
				for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
					if(map.containsKey(k)){
						j = map.get(k);
						max[j].add(1);
					}
				}
			}
		}
		for (int i = 0; i < values.length; i++) {
			cards[i].updateLowerBound(min[i].get(),aCause);
			cards[i].updateUpperBound(max[i].get(), aCause);
		}
		for (int i = 0; i < n; i++) {
			idms[i].unfreeze();
		}
	}

	@Override
	public void propagate(int varIdx, int mask) throws ContradictionException {
		IntVar v = vars[varIdx];
		if(v.instantiated() && map.containsKey(v.getValue())){
			int j = map.get(v.getValue());
			min[j].add(1);
			cards[j].updateLowerBound(min[j].get(),aCause);
		}
		idms[varIdx].freeze();
		idms[varIdx].forEach(valRem,EventType.REMOVE);
		idms[varIdx].unfreeze();
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INT_ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		int[] min = new int[n2];
		int[] max = new int[n2];
		int j, k, ub;
		IntVar v;
		for (int i = 0; i < n; i++) {
			v = vars[i];
			ub = v.getUB();
			if (v.instantiated()) {
				if(map.containsKey(v.getValue())){
					j = map.get(v.getValue());
					min[j]++;
					max[j]++;
				}
			} else {
				for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
					if(map.containsKey(k)){
						j = map.get(k);
						max[j]++;
					}
				}
			}
		}
		for (int i = 0; i < n2; i++) {
			if(cards[i].getLB()>max[i] || cards[i].getUB()<min[i]){
				return ESat.FALSE;
			}
		}
		for (int i = 0; i < n2; i++) {
			if(!(cards[i].instantiated() && max[i]==min[i])){
				return ESat.UNDEFINED;
			}
		}
		return ESat.TRUE;
	}
}
