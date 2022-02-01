/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.XParameters;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
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
 * @since 07/11/2018.
 */
public class ExplAllDiffInstTest {

    @Test(groups = "1s", timeOut = 60000)
    public void test1() throws ContradictionException {
        Model mo = new Model();
        IntVar a = mo.intVar("a", 0, 6);
        IntVar b = mo.intVar("b", 0, 6);
        IntVar c = mo.intVar("c", 0, 6);
        IntVar d = mo.intVar("d", 0, 6);
        PropAllDiffInst prop = new PropAllDiffInst(new IntVar[]{a, b, c, d});
        mo.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(),
                        i -> {
                            b.updateBounds(0, 1, Cause.Null);
                            c.updateBounds(4, 4, Cause.Null);
                            d.updateBounds(5, 6, Cause.Null);
                        }, prop, a);
        Assert.assertTrue(lits.containsKey(a));
        Assert.assertFalse(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(c));
        Assert.assertFalse(lits.containsKey(d));
        IntIterableRangeSet rng = new IntIterableRangeSet(0,6);
        rng.remove(4);
        Assert.assertEquals(lits.get(a), rng);
        Assert.assertEquals(lits.get(c), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() throws ContradictionException {
        Model mo = new Model();
        IntVar a = mo.intVar("a", 2, 4);
        IntVar b = mo.intVar("b", 2, 4);
        IntVar c = mo.intVar("c", 2, 4);
        IntVar d = mo.intVar("d", 2, 4);
        PropAllDiffInst prop = new PropAllDiffInst(new IntVar[]{a, b, c});
        mo.post(new Constraint("test", prop));
        XParameters.DEFAULT_X = false;
        IntDecision dec = new IntDecision(null);
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(mo.getSolver(),
                        i -> {
                            mo.getSolver().getEventObserver().pushDecisionLevel();
                            d.instantiateTo(2, dec);
                            b.updateBounds(3, 4, Cause.Null);
                            c.updateBounds(4, 4, Cause.Null);
                        }, prop, a);
        Assert.assertTrue(lits.containsKey(a));
        Assert.assertTrue(lits.containsKey(b));
        Assert.assertTrue(lits.containsKey(c));
        IntIterableRangeSet rng = new IntIterableRangeSet(2,2);
        Assert.assertEquals(lits.get(a), rng);
        Assert.assertEquals(lits.get(b), rng);
        rng.clear();
        rng.addBetween(2,3);
        Assert.assertEquals(lits.get(c), rng);
    }


}