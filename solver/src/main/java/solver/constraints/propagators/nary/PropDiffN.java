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
package solver.constraints.propagators.nary;

import common.ESat;
import common.util.tools.ArrayUtils;
import memory.graphs.UndirectedGraph;
import memory.setDataStructures.ISet;
import memory.setDataStructures.SetFactory;
import memory.setDataStructures.SetType;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * @author Jean-Guillaume Fages
 * @since 31/01/13
 */
public class PropDiffN extends Propagator<IntVar> {

    private int n;
    private UndirectedGraph overlappingBoxes;
    private ISet boxesToCompute;

    public PropDiffN(IntVar[] x, IntVar[] y, IntVar[] dx, IntVar[] dy) {
        super(ArrayUtils.append(x, y, dx, dy), PropagatorPriority.LINEAR, false);
        n = x.length;
        if (!(n == y.length && n == dx.length && n == dy.length)) {
            throw new UnsupportedOperationException();
        }
        overlappingBoxes = new UndirectedGraph(environment, n, SetType.LINKED_LIST, true);
        boxesToCompute = SetFactory.makeStoredSet(SetType.LINKED_LIST, n, environment);
    }

    @Override
    public int getPropagationConditions(int idx) {
        return EventType.INSTANTIATE.mask + +EventType.BOUND.mask + EventType.REMOVE.mask;
    }

    @Override
    public boolean advise(int varIdx, int mask) {
        int v = varIdx % n;
        ISet s = overlappingBoxes.getNeighborsOf(v);
        for (int i = s.getFirstElement(); i >= 0; i = s.getNextElement()) {
            if (!overlap(v, i)) {
                overlappingBoxes.removeEdge(v, i);
            }
        }
        if (!boxesToCompute.contain(v)) {
            boxesToCompute.add(v);
        }
        return super.advise(varIdx, mask);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            for (int i = 0; i < n; i++) {
                overlappingBoxes.getNeighborsOf(i).clear();
            }
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (overlap(i, j)) {
                        overlappingBoxes.addEdge(i, j);
                        if (boxInstantiated(i) && boxInstantiated(j)) {
                            contradiction(vars[i], "");
                        }
                    }
                }
            }
            boxesToCompute.clear();
            for (int i = 0; i < n; i++) {
                boxesToCompute.add(i);
            }
        }
        for (int i = boxesToCompute.getFirstElement(); i >= 0; i = boxesToCompute.getNextElement()) {
            filterFromBox(i);
        }
        boxesToCompute.clear();
    }

    private boolean overlap(int i, int j) {
        if (disjoint(i, j, true) || disjoint(i, j, false)) {
            return false;
        }
        return true;
    }

    private boolean disjoint(int i, int j, boolean horizontal) {
        int off = (horizontal) ? 0 : n;
        return (vars[i + off].getLB() >= vars[j + off].getUB() + vars[j + off + 2 * n].getUB())
                || (vars[j + off].getLB() >= vars[i + off].getUB() + vars[i + off + 2 * n].getUB());
    }

    protected void filterFromBox(int b) throws ContradictionException {
        ISet s = overlappingBoxes.getNeighborsOf(b);
        // check energy
        int xm = vars[b].getLB();
        int xM = vars[b].getUB() + vars[b + 2 * n].getUB();
        int ym = vars[b + n].getLB();
        int yM = vars[b + n].getUB() + vars[b + 3 * n].getUB();
        int am = vars[b + 2 * n].getLB() * vars[b + 3 * n].getLB();
        for (int j = s.getFirstElement(); j >= 0; j = s.getNextElement()) {
            xm = Math.min(xm, vars[j].getLB());
            xM = Math.max(xM, vars[j].getUB() + vars[j + 2 * n].getUB());
            ym = Math.min(ym, vars[j + n].getLB());
            yM = Math.max(yM, vars[j + n].getUB() + vars[j + 3 * n].getUB());
            am += vars[j + 2 * n].getLB() * vars[j + 3 * n].getLB();
            if (am > (xM - xm) * (yM - ym)) {
                contradiction(vars[b], "");
            }
        }
        // mandatory part of box b
        int mps_xi = vars[b].getUB();
        int mpe_xi = vars[b].getLB() + vars[b + 2 * n].getLB();
        int mps_yi = vars[b + n].getUB();
        int mpe_yi = vars[b + n].getLB() + vars[b + 3 * n].getLB();
        // mandatory part exists
        if (mps_xi < mpe_xi && mps_yi < mpe_yi) {
            for (int j = s.getFirstElement(); j >= 0; j = s.getNextElement()) {
                filterBox(b, j, mps_xi, mpe_xi, mps_yi, mpe_yi);
            }
        }
    }

    private void filterBox(int i, int j, int mps_xi, int mpe_xi, int mps_yi, int mpe_yi) throws ContradictionException {
        // mandatory part of box j
        int mps_xj = vars[j].getUB();
        int mpe_xj = vars[j].getLB() + vars[j + 2 * n].getLB();
        int mps_yj = vars[j + n].getUB();
        int mpe_yj = vars[j + n].getLB() + vars[j + 3 * n].getLB();
        // mandatory part exists
        if (mps_xj < mpe_xj && mps_yj < mpe_yj) {
            boolean overH = mps_xj < mpe_xi && mpe_xj > mps_xi;
            boolean overV = mps_yj < mpe_yi && mpe_yj > mps_yi;
            if (overH && overV) {// mandatory parts overlap
                contradiction(vars[i], "");
            } else if (overH) {// mandatory parts overlap horizontally only
                if (mps_yi < mps_yj) {
                    vars[j + n].updateLowerBound(mpe_yi, aCause);
                    vars[i + n].updateUpperBound(mps_yj - vars[i + 3 * n].getLB(), aCause);
                    vars[i + 3 * n].updateUpperBound(mps_yj - vars[i + n].getLB(), aCause);
                } else {
                    vars[i + n].updateLowerBound(mpe_yj, aCause);
                    vars[j + n].updateUpperBound(mps_yi - vars[j + 3 * n].getLB(), aCause);
                    vars[j + 3 * n].updateUpperBound(mps_yi - vars[j + n].getLB(), aCause);
                }
            } else if (overV) {// mandatory parts overlap vertically only
                if (mps_xi < mps_xj) {
                    vars[j].updateLowerBound(mpe_xi, aCause);
                    vars[i].updateUpperBound(mps_xj - vars[i + 2 * n].getLB(), aCause);
                    vars[i + 2 * n].updateUpperBound(mps_xj - vars[i].getLB(), aCause);
                } else {
                    vars[i].updateLowerBound(mpe_xj, aCause);
                    vars[j].updateUpperBound(mps_xi - vars[j + 2 * n].getLB(), aCause);
                    vars[j + 2 * n].updateUpperBound(mps_xi - vars[j].getLB(), aCause);
                }
            }
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            if (boxInstantiated(i))
                for (int j = i + 1; j < n; j++) {
                    if (boxInstantiated(j)) {
                        if (overlap(i, j)) {
                            return ESat.FALSE;
                        }
                    }
                }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    private boolean boxInstantiated(int i) {
        return vars[i].instantiated() && vars[i + n].instantiated()
                && vars[i + 2 * n].instantiated() && vars[i + 3 * n].instantiated();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DIFFN(");
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
