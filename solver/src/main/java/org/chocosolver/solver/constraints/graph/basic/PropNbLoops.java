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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Propagator that ensures that k loops belong to the final graph
 *
 * @author Jean-Guillaume Fages
 */
public class PropNbLoops extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final GraphVar g;
    private final IntVar k;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNbLoops(GraphVar graph, IntVar k) {
        super(new Variable[]{graph, k}, PropagatorPriority.LINEAR, false);
        this.g = graph;
        this.k = k;
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int min = 0;
        int max = 0;
        ISet nodes = g.getPotentialNodes();
        for (int i : nodes) {
            if (g.getMandatorySuccessorsOf(i).contains(i)) {
                min++;
                max++;
            } else if (g.getPotentialSuccessorsOf(i).contains(i)) {
                max++;
            }
        }
        k.updateLowerBound(min, this);
        k.updateUpperBound(max, this);
        if (min == max) {
            setPassive();
        } else if (k.isInstantiated()) {
            if (k.getValue() == max) {
                for (int i : nodes) {
                    if (g.getPotentialSuccessorsOf(i).contains(i)) {
                        g.enforceEdge(i, i, this);
                    }
                }
                setPassive();
            } else if (k.getValue() == min) {
                for (int i : nodes) {
                    if (!g.getMandatorySuccessorsOf(i).contains(i)) {
                        g.removeEdge(i, i, this);
                    }
                }
                setPassive();
            }
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return GraphEventType.REMOVE_EDGE.getMask() + GraphEventType.ADD_EDGE.getMask();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public ESat isEntailed() {
        int min = 0;
        int max = 0;
        ISet env = g.getPotentialNodes();
        for (int i : env) {
            if (g.getMandatorySuccessorsOf(i).contains(i)) {
                min++;
                max++;
            } else if (g.getPotentialSuccessorsOf(i).contains(i)) {
                max++;
            }
        }
        if (k.getLB() > max || k.getUB() < min) {
            return ESat.FALSE;
        }
        if (min == max) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
