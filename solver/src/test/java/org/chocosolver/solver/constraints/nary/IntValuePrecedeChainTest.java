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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;

/**
 * Created by cprudhom on 07/07/15.
 * Project: choco.
 */
public class IntValuePrecedeChainTest {

    public static void int_value_precede_chain_dec(IntVar[] X, int S, int T) {
        Model model = X[0].getModel();
        model.arithm(X[0], "!=", T).post();
        for (int j = 1; j < X.length; j++) {
            BoolVar bj = model.arithm(X[j], "=", T).reify();
            BoolVar[] bis = new BoolVar[j];
            for (int i = 0; i < j; i++) {
                bis[i] = model.arithm(X[i], "=", S).reify();
            }
            model.ifThen(bj, model.or(bis));
        }
    }


    @Test(groups="10s", timeOut=60000)
    public void test1() {
        for (int i = 0; i < 200; i++) {
            long s1, s2;
            {
                Model model = new Model();
                IntVar[] vars = model.intVarArray("X", 5, 0, 5, false);
                model.intValuePrecedeChain(vars, 1, 2).post();
                model.getSolver().setSearch(randomSearch(vars, 0));
                while (model.getSolver().solve()) ;
                s1 = model.getSolver().getSolutionCount();
            }
            {
                Model model = new Model();
                IntVar[] vars = model.intVarArray("X", 5, 0, 5, false);
                int_value_precede_chain_dec(vars, 1, 2);
                model.getSolver().setSearch(randomSearch(vars, 0));
                while (model.getSolver().solve()) ;
                s2 = model.getSolver().getSolutionCount();
            }
            Assert.assertEquals(s1, s2);

        }
    }


}
