/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * @author Jean-Guillaume Fages
 * @since 09/09/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.constraints.graph.cost.tsp;

import org.testng.annotations.Test;

public class TspTest {

    public final static String INSTANCE = "bier127.tsp";
    public final static int MAX_SIZE = 300;

    @Test(groups = "10s", timeOut = 300000)
    public void testTSP() {
        String path = getClass().getResource(INSTANCE).getPath();
        int[][] data = TSP_Utils.parseInstance(path, MAX_SIZE);
        // presolve with LNS
        TSP_lns lns = new TSP_lns(data);
        if(!lns.optimalityProved()) {
            // optimality proof with Lagrangian relaxation
            new TSP_exact(data, lns.getBestSolutionValue());
        }
    }
}
