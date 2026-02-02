/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
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

/**
 * Implementation of refined DowOverWDeg.
 *
 * @author Charles Prud'homme
 * @implNote This is based on "Refining Constraint Weighting." Wattez et al. ICTAI 2019.
 * <a href="https://dblp.org/rec/conf/ictai/WattezLPT19">https://dblp.org/rec/conf/ictai/WattezLPT19</a>
 * @since 12/06/20
 */
public class DomOverWDegRef<V extends Variable> extends DomOverWDeg<V> {

    /**
     * Creates a DomOverWDegRef variable selector with "CACD" as weight incrementer.
     *
     * @param variables decision variables
     * @param seed      seed for breaking ties randomly
     */
    public DomOverWDegRef(V[] variables, long seed) {
        super(variables, seed);
    }

    /**
     * Creates a DomOverWDegRef variable selector with "CACD" as weight incrementer.
     *
     * @param variables decision variables
     * @param seed      seed for breaking ties randomly
     * @param flushThs  flush threshold, when reached, it flushes scores
     */
    public DomOverWDegRef(V[] variables, long seed, int flushThs) {
        super(variables, seed, flushThs);
    }

    /**
     * @implNote This is the reason this class exists.
     * The only difference with {@link DomOverWDeg} is the increment
     * which is not 1 for each variable.
     */
    @Override
    void increase(Propagator<?> prop, Element elt, double[] ws) {
        int s = prop.getModel().getEnvironment().getWorldIndex();
        int dj = prop.getVar(elt.ws[0]).instantiationWorldIndex();
        int dk = prop.getVar(elt.ws[1]).instantiationWorldIndex();
        boolean futVar1 = Math.min(dj, dk) < s; // that is, futvars == 1 until we reach 'dk'
        long futvars = 0;
        for (int i = 0; i < prop.getNbVars(); i++) {
            futvars += prop.getVar(i).isInstantiated() ? 0 : 1;
        }
        for (int i = 0; i < prop.getNbVars() && futvars > 0; i++) {
            if (prop.getVar(i).isAConstant() || !VariableUtils.isInt(prop.getVar(i))) continue;
            // variables instanced in previous worlds are not incremented
            if (prop.getVar(i).instantiationWorldIndex() < s) continue;
            IntVar ivar = (IntVar) prop.getVar(i);
            // recall that variable at 0 is the 'deepest' one
            final double inc = 1.0 / (futvars * (ivar.getDomainSize() == 0 ? 0.5 : ivar.getDomainSize()));
            // recall that variable at 0 is the 'deepest' one
            if (i == elt.ws[0] && futVar1) {
                // it should be restored upon backtrack
                environment.saveAt(() -> weights.inc(ivar, inc), dk);
            } else {
                weights.inc(ivar, inc);
            }
            ws[i] += inc;
        }
    }


    // <-- FOR DEBUGGING PURPOSE ONLY
    /*@Override
    double futvarsW(Propagator<?> prop, Variable v) {
        int futVars = 0;
        for (int i = 0; i < prop.getNbVars(); i++) {
            if (!prop.getVar(i).isInstantiated()) {
                if (++futVars > 1) {
                    Element elt = failCount.get(prop);
                    if (elt != null) {
                        int j = 0;
                        while (j < prop.getNbVars() && prop.getVar(j) != v) {
                            j++;
                        }
                        if (j < prop.getNbVars()) {
                            // recall that variable at 0 is the 'deepest' one
                            double[] ws = refinedWeights.compute(prop, remapWeights);
                            return ws[j];
                        }
                    } else break;
                }
            }
        }
        return 0.;
    } */
    // FOR DEBUGGING PURPOSE ONLY  -->

}
