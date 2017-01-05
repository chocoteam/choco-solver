/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.explanations;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.flatten;
import static org.chocosolver.util.tools.ArrayUtils.toArray;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 30/10/11
 * Time: 19:10
 */
public class EqualXYCExplTest {

    public void model(int seed, int nbvars) {

        Random r = new Random(seed);
        int[] values = new int[nbvars];
        for (int i = 0; i < values.length; i++) {
            values[i] = r.nextInt(nbvars);
        }
        Model ref = new Model();
        Model sol = new Model();
        sol.getSolver().setCBJLearning(false, false);

        IntVar[] varsr = new IntVar[nbvars];
        IntVar[] indicesr = new IntVar[nbvars];
        IntVar[] varss = new IntVar[nbvars];
        IntVar[] indicess = new IntVar[nbvars];
        for (int i = 0; i < varsr.length; i++) {
            varsr[i] = ref.intVar("v_" + i, 0, nbvars, false);
            indicesr[i] = ref.intVar("i_" + i, 0, nbvars, false);
            varss[i] = sol.intVar("v_" + i, 0, nbvars, false);
            indicess[i] = sol.intVar("i_" + i, 0, nbvars, false);
        }
        IntVar[] allvarsr = flatten(toArray(varsr, indicesr));
        ref.getSolver().setSearch(inputOrderLBSearch(allvarsr));

        IntVar[] allvarss = flatten(toArray(varss, indicess));
        sol.getSolver().setSearch(inputOrderLBSearch(allvarss));


        for (int i = 0; i < varsr.length - 1; i++) {
            ref.element(varsr[i], values, indicesr[i], 0).post();
            ref.arithm(varsr[i], "+", indicesr[i + 1], "=", 2 * nbvars / 3).post();
            sol.element(varss[i], values, indicess[i], 0).post();
            sol.arithm(varss[i], "+", indicess[i + 1], "=", 2 * nbvars / 3).post();
        }

        while (ref.getSolver().solve()) ;
        while (sol.getSolver().solve()) ;


        assertEquals(sol.getSolver().getSolutionCount(), ref.getSolver().getSolutionCount());
        assertTrue(sol.getSolver().getBackTrackCount() <= ref.getSolver().getBackTrackCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        model(125, 4);
        model(125, 10);
        model(153, 15);
        model(1234, 12);
    }
}
