/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.flatten;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/04/2014
 */
public class KeysortingTest {

    @Test(groups="1s", timeOut=60000)
    public void test01() {
        Model model = new Model();
        IntVar[][] X, Y;
        X = new IntVar[4][3];
        Y = new IntVar[4][3];
        X[0][0] = model.intVar("X11", 2);
        X[0][1] = model.intVar("X12", 3);
        X[0][2] = model.intVar("X13", 1001);
        X[1][0] = model.intVar("X21", 2);
        X[1][1] = model.intVar("X22", 4);
        X[1][2] = model.intVar("X23", 1002);
        X[2][0] = model.intVar("X31", 1);
        X[2][1] = model.intVar("X32", 5);
        X[2][2] = model.intVar("X33", 1003);
        X[3][0] = model.intVar("X41", 2);
        X[3][1] = model.intVar("X42", 3);
        X[3][2] = model.intVar("X43", 1004);

        Y[0][0] = model.intVar("Y11", 0, 3, true);
        Y[0][1] = model.intVar("Y12", 2, 6, true);
        Y[0][2] = model.intVar("Y13", 1000, 10006, true);
        Y[1][0] = model.intVar("Y21", 0, 3, true);
        Y[1][1] = model.intVar("Y22", 2, 6, true);
        Y[1][2] = model.intVar("Y23", 1000, 10006, true);
        Y[2][0] = model.intVar("Y31", 0, 3, true);
        Y[2][1] = model.intVar("Y32", 2, 6, true);
        Y[2][2] = model.intVar("Y33", 1000, 10006, true);
        Y[3][0] = model.intVar("Y41", 0, 3, true);
        Y[3][1] = model.intVar("Y42", 2, 6, true);
        Y[3][2] = model.intVar("Y43", 1000, 10006, true);

        model.keySort(X, null, Y, 2).post();
        model.getSolver().solve();
        assertEquals(Y[0][0].getValue(), 1);
        assertEquals(Y[0][1].getValue(), 5);
        assertEquals(Y[0][2].getValue(), 1003);
        assertEquals(Y[1][0].getValue(), 2);
        assertEquals(Y[1][1].getValue(), 3);
        assertEquals(Y[1][2].getValue(), 1001);
        assertEquals(Y[2][0].getValue(), 2);
        assertEquals(Y[2][1].getValue(), 3);
        assertEquals(Y[2][2].getValue(), 1004);
        assertEquals(Y[3][0].getValue(), 2);
        assertEquals(Y[3][1].getValue(), 4);
        assertEquals(Y[3][2].getValue(), 1002);
    }

    @Test(groups="1s", timeOut=60000)
    public void test02() {
        Model model = new Model();
        IntVar[][] X, Y;
        X = new IntVar[4][3];
        Y = new IntVar[4][3];
        X[0][0] = model.intVar("X11", 2);
        X[0][1] = model.intVar("X12", 3);
        X[0][2] = model.intVar("X13", 1001);
        X[1][0] = model.intVar("X21", 2);
        X[1][1] = model.intVar("X22", 4);
        X[1][2] = model.intVar("X23", 1002);
        X[2][0] = model.intVar("X31", 1);
        X[2][1] = model.intVar("X32", 5);
        X[2][2] = model.intVar("X33", 1003);
        X[3][0] = model.intVar("X41", 2);
        X[3][1] = model.intVar("X42", 3);
        X[3][2] = model.intVar("X43", 1004);

        Y[0][0] = model.intVar("Y11", 0, 3, true);
        Y[0][1] = model.intVar("Y12", 2, 6, true);
        Y[0][2] = model.intVar("Y13", 1000, 10006, true);
        Y[1][0] = model.intVar("Y21", 0, 3, true);
        Y[1][1] = model.intVar("Y22", 2, 6, true);
        Y[1][2] = model.intVar("Y23", 1000, 10006, true);
        Y[2][0] = model.intVar("Y31", 0, 3, true);
        Y[2][1] = model.intVar("Y32", 2, 6, true);
        Y[2][2] = model.intVar("Y33", 1000, 10006, true);
        Y[3][0] = model.intVar("Y41", 0, 3, true);
        Y[3][1] = model.intVar("Y42", 2, 6, true);
        Y[3][2] = model.intVar("Y43", 1000, 10006, true);

        model.keySort(X, null, Y, 1).post();
        model.getSolver().solve();
        assertEquals(Y[0][0].getValue(), 1);
        assertEquals(Y[0][1].getValue(), 5);
        assertEquals(Y[0][2].getValue(), 1003);
        assertEquals(Y[1][0].getValue(), 2);
        assertEquals(Y[1][1].getValue(), 3);
        assertEquals(Y[1][2].getValue(), 1001);
        assertEquals(Y[2][0].getValue(), 2);
        assertEquals(Y[2][1].getValue(), 4);
        assertEquals(Y[2][2].getValue(), 1002);
        assertEquals(Y[3][0].getValue(), 2);
        assertEquals(Y[3][1].getValue(), 3);
        assertEquals(Y[3][2].getValue(), 1004);
    }


