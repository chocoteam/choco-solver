/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.channeling.nodes;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * Channeling between a graph variable and set variables
 * representing either node neighbors or node successors
 *
 * @author Jean-Guillaume Fages
 */
public class PropNodeBoolChannel extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final BoolVar bool;
    private final int vertex;
    private final GraphVar g;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNodeBoolChannel(BoolVar isIn, int vertex, GraphVar gV) {
        super(new Variable[]{isIn, gV}, PropagatorPriority.UNARY, false);
        this.bool = isIn;
        this.vertex = vertex;
        this.g = gV;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 1) {
            return GraphEventType.ADD_NODE.getMask() + GraphEventType.REMOVE_NODE.getMask();
        } else {
            return IntEventType.all();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (vertex < 0 || vertex >= g.getNbMaxNodes() || !g.getPotentialNodes().contains(vertex)) {
            bool.setToFalse(this);
        } else if (g.getMandatoryNodes().contains(vertex)) {
            bool.setToTrue(this);
        } else if (bool.getLB() == 1) {
            g.enforceNode(vertex, this);
        } else if (bool.getUB() == 0) {
            g.removeNode(vertex, this);
        }
    }

    @Override
    public ESat isEntailed() {
        if ((vertex < 0 || vertex >= g.getNbMaxNodes())
                || (bool.getLB() == 1 && !g.getPotentialNodes().contains(vertex))
                || (bool.getUB() == 0 && g.getMandatoryNodes().contains(vertex))
                ) {
            return ESat.FALSE;
        }
        if (bool.isInstantiated()
                && g.getMandatoryNodes().contains(vertex) == g.getPotentialNodes().contains(vertex)) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
