/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/04/2014
 */
public class KeysortingTest {

    @Test(groups = "1s")
    public void test01() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[4][3];
        Y = new IntVar[4][3];
        X[0][0] = VF.fixed("X11", 2, solver);
        X[0][1] = VF.fixed("X12", 3, solver);
        X[0][2] = VF.fixed("X13", 1001, solver);
        X[1][0] = VF.fixed("X21", 2, solver);
        X[1][1] = VF.fixed("X22", 4, solver);
        X[1][2] = VF.fixed("X23", 1002, solver);
        X[2][0] = VF.fixed("X31", 1, solver);
        X[2][1] = VF.fixed("X32", 5, solver);
        X[2][2] = VF.fixed("X33", 1003, solver);
        X[3][0] = VF.fixed("X41", 2, solver);
        X[3][1] = VF.fixed("X42", 3, solver);
        X[3][2] = VF.fixed("X43", 1004, solver);

        Y[0][0] = VF.bounded("Y11", 0, 3, solver);
        Y[0][1] = VF.bounded("Y12", 2, 6, solver);
        Y[0][2] = VF.bounded("Y13", 1000, 10006, solver);
        Y[1][0] = VF.bounded("Y21", 0, 3, solver);
        Y[1][1] = VF.bounded("Y22", 2, 6, solver);
        Y[1][2] = VF.bounded("Y23", 1000, 10006, solver);
        Y[2][0] = VF.bounded("Y31", 0, 3, solver);
        Y[2][1] = VF.bounded("Y32", 2, 6, solver);
        Y[2][2] = VF.bounded("Y33", 1000, 10006, solver);
        Y[3][0] = VF.bounded("Y41", 0, 3, solver);
        Y[3][1] = VF.bounded("Y42", 2, 6, solver);
        Y[3][2] = VF.bounded("Y43", 1000, 10006, solver);

