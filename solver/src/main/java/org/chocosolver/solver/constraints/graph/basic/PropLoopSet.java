/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.basic;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Propagator to catch the set of loops in a set variable
 *
 * @author Jean-Guillaume Fages
 */
public class PropLoopSet extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final GraphVar g;
    private final SetVar loops;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropLoopSet(GraphVar graph, SetVar loops) {
        super(new Variable[]{graph, loops}, PropagatorPriority.LINEAR, false);
        this.g = graph;
        this.loops = loops;
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ISet nodes = g.getPotentialNodes();
        for (int i : nodes) {
            if (g.getMandatorySuccessorsOf(i).contains(i)) { // mandatory loop detected
                loops.force(i, this);
            } else if (!g.getPotentialSuccessorsOf(i).contains(i)) { // no potential loop
                loops.remove(i, this);
            } else if (loops.getLB().contains(i)) {
                g.enforceEdge(i, i, this);
            } else if (!loops.getUB().contains(i)) {
                g.removeEdge(i, i, this);
            }
        }
        for (int i : loops.getUB()) {
            if (!nodes.contains(i)) {
                loops.remove(i, this);
            }
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        for (int i : loops.getLB()) {
            if (!g.getPotentialSuccessorsOf(i).contains(i)) {
                return ESat.FALSE;
            }
        }
        for (int i : g.getMandatoryNodes()) {
            if (g.getMandatorySuccessorsOf(i).contains(i) && !loops.getUB().contains(i)) {
                return ESat.FALSE;
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
