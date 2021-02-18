/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.function.Consumer;

/**
 * An abstract class that defines services required for the LNS to select some IntVar to freeze-unfreeze. <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public abstract class IntNeighbor implements INeighbor {

    protected final IntVar[] variables;
    protected final int[] values;

    protected IntNeighbor(IntVar[] variables) {
        this.variables = variables;
        this.values = new int[variables.length];
    }

    /**
     * Record values of decision variables to freeze some ones during the next LNS run
     */
    @Override
    public void recordSolution() {
        for (int i = 0; i < variables.length; i++) {
            values[i] = variables[i].getValue();
        }
    }

    /**
     * Freeze variable at position 'i' to its value in that recorded solution.
     * @param i position of the variable to instantiate
     * @throws ContradictionException if a contradiction occurs
     */
    public void freeze(int i) throws ContradictionException {
        variables[i].instantiateTo(values[i], this);
    }

    /**
     * Load a solution and record it
     *
     * @param solution a solution to record
     */
    @Override
    public void loadFromSolution(Solution solution) {
        for (int i = 0; i < variables.length; i++) {
            values[i] = solution.getIntVal(variables[i]);
        }
    }

    @Override
    public void forEachIntVar(Consumer<IntVar> action) {
        for (int i = 0; i < variables.length; i++) {
            action.accept(variables[i]);
        }
    }
}
