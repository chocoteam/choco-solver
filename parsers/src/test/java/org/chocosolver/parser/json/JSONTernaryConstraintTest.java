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
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

/**
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 12/09/2017.
 */
public class JSONTernaryConstraintTest extends JSONConstraintTest{

    @Test(groups = "1s", timeOut = 60000)
    public void testTimes() {
        Model model = new Model();
        IntVar x = model.intVar(2, 100);
        IntVar y = model.intVar(3, 200);
        IntVar z = model.intVar(4, 20);
        model.times(x, y, z).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testTimesReif() {
        Model model = new Model();
        IntVar x = model.intVar(2, 100);
        IntVar y = model.intVar(3, 200);
        IntVar z = model.intVar(4, 20);
        model.times(x, y, z).reify();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDiv() {
        Model model = new Model();
        IntVar x = model.intVar(-50, 50);
        IntVar y = model.intVar(-100, 100);
        IntVar z = model.intVar(-10, 10);
        model.div(x, y, z).post();
        eval(model, true);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDivReif() {
        Model model = new Model();
        IntVar x = model.intVar(-50, 50);
        IntVar y = model.intVar(-100, 100);
        IntVar z = model.intVar(-10, 10);
        model.div(x, y, z).reify();
        eval(model, true);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMin() {
        Model model = new Model();
        IntVar x = model.intVar(2, 10);
        IntVar y = model.intVar(3, 20);
        IntVar z = model.intVar(4, 16);
        model.min(x, y, z).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMinReif() {
        Model model = new Model();
        IntVar x = model.intVar(2, 10);
        IntVar y = model.intVar(3, 20);
        IntVar z = model.intVar(4, 16);
        model.min(x, y, z).reify();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMax() {
        Model model = new Model();
        IntVar x = model.intVar(2, 10);
        IntVar y = model.intVar(3, 20);
        IntVar z = model.intVar(4, 16);
        model.max(x, y, z).post();
        eval(model, false);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMaxReif() {
        Model model = new Model();
        IntVar x = model.intVar(2, 10);
        IntVar y = model.intVar(3, 20);
        IntVar z = model.intVar(4, 16);
        model.max(x, y, z).reify();
        eval(model, false);
    }
}