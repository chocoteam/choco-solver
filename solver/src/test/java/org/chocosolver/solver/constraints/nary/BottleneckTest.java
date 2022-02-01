/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.minDomLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/01/11
 */
public class BottleneckTest {


    @Test(groups="10s", timeOut=300000)
    public void testStynes1() {
        for (int n = 60; n < 150; n += 50) {
            Model model = new Model();

            IntVar[] nexts = new IntVar[n];
            IntVar[] exps = new IntVar[n];
            IntVar[] bws = new IntVar[n];
            for (int i = 0; i < n; i++) {
                nexts[i] = model.intVar("n_" + i, 0, 200, false);
                exps[i] = model.intVar("e_" + i, 0, 200, false);
                bws[i] = model.intVar("b_" + i, 0, 2000, false);
                model.scalar(new IntVar[]{bws[i], exps[i]}, new int[]{1, 1}, "=", nexts[i]).post();
            }

            IntVar sum = model.sum("sum", bws);

            IntVar[] allvars = append(nexts, exps, bws, new IntVar[]{sum});


            model.getSolver().setSearch(minDomLBSearch(allvars));
            model.setObjective(Model.MAXIMIZE, sum);
            while(model.getSolver().solve());
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void testStynes2() {
        int n = 5000;
        {
            Model model = new Model();

            IntVar[] nexts = new IntVar[n];
            IntVar[] exps = new IntVar[n];
            IntVar[] bws = new IntVar[n];
            for (int i = 0; i < n; i++) {
                nexts[i] = model.intVar("n_" + i, 0, 200, false);
                exps[i] = model.intVar("e_" + i, 0, 200, false);
                bws[i] = model.intVar("b_" + i, 0, 2000, false);
                model.scalar(new IntVar[]{bws[i], exps[i]}, new int[]{1, 1}, "=", nexts[i]).post();
            }

            IntVar sum = model.intVar("sum", 0, 2000 * n, true);
            model.sum(bws, "=", sum).post();

            IntVar[] allvars = append(nexts, exps, bws, new IntVar[]{sum});

            // Heuristic val
            model.getSolver().setSearch(minDomLBSearch(allvars));

            model.getSolver().solve();
        }
    }
}
