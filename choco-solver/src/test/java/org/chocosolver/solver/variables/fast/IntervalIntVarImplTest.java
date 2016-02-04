/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables.fast;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.impl.IntervalIntVarImpl;
import org.chocosolver.solver.variables.ranges.IntIterableBitSet;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
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
        var = new IntervalIntVarImpl("test", -2, 2, new Solver());
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
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -1, 1, 2, 4);
        x.removeValues(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), -2);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals21() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -2);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals22() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-4);
        rems.add(-4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals3() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(1, 2, 3);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals31() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 3);
    }


    @Test(groups="1s", timeOut=60000)
    public void testRemVals41() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 0, 1);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals42() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(0);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemVals5() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -1, 1, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-1);
        rems.addAll(-1, 0, 1);
        x.removeValues(rems, Cause.Null);
        Assert.fail();
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds1() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        x.updateBounds(-2, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), -2);
        Assert.assertEquals(x.getUB(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds2() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        x.updateBounds(-2, 4, Cause.Null);
        Assert.assertEquals(x.getLB(), -2);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds3() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        x.updateBounds(-4, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), -3);
        Assert.assertEquals(x.getUB(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds4() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        x.updateBounds(0, 0, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testUpdBounds5() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        x.updateBounds(4, -2, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut1() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 1, 2, 4);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), -1);
        Assert.assertEquals(x.getUB(), 2);

    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut21() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -2);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -3);
        Assert.assertEquals(x.getUB(), -2);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut22() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-4);
        rems.add(-4);
        x.removeAllValuesBut(rems, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut3() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(1, 2, 3);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), 1);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut31() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(4);
        x.removeAllValuesBut(rems, Cause.Null);
    }


    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut41() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -1, 1, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 0, 1);
        Assert.assertFalse(x.removeAllValuesBut(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut42() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = solver.intVar("X", -3, 3, true);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(0);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertTrue(x.isInstantiatedTo(0));
    }
}
