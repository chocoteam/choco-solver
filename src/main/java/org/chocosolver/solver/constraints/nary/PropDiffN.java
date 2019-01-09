/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;

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
            throw new SolverException("PropDiffN variable arrays do not have same size");
        }
        overlappingBoxes = new UndirectedGraph(model, n, SetType.LINKED_LIST, true);
        boxesToCompute = SetFactory.makeStoredSet(SetType.LINKED_LIST, 0, model);
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
        ISetIterator iter = overlappingBoxes.getNeighOf(v).iterator();
        while (iter.hasNext()) {
            int i = iter.nextInt();
			if (!mayOverlap(v, i)) {
				overlappingBoxes.removeEdge(v, i);
			}
		}
		if (!boxesToCompute.contains(v)) {
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
                            fails(); // TODO: could be more precise, for explanation purpose
                        }
                    }
                }
            }
            boxesToCompute.clear();
            for (int i = 0; i < n; i++) {
                boxesToCompute.add(i);
            }
        }
        ISetIterator iter = boxesToCompute.iterator();
        while (iter.hasNext()) {
            filterFromBox(iter.nextInt());
        }
        boxesToCompute.clear();
    }

    private boolean mayOverlap(int i, int j) {
        return isNotDisjoint(i, j, true) && isNotDisjoint(i, j, false);
    }

    private boolean isNotDisjoint(int i, int j, boolean horizontal) {
        int off = (horizontal) ? 0 : n;
        return (vars[i + off].getLB() < vars[j + off].getUB() + vars[j + off + 2 * n].getUB())
                && (vars[j + off].getLB() < vars[i + off].getUB() + vars[i + off + 2 * n].getUB());
    }

    protected void filterFromBox(int i) throws ContradictionException {
        // check energy
        int xm = vars[i].getLB();
        int xM = vars[i].getUB() + vars[i + 2 * n].getUB();
        int ym = vars[i + n].getLB();
        int yM = vars[i + n].getUB() + vars[i + 3 * n].getUB();
        int am = vars[i + 2 * n].getLB() * vars[i + 3 * n].getLB();
        ISetIterator iter = overlappingBoxes.getNeighOf(i).iterator();
        while (iter.hasNext()) {
            int j = iter.nextInt();
            xm = Math.min(xm, vars[j].getLB());
            xM = Math.max(xM, vars[j].getUB() + vars[j + 2 * n].getUB());
            ym = Math.min(ym, vars[j + n].getLB());
            yM = Math.max(yM, vars[j + n].getUB() + vars[j + 3 * n].getUB());
            am += vars[j + 2 * n].getLB() * vars[j + 3 * n].getLB();
            if (am > (xM - xm) * (yM - ym)) {
                fails(); // TODO: could be more precise, for explanation purpose
            }
        }
        // mandatory part based filtering
        boolean horizontal = true;
        boolean vertical = false;
        iter = overlappingBoxes.getNeighOf(i).iterator(); // reset iteration
        while (iter.hasNext()) {
            int j = iter.nextInt();
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
                vars[j + offSet].updateLowerBound(e_i, this);
                vars[i + offSet].updateUpperBound(S_j - vars[i + 2 * n + offSet].getLB(), this);
                vars[i + offSet + 2 * n].updateUpperBound(S_j - vars[i + offSet].getLB(), this);
            }
            if (S_j < e_i) {
                vars[i + offSet].updateLowerBound(e_j, this);
                vars[j + offSet].updateUpperBound(S_i - vars[j + 2 * n + offSet].getLB(), this);
                vars[j + offSet + 2 * n].updateUpperBound(S_i - vars[j + offSet].getLB(), this);
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
            sb.append("[").append(vars[i].toString());
            sb.append(",").append(vars[i + n].toString());
            sb.append(",").append(vars[i + 2 * n].toString());
            sb.append(",").append(vars[i + 3 * n].toString()).append("]");
        }
        sb.append(")");
        return sb.toString();
    }

}
