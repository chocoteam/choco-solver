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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class ScalarTest {

    private Model model;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        model = new Model();
    }


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        int[] coeffs = new int[]{1, 5, 7, 8};
        IntVar[] vars = model.intVarArray(4, new int[]{1, 3, 5});
        model.scalar(vars, coeffs, "=", 35).post();

        checkSolutions(coeffs, vars, model.intVar(35));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominalBounded() {
        int[] coeffs = new int[]{1, 5, 7, 8};
        IntVar[] vars = model.intVarArray(4, 1, 6, true);
        model.scalar(vars, coeffs, "=", 35).post();

        checkSolutions(coeffs, vars, model.intVar(35));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominalBoundedWithNegatives() {
        int[] coeffs = new int[]{5, 6, 7, 9};
        IntVar[] vars = model.intVarArray(4, -5, 5, true);
        model.scalar(vars, coeffs, "<=", 0).post();

        checkSolutions(coeffs, vars, model.intVar(0), "<=");
    }

    @Test(groups = "1s", timeOut=60000)
    public void testCoeffAtZeroSolutions() {
        int[] coeffs = new int[]{0, 4, 5};
        IntVar[] vars = model.intVarArray(3, 0, 1000);
        model.scalar(vars, coeffs, "=", 9).post();

        model.getEnvironment().worldPush();
        int nbSol = checkSolutions(coeffs, vars, model.intVar(9));
        model.getEnvironment().worldPop();

        assertEquals(nbSol, 1001);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testCoeffAtZeroNoSolutions() {
        int[] coeffs = new int[]{0};
        IntVar[] vars = new IntVar[]{
                model.intVar(1, 10)
        };
        model.scalar(vars, coeffs, ">=", 1).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testWithSumVariable() {
        int[] coeffs = new int[]{1};
        IntVar[] vars = new IntVar[]{
            model.intVar(1, 100)
        };
        IntVar sum = model.intVar(1, 100);
        model.scalar(vars, coeffs, "=", sum).post();

        assertEquals(checkSolutions(coeffs, vars, sum) , 100);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSameVariableNoSolution() {
        IntVar ref = model.intVar(new int[]{1, 5});
        IntVar[] vars = new IntVar[]{ref, ref};
        int[] coeffs = new int[]{1, 1};
        model.scalar(vars, coeffs, "=", 6).post();

        assertFalse(model.solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSameVariableSolution() {
        IntVar ref = model.intVar(1, 5);
        IntVar[] vars = new IntVar[]{ref, ref};
        int[] coeffs = new int[]{1, 3};
        model.scalar(vars, coeffs, "=", 20).post();

        checkSolutions(coeffs, vars, model.intVar(20));
    }

    private int checkSolutions(int[] coeffs, IntVar[] vars, IntVar sum, String operator) {
        Model model = vars[0].getModel();
        int nbSol = 0;
        while (model.solve()) {
            nbSol++;
            int computed = 0;
            for (int i = 0; i < vars.length; i++) {
                computed += coeffs[i] * vars[i].getValue();
            }
            switch (operator) {
                case "=":
                    assertEquals(sum.getValue(), computed);
                    break;
                case "<=":
                    assertTrue(computed <= sum.getValue());
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }

    private int checkSolutions(int[] coeffs, IntVar[] vars, IntVar sum) {
        return checkSolutions(coeffs, vars, sum, "=");
    }

}
