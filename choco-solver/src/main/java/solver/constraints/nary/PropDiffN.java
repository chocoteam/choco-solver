/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package solver.constraints.nary;

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.events.IntEventType;
import solver.variables.events.PropagatorEventType;
import util.ESat;
import util.objects.graphs.UndirectedGraph;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;
import util.objects.setDataStructures.SetType;
import util.tools.ArrayUtils;

/**
 * @author Jean-Guillaume Fages
 * @since 31/01/13
 */
public class PropDiffN extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private UndirectedGraph overlappingBoxes;
    private ISet boxesToCompute;
    private boolean fast;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public PropDiffN(IntVar[] x, IntVar[] y, IntVar[] dx, IntVar[] dy, boolean fast) {
        super(ArrayUtils.append(x, y, dx, dy), PropagatorPriority.LINEAR, true);
        this.fast = fast;
        n = x.length;
        if (!(n == y.length && n == dx.length && n == dy.length)) {
            throw new UnsupportedOperationException();
        }
        overlappingBoxes = new UndirectedGraph(solver, n, SetType.LINKED_LIST, true);
        boxesToCompute = SetFactory.makeStoredSet(SetType.LINKED_LIST, n, solver);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int idx) {
        if (fast) return IntEventType.instantiation();
        return IntEventType.boundAndInst();
    }

	@Override
	public void propagate(int varIdx, int mask) throws ContradictionException {
		int v = varIdx % n;
		ISet s = overlappingBoxes.getNeighOf(v);
		for (int i = s.getFirstElement(); i >= 0; i = s.getNextElement()) {
			if (!mayOverlap(v, i)) {
				overlappingBoxes.removeEdge(v, i);
			}
		}
		if (!boxesToCompute.contain(v)) {
			boxesToCompute.add(v);
		}
		forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
	}

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int i = 0; i < n; i++) {
                overlappingBoxes.getNeighOf(i).clear();
            }
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (mayOverlap(i, j)) {
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

    private boolean mayOverlap(int i, int j) {
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

    protected void filterFromBox(int i) throws ContradictionException {
        ISet s = overlappingBoxes.getNeighOf(i);
        // check energy
        int xm = vars[i].getLB();
        int xM = vars[i].getUB() + vars[i + 2 * n].getUB();
        int ym = vars[i + n].getLB();
        int yM = vars[i + n].getUB() + vars[i + 3 * n].getUB();
        int am = vars[i + 2 * n].getLB() * vars[i + 3 * n].getLB();
        for (int j = s.getFirstElement(); j >= 0; j = s.getNextElement()) {
            xm = Math.min(xm, vars[j].getLB());
            xM = Math.max(xM, vars[j].getUB() + vars[j + 2 * n].getUB());
            ym = Math.min(ym, vars[j + n].getLB());
            yM = Math.max(yM, vars[j + n].getUB() + vars[j + 3 * n].getUB());
            am += vars[j + 2 * n].getLB() * vars[j + 3 * n].getLB();
            if (am > (xM - xm) * (yM - ym)) {
                contradiction(vars[i], "");
            }
        }
        // mandatory part based filtering
        boolean horizontal = true;
        boolean vertical = false;
        for (int j = s.getFirstElement(); j >= 0; j = s.getNextElement()) {
            if (doOverlap(i, j, horizontal)) {
                filter(i, j, vertical);
            }
            if (doOverlap(i, j, vertical)) {
                filter(i, j, horizontal);
            }
            assert !(doOverlap(i, j, horizontal) && doOverlap(i, j, vertical));
        }
    }

    private boolean doOverlap(int i, int j, boolean hori) {
        int offSet = hori ? 0 : n;
        int S_i = vars[i + offSet].getUB();
        int e_i = vars[i + offSet].getLB() + vars[i + 2 * n + offSet].getLB();
        int S_j = vars[j + offSet].getUB();
        int e_j = vars[j + offSet].getLB() + vars[j + 2 * n + offSet].getLB();
        return (S_i < e_i && e_j > S_i && S_j < e_i)
                || (S_j < e_j && e_i > S_j && S_i < e_j);
    }

    private void filter(int i, int j, boolean hori) throws ContradictionException {
        int offSet = hori ? 0 : n;
        int S_i = vars[i + offSet].getUB();
        int e_i = vars[i + offSet].getLB() + vars[i + 2 * n + offSet].getLB();
        int S_j = vars[j + offSet].getUB();
        int e_j = vars[j + offSet].getLB() + vars[j + 2 * n + offSet].getLB();
        if (S_i < e_i || S_j < e_j) {
            if (e_j > S_i) {
                vars[j + offSet].updateLowerBound(e_i, aCause);
                vars[i + offSet].updateUpperBound(S_j - vars[i + 2 * n + offSet].getLB(), aCause);
                vars[i + offSet + 2 * n].updateUpperBound(S_j - vars[i + offSet].getLB(), aCause);
            }
            if (S_j < e_i) {
                vars[i + offSet].updateLowerBound(e_j, aCause);
                vars[j + offSet].updateUpperBound(S_i - vars[j + 2 * n + offSet].getLB(), aCause);
                vars[j + offSet + 2 * n].updateUpperBound(S_i - vars[j + offSet].getLB(), aCause);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            if (boxInstantiated(i))
                for (int j = i + 1; j < n; j++) {
                    if (boxInstantiated(j)) {
                        if (mayOverlap(i, j)) {
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
        return vars[i].isInstantiated() && vars[i + n].isInstantiated()
                && vars[i + 2 * n].isInstantiated() && vars[i + 3 * n].isInstantiated();
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

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.n;
            IntVar[] X = new IntVar[size];
            IntVar[] Y = new IntVar[size];
            IntVar[] dX = new IntVar[size];
            IntVar[] dY = new IntVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                X[i] = (IntVar) identitymap.get(this.vars[i]);
                this.vars[i + n].duplicate(solver, identitymap);
                Y[i] = (IntVar) identitymap.get(this.vars[i + n]);
                this.vars[i + 2 * n].duplicate(solver, identitymap);
                dX[i] = (IntVar) identitymap.get(this.vars[i + 2 * n]);
                this.vars[i + 3 * n].duplicate(solver, identitymap);
                dY[i] = (IntVar) identitymap.get(this.vars[i + 3 * n]);
            }
            identitymap.put(this, new PropDiffN(X, Y, dX, dY, this.fast));
        }
    }
}
