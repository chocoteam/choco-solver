/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 12/09/2017.
 */
public class JSONNaryConstraintTest extends JSONConstraintTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testAlldiferent() {
        Model model = new Model();
        IntVar[] x = model.intVarArray(5, 0, 5);
        model.allDifferent(x).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAmong() {
        Model model = new Model();
        IntVar x = model.intVar(1, 2);
        IntVar[] y = model.intVarArray(5, 0, 5);
        model.among(x, y, new int[]{2, 3}).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAtleast() {
        Model model = new Model();
        IntVar x = model.intVar(1, 2);
        IntVar[] y = model.intVarArray(5, 0, 5);
        model.atLeastNValues(y, x, true).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAtMost() {
        Model model = new Model();
        IntVar x = model.intVar(1, 2);
        IntVar[] y = model.intVarArray(5, 0, 5);
        model.atMostNValues(y, x, true).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBinPacjing() {
        Model model = new Model();
        int[] itemSize = new int[]{2, 3, 1};
        IntVar[] itemBin = model.intVarArray("binOfItem", 3, -1, 1);
        IntVar[] binLoad = model.intVarArray("binLoad", 2, 3, 3);
        model.binPacking(itemBin, itemSize, binLoad, 0).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000,
            dataProvider = "bool", dataProviderClass = JSONConstraintTest.class)
    public void testboolsIntChanneling(boolean bounded) {
        Model model = new Model();
        BoolVar[] boolVars = model.boolVarArray(5);
        IntVar intVar = model.intVar(0, 4, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testbitsIntChanneling() {
        Model model = new Model();
        BoolVar[] bits = model.boolVarArray(7);
        IntVar intVar = model.intVar(10);
        model.bitsIntChanneling(bits, intVar).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCircuit() {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 4, 0, 8, true);
        model.circuit(x).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCountCst() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(2, 0, 2);
        IntVar occ = model.intVar(0, 2);
        model.count(1, vars, occ).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCountVar() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(2, 0, 2);
        IntVar val = model.intVar("V", 1, 3);
        IntVar occ = model.intVar("L", 0, 2);
        model.count(val, vars, occ).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000,
            dataProvider = "bool", dataProviderClass = JSONConstraintTest.class)
    public void testCumulative(boolean bounded) {
        final Model model = new Model();
        final IntVar[] s = model.intVarArray("s", 3, 0, 4, false);
        final IntVar[] d = model.intVarArray("d", 3, 3, 5, false);
        final IntVar[] e = model.intVarArray("e", 3, 4, 10, false);
        final IntVar[] h = model.intVarArray("h", 3, 0, 2, false);
        final IntVar capa = model.intVar("C", 0, 5, false);
        Task[] t = new Task[3];
        for (int i = 0; i < 3; i++) {
            t[i] = new Task(s[i], d[i], e[i]);
        }
        model.cumulative(t, h, capa, bounded).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = UnsupportedOperationException.class)
    public void testDiffn1() {
        Model model = new Model();
        IntVar[] x = model.intVarArray(2, 0, 3);
        IntVar[] y = model.intVarArray(2, 2, 5);
        IntVar[] dx = model.intVarArray(2, 0, 2);
        IntVar[] dy = model.intVarArray(2, 0, 1);
        model.diffN(x, y, dx, dy, true).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDiffn2() {
        Model model = new Model();
        IntVar[] x = model.intVarArray(2, 0, 3);
        IntVar[] y = model.intVarArray(2, 2, 5);
        IntVar[] dx = model.intVarArray(2, 0, 2);
        IntVar[] dy = model.intVarArray(2, 0, 1);
        model.diffN(x, y, dx, dy, false).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testElement() {
        Model model = new Model();
        IntVar val = model.intVar(4, 6);
        IntVar[] values = model.intVarArray(3, 4, 6);
        IntVar idx = model.intVar(1, 3);
        model.element(val, values, idx, 1).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000,
            dataProvider = "bool", dataProviderClass = JSONConstraintTest.class)
    public void testGcc(boolean bounded) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(6, 0, 4, true);
        IntVar[] card = model.intVarArray(4, 0, 6, true);
        int[] values = new int[4];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }
        model.globalCardinality(vars, values, card, bounded).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000,
            dataProvider = "bool", dataProviderClass = JSONConstraintTest.class)
    public void testInversechanneling(boolean bounded) {
        Model model = new Model();
        IntVar[] intVars1 = model.intVarArray(5, 1, 5, bounded);
        IntVar[] intVars2 = model.intVarArray(5, 0, 4, bounded);
        model.inverseChanneling(intVars1, intVars2, 1, 0).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testKnapsack() {
        Model model = new Model();
        IntVar[] os = model.intVarArray(4, 0, 3);
        IntVar c = model.intVar(0, 10);
        IntVar e = model.intVar(0, 20);
        int[] ws = new int[]{5, 4, 3, 6};
        int[] es = new int[]{7, 9, 5, 1};
        model.knapsack(os, c, e, ws, es).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testLexchain() {
        Model model = new Model();
        IntVar[][] X = new IntVar[2][3];
        for (int i = 0; i < 2; i++) {
            X[i] = model.intVarArray(3, 1, 4);
        }
        model.lexChainLess(X).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testLexchainEq() {
        Model model = new Model();
        IntVar[][] X = new IntVar[2][3];
        for (int i = 0; i < 2; i++) {
            X[i] = model.intVarArray(3, 1, 4);
        }
        model.lexChainLessEq(X).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testLex() {
        Model model = new Model();
        IntVar[] X = model.intVarArray(3, 1, 4);
        IntVar[] Y = model.intVarArray(3, 1, 4);
        model.lexLess(X, Y).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testLexEq() {
        Model model = new Model();
        IntVar[] X = model.intVarArray(3, 1, 4);
        IntVar[] Y = model.intVarArray(3, 1, 4);
        model.lexLessEq(X, Y).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMax() {
        Model model = new Model();
        IntVar x = model.intVar(2, 10);
        IntVar[] ys = model.intVarArray(5, 3, 9);
        model.max(x, ys).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMin() {
        Model model = new Model();
        IntVar x = model.intVar(2, 10);
        IntVar[] ys = model.intVarArray(5, 3, 9);
        model.min(x, ys).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMaxBool() {
        Model model = new Model();
        BoolVar x = model.boolVar();
        BoolVar[] ys = model.boolVarArray(5);
        model.max(x, ys).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMinBool() {
        Model model = new Model();
        BoolVar x = model.boolVar();
        BoolVar[] ys = model.boolVarArray(5);
        model.min(x, ys).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNvalues() {
        Model model = new Model();
        IntVar[] XS = model.intVarArray(6, 0, 5);
        IntVar N = model.intVar(6);
        model.nValues(XS, N).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testSum(String sign) {
        Model model = new Model();
        IntVar[] XS = model.intVarArray(6, 0, 5);
        IntVar N = model.intVar(0, 6);
        model.sum(XS, sign, N).post();
        eval(model, true/*factory rearrange variables*/);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testBoolSum(String sign) {
        Model model = new Model();
        BoolVar[] XS = model.boolVarArray(6);
        IntVar N = model.intVar(0, 6);
        model.sum(XS, sign, N).post();
        eval(model, true/*factory rearrange variables*/);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testScalar(String sign) {
        Model model = new Model();
        IntVar[] XS = model.intVarArray(6, 0, 6);
        int[] cs = new int[]{1, 2, 3, 4, 5, 6};
        IntVar N = model.intVar(4, 6);
        model.scalar(XS, cs, sign, N).post();
        eval(model, true/*factory rearrange variables*/);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSubcircuit() {
        Model model = new Model();
        IntVar[] x = model.intVarArray(5, 0, 10, true);
        IntVar y = model.intVar(0, x.length - 1, true);
        model.subCircuit(x, 0, y).post();
        eval(model, true);
    }

    @DataProvider(name = "table")
    private Object[][] algo() {
        return new String[][]{
                {"GAC2001"},{"FC"}, {"GACSTR+"},
                {"GAC2001+"}, {"GAC3rm+"}, {"GAC3rm"},
                {"STR2+"}, {"MDD+"},{"CT+"},
        };
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "table")
    public void testTable(String algo) {
        Model model = new Model();
        IntVar x = model.intVar(0, 4);
        IntVar y = model.boolVar();
        IntVar z = model.boolVar();
        Tuples t = new Tuples(true);
        t.add(0, -1, 1);
        t.add(0, 0, 1);
        t.add(2, -1, 1);
        t.add(1, 0, 1);
        model.table(new IntVar[]{x, y, z}, t, algo).post();
        eval(model, true);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testTree() {
        Model model = new Model();
        IntVar[] VS = model.intVarArray(6, -1, 6, false);
        IntVar NT = model.intVar(2, 3, false);
        model.tree(VS, NT, 0).post();
        eval(model, false);
    }

}