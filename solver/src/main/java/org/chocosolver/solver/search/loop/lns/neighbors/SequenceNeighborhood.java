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

/**
 * A neighbor which is based on mutliple neighbors.
 * They are called sequentially.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public class SequenceNeighborhood implements INeighbor {

    /**
     * neighbor currently selected
     */
    int who;
    /**
     * number of neighbors declared
     */
    protected int count;
    /**
     * neighbors declared
     */
    protected INeighbor[] neighbors;
    /**
     * Number of time each neighbor succeed in finding a solution
     */
    int[] counters;

    public SequenceNeighborhood(INeighbor... neighbors) {
        this.neighbors = neighbors;
        who = 0;
        count = neighbors.length;
        counters = new int[count];
        counters[0] = -1;
    }

    @Override
    public void recordSolution() {
        counters[who]++;
        for (int i = 0; i < count; i++) {
            neighbors[i].recordSolution();
        }
        who = count - 1; // forces to start with the first neighbor
//        System.out.printf("%s %s\n", "% REPARTITION", Arrays.toString(counters));
    }

    @Override
    public void loadFromSolution(Solution solution) {
        for (int i = 0; i < count; i++) {
            neighbors[i].loadFromSolution(solution);
        }
        who = count - 1; // forces to start with the first neighbor
    }

    @Override
    public void fixSomeVariables() throws ContradictionException {
        nextNeighbor();
        if (who == count) who = 0;
        neighbors[who].fixSomeVariables();
    }

    @Override
    public void restrictLess() {
        neighbors[who].restrictLess();
    }

    @Override
    public boolean isSearchComplete() {
        boolean isComplete = false;
        for (int i = 0; i < count; i++) {
            isComplete |= neighbors[i].isSearchComplete();
        }
        return isComplete;
    }

    @Override
    public void init() {
        for (int i = 0; i < count; i++) {
            neighbors[i].init();
        }
    }

    protected void nextNeighbor(){
        who++;
    }
}
