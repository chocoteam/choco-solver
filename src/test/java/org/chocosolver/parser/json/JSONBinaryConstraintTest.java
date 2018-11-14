/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 12/09/2017.
 */
public class JSONBinaryConstraintTest extends JSONConstraintTest {

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testArithm1(String sign) {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.arithm(x, sign, y).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testArithm2(String sign) {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.arithm(x, sign, y, "+", 3).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testArithm3(String sign) {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.arithm(x, sign, y, "-", 3).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testArithmReif1(String sign) {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.arithm(x, sign, y).reify();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testArithmReif2(String sign) {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.arithm(x, sign, y, "+", 3).reify();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testArithmReif3(String sign) {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.arithm(x, sign, y, "-", 3).reify();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAbs() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.absolute(x, y).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAbsReif() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.absolute(x, y).reify();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSqr() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.square(x, y).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSqrReif() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.square(x, y).reify();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testScal() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.times(x, 2, y).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testScalReif() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.times(x, 2, y).reify();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDist() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.distance(x, y, "<", 3).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDistReif() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.distance(x, y, "<", 3).reify();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDistReifTrue() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.distance(x, y, "<", 3).reifyWith(model.boolVar(true));
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDistReifFalse() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(3, 7);
        model.distance(x, y, "<", 3).reifyWith(model.boolVar(false));
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testElmt() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        int[] values = new int[]{3, 6, 4, 5, 2};
        IntVar y = model.intVar(1, 5);
        model.element(x, values, y, 1).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testElmtReif() {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        int[] values = new int[]{3, 6, 4, 5, 2};
        IntVar y = model.intVar(1, 5);
        model.element(x, values, y, 1).reify();
        eval(model, false);
    }

    @DataProvider(name = "table2")
    private Object[][] algo() {
        return new String[][]{
                {"AC2001"},
                {"FC"},
                {"AC3"},
                {"AC3rm"},
                {"AC3bit+rm"},
        };
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "table2")
    public void testTableF(String algo) {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(1, 5);
        Tuples tuples = new Tuples(true);
        tuples.add(2,2);
        tuples.add(3,5);
        tuples.add(4,5);
        model.table(x, y, tuples, algo).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "table2")
    public void testTableI(String algo) {
        Model model = new Model();
        IntVar x = model.intVar(2, 6);
        IntVar y = model.intVar(1, 5);
        Tuples tuples = new Tuples(false);
        tuples.add(2,2);
        tuples.add(3,5);
        tuples.add(4,5);
        model.table(x, y, tuples, algo).post();
        eval(model, false);
    }
}