/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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
public class PropXeqYCReifTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testBpivot1() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXeqYCReif prop = new PropXeqYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    x.instantiateTo(7, Cause.Null);
                    y.instantiateTo(2, Cause.Null);
                }, prop, b);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(1);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(-999, 999);
        rng.remove(7);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 999);
        rng.remove(2);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBpivot2() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXeqYCReif prop = new PropXeqYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    x.instantiateTo(6, Cause.Null);
                    y.instantiateTo(3, Cause.Null);
                }, prop, b);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(0);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(-999, 5);
        rng.addBetween(7, 999);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 2);
        rng.addBetween(4, 999);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testXpivot1() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXeqYCReif prop = new PropXeqYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    b.instantiateTo(1, Cause.Null);
                    y.instantiateTo(2, Cause.Null);
                }, prop, x);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(0);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.add(7);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 999);
        rng.remove(2);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testXpivot2() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXeqYCReif prop = new PropXeqYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    b.instantiateTo(0, Cause.Null);
                    y.instantiateTo(3, Cause.Null);
                }, prop, x);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(1);
        Assert.assertEquals(lits.get(b), rng);
        rng.addBetween(-999, 999);
        rng.remove(8);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 999);
        rng.remove(3);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testYpivot1() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXeqYCReif prop = new PropXeqYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    b.instantiateTo(1, Cause.Null);
                    x.instantiateTo(7, Cause.Null);
                }, prop, y);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(0);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(-999, 999);
        rng.remove(7);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.add(2);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testYpivot2() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        BoolVar b = mo.boolVar("b");
        PropXeqYCReif prop = new PropXeqYCReif(x, y, 5, b);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i -> {
                    b.instantiateTo(0, Cause.Null);
                    x.instantiateTo(6, Cause.Null);
                }, prop, y);
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));

        IntIterableRangeSet rng = new IntIterableRangeSet(1);
        Assert.assertEquals(lits.get(b), rng);
        rng.addBetween(-999, 999);
        rng.remove(6);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 999);
        rng.remove(1);
        Assert.assertEquals(lits.get(y), rng);
    }
}