/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/06/2021
 */
public class IfThenElseTest {

    @Test(groups = "1s")
    public void test1() {
        Model model = new Model();
        BoolVar[] c = model.boolVarArray("c", 4);
        int[] x = {2, 2, 1, 4};
        IntVar y = model.intVar("y", 1, 4);
        model.ifThenElseDec(c, x, y);
        Solver solver = model.getSolver();
        //solver.showSolutions();
        solver.setSearch(Search.inputOrderLBSearch(model.retrieveIntVars(true)));
        while (solver.solve()) {
            //System.out.printf("%s %s %s%n", Arrays.toString(c), Arrays.toString(x), y);
        }
        Assert.assertEquals(solver.getSolutionCount(), 19);
    }

    @Test(groups = "1s")
    public void test2() {
        Model model = new Model();
        int n = 3;
        BoolVar[] c = model.boolVarArray("c", n);
        IntVar[] x = model.intVarArray("x", n, 1, n);
        IntVar y = model.intVar("y", 1, n - 1);
        model.ifThenElseDec(c, x, y);
        Solver solver = model.getSolver();
        //solver.showSolutions();
        solver.setSearch(Search.randomSearch(ArrayUtils.append(c, x,new IntVar[]{y}), 0));
        while (solver.solve()) {
            //System.out.printf("%s %s %s%n", Arrays.toString(c), Arrays.toString(x), y);
        }
        Assert.assertEquals(solver.getSolutionCount(), 180);
    }
}
