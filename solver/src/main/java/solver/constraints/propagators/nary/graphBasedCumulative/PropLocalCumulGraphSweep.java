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
package solver.constraints.propagators.nary.graphBasedCumulative;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.setDataStructures.ISet;
import choco.kernel.memory.setDataStructures.SetFactory;
import choco.kernel.memory.setDataStructures.SetType;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.nary.graphBasedCumulative.sweep.StaticSweep;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.UndirectedGraph;

/**
 * Graph based cumulative
 * Maintains incrementally overlapping tasks
 * Performs energy checks and sweep algorithm (Arnaud's) Locally
 * @author Jean-Guillaume Fages
 * @since 31/01/13
 */
public class PropLocalCumulGraphSweep extends Propagator<IntVar> {

	private int n;
	private IntVar[] s,d,e,h;
	private IntVar capa;
	private UndirectedGraph g;
	private ISet toCompute, tasks;

	public PropLocalCumulGraphSweep(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, Constraint constraint, Solver solver) {
		super(ArrayUtils.append(s,d,e,h), solver, constraint, PropagatorPriority.LINEAR, false);
		n = s.length;
		if(!(n==d.length && n==e.length && n==h.length)){
			throw new UnsupportedOperationException();
		}
		this.s = s;
		this.d = d;
		this.e = e;
		this.h = h;
		this.g = new UndirectedGraph(environment,n, SetType.SWAP_ARRAY,true);
		this.capa = capa;
		this.tasks = SetFactory.makeLinkedList(false);
		this.toCompute = SetFactory.makeSwap(n,false);
	}

	@Override
	public int getPropagationConditions(int idx) {
		return EventType.BOUND.mask+EventType.INSTANTIATE.mask;
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
			toCompute.clear();
			energyOn(g.getActiveNodes());
			sweepOn(g.getActiveNodes());
			for(int i=0;i<n;i++){
				g.getNeighborsOf(i).clear();
			}
			for(int i=0;i<n;i++){
				for(int j=i+1;j<n;j++){
					if(!disjoint(i, j)){
						g.addEdge(i,j);
					}
				}
			}
		}
		for(int i=toCompute.getFirstElement();i>=0;i=toCompute.getNextElement()){
			filterAround(i);
		}
		toCompute.clear();
	}

	@Override
	public void propagate(int varIdx, int mask) throws ContradictionException {
		int v = varIdx%n;
		ISet s = g.getNeighborsOf(v);
		for(int i=s.getFirstElement();i>=0;i=s.getNextElement()){
			if(disjoint(v, i)){
				g.removeEdge(v,i);
			}
		}
		if(!toCompute.contain(v)){
			toCompute.add(v);
		}
		forcePropagate(EventType.CUSTOM_PROPAGATION);
	}

	private boolean disjoint(int i, int j) {
		return s[i].getLB()>=e[j].getUB() || s[j].getLB()>=e[i].getUB();
	}

	protected void filterAround(int taskIndex) throws ContradictionException {
		tasks.clear();
		tasks.add(taskIndex);
		ISet env = g.getNeighborsOf(taskIndex);
		for(int i=env.getFirstElement();i>=0;i=env.getNextElement()){
			tasks.add(i);
		}
		energyOn(tasks);
		sweepOn(tasks);
	}

	protected void sweepOn(ISet tasks) throws ContradictionException {
		if(tasks.getSize()>1){
			int k = tasks.getSize();
			IntVar[] varsLocal = new IntVar[k*4];
			int idx = 0;
			for(int i=tasks.getFirstElement();i>=0;i=tasks.getNextElement()){
				varsLocal[idx] = s[i];
				varsLocal[idx+k] = d[i];
				varsLocal[idx+2*k] = e[i];
				varsLocal[idx+3*k] = h[i];
				idx++;
			}
			StaticSweep ss = new StaticSweep(varsLocal,capa.getUB(),this,0,aCause);
			ss.mainLoop();
		}
	}

	protected void energyOn(ISet tasks) throws ContradictionException {
		int xMin = -99999;
		int xMax =  99999;
		int surface =   0;
		int camax = capa.getUB();
		for(int i=tasks.getFirstElement();i>=0;i=tasks.getNextElement()){
			surface += d[i].getLB()*h[i].getLB();
			xMax = Math.max(xMax,e[i].getUB());
			xMin = Math.min(xMin,s[i].getLB());
			if(surface>(xMax-xMin)*camax){
				contradiction(vars[0],"");
			}
		}
	}

	@Override
	public ESat isEntailed() {
		if (!isCompletelyInstantiated()) {
			return ESat.UNDEFINED;
		}
		for(int i=0;i<n;i++){
			int x = s[i].getValue();
			int y = e[i].getValue();
			for(int t=x;t<y;t++){
				int conso = h[i].getValue();
				for(int j=0;j<n;j++){
					if(i!=j && s[j].getValue()>=t && e[j].getValue()<t){
						conso+=h[j].getValue();
					}
				}
				if(conso>capa.getValue()){
					return ESat.FALSE;
				}
			}
		}
		return ESat.TRUE;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CumulativePropSweep(");
		sb.append("");
		for (int i = 0; i < n; i++) {
			if (i > 0) sb.append(",");
			sb.append("["+vars[i].toString());
			sb.append(","+vars[i+n].toString());
			sb.append(","+vars[i+2*n].toString());
			sb.append(","+vars[i+3*n].toString()+"]");
		}
		sb.append(")");
		return sb.toString();
	}
}
