/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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
public class PropMaxTest {

    @Test(groups = "1s", timeOut = 60000)
    public void test1() throws ContradictionException {
        // [0,10] = max([0,6], [0,10])
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMax prop = new PropMax(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateUpperBound(6, Cause.Null);
                    x1.updateUpperBound(10, Cause.Null);
                }, prop, z);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 10);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(11, 999);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 600000)
    public void test1bis() throws ContradictionException {
        // [0,10] = max([0,6], [0,10])
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMax prop = new PropMax(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    z.removeValue(10, Cause.Null);
                    x0.updateUpperBound(6, Cause.Null);
                    x1.updateUpperBound(10, Cause.Null);
                }, prop, z);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 10);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(11, 999);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() throws ContradictionException {
        // [10,999] = max([10,999], [6,999])
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMax prop = new PropMax(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateLowerBound(6, Cause.Null);
                    x1.updateLowerBound(10, Cause.Null);
                }, prop, z);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(10, 999);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(-999, 9);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2bis() throws ContradictionException {
        // [10,999] = max([10,999], [6,999])
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMax prop = new PropMax(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    z.removeValue(10, Cause.Null);
                    x0.updateLowerBound(6, Cause.Null);
                    x1.updateLowerBound(10, Cause.Null);
                }, prop, z);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(10, 999);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(-999, 9);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3() throws ContradictionException {
        // [0,10] = max([-999,999], [0,8])
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMax prop = new PropMax(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateUpperBound(6, Cause.Null);
                    z.updateUpperBound(10, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertFalse(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(11, 999);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(-999, 10);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3bis() throws ContradictionException {
        // [0,10] = max([-999,999], [0,8])
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMax prop = new PropMax(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    z.updateUpperBound(10, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertFalse(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(11, 999);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(-999, 10);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3ter() throws ContradictionException {
        // [0,10] = max([-999,999], [0,8])
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMax prop = new PropMax(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x1.removeValue(10, Cause.Null);
                    x0.updateUpperBound(6, Cause.Null);
                    z.updateUpperBound(10, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertFalse(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(11, 999);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(-999, 10);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test4() throws ContradictionException {
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMax prop = new PropMax(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateUpperBound(6, Cause.Null);
                    z.updateLowerBound(10, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertFalse(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 9);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(10, 999);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test4bis() throws ContradictionException {
        Model model = new Model();
        IntVar z = model.intVar("z", -999, 999);
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        PropMax prop = new PropMax(new IntVar[]{x0, x1}, z);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x1.removeValue(10, Cause.Null);
                    x0.updateUpperBound(6, Cause.Null);
                    z.updateLowerBound(10, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(z));
        Assert.assertFalse(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 9);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(10, 999);
        Assert.assertEquals(lits.get(x1), rng);
    }

}