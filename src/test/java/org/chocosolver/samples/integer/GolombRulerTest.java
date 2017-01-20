/**
 * This file is part of samples, https://github.com/chocoteam/samples
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.samples.integer;

import org.chocosolver.pf4cs.SetUpException;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.propagation.PropagationEngineFactory.values;
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

    @Test(groups="10s", timeOut=60000)
    public void testAll() throws SetUpException {
        Model sol;
        for (int j = 0; j < OPTIMAL_RULER.length; j++) {
            sol = modeler(OPTIMAL_RULER[j][0]);
            sol.setObjective(false, (IntVar) sol.getVars()[OPTIMAL_RULER[j][0] - 1]);
            int nb = 0;
            while (sol.getSolver().solve()) {
                nb++;
            }
            long sols = sol.getSolver().getSolutionCount();
            assertEquals(nb, sols);
            long nodes = sol.getSolver().getNodeCount();
            for (int k = 1; k < values().length; k++) {
                sol = modeler(OPTIMAL_RULER[j][0]);
                values()[k].make(sol);
                sol.setObjective(false, (IntVar) sol.getVars()[OPTIMAL_RULER[j][0] - 1]);
                while (sol.getSolver().solve()) ;
                assertEquals(sol.getSolver().getSolutionCount(), sols);
                assertEquals(sol.getSolver().getNodeCount(), nodes);

            }
        }
    }

}
