/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.connectivity;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.StrongConnectivityFinder;

/**
 * Propagator that ensures that the final graph consists in K Strongly Connected Components (SCC)
 * <p/>
 * simple checker and a bit of pruning (runs in linear time)
 *
 * @author Jean-Guillaume Fages
 */
public class PropNbSCC extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final DirectedGraphVar g;
    private final IntVar k;
    private final StrongConnectivityFinder envCCFinder;
    private final StrongConnectivityFinder kerCCFinder;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNbSCC(DirectedGraphVar graph, IntVar k) {
        super(new Variable[]{graph, k}, PropagatorPriority.LINEAR, false);
        this.g = graph;
        this.k = k;
        envCCFinder = new StrongConnectivityFinder(g.getUB());
        kerCCFinder = new StrongConnectivityFinder(g.getLB());
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // trivial case
        k.updateLowerBound(0, this);
        if (g.getPotentialNodes().size() == 0) {
            k.instantiateTo(0, this);
            return;
        }
        if (k.getUB() == 0) {
            for (int i : g.getPotentialNodes()) {
                g.removeNode(i, this);
            }
            return;
        }

        // bound computation
        int min = minCC();
        int max = maxCC();
        k.updateLowerBound(min, this);
        k.updateUpperBound(max, this);

        // A bit of pruning (removes unreachable nodes)
        if (k.getUB() == min && min != max) {
            int ccs = envCCFinder.getNbSCC();
            boolean pot = true;
            for (int cc = 0; cc < ccs; cc++) {
                for (int i = envCCFinder.getSCCFirstNode(cc); i >= 0 && pot; i = envCCFinder.getNextNode(i)) {
                    if (g.getMandatoryNodes().contains(i)) {
                        pot = false;
                    }
                }
                if (pot) {
                    for (int i = envCCFinder.getSCCFirstNode(cc); i >= 0; i = envCCFinder.getNextNode(i)) {
                        g.removeNode(i, this);
                    }
                }
            }
        }
    }

    public int minCC() {
        envCCFinder.findAllSCC();
        int ccs = envCCFinder.getNbSCC();
        int minCC = 0;
        for (int cc = 0; cc < ccs; cc++) {
            for (int i = envCCFinder.getSCCFirstNode(cc); i >= 0; i = envCCFinder.getNextNode(i)) {
                if (g.getMandatoryNodes().contains(i)) {
                    minCC++;
                    break;
                }
            }
        }
        return minCC;
    }

    public int maxCC() {
        kerCCFinder.findAllSCC();
        int nbK = kerCCFinder.getNbSCC();
        int delta = g.getPotentialNodes().size() - g.getMandatoryNodes().size();
        return nbK + delta;
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        if (k.getUB() < minCC() || k.getLB() > maxCC()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