    @Test(groups="1s", timeOut=60000)
    public void test03() {
        Model model = new Model();
        IntVar[][] X, Y;
        X = new IntVar[4][3];
        Y = new IntVar[4][3];
        X[0][0] = model.intVar("X11", 2);
        X[0][1] = model.intVar("X12", 3);
        X[0][2] = model.intVar("X13", 1001);
        X[1][0] = model.intVar("X21", 2);
        X[1][1] = model.intVar("X22", 4);
        X[1][2] = model.intVar("X23", 1002);
        X[2][0] = model.intVar("X31", 1);
        X[2][1] = model.intVar("X32", 5);
        X[2][2] = model.intVar("X33", 1003);
        X[3][0] = model.intVar("X41", 2);
        X[3][1] = model.intVar("X42", 3);
        X[3][2] = model.intVar("X43", 1004);

        Y[0][0] = model.intVar("Y11", 0, 3, true);
        Y[0][1] = model.intVar("Y12", 2, 6, true);
        Y[0][2] = model.intVar("Y13", 1000, 10006, true);
        Y[1][0] = model.intVar("Y21", 0, 3, true);
        Y[1][1] = model.intVar("Y22", 2, 6, true);
        Y[1][2] = model.intVar("Y23", 1000, 10006, true);
        Y[2][0] = model.intVar("Y31", 0, 3, true);
        Y[2][1] = model.intVar("Y32", 2, 6, true);
        Y[2][2] = model.intVar("Y33", 1000, 10006, true);
        Y[3][0] = model.intVar("Y41", 0, 3, true);
        Y[3][1] = model.intVar("Y42", 2, 6, true);
        Y[3][2] = model.intVar("Y43", 1000, 10006, true);

        model.keySort(X, null, Y, 0).post();
        model.getSolver().solve();
        assertEquals(Y[0][0].getValue(), 2);
        assertEquals(Y[0][1].getValue(), 3);
        assertEquals(Y[0][2].getValue(), 1001);
        assertEquals(Y[1][0].getValue(), 2);
        assertEquals(Y[1][1].getValue(), 4);
        assertEquals(Y[1][2].getValue(), 1002);
        assertEquals(Y[2][0].getValue(), 1);
        assertEquals(Y[2][1].getValue(), 5);
        assertEquals(Y[2][2].getValue(), 1003);
        assertEquals(Y[3][0].getValue(), 2);
        assertEquals(Y[3][1].getValue(), 3);
        assertEquals(Y[3][2].getValue(), 1004);
    }

    @Test(groups="1s", timeOut=60000)
    public void test04() {
        Model model = new Model();
        IntVar[][] X, Y;
        X = new IntVar[3][2];
        Y = new IntVar[3][2];
        X[0][0] = model.intVar("X11", 15);
        X[0][1] = model.intVar("X12", 0);
        X[1][0] = model.intVar("X21", 15);
        X[1][1] = model.intVar("X22", 8);
        X[2][0] = model.intVar("X31", 15);
        X[2][1] = model.intVar("X32", 19);

        Y[0][0] = model.intVar("Y11", 14, 16, true);
        Y[0][1] = model.intVar("Y12", 0, 19, true);
        Y[1][0] = model.intVar("Y21", 14, 16, true);
        Y[1][1] = model.intVar("Y22", 0, 19, true);
        Y[2][0] = model.intVar("Y31", 14, 16, true);
        Y[2][1] = model.intVar("Y32", 0, 19, true);

        model.keySort(X, null, Y, 2).post();
        model.getSolver().solve();
        assertEquals(Y[0][0].getValue(), 15);
        assertEquals(Y[0][1].getValue(), 0);
        assertEquals(Y[1][0].getValue(), 15);
        assertEquals(Y[1][1].getValue(), 8);
        assertEquals(Y[2][0].getValue(), 15);
        assertEquals(Y[2][1].getValue(), 19);
    }

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Model model = new Model();
        IntVar[][] X, Y;
        X = new IntVar[5][1];
        Y = new IntVar[5][1];
        X[0][0] = model.intVar("X1", 1, 16, true);
        X[1][0] = model.intVar("X2", 5, 10, true);
        X[2][0] = model.intVar("X3", 7, 9, true);
        X[3][0] = model.intVar("X4", 12, 15, true);
        X[4][0] = model.intVar("X5", 1, 13, true);

        Y[0][0] = model.intVar("Y1", 2, 3, true);
        Y[1][0] = model.intVar("Y2", 6, 7, true);
        Y[2][0] = model.intVar("Y3", 8, 11, true);
        Y[3][0] = model.intVar("Y4", 13, 16, true);
        Y[4][0] = model.intVar("Y5", 14, 18, true);

