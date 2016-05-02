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
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.Condition;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */


public class PropCondAllDiffTest {

    /**
     * Provides two test data to use the two propagation methods: ac and without ac
     * @return ac boolean
     */
    @DataProvider(name = "ac")
    public Object[][] createData() {
        return new Object[][] {
                new Object[]{false},
                new Object[]{true}
        };
    }

    /**
     * Give a <code>TRUE</code> condition to have a AllDifferent like handling
     */
    @Test(groups = "1s", timeOut=60000)
    public void testTrueCondition() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 4);
        model.allDifferentUnderCondition(vars, Condition.TRUE, false).post();
        while(model.solve());

        Model model2 = new Model();
        IntVar[] vars2 = model2.intVarArray(5, 0, 4);
        model2.allDifferent(vars2).post();
        while(model2.solve());

        assertEquals(model.getSolver().getSolutionCount(), model2.getSolver().getSolutionCount());
    }

    /**
     * Nominal test case : three variables are subject to the AllDifferent constraint
     */
    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 2);
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);
        model.allDifferentUnderCondition(vars,mustBeDiff::get,false).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        this.solveAndCheck(vars, model);
    }

    /**
     * Nominal test case with negative values
     */
    @Test(groups = "1s", timeOut=60000)
    public void testNominalNegative() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, -2, 0);
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, false).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        this.solveAndCheck(vars, model);
    }

    /**
     * Nominal test case, with enumerated values
     */
    @Test(groups = "1s", timeOut=60000)
    public void testEnumeratedValues() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, new int[]{0, 5, 7, 5, 5, 8});
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, false).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        this.solveAndCheck(vars, model);
    }

    /**
     * A single variable is subject to the AllDifferent constraint
     * It must lead to trivial solutions
     */
    @Test(groups = "1s", timeOut=60000)
    public void singleVariable() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 4);
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 1);
        model.allDifferentUnderCondition(vars, mustBeDiff::get, false).post();

        boolean solutionFound = false;
        while(model.solve()) {
            solutionFound = true;
        }
        assertTrue(solutionFound);
    }

    /**
     * No variable is subject to the AllDifferent constraint
     * The constraint is always satisfied, as each variable is different from the others
     */
    @Test(groups = "1s", timeOut=60000)
    public void noVariable() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 4);
        model.allDifferentUnderCondition(vars, v -> false, false).post();

        boolean solutionFound = false;
        while(model.solve()) {
            solutionFound = true;
        }
        assertTrue(solutionFound);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testImpossible() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 2);
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 4);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, false).post();

        assertFalse(model.solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testExceptZero() {
        Model model = new Model();
        IntVar[] vars = new IntVar[5];
        vars[0] = model.intVar(1, 3);
        vars[1] = model.intVar(1, 3);
        vars[2] = model.intVar(1, 3);
        vars[3] = model.intVar(0, 3);
        vars[4] = model.intVar(0, 3);

        model.allDifferentUnderCondition(vars, Condition.EXCEPT_0, false).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        this.solveAndCheck(vars, model);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testFreeVariables() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 4);
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, false).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        this.solveAndCheck(vars, model);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTrivialTrue() {
        Model model = new Model();
        IntVar[] vars = new IntVar[5];
        vars[0] = model.intVar(1);
        vars[1] = model.intVar(2);
        vars[2] = model.intVar(3);
        vars[3] = model.intVar(4);
        vars[4] = model.intVar(5);

        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, false).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        solveAndCheck(vars, model);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testPropagation() {Model m = new Model();
        IntVar x = m.intVar(new int[]{0,1,3,4});
        IntVar y = m.intVar(4);
        m.allDifferentExcept0(new IntVar[]{x,y}).post();
        System.out.println(x);
        System.out.println(y);
        System.out.println("%%%");
        try {
            m.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        System.out.println(x);
        System.out.println(y);
        assertFalse(x.contains(4));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTrivialFalse() {
        Model model = new Model();
        IntVar[] vars = new IntVar[5];
        vars[0] = model.intVar(1);
        vars[1] = model.intVar(1);
        vars[2] = model.intVar(2);
        vars[3] = model.intVar(0, 6);
        vars[4] = model.intVar(0, 6);

        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, false).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);

        assertFalse(model.solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testDynamicCondition() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 2);

        model.allDifferentUnderCondition(vars, v -> v.getLB() > 1, false).post();
        boolean solutionFound = false;
        if (model.solve()) {
            solutionFound = true;
        }
        assertTrue(solutionFound);
    }


    private Map<IntVar, Boolean> mustBeDiff(IntVar[] vars, int nDiff) {
        Map<IntVar, Boolean> mustBeDiff = new HashMap<>();
        for (int i=0;i<vars.length;i++) {
            mustBeDiff.put(vars[i], i<nDiff);
        }
        return mustBeDiff;
    }

    /**
     * The first three variables of the array must be different for each solution
     * @param vars variables to check
     * @param model model permitting to reach the {@link Model#solve()} method
     */
    private void solveAndCheck(IntVar[] vars, Model model) {
        boolean solutionFound = false;
        while(model.solve()) {
            solutionFound = true;
            assertNotEquals(vars[0].getValue(), vars[1].getValue());
            assertNotEquals(vars[1].getValue(), vars[2].getValue());
            assertNotEquals(vars[0].getValue(), vars[2].getValue());
        }
        assertTrue(solutionFound);
    }

}
