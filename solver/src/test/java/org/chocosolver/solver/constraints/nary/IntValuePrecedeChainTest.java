/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;

/**
 * Created by cprudhom on 07/07/15.
 * Project: choco.
 */
public class IntValuePrecedeChainTest {

    @Test(groups = "10s", timeOut = 60000)
    public void test1() {
        for (int i = 0; i < 200; i++) {
            long s1, s2;
            {
                Model model = new Model();
                IntVar[] vars = model.intVarArray("X", 5, 0, 5, false);
                model.intValuePrecedeChain(vars, 1, 2).post();
                model.getSolver().setSearch(randomSearch(vars, i));
                while (model.getSolver().solve()) ;
                s1 = model.getSolver().getSolutionCount();
            }
            {
                Model model = new Model();
                IntVar[] vars = model.intVarArray("X", 5, 0, 5, false);
                model.intValuePrecedeChainDec(vars, 1, 2);
                model.getSolver().setSearch(randomSearch(vars, i));
                while (model.getSolver().solve()) ;
                s2 = model.getSolver().getSolutionCount();
            }
            Assert.assertEquals(s1, s2);

        }
    }

    @Test(groups = "1s")
    public void test2() {
        long s1, s2;
        for (int i = 0; i < 200; i++) {
            {
                Model model = new Model();
                IntVar[] vars = model.intVarArray("X", 5, 0, 5, false);
                model.intValuePrecedeChain(vars, Arrays.stream(vars)
                        .flatMapToInt(IntVar::stream)
                        .boxed()
                        .collect(Collectors.toSet())
                        .stream().mapToInt(k -> k)
                        .sorted().toArray()).post();
                model.getSolver().setSearch(randomSearch(vars, i));
                while (model.getSolver().solve()) ;
                s1 = model.getSolver().getSolutionCount();
            }
            {
                Model model = new Model();
                IntVar[] vars = model.intVarArray("X", 5, 0, 5, false);
                model.intValuePrecedeChainDec(vars, Arrays.stream(vars)
                        .flatMapToInt(IntVar::stream)
                        .boxed()
                        .collect(Collectors.toSet())
                        .stream().mapToInt(k -> k)
                        .sorted().toArray());
                model.getSolver().setSearch(randomSearch(vars, i));
                while (model.getSolver().solve()) ;
                s2 = model.getSolver().getSolutionCount();
            }
            Assert.assertEquals(s1, s2);
        }
    }
}
