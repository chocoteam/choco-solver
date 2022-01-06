/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.parser.SetUpException;
import org.chocosolver.solver.Model;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/03/11
 */
public class GolombRulerTest {

    public final static int[][] OPTIMAL_RULER = {
            {5, 11}, {6, 17}, {7, 25}, {8, 34}, {9, 44}, {10, 55}//, {11, 72}
    };

    protected Model modeler(int m) throws SetUpException {
        GolombRuler pb = new GolombRuler();
        pb.setUp("-m", Integer.toString(m));
        pb.buildModel();
        pb.configureSearch();
        return pb.getModel();
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testAll() throws SetUpException {
        Model sol;
        for (int j = 0; j < OPTIMAL_RULER.length; j++) {
            sol = modeler(OPTIMAL_RULER[j][0]);
            sol.setObjective(false, sol.getVars()[OPTIMAL_RULER[j][0] - 1]);
            int nb = 0;
            while (sol.getSolver().solve()) {
                nb++;
            }
            long sols = sol.getSolver().getSolutionCount();
            assertEquals(nb, sols);
            long nodes = sol.getSolver().getNodeCount();
            sol = modeler(OPTIMAL_RULER[j][0]);
            sol.setObjective(false, sol.getVars()[OPTIMAL_RULER[j][0] - 1]);
            while (sol.getSolver().solve()) ;
            assertEquals(sol.getSolver().getSolutionCount(), sols);
            assertEquals(sol.getSolver().getNodeCount(), nodes);

        }
    }

}
