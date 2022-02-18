/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.chocosolver.solver.constraints.Explainer.execute;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 06/11/2018.
 */
public class PropXltYCReifTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testBpivot1a() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXltYCReif prop = new PropXltYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    x.updateUpperBound(7, Cause.Null);
                    y.updateLowerBound(4, Cause.Null);
                }, prop, b);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(1);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(8, 999);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 2);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBpivot1b() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXltYCReif prop = new PropXltYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    y.updateLowerBound(4, Cause.Null);
                    x.updateUpperBound(7, Cause.Null);
                }, prop, b);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(1);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(9, 999);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 3);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBpivot2a() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXltYCReif prop = new PropXltYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    x.updateLowerBound(10, Cause.Null);
                    y.updateUpperBound(3, Cause.Null);
                }, prop, b);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(0);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(-999, 9);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(6, 999);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBpivot2b() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXltYCReif prop = new PropXltYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    y.updateUpperBound(3, Cause.Null);
                    x.updateLowerBound(10, Cause.Null);
                }, prop, b);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(0);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(-999, 7);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(4, 999);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testXpivot1() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXltYCReif prop = new PropXltYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    b.instantiateTo(1, Cause.Null);
                    y.updateUpperBound(2, Cause.Null);
                }, prop, x);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(0);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(-999, 6);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(3, 999);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testXpivot2() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXltYCReif prop = new PropXltYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    b.instantiateTo(0, Cause.Null);
                    y.updateLowerBound(2, Cause.Null);
                }, prop, x);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(1);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(7, 999);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 1);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testYpivot1() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXltYCReif prop = new PropXltYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    b.instantiateTo(1, Cause.Null);
                    x.updateLowerBound(7, Cause.Null);
                }, prop, y);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(0);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(-999, 6);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(3, 999);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testYpivot2() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXltYCReif prop = new PropXltYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    b.instantiateTo(0, Cause.Null);
                    x.updateUpperBound(6, Cause.Null);
                }, prop, y);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(1);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(7, 999);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 1);
        Assert.assertEquals(lits.get(y), rng);
    }

}