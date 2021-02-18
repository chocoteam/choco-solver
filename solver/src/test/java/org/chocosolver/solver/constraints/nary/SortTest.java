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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.sort.PropSort;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/04/2014
 */
public class SortTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Model model = new Model();
        IntVar[] X, Y;
        X = new IntVar[5];
        Y = new IntVar[5];
        X[0] = model.intVar("X1", 1, 16, true);
        X[1] = model.intVar("X2", 5, 10, true);
        X[2] = model.intVar("X3", 7, 9, true);
        X[3] = model.intVar("X4", 12, 15, true);
        X[4] = model.intVar("X5", 1, 13, true);

        Y[0] = model.intVar("Y1", 2, 3, true);
        Y[1] = model.intVar("Y2", 6, 7, true);
        Y[2] = model.intVar("Y3", 8, 11, true);
        Y[3] = model.intVar("Y4", 13, 16, true);
        Y[4] = model.intVar("Y5", 14, 18, true);

        new Constraint("sort", new PropSort(X, Y)).post();
        /*if (solver.solve()) {
            do {
                System.out.printf("Solution:\n");
                for (IntVar x : X) {
                    System.out.printf("%d ", x.getValue());
                }
                System.out.printf("\n");
                for (IntVar x : Y) {
                    System.out.printf("%d ", x.getValue());
                }
                System.out.printf("\n\n");
            } while (solver.solve());
        }*/
        long nbSolutions = 0;
        while (model.getSolver().solve()) {
            nbSolutions++;
        }
        assertEquals(nbSolutions, 182);

    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Model model = new Model();
        IntVar[] X, Y;
        X = new IntVar[3];
        Y = new IntVar[3];
        X[0] = model.intVar("X1", 0, 0, true);
        X[1] = model.intVar("X2", 0, 1, true);
        X[2] = model.intVar("X3", 1, 1, true);

        Y[0] = model.intVar("Y1", 0, 0, true);
        Y[1] = model.intVar("Y2", 0, 0, true);
        Y[2] = model.intVar("Y3", 1, 1, true);

        model.sort(X, Y).post();
        try {
            model.getSolver().propagate();
            assertEquals(X[1].getValue(), 0);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Model model = new Model();
        IntVar[] X, Y;
        X = new IntVar[3];
        Y = new IntVar[3];
        X[0] = model.intVar("X1", 2, 2, true);
        X[1] = model.intVar("X2", 0, 2, true);
        X[2] = model.intVar("X3", 0, 0, true);

        Y[0] = model.intVar("Y1", 0, 0, true);
        Y[1] = model.intVar("Y2", 0, 0, true);
        Y[2] = model.intVar("Y3", 2, 2, true);

        model.sort(X, Y).post();
        try {
            model.getSolver().propagate();
            assertEquals(X[1].getValue(), 0);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Model model = new Model();
        IntVar[] X, Y;
        X = new IntVar[3];
        Y = new IntVar[3];
        X[0] = model.intVar("X1", 0, 7, true);
        X[1] = model.intVar("X2", 3, 5, true);
        X[2] = model.intVar("X3", 1, 5, true);

        Y[0] = model.intVar("Y1", 0, 2, true);
        Y[1] = model.intVar("Y2", 1, 9, true);
        Y[2] = model.intVar("Y3", 7, 9, true);

        model.sort(X, Y).post();
        try {
            model.getSolver().propagate();
            assertEquals(X[0].getValue(), 7);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }
}
