/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/04/2014
 */
public class KeysortingTest {

    @Test(groups="1s", timeOut=60000)
    public void test01() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[4][3];
        Y = new IntVar[4][3];
        X[0][0] = solver.intVar("X11", 2);
        X[0][1] = solver.intVar("X12", 3);
        X[0][2] = solver.intVar("X13", 1001);
        X[1][0] = solver.intVar("X21", 2);
        X[1][1] = solver.intVar("X22", 4);
        X[1][2] = solver.intVar("X23", 1002);
        X[2][0] = solver.intVar("X31", 1);
        X[2][1] = solver.intVar("X32", 5);
        X[2][2] = solver.intVar("X33", 1003);
        X[3][0] = solver.intVar("X41", 2);
        X[3][1] = solver.intVar("X42", 3);
        X[3][2] = solver.intVar("X43", 1004);

        Y[0][0] = solver.intVar("Y11", 0, 3, true);
        Y[0][1] = solver.intVar("Y12", 2, 6, true);
        Y[0][2] = solver.intVar("Y13", 1000, 10006, true);
        Y[1][0] = solver.intVar("Y21", 0, 3, true);
        Y[1][1] = solver.intVar("Y22", 2, 6, true);
        Y[1][2] = solver.intVar("Y23", 1000, 10006, true);
        Y[2][0] = solver.intVar("Y31", 0, 3, true);
        Y[2][1] = solver.intVar("Y32", 2, 6, true);
        Y[2][2] = solver.intVar("Y33", 1000, 10006, true);
        Y[3][0] = solver.intVar("Y41", 0, 3, true);
        Y[3][1] = solver.intVar("Y42", 2, 6, true);
        Y[3][2] = solver.intVar("Y43", 1000, 10006, true);

        solver.post(solver.keySort(X, null, Y, 2));
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

    @Test(groups="1s", timeOut=60000)
    public void test02() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[4][3];
        Y = new IntVar[4][3];
        X[0][0] = solver.intVar("X11", 2);
        X[0][1] = solver.intVar("X12", 3);
        X[0][2] = solver.intVar("X13", 1001);
        X[1][0] = solver.intVar("X21", 2);
        X[1][1] = solver.intVar("X22", 4);
        X[1][2] = solver.intVar("X23", 1002);
        X[2][0] = solver.intVar("X31", 1);
        X[2][1] = solver.intVar("X32", 5);
        X[2][2] = solver.intVar("X33", 1003);
        X[3][0] = solver.intVar("X41", 2);
        X[3][1] = solver.intVar("X42", 3);
        X[3][2] = solver.intVar("X43", 1004);

        Y[0][0] = solver.intVar("Y11", 0, 3, true);
        Y[0][1] = solver.intVar("Y12", 2, 6, true);
        Y[0][2] = solver.intVar("Y13", 1000, 10006, true);
        Y[1][0] = solver.intVar("Y21", 0, 3, true);
        Y[1][1] = solver.intVar("Y22", 2, 6, true);
        Y[1][2] = solver.intVar("Y23", 1000, 10006, true);
        Y[2][0] = solver.intVar("Y31", 0, 3, true);
        Y[2][1] = solver.intVar("Y32", 2, 6, true);
        Y[2][2] = solver.intVar("Y33", 1000, 10006, true);
        Y[3][0] = solver.intVar("Y41", 0, 3, true);
        Y[3][1] = solver.intVar("Y42", 2, 6, true);
        Y[3][2] = solver.intVar("Y43", 1000, 10006, true);

        solver.post(solver.keySort(X, null, Y, 1));
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


    @Test(groups="1s", timeOut=60000)
    public void test03() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[4][3];
        Y = new IntVar[4][3];
        X[0][0] = solver.intVar("X11", 2);
        X[0][1] = solver.intVar("X12", 3);
        X[0][2] = solver.intVar("X13", 1001);
        X[1][0] = solver.intVar("X21", 2);
        X[1][1] = solver.intVar("X22", 4);
        X[1][2] = solver.intVar("X23", 1002);
        X[2][0] = solver.intVar("X31", 1);
        X[2][1] = solver.intVar("X32", 5);
        X[2][2] = solver.intVar("X33", 1003);
        X[3][0] = solver.intVar("X41", 2);
        X[3][1] = solver.intVar("X42", 3);
        X[3][2] = solver.intVar("X43", 1004);

