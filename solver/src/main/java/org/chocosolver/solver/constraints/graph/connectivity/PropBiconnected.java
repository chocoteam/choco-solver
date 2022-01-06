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
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.ConnectivityFinder;


/**
 * Propagator for enforcing a graph variable to be bi-connected
 * The empty graph is not biconnected, so as graphs with only one node.
 *
 * @author Jean-Guillaume Fages
 */
public class PropBiconnected extends Propagator<UndirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final UndirectedGraphVar g;
    private final ConnectivityFinder env_CC_finder;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropBiconnected(UndirectedGraphVar graph) {
        super(new UndirectedGraphVar[]{graph}, PropagatorPriority.LINEAR, false);
        this.g = graph;
        env_CC_finder = new ConnectivityFinder(g.getUB());
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (g.getPotentialNodes().size() == g.getMandatoryNodes().size() && !env_CC_finder.isBiconnected()) {
            fails();
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.REMOVE_NODE.getMask() + GraphEventType.REMOVE_EDGE.getMask() + GraphEventType.ADD_NODE.getMask();
    }

    @Override
    public ESat isEntailed() {
        if (!env_CC_finder.isBiconnected()) {
            return ESat.FALSE;
        }
        if (g.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
