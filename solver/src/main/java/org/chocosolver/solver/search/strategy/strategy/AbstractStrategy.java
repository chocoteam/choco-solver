/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

/**
 * A search strategy provides decisions to go down in the search space.
 * The main method is {@link #computeDecision(Variable)} which returns the next decision to apply.
 *
 * @author Charles Prud'homme
 * @since 1 juil. 2010
 */
public abstract class AbstractStrategy<V extends Variable>  {

    protected final V[] vars;

    @SafeVarargs
    protected AbstractStrategy(V... variables) {
        this.vars = variables.clone();
    }

    /**
     * Prepare <code>this</code> to be used in a search loop
     * The initialization can detect inconsistency, in that case, it returns false
     */
    public boolean init(){
        return true;
    }

    /**
     * Remove the current strategy.
     * This implies unplugging variable or search monitors.
     */
    public void remove(){

    }

    /**
     * Provides access to the current decision in the strategy.
     * If there are no more decision to provide, it returns <code>null</code>.
     *
     * @return the current decision
     */
    public abstract Decision<V> getDecision();

    /**
     * Creates a <code>String</code> object containing a pretty print of the current variables.
     *
     * @return a <code>String</code> object
     */
    public String toString() {
        StringBuilder s = new StringBuilder(32);
        for (Variable v : vars) {
            s.append(v).append(' ');
        }
        return s.toString();
    }

    /**
     * Computes a decision to be applied to variable var
     * This method should be implemented in order to use search patterns
     *
     * @param var a variable
     * @return a decision to be applied to variable var
     */
    protected Decision<V> computeDecision(V var) {
        return null;
    }

    /**
     * @return array of variables
     */
    public V[] getVariables() {
        return vars;
    }

    /**
     *  Creates an assignment decision object for integer variables
     *  Just a simple shortcut for :
     *  solver.getDecisionPath().makeIntDecision(var,DecisionOperatorFactory.makeIntEq(),val);
     * @param var variable to branch on
     * @param val value to branch on
     * @return an assignment decision object (var = val) for integer variables
     */
    protected final IntDecision makeIntDecision(IntVar var, int val){
        return var.getModel().getSolver().getDecisionPath().makeIntDecision(var, DecisionOperatorFactory.makeIntEq(),val);
    }
}
