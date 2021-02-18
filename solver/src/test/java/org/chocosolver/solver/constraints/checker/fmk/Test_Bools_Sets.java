/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.checker.fmk;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Jean-Guillaume Fages
 * @since 01/13
 */
public class Test_Bools_Sets {

    public Test_Bools_Sets(){
    }

    @DataProvider(name = "params")
    public Object[][] dataCL1(){
        List<Object[]> elt = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                elt.add(new Object[]{i, n});
            }
        }
        return elt.toArray(new Object[elt.size()][2]);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test(groups="checker", timeOut=60000, dataProvider = "params")
    public void testBOOL_SUM(int i, int n) {
        long seed = System.currentTimeMillis();
        Correctness.checkCorrectness(SetTestModel.boolSum, n, -n / 2, 2 * n, seed, null);
    }

    @Test(groups="checker", timeOut=60000, dataProvider = "params")
    public void setUnion(int i, int n) {
        long seed = System.currentTimeMillis();
        Correctness.checkCorrectness(SetTestModel.setUnion, n, -n / 2, 2 * n, seed, null);
    }

    @Test(groups="checker", timeOut=60000, dataProvider = "params")
    public void setInter(int i, int n) {
        long seed = System.currentTimeMillis();
        Correctness.checkCorrectness(SetTestModel.setInter, n, -n / 2, 2 * n, seed, null);
    }

    @Test(groups="checker", timeOut=60000, dataProvider = "params")
    public void setDisj(int i, int n) {
        long seed = System.currentTimeMillis();
        Correctness.checkCorrectness(SetTestModel.setDisj, n, -n / 2, 2 * n, seed, null);
    }

    @Test(groups="checker", timeOut=60000, dataProvider = "params")
    public void setDiff(int i, int n) {
        long seed = System.currentTimeMillis();
        Correctness.checkCorrectness(SetTestModel.setDiff, n, -n / 2, 2 * n, seed, null);

    }

    @Test(groups="checker", timeOut=60000, dataProvider = "params")
    public void setSubSet(int i, int n) {
        long seed = System.currentTimeMillis();
        Correctness.checkCorrectness(SetTestModel.setSubSet, n, -n / 2, 2 * n, seed, null);
    }

    @Test(groups="checker", timeOut=60000, dataProvider = "params")
    public void setAllEq(int i, int n) {
        long seed = System.currentTimeMillis();
        Correctness.checkCorrectness(SetTestModel.setAllEq, n, -n / 2, 2 * n, seed, null);
    }
}
