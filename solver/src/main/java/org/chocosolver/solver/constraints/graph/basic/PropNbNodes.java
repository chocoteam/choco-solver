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

import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Propagator that ensures that k nodes belong to the final graph
 *
 * @author Jean-Guillaume Fages
 */
public class PropNbNodes extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final GraphVar g;
    private final IntVar k;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNbNodes(GraphVar graph, IntVar k) {
        super(new Variable[]{graph, k}, PropagatorPriority.LINEAR, false);
        this.g = graph;
        this.k = k;
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int env = g.getPotentialNodes().size();
        int ker = g.getMandatoryNodes().size();
        k.updateLowerBound(ker, this);
        k.updateUpperBound(env, this);
        if (ker == env) {
            setPassive();
        } else if (k.isInstantiated()) {
            int v = k.getValue();
            ISet envNodes = g.getPotentialNodes();
            if (v == env) {
                for (int i : envNodes) {
                    g.enforceNode(i, this);
                }
                setPassive();
            } else if (v == ker) {
                ISet kerNodes = g.getMandatoryNodes();
                for (int i : envNodes) {
                    if (!kerNodes.contains(i)) {
                        g.removeNode(i, this);
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
            return GraphEventType.REMOVE_NODE.getMask() + GraphEventType.ADD_NODE.getMask();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public ESat isEntailed() {
        int env = g.getPotentialNodes().size();
        int ker = g.getMandatoryNodes().size();
        if (env < k.getLB() || ker > k.getUB()) {
            return ESat.FALSE;
        }
        if (env == ker) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
