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

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.impl.BitsetArrayIntVarImpl;
import org.chocosolver.solver.variables.ranges.IntIterableBitSet;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
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
        var = new BitsetArrayIntVarImpl("test", new int[]{-5, 0, 3, 4, 5}, new Solver());
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemoveValue() throws Exception {
        setUp();
        Assert.assertFalse(var.removeValue(7, Cause.Null));
        Assert.assertTrue(var.removeValue(0, Cause.Null));
        Assert.assertFalse(var.contains(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemoveInterval() throws Exception {
        setUp();
        Assert.assertFalse(var.removeInterval(7, 8, Cause.Null));
        Assert.assertTrue(var.removeInterval(0, 3, Cause.Null));
        Assert.assertFalse(var.contains(0));
        Assert.assertFalse(var.contains(3));
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdateLowerBound() throws Exception {
        setUp();
        Assert.assertFalse(var.updateLowerBound(-6, Cause.Null));
        Assert.assertTrue(var.updateLowerBound(0, Cause.Null));
        Assert.assertTrue(var.contains(0));
        Assert.assertFalse(var.contains(-5));

    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdateUpperBound() throws Exception {
        setUp();
        Assert.assertFalse(var.updateUpperBound(6, Cause.Null));
        Assert.assertTrue(var.updateUpperBound(0, Cause.Null));
        Assert.assertTrue(var.contains(0));
        Assert.assertFalse(var.contains(5));
    }


    @Test(groups="1s", timeOut=60000)
    public void testGetLB() throws Exception {
        setUp();
        Assert.assertEquals(-5, var.getLB());
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetUB() throws Exception {
        setUp();
        Assert.assertEquals(5, var.getUB());
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetDomainSize() throws Exception {
        setUp();
        Assert.assertEquals(5, var.getDomainSize());
    }

    @Test(groups="1s", timeOut=60000)
    public void testNextValue() throws Exception {
        setUp();
        Assert.assertEquals(-5, var.nextValue(-6));
        Assert.assertEquals(0, var.nextValue(-5));
        Assert.assertEquals(Integer.MAX_VALUE, var.nextValue(5));
    }

    @Test(groups="1s", timeOut=60000)
    public void testPreviousValue() throws Exception {
        setUp();
        Assert.assertEquals(5, var.previousValue(6));
        Assert.assertEquals(0, var.previousValue(3));
        Assert.assertEquals(Integer.MIN_VALUE, var.previousValue(-5));
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
    }

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
    public void testRemVals0() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals1() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -1, 1, 2, 4);
        x.removeValues(rems, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals11() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(2, 5, 8, 9);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertTrue(x.isInstantiatedTo(6));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals12() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(2, 5, 6, 8);
        x.removeValues(rems, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(9));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals13() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(2, 6, 9);
        x.removeValues(rems, Cause.Null);
        Assert.assertTrue(x.getDomainSize() == 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals14() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(5, 6, 8, 9);
        x.removeValues(rems, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(2));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemVals15() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(1, 2, 5, 6, 8, 9);
        x.removeValues(rems, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals21() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -2);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals22() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-4);
        rems.add(-4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals3() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(1, 2, 3);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals31() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 3);
    }


    @Test(groups="1s", timeOut=60000)
    public void testRemVals41() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 0, 1);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals42() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.add(0);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemVals5() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-1);
        rems.addAll(-1, 0, 1);
        x.removeValues(rems, Cause.Null);
        Assert.fail();
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals6() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(0, 1, 2);
        x.removeValues(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals7() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 1, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(1, 2, 3);
        x.removeValues(rems, Cause.Null);
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemVals81() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 1, 3}, solver);
        x.removeValue(1, Cause.Null);
        x.instantiateTo(1, Cause.Null);
        Assert.assertFalse(x.isInstantiated());
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemVals82() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 1, 3}, solver);
        x.removeInterval(1, 2, Cause.Null);
        x.removeInterval(1, 2, Cause.Null);
        Assert.assertEquals(x.getUB(), 3);
        Assert.assertEquals(x.getLB(), 0);
        Assert.assertEquals(x.getDomainSize(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds1() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        x.updateBounds(-2, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), -2);
        Assert.assertEquals(x.getUB(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds2() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        x.updateBounds(-2, 4, Cause.Null);
        Assert.assertEquals(x.getLB(), -2);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds3() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        x.updateBounds(-4, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), -3);
        Assert.assertEquals(x.getUB(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds4() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        x.updateBounds(0, 0, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testUpdBounds5() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        x.updateBounds(4, -2, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds6() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 0, 3}, solver);
        x.updateBounds(-2, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), 0);
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds7() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 0, 3}, solver);
        x.updateBounds(-2, 4, Cause.Null);
        Assert.assertEquals(x.getLB(), 0);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds8() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 0, 3}, solver);
        x.updateBounds(-4, 2, Cause.Null);
        Assert.assertEquals(x.getLB(), -3);
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUpdBounds9() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 0, 3}, solver);
        x.updateBounds(0, 0, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testUpdBounds10() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 0, 3}, solver);
        x.updateBounds(4, -2, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testUpdBounds11() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        x.updateBounds(3, 4, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut0() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        Assert.assertFalse(x.removeAllValuesBut(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut1() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
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

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut11() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(1, 6, 10, 11);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertTrue(x.isInstantiatedTo(6));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut12() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(9, 10, 11);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(9));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut13() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(2, 6, 9);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getDomainSize(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut14() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(2);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(2));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut15() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(1, 3, 4);
        x.removeAllValuesBut(rems, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut21() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-1, 0, 1);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut22() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-4);
        rems.addAll(-3, -2, -1, 0, 1, 2, 3);
        Assert.assertFalse(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -3);
        Assert.assertEquals(x.getUB(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut3() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(1, 2, 3);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut41() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, -2, 2, 3);
        Assert.assertTrue(x.removeAllValuesBut(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut42() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-3);
        rems.addAll(-3, 3);
        Assert.assertFalse(x.removeAllValuesBut(rems, Cause.Null));
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut5() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-1);
        rems.addAll(2, 3);
        x.removeAllValuesBut(rems, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut6() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 2, 3}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(0);
        rems.addAll(1, 2, 4);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), 2);
        Assert.assertEquals(x.getUB(), 2);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemValsBut7() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-27,-25,-20}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-29);
        rems.addAll(-29,-28,-26,-22,-21);
        x.removeAllValuesBut(rems, Cause.Null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut8() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-27,-25,-21}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-29);
        rems.addAll(-29,-28,-26,-22,-21);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), -21);
        Assert.assertEquals(x.getUB(), -21);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemValsBut9() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-29,-25,-21}, solver);
        IntIterableSet rems = new IntIterableBitSet();
        rems.setOffset(-29);
        rems.addAll(-29,-28,-26,-22,-20);
        x.removeAllValuesBut(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), -29);
        Assert.assertEquals(x.getUB(), -29);
    }


    @Test(groups="1s", timeOut=60000)
    public void testJL01() throws ContradictionException {
        Solver s = new Solver();
        IntVar i = new BitsetArrayIntVarImpl("i", new int[]{0,98,99}, s);
        IIntDeltaMonitor d= i.monitorDelta(Cause.Null);
        i.updateUpperBound(98, Cause.Null);
        d.freeze();
        TIntArrayList remvals= new TIntArrayList(1);
        d.forEachRemVal((IntProcedure) remvals::add);
        d.unfreeze();
        Assert.assertEquals(remvals.size(), 1);
        Assert.assertEquals(remvals.get(0), 99);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL02() throws ContradictionException {
        Solver s = new Solver();
        IntVar i = new BitsetArrayIntVarImpl("i", new int[]{0,98,99}, s);
        IIntDeltaMonitor d= i.monitorDelta(Cause.Null);
        i.updateBounds(0,98, Cause.Null);
        d.freeze();
        TIntArrayList remvals= new TIntArrayList(1);
        d.forEachRemVal((IntProcedure) remvals::add);
        d.unfreeze();
        Assert.assertEquals(remvals.size(), 1);
        Assert.assertEquals(remvals.get(0), 99);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL03() throws ContradictionException {
        Solver s = new Solver();
        IntVar i = new BitsetArrayIntVarImpl("i", new int[]{2,3,99}, s);
        IIntDeltaMonitor d= i.monitorDelta(Cause.Null);
        i.updateLowerBound(3, Cause.Null);
        d.freeze();
        TIntArrayList remvals= new TIntArrayList(1);
        d.forEachRemVal((IntProcedure) remvals::add);
        d.unfreeze();
        Assert.assertEquals(remvals.size(), 1);
        Assert.assertEquals(remvals.get(0), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL04() throws ContradictionException {
        Solver s = new Solver();
        IntVar i = new BitsetArrayIntVarImpl("i", new int[]{2,3,99}, s);
        IIntDeltaMonitor d= i.monitorDelta(Cause.Null);
        i.updateBounds(3,99, Cause.Null);
        d.freeze();
        TIntArrayList remvals= new TIntArrayList(1);
        d.forEachRemVal((IntProcedure) remvals::add);
        d.unfreeze();
        Assert.assertEquals(remvals.size(), 1);
        Assert.assertEquals(remvals.get(0), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNextOut1(){
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, solver);
        Assert.assertEquals(x.nextValueOut(-4), -3);
        Assert.assertEquals(x.nextValueOut(-3), -2);
        Assert.assertEquals(x.nextValueOut(-2), 2);
        Assert.assertEquals(x.nextValueOut(2), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNextOut2() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, solver);
        x.instantiateTo(-1, Cause.Null);
        Assert.assertEquals(x.nextValueOut(-4), -3);
        Assert.assertEquals(x.nextValueOut(-3), -2);
        Assert.assertEquals(x.nextValueOut(-2), 0);
        Assert.assertEquals(x.nextValueOut(0), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNextOut3() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-2,-1,1,2,5,6,7}, solver);
        Assert.assertEquals(x.nextValueOut(-4), -3);
        Assert.assertEquals(x.nextValueOut(-3), 0);
        Assert.assertEquals(x.nextValueOut(0), 3);
        Assert.assertEquals(x.nextValueOut(3), 4);
        Assert.assertEquals(x.nextValueOut(4), 8);
        Assert.assertEquals(x.nextValueOut(8), 9);
    }

    @Test(groups="1s", timeOut=60000)
    public void testPreviousOut1(){
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, solver);
        Assert.assertEquals(x.previousValueOut(4), 3);
        Assert.assertEquals(x.previousValueOut(3), 2);
        Assert.assertEquals(x.previousValueOut(2), -2);
        Assert.assertEquals(x.previousValueOut(-2), -3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testPreviousOut2() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, solver);
        x.instantiateTo(1, Cause.Null);
        Assert.assertEquals(x.previousValueOut(4), 3);
        Assert.assertEquals(x.previousValueOut(3), 2);
        Assert.assertEquals(x.previousValueOut(2), 0);
        Assert.assertEquals(x.previousValueOut(0), -1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testPreviousOut3() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-2,-1,1,2,5,6,7}, solver);
        Assert.assertEquals(x.previousValueOut(9), 8);
        Assert.assertEquals(x.previousValueOut(8), 4);
        Assert.assertEquals(x.previousValueOut(4), 3);
        Assert.assertEquals(x.previousValueOut(3), 0);
        Assert.assertEquals(x.previousValueOut(0), -3);
        Assert.assertEquals(x.previousValueOut(-3), -4);
    }
}
