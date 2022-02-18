/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Jean-Guillaume Fages
 */
public class SquareTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testCst() {
        Model m = new Model();
        IntVar x = m.intVar(4);
        IntVar y = m.intVar(2);
        m.square(x, y).post();
        while (m.getSolver().solve()) ;
        assertEquals(m.getSolver().getSolutionCount(), 1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCstNeg() {
        Model m = new Model();
        IntVar x = m.intVar(4);
        IntVar y = m.intVar(-2);
        m.square(x, y).post();
        while (m.getSolver().solve()) ;
        assertEquals(m.getSolver().getSolutionCount(), 1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSimple() {
        Model m = new Model();
        IntVar x = m.intVar(4);
        IntVar y = m.intVar(-2, 2);
        m.square(x, y).post();
        while (m.getSolver().solve()) ;
        assertEquals(m.getSolver().getSolutionCount(), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCstFail() {
        Model m = new Model();
        IntVar x = m.intVar(2);
        IntVar y = m.intVar(2);
        m.square(x, y).post();
        while (m.getSolver().solve()) ;
        assertEquals(m.getSolver().getSolutionCount(), 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test() {
        Model m = new Model();
        IntVar x = m.intVar(0, 4);
        IntVar y = m.intVar(-2, 2);
        m.square(x, y).post();
        while (m.getSolver().solve()) ;
        assertEquals(m.getSolver().getSolutionCount(), 5);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() throws ContradictionException {
        Model m = new Model();
        IntVar x = m.intVar("y",new int[]{4,7,8,9});
        IntVar y = m.intVar("x", new int[]{2,3});
        m.square(x, y).post();
        m.getSolver().propagate();
        Assert.assertEquals(x.stream().toArray(), new int[]{4,9});
        m.getSolver().findAllSolutions();
        assertEquals(m.getSolver().getSolutionCount(), 2);
    }


    @Test(groups = "1s", timeOut = 6000000)
    public void testSquare() {
        int LANDSCAPE_SIZE = 200 * 200;
        int NB_LANDSCAPE_CLASSES = 4;
        int[] MIN_NB_PATCHES = new int[]{5, 2, 1, 2};
        int[] MAX_NB_PATCHES = new int[]{30, 25, 15, 9};
        int[] MIN_PATCH_SIZE = new int[]{300, 200, 800, 710};
        int[] MAX_PATCH_SIZE = new int[]{4000, 1200, 1200, 1200};
        int[] MIN_NET_PRODUCT = new int[]{300 * LANDSCAPE_SIZE, 0, 0, 50 * LANDSCAPE_SIZE};
        int[] MAX_NET_PRODUCT = new int[]{800 * LANDSCAPE_SIZE, LANDSCAPE_SIZE * LANDSCAPE_SIZE, LANDSCAPE_SIZE * LANDSCAPE_SIZE, 200 * LANDSCAPE_SIZE};
        Model model;
        IntVar[][] patchSizes;
        IntVar[][] squaredPatchSizes;
        IntVar[] nbPatches;
        IntVar[] sumOfPatchSizes;
        IntVar[] sumOfSquaredPatchSized;
        IntVar totalSum;

        model = new Model();
        patchSizes = new IntVar[NB_LANDSCAPE_CLASSES][];
        squaredPatchSizes = new IntVar[NB_LANDSCAPE_CLASSES][];
        nbPatches = new IntVar[NB_LANDSCAPE_CLASSES];
        sumOfPatchSizes = new IntVar[NB_LANDSCAPE_CLASSES];
        sumOfSquaredPatchSized = new IntVar[NB_LANDSCAPE_CLASSES];
        for (int c = 0; c < NB_LANDSCAPE_CLASSES; c++) {
            patchSizes[c] = model.intVarArray(MAX_NB_PATCHES[c], 0, MAX_PATCH_SIZE[c]);
            IntVar limit = model.intVar(0, MAX_NB_PATCHES[c] - MIN_NB_PATCHES[c]);
            nbPatches[c] = model.intVar(MIN_NB_PATCHES[c], MAX_NB_PATCHES[c]);
            model.arithm(nbPatches[c], "=", model.intVar(MAX_NB_PATCHES[c]), "-", limit).post();
            for (int i = 0; i < MAX_NB_PATCHES[c] - 1; i++) {
                model.ifThen(
                        model.arithm(patchSizes[c][i], "!=", 0).reify(),
                        model.arithm(patchSizes[c][i], ">=", MIN_PATCH_SIZE[c])
                );
                model.arithm(patchSizes[c][i], "<=", patchSizes[c][i + 1]).post();
            }
            sumOfPatchSizes[c] = model.intVar(MIN_PATCH_SIZE[c] * MIN_NB_PATCHES[c], MAX_PATCH_SIZE[c] * MAX_NB_PATCHES[c]);
            model.sum(patchSizes[c], "=", sumOfPatchSizes[c]).post();
            squaredPatchSizes[c] = model.intVarArray(MAX_NB_PATCHES[c], 0, MAX_PATCH_SIZE[c] * MAX_PATCH_SIZE[c]);
            for (int i = 0; i < MAX_NB_PATCHES[c]; i++) {
                //model.times(patchSizes[c][i], patchSizes[c][i], squaredPatchSizes[c][i]).post();
                model.square(squaredPatchSizes[c][i], patchSizes[c][i]).post();
            }
            sumOfSquaredPatchSized[c] = model.intVar(MIN_NET_PRODUCT[c], MAX_NET_PRODUCT[c]);
            model.sum(squaredPatchSizes[c], "=", sumOfSquaredPatchSized[c]).post();
        }
        totalSum = model.intVar(0, LANDSCAPE_SIZE);
        model.sum(sumOfPatchSizes, "=", totalSum).post();
        model.getSolver().setSearch(Search.inputOrderLBSearch(model.retrieveIntVars(true)));
        model.getSolver().showStatistics();
        model.getSolver().solve();
        Assert.assertEquals(1, model.getSolver().getSolutionCount());
        Assert.assertEquals(83, model.getSolver().getNodeCount());

    }
}
