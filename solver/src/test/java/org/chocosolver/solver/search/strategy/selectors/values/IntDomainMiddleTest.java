/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.limits.SolutionCounter;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/04/2019
 */
public class IntDomainMiddleTest {

    @Test(timeOut = 60000, groups = "1s", expectedExceptions = ContradictionException.class)
    public void testSelectValue1() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("x", new int[]{0, 3, 5, 6, 7, 8, 11});
        int[] order = {5, 6, 7, 3, 8, 0, 11};
        IntDomainMiddle sel = new IntDomainMiddle(true);
        int i = 0;
        while (x.getDomainSize() > 0) {
            int val = sel.selectValue(x);
            Assert.assertEquals(val, order[i++]);
            x.removeValue(val, Cause.Null);
        }
    }

    @Test(timeOut = 60000, groups = "1s", expectedExceptions = ContradictionException.class)
    public void testSelectValue2() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("x", new int[]{0, 3, 5, 6, 7, 8, 11});
        int[] order = {7, 6, 8, 5, 3, 11, 0};
        IntDomainMiddle sel = new IntDomainMiddle(v -> 7, true);
        int i = 0;
        while (x.getDomainSize() > 0) {
            int val = sel.selectValue(x);
            Assert.assertEquals(val, order[i++]);
            x.removeValue(val, Cause.Null);
        }
    }

    @Test(groups = "1s")
    public void test3() {
        // Create a constraint network
        Model net = new Model();
        Solver solver = net.getSolver();

        IntVar x = net.intVar(//
                "x", //
                -1000, //
                1000);
        IntVar y = net.intVar(//
                "y", //
                -1000, //
                1000);
        IntVar[] vars = new IntVar[]{x, y};
        IntValueSelector sel = var -> {
            int pos = var.nextValue(-1);
            int neg = var.nextValue(-1);
            return pos < -neg? pos:neg;
        };
        solver.setSearch(new IntStrategy(vars, new InputOrder<>(net), sel));
        x.add(y).gt(5).and(x.mul(y).ne(0)).post();
        solver.showDecisions(()->"");
        List<Solution> res = net.getSolver().findAllSolutions(new SolutionCounter(net, 10));
        // System.out.println(res.toString());
        for (int i = 0; i < res.size(); i++) {
            Solution solution = res.get(i);
            if (solution != null) {
                System.out.println(solution);
            }
        }
    }
}