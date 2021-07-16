/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.fast;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.impl.IntervalIntVarImpl;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/02/13
 */
public class IntervalIntVarImplTest {

    IntervalIntVarImpl var;

    public void setUp() throws Exception {
        var = new IntervalIntVarImpl("test", -2, 2, new Model());
    }


    @Test(groups="1s", timeOut=60000)
    public void testRemoveValue() throws Exception {
        setUp();
        Assert.assertFalse(var.removeValue(7, Cause.Null));
        Assert.assertFalse(var.removeValue(0, Cause.Null));
        Assert.assertTrue(var.removeValue(-2, Cause.Null));
        Assert.assertFalse(var.contains(-2));
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
        Assert.assertTrue(var.updateLowerBound(0, Cause.Null));
        Assert.assertTrue(var.contains(0));
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
        Assert.assertEquals(-2, var.getLB());
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetUB() throws Exception {
        setUp();
        Assert.assertEquals(2, var.getUB());
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetDomainSize() throws Exception {
        setUp();
        Assert.assertEquals(5, var.getDomainSize());
    }

    @Test(groups="1s", timeOut=60000)
    public void testNextValue() throws Exception {
        setUp();
        Assert.assertEquals(-2, var.nextValue(-3));
        Assert.assertEquals(0, var.nextValue(-1));
        Assert.assertEquals(Integer.MAX_VALUE, var.nextValue(2));
    }

    @Test(groups="1s", timeOut=60000)
    public void testPreviousValue() throws Exception {
        setUp();
        Assert.assertEquals(2, var.previousValue(6));
        Assert.assertEquals(0, var.previousValue(1));
        Assert.assertEquals(Integer.MIN_VALUE, var.previousValue(-2));
    }

    @Test(groups="1s", timeOut=60000)
    public void testHasEnumeratedDomain() throws Exception {

        setUp();
        Assert.assertFalse(var.hasEnumeratedDomain());
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetTypeAndKind() throws Exception {
        setUp();
        Assert.assertTrue((Variable.INT & var.getTypeAndKind()) != 0);
        Assert.assertTrue((Variable.VAR & var.getTypeAndKind()) != 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetValueIterator() throws Exception {
        setUp();
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-2, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-1, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(0, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(2, vit.next());
        Assert.assertFalse(vit.hasNext());
        vit.dispose();

        vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(2, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(0, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-1, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-2, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
        vit.dispose();
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetRangeIterator() throws Exception {
        setUp();
        DisposableRangeIterator rit = var.getRangeIterator(true);
        Assert.assertTrue(rit.hasNext());
        Assert.assertEquals(-2, rit.min());
        Assert.assertEquals(2, rit.max());
        rit.next();
        Assert.assertFalse(rit.hasNext());

        rit = var.getRangeIterator(false);
        Assert.assertTrue(rit.hasPrevious());
        Assert.assertEquals(-2, rit.min());
        Assert.assertEquals(2, rit.max());
        rit.previous();
        Assert.assertFalse(rit.hasPrevious());
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals1() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -1, 1, 2, 4);
        x.removeValues(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), -2);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals21() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -2);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals22() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-4);
        rems.add(-4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals3() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(1, 2, 3);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals31() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 3);
    }


    @Test(groups="1s", timeOut=60000)
    public void testRemVals41() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 0, 1);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals42() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(0);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemVals5() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -1, 1, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-1);
        rems.addAll(-1, 0, 1);
        x.removeValues(rems, Cause.Null);
        Assert.fail();
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds1() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        x.updateBounds(-2, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), -2);
        Assert.assertEquals(x.getUB(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds2() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        x.updateBounds(-2, 4, Cause.Null);
        Assert.assertEquals(x.getLB(), -2);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds3() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        x.updateBounds(-4, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), -3);
        Assert.assertEquals(x.getUB(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds4() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        x.updateBounds(0, 0, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testUpdBounds5() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        x.updateBounds(4, -2, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut1() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 1, 2, 4);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), -1);
        Assert.assertEquals(x.getUB(), 2);

    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut21() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -2);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -3);
        Assert.assertEquals(x.getUB(), -2);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut22() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-4);
        rems.add(-4);
        x.removeAllValuesBut(rems, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut3() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(1, 2, 3);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), 1);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut31() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(4);
        x.removeAllValuesBut(rems, Cause.Null);
    }


    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut41() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -1, 1, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 0, 1);
        Assert.assertFalse(x.removeAllValuesBut(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut42() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -3, 3, true);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(0);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertTrue(x.isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut8() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -27,-20);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-29);
        rems.addAll(-29,-28,-26,-22,-21);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), -26);
        Assert.assertEquals(x.getUB(), -21);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut9() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -27,-20);
        IntIterableBitSet rems = new IntIterableBitSet();
        rems.setOffset(-29);
        rems.addAll(-29,-28,-26,-22,-20);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), -26);
        Assert.assertEquals(x.getUB(), -20);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNextOut1(){
        Model model = new Model();
        IntVar x = model.intVar("X", -1, 1);
        Assert.assertEquals(x.nextValueOut(-4), -3);
        Assert.assertEquals(x.nextValueOut(-3), -2);
        Assert.assertEquals(x.nextValueOut(-2), 2);
        Assert.assertEquals(x.nextValueOut(2), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNextOut2() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -1, 1);
        x.instantiateTo(-1, Cause.Null);
        Assert.assertEquals(x.nextValueOut(-4), -3);
        Assert.assertEquals(x.nextValueOut(-3), -2);
        Assert.assertEquals(x.nextValueOut(-2), 0);
        Assert.assertEquals(x.nextValueOut(0), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testPreviousOut1(){
        Model model = new Model();
        IntVar x = model.intVar("X", -1, 1);
        Assert.assertEquals(x.previousValueOut(4), 3);
        Assert.assertEquals(x.previousValueOut(3), 2);
        Assert.assertEquals(x.previousValueOut(2), -2);
        Assert.assertEquals(x.previousValueOut(-2), -3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testPreviousOut2() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", -1, 1);
        x.instantiateTo(1, Cause.Null);
        Assert.assertEquals(x.previousValueOut(4), 3);
        Assert.assertEquals(x.previousValueOut(3), 2);
        Assert.assertEquals(x.previousValueOut(2), 0);
        Assert.assertEquals(x.previousValueOut(0), -1);
    }
}
