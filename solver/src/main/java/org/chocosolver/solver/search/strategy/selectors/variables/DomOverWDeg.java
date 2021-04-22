/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.IntMap;

import java.util.stream.Stream;

/**
 * Implementation of DowOverWDeg[1].
 *
 * [1]: F. Boussemart, F. Hemery, C. Lecoutre, and L. Sais, Boosting Systematic Search by Weighting
 * Constraints, ECAI-04. <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/12
 */
@SuppressWarnings("rawtypes")
public class DomOverWDeg extends AbstractCriterionBasedVariableSelector implements IMonitorContradiction {

    /**
     * Map (propagator - weight), where weight is the number of times the propagator fails.
     */
    protected IntMap p2w;

    /**
     * Creates a DomOverWDeg variable selector
     *
     * @param variables     decision variables
     * @param seed          seed for breaking ties randomly
     */
    public DomOverWDeg(IntVar[] variables, long seed) {
        super(variables, seed);
        Model model = variables[0].getModel();
        p2w = new IntMap(10, 0);
        init(Stream.of(model.getCstrs())
                .flatMap(c -> Stream.of(c.getPropagators()))
                .toArray(Propagator[]::new));
    }

    private void init(Propagator[] propagators) {
        for (Propagator propagator : propagators) {
            p2w.put(propagator.getId(), 0);
        }
    }

    @Override
    public boolean init() {
        if(!solver.getSearchMonitors().contains(this)) {
            solver.plugMonitor(this);
        }
        return true;
    }

    @Override
    public void remove() {
        if(solver.getSearchMonitors().contains(this)) {
            solver.unplugMonitor(this);
        }
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        if (cex.c instanceof Propagator) {
            Propagator p = (Propagator) cex.c;
            p2w.putOrAdjust(p.getId(), 1, 1);
        }
    }

    @Override
    protected int weight(IntVar v) {
        int w = 0;
        int nbp = v.getNbProps();
        for (int i = 0; i < nbp; i++) {
            Propagator<?> prop = v.getPropagator(i);
            if (futVars(prop) > 1) {
                w += p2w.get(prop.getId());
            }
        }
        return w;
    }
}
