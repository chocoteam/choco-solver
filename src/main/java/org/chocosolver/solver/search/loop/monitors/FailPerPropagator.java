/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.IntMap;

/**
 * A counter which maintains the number of times a propagator fails during the resolution.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11/06/12
 */
public class FailPerPropagator implements IMonitorContradiction {

    /**
     * Map (propagator - weight), where weight is the number of times the propagator fails.
     */
    protected IntMap p2w;


    /**
     * Create an observer on propagators failures, based on the constraints in input
     * @param constraints set of constraints to observe
     * @param model the target model
     */
    public FailPerPropagator(Constraint[] constraints, Model model) {
        p2w = new IntMap(10, 0);
        init(constraints);
        model.getSolver().plugMonitor(this);
    }

    private void init(Constraint[] constraints) {
        for (Constraint cstr : constraints) {
            for (Propagator propagator : cstr.getPropagators()) {
                p2w.put(propagator.getId(), 0);
            }
        }
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        if (cex.c != null && cex.c instanceof Propagator) {
            p2w.putOrAdjust(((Propagator) cex.c).getId(), 1, 1);
        }
    }

    /**
     * Gets, for a given propagator, the number of times it has failed during the resolution
     * @param p the propagator to evaluate
     * @return the number of times <code>p</code> has failed from the beginning of the resolution
     */
    public int getFails(Propagator p) {
        int f = p2w.get(p.getId());
        return f < 0 ? 0 : f;
    }
}
