/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.fast;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.impl.BitsetArrayIntVarImpl;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.chocosolver.util.procedure.IntProcedure;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/02/13
 */
public class BitsetArrayIntVarImplTest {

    BitsetArrayIntVarImpl var;

    public void setUp() throws Exception {
        var = new BitsetArrayIntVarImpl("test", new int[]{-5, 0, 3, 4, 5}, new Model());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemoveValue() throws Exception {
        setUp();
        Assert.assertFalse(var.removeValue(7, Cause.Null));
        Assert.assertTrue(var.removeValue(0, Cause.Null));
        Assert.assertFalse(var.contains(0));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemoveInterval() throws Exception {
        setUp();
        Assert.assertFalse(var.removeInterval(7, 8, Cause.Null));
        Assert.assertTrue(var.removeInterval(0, 3, Cause.Null));
        Assert.assertFalse(var.contains(0));
        Assert.assertFalse(var.contains(3));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUpdateLowerBound() throws Exception {
        setUp();
        Assert.assertFalse(var.updateLowerBound(-6, Cause.Null));
        Assert.assertTrue(var.updateLowerBound(0, Cause.Null));
        Assert.assertTrue(var.contains(0));
        Assert.assertFalse(var.contains(-5));

    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUpdateUpperBound() throws Exception {
        setUp();
        Assert.assertFalse(var.updateUpperBound(6, Cause.Null));
        Assert.assertTrue(var.updateUpperBound(0, Cause.Null));
        Assert.assertTrue(var.contains(0));
        Assert.assertFalse(var.contains(5));
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testGetLB() throws Exception {
        setUp();
        Assert.assertEquals(-5, var.getLB());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testGetUB() throws Exception {
        setUp();
        Assert.assertEquals(5, var.getUB());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testGetDomainSize() throws Exception {
        setUp();
        Assert.assertEquals(5, var.getDomainSize());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNextValue() throws Exception {
        setUp();
        Assert.assertEquals(-5, var.nextValue(-6));
        Assert.assertEquals(0, var.nextValue(-5));
        Assert.assertEquals(Integer.MAX_VALUE, var.nextValue(5));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPreviousValue() throws Exception {
        setUp();
        Assert.assertEquals(5, var.previousValue(6));
        Assert.assertEquals(0, var.previousValue(3));
        Assert.assertEquals(Integer.MIN_VALUE, var.previousValue(-5));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testHasEnumeratedDomain() throws Exception {
        setUp();
        Assert.assertTrue(var.hasEnumeratedDomain());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testGetTypeAndKind() throws Exception {
        setUp();
        Assert.assertTrue((Variable.INT & var.getTypeAndKind()) != 0);
        Assert.assertTrue((Variable.VAR & var.getTypeAndKind()) != 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testGetValueIterator() throws Exception {
        setUp();
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-5, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(0, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(3, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(5, vit.next());
        Assert.assertFalse(vit.hasNext());
        vit.dispose();

        vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(5, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(3, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(0, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-5, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
        vit.dispose();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testGetRangeIterator() throws Exception {
        setUp();
        DisposableRangeIterator rit = var.getRangeIterator(true);
        Assert.assertTrue(rit.hasNext());
        Assert.assertEquals(-5, rit.min());
        Assert.assertEquals(-5, rit.max());
        rit.next();
        Assert.assertTrue(rit.hasNext());
        Assert.assertEquals(0, rit.min());
        Assert.assertEquals(0, rit.max());
        rit.next();
        Assert.assertTrue(rit.hasNext());
        Assert.assertEquals(3, rit.min());
        Assert.assertEquals(5, rit.max());
        rit.next();
        Assert.assertFalse(rit.hasNext());

        rit = var.getRangeIterator(false);
        Assert.assertTrue(rit.hasPrevious());
        Assert.assertEquals(3, rit.min());
        Assert.assertEquals(5, rit.max());
        rit.previous();
        Assert.assertTrue(rit.hasPrevious());
        Assert.assertEquals(0, rit.min());
        Assert.assertEquals(0, rit.max());
        rit.previous();
        Assert.assertTrue(rit.hasPrevious());
        Assert.assertEquals(-5, rit.min());
        Assert.assertEquals(-5, rit.max());
        rit.previous();
        Assert.assertFalse(rit.hasPrevious());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals0() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals1() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -1, 1, 2, 4);
        x.removeValues(rems, Cause.Null);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals11() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(2, 5, 8, 9);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertTrue(x.isInstantiatedTo(6));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals12() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(2, 5, 6, 8);
        x.removeValues(rems, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(9));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals13() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(2, 6, 9);
        x.removeValues(rems, Cause.Null);
        Assert.assertTrue(x.getDomainSize() == 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals14() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(5, 6, 8, 9);
        x.removeValues(rems, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(2));
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testRemVals15() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(1, 2, 5, 6, 8, 9);
        x.removeValues(rems, Cause.Null);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals21() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -2);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals22() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-4);
        rems.add(-4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals3() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(1, 2, 3);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals31() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 3);
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals41() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 0, 1);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals42() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(0);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testRemVals5() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-1);
        rems.addAll(-1, 0, 1);
        x.removeValues(rems, Cause.Null);
        Assert.fail();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals6() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(0, 1, 2);
        x.removeValues(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals7() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 1, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(1, 2, 3);
        x.removeValues(rems, Cause.Null);
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testRemVals81() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 1, 3}, model);
        x.removeValue(1, Cause.Null);
        x.instantiateTo(1, Cause.Null);
        Assert.assertFalse(x.isInstantiated());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemVals82() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 1, 3}, model);
        x.removeInterval(1, 2, Cause.Null);
        x.removeInterval(1, 2, Cause.Null);
        Assert.assertEquals(x.getUB(), 3);
        Assert.assertEquals(x.getLB(), 0);
        Assert.assertEquals(x.getDomainSize(), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUpdBounds1() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        x.updateBounds(-2, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), -2);
        Assert.assertEquals(x.getUB(), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUpdBounds2() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        x.updateBounds(-2, 4, Cause.Null);
        Assert.assertEquals(x.getLB(), -2);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUpdBounds3() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        x.updateBounds(-4, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), -3);
        Assert.assertEquals(x.getUB(), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUpdBounds4() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        x.updateBounds(0, 0, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(0));
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testUpdBounds5() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        x.updateBounds(4, -2, Cause.Null);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUpdBounds6() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 0, 3}, model);
        x.updateBounds(-2, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), 0);
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUpdBounds7() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 0, 3}, model);
        x.updateBounds(-2, 4, Cause.Null);
        Assert.assertEquals(x.getLB(), 0);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUpdBounds8() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 0, 3}, model);
        x.updateBounds(-4, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), -3);
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUpdBounds9() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 0, 3}, model);
        x.updateBounds(0, 0, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(0));
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testUpdBounds10() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 0, 3}, model);
        x.updateBounds(4, -2, Cause.Null);
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testUpdBounds11() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, model);
        x.updateBounds(3, 4, Cause.Null);
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut0() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        Assert.assertFalse(x.removeAllValuesBut(rems, Cause.Null));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut1() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -1, 1, 2, 4);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getDomainSize(), 4);
        Assert.assertTrue(x.contains(-3));
        Assert.assertTrue(x.contains(-1));
        Assert.assertTrue(x.contains(1));
        Assert.assertTrue(x.contains(2));
        Assert.assertFalse(x.contains(-2));
        Assert.assertFalse(x.contains(0));
        Assert.assertFalse(x.contains(3));
        Assert.assertFalse(x.contains(4));

    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut11() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(1, 6, 10, 11);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertTrue(x.isInstantiatedTo(6));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut12() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(9, 10, 11);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(9));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut13() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(2, 6, 9);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getDomainSize(), 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut14() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(2);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(2));
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut15() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(1, 3, 4);
        x.removeAllValuesBut(rems, Cause.Null);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut21() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 0, 1);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut22() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-4);
        rems.addAll(-3, -2, -1, 0, 1, 2, 3);
        Assert.assertFalse(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -3);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut3() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(1, 2, 3);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), 1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut41() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -2, 2, 3);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut42() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, 3);
        Assert.assertFalse(x.removeAllValuesBut(rems, Cause.Null));
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut5() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-1);
        rems.addAll(2, 3);
        x.removeAllValuesBut(rems, Cause.Null);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut6() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 2, 3}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(1, 2, 4);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), 2);
        Assert.assertEquals(x.getUB(), 2);
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut7() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-27, -25, -20}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-29);
        rems.addAll(-29, -28, -26, -22, -21);
        x.removeAllValuesBut(rems, Cause.Null);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut8() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-27, -25, -21}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-29);
        rems.addAll(-29, -28, -26, -22, -21);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), -21);
        Assert.assertEquals(x.getUB(), -21);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemValsBut9() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-29, -25, -21}, model);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-29);
        rems.addAll(-29, -28, -26, -22, -20);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), -29);
        Assert.assertEquals(x.getUB(), -29);
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testJL01() throws ContradictionException {
        Model s = new Model();
        IntVar i = s.intVar("i", new int[]{0, 98, 99});
        IIntDeltaMonitor d = i.monitorDelta(Cause.Null);
        d.startMonitoring();
        i.updateUpperBound(98, Cause.Null);
        TIntArrayList remvals = new TIntArrayList(1);
        d.forEachRemVal((IntProcedure) remvals::add);
        Assert.assertEquals(remvals.size(), 1);
        Assert.assertEquals(remvals.get(0), 99);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testJL02() throws ContradictionException {
        Model s = new Model();
        IntVar i = s.intVar("i", new int[]{0, 98, 99});
        IIntDeltaMonitor d = i.monitorDelta(Cause.Null);
        d.startMonitoring();
        i.updateBounds(0, 98, Cause.Null);
        TIntArrayList remvals = new TIntArrayList(1);
        d.forEachRemVal((IntProcedure) remvals::add);
        Assert.assertEquals(remvals.size(), 1);
        Assert.assertEquals(remvals.get(0), 99);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testJL03() throws ContradictionException {
        Model s = new Model();
        IntVar i = s.intVar("i", new int[]{2, 3, 99});
        IIntDeltaMonitor d = i.monitorDelta(Cause.Null);
        d.startMonitoring();
        i.updateLowerBound(3, Cause.Null);
        TIntArrayList remvals = new TIntArrayList(1);
        d.forEachRemVal((IntProcedure) remvals::add);
        Assert.assertEquals(remvals.size(), 1);
        Assert.assertEquals(remvals.get(0), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testJL04() throws ContradictionException {
        Model s = new Model();
        IntVar i = s.intVar("i", new int[]{2, 3, 99});
        IIntDeltaMonitor d = i.monitorDelta(Cause.Null);
        d.startMonitoring();
        i.updateBounds(3, 99, Cause.Null);
        TIntArrayList remvals = new TIntArrayList(1);
        d.forEachRemVal((IntProcedure) remvals::add);
        Assert.assertEquals(remvals.size(), 1);
        Assert.assertEquals(remvals.get(0), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNextOut1() {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, model);
        Assert.assertEquals(x.nextValueOut(-4), -3);
        Assert.assertEquals(x.nextValueOut(-3), -2);
        Assert.assertEquals(x.nextValueOut(-2), 2);
        Assert.assertEquals(x.nextValueOut(2), 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNextOut2() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, model);
        x.instantiateTo(-1, Cause.Null);
        Assert.assertEquals(x.nextValueOut(-4), -3);
        Assert.assertEquals(x.nextValueOut(-3), -2);
        Assert.assertEquals(x.nextValueOut(-2), 0);
        Assert.assertEquals(x.nextValueOut(0), 1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNextOut3() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-2, -1, 1, 2, 5, 6, 7}, model);
        Assert.assertEquals(x.nextValueOut(-4), -3);
        Assert.assertEquals(x.nextValueOut(-3), 0);
        Assert.assertEquals(x.nextValueOut(0), 3);
        Assert.assertEquals(x.nextValueOut(3), 4);
        Assert.assertEquals(x.nextValueOut(4), 8);
        Assert.assertEquals(x.nextValueOut(8), 9);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPreviousOut1() {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, model);
        Assert.assertEquals(x.previousValueOut(4), 3);
        Assert.assertEquals(x.previousValueOut(3), 2);
        Assert.assertEquals(x.previousValueOut(2), -2);
        Assert.assertEquals(x.previousValueOut(-2), -3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPreviousOut2() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, model);
        x.instantiateTo(1, Cause.Null);
        Assert.assertEquals(x.previousValueOut(4), 3);
        Assert.assertEquals(x.previousValueOut(3), 2);
        Assert.assertEquals(x.previousValueOut(2), 0);
        Assert.assertEquals(x.previousValueOut(0), -1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPreviousOut3() throws ContradictionException {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-2, -1, 1, 2, 5, 6, 7}, model);
        Assert.assertEquals(x.previousValueOut(9), 8);
        Assert.assertEquals(x.previousValueOut(8), 4);
        Assert.assertEquals(x.previousValueOut(4), 3);
        Assert.assertEquals(x.previousValueOut(3), 0);
        Assert.assertEquals(x.previousValueOut(0), -3);
        Assert.assertEquals(x.previousValueOut(-3), -4);
    }

    @Test(groups = "1s")
    public void testErrorLB1() {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("", new int[]{1,4}, model);
        try {
            x.updateLowerBound(5, Cause.Null);
            Assert.fail();
        } catch (ContradictionException e) {
            Assert.assertEquals(e.s, "the new lower bound is greater than the current upper bound");
        }
    }

    @Test(groups = "1s")
    public void testErrorLB2() {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("", new int[]{1,4}, model);
        try {
            x.updateBounds(5, 6, Cause.Null);
            Assert.fail();
        } catch (ContradictionException e) {
            Assert.assertEquals(e.s, "the new lower bound is greater than the current upper bound");
        }
    }

    @Test(groups = "1s")
    public void testErrorUB1() {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("", new int[]{1,4}, model);
        try {
            x.updateUpperBound(0, Cause.Null);
            Assert.fail();
        } catch (ContradictionException e) {
            Assert.assertEquals(e.s, "the new upper bound is lesser than the current lower bound");
        }
    }

    @Test(groups = "1s")
    public void testErrorUB2() {
        Model model = new Model();
        IntVar x = new BitsetArrayIntVarImpl("", new int[]{1,4}, model);
        try {
            x.updateBounds(-1, 0, Cause.Null);
            Assert.fail();
        } catch (ContradictionException e) {
            Assert.assertEquals(e.s, "the new upper bound is lesser than the current lower bound");
        }
    }
}