        model.keySort(X, null, Y, 1).post();
        long nbSolutions = 0;
        while (model.getSolver().solve()) {
            nbSolutions++;
        }
        assertEquals(nbSolutions, 182);

    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Model model = new Model();
        IntVar[][] X, Y;
        X = new IntVar[1][3];
        Y = new IntVar[1][3];
        X[0][0] = model.intVar("X1", 0, 0, true);
        X[0][1] = model.intVar("X2", 0, 1, true);
        X[0][2] = model.intVar("X3", 1, 1, true);

        Y[0][0] = model.intVar("Y1", 0, 0, true);
        Y[0][1] = model.intVar("Y2", 0, 0, true);
        Y[0][2] = model.intVar("Y3", 1, 1, true);

        model.keySort(X, null, Y, 1).post();
        try {
            model.getSolver().propagate();
            assertEquals(X[0][1].getValue(), 0);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Model model = new Model();
        IntVar[][] X, Y;
        X = new IntVar[1][3];
        Y = new IntVar[1][3];
        X[0][0] = model.intVar("X1", 2, 2, true);
        X[0][1] = model.intVar("X2", 0, 2, true);
        X[0][2] = model.intVar("X3", 0, 0, true);

        Y[0][0] = model.intVar("Y1", 0, 0, true);
        Y[0][1] = model.intVar("Y2", 0, 0, true);
        Y[0][2] = model.intVar("Y3", 2, 2, true);

        model.keySort(X, null, Y, 1).post();
        try {
            model.getSolver().propagate();
            assertEquals(X[0][1].getValue(), 0);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Model model = new Model();
        IntVar[][] X, Y;
        X = new IntVar[1][3];
        Y = new IntVar[1][3];
        X[0][0] = model.intVar("X1", 0, 7, true);
        X[0][1] = model.intVar("X2", 3, 5, true);
        X[0][2] = model.intVar("X3", 1, 5, true);

        Y[0][0] = model.intVar("Y1", 0, 2, true);
        Y[0][1] = model.intVar("Y2", 1, 9, true);
        Y[0][2] = model.intVar("Y3", 7, 9, true);

        model.keySort(X, null, Y, 1).post();
        try {
            model.getSolver().propagate();
            assertEquals(X[0][0].getValue(), 7);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test5() {
        Model model = new Model();
        IntVar[][] X, Y;
        X = new IntVar[4][2];
        Y = new IntVar[4][2];
        X[0][0] = model.intVar("X21", 3, 3, true);
        X[0][1] = model.intVar("X22", 1, 1, true);
        X[1][0] = model.intVar("X31", 1, 4, true);
        X[1][1] = model.intVar("X32", 2, 2, true);
        X[2][0] = model.intVar("X41", 4, 4, true);
        X[2][1] = model.intVar("X42", 3, 3, true);
        X[3][0] = model.intVar("X51", 1, 4, true);
        X[3][1] = model.intVar("X52", 4, 4, true);

        Y[0][0] = model.intVar("Y21", 1, 4, true);
        Y[0][1] = model.intVar("Y22", 1, 4, true);
        Y[1][0] = model.intVar("Y31", 1, 4, true);
        Y[1][1] = model.intVar("Y32", 1, 4, true);
        Y[2][0] = model.intVar("Y41", 1, 4, true);
        Y[2][1] = model.intVar("Y42", 1, 4, true);
        Y[3][0] = model.intVar("Y51", 1, 4, true);
        Y[3][1] = model.intVar("Y52", 1, 4, true);


        model.keySort(X, null, Y, 2).post();
        Solver r = model.getSolver();
        r.setSearch(inputOrderLBSearch(flatten(X)), inputOrderLBSearch(flatten(Y)));

        while (model.getSolver().solve()) ;
        assertEquals(r.getMeasures().getSolutionCount(), 16);
    }


    @Test(groups="1s", timeOut=60000)
    public void test6() {
        Model model = new Model();
        IntVar[][] X, Y;
        X = new IntVar[1][4];
        Y = new IntVar[1][4];
        X[0][0] = model.intVar("X21", 3, 3, true);
        X[0][1] = model.intVar("X31", 1, 4, true);
        X[0][2] = model.intVar("X41", 4, 4, true);
        X[0][3] = model.intVar("X51", 1, 4, true);

        Y[0][0] = model.intVar("Y21", 1, 4, true);
        Y[0][1] = model.intVar("Y31", 1, 4, true);
        Y[0][2] = model.intVar("Y41", 1, 4, true);
        Y[0][3] = model.intVar("Y51", 1, 4, true);


        model.keySort(X, null, Y, 1).post();
        Solver r = model.getSolver();
        r.setSearch(inputOrderLBSearch(flatten(X)), inputOrderLBSearch(flatten(Y)));

        while (model.getSolver().solve()) ;
        assertEquals(r.getMeasures().getSolutionCount(), 16);
    }
}
