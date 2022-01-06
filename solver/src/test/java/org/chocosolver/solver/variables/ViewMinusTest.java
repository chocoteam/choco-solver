/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public class ViewMinusTest {


    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Model model = new Model();

        IntVar X = model.intVar("X", 1, 10, false);
        IntVar Y = model.intMinusView(X);

        try {
			if(!model.getSettings().enableViews())
				model.getSolver().propagate();
            Assert.assertFalse(Y.isInstantiated());
            Assert.assertEquals(Y.getLB(), -10);
            Assert.assertEquals(Y.getUB(), -1);
            Assert.assertTrue(Y.contains(-5));
            Assert.assertEquals(Y.nextValue(-11), -10);
            Assert.assertEquals(Y.nextValue(-5), -4);
            Assert.assertEquals(Y.nextValue(-1), Integer.MAX_VALUE);
            Assert.assertEquals(Y.previousValue(0), -1);
            Assert.assertEquals(Y.previousValue(-4), -5);
            Assert.assertEquals(Y.previousValue(-10), Integer.MIN_VALUE);

            Y.updateLowerBound(-9, Cause.Null);
			if(!model.getSettings().enableViews())
				model.getSolver().propagate();
            Assert.assertEquals(Y.getLB(), -9);
            Assert.assertEquals(X.getUB(), 9);

            Y.updateUpperBound(-2, Cause.Null);
			if(!model.getSettings().enableViews())
				model.getSolver().propagate();
            Assert.assertEquals(Y.getUB(), -2);
            Assert.assertEquals(X.getLB(), 2);

            Y.removeValue(-4, Cause.Null);
			if(!model.getSettings().enableViews())
				model.getSolver().propagate();
            Assert.assertFalse(Y.contains(-4));
            Assert.assertFalse(X.contains(4));

            Y.removeInterval(-8, -6, Cause.Null);
			if(!model.getSettings().enableViews())
				model.getSolver().propagate();
            Assert.assertFalse(Y.contains(-8));
            Assert.assertFalse(Y.contains(-7));
            Assert.assertFalse(Y.contains(-6));
            Assert.assertFalse(X.contains(6));
            Assert.assertFalse(X.contains(7));
            Assert.assertFalse(X.contains(8));

            Assert.assertEquals(X.getDomainSize(), 4);
            Assert.assertEquals(Y.getDomainSize(), 4);

            Y.instantiateTo(-5, Cause.Null);
			if(!model.getSettings().enableViews())
				model.getSolver().propagate();
            Assert.assertTrue(X.isInstantiated());
            Assert.assertTrue(Y.isInstantiated());
            Assert.assertEquals(X.getValue(), 5);
            Assert.assertEquals(Y.getValue(), -5);

        } catch (ContradictionException ignored) {

        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test2() {
        Random random = new Random();
        for (int seed = 0; seed < 2000; seed++) {
            random.setSeed(seed);
            Model ref = new Model();
            {
                IntVar[] xs = new IntVar[2];
                xs[0] = ref.intVar("x", 1, 15, true);
                xs[1] = ref.intVar("y", -15, -1, true);
                ref.sum(xs, "=", 0).post();
                ref.getSolver().setSearch(randomSearch(xs, seed));
            }
            Model model = new Model();
            {
                IntVar[] xs = new IntVar[2];
                xs[0] = model.intVar("x", 1, 15, true);
                xs[1] = model.intMinusView(xs[0]);
                model.sum(xs, "=", 0).post();
                model.getSolver().setSearch(randomSearch(xs, seed));
            }
            while (ref.getSolver().solve()) ;
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), ref.getSolver().getSolutionCount());

        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test3() {
        Random random = new Random();
        for (int seed = 0; seed < 2000; seed++) {
            random.setSeed(seed);
            Model ref = new Model();
            {
                IntVar[] xs = new IntVar[2];
                xs[0] = ref.intVar("x", 1, 15, false);
                xs[1] = ref.intVar("y", -15, -1, false);
                ref.sum(xs, "=", 0).post();
                ref.getSolver().setSearch(randomSearch(xs, seed));
            }
            Model model = new Model();
            {
                IntVar[] xs = new IntVar[2];
                xs[0] = model.intVar("x", 1, 15, false);
                xs[1] = model.intMinusView(xs[0]);
                model.sum(xs, "=", 0).post();
                model.getSolver().setSearch(randomSearch(xs, seed));
            }
            while (ref.getSolver().solve()) ;
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), ref.getSolver().getSolutionCount());

        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testIt1() {
        Random random = new Random();
        for (int seed = 0; seed < 200; seed++) {
            random.setSeed(seed);
            Model model = new Model();
            int[][] domains = DomainBuilder.buildFullDomains(1, -5, 5, random, random.nextDouble(), random.nextBoolean());
            IntVar o = model.intVar("o", domains[0][0], domains[0][domains[0].length - 1], true);
            IntVar v = model.intMinusView(o);
            DisposableValueIterator vit = v.getValueIterator(true);
            while (vit.hasNext()) {
                Assert.assertTrue(o.contains(-vit.next()));
            }
            vit.dispose();
            vit = v.getValueIterator(false);
            while (vit.hasPrevious()) {
                Assert.assertTrue(o.contains(-vit.previous()));
            }
            vit.dispose();
            DisposableRangeIterator rit = v.getRangeIterator(true);
            while (rit.hasNext()) {
                Assert.assertTrue(o.contains(-rit.min()));
                Assert.assertTrue(o.contains(-rit.max()));
                rit.next();
            }
            rit = v.getRangeIterator(false);
            while (rit.hasPrevious()) {
                Assert.assertTrue(o.contains(-rit.min()));
                Assert.assertTrue(o.contains(-rit.max()));
                rit.previous();
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testIt2() {
        Random random = new Random();
        for (int seed = 0; seed < 200; seed++) {
            random.setSeed(seed);
            Model model = new Model();
            int[][] domains = DomainBuilder.buildFullDomains(1, -5, 5, random, random.nextDouble(), random.nextBoolean());
            IntVar o = model.intVar("o", domains[0]);
            IntVar v = model.intMinusView(o);
			if(!model.getSettings().enableViews()){
				try {
					model.getSolver().propagate();
				}catch (Exception e){
					e.printStackTrace();
					throw new UnsupportedOperationException();
				}
			}
            DisposableValueIterator vit = v.getValueIterator(true);
            while (vit.hasNext()) {
                Assert.assertTrue(o.contains(-vit.next()));
            }
            vit.dispose();
            vit = v.getValueIterator(false);
            while (vit.hasPrevious()) {
                Assert.assertTrue(o.contains(-vit.previous()));
            }
            vit.dispose();
            DisposableRangeIterator rit = v.getRangeIterator(true);
            while (rit.hasNext()) {
                Assert.assertTrue(o.contains(-rit.min()));
                Assert.assertTrue(o.contains(-rit.max()));
                rit.next();
            }
            rit = v.getRangeIterator(false);
            while (rit.hasPrevious()) {
                Assert.assertTrue(o.contains(-rit.min()));
                Assert.assertTrue(o.contains(-rit.max()));
                rit.previous();
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testB1() throws ContradictionException {
        Model model = new Model();
        IntVar v_0 = model.intVar("v_0",new int[]{-3,-2,-1,0,3,4});
        IntVar v_1 = model.intVar("v_1",-4);
        IntVar v_2 = model.intVar("v_2",new int[]{-3,-2,-1,0,4});
        model.times(v_0, v_1, v_2).post();
        System.out.println(model);
        model.getSolver().propagate();
        System.out.println(model);
        while(model.getSolver().solve());
        Assert.assertEquals(model.getSolver().getSolutionCount(), 2);
    }
}
