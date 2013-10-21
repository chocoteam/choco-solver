/**
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
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;
import util.tools.ArrayUtils;
import java.util.Arrays;

/**
 * Cumulative propagator
 * Performs energy checking and mandatory part based filtering
 * BEWARE : not idempotent, use two propagators to get the fix point
 *
 * @author Jean-Guillaume Fages
 * @since 31/01/13
 */
public class PropFullCumulative extends Propagator<IntVar> {

	protected final int n;
	protected final IntVar[] s, d, e, h;
	protected final IntVar capa;
	protected ISet alltasks;
	protected CumulFilter[] filters;

	public PropFullCumulative(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa) {
		super(ArrayUtils.append(s, d, e, h, new IntVar[]{capa}), PropagatorPriority.QUADRATIC, false);
		this.n = s.length;
		if (!(n == d.length && n == e.length && n == h.length)) {
			throw new UnsupportedOperationException();
		}
		this.s = Arrays.copyOfRange(vars, 0, s.length);
		this.d = Arrays.copyOfRange(vars, s.length, s.length + d.length);
		this.e = Arrays.copyOfRange(vars, s.length + d.length, s.length + d.length + e.length);
		this.h = Arrays.copyOfRange(vars, s.length + d.length + e.length, s.length + d.length + e.length + h.length);
		this.capa = this.vars[vars.length - 1];
		this.alltasks = SetFactory.makeFullSet(n);
		filters = new CumulFilter[]{new NRJCumulFilter(s,d,e,h,capa,this), new BasicCumulativeSweep(s,d,e,h,capa,this)};
	}

	@Override
	public int getPropagationConditions(int idx) {
		if(idx==4*n){
			return EventType.DECUPP.mask + EventType.INSTANTIATE.mask;
		}
		return EventType.BOUND.mask + EventType.INSTANTIATE.mask;
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
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
		}
		filter(alltasks);
	}

	@Override
	public void propagate(int varIdx, int mask) throws ContradictionException {
		forcePropagate(EventType.CUSTOM_PROPAGATION);
	}

	protected boolean disjoint(int i, int j) {
		return s[i].getLB() >= e[j].getUB() || s[j].getLB() >= e[i].getUB();
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
		StringBuilder sb = new StringBuilder("PropFullCumulative(");
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
