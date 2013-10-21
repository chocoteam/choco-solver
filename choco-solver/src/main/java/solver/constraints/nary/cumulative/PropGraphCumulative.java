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
package solver.constraints.nary.cumulative;

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.objects.graphs.UndirectedGraph;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;
import util.objects.setDataStructures.SetType;
import util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.Random;

/**
 * Graph based cumulative
 * Maintains incrementally overlapping tasks
 * Performs energy checking and mandatory part based filtering
 * BEWARE : not idempotent, use two propagators to get the fix point
 *
 * @author Jean-Guillaume Fages
 * @since 31/01/13
 */
public class PropGraphCumulative extends Propagator<IntVar> {

	protected final int n;
	protected final IntVar[] s, d, e, h;
	protected final IntVar capa;
	protected final UndirectedGraph g;
	protected ISet toCompute, tasks;
	protected CumulFilter[] filters;
	protected final boolean fast;
	protected final Random rd = new Random(0);
	protected int maxrd = 10;

	public PropGraphCumulative(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, boolean fast) {
		super(ArrayUtils.append(s, d, e, h, new IntVar[]{capa}), PropagatorPriority.QUADRATIC, true);
		this.n = s.length;
		this.fast = fast;
		if (!(n == d.length && n == e.length && n == h.length)) {
			throw new UnsupportedOperationException();
		}
		this.s = Arrays.copyOfRange(vars, 0, s.length);
		this.d = Arrays.copyOfRange(vars, s.length, s.length + d.length);
		this.e = Arrays.copyOfRange(vars, s.length + d.length, s.length + d.length + e.length);
		this.h = Arrays.copyOfRange(vars, s.length + d.length + e.length, s.length + d.length + e.length + h.length);
		this.capa = this.vars[vars.length - 1];
		this.g = new UndirectedGraph(environment, n, SetType.SWAP_ARRAY, true);
		this.tasks = SetFactory.makeSwap(n,false);
		this.toCompute = SetFactory.makeSwap(n, false);
		filters = new CumulFilter[]{new NRJCumulFilter(s,d,e,h,capa,this), new BasicCumulativeSweep(s,d,e,h,capa,this)};
	}

	@Override
	public int getPropagationConditions(int idx) {
		if(idx==4*n){
			return EventType.DECUPP.mask + EventType.INSTANTIATE.mask;
		}
		if(fast)
			return EventType.INSTANTIATE.mask;
		return EventType.BOUND.mask + EventType.INSTANTIATE.mask;
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
			toCompute.clear();
			int capaMax = capa.getUB();
			for (int i = 0; i < n; i++) {
				d[i].updateLowerBound(0,aCause); // should even be 1
				h[i].updateLowerBound(0,aCause);
				h[i].updateUpperBound(capaMax,aCause);
				s[i].updateLowerBound(e[i].getLB() - d[i].getUB(), aCause);
				s[i].updateUpperBound(e[i].getUB() - d[i].getLB(), aCause);
				e[i].updateUpperBound(s[i].getUB() + d[i].getUB(), aCause);
				e[i].updateLowerBound(s[i].getLB() + d[i].getLB(), aCause);
				d[i].updateUpperBound(e[i].getUB() - s[i].getLB(), aCause);
				d[i].updateLowerBound(e[i].getLB() - s[i].getUB(), aCause);
			}
			filter(g.getActiveNodes());
			for (int i = 0; i < n; i++) {
				g.getNeighborsOf(i).clear();
			}
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					if (!disjoint(i, j)) {
						g.addEdge(i, j);
					}
				}
			}
		}else{
			if(toCompute.getSize()/n>5){
				filter(g.getActiveNodes());
			}else{
				for (int i = toCompute.getFirstElement(); i >= 0; i = toCompute.getNextElement()) {
					filterAround(i);
				}
			}
		}
		toCompute.clear();
	}

	@Override
	public void propagate(int varIdx, int mask) throws ContradictionException {
		if (varIdx < 4 * n) {
			int v = varIdx % n;
			if((!fast) || mandPartExists(v) || rd.nextInt(maxrd)==0){
				if (!toCompute.contain(v)) {
					toCompute.add(v);
				}
			}
		} else {
			toCompute.clear();
			int capaMax = capa.getUB();
			for (int i = 0; i < n; i++) {
				h[i].updateUpperBound(capaMax,aCause);
				toCompute.add(i);
			}
		}
		if(toCompute.getSize()>0){
			forcePropagate(EventType.CUSTOM_PROPAGATION);
		}
	}

	protected boolean mandPartExists(int i) {
		int lastStart = Math.min(s[i].getUB(),e[i].getUB()-d[i].getLB());
		int earliestEnd = Math.max(s[i].getLB()+d[i].getLB(),e[i].getLB());
		return lastStart<earliestEnd;
	}

	protected boolean disjoint(int i, int j) {
		return s[i].getLB() >= e[j].getUB() || s[j].getLB() >= e[i].getUB();
	}

	protected void filterAround(int taskIndex) throws ContradictionException {
		tasks.clear();
		tasks.add(taskIndex);
		ISet env = g.getNeighborsOf(taskIndex);
		for (int i = env.getFirstElement(); i >= 0; i = env.getNextElement()) {
			if (disjoint(taskIndex, i)) {
				g.removeEdge(taskIndex, i);
			} else {
				tasks.add(i);
			}
		}
		filter(tasks);
	}

	public void filter(ISet tasks) throws ContradictionException{
		for(CumulFilter cf:filters){
			cf.filter(tasks);
		}
	}

	@Override
	public ESat isEntailed() {
		if (!isCompletelyInstantiated()) {
			return ESat.UNDEFINED;
		}
		int min = s[0].getUB();
		int max = e[0].getLB();
		for (int i = 0; i < n; i++) {
			min = Math.min(min, s[i].getUB());
			max = Math.max(max, e[i].getLB());
		}
		if (max <= min) {
			return ESat.TRUE;
		}
		int[] consoMin = new int[max - min];
		for (int i = 0; i < n; i++) {
			for (int t = s[i].getUB(); t < e[i].getLB(); t++) {
				consoMin[t - min] += h[i].getLB();
				if (consoMin[t - min] > capa.getUB()) {
					return ESat.FALSE;
				}
			}
		}
		return ESat.TRUE;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CumulativeGraphProp(");
		sb.append("");
		for (int i = 0; i < n; i++) {
			if (i > 0) sb.append(",");
			sb.append("[" + vars[i].toString());
			sb.append("," + vars[i + n].toString());
			sb.append("," + vars[i + 2 * n].toString());
			sb.append("," + vars[i + 3 * n].toString() + "]");
		}
		sb.append(")");
		return sb.toString();
	}
}
