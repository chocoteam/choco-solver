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

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * A <code>StrategiesSequencer</code> is class for <code>AbstractStrategy</code> composition.
 * <code>this</code> is created with a list of <code>AbstractStrategy</code>, and calling
 * <code>getDecision()</code> retrieves the current active <code>AbstractStrategy</code> and
 * calls the delegate <code>getDecision()</code> method.
 * <br/>
 * A <code>AbstractStrategy</code> becomes "inactive" when no more decision can be computed,
 * ie every decisions have been computed and used.
 * <br/>
 *
 * @author Charles Prud'homme
 * @author Guillaume Le Louët
 * @since 5 juil. 2010
 */
@SuppressWarnings({"UnusedDeclaration", "ForLoopReplaceableByForEach"})
public class StrategiesSequencer<U extends Variable> extends AbstractStrategy<U> {

    private final AbstractStrategy<U>[] strategies;
    private final IStateInt index;

    @SuppressWarnings("unchecked")
    @SafeVarargs
    private static <V extends Variable> V[] make(AbstractStrategy<V>... strategies) {
        V[] vars = strategies[0].vars.clone();
        for (int i = 1; i < strategies.length; i++) {
            vars = ArrayUtils.append(vars, strategies[i].vars);
        }
        return vars;
    }

    @SafeVarargs
    public StrategiesSequencer(IEnvironment environment, AbstractStrategy<U>... strategies) {
        super(make(strategies));
        index = environment.makeInt(0);
        this.strategies = strategies;
    }

    @SafeVarargs
	public StrategiesSequencer(AbstractStrategy<U>... strategies) {
		super(make(strategies));
		index = null;
		this.strategies = strategies;
	}

    @Override
    public boolean init() {
        boolean ok = true;
        for (int i = 0; i < strategies.length; i++) {
            ok &= strategies[i].init();
        }
        return ok;
    }

    @Override
    public void remove() {
        for (int i = 0; i < strategies.length; i++) {
            strategies[i].remove();
        }
    }

    @Override
    public Decision<U> computeDecision(U variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        int idx = (index==null)?0:index.get();
        Decision<U> decision = null;
        while (decision == null && idx < strategies.length) {
            if (contains(strategies[idx].vars, variable)) {
                decision = strategies[idx].computeDecision(variable);
            }
            idx++;
        }
        return decision;
    }

    private static boolean contains(Variable[] vars, Variable variable) {
        for (Variable v : vars) {
            if (v.equals(variable)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * Iterates over the declared sub-strategies and gets the overall current decision.
     */
    @Override
    public Decision<U> getDecision() {
        int idx = (index==null)?0:index.get();
        Decision<U> decision = strategies[idx].getDecision();
        while (decision == null && idx < strategies.length - 1) {
            decision = strategies[++idx].getDecision();
        }
		if(index!=null){
			index.set(idx);
		}
        return decision;
    }

    /**
     * {@inheritDoc}
     * This is based on the <code>print()</code> method of every sub-strategies.
     */
    @Override
    public String toString() {
        StringBuilder st = new StringBuilder("Sequence of:\n");
        for (int i = 0; i < strategies.length; i++) {
            st.append("\t").append(strategies[i].toString()).append("\n");
        }
        return st.toString();
    }
}
