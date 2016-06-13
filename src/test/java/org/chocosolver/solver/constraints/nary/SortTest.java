/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
