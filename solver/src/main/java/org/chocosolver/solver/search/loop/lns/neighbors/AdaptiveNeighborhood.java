/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Solution;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public class AdaptiveNeighborhood extends SequenceNeighborhood {

    private final Random random;
    private int sum;

    public AdaptiveNeighborhood(long seed, INeighbor... neighbors) {
        super(neighbors);
        this.random = new Random(seed);
        sum = count - 1;
    }

    @Override
    public void recordSolution() {
        sum++;
        super.recordSolution();
    }

    @Override
    public void loadFromSolution(Solution solution) {
        sum++;
        super.loadFromSolution(solution);
    }

    @Override
    protected void nextNeighbor() {
        int r = random.nextInt(sum);
        for (int i = 0; i < count; i++) {
            r -= (counters[i] + 1);
            if (r < 0) {
                who = i;
                return;
            }
        }
    }
}