        solver.post(ICF.keysorting(X, null, Y, 2));
        solver.findSolution();
        Assert.assertEquals(Y[0][0].getValue(), 1);
        Assert.assertEquals(Y[0][1].getValue(), 5);
        Assert.assertEquals(Y[0][2].getValue(), 1003);
        Assert.assertEquals(Y[1][0].getValue(), 2);
        Assert.assertEquals(Y[1][1].getValue(), 3);
        Assert.assertEquals(Y[1][2].getValue(), 1001);
        Assert.assertEquals(Y[2][0].getValue(), 2);
        Assert.assertEquals(Y[2][1].getValue(), 3);
        Assert.assertEquals(Y[2][2].getValue(), 1004);
        Assert.assertEquals(Y[3][0].getValue(), 2);
        Assert.assertEquals(Y[3][1].getValue(), 4);
        Assert.assertEquals(Y[3][2].getValue(), 1002);
    }

    @Test(groups = "1s")
    public void test02() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[4][3];
        Y = new IntVar[4][3];
        X[0][0] = VF.fixed("X11", 2, solver);
        X[0][1] = VF.fixed("X12", 3, solver);
        X[0][2] = VF.fixed("X13", 1001, solver);
        X[1][0] = VF.fixed("X21", 2, solver);
        X[1][1] = VF.fixed("X22", 4, solver);
        X[1][2] = VF.fixed("X23", 1002, solver);
        X[2][0] = VF.fixed("X31", 1, solver);
        X[2][1] = VF.fixed("X32", 5, solver);
        X[2][2] = VF.fixed("X33", 1003, solver);
        X[3][0] = VF.fixed("X41", 2, solver);
        X[3][1] = VF.fixed("X42", 3, solver);
        X[3][2] = VF.fixed("X43", 1004, solver);

        Y[0][0] = VF.bounded("Y11", 0, 3, solver);
        Y[0][1] = VF.bounded("Y12", 2, 6, solver);
        Y[0][2] = VF.bounded("Y13", 1000, 10006, solver);
        Y[1][0] = VF.bounded("Y21", 0, 3, solver);
        Y[1][1] = VF.bounded("Y22", 2, 6, solver);
        Y[1][2] = VF.bounded("Y23", 1000, 10006, solver);
        Y[2][0] = VF.bounded("Y31", 0, 3, solver);
        Y[2][1] = VF.bounded("Y32", 2, 6, solver);
        Y[2][2] = VF.bounded("Y33", 1000, 10006, solver);
        Y[3][0] = VF.bounded("Y41", 0, 3, solver);
        Y[3][1] = VF.bounded("Y42", 2, 6, solver);
        Y[3][2] = VF.bounded("Y43", 1000, 10006, solver);

        solver.post(ICF.keysorting(X, null, Y, 1));
        solver.findSolution();
        Assert.assertEquals(Y[0][0].getValue(), 1);
        Assert.assertEquals(Y[0][1].getValue(), 5);
        Assert.assertEquals(Y[0][2].getValue(), 1003);
        Assert.assertEquals(Y[1][0].getValue(), 2);
        Assert.assertEquals(Y[1][1].getValue(), 3);
        Assert.assertEquals(Y[1][2].getValue(), 1001);
        Assert.assertEquals(Y[2][0].getValue(), 2);
        Assert.assertEquals(Y[2][1].getValue(), 4);
        Assert.assertEquals(Y[2][2].getValue(), 1002);
        Assert.assertEquals(Y[3][0].getValue(), 2);
        Assert.assertEquals(Y[3][1].getValue(), 3);
        Assert.assertEquals(Y[3][2].getValue(), 1004);
    }


    @Test(groups = "1s")
    public void test03() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[4][3];
        Y = new IntVar[4][3];
        X[0][0] = VF.fixed("X11", 2, solver);
        X[0][1] = VF.fixed("X12", 3, solver);
        X[0][2] = VF.fixed("X13", 1001, solver);
        X[1][0] = VF.fixed("X21", 2, solver);
        X[1][1] = VF.fixed("X22", 4, solver);
        X[1][2] = VF.fixed("X23", 1002, solver);
        X[2][0] = VF.fixed("X31", 1, solver);
        X[2][1] = VF.fixed("X32", 5, solver);
        X[2][2] = VF.fixed("X33", 1003, solver);
        X[3][0] = VF.fixed("X41", 2, solver);
        X[3][1] = VF.fixed("X42", 3, solver);
        X[3][2] = VF.fixed("X43", 1004, solver);

        Y[0][0] = VF.bounded("Y11", 0, 3, solver);
        Y[0][1] = VF.bounded("Y12", 2, 6, solver);
        Y[0][2] = VF.bounded("Y13", 1000, 10006, solver);
        Y[1][0] = VF.bounded("Y21", 0, 3, solver);
        Y[1][1] = VF.bounded("Y22", 2, 6, solver);
        Y[1][2] = VF.bounded("Y23", 1000, 10006, solver);
        Y[2][0] = VF.bounded("Y31", 0, 3, solver);
        Y[2][1] = VF.bounded("Y32", 2, 6, solver);
        Y[2][2] = VF.bounded("Y33", 1000, 10006, solver);
        Y[3][0] = VF.bounded("Y41", 0, 3, solver);
        Y[3][1] = VF.bounded("Y42", 2, 6, solver);
        Y[3][2] = VF.bounded("Y43", 1000, 10006, solver);

        solver.post(ICF.keysorting(X, null, Y, 0));
        solver.findSolution();
        Assert.assertEquals(Y[0][0].getValue(), 2);
        Assert.assertEquals(Y[0][1].getValue(), 3);
        Assert.assertEquals(Y[0][2].getValue(), 1001);
        Assert.assertEquals(Y[1][0].getValue(), 2);
        Assert.assertEquals(Y[1][1].getValue(), 4);
        Assert.assertEquals(Y[1][2].getValue(), 1002);
        Assert.assertEquals(Y[2][0].getValue(), 1);
        Assert.assertEquals(Y[2][1].getValue(), 5);
        Assert.assertEquals(Y[2][2].getValue(), 1003);
        Assert.assertEquals(Y[3][0].getValue(), 2);
        Assert.assertEquals(Y[3][1].getValue(), 3);
        Assert.assertEquals(Y[3][2].getValue(), 1004);
    }

    @Test(groups = "1s")
    public void test04() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[3][2];
        Y = new IntVar[3][2];
        X[0][0] = VF.fixed("X11", 15, solver);
        X[0][1] = VF.fixed("X12", 0, solver);
        X[1][0] = VF.fixed("X21", 15, solver);
        X[1][1] = VF.fixed("X22", 8, solver);
        X[2][0] = VF.fixed("X31", 15, solver);
        X[2][1] = VF.fixed("X32", 19, solver);

        Y[0][0] = VF.bounded("Y11", 14, 16, solver);
        Y[0][1] = VF.bounded("Y12", 0, 19, solver);
        Y[1][0] = VF.bounded("Y21", 14, 16, solver);
        Y[1][1] = VF.bounded("Y22", 0, 19, solver);
        Y[2][0] = VF.bounded("Y31", 14, 16, solver);
        Y[2][1] = VF.bounded("Y32", 0, 19, solver);

        solver.post(ICF.keysorting(X, null, Y, 2));
        solver.findSolution();
        Assert.assertEquals(Y[0][0].getValue(), 15);
        Assert.assertEquals(Y[0][1].getValue(), 0);
        Assert.assertEquals(Y[1][0].getValue(), 15);
        Assert.assertEquals(Y[1][1].getValue(), 8);
        Assert.assertEquals(Y[2][0].getValue(), 15);
        Assert.assertEquals(Y[2][1].getValue(), 19);
    }

    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[5][1];
        Y = new IntVar[5][1];
        X[0][0] = VF.bounded("X1", 1, 16, solver);
        X[1][0] = VF.bounded("X2", 5, 10, solver);
        X[2][0] = VF.bounded("X3", 7, 9, solver);
        X[3][0] = VF.bounded("X4", 12, 15, solver);
        X[4][0] = VF.bounded("X5", 1, 13, solver);

        Y[0][0] = VF.bounded("Y1", 2, 3, solver);
        Y[1][0] = VF.bounded("Y2", 6, 7, solver);
        Y[2][0] = VF.bounded("Y3", 8, 11, solver);
        Y[3][0] = VF.bounded("Y4", 13, 16, solver);
        Y[4][0] = VF.bounded("Y5", 14, 18, solver);

        solver.post(ICF.keysorting(X, null, Y, 1));
        Assert.assertEquals(solver.findAllSolutions(), 28);

    }

    @Test(groups = "1s")
    public void test2() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[1][3];
        Y = new IntVar[1][3];
        X[0][0] = VF.bounded("X1", 0, 0, solver);
        X[0][1] = VF.bounded("X2", 0, 1, solver);
        X[0][2] = VF.bounded("X3", 1, 1, solver);

        Y[0][0] = VF.bounded("Y1", 0, 0, solver);
        Y[0][1] = VF.bounded("Y2", 0, 0, solver);
        Y[0][2] = VF.bounded("Y3", 1, 1, solver);

        solver.post(ICF.keysorting(X, null, Y, 1));
        try {
            solver.propagate();
            Assert.assertEquals(X[0][1].getValue(), 0);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = "1s")
    public void test3() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[1][3];
        Y = new IntVar[1][3];
        X[0][0] = VF.bounded("X1", 2, 2, solver);
        X[0][1] = VF.bounded("X2", 0, 2, solver);
        X[0][2] = VF.bounded("X3", 0, 0, solver);

        Y[0][0] = VF.bounded("Y1", 0, 0, solver);
        Y[0][1] = VF.bounded("Y2", 0, 0, solver);
        Y[0][2] = VF.bounded("Y3", 2, 2, solver);

        solver.post(ICF.keysorting(X, null, Y, 1));
        try {
            solver.propagate();
            Assert.assertEquals(X[0][1].getValue(), 0);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = "1s")
    public void test4() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[1][3];
        Y = new IntVar[1][3];
        X[0][0] = VF.bounded("X1", 0, 7, solver);
        X[0][1] = VF.bounded("X2", 3, 5, solver);
        X[0][2] = VF.bounded("X3", 1, 5, solver);

        Y[0][0] = VF.bounded("Y1", 0, 2, solver);
        Y[0][1] = VF.bounded("Y2", 1, 9, solver);
        Y[0][2] = VF.bounded("Y3", 7, 9, solver);

        solver.post(ICF.keysorting(X, null, Y, 1));
        try {
            solver.propagate();
            Assert.assertEquals(X[0][0].getValue(), 7);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

}
