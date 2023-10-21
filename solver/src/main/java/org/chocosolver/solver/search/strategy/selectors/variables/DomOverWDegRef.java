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
     * The default tiebreaker is lexical ordering.
     * The default flush rate is 32.
     *
     * @param variables decision variables
     */
    public DomOverWDegRef(V[] variables) {
        this(variables, (v1, v2) -> 0, 32);
    }

    /**
     * Creates a DomOverWDegRef variable selector with "CACD" as weight incrementer.
     *
     * @param variables  scope variables
     * @param tieBreaker a tiebreaker comparator when two variables have the same score
     * @param flushRate  the number of restarts before forgetting scores
     */
    public DomOverWDegRef(V[] variables, Comparator<V> tieBreaker, int flushRate) {
        super(variables, tieBreaker, flushRate);
    }

    /**
     * @implNote This is the reason this class exists.
     * The only difference with {@link DomOverWDeg} is the increment
     * which is not 1 for each variable.
     */
    @Override
    void increase(Propagator<?> prop, Element elt, double[] ws) {
        long futvars = prop.getNbVars();
        // Increase weights of all variables in this propagator
        // even if they are already instantiated
        final double[] inc = new double[]{1.};
        for (int i = 0; i < prop.getNbVars(); i++) {
            if (prop.getVar(i).isAConstant() || !VariableUtils.isInt(prop.getVar(i))) continue;
            IntVar ivar = (IntVar) prop.getVar(i);
            inc[0] = 1.0 / (futvars * (ivar.getDomainSize() == 0 ? 0.5 : ivar.getDomainSize()));
            weights.adjustOrPutValue(ivar, inc[0], inc[0]);
            ws[i] += inc[0];
        }
    }
}
