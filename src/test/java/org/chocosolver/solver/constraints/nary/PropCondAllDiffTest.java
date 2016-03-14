package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.Condition;
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
    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void testTrueCondition(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 4);
        model.allDifferentUnderCondition(vars, Condition.TRUE, ac).post();
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
    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void testNominal(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 2);
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);
        model.allDifferentUnderCondition(vars,mustBeDiff::get,ac).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        this.solveAndCheck(vars, model);
    }

    /**
     * Nominal test case with negative values
     */
    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void testNominalNegative(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, -2, 0);
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, ac).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        this.solveAndCheck(vars, model);
    }

    /**
     * Nominal test case, with enumerated values
     */
    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void testEnumeratedValues(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, new int[]{0, 5, 7, 5, 5, 8});
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, ac).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        this.solveAndCheck(vars, model);
    }

    /**
     * A single variable is subject to the AllDifferent constraint
     * It must lead to trivial solutions
     */
    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void singleVariable(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 4);
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 1);
        model.allDifferentUnderCondition(vars, mustBeDiff::get, ac).post();

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
    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void noVariable(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 4);
        model.allDifferentUnderCondition(vars, v -> false, ac).post();

        boolean solutionFound = false;
        while(model.solve()) {
            solutionFound = true;
        }
        assertTrue(solutionFound);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void testImpossible(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 2);
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 4);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, ac).post();

        assertFalse(model.solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void testExceptZero(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = new IntVar[5];
        vars[0] = model.intVar(1, 3);
        vars[1] = model.intVar(1, 3);
        vars[2] = model.intVar(1, 3);
        vars[3] = model.intVar(0, 3);
        vars[4] = model.intVar(0, 3);

        model.allDifferentUnderCondition(vars, Condition.EXCEPT_0, ac).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        this.solveAndCheck(vars, model);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void testFreeVariables(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 4);
        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, ac).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        this.solveAndCheck(vars, model);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void testTrivialTrue(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = new IntVar[5];
        vars[0] = model.intVar(1);
        vars[1] = model.intVar(2);
        vars[2] = model.intVar(3);
        vars[3] = model.intVar(4);
        vars[4] = model.intVar(5);

        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, ac).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        solveAndCheck(vars, model);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void testTrivialFalse(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = new IntVar[5];
        vars[0] = model.intVar(1);
        vars[1] = model.intVar(1);
        vars[2] = model.intVar(2);
        vars[3] = model.intVar(0, 6);
        vars[4] = model.intVar(0, 6);

        Map<IntVar, Boolean> mustBeDiff = mustBeDiff(vars, 3);

        model.allDifferentUnderCondition(vars, mustBeDiff::get, ac).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);

        assertFalse(model.solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "ac")
    public void testDynamicCondition(Boolean ac) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 2);

        model.allDifferentUnderCondition(vars, v -> v.getLB() > 1, ac).post();
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
