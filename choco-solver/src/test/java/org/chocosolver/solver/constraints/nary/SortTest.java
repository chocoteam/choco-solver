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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.nary.sort.PropSort;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/04/2014
 */
public class SortTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Solver solver = new Solver();
        IntVar[] X, Y;
        X = new IntVar[5];
        Y = new IntVar[5];
        X[0] = VariableFactory.bounded("X1", 1, 16, solver);
        X[1] = VariableFactory.bounded("X2", 5, 10, solver);
        X[2] = VariableFactory.bounded("X3", 7, 9, solver);
        X[3] = VariableFactory.bounded("X4", 12, 15, solver);
        X[4] = VariableFactory.bounded("X5", 1, 13, solver);

        Y[0] = VariableFactory.bounded("Y1", 2, 3, solver);
        Y[1] = VariableFactory.bounded("Y2", 6, 7, solver);
        Y[2] = VariableFactory.bounded("Y3", 8, 11, solver);
        Y[3] = VariableFactory.bounded("Y4", 13, 16, solver);
        Y[4] = VariableFactory.bounded("Y5", 14, 18, solver);

        solver.post(new Constraint("sort", new PropSort(X, Y)));
        /*if (solver.findSolution()) {
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
            } while (solver.nextSolution());
        }*/
        Assert.assertEquals(solver.findAllSolutions(), 182);

    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Solver solver = new Solver();
        IntVar[] X, Y;
        X = new IntVar[3];
        Y = new IntVar[3];
        X[0] = VariableFactory.bounded("X1", 0, 0, solver);
        X[1] = VariableFactory.bounded("X2", 0, 1, solver);
        X[2] = VariableFactory.bounded("X3", 1, 1, solver);

        Y[0] = VariableFactory.bounded("Y1", 0, 0, solver);
        Y[1] = VariableFactory.bounded("Y2", 0, 0, solver);
        Y[2] = VariableFactory.bounded("Y3", 1, 1, solver);

        solver.post(ICF.sort(X, Y));
        try {
            solver.propagate();
            Assert.assertEquals(X[1].getValue(), 0);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Solver solver = new Solver();
        IntVar[] X, Y;
        X = new IntVar[3];
        Y = new IntVar[3];
        X[0] = VariableFactory.bounded("X1", 2, 2, solver);
        X[1] = VariableFactory.bounded("X2", 0, 2, solver);
        X[2] = VariableFactory.bounded("X3", 0, 0, solver);

        Y[0] = VariableFactory.bounded("Y1", 0, 0, solver);
        Y[1] = VariableFactory.bounded("Y2", 0, 0, solver);
        Y[2] = VariableFactory.bounded("Y3", 2, 2, solver);

        solver.post(ICF.sort(X, Y));
        try {
            solver.propagate();
            Assert.assertEquals(X[1].getValue(), 0);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Solver solver = new Solver();
        IntVar[] X, Y;
        X = new IntVar[3];
        Y = new IntVar[3];
        X[0] = VariableFactory.bounded("X1", 0, 7, solver);
        X[1] = VariableFactory.bounded("X2", 3, 5, solver);
        X[2] = VariableFactory.bounded("X3", 1, 5, solver);

        Y[0] = VariableFactory.bounded("Y1", 0, 2, solver);
        Y[1] = VariableFactory.bounded("Y2", 1, 9, solver);
        Y[2] = VariableFactory.bounded("Y3", 7, 9, solver);

        solver.post(ICF.sort(X, Y));
        try {
            solver.propagate();
            Assert.assertEquals(X[0].getValue(), 7);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }
}
