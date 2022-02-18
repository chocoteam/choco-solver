/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.channeling.edges;

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
 * @author Jean-Guillaume Fages
 */
public class PropEdgeBoolChannel extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final BoolVar bool;
    private final int from;
    private final int to;
    private final GraphVar g;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropEdgeBoolChannel(BoolVar isIn, int from, int to, GraphVar gV) {
        super(new Variable[]{isIn, gV}, PropagatorPriority.UNARY, false);
        this.bool = isIn;
        this.from = from;
        this.to = to;
        this.g = gV;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 1) {
            return GraphEventType.ADD_EDGE.getMask() + GraphEventType.REMOVE_EDGE.getMask();
        } else {
            return IntEventType.all();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (from < 0 || to < 0 || from >= g.getNbMaxNodes() || to >= g.getNbMaxNodes()
                || !g.getPotentialSuccessorsOf(from).contains(to)) {
            bool.setToFalse(this);
        } else if (g.getMandatorySuccessorsOf(from).contains(to)) {
            bool.setToTrue(this);
        } else if (bool.getLB() == 1) {
            g.enforceEdge(from, to, this);
        } else if (bool.getUB() == 0) {
            g.removeEdge(from, to, this);
        }
    }

    @Override
    public ESat isEntailed() {
        if ((from < 0 || from >= g.getNbMaxNodes() || to < 0 || to >= g.getNbMaxNodes())
                || (bool.getLB() == 1 && !g.getPotentialSuccessorsOf(from).contains(to))
                || (bool.getUB() == 0 && g.getMandatorySuccessorsOf(from).contains(to))
                ) {
            return ESat.FALSE;
        }
        if (bool.isInstantiated()
                && g.getMandatorySuccessorsOf(from).contains(to) == g.getPotentialSuccessorsOf(from).contains(to)) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
