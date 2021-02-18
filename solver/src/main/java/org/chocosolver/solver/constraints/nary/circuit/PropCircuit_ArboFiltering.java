/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.circuit;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.dominance.AbstractLengauerTarjanDominatorsFinder;
import org.chocosolver.util.graphOperations.dominance.SimpleDominatorsFinder;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Random;

/**
 * Propagator for sub-circuit constraint based on dominators
 * Redundant propagator
 *
 * @author Jean-Guillaume Fages
 */
public class PropCircuit_ArboFiltering extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // flow graph
    protected DirectedGraph connectedGraph;
    // number of nodes
    protected int n;
    // dominators finder that contains the dominator tree
    protected AbstractLengauerTarjanDominatorsFinder domFinder;
    // offset (usually 0 but 1 with MiniZinc)
    protected int offSet;
    // random function
    protected Random rd;
    protected CircuitConf conf;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropCircuit_ArboFiltering(IntVar[] succs, int offSet, CircuitConf conf) {
        super(succs, PropagatorPriority.QUADRATIC, false);
        this.conf = conf;
        this.n = succs.length;
        this.offSet = offSet;
        this.connectedGraph = new DirectedGraph(n + 1, SetType.BITSET, false);
        domFinder = new SimpleDominatorsFinder(n, connectedGraph);
        if (conf == CircuitConf.RD) {
            rd = new Random(vars[0].getModel().getSeed());
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
        switch (conf) {
            case FIRST:
                filterFromDom(0);
                break;
            default:
            case RD:
                filterFromDom(rd.nextInt(n));
                break;
            case ALL:
                for (int i = 0; i < n; i++) {
                    filterFromDom(i);
                }
                break;
        }
    }

    protected void filterFromDom(int duplicatedNode) throws ContradictionException {
        for (int i = 0; i < n + 1; i++) {
            connectedGraph.getSuccOf(i).clear();
            connectedGraph.getPredOf(i).clear();
        }
        for (int i = 0; i < n; i++) {
            int ub = vars[i].getUB();
            for (int y = vars[i].getLB(); y <= ub; y = vars[i].nextValue(y)) {
                if (i == duplicatedNode) {
                    connectedGraph.addArc(n, y - offSet);
                } else {
                    connectedGraph.addArc(i, y - offSet);
                }
            }
        }
        if (domFinder.findDominators()) {
            for (int x = 0; x < n; x++) {
                if (x != duplicatedNode) {
                    int ub = vars[x].getUB();
                    for (int y = vars[x].getLB(); y <= ub; y = vars[x].nextValue(y)) {
                        if (domFinder.isDomminatedBy(x, y - offSet)) {
                            vars[x].removeValue(y, this);
                        }
                    }
                }
            }
        } else {
            // "the source cannot reach all nodes"
            fails();
        }
    }

    @Override
    public ESat isEntailed() {
        // redundant filtering
        return ESat.TRUE;
    }

}
