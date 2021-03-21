/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.tree;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.strategy.GraphStrategy;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class ArboTest {

    private static SetType graphTypeEnv = SetType.BITSET;
    private static SetType graphTypeKer = SetType.BITSET;

    public static void model(int n, int seed) {
        Model model = new Model();
        DirectedGraph GLB = new DirectedGraph(model,n,graphTypeKer, false);
        DirectedGraph GUB = new DirectedGraph(model,n,graphTypeEnv, false);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                GUB.addEdge(i, j);
            }
        }
        DirectedGraphVar g = model.digraphVar("G", GLB, GUB);
        model.directedForest(g).post();
        model.getSolver().setSearch(new GraphStrategy(g, seed));

        model.getSolver().limitSolution(100);
        while (model.getSolver().solve());

        assertTrue(model.getSolver().getFailCount() == 0);
        assertTrue(model.getSolver().getSolutionCount() > 0);
    }

    @Test(groups = "1m")
    public static void debug() {
        for (int n = 5; n < 7; n++) {
            System.out.println("tree : n=" + n);
            model(n, (int) System.currentTimeMillis());
        }
    }

    @Test(groups = "1m")
    public static void testAllDataStructure() {
        for (SetType ge : new SetType[]{SetType.BIPARTITESET,SetType.LINKED_LIST,SetType.BITSET}) {
            graphTypeEnv = ge;
            graphTypeKer = ge;
            System.out.println("env:" + ge + " ker :" + ge);
            debug();
        }
    }
}
