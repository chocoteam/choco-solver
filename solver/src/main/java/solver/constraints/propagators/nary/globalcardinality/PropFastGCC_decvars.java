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
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.setDataStructures.ISet;
import choco.kernel.memory.setDataStructures.SetFactory;
import choco.kernel.memory.setDataStructures.SetType;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * Propagator for Global Cardinality Constraint (GCC) for integer variables
 * Incremental checker and basic filter: no particular consistency
 * Filter decvars only
 *
 * @author Jean-Guillaume Fages
 */
public class PropFastGCC_decvars extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n, n2;
	private int[] values;
	private IntVar[] cards;
	private ISet[] possibles,mandatories;
	private TIntIntHashMap map;
	private TIntArrayList boundToCompute;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Propagator for Global Cardinality Constraint (GCC) for integer variables
	 * Incremental checker and basic filter: no particular consistency
	 * Filter vars only
	 * @param decvars
	 * @param restrictedValues
	 * @param valueCardinalities
	 * @param constraint
	 * @param sol
	 */
	public PropFastGCC_decvars(IntVar[] decvars, int[] restrictedValues, IntVar[] valueCardinalities, Constraint constraint, Solver sol) {
		super(ArrayUtils.append(decvars,valueCardinalities), sol, constraint, PropagatorPriority.LINEAR, false);
		if (restrictedValues.length != valueCardinalities.length) {
			throw new UnsupportedOperationException();
		}
		this.boundToCompute = new TIntArrayList();
		this.values = restrictedValues;
		this.cards = valueCardinalities;
		this.n = decvars.length;
		this.n2 = values.length;
		this.map = new TIntIntHashMap();
		this.possibles = new ISet[n2];
		this.mandatories = new ISet[n2];
		int idx = 0;
		for (int v:values) {
			if (!map.containsKey(v)) {
				mandatories[idx] = SetFactory.makeStoredSet(SetType.LINKED_LIST, n, environment);
				possibles[idx] = SetFactory.makeStoredSet(SetType.LINKED_LIST, n, environment);
				map.put(v, idx);
				idx++;
			}else{
				throw new UnsupportedOperationException("multiple occurrence of value: "+v);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder st = new StringBuilder();
		st.append("PropFastGCC_varsFilter(");
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
		if((evtmask&EventType.FULL_PROPAGATION.mask)!=0){
			for(int i=0;i<n2;i++){
				mandatories[i].clear();
				possibles[i].clear();
			}
			for (int i = 0; i < n; i++) {
				IntVar v = vars[i];
				int ub = v.getUB();
				if (v.instantiated()) {
					if(map.containsKey(v.getValue())){
						int j = map.get(v.getValue());
						mandatories[j].add(i);
					}
				} else {
					for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
						if(map.containsKey(k)){
							int j = map.get(k);
							possibles[j].add(i);
						}
					}
				}
			}
		}else{
			for(int i=0;i<n2;i++){
				for(int var=possibles[i].getFirstElement();var>=0;var=possibles[i].getNextElement()){
					if(!vars[var].contains(values[i])){
						possibles[i].remove(var);
					}else if(vars[var].instantiated()){
						possibles[i].remove(var);
						mandatories[i].add(var);
					}
				}
			}
		}
		boundToCompute.clear();
		for(int i=0;i<n2;i++){
			if(mandatories[i].getSize()==cards[i].getUB()){
				for(int j=possibles[i].getFirstElement();j>=0;j=possibles[i].getNextElement()){
					if(!vars[j].removeValue(values[i],aCause)){
						assert (!vars[j].hasEnumeratedDomain());
						boundToCompute.add(j);
					}
					possibles[i].remove(j);
				}
			}
			if(possibles[i].getSize()+mandatories[i].getSize()==cards[i].getLB()){
				for(int j=possibles[i].getFirstElement();j>=0;j=possibles[i].getNextElement()){
					mandatories[i].add(j);
					vars[j].instantiateTo(values[i],aCause);
				}
				possibles[i].clear();
			}
		}
		if(boundToCompute.size()>0){
			filterBounds();
		}
	}

	@Override
	public void propagate(int varIdx, int mask) throws ContradictionException {
		forcePropagate(EventType.FULL_PROPAGATION);
	}

	private void filterBounds() throws ContradictionException {
		for(int i=0;i<boundToCompute.size();i++){
			int var = boundToCompute.get(i);
			int lb = vars[var].getLB();
			int index = -1;
			if(map.containsKey(lb)){
				index = map.get(lb);
			}
			boolean b = index!=-1&&!(possibles[index].contain(var)||mandatories[index].contain(var));
			while(b){
				vars[var].removeValue(lb,aCause);
				lb = vars[var].getLB();
				index = -1;
				if(map.containsKey(lb)){
					index = map.get(lb);
				}
				b = index!=-1&&!(possibles[index].contain(var)||mandatories[index].contain(var));
			}
			int ub = vars[var].getUB();
			index = -1;
			if(map.containsKey(ub)){
				index = map.get(ub);
			}
			b = index!=-1&&!(possibles[index].contain(var)||mandatories[index].contain(var));
			while(b){
				vars[var].removeValue(ub,aCause);
				ub = vars[var].getUB();
				index = -1;
				if(map.containsKey(ub)){
					index = map.get(ub);
				}
				b = index!=-1&&!(possibles[index].contain(var)||mandatories[index].contain(var));
			}
		}
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