        Y[0][0] = solver.intVar("Y11", 0, 3, true);
        Y[0][1] = solver.intVar("Y12", 2, 6, true);
        Y[0][2] = solver.intVar("Y13", 1000, 10006, true);
        Y[1][0] = solver.intVar("Y21", 0, 3, true);
        Y[1][1] = solver.intVar("Y22", 2, 6, true);
        Y[1][2] = solver.intVar("Y23", 1000, 10006, true);
        Y[2][0] = solver.intVar("Y31", 0, 3, true);
        Y[2][1] = solver.intVar("Y32", 2, 6, true);
        Y[2][2] = solver.intVar("Y33", 1000, 10006, true);
        Y[3][0] = solver.intVar("Y41", 0, 3, true);
        Y[3][1] = solver.intVar("Y42", 2, 6, true);
        Y[3][2] = solver.intVar("Y43", 1000, 10006, true);

        solver.post(solver.keySort(X, null, Y, 0));
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

    @Test(groups="1s", timeOut=60000)
    public void test04() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[3][2];
        Y = new IntVar[3][2];
        X[0][0] = solver.intVar("X11", 15);
        X[0][1] = solver.intVar("X12", 0);
        X[1][0] = solver.intVar("X21", 15);
        X[1][1] = solver.intVar("X22", 8);
        X[2][0] = solver.intVar("X31", 15);
        X[2][1] = solver.intVar("X32", 19);

        Y[0][0] = solver.intVar("Y11", 14, 16, true);
        Y[0][1] = solver.intVar("Y12", 0, 19, true);
        Y[1][0] = solver.intVar("Y21", 14, 16, true);
        Y[1][1] = solver.intVar("Y22", 0, 19, true);
        Y[2][0] = solver.intVar("Y31", 14, 16, true);
        Y[2][1] = solver.intVar("Y32", 0, 19, true);

