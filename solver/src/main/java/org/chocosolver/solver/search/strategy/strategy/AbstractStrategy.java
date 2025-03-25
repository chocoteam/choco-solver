/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.BitSet;

/**
 * A search strategy provides decisions to go down in the search space.
 * The main method is {@link #computeDecision(Variable)} which returns the next decision to apply.
 *
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @since 1 juil. 2010
 */
public abstract class AbstractStrategy<V extends Variable> {

    protected final Model model;
    protected final DecisionPath decisionPath;
    protected final V[] vars;
    protected final BitSet idScope; // bitset representing the indexes of the variables in the scope

    protected AbstractStrategy(V... variables) {
        this(variables[0].getModel(), variables);
    }

    protected AbstractStrategy(Model model, V... variables) {
        this.model = model;
        this.decisionPath = model.getSolver().getDecisionPath();
        this.vars = variables.clone();
        this.idScope = new BitSet(variables.length);
        for (V v : variables) {
            idScope.set(v.getId());
        }
    }

    // *****************************************************************************************************************
    // DECISION COMPUTATION
    // *****************************************************************************************************************

    /**
     * Computes a new decision to search for a solution.
     * Returns <code>null</code> where there are no more decision to provide,
     * usually when all variables within the scope are instantiated.
     *
     * @return a new decision
     */
    public abstract Decision<V> getDecision();

    /**
     * Computes a decision to be applied to variable var
     * This method should be implemented in order to use search patterns
     *
     * @param var a variable
     * @return a decision to be applied to variable var
     */
    public Decision<V> computeDecision(V var) {
        return null;
    }

    /**
     * Creates an assignment decision object for integer variables
     * Just a simple shortcut for :
     * solver.getDecisionPath().makeIntDecision(var,DecisionOperatorFactory.makeIntEq(),val);
     *
     * @param var variable to branch on
     * @param val value to branch on
     * @return an assignment decision object (var = val) for integer variables
     */
    public final IntDecision makeIntDecision(IntVar var, int val) {
        return decisionPath.makeIntDecision(var, DecisionOperatorFactory.makeIntEq(), val);
    }

    // *****************************************************************************************************************
    // ACCESSORS AND OTHER METHODS
    // *****************************************************************************************************************

    /**
     * Get the model associated to this search strategy
     *
     * @return the model associated to this search strategy
     */
    public Model getModel() {
        return model;
    }

    /**
     * @return array of variables
     */
    public V[] getVariables() {
        return vars;
    }

    /**
     * Indicates whether a given variable var is within the scope of this strategy
     *
     * @param var a variable
     * @return true iff a given variable var is within the scope of this strategy
     */
    public boolean isVarInScope(Variable var) {
        return idScope.get(var.getId());
    }

    /**
     * Prepare <code>this</code> to be used in a search loop
     * The initialization can detect inconsistency, in that case, it returns false
     */
    public boolean init() {
        return true;
    }

    /**
     * Remove the current strategy.
     * This implies unplugging variable or search monitors.
     */
    public void remove() {

    }

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
}
