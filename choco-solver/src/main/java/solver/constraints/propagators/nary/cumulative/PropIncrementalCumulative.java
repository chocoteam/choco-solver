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
package solver.constraints.propagators.nary.cumulative;

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
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

/**
 * Graph based cumulative
 * Maintains incrementally overlapping tasks
 * Performs energy checking and mandatory part based filtering
 *
 * @author Jean-Guillaume Fages
 * @since 31/01/13
 */
public class PropIncrementalCumulative extends Propagator<IntVar> {

    private int n;
    private IntVar[] s, d, e, h;
    private IntVar capa;
    private UndirectedGraph g;
    private ISet toCompute, tasks;

    public PropIncrementalCumulative(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa) {
        super(ArrayUtils.append(s, d, e, h, new IntVar[]{capa}), PropagatorPriority.LINEAR, false);
        this.n = s.length;
        if (!(n == d.length && n == e.length && n == h.length)) {
            throw new UnsupportedOperationException();
        }
        this.s = Arrays.copyOfRange(vars, 0, s.length);
        this.d = Arrays.copyOfRange(vars, s.length, s.length + d.length);
        this.e = Arrays.copyOfRange(vars, s.length + d.length, s.length + d.length + e.length);
        this.h = Arrays.copyOfRange(vars, s.length + d.length + e.length, s.length + d.length + e.length + h.length);
        this.g = new UndirectedGraph(environment, n, SetType.SWAP_ARRAY, true);
        this.capa = this.vars[vars.length - 1];
        this.tasks = SetFactory.makeLinkedList(false);
        this.toCompute = SetFactory.makeSwap(n, false);
    }

    @Override
    public int getPropagationConditions(int idx) {
        return EventType.BOUND.mask + EventType.INSTANTIATE.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            toCompute.clear();
            for (int i = 0; i < n; i++) {
                s[i].updateLowerBound(e[i].getLB() - d[i].getUB(), aCause);
                s[i].updateUpperBound(e[i].getUB() - d[i].getLB(), aCause);
                e[i].updateUpperBound(s[i].getUB() + d[i].getUB(), aCause);
                e[i].updateLowerBound(s[i].getLB() + d[i].getLB(), aCause);
                d[i].updateUpperBound(e[i].getUB() - s[i].getLB(), aCause);
                d[i].updateLowerBound(e[i].getLB() - s[i].getUB(), aCause);
            }
            energyOn(g.getActiveNodes());
            sweepOn(g.getActiveNodes());
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
			for (int i = toCompute.getFirstElement(); i >= 0; i = toCompute.getNextElement()) {
				filterAround(i);
			}
		}
        toCompute.clear();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (varIdx < 4 * n) {
            int v = varIdx % n;
            if (!toCompute.contain(v)) {
                toCompute.add(v);
            }
        } else {
            toCompute.clear();
            for (int i = 0; i < n; i++) {
                toCompute.add(i);
            }
        }
        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }

    private boolean disjoint(int i, int j) {
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
        sweepOn(tasks);
        energyOn(tasks);
    }

	TTDynamicSweep sweep;
    protected void sweepOn(ISet tasks) throws ContradictionException {
        // todo add sweep algorithm once fixed
		if(sweep == null) {
			sweep = new TTDynamicSweep(vars,n,1,aCause);
		}
		sweep.set(tasks);
		sweep.mainLoop();
//        naiveFilter(tasks);
    }

    private void naiveFilter(ISet tasks) throws ContradictionException {
        int min = Integer.MAX_VALUE / 2;
        int max = Integer.MIN_VALUE / 2;
        for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
            if (s[i].getUB() < e[i].getLB()) {
                min = Math.min(min, s[i].getUB());
                max = Math.max(max, e[i].getLB());
            }
        }
        if (min < max) {
            int[] time = new int[max - min];
            int capaMax = capa.getUB();
            for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
                for (int t = s[i].getUB(); t < e[i].getLB(); t++) {
                    time[t - min] += h[i].getLB();
                    capa.updateLowerBound(time[t - min], aCause);
                }
            }
            for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
                if (d[i].getLB() > 0 && h[i].getLB() > 0) {
                    // filters
                    if (s[i].getLB() + d[i].getLB() > min) {
                        filterInf(i, min, max, time, capaMax);
                    }
                    if (e[i].getUB() - d[i].getLB() < max) {
                        filterSup(i, min, max, time, capaMax);
                    }
                }
            }
        }
    }

    private void filterInf(int i, int min, int max, int[] time, int capaMax) throws ContradictionException {
        int nbOk = 0;
        for (int t = s[i].getLB(); t < s[i].getUB(); t++) {
            if (t < min || t >= max || h[i].getLB() + time[t - min] <= capaMax) {
                nbOk++;
                if (nbOk == d[i].getLB()) {
                    return;
                }
            } else {
                nbOk = 0;
                s[i].updateLowerBound(t + 1, aCause);
            }
        }
    }

    private void filterSup(int i, int min, int max, int[] time, int capaMax) throws ContradictionException {
        int nbOk = 0;
        for (int t = e[i].getUB(); t > e[i].getLB(); t--) {
            if (t - 1 < min || t - 1 >= max || h[i].getLB() + time[t - min - 1] <= capaMax) {
                nbOk++;
                if (nbOk == d[i].getLB()) {
                    return;
                }
            } else {
                nbOk = 0;
                e[i].updateUpperBound(t - 1, aCause);
            }
        }
    }

    protected void energyOn(ISet tasks) throws ContradictionException {
        int xMin = Integer.MAX_VALUE / 2;
        int xMax = Integer.MIN_VALUE / 2;
        int surface = 0;
        int camax = capa.getUB();
        for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
            surface += d[i].getLB() * h[i].getLB();
            xMax = Math.max(xMax, e[i].getUB());
            xMin = Math.min(xMin, s[i].getLB());
            if (xMax >= xMin && surface > (xMax - xMin) * camax) {
                contradiction(vars[i], "");
            }
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
