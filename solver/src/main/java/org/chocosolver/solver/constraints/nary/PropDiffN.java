/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
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

    private final int n;
    private final UndirectedGraph overlappingBoxes;
    private final TIntArrayList boxesToCompute;
    private final TIntArrayList pruneList;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public PropDiffN(IntVar[] x, IntVar[] y, IntVar[] dx, IntVar[] dy) {
        super(ArrayUtils.append(x, y, dx, dy), PropagatorPriority.LINEAR, true);
        n = x.length;
        if (!(n == y.length && n == dx.length && n == dy.length)) {
            throw new SolverException("PropDiffN variable arrays do not have same size");
        }
        overlappingBoxes = new UndirectedGraph(model, n, SetType.LINKED_LIST, true);
        boxesToCompute = new TIntArrayList(n);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (mayOverlap(i, j)) {
                    overlappingBoxes.addEdge(i, j);
                }
            }
        }
        pruneList = new TIntArrayList(n);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int idx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        prop(varIdx);
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    private void prop(int varIdx) {
        int v = varIdx % n;
        ISetIterator iter = overlappingBoxes.getNeighborsOf(v).iterator();
        while (iter.hasNext()) {
            int i = iter.nextInt();
            if (!mayOverlap(v, i)) {
                overlappingBoxes.removeEdge(v, i);
            }
        }
        if (!boxesToCompute.contains(v)) {
            boxesToCompute.add(v);
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        boolean hasFiltered = true;
        while(hasFiltered) {
            hasFiltered = false;
            if(PropagatorEventType.isFullPropagation(evtmask)) {
                boxesToCompute.resetQuick();
                for (int i = 0; i < n; i++) {
                    boxesToCompute.add(i);
                    for (int j = i + 1; j < n; j++) {
                        if (mayOverlap(i, j)) {
                            overlappingBoxes.addEdge(i, j);
                            if (boxInstantiated(i) && boxInstantiated(j)) {
                                fails(); // TODO: could be more precise, for explanation purpose
                            }
                        } else {
                            overlappingBoxes.removeEdge(i, j);
                        }
                    }
                }
            }
            pruneList.clear();
            for(int k = 0; k<boxesToCompute.size(); k++)  {
                int i = boxesToCompute.getQuick(k);
                energyCheck(i);
                hasFiltered |= prune(i);
            }
            boxesToCompute.clear();
            for(int k = 0; k< pruneList.size(); k++) {
                prop(pruneList.getQuick(k));
            }
        }
    }

    private boolean prune(int j) throws ContradictionException {
        boolean hasFiltered = false;
        ISetIterator iter = overlappingBoxes.getNeighborsOf(j).iterator();
        while (iter.hasNext()) {
            int i = iter.nextInt();
            if(doOverlap(i, j, true)) {
                hasFiltered |= filter(i, j, false);
            }
            if(doOverlap(i, j, false)) {
                hasFiltered |= filter(i, j, true);
            }
        }
        return hasFiltered;
    }

    private void energyCheck(int i) throws ContradictionException {
        int xm = vars[i].getLB();
        int xM = vars[i].getUB() + vars[i + 2 * n].getUB();
        int ym = vars[i + n].getLB();
        int yM = vars[i + n].getUB() + vars[i + 3 * n].getUB();
        int am = vars[i + 2 * n].getLB() * vars[i + 3 * n].getLB();
        int xLengthMin = vars[i + 2 * n].getLB();
        int yLengthMin = vars[i + 3 * n].getLB();
        ISetIterator iter = overlappingBoxes.getNeighborsOf(i).iterator();
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
            xLengthMin = Math.min(xLengthMin, vars[j + 2 * n].getLB());
            yLengthMin = Math.min(yLengthMin, vars[j + 3 * n].getLB());
        }

        if (xLengthMin > 0 && yLengthMin > 0) {
            int maxNumberRectangles = ((xM - xm) / xLengthMin) * ((yM - ym) / yLengthMin);
            if (maxNumberRectangles < overlappingBoxes.getNeighborsOf(i).size()+1) {
                fails();
            }
        }
    }

    private boolean mayOverlap(int i, int j) {
        return isNotDisjoint(i, j, true) && isNotDisjoint(i, j, false);
    }

    private boolean isNotDisjoint(int i, int j, boolean horizontal) {
        int off = (horizontal) ? 0 : n;
        return (vars[i + off].getLB() < vars[j + off].getUB() + vars[j + off + 2 * n].getUB())
            && (vars[j + off].getLB() < vars[i + off].getUB() + vars[i + off + 2 * n].getUB());
    }

    private boolean doOverlap(int i, int j, boolean hori) {
        int offSet = hori ? 0 : n;
        int s_i = vars[i + offSet].getUB();
        int e_i = vars[i + offSet].getLB() + vars[i + 2 * n + offSet].getLB();
        int s_j = vars[j + offSet].getUB();
        int e_j = vars[j + offSet].getLB() + vars[j + 2 * n + offSet].getLB();
        return (s_i < e_i && e_j > s_i && s_j < e_i)
            || (s_j < e_j && e_i > s_j && s_i < e_j);
    }

    private boolean filter(int i, int j, boolean hori) throws ContradictionException {
        boolean hasFiltered = false;
        int offSet = hori ? 0 : n;
        int s_i = vars[i + offSet].getUB();
        int e_i = vars[i + offSet].getLB() + vars[i + 2 * n + offSet].getLB();
        int s_j = vars[j + offSet].getUB();
        int e_j = vars[j + offSet].getLB() + vars[j + 2 * n + offSet].getLB();
        if (s_i < e_i || s_j < e_j) {
            if (e_j > s_i) {
                if(vars[j + offSet].updateLowerBound(e_i, this)) {
                    if(!pruneList.contains(j)) {
                        pruneList.add(j);
                    }
                    hasFiltered = true;
                }
                boolean filtPrun1 = vars[i + offSet].updateUpperBound(s_j - vars[i + 2 * n + offSet].getLB(), this);
                boolean filtPrun2 = vars[i + offSet + 2 * n].updateUpperBound(s_j - vars[i + offSet].getLB(), this);
                if(filtPrun1 || filtPrun2) {
                    if(!pruneList.contains(i)) {
                        pruneList.add(i);
                    }
                    hasFiltered = true;
                }
            }
            if (s_j < e_i) {
                if(vars[i + offSet].updateLowerBound(e_j, this)) {
                    if(!pruneList.contains(i)) {
                        pruneList.add(i);
                    }
                    hasFiltered = true;
                }
                boolean filtPrun1 = vars[j + offSet].updateUpperBound(s_i - vars[j + 2 * n + offSet].getLB(), this);
                boolean filtPrun2 = vars[j + offSet + 2 * n].updateUpperBound(s_i - vars[j + offSet].getLB(), this);
                if(filtPrun1 || filtPrun2) {
                    if(!pruneList.contains(j)) {
                        pruneList.add(j);
                    }
                    hasFiltered = true;
                }
            }
        }
        return hasFiltered;
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
