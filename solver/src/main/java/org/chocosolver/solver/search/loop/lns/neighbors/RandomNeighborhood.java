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

import java.util.BitSet;
import java.util.Random;

/**
 * A Random LNS
 * <p>
 *
 * @author Charles Prud'homme
 * @since 18/04/13
 */
public class RandomNeighborhood extends IntNeighbor {

    /**
     * Number of variables to consider in this neighbor
     */
    protected final int n;
    /**
     * For randomness
     */
    private Random rd;
    /**
     * Size of the fragment
     */
    private double nbFixedVariables = 0d;
    /**
     * Number of times this neighbor is called
     */
    private int nbCall;
    /**
     * Next time the level should be increased
     */
    protected int limit;
    /**
     * Relaxing factor
     */
    protected final int level;
    /**
     * Indicate which variables are selected to be part of the fragment
     */
    protected BitSet fragment;

    /**
     * Create a neighbor for LNS which randomly selects variable to be part of a fragment
     * @param vars variables to consider in this
     * @param level relaxing factor
     * @param seed for randomness
     */
    public RandomNeighborhood(IntVar[] vars, int level, long seed) {
        super(vars);
        this.n = vars.length;
        this.level = level;
        this.rd = new Random(seed);
        this.fragment = new BitSet(n);
    }

    @Override
    public void recordSolution() {
        super.recordSolution();
        nbFixedVariables = 2. * n / 3. + 1;
        nbCall = 0;
        limit = 200; //geo.getNextCutoff(nbCall);
    }

    @Override
    public void loadFromSolution(Solution solution) {
        super.loadFromSolution(solution);
        nbFixedVariables = 2. * n / 3. + 1;
        nbCall = 0;
        limit = 200; //geo.getNextCutoff(nbCall);
    }

    @Override
    public void fixSomeVariables() throws ContradictionException {
        nbCall++;
        restrictLess();
        fragment.set(0, n); // all variables are frozen
        for (int i = 0; i < nbFixedVariables - 1 && fragment.cardinality() > 0; i++) {
            int id = selectVariable();
            if (variables[id].contains(values[id])) {  // to deal with objective variable and related
                freeze(id);
            }
            fragment.clear(id);
        }
    }

    /**
     * @return a variable id in {@link #variables} to be part of the fragment
     */
    private int selectVariable() {
        int id;
        int cc = rd.nextInt(fragment.cardinality());
        for (id = fragment.nextSetBit(0); id >= 0 && cc > 0; id = fragment.nextSetBit(id + 1)) {
            cc--;
        }
        return id;
    }

    @Override
    public void restrictLess() {
        if (nbCall > limit) {
            limit = nbCall + level;
            nbFixedVariables = rd.nextDouble() * n;
        }
    }
}
