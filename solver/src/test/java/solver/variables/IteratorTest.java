/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.variables;

import choco.kernel.common.util.iterators.DisposableRangeIterator;
import choco.kernel.common.util.iterators.DisposableValueIterator;
import junit.framework.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.nary.Sum;
import solver.constraints.ternary.Max;
import solver.variables.view.Views;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/09/11
 */
public class IteratorTest {


    @Test
    public void testBool1() {
        Solver solver = new Solver();
        BoolVar var = VariableFactory.bool("b", solver);
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(0, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testBool2() {
        Solver solver = new Solver();
        BoolVar var = VariableFactory.bool("b", solver);
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(0, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testBool3() {
        Solver solver = new Solver();
        BoolVar var = VariableFactory.bool("b", solver);
        DisposableRangeIterator rit = var.getRangeIterator(true);
        Assert.assertTrue(rit.hasNext());
        Assert.assertEquals(0, rit.min());
        Assert.assertEquals(1, rit.max());
        rit.next();
        Assert.assertFalse(rit.hasNext());
    }

    @Test
    public void testBool4() {
        Solver solver = new Solver();
        BoolVar var = VariableFactory.bool("b", solver);
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(0, vit.min());
        Assert.assertEquals(1, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testBound1() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.bounded("b", 1, 3, solver);
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(2, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(3, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testBound2() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.bounded("b", 1, 3, solver);
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(3, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(2, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testBound3() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.bounded("b", 1, 3, solver);
        DisposableRangeIterator vit = var.getRangeIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.min());
        Assert.assertEquals(3, vit.max());
        vit.next();
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testBound4() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.bounded("b", 1, 3, solver);
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.min());
        Assert.assertEquals(3, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testEnum1() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver);
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(2, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testEnum2() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver);
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(2, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testEnum3() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver);
        DisposableRangeIterator vit = var.getRangeIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.min());
        Assert.assertEquals(2, vit.max());
        vit.next();
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.min());
        Assert.assertEquals(4, vit.max());
        vit.next();
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testEnum4() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver);
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.min());
        Assert.assertEquals(4, vit.max());
        vit.previous();
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.min());
        Assert.assertEquals(2, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testCste1() {
        Solver solver = new Solver();
        IntVar var = Views.fixed(8, solver);
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(8, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testCste2() {
        Solver solver = new Solver();
        IntVar var = Views.fixed(8, solver);
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(8, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testCste3() {
        Solver solver = new Solver();
        IntVar var = Views.fixed(8, solver);
        DisposableRangeIterator vit = var.getRangeIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(8, vit.min());
        Assert.assertEquals(8, vit.max());
        vit.next();
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testCste4() {
        Solver solver = new Solver();
        IntVar var = Views.fixed(8, solver);
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(8, vit.min());
        Assert.assertEquals(8, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testOffset1() {
        Solver solver = new Solver();
        IntVar var = Views.offset(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(3, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(6, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testOffset2() {
        Solver solver = new Solver();
        IntVar var = Views.offset(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(6, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(3, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testOffset3() {
        Solver solver = new Solver();
        IntVar var = Views.offset(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        DisposableRangeIterator vit = var.getRangeIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(3, vit.min());
        Assert.assertEquals(4, vit.max());
        vit.next();
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(6, vit.min());
        Assert.assertEquals(6, vit.max());
        vit.next();
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testOffset4() {
        Solver solver = new Solver();
        IntVar var = Views.offset(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(6, vit.min());
        Assert.assertEquals(6, vit.max());
        vit.previous();
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(3, vit.min());
        Assert.assertEquals(4, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testScale1() {
        Solver solver = new Solver();
        IntVar var = Views.scale(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(2, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(8, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testScale2() {
        Solver solver = new Solver();
        IntVar var = Views.scale(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(8, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(2, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testScale3() {
        Solver solver = new Solver();
        IntVar var = Views.scale(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        DisposableRangeIterator vit = var.getRangeIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(2, vit.min());
        Assert.assertEquals(2, vit.max());
        vit.next();
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.min());
        Assert.assertEquals(4, vit.max());
        vit.next();
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(8, vit.min());
        Assert.assertEquals(8, vit.max());
        vit.next();
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testScale4() {
        Solver solver = new Solver();
        IntVar var = Views.scale(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(8, vit.min());
        Assert.assertEquals(8, vit.max());
        vit.previous();
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.min());
        Assert.assertEquals(4, vit.max());
        vit.previous();
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(2, vit.min());
        Assert.assertEquals(2, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testMinus1() {
        Solver solver = new Solver();
        IntVar var = Views.minus(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver));
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-4, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-2, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-1, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testMinus2() {
        Solver solver = new Solver();
        IntVar var = Views.minus(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver));
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-1, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-2, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-4, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testMinus3() {
        Solver solver = new Solver();
        IntVar var = Views.minus(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver));
        DisposableRangeIterator vit = var.getRangeIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-4, vit.min());
        Assert.assertEquals(-4, vit.max());
        vit.next();
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-2, vit.min());
        Assert.assertEquals(-1, vit.max());
        vit.next();
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testMinus4() {
        Solver solver = new Solver();
        IntVar var = Views.minus(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver));
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-2, vit.min());
        Assert.assertEquals(-1, vit.max());
        vit.previous();
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-4, vit.min());
        Assert.assertEquals(-4, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testAbs1() {
        Solver solver = new Solver();
        IntVar var = Views.abs(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(2, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testAbs2() {
        Solver solver = new Solver();
        IntVar var = Views.abs(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(2, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testAbs3() {
        Solver solver = new Solver();
        IntVar var = Views.abs(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        DisposableRangeIterator vit = var.getRangeIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.min());
        Assert.assertEquals(2, vit.max());
        vit.next();
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.min());
        Assert.assertEquals(4, vit.max());
        vit.next();
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testAbs4() {
        Solver solver = new Solver();
        IntVar var = Views.abs(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.min());
        Assert.assertEquals(4, vit.max());
        vit.previous();
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.min());
        Assert.assertEquals(2, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testMax1() {
        Solver solver = new Solver();
        IntVar var = Max.var(VariableFactory.enumerated("a", new int[]{3, 4}, solver), VariableFactory.enumerated("b", new int[]{2, 5}, solver));
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(3, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(5, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testMax2() {
        Solver solver = new Solver();
        IntVar var = Max.var(VariableFactory.enumerated("a", new int[]{3, 4}, solver), VariableFactory.enumerated("b", new int[]{2, 5}, solver));
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(5, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(3, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testMax3() {
        Solver solver = new Solver();
        IntVar var = Max.var(VariableFactory.enumerated("a", new int[]{3, 4}, solver), VariableFactory.enumerated("b", new int[]{2, 5}, solver));
        DisposableRangeIterator vit = var.getRangeIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(3, vit.min());
        Assert.assertEquals(5, vit.max());
        vit.next();
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testMax4() {
        Solver solver = new Solver();
        IntVar var = Max.var(VariableFactory.enumerated("a", new int[]{3, 4}, solver), VariableFactory.enumerated("b", new int[]{2, 5}, solver));
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(3, vit.min());
        Assert.assertEquals(5, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testSum1() {
        Solver solver = new Solver();
        IntVar var = Sum.var(VariableFactory.enumerated("a", new int[]{1, 3, 4}, solver), VariableFactory.enumerated("b", new int[]{2, 5}, solver));
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(3, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(5, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(6, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(8, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(9, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testSum2() {
        Solver solver = new Solver();
        IntVar var = Sum.var(VariableFactory.enumerated("a", new int[]{1, 3, 4}, solver), VariableFactory.enumerated("b", new int[]{2, 5}, solver));
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(9, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(8, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(6, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(5, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(3, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testSum3() {
        Solver solver = new Solver();
        IntVar var = Sum.var(VariableFactory.enumerated("a", new int[]{1, 3, 4}, solver), VariableFactory.enumerated("b", new int[]{2, 5}, solver));
        DisposableRangeIterator vit = var.getRangeIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(3, vit.min());
        Assert.assertEquals(3, vit.max());
        vit.next();
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(5, vit.min());
        Assert.assertEquals(6, vit.max());
        vit.next();
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(8, vit.min());
        Assert.assertEquals(9, vit.max());
        vit.next();
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testSum4() {
        Solver solver = new Solver();
        IntVar var = Sum.var(VariableFactory.enumerated("a", new int[]{1, 3, 4}, solver), VariableFactory.enumerated("b", new int[]{2, 5}, solver));
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(8, vit.min());
        Assert.assertEquals(9, vit.max());
        vit.previous();
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(5, vit.min());
        Assert.assertEquals(6, vit.max());
        vit.previous();
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(3, vit.min());
        Assert.assertEquals(3, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testSqr1() {
        Solver solver = new Solver();
        IntVar var = Views.sqr(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(16, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testSqr2() {
        Solver solver = new Solver();
        IntVar var = Views.sqr(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(16, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test
    public void testSqr3() {
        Solver solver = new Solver();
        IntVar var = Views.sqr(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        DisposableRangeIterator vit = var.getRangeIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.min());
        Assert.assertEquals(1, vit.max());
        vit.next();
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.min());
        Assert.assertEquals(4, vit.max());
        vit.next();
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(16, vit.min());
        Assert.assertEquals(16, vit.max());
        vit.next();
        Assert.assertFalse(vit.hasNext());
    }

    @Test
    public void testSqr4() {
        Solver solver = new Solver();
        IntVar var = Views.sqr(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(16, vit.min());
        Assert.assertEquals(16, vit.max());
        vit.previous();
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.min());
        Assert.assertEquals(4, vit.max());
        vit.previous();
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.min());
        Assert.assertEquals(1, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

}
