/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
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

    @Test(groups="1s", timeOut=60000)
    public void testJL01(){
        Model m = new Model();
        IntVar i = m.intVar("i", -1, 0);
        IntVar j = m.intVar("j", 0, 1);
        IntVar k = m.intVar("k", 0, 1);
        m.sum(new IntVar[]{i, j, k}, "=", m.intVar(1)).post();
        m.getSolver().showSolutions();
        m.getSolver().findAllSolutions();
        Assert.assertEquals(m.getSolver().getSolutionCount(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testFH01(){
        Model m = new Model();
        IntVar[] i = m.intVarArray("i", 3, 0, 2);
        IntVar o = m.intVar("o", 0, 6);
        m.getSolver().setSearch(Search.inputOrderLBSearch(ArrayUtils.append(i, new IntVar[]{o})));
        m.getSolver().plugMonitor((IMonitorSolution) () -> m.sum(i, "=", o).post());
        m.getSolver().findOptimalSolution(o, true);
        Assert.assertEquals(m.getSolver().getBestSolutionValue(), 6);
    }

/////////// SUM GT /////////////////////////////////////////////////////////

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !! **BUG SPOTTED**                                                     !!
     * !! SolverCheck automatically identified a problem in the choco         !!
     * !! implementation of the constraint SUM(X) >= k                        !!
     * !!                                                                     !!
     * !! As shown by the following example:                                  !!
     * !! SUM(x0={2147483636}) > -581118 is considered to always be FALSE     !!
     * !!                                   even though is is TRIVIALLY TRUE  !!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @throws ContradictionException always. But it really should not.
     */
    @Test(groups="1s", timeOut=60000)
    public void sumGtIsSubjectToUnderflowWhenRhsIsConstant() throws ContradictionException {
        Model choco = new Model();

        IntVar x0 = choco.intVar(2_147_483_636);

        choco.sum(new IntVar[]{x0}, ">", -581_118).post();
        choco.getSolver().propagate();

        // Never reached !
        Assert.assertEquals(x0.getLB(), 2147483636);
    }

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !! **BUG SPOTTED**                                                     !!
     * !! SolverCheck automatically identified a problem in the choco         !!
     * !! implementation of the constraint SUM(X) >= k                        !!
     * !!                                                                     !!
     * !! As shown by the following example:                                  !!
     * !! SUM(x0={2147483636}) > x1={-581118} is considered to always be      !!
     * !!                             FALSE even though is is TRIVIALLY TRUE  !!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @throws ContradictionException always. But it really should not.
     */
    @Test(groups="1s", timeOut=60000)
    public void sumGtIsSubjectToUnderflowWhenRhsIsVar() throws ContradictionException {
        Model choco = new Model();

        IntVar x0 = choco.intVar(2147483636);
        IntVar x1 = choco.intVar(-581118);

        choco.sum(new IntVar[]{x0}, ">=", x1).post();
        choco.getSolver().propagate();

        // Never reached !
        Assert.assertEquals(x0.getLB(), 2147483636);
        Assert.assertEquals(x1.getLB(), -581118);
    }


    /////////// SUM GE /////////////////////////////////////////////////////////

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !! **BUG SPOTTED**                                                     !!
     * !! SolverCheck automatically identified a problem in the choco         !!
     * !! implementation of the constraint SUM(X) >= k                        !!
     * !!                                                                     !!
     * !! As shown by the following example:                                  !!
     * !! SUM(x0={2147483636}) >= -27121249 is considered to always be FALSE  !!
     * !!                                   even though is is TRIVIALLY TRUE  !!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @throws ContradictionException always. But it really should not.
     */
    @Test(groups="1s", timeOut=60000)
    public void sumGeIsSubjectToUnderflowWhenRhsIsConstant() throws ContradictionException {
        Model choco = new Model();

        IntVar x0 = choco.intVar(2147483636);

        choco.sum(new IntVar[]{x0}, ">=", -27121249).post();
        choco.getSolver().propagate();

        // Never reached !
        Assert.assertEquals(x0.getLB(), 2147483636);
    }

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !! **BUG SPOTTED**                                                     !!
     * !! SolverCheck automatically identified a problem in the choco         !!
     * !! implementation of the constraint SUM(X) >= k                        !!
     * !!                                                                     !!
     * !! As shown by the following example:                                  !!
     * !! SUM(x0={2147483636}) >= x1={-27121249} is considered to always be   !!
     * !!                             FALSE even though is is TRIVIALLY TRUE  !!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @throws ContradictionException always. But it really should not.
     */
    @Test(groups="1s", timeOut=60000)
    public void sumGeIsSubjectToUnderflowWhenRhsIsVar() throws ContradictionException {
        Model choco = new Model();

        IntVar x0 = choco.intVar(2147483636);
        IntVar x1 = choco.intVar(-27121249);

        choco.sum(new IntVar[]{x0}, ">=", x1).post();
        choco.getSolver().propagate();

        // Never reached !
        Assert.assertEquals(x0.getLB(), 2147483636);
        Assert.assertEquals(x1.getLB(), -27121249);
    }

    /////////// SUM LE /////////////////////////////////////////////////////////

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !! **BUG SPOTTED**                                                     !!
     * !! SolverCheck automatically identified a problem in the choco         !!
     * !! implementation of the constraint SUM(X) >= k                        !!
     * !!                                                                     !!
     * !! As shown by the following example:                                  !!
     * !! SUM(x0={2147483636}) <= -35468 is considered to always be TRUE      !!
     * !!                                  even though is is TRIVIALLY FALSE  !!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @throws ContradictionException always. But it really should not.
     */
    @Test(groups="1s", timeOut=60000,expectedExceptions = ContradictionException.class)
    public void sumLeIsSubjectToUnderflowWhenRhsIsConstant() throws ContradictionException {
        Model choco = new Model();

        IntVar x0 = choco.intVar(2147483636);

        choco.sum(new IntVar[]{x0}, "<=", -35468).post();
        choco.getSolver().propagate();

        // This point should never be reached !
    }

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !! **BUG SPOTTED**                                                     !!
     * !! SolverCheck automatically identified a problem in the choco         !!
     * !! implementation of the constraint SUM(X) >= k                        !!
     * !!                                                                     !!
     * !! As shown by the following example:                                  !!
     * !! SUM(x0={2147483636}) <= x1={-35468} is considered to always be TRUE !!
     * !!                                  even though is is TRIVIALLY FALSE  !!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @throws ContradictionException never. But it really should throw one.
     */
    @Test(groups="1s", timeOut=60000,expectedExceptions = ContradictionException.class)
    public void sumLeIsSubjectToUnderflowWhenRhsIsVar() throws ContradictionException {
        Model choco = new Model();

        IntVar x0 = choco.intVar(2147483636);
        IntVar x1 = choco.intVar(-35468);

        choco.sum(new IntVar[]{x0}, "<=", x1).post();
        choco.getSolver().propagate();

        // This point should never be reached !
    }


    /////////// SUM LT /////////////////////////////////////////////////////////

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !! **BUG SPOTTED**                                                     !!
     * !! SolverCheck automatically identified a problem in the choco         !!
     * !! implementation of the constraint SUM(X) >= k                        !!
     * !!                                                                     !!
     * !! As shown by the following example:                                  !!
     * !! SUM(x0={2147483636}) < -581118 is considered to always be TRUE      !!
     * !!                                  even though is is TRIVIALLY FALSE  !!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @throws ContradictionException always. But it really should not.
     */
    @Test(groups="1s", timeOut=60000,expectedExceptions = ContradictionException.class)
    public void sumLtIsSubjectToUnderflowWhenRhsIsConstant() throws ContradictionException {
        Model choco = new Model();

        IntVar x0 = choco.intVar(2147483636);

        choco.sum(new IntVar[]{x0}, "<", -581118).post();
        choco.getSolver().propagate();

        // This point should never be reached !
    }

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !! **BUG SPOTTED**                                                     !!
     * !! SolverCheck automatically identified a problem in the choco         !!
     * !! implementation of the constraint SUM(X) >= k                        !!
     * !!                                                                     !!
     * !! As shown by the following example:                                  !!
     * !! SUM(x0={2147483636}) <= x1={-581118} is considered to always be TRUE!!
     * !!                                  even though is is TRIVIALLY FALSE  !!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @throws ContradictionException never. But it really should throw one.
     */
    @Test(groups="1s", timeOut=60000,expectedExceptions = ContradictionException.class)
    public void sumLtIsSubjectToUnderflowWhenRhsIsVar() throws ContradictionException {
        Model choco = new Model();

        IntVar x0 = choco.intVar(2_147_483_636);
        IntVar x1 = choco.intVar(-581118);

        choco.sum(new IntVar[]{x0}, "<", x1).post();
        choco.getSolver().propagate();

        // This point should never be reached !
    }

    ///
    /**
     * This case illustrates that the same problem occurs even when there is
     * more than one single variable involved in the constraint.
     *
     * @throws ContradictionException always. But it really should not.
     */
    @Test(groups="1s", timeOut=60000)
    public void isAlsoHappensWhenThereIsMoreThanOneVariableInTheSum() throws ContradictionException {
        Model choco = new Model();

        IntVar x0 = choco.intVar(715827876);
        IntVar x1 = choco.intVar(715827877);
        IntVar x2 = choco.intVar(715827878);

        // All of the below cases reproduce the bug.
        choco.sum(new IntVar[]{x0, x1, x2}, ">=", -581118).post();
        //choco.sum(new IntVar[]{x0, x1, x2}, ">", -581118).post();
        //choco.sum(new IntVar[]{x0, x1, x2}, "<", -581118).post();
        //choco.sum(new IntVar[]{x0, x1, x2}, "<=", -581118).post();
        choco.getSolver().propagate();

        // Never reached !
        Assert.assertEquals(x0.getLB(), 715827876);
        Assert.assertEquals(x1.getLB(), 715827877);
        Assert.assertEquals(x2.getLB(), 715827878);
    }

}
