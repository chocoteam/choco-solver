/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.VariableUtils;

import java.util.Comparator;

/**
 * Implementation of DowOverWDeg[1].
 * <p>
 * [1]: F. Boussemart, F. Hemery, C. Lecoutre, and L. Sais, Boosting Systematic Search by Weighting
 * Constraints, ECAI-04. <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/12
 */
public class DomOverWDeg<V extends Variable> extends AbstractFailureBasedVariableSelector<V> {

    /**
     * Creates a DomOverWDeg variable selector.
     * The default tiebreaker is lexical ordering.
     * The default flush rate is 32.
     *
     * @param variables decision variables
     */
    public DomOverWDeg(V[] variables) {
        this(variables, (v1, v2)->0, 32);
    }

    /**
     * Creates a DomOverWDeg variable selector
     * @param variables scope variables
     * @param tieBreaker a tiebreaker comparator when two variables have the same score
     * @param flushRate the number of restarts before forgetting scores
     */
    public DomOverWDeg(V[] variables, Comparator<V> tieBreaker, int flushRate) {
        super(variables, tieBreaker, flushRate);
    }


    @Override
    public final boolean init() {
        if (!solver.getSearchMonitors().contains(this)) {
            solver.plugMonitor(this);
        }
        return true;
    }

    @Override
    public final void remove() {
        if (solver.getSearchMonitors().contains(this)) {
            solver.unplugMonitor(this);
        }
    }

    @Override
    protected final double score(Variable v) {
        //assert weightW(v) == weights.get(v) : "wrong weight for " + v + ", expected " + weightW(v) + ", but found " + weights.get(v);
        return 1 + weights.get(v);
    }


    @Override
    void increase(Propagator<?> prop, Element elt, double[] ws) {
        // Increase weights of all variables in this propagator
        // even if they are already instantiated
        int s = prop.getModel().getEnvironment().getWorldIndex();
        int dj = prop.getVar(elt.ws[0]).instantiationWorldIndex();
        int dk = prop.getVar(elt.ws[1]).instantiationWorldIndex();
        boolean futVar1 = Math.min(dj, dk) < s; // that is, futvars == 1 until we reach 'dk'
        for (int i = 0; i < prop.getNbVars(); i++) {
            if (prop.getVar(i).isAConstant() || !VariableUtils.isInt(prop.getVar(i))) continue;
            IntVar ivar = (IntVar) prop.getVar(i);
            // recall that variable at 0 is the 'deepest' one
            if (i == elt.ws[0] && futVar1) {
                // it should be restored upon backtrack
                environment.saveAt(() -> weights.adjustOrPutValue(ivar, 1., 1.), dk);
            } else {
                weights.adjustOrPutValue(ivar, 1., 1.);
            }
            ws[i] += 1;
        }
    }

    @Override
    final int remapInc() {
        return 1;
    }
    
    @Override
    public void flushScores() {
        weights.forEachEntry((a1, b) -> {
            weights.put(a1, 0.);
            return true;
        });
    }

    // <-- FOR DEBUGGING PURPOSE ONLY
    /*double weightW(IntVar v) {
        int w = 0;
        int nbp = v.getNbProps();
        for (int i = 0; i < nbp; i++) {
            Propagator<?> prop = v.getPropagator(i);
            // BEWARE: propagators that accept to add dynamically variables led to trouble
            // when it comes to compute their weight incrementally.
            w += futvarsW(prop);
        }
        return w;
    }

    int futvarsW(Propagator<?> prop) {
        int futVars = 0;
        for (int i = 0; i < prop.getNbVars(); i++) {
            if (!prop.getVar(i).isInstantiated()) {
                if (++futVars > 1) {
                    Element elt = failCount.get(prop);
                    if (elt != null) {
                        return elt.ws[2];
                    } else break;
                }
            }
        }
        return 0;
    }*/
    // FOR DEBUGGING PURPOSE ONLY  -->
}
