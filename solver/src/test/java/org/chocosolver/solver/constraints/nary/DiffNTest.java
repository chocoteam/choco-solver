/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/02/2023
 */
public class DiffNTest {

    private final int[] sizes = new int[]{2, 4, 6, 7, 8, 9, 11, 15, 16, 17, 18, 19, 24, 25, 27, 29, 33, 35, 37, 42, 50};

    @DataProvider
    public Object[][] params() {
        int n = 19;
        Object[][] res = new Object[n][1];
        for (int i = 0; i < n; i++) {
            res[i][0] = i + 1;
        }
        return res;
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "params")
    public void test1(int n) {
        //Perfect square problem
        // n first squares of size respectively 2,4,6,7,8,9,11,15,16,17,18,19,24,25,27,29,33,35,37,42,50
        // must be placed in a 112x112 square
        // the squares must not overlap
        Model model = new Model();
        IntVar[] x = new IntVar[n];
        IntVar[] y = new IntVar[n];
        IntVar[] w = new IntVar[n]; // w[i] = sizes[i]
        IntVar[] h = new IntVar[n]; // height[i] = sizes[i]
        for (int i = 0; i < n; i++) {
            x[i] = model.intVar("x_" + i, 0, 112 - sizes[i]);
            y[i] = model.intVar("y_" + i, 0, 112 - sizes[i]);
            w[i] = model.intVar("width_" + i, sizes[i], sizes[i]);
            h[i] = model.intVar("height_" + i, sizes[i], sizes[i]);
        }
        model.diffN(x, y, w, h, true).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(ArrayUtils.interlace(x, y)));
        solver.solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }


    @Test(groups = "1s", timeOut = 60000, dataProvider = "params")
    public void test2(int n) {
        //Perfect square problem
        // n first squares of size respectively 2,4,6,7,8,9,11,15,16,17,18,19,24,25,27,29,33,35,37,42,50
        // must be placed in a 112x112 square
        // the squares must not overlap
        Model model = new Model();
        IntVar[][] x = new IntVar[n][2];
        int[][] w = new int[n][2];
        for (int i = 0; i < n; i++) {
            x[i][0] = model.intVar("x_" + i, 0, 112 - sizes[i]);
            x[i][1] = model.intVar("y_" + i, 0, 112 - sizes[i]);
            w[i][0] = sizes[i];
            w[i][1] = sizes[i];
        }
        model.diffN(x,w).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(ArrayUtils.flatten(x)));
        solver.solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

}