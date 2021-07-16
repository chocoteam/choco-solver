/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 04/02/2016.
 */
public class BoolVarImplTest {

    BoolVarImpl var;

    public void setUp() throws Exception {
        var = new BoolVarImpl("test", new Model());
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemoveValue() throws Exception {
        setUp();
        Assert.assertFalse(var.removeValue(7, Cause.Null));
        Assert.assertTrue(var.removeValue(0, Cause.Null));
        Assert.assertFalse(var.removeValue(2, Cause.Null));
        Assert.assertFalse(var.contains(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemoveInterval() throws Exception {
        setUp();
        Assert.assertFalse(var.removeInterval(7, 8, Cause.Null));
        Assert.assertTrue(var.removeInterval(1, 3, Cause.Null));
        Assert.assertFalse(var.contains(1));
        Assert.assertFalse(var.contains(2));
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdateLowerBound() throws Exception {
        setUp();
        Assert.assertFalse(var.updateLowerBound(-6, Cause.Null));
        Assert.assertTrue(var.updateLowerBound(1, Cause.Null));
        Assert.assertTrue(var.contains(1));
        Assert.assertFalse(var.contains(-2));

    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdateUpperBound() throws Exception {
        setUp();
        Assert.assertFalse(var.updateUpperBound(6, Cause.Null));
        Assert.assertTrue(var.updateUpperBound(0, Cause.Null));
        Assert.assertTrue(var.contains(0));
        Assert.assertFalse(var.contains(2));
    }


    @Test(groups="1s", timeOut=60000)
    public void testGetLB() throws Exception {

        setUp();
        Assert.assertEquals(0, var.getLB());
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetUB() throws Exception {
        setUp();
        Assert.assertEquals(1, var.getUB());
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetDomainSize() throws Exception {
        setUp();
        Assert.assertEquals(2, var.getDomainSize());
    }

    @Test(groups="1s", timeOut=60000)
    public void testNextValue() throws Exception {
        setUp();
        Assert.assertEquals(0, var.nextValue(-3));
        Assert.assertEquals(1, var.nextValue(0));
        Assert.assertEquals(Integer.MAX_VALUE, var.nextValue(1));
    }

    @Test(groups="1s", timeOut=60000)
    public void testPreviousValue() throws Exception {
        setUp();
        Assert.assertEquals(1, var.previousValue(6));
        Assert.assertEquals(0, var.previousValue(1));
        Assert.assertEquals(Integer.MIN_VALUE, var.previousValue(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testHasEnumeratedDomain() throws Exception {

        setUp();
        Assert.assertTrue(var.hasEnumeratedDomain());
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetTypeAndKind() throws Exception {
        setUp();
        Assert.assertTrue((Variable.INT & var.getTypeAndKind()) != 0);
        Assert.assertTrue((Variable.VAR & var.getTypeAndKind()) != 0);
        Assert.assertTrue((Variable.BOOL & var.getTypeAndKind()) != 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetValueIterator() throws Exception {
        setUp();
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(0, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.next());
        Assert.assertFalse(vit.hasNext());
        vit.dispose();

        vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(0, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
        vit.dispose();
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetRangeIterator() throws Exception {
        setUp();
        DisposableRangeIterator rit = var.getRangeIterator(true);
        Assert.assertTrue(rit.hasNext());
        Assert.assertEquals(0, rit.min());
        Assert.assertEquals(1, rit.max());
        rit.next();
        Assert.assertFalse(rit.hasNext());

        rit = var.getRangeIterator(false);
        Assert.assertTrue(rit.hasPrevious());
        Assert.assertEquals(0, rit.min());
        Assert.assertEquals(1, rit.max());
        rit.previous();
        Assert.assertFalse(rit.hasPrevious());
    }

    @Test(groups="1s", timeOut=60000)
    public void testIterator() throws Exception {
        setUp();
        Iterator<Integer> iter = var.iterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals(iter.next().intValue(), 0);
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals(iter.next().intValue(), 1);
        Assert.assertFalse(iter.hasNext());
        Assert.assertThrows(NoSuchElementException.class, () -> iter.next());
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals1() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -1, 1, 2, 4);
        x.removeValues(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), 0);
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals21() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -2);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals22() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-4);
        rems.add(-4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals3() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(1, 2, 3);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals31() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 1);
    }


    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemVals41() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 0, 1);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals42() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(0);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemVals5() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-1);
        rems.addAll(-1, 0, 1);
        x.removeValues(rems, Cause.Null);
        Assert.fail();
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds1() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        x.updateBounds(-2, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), 0);
        Assert.assertEquals(x.getUB(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds2() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        x.updateBounds(-2, 0, Cause.Null);
        Assert.assertEquals(x.getLB(), 0);
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds3() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        x.updateBounds(1, 4, Cause.Null);
        Assert.assertEquals(x.getLB(), 1);
        Assert.assertEquals(x.getUB(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds4() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        x.updateBounds(0, 0, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testUpdBounds5() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        x.updateBounds(4, -2, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut1() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 1, 2, 4);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), 1);
        Assert.assertEquals(x.getUB(), 1);

    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut21() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(0, 1);
        Assert.assertFalse(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), 0);
        Assert.assertEquals(x.getUB(), 1);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut22() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-4);
        rems.add(-4);
        x.removeAllValuesBut(rems, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut3() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(1, 2, 3);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), 1);
        Assert.assertEquals(x.getUB(), 1);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut31() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(4);
        x.removeAllValuesBut(rems, Cause.Null);
    }


    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut41() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 0, 1);
        Assert.assertFalse(x.removeAllValuesBut(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut42() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(0);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertTrue(x.isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testNextOut1(){
        Model model = new Model();
        IntVar x = model.boolVar();
        Assert.assertEquals(x.nextValueOut(-2), -1);
        Assert.assertEquals(x.nextValueOut(-1), 2);
        Assert.assertEquals(x.nextValueOut(2), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNextOut2() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        x.instantiateTo(0, Cause.Null);
        Assert.assertEquals(x.nextValueOut(-2), -1);
        Assert.assertEquals(x.nextValueOut(-1), 1);
        Assert.assertEquals(x.nextValueOut(1), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testPreviousOut1(){
        Model model = new Model();
        IntVar x = model.boolVar();
        Assert.assertEquals(x.previousValueOut(3), 2);
        Assert.assertEquals(x.previousValueOut(2), -1);
        Assert.assertEquals(x.previousValueOut(-1), -2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testPreviousOut2() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.boolVar();
        x.instantiateTo(1, Cause.Null);
        Assert.assertEquals(x.previousValueOut(4), 3);
        Assert.assertEquals(x.previousValueOut(3), 2);
        Assert.assertEquals(x.previousValueOut(2), 0);
        Assert.assertEquals(x.previousValueOut(0), -1);
    }
}