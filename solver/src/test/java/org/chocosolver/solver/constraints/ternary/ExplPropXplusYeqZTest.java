/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

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
 * @since 13/11/2018.
 */
public class ExplPropXplusYeqZTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testXpivot1() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        IntVar z = mo.intVar("z", -999, 999);

        PropXplusYeqZ prop = new PropXplusYeqZ(x, y, z);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i ->
                {
                    y.updateLowerBound(5,Cause.Null);
                    z.updateUpperBound(8, Cause.Null);
                }, prop, x);
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));
        Assert.assertTrue(lits.containsKey(z));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999,3);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 4);
        Assert.assertEquals(lits.get(y), rng);
        rng.clear();
        rng.addBetween(9, 999);
        Assert.assertEquals(lits.get(z), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testXpivot2() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        IntVar z = mo.intVar("z", -999, 999);

        PropXplusYeqZ prop = new PropXplusYeqZ(x, y, z);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i ->
                {
                    y.updateUpperBound(5,Cause.Null);
                    z.updateLowerBound(8, Cause.Null);
                }, prop, x);
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));
        Assert.assertTrue(lits.containsKey(z));
        IntIterableRangeSet rng = new IntIterableRangeSet(3,999);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(6, 999);
        Assert.assertEquals(lits.get(y), rng);
        rng.clear();
        rng.addBetween(-999, 7);
        Assert.assertEquals(lits.get(z), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testYpivot1() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        IntVar z = mo.intVar("z", -999, 999);

        PropXplusYeqZ prop = new PropXplusYeqZ(x, y, z);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i ->
                {
                    x.updateLowerBound(5,Cause.Null);
                    z.updateUpperBound(8, Cause.Null);
                }, prop, y);
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));
        Assert.assertTrue(lits.containsKey(z));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999,3);
        Assert.assertEquals(lits.get(y), rng);
        rng.clear();
        rng.addBetween(-999, 4);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(9, 999);
        Assert.assertEquals(lits.get(z), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testYpivot2() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        IntVar z = mo.intVar("z", -999, 999);

        PropXplusYeqZ prop = new PropXplusYeqZ(x, y, z);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i ->
                {
                    x.updateUpperBound(5,Cause.Null);
                    z.updateLowerBound(8, Cause.Null);
                }, prop, y);
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));
        Assert.assertTrue(lits.containsKey(z));
        IntIterableRangeSet rng = new IntIterableRangeSet(3,999);
        Assert.assertEquals(lits.get(y), rng);
        rng.clear();
        rng.addBetween(6, 999);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 7);
        Assert.assertEquals(lits.get(z), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testZpivot1() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        IntVar z = mo.intVar("z", -999, 999);

        PropXplusYeqZ prop = new PropXplusYeqZ(x, y, z);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i ->
                {
                    x.updateLowerBound(5,Cause.Null);
                    y.updateLowerBound(6,Cause.Null);
                }, prop, z);
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));
        Assert.assertTrue(lits.containsKey(z));
        IntIterableRangeSet rng = new IntIterableRangeSet(11,999);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(-999, 4);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(-999, 5);
        Assert.assertEquals(lits.get(y), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testZpivot2() throws ContradictionException {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999, 999);
        IntVar y = mo.intVar("y", -999, 999);
        IntVar z = mo.intVar("z", -999, 999);

        PropXplusYeqZ prop = new PropXplusYeqZ(x, y, z);
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(), i ->
                {
                    x.updateUpperBound(5,Cause.Null);
                    y.updateUpperBound(4,Cause.Null);
                }, prop, z);
        Assert.assertTrue(lits.containsKey(x));
        Assert.assertTrue(lits.containsKey(y));
        Assert.assertTrue(lits.containsKey(z));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999,9);
        Assert.assertEquals(lits.get(z), rng);
        rng.clear();
        rng.addBetween(6, 999);
        Assert.assertEquals(lits.get(x), rng);
        rng.clear();
        rng.addBetween(5, 999);
        Assert.assertEquals(lits.get(y), rng);
    }

}