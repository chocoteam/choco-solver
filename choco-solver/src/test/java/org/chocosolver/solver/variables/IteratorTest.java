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
package org.chocosolver.solver.variables;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ternary.Max;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/09/11
 */
public class IteratorTest {


    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
    public void testCste1() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.fixed(8, solver);
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(8, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test(groups="1s", timeOut=60000)
    public void testCste2() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.fixed(8, solver);
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(8, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test(groups="1s", timeOut=60000)
    public void testCste3() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.fixed(8, solver);
        DisposableRangeIterator vit = var.getRangeIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(8, vit.min());
        Assert.assertEquals(8, vit.max());
        vit.next();
        Assert.assertFalse(vit.hasNext());
    }

    @Test(groups="1s", timeOut=60000)
    public void testCste4() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.fixed(8, solver);
        DisposableRangeIterator vit = var.getRangeIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(8, vit.min());
        Assert.assertEquals(8, vit.max());
        vit.previous();
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test(groups="1s", timeOut=60000)
    public void testOffset1() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.offset(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        if (!solver.getSettings().enableViews()) {
            try {
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(3, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(6, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test(groups="1s", timeOut=60000)
    public void testOffset2() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.offset(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        if (!solver.getSettings().enableViews()) {
            try {
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(6, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(3, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test(groups="1s", timeOut=60000)
    public void testOffset3() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.offset(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        if (!solver.getSettings().enableViews()) {
            try {
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
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

    @Test(groups="1s", timeOut=60000)
    public void testOffset4() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.offset(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        if (!solver.getSettings().enableViews()) {
            try {
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
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

    @Test(groups="1s", timeOut=60000)
    public void testScale1() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.scale(VariableFactory.bounded("b", 1, 4, solver), 2);
        if (!solver.getSettings().enableViews()) {
            try {
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
        Assert.assertEquals(2, var.getLB());
        Assert.assertEquals(8, var.getUB());
    }

    @Test(groups="1s", timeOut=60000)
    public void testScale2() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.scale(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        if (!solver.getSettings().enableViews()) {
            try {
                // currently, the propagation is not sufficient (bound)
                // could be fixed with an extension filtering
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(8, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(2, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test(groups="1s", timeOut=60000)
    public void testScale3() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.scale(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        if (!solver.getSettings().enableViews()) {
            try {
                // currently, the propagation is not sufficient (bound)
                // could be fixed with an extension filtering
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
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

    @Test(groups="1s", timeOut=60000)
    public void testScale4() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.scale(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver), 2);
        if (!solver.getSettings().enableViews()) {
            try {
                // currently, the propagation is not sufficient (bound)
                // could be fixed with an extension filtering
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
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

    @Test(groups="1s", timeOut=60000)
    public void testMinus1() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.minus(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver));
        if (!solver.getSettings().enableViews()) {
            try {
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-4, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-2, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-1, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test(groups="1s", timeOut=60000)
    public void testMinus2() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.minus(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver));
        if (!solver.getSettings().enableViews()) {
            try {
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-1, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-2, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-4, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test(groups="1s", timeOut=60000)
    public void testMinus3() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.minus(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver));
        if (!solver.getSettings().enableViews()) {
            try {
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
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

    @Test(groups="1s", timeOut=60000)
    public void testMinus4() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.minus(VariableFactory.enumerated("b", new int[]{1, 2, 4}, solver));
        if (!solver.getSettings().enableViews()) {
            try {
                solver.propagate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
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

    @Test(groups="1s", timeOut=60000)
    public void testAbs1() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.abs(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(1, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(2, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.next());
        Assert.assertFalse(vit.hasNext());
    }

    @Test(groups="1s", timeOut=60000)
    public void testAbs2() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.abs(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        DisposableValueIterator vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(2, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(1, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
    }

    @Test(groups="1s", timeOut=60000)
    public void testAbs3() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.abs(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
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

    @Test(groups="1s", timeOut=60000)
    public void testAbs4() {
        Solver solver = new Solver();
        IntVar var = VariableFactory.abs(VariableFactory.enumerated("b", new int[]{-2, 1, 4}, solver));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
    public void JLiangWaterlooTest() throws ContradictionException {
        Solver s = new Solver();
        IntVar ivar = VariableFactory.enumerated("ivar", new int[]{1, 2, 3, 888, 1000, 2000}, s);
        ivar.removeValue(1000, Cause.Null);

        DisposableRangeIterator iter = ivar.getRangeIterator(true);
        Assert.assertEquals(iter.min(), 1);
        Assert.assertEquals(iter.max(), 3);
        iter.next();
        Assert.assertEquals(iter.min(), 888);
        Assert.assertEquals(iter.max(), 888);
        iter.next();
        Assert.assertEquals(iter.min(), 2000);
        Assert.assertEquals(iter.max(), 2000);
        iter.dispose();

        iter = ivar.getRangeIterator(false);
        Assert.assertEquals(iter.min(), 2000);
        Assert.assertEquals(iter.max(), 2000);
        iter.previous();
        Assert.assertEquals(iter.min(), 888);
        Assert.assertEquals(iter.max(), 888);
        iter.previous();
        Assert.assertEquals(iter.min(), 1);
        Assert.assertEquals(iter.max(), 3);
    }
}
