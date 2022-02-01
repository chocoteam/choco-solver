/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.graph.G;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.mis.F;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.rules.R;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;

import static org.chocosolver.solver.constraints.PropagatorPriority.CUBIC;
import static org.chocosolver.util.tools.ArrayUtils.concat;

/**
 * Propagator for the atMostNValues constraint
 * The number of distinct values in the set of variables vars is at most equal to nValues
 *
 * @author Jean-Guillaume Fages
 * @since 01/01/2014
 */
public class PropAMNV extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final G graph;
    private final F heur;
    private final R[] rules;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates a propagator for the atMostNValues constraint
     * The number of distinct values in X is at most equal to N
     */
    public PropAMNV(IntVar[] X, IntVar N, G graph, F heur, R[] rules) {
        super(concat(X, N), CUBIC, true);
        this.graph = graph;
        this.heur = heur;
        this.rules = rules;
        graph.build();
    }

    //***********************************************************************************
    // ALGORITHMS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int i) {
        return IntEventType.all();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            graph.update();
        }
        heur.prepare();
        do {
            heur.computeMIS();
            for (R rule : rules) {
                rule.filter(vars, graph, heur, this);
            }
        } while (heur.hasNextMIS());
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < vars.length - 1) {
            graph.update(idxVarInProp);
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        // this is only a redundant propagator (solution checking uses the default NValue propagator)
        return ESat.TRUE;
    }

}
