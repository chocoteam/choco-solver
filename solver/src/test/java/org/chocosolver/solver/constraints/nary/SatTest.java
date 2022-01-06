/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;


import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.util.ESat.FALSE;
import static org.chocosolver.util.ESat.TRUE;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/13
 */
public class SatTest {

    @DataProvider(name = "satornot")
    public Object[][] provide() {
        return new Object[][]{{true}, {false}};
    }

    @Test(groups = "1s", timeOut = 600000, dataProvider = "satornot")
    public void test1(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar b1, b2;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        model.addClausesBoolEq(b1, b2);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test2(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar b1, b2;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        model.addClausesBoolNot(b1, b2);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test3(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar b1, b2;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        model.addClausesBoolLe(b1, b2);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 3);
    }


    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test4(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar b1, b2, r;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        r = model.boolVar("r");
        model.addClausesBoolIsEqVar(b1, b2, r);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test5(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar b1, b2, r;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        r = model.boolVar("r");
        model.addClausesBoolAndEqVar(b1, b2, r);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test6(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar b1, b2, r;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        r = model.boolVar("r");
        model.addClausesBoolOrEqVar(b1, b2, r);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test7(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar b1, b2;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        model.addClausesBoolLt(b1, b2);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test8(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar b1, b2, r;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        r = model.boolVar("r");
        model.addClausesBoolIsLeVar(b1, b2, r);
//        SMF.log(solver, true, true);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test9(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar b1, b2, r;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        r = model.boolVar("r");
        model.addClausesBoolIsLtVar(b1, b2, r);
//        SMF.log(solver, true, true);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4);
    }


    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test10(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar b1;
        b1 = model.boolVar("b1");
        model.addClauseTrue(b1);
        //        SMF.log(solver, true, true);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 1);
        assertEquals(b1.getBooleanValue(), TRUE);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test11(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar b1;
        b1 = model.boolVar("b1");
        model.addClauseFalse(b1);
        //        SMF.log(solver, true, true);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 1);
        assertEquals(b1.getBooleanValue(), FALSE);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test12(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar[] bs = model.boolVarArray("b", 3);
        model.addClausesBoolOrArrayEqualTrue(bs);
        model.addClauseFalse(bs[0]);
        model.addClauseFalse(bs[1]);
        model.addClauseFalse(bs[2]);
        //        SMF.log(solver, true, true);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void test13(boolean sat) {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        BoolVar[] bs = model.boolVarArray("b", 3);
        BoolVar d = model.boolVar("d");
        model.addClausesSumBoolArrayLessEqKVar(bs, d);

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 9);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "satornot")
    public void testAlexLoboda(boolean sat) throws ContradictionException {
        Model model = new Model(Settings.init().setEnableSAT(sat));
        // VARS
        IntVar var = model.intVar("var", new int[]{0, 2});
        BoolVar eq2 = model.boolVar("eq2");
        BoolVar bvar = model.boolVar("already");
        BoolVar bvar2 = model.boolVar("bvar2");
        BoolVar cond = model.boolVar("cond");
        // CSTRS
        model.addClauseFalse(bvar);
        model.arithm(var, "=", 2).reifyWith(eq2);
        model.addClausesBoolAndArrayEqVar(new BoolVar[]{eq2, bvar.not()}, cond);
        model.addClausesBoolOrArrayEqualTrue(new BoolVar[]{eq2.not(), cond});
        model.addClausesBoolOrArrayEqVar(new BoolVar[]{bvar, cond}, bvar2);
        // SEARCH
        model.getSolver().setSearch(inputOrderLBSearch(var));

        model.setObjective(Model.MAXIMIZE, var);
        Solution solution = new Solution(model);
        while (model.getSolver().solve()) {
            solution.record();
        }
        assertEquals(solution.getIntVal(var), 2);

    }


    @Test(groups = "1s")
    public void testSatVar() {
        Model model = new Model();
        BoolVar[] boolVars = model.boolVarArray(3);
        int b1 = boolVars[0].satVar();
        int b2 = boolVars[1].satVar();
        int b3 = boolVars[2].satVar();
        model.addClause(model.lit(b1), model.lit(b2), model.lit(b3));
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 7);
        Assert.assertEquals(model.getMinisat().getPropSat().getMiniSat().solve(),
                TRUE);
    }

    @Test(groups = "1s")
    public void testSatVar2() {
        Model model = new Model();
        IntVar[] intVars = model.intVarArray(3, 1, 2);
        int b1 = intVars[0].eq(1).satVar();
        int b2 = intVars[1].eq(1).satVar();
        int b3 = intVars[2].eq(1).satVar();
        model.addClause(model.lit(b1), model.lit(b2), model.lit(b3));
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 7);
        Assert.assertEquals(model.getMinisat().getPropSat().getMiniSat().solve(),
                TRUE);
    }
}
