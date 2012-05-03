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
package solver.constraints.propagators.nary.nValue;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.GraphType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.StoredUndirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraph;

import java.util.BitSet;

/**
 * Propagator for the atMostNValues constraint
 * The number of distinct values in the set of variables vars is at most equal to nValues
 * No level of consistency but better than BC in general (for enumerated domains with holes)
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtMostNValues_Greedy extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private IntVar nValues;
	// graph model
	private int n, n2;
	private UndirectedGraph digraph;
	private TIntIntHashMap map;
	private int[] value;
	// required data structure
	private int[] nbNeighbors;
	private BitSet in, inMIS;
	private TIntArrayList list;
	private IntProcedure remProc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Propagator for the atMostNValues constraint
	 * The number of distinct values in the set of variables vars is at most equal to nValues
	 * No level of consistency but better than BC in general (for enumerated domains with holes)
	 *
	 * @param vars
	 * @param nValues
	 * @param constraint
	 * @param solver
	 */
	public PropAtMostNValues_Greedy(IntVar[] vars, IntVar nValues, Constraint constraint, Solver solver) {
		super(ArrayUtils.append(vars,new IntVar[]{nValues}), solver, constraint, PropagatorPriority.QUADRATIC, true);
		n = vars.length;
		this.nValues = nValues;
		map = new TIntIntHashMap();
		IntVar v;
		int ub;
		int idx = n;
		for (int i = 0; i < n; i++) {
			v = vars[i];
			ub = v.getUB();
			for (int j = v.getLB(); j <= ub; j = v.nextValue(j)) {
				if (!map.containsKey(j)) {
					map.put(j, idx);
					idx++;
				}
			}
		}
		n2 = idx;
		value = new int[n2-n];
		for (int i = 0; i < n; i++) {
			v = vars[i];
			ub = v.getUB();
			for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
				value[map.get(k)-n] = k;
			}
		}
		digraph = new StoredUndirectedGraph(solver.getEnvironment(), n2, GraphType.LINKED_LIST);
		remProc = new RemProc();
		in = new BitSet(n2);
		inMIS = new BitSet(n2);
		nbNeighbors = new int[n2];
		list = new TIntArrayList();
	}

	//***********************************************************************************
	// ALGORITHMS
	//***********************************************************************************

	private void buildDigraph() {
		for (int i = 0; i < n2; i++) {
			digraph.getSuccessorsOf(i).clear();
			digraph.getPredecessorsOf(i).clear();
		}
		int j, k, ub;
		IntVar v;
		for (int i = n; i < n2; i++) {
			digraph.desactivateNode(i);
		}
		for (int i = 0; i < n; i++) {
			v = vars[i];
			ub = v.getUB();
			for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
				j = map.get(k);
				digraph.activateNode(j);
				digraph.addEdge(i,j);
			}
			for (int i2 = i+1; i2 < n; i2++) {
				if(intersect(i,i2)){
					digraph.addEdge(i,i2);
				}
			}
		}
	}

	private boolean intersect(int i, int j) {
		IntVar x = vars[i];
		IntVar y = vars[j];
		if (x.getLB()>y.getUB() || y.getLB()>x.getUB()){
			return false;
		}
		int ub = x.getUB();
		for(int val=x.getLB();val<=ub;val=x.nextValue(val)){
			if(y.contains(val)){
				return true;
			}
		}
		return false;
	}
	
	private int greedySearch(){
		// prepare data structures
		for (int i = 0; i < n2; i++) {
			nbNeighbors[i] = 0;
		}
		in.clear();
		inMIS.clear();
		IntVar v;
		int j;
		for(int i=0;i<n;i++){
			in.set(i);
			v = vars[i];
			if(v.instantiated()){
				j = map.get(v.getLB());
				in.set(j);
				nbNeighbors[j]++;
			}
			nbNeighbors[i] = v.getDomainSize();
		}
		INeighbors nei;
		list.clear();
		int min = 0;
		// find MIS
		int idx = in.nextSetBit(0);
		while (idx>=0){
			for(int i=in.nextSetBit(idx+1);i>=0;i=in.nextSetBit(i+1)){
				if(nbNeighbors[i]<nbNeighbors[idx]){
					idx = i;
				}
			}
			nei = digraph.getNeighborsOf(idx);
			in.clear(idx);
			inMIS.set(idx);
			for(j=nei.getFirstElement(); j>=0; j = nei.getNextElement()){
				if(in.get(j)){
					in.clear(j);
					list.add(j);
				}
			}
			for(int i=list.size()-1;i>=0;i--){
				nei = digraph.getNeighborsOf(list.get(i));
				for(j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					nbNeighbors[j]--;
				}
			}
			list.clear();
			min ++;
			idx = in.nextSetBit(0);
		}
		return min;
	}

	private void filter() throws ContradictionException {
		IActiveNodes nodes = digraph.getActiveNodes();
		INeighbors nei;
		int mate;
		for(int i=nodes.getFirstElement();i>=0;i=nodes.getNextElement()){
			if(!inMIS.get(i)){
				mate = -1;
				nei = digraph.getNeighborsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(inMIS.get(j)){
						if(mate == -1){
							mate = j;
						}else{
							mate = -2;
							break;
						}
					}
				}
				if(mate>=0){
					enforce(i,mate);
				}
			}
		}
	}

	private void enforce(int i, int j) throws ContradictionException {
		if(i>j){
			enforce(j,i);
		}else{
			if(j>=n){	//vars[i] = value[j]
				vars[i].instantiateTo(value[j-n],this);
			}else{		//vars[i] = vars[j]
				IntVar x = vars[i];
				IntVar y = vars[j];
				x.updateUpperBound(y.getUB(),this);
				y.updateUpperBound(x.getUB(),this);
				x.updateLowerBound(y.getLB(),this);
				y.updateLowerBound(x.getLB(),this);
				int ub = x.getUB();
				for(int val=x.getLB();val<=ub;val=x.nextValue(val)){
					if(!y.contains(val)){
						x.removeValue(val,this);
					}
				}
				ub = y.getUB();
				for(int val=y.getLB();val<=ub;val=y.nextValue(val)){
					if(!x.contains(val)){
						y.removeValue(val,this);
					}
				}
			}
		}
	}

	//***********************************************************************************
	// PROPAGATION
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if((evtmask &= EventType.FULL_PROPAGATION.mask)!=0){
			buildDigraph();
		}
		if(digraph.getActiveNodes().neighborhoodSize()<=nValues.getLB()){
			setPassive();
		}else{
			int min = greedySearch();
			nValues.updateLowerBound(min, this);
			if(min == nValues.getUB()){
				filter();
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(idxVarInProp<n){
			eventRecorder.getDeltaMonitor(this, vars[idxVarInProp]).forEach(remProc, EventType.REMOVE);
			INeighbors nei = digraph.getNeighborsOf(idxVarInProp);
			for(int v=nei.getFirstElement();v>=0;v=nei.getNextElement()){
				if(v<n && !intersect(idxVarInProp,v)){
					digraph.removeEdge(idxVarInProp,v);
				}
			}
		}
		forcePropagate(EventType.CUSTOM_PROPAGATION);
//		if (nbPendingER == 0) {
//			int card = repairMatching();
//			nValues.updateUpperBound(card,this);
//			if(nValues.getLB()==card){
//				filter();
//			}
//		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INT_ALL_MASK();
	}

	@Override
	public int getPropagationConditions() {
		return EventType.FULL_PROPAGATION.mask+EventType.CUSTOM_PROPAGATION.mask;
	}

	@Override
	public ESat isEntailed() {
		BitSet values = new BitSet(nValues.getUB());
		BitSet mandatoryValues = new BitSet(nValues.getUB());
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

	//***********************************************************************************
	// PROCEDURE
	//***********************************************************************************

	private class RemProc implements IntProcedure {
		public void execute(int i) throws ContradictionException {
			int j = map.get(i);
			digraph.removeEdge(i,j);
			if(digraph.getNeighborsOf(j).isEmpty()){
				digraph.desactivateNode(j);
			}
		}
	}
}
