/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.FA.utils;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 24, 2010
 * Time: 10:37:53 AM
 */
public class CounterState implements ICounter {

    private final int[][][] costs;
    private final Bounds bounds;

    public CounterState(int[][][] layer_value_state, int min, int max) {
        this.costs = layer_value_state;
        this.bounds = Bounds.makeBounds(min, min, max, max);
    }


    @Override
    public Bounds bounds() {
        return bounds;
    }

    @Override
    public double cost(int layer, int value) {
        return cost(layer, value, 0);
    }

    @Override
    public double cost(int layer, int value, int state) {
        return this.costs[layer][value][state];
    }

}
