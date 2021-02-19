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

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

/**
 * <b>First fail</b> variable selector generalized to all variables.
 * It chooses the variable with the smallest domain (instantiated variables are ignored).
 * <br/>
 *
 * @author Jean-Guillaume Fages
 */
public class GeneralizedMinDomVarSelector<V extends Variable> implements VariableSelector<V> {

    private boolean least;

    /**
     * Chooses the non-instantiated variable with the smallest domain
     * <b>First fail</b> generalization to all variable kinds.
     */
    public GeneralizedMinDomVarSelector(){
        this(true);
    }

	/**
     * Chooses the non-instantiated variable with the smallest domain
     * <b>First fail</b> generalization to all variable kinds.
     * @param leastFree chooses the most Free variable if set to false
     */
    public GeneralizedMinDomVarSelector(boolean leastFree){
        this.least = leastFree;
    }

    @Override
    public V getVariable(V[] variables) {
        int small_dsize = Integer.MAX_VALUE;
        V nextVar = null;
        for (V v:variables) {
            if(!v.isInstantiated()) {
                int kind = (v.getTypeAndKind() & Variable.KIND);
                int dsize;
                if (kind == Variable.INT || kind == Variable.BOOL) {
                    dsize = ((IntVar) v).getDomainSize();
                } else if (kind == Variable.REAL) {
                    RealVar rv = (RealVar) v;
                    dsize = 2 + (int) ((rv.getUB() - rv.getLB())/rv.getPrecision());
                } else if (kind == Variable.SET) {
                    SetVar sv = (SetVar) v;
                    dsize = 1 + (sv.getUB().size() - sv.getLB().size());
                } else {
                    throw new UnsupportedOperationException("unrocognised variable kind");
                }
                if (nextVar == null) {
                    nextVar = v;
                    small_dsize = dsize;
                }else if(dsize > 1 && dsize < Integer.MAX_VALUE){
                    if (dsize < small_dsize == least) {
                        small_dsize = dsize;
                        nextVar = v;
                    }
                }
            }
        }
        return nextVar;
    }
}
