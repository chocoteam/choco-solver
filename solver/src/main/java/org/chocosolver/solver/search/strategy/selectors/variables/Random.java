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

import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.objects.IntList;

/**
 * <b>Random</b> variable selector.
 * It chooses variables randomly, among uninstantiated ones.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 * @param <T> type of variable
 */
public class Random<T extends Variable> implements VariableSelector<T>, VariableEvaluator<T> {

    /**
     * To store index of variable to select randomly
     */
    private IntList sets;

    /**
     * Random number generator.
     */
    private java.util.Random random;

    /**
     * Random variable selector
     * @param seed seed for random number generator.
     */
    public Random(long seed) {
        sets = new IntList();
        random = new java.util.Random(seed);
    }


    @Override
    public T getVariable(T[] variables) {
        sets.clear();
        for (int idx = 0; idx < variables.length; idx++) {
            if (!variables[idx].isInstantiated()) {
                sets.add(idx);
            }
        }
        if (sets.size() > 0) {
            int rand_idx = sets.get(random.nextInt(sets.size()));
            return variables[rand_idx];
        } else return null;
    }

    @Override
    public double evaluate(T variable) {
        return random.nextDouble();
    }
}
