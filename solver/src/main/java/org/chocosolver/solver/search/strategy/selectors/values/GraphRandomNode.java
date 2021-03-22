/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.GraphVar;

import java.util.Random;

public class GraphRandomNode extends GraphNodeSelector<GraphVar> {

    private Random rd;

    public GraphRandomNode(GraphVar g, long seed) {
        super(g);
        this.rd = new Random(seed);

    }

    @Override
    public int nextNode() {
        int delta = envNodes.size() - kerNodes.size();
        if (delta != 0) {
            delta = rd.nextInt(delta);
            for (int i : envNodes) {
                if (!kerNodes.contains(i)) {
                    if (delta == 0) {
                        return i;
                    } else {
                        delta--;
                    }
                }
            }
        }
        return -1;
    }
}
