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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.stream;
import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class SumTest {

    private Model model;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        model = new Model();
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominal() throws ContradictionException{
        IntVar[] vars = model.intVarArray(5, 0, 5);
        IntVar sum = model.intVar(15, 20);
        model.sum(vars, "=", sum).post();
        int nbSol = checkSolutions(vars, sum);

        // compare to scalar
        int[] coeffs = new int[]{1, 1, 1, 1, 1};
        model = new Model();
        vars = model.intVarArray(5, 0, 5);
        sum = model.intVar(15, 20);
        model.scalar(vars, coeffs, "=", sum).post();
        int nbSol2 = 0;
        while (model.getSolver().solve()) {
            nbSol2++;
        }
        assertEquals(nbSol, nbSol2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoSolution() {
        IntVar[] vars = model.intVarArray(5, 0, 5);
        IntVar sum = model.intVar(26, 30);
        model.sum(vars, "=", sum).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testZero() {
        IntVar[] vars = new IntVar[]{
                model.intVar(-5, -1),
                model.intVar(1, 5),
                model.intVar(-5, -1),
                model.intVar(1, 5)
        };
        IntVar sum = model.intVar(0);
        model.sum(vars, "=", sum).post();

        checkSolutions(vars, sum);
    }


    @Test(groups = "1s", timeOut=60000)
    public void testSameVariableSolution() {
        IntVar ref = model.intVar(1, 5);
        IntVar[] vars = new IntVar[]{ref, ref};
        model.sum(vars, "=", 10).post();

        checkSolutions(vars, model.intVar(10));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSameVariableNoSolution() {
        IntVar ref = model.intVar(1, 5);
        IntVar[] vars = new IntVar[]{ref, ref};
        model.sum(vars, "=", 9).post();

        assertFalse(model.getSolver().solve());
    }


    @Test(groups = "1s", timeOut=60000)
    public void testZeroElements() {
        IntVar[] vars = new IntVar[0];
        IntVar sum = model.intVar(-100, 100);
        model.sum(vars, "=", sum).post();

        assertEquals(checkSolutions(vars, sum), 1);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoExactSolution() {
        IntVar[] vars = model.intVarArray(5, new int[]{0, 2});
        IntVar sum = model.intVar(101);
        model.sum(vars, "=", sum).post();

        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSimpleSum() {
        IntVar[] vars = model.intVarArray(2, new int[]{2, 3});
        IntVar sum = model.intVar(5);
        model.sum(vars, ">=", sum).post();

        assertEquals(checkSolutions(">=", vars, sum), 3);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testJustAbove() {
        IntVar[] vars = model.intVarArray(6, 6, 10);
        IntVar sum = model.intVar(0, 36);
        model.sum(vars, "<", sum).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void justBelow() {
        IntVar[] vars = model.intVarArray(7, 0, 7);
        IntVar sum = model.intVar(49);
        model.sum(vars, ">", sum).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }


    private int checkSolutions(IntVar[] intVars, IntVar sum) {
        return checkSolutions("=", intVars, sum);
    }

    private int checkSolutions(String operator, IntVar[] intVars, IntVar sum) {
        Model model = sum.getModel();
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            int computed = stream(intVars)
                    .mapToInt(IntVar::getValue)
                    .sum();
            switch (operator) {
                case "=":
                    assertEquals(computed, sum.getValue());
                    break;
                case ">=":
                    assertTrue(computed >= sum.getValue());
                    break;
                case "<=":
                    assertTrue(computed <= sum.getValue());
                    break;
            }

        }
        assertTrue(nbSol > 0);
        return nbSol;
    }


}
