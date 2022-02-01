/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sum;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.nary.clauses.ClauseStore;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.stream.IntStream;

import static org.chocosolver.solver.constraints.Explainer.execute;
import static org.chocosolver.solver.constraints.Explainer.fail;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 06/11/2018.
 */
public class ExplScalarTest {


    @Test(groups = "1s", timeOut = 60000)
    public void test1() throws ContradictionException {
        // 160.[0,1] + 1.[6,999] + (-1).[-999,2]<= 160
        Model model = new Model();
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        IntVar x2 = model.intVar("x2", -999, 999);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2}, new int[]{160, 1, -1}, 2, Operator.LE, 160);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x1.updateLowerBound(6, Cause.Null);
                    x2.updateUpperBound(2, Cause.Null);
                }, prop, x0);
        Assert.assertTrue(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 0);
        Assert.assertEquals(lits.get(x0), rng);
        rng.clear();
        rng.addBetween(-999, 2);
        Assert.assertEquals(lits.get(x1), rng);
        rng.clear();
        rng.addBetween(6, 999);
        Assert.assertEquals(lits.get(x2), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() throws ContradictionException {
        // 160.[0,1] + 1.[6,6] + (-1).[2,2]<= 160
        Model model = new Model();
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        IntVar x2 = model.intVar("x2", -999, 999);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2}, new int[]{160, 1, -1}, 2, Operator.LE, 160);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x1.instantiateTo(6, Cause.Null);
                    x2.instantiateTo(2, Cause.Null);
                }, prop, x0);
        Assert.assertTrue(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 0);
        Assert.assertEquals(lits.get(x0), rng);
        rng.clear();
        rng.addBetween(-999, 2);
        Assert.assertEquals(lits.get(x1), rng);
        rng.clear();
        rng.addBetween(6, 999);
        Assert.assertEquals(lits.get(x2), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3() throws ContradictionException {
        // 5.[2,7] + 1.[8,10] + 1.[-4,-1]<= 40
        Model model = new Model();
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        IntVar x2 = model.intVar("x2", -999, 999);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2}, new int[]{5, 1, 1}, 3, Operator.LE, 40);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x1.updateBounds(8, 10, Cause.Null);
                    x2.updateBounds(-4, -1, Cause.Null);
                }, prop, x0);
        Assert.assertTrue(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 7);
        Assert.assertEquals(lits.get(x0), rng);
        rng.clear();
        rng.addBetween(-999, 4);
        Assert.assertEquals(lits.get(x1), rng);
        rng.clear();
        rng.addBetween(-999, -8);
        Assert.assertEquals(lits.get(x2), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test4() throws ContradictionException {
        // 289.[1,1] + 1.[44,56] + (-1).[42,42]<= 291 && 1
        Model model = new Model();
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        IntVar x2 = model.intVar("x2", -999, 999);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2}, new int[]{289, 1, -1}, 2, Operator.LE, 291);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateBounds(1, 10, Cause.Null);
                    x2.updateBounds(36, 42, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 0);
        Assert.assertEquals(lits.get(x0), rng);
        rng.clear();
        rng.addBetween(-999, 44);
        Assert.assertEquals(lits.get(x1), rng);
        rng.clear();
        rng.addBetween(43, 999);
        Assert.assertEquals(lits.get(x2), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test5() throws ContradictionException {
        // 284.[1,1] + 1.[52,53] + (-1).[44,50]<= 291 && 2
        Model model = new Model();
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        IntVar x2 = model.intVar("x2", -999, 999);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2}, new int[]{284, 1, -1}, 2, Operator.LE, 291);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateBounds(1, 10, Cause.Null);
                    x2.updateBounds(52, 53, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 0);
        Assert.assertEquals(lits.get(x0), rng);
        rng.clear();
        rng.addBetween(-999, 60);
        Assert.assertEquals(lits.get(x1), rng);
        rng.clear();
        rng.addBetween(54, 999);
        Assert.assertEquals(lits.get(x2), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test6() throws ContradictionException {
        // 5.[0,0] + 1.[1,1] + 6.[1,1] + 4.[0,1] + 6.[1,1]<= 10
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 5);
        PropScalar prop = new PropScalar(bs, new int[]{5, 1, 6, 4, 6}, 5, Operator.LE, 10);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                fail(model.getSolver(), i -> {
                    bs[0].instantiateTo(0, Cause.Null);
                    bs[1].instantiateTo(1, Cause.Null);
                    bs[3].instantiateTo(1, Cause.Null);
                    bs[4].instantiateTo(1, Cause.Null);
                });
        Assert.assertFalse(lits.containsKey(bs[0]));
        Assert.assertTrue(lits.containsKey(bs[1]));
        Assert.assertFalse(lits.containsKey(bs[2]));
        Assert.assertTrue(lits.containsKey(bs[3]));
        Assert.assertTrue(lits.containsKey(bs[4]));
        IntIterableRangeSet rng = new IntIterableRangeSet(0);
        Assert.assertEquals(lits.get(bs[1]), rng);
        Assert.assertEquals(lits.get(bs[3]), rng);
        Assert.assertEquals(lits.get(bs[4]), rng);

    }

    @Test(groups = "1s", timeOut = 60000)
    public void test7() throws ContradictionException {
        // 5.[0,0] + 1.[1,1] + 6.[0,0] + 4.[0,1] + 6.[0,0]>= 10
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 5);
        PropScalar prop = new PropScalar(bs, new int[]{5, 1, 6, 4, 6}, 5, Operator.GE, 10);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                fail(model.getSolver(), i -> {
                    bs[0].instantiateTo(0, Cause.Null);
                    bs[1].instantiateTo(1, Cause.Null);
                    bs[2].instantiateTo(0, Cause.Null);
                    bs[4].instantiateTo(0, Cause.Null);
                });
        Assert.assertTrue(lits.containsKey(bs[0]));
        Assert.assertFalse(lits.containsKey(bs[1]));
        Assert.assertTrue(lits.containsKey(bs[2]));
        Assert.assertFalse(lits.containsKey(bs[3]));
        Assert.assertTrue(lits.containsKey(bs[4]));
        IntIterableRangeSet rng = new IntIterableRangeSet(1);
        Assert.assertEquals(lits.get(bs[0]), rng);
        Assert.assertEquals(lits.get(bs[2]), rng);
        Assert.assertEquals(lits.get(bs[4]), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test8() throws ContradictionException {
        // 165.[0,1] + 1.[2,2] + (-1).[1,1]>= 5
        Model model = new Model();
        IntVar x0 = model.intVar("x0", 0, 1);
        IntVar x1 = model.intVar("x1", -999, 999);
        IntVar x2 = model.intVar("x2", -999, 999);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2}, new int[]{165, 1, -1}, 2, Operator.GE, 5);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x1.updateBounds(2, 2, Cause.Null);
                    x2.updateBounds(1, 1, Cause.Null);
                }, prop, x0);
        Assert.assertTrue(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        IntIterableRangeSet rng = new IntIterableRangeSet(1);
        Assert.assertEquals(lits.get(x0), rng);
        rng.clear();
        rng.addBetween(6, 999);
        Assert.assertEquals(lits.get(x1), rng);
        rng.clear();
        rng.addBetween(-999, -3);
        Assert.assertEquals(lits.get(x2), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test9() throws ContradictionException {
        // 296.[0,0] + 1.[24,270] + (-1).[24,24]>= 5
        Model model = new Model();
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        IntVar x2 = model.intVar("x2", -999, 999);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2}, new int[]{296, 1, -1}, 2, Operator.GE, 5);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateBounds(0, 0, Cause.Null);
                    x2.updateBounds(24, 24, Cause.Null);
                }, prop, x1);
        Assert.assertTrue(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        IntIterableRangeSet rng = new IntIterableRangeSet(29, 999);
        Assert.assertEquals(lits.get(x1), rng);
        rng.clear();
        rng.addBetween(1, 999);
        Assert.assertEquals(lits.get(x0), rng);
        rng.clear();
        rng.addBetween(-999, 23);
        Assert.assertEquals(lits.get(x2), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test10() throws ContradictionException {
        // 166.[0,0] + 1.[63,65] + (-1).[60,68]>= 1 & 3
        Model model = new Model();
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        IntVar x2 = model.intVar("x2", -999, 999);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2}, new int[]{166, 1, -1}, 2, Operator.GE, 1);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x0.updateBounds(0, 0, Cause.Null);
                    x1.updateBounds(63, 65, Cause.Null);
                }, prop, x2);
        Assert.assertTrue(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        IntIterableRangeSet rng = new IntIterableRangeSet(-999, 64);
        Assert.assertEquals(lits.get(x2), rng);
        rng.clear();
        rng.addBetween(1, 999);
        Assert.assertEquals(lits.get(x0), rng);
        rng.clear();
        rng.addBetween(66, 999);
        Assert.assertEquals(lits.get(x1), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test11() throws ContradictionException {
        // 1.[0,1] + 2.[2,3] + 2.[3,4] = 20
        Model model = new Model();
        IntVar x0 = model.intVar("x0", -999, 999);
        IntVar x1 = model.intVar("x1", -999, 999);
        IntVar x2 = model.intVar("x2", -999, 999);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2}, new int[]{1, 2, 2}, 3, Operator.EQ, 20);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                execute(model.getSolver(), i -> {
                    x1.updateBounds(2, 3, Cause.Null);
                    x2.updateBounds(3, 4, Cause.Null);
                }, prop, x0);
        Assert.assertTrue(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        IntIterableRangeSet rng = new IntIterableRangeSet(6, 10);
        Assert.assertEquals(lits.get(x0), rng);
        rng.clear();
        rng.addBetween(-999, 999);
        rng.removeBetween(2, 3);
        Assert.assertEquals(lits.get(x1), rng);
        rng.clear();
        rng.addBetween(-999, 999);
        rng.removeBetween(3, 4);
        Assert.assertEquals(lits.get(x2), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test13() throws ContradictionException {
        // 3.[0,1] + 3.[0,1] + 3.[0,1] + 3.[0,1] <= 5
        Model model = new Model(Settings.init().explainGlobalFailureInSum(true));
        IntVar x0 = model.intVar("w", 0, 1);
        IntVar x1 = model.intVar("x", 0, 1);
        IntVar x2 = model.intVar("y", 0, 1);
        IntVar x3 = model.intVar("z", 0, 1);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2, x3}, new int[]{3, 3, 3, 3}, 4, Operator.LE, 5);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                fail(model.getSolver(), i -> {
                    x0.updateBounds(0, 0, Cause.Null);
                    x1.updateBounds(1, 1, Cause.Null);
                    x2.updateBounds(1, 1, Cause.Null);
                    x3.updateBounds(1, 1, Cause.Null);
                });
        Assert.assertFalse(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        Assert.assertTrue(lits.containsKey(x3));
        IntIterableRangeSet rng = new IntIterableRangeSet(0);
        Assert.assertEquals(lits.get(x1), rng);
        Assert.assertEquals(lits.get(x2), rng);
        Assert.assertEquals(lits.get(x3), rng);
        Assert.assertEquals(model.getNbCstrs(), 2);
        Assert.assertEquals(model.getCstrs()[1].getPropagator(0).getClass(), ClauseStore.class);
        Assert.assertEquals(((ClauseStore)model.getCstrs()[1].getPropagator(0)).getNbLearntClauses(), 4);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test14() throws ContradictionException {
        // 1.[0,1] + 2.[2,3] + 2.[3,4] <= 20
        Model model = new Model(Settings.init().explainGlobalFailureInSum(true));
        IntVar x0 = model.intVar("x0", 0, 19);
        IntVar x1 = model.intVar("x1", 0, 19);
        IntVar x2 = model.intVar("x2", 0, 19);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2}, new int[]{1, 2, 2}, 3, Operator.LE, 20);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                fail(model.getSolver(), i -> {
                    x0.updateBounds(10, 12, Cause.Null);
                    x1.updateBounds(4, 6, Cause.Null);
                    x2.updateBounds(3, 4, Cause.Null);
                });
        Assert.assertTrue(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        IntIterableRangeSet rng = new IntIterableRangeSet(0, 9);
        Assert.assertEquals(lits.get(x0), rng);
        rng.clear();
        rng.addBetween(0, 3);
        Assert.assertEquals(lits.get(x1), rng);
        rng.clear();
        rng.addBetween(0, 2);
        Assert.assertEquals(lits.get(x2), rng);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test15() throws ContradictionException {
        // 5.[7,8] + 1.[12,14] + 1.[-3,-1] <= 40
        Model model = new Model(Settings.init().explainGlobalFailureInSum(true));
        IntVar x0 = model.intVar("w", 0, 8);
        IntVar x1 = model.intVar("x", 0, 15);
        IntVar x2 = model.intVar("y", -15, 0);
        PropScalar prop = new PropScalar(new IntVar[]{x0, x1, x2}, new int[]{5, 1, 1}, 3, Operator.LE, 40);
        model.post(new Constraint("test", prop));
        HashMap<IntVar, IntIterableRangeSet> lits =
                fail(model.getSolver(), i -> {
                    x0.updateBounds(7, 8, Cause.Null);
                    x1.updateBounds(12, 14, Cause.Null);
                    x2.updateBounds(-3, -1, Cause.Null);
                });
        Assert.assertTrue(lits.containsKey(x0));
        Assert.assertTrue(lits.containsKey(x1));
        Assert.assertTrue(lits.containsKey(x2));
        IntIterableRangeSet rng = new IntIterableRangeSet(0, 6);
        Assert.assertEquals(lits.get(x0), rng);
        rng.clear();
        rng.addBetween(0, 11);
        Assert.assertEquals(lits.get(x1), rng);
        rng.clear();
        rng.addBetween(-15, -4);
        Assert.assertEquals(lits.get(x2), rng);
        Assert.assertEquals(model.getNbCstrs(), 2);
        Assert.assertEquals(model.getCstrs()[1].getPropagator(0).getClass(), ClauseStore.class);
        Assert.assertEquals(((ClauseStore)model.getCstrs()[1].getPropagator(0)).getNbLearntClauses(), 3);
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testScalarBoolEQP0() throws ContradictionException {
        for (int sca = 0; sca < 2; sca++) {
            int B = 169;
            int B1 = 71;
            int OU = 126;

            Model model = new Model();
            IntVar[] bs = model.boolVarArray("b", B);
            IntVar obj = model.intVar("OBJ", -999, 999);
            int[] cs = IntStream.range(0, B + 1).map(i -> i < B ? 1 : -1).toArray();

            PropSum prop;
            if (sca == 0) {
                prop = new PropSum(ArrayUtils.append(bs, new IntVar[]{obj}), B, Operator.EQ, 0);
            } else {
                prop = new PropScalar(ArrayUtils.append(bs, new IntVar[]{obj}), cs, B, Operator.EQ, 0);
            }
            model.post(new Constraint("test", prop));
            HashMap<IntVar, IntIterableRangeSet> lits =
                    execute(model.getSolver(), k -> {
                        for (int i = 0; i < B1; i++) {
                            bs[i].updateBounds(1, 1, Cause.Null);
                        }
                        for (int i = B1; i < B; i++) {
                            bs[i].updateBounds(0, 0, Cause.Null);
                        }
                        obj.updateBounds(B1, OU, Cause.Null);
                    }, prop, obj);
            Assert.assertTrue(lits.containsKey(obj));
            IntIterableRangeSet rng = new IntIterableRangeSet(B1, B1);
            Assert.assertEquals(lits.get(obj), rng);
            rng.clear();
            rng.add(0);
            for (int i = 0; i < B1; i++) {
                Assert.assertTrue(lits.containsKey(bs[i]));
                Assert.assertEquals(lits.get(bs[i]), rng);
            }
            rng.clear();
            rng.add(1);
            for (int i = B1; i < B; i++) {
                Assert.assertTrue(lits.containsKey(bs[i]));
                Assert.assertEquals(lits.get(bs[i]), rng);
            }
        }
    }
}