        solver.post(solver.keySort(X, null, Y, 2));
        solver.findSolution();
        Assert.assertEquals(Y[0][0].getValue(), 15);
        Assert.assertEquals(Y[0][1].getValue(), 0);
        Assert.assertEquals(Y[1][0].getValue(), 15);
        Assert.assertEquals(Y[1][1].getValue(), 8);
        Assert.assertEquals(Y[2][0].getValue(), 15);
        Assert.assertEquals(Y[2][1].getValue(), 19);
    }

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[5][1];
        Y = new IntVar[5][1];
        X[0][0] = solver.intVar("X1", 1, 16, true);
        X[1][0] = solver.intVar("X2", 5, 10, true);
        X[2][0] = solver.intVar("X3", 7, 9, true);
        X[3][0] = solver.intVar("X4", 12, 15, true);
        X[4][0] = solver.intVar("X5", 1, 13, true);

        Y[0][0] = solver.intVar("Y1", 2, 3, true);
        Y[1][0] = solver.intVar("Y2", 6, 7, true);
        Y[2][0] = solver.intVar("Y3", 8, 11, true);
        Y[3][0] = solver.intVar("Y4", 13, 16, true);
        Y[4][0] = solver.intVar("Y5", 14, 18, true);

        solver.post(solver.keySort(X, null, Y, 1));
        Assert.assertEquals(solver.findAllSolutions(), 182);

    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[1][3];
        Y = new IntVar[1][3];
        X[0][0] = solver.intVar("X1", 0, 0, true);
        X[0][1] = solver.intVar("X2", 0, 1, true);
        X[0][2] = solver.intVar("X3", 1, 1, true);

        Y[0][0] = solver.intVar("Y1", 0, 0, true);
        Y[0][1] = solver.intVar("Y2", 0, 0, true);
        Y[0][2] = solver.intVar("Y3", 1, 1, true);

        solver.post(solver.keySort(X, null, Y, 1));
        try {
            solver.propagate();
            Assert.assertEquals(X[0][1].getValue(), 0);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[1][3];
        Y = new IntVar[1][3];
        X[0][0] = solver.intVar("X1", 2, 2, true);
        X[0][1] = solver.intVar("X2", 0, 2, true);
        X[0][2] = solver.intVar("X3", 0, 0, true);

        Y[0][0] = solver.intVar("Y1", 0, 0, true);
        Y[0][1] = solver.intVar("Y2", 0, 0, true);
        Y[0][2] = solver.intVar("Y3", 2, 2, true);

        solver.post(solver.keySort(X, null, Y, 1));
        try {
            solver.propagate();
            Assert.assertEquals(X[0][1].getValue(), 0);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[1][3];
        Y = new IntVar[1][3];
        X[0][0] = solver.intVar("X1", 0, 7, true);
        X[0][1] = solver.intVar("X2", 3, 5, true);
        X[0][2] = solver.intVar("X3", 1, 5, true);

        Y[0][0] = solver.intVar("Y1", 0, 2, true);
        Y[0][1] = solver.intVar("Y2", 1, 9, true);
        Y[0][2] = solver.intVar("Y3", 7, 9, true);

        solver.post(solver.keySort(X, null, Y, 1));
        try {
            solver.propagate();
            Assert.assertEquals(X[0][0].getValue(), 7);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test5() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[4][2];
        Y = new IntVar[4][2];
        X[0][0] = solver.intVar("X21", 3, 3, true);
        X[0][1] = solver.intVar("X22", 1, 1, true);
        X[1][0] = solver.intVar("X31", 1, 4, true);
        X[1][1] = solver.intVar("X32", 2, 2, true);
        X[2][0] = solver.intVar("X41", 4, 4, true);
        X[2][1] = solver.intVar("X42", 3, 3, true);
        X[3][0] = solver.intVar("X51", 1, 4, true);
        X[3][1] = solver.intVar("X52", 4, 4, true);

        Y[0][0] = solver.intVar("Y21", 1, 4, true);
        Y[0][1] = solver.intVar("Y22", 1, 4, true);
        Y[1][0] = solver.intVar("Y31", 1, 4, true);
        Y[1][1] = solver.intVar("Y32", 1, 4, true);
        Y[2][0] = solver.intVar("Y41", 1, 4, true);
        Y[2][1] = solver.intVar("Y42", 1, 4, true);
        Y[3][0] = solver.intVar("Y51", 1, 4, true);
        Y[3][1] = solver.intVar("Y52", 1, 4, true);


        solver.post(solver.keySort(X, null, Y, 2));
        solver.set(ISF.lexico_LB(ArrayUtils.flatten(X)), ISF.lexico_LB(ArrayUtils.flatten(Y)));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 16);
    }


    @Test(groups="1s", timeOut=60000)
    public void test6() {
        Solver solver = new Solver();
        IntVar[][] X, Y;
        X = new IntVar[1][4];
        Y = new IntVar[1][4];
        X[0][0] = solver.intVar("X21", 3, 3, true);
        X[0][1] = solver.intVar("X31", 1, 4, true);
        X[0][2] = solver.intVar("X41", 4, 4, true);
        X[0][3] = solver.intVar("X51", 1, 4, true);

        Y[0][0] = solver.intVar("Y21", 1, 4, true);
        Y[0][1] = solver.intVar("Y31", 1, 4, true);
        Y[0][2] = solver.intVar("Y41", 1, 4, true);
        Y[0][3] = solver.intVar("Y51", 1, 4, true);


        solver.post(solver.keySort(X, null, Y, 1));
        solver.set(ISF.lexico_LB(ArrayUtils.flatten(X)), ISF.lexico_LB(ArrayUtils.flatten(Y)));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 16);
    }
}
