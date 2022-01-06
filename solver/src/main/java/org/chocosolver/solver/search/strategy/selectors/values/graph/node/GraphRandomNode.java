/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values.graph.node;

import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.Random;

public class GraphRandomNode implements GraphNodeSelector {

    private final Random rd;

    public GraphRandomNode(long seed) {
        this.rd = new Random(seed);

    }

    public int selectNode(GraphVar g) {
        ISet envNodes = g.getPotentialNodes();
        ISet kerNodes = g.getMandatoryNodes();
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
