/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.function.Consumer;

/**
 * An abstract class that defines services required for the LNS to select variables to
 * freeze-unfreeze. <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public abstract class Neighbor implements ICause {

    protected final IntVar[] variables;
    protected final int[] values;

    protected Neighbor(IntVar[] variables) {
        this.variables = variables;
        this.values = new int[variables.length];
    }

    /**
     * Initialize this neighbor
     */
    public void init(){}

    /**
     * Record values of decision variables to freeze some ones during the next LNS run
     */
    public void recordSolution() {
        for (int i = 0; i < variables.length; i++) {
            values[i] = variables[i].getValue();
        }
    }

    /**
     * Freezes some variables in order to have a fast computation. The fixed variables are declared
     * as decisions in the decision path.
     */
    public abstract void fixSomeVariables() throws ContradictionException;

    /**
     * Freeze variable at position 'i' to its value in that recorded solution.
     * @param i position of the variable to instantiate
     * @throws ContradictionException if a contradiction occurs
     */
    public void freeze(int i) throws ContradictionException {
        variables[i].instantiateTo(values[i], this);
    }

    /**
     * Use less restriction at the beginning of a LNS run in order to get better solutions Called
     * when no solution was found during a LNS run (trapped into a local optimum)
     */
    public void restrictLess(){

    }

    /**
     * @return true iff the search is in a complete mode (no fixed variable)
     */
    public boolean isSearchComplete(){
        return false;
    }

    /**
     * Load a solution and record it
     *
     * @param solution a solution to record
     */
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
