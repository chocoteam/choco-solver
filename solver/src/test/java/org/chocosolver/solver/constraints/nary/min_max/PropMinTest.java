/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.min_max;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
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
 * @since 29/11/2018.
 */
public class PropMinTest {

    @Test(groups = "1s", timeOut = 60000)
    public void test1() throws ContradictionException {
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMin prop = new PropMin(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateUpperBound(6, Cause.Null);
                    x1.updateUpperBound(10, Cause.Null);
                }, prop, z);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertTrue(lits.containsKey(x0));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 6);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(7, 999);
        Assert.assertEquals(lits.get(x0), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test1bis() throws ContradictionException {
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMin prop = new PropMin(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    z.removeValue(6, Cause.Null);
                    x0.updateUpperBound(6, Cause.Null);
                    x1.updateUpperBound(10, Cause.Null);
                }, prop, z);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertTrue(lits.containsKey(x0));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 6);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(7, 999);
        Assert.assertEquals(lits.get(x0), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() throws ContradictionException {
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMin prop = new PropMin(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateLowerBound(6, Cause.Null);
                    x1.updateLowerBound(10, Cause.Null);
                }, prop, z);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertTrue(lits.containsKey(x0));
        IntIterableRangeSet rng = new IntIterableRangeSet(6, 999);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(-999, 5);
        Assert.assertEquals(lits.get(x0), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2bis() throws ContradictionException {
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMin prop = new PropMin(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    z.removeValue(6, Cause.Null);
                    x0.updateLowerBound(6, Cause.Null);
                    x1.updateLowerBound(10, Cause.Null);
                }, prop, z);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertTrue(lits.containsKey(x0));
        IntIterableRangeSet rng = new IntIterableRangeSet(6, 999);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(-999, 5);
        Assert.assertEquals(lits.get(x0), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3() throws ContradictionException {
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMin prop = new PropMin(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateLowerBound(10, Cause.Null);
                    z.updateLowerBound(6, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertFalse(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 5);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(6,999);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3bis() throws ContradictionException {
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMin prop = new PropMin(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    z.updateLowerBound(6, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertFalse(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 5);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(6,999);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3ter() throws ContradictionException {
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMin prop = new PropMin(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x1.removeValue(6, Cause.Null);
                    x0.updateLowerBound(10, Cause.Null);
                    z.updateLowerBound(6, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertFalse(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 5);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(6,999);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test4() throws ContradictionException {
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMin prop = new PropMin(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateLowerBound(10, Cause.Null);
                    z.updateUpperBound(6, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertFalse(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(7, 999);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(-999, 6);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test4bis() throws ContradictionException {
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMin prop = new PropMin(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x1.removeValue(6, Cause.Null);
                    x0.updateLowerBound(10, Cause.Null);
                    z.updateUpperBound(6, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertFalse(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(7, 999);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(-999, 6);
        Assert.assertEquals(lits.get(x1), rng);
    }



}