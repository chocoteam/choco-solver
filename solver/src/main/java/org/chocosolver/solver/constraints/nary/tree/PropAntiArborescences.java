/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.tree;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.dominance.AbstractLengauerTarjanDominatorsFinder;
import org.chocosolver.util.graphOperations.dominance.AlphaDominatorsFinder;
import org.chocosolver.util.graphOperations.dominance.SimpleDominatorsFinder;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * AntiArborescences propagation (simplification from tree constraint) based on dominators
 * loops (i.e., variables such that x[i]=i) are considered as roots
 * Can use the simple LT algorithm which runs in O(m.log(n)) worst case time
 * Or a slightly more sophisticated one, linear in theory but not necessarily faster in practice
 *
 * @author Jean-Guillaume Fages
 */
public class PropAntiArborescences extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // flow graph
    private final DirectedGraph connectedGraph;
    // number of nodes
    private final int n;
    // dominators finder that contains the dominator tree
    private final AbstractLengauerTarjanDominatorsFinder domFinder;
    // offset (usually 0 but 1 with MiniZinc)
    private final int offSet;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AntiArborescences propagation (simplification from tree constraint) based on dominators
     *
     * @param succs array of integer variables
     * @param offSet int
     * @param linear boolean
     */
    public PropAntiArborescences(IntVar[] succs, int offSet, boolean linear) {
        super(succs, PropagatorPriority.LINEAR, false);
        this.n = succs.length;
        this.offSet = offSet;
        this.connectedGraph = new DirectedGraph(n + 1, SetType.BITSET, false);
        if (linear) {
            domFinder = new AlphaDominatorsFinder(n, connectedGraph);
        } else {
            domFinder = new SimpleDominatorsFinder(n, connectedGraph);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		if (PropagatorEventType.isFullPropagation(evtmask)) {
			for (int i = 0; i < n; i++) {
                vars[i].updateBounds(offSet, n - 1 + offSet, this);
            }
		}
        structuralPruning();
    }

    private void structuralPruning() throws ContradictionException {
        for (int i = 0; i < n + 1; i++) {
            connectedGraph.getSuccessorsOf(i).clear();
            connectedGraph.getPredecessorsOf(i).clear();
        }
        for (int i = 0; i < n; i++) {
            int ub = vars[i].getUB();
            for (int y = vars[i].getLB(); y <= ub; y = vars[i].nextValue(y)) {
                if (i == y) { // can be a root node
                    connectedGraph.addEdge(i, n);
                } else {
                    connectedGraph.addEdge(i, y - offSet);
                }
            }
        }
        if (domFinder.findPostDominators()) {
            for (int x = 0; x < n; x++) {
                int ub = vars[x].getUB();
                for (int y = vars[x].getLB(); y <= ub; y = vars[x].nextValue(y)) {
                    if (x != y) {
                        if (domFinder.isDomminatedBy(y - offSet, x)) {
                            vars[x].removeValue(y, this);
                        }
                    }
                }
            }
        } else {
            // the source cannot reach all nodes
            fails();
        }
    }

    @Override
    public ESat isEntailed() {
        if (!isCompletelyInstantiated()) {
            return ESat.UNDEFINED;
        }
        ISet tmp = SetFactory.makeBitSet(0);
        for (int i = 0; i < n; i++) {
            if (circuit(tmp, i)) {
                return ESat.FALSE;
            }
        }
        return ESat.TRUE;
    }

    private boolean circuit(ISet tmp, int i) {
        tmp.clear();
        int x = i;
        tmp.add(x);
        int y = vars[x].getValue()-offSet;
        while (x != y) {
            x = y;
            if (tmp.contains(x)) {
                return true;
            }
            tmp.add(x);
            y = vars[x].getValue()-offSet;
        }
        return false;
    }

}
