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

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.view.SqrView;
import util.iterators.DisposableRangeIterator;
import util.iterators.DisposableValueIterator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/07/13
 */
public class ViewsTest {

    @Test(groups = "30s")
    public void test1e() {
        // Z = X^2
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, ref);
                IntVar z = VariableFactory.enumerated("z", 0, 4, ref);
                ref.post(IntConstraintFactory.times(x, x, z));
                ref.set(IntStrategyFactory.random(new IntVar[]{x, z}, seed));
            }
            {
                IntVar z = VariableFactory.enumerated("z", 0, 4, solver);
                IntVar x = new SqrView(z, solver);
                solver.set(IntStrategyFactory.random(new IntVar[]{x, z}, seed));
            }
            check(ref, solver, seed, false, true);
        }
    }

    public static void check(Solver ref, Solver solver, long seed, boolean strict, boolean solveAll) {
        //        SearchMonitorFactory.log(ref, true, true);
        //        SearchMonitorFactory.log(solver, true, true);
        if (solveAll) {
            ref.findAllSolutions();
            solver.findAllSolutions();
        } else {
            //            System.out.printf("%s\n", ref.toString());
            ref.findSolution();
            //            System.out.printf("%s\n", solver.toString());
            solver.findSolution();
        }
        Assert.assertEquals(solver.getMeasures().getSolutionCount(),
                ref.getMeasures().getSolutionCount(), "solutions (" + seed + ")");
        //        System.out.printf("%d : %d vs. %d  -- ", seed, ref.getMeasures().getNodeCount(),
        //                solver.getMeasures().getNodeCount());
        if (strict) {
            Assert.assertEquals(solver.getMeasures().getNodeCount(), ref.getMeasures().getNodeCount(), "nodes (" + seed + ")");
        } else {
            Assert.assertTrue(ref.getMeasures().getNodeCount() >=
                    solver.getMeasures().getNodeCount(), seed + "");
        }
        //        System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
        //                solver.getMeasures().getTimeCount(),
        //                ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
    }

    @Test
        public void testSqr1() {
            Solver solver = new Solver();
            IntVar var = new SqrView(VariableFactory.enumerated("b", new int[]{-2, 0, 1, 4}, solver), solver);
            DisposableValueIterator vit = var.getValueIterator(true);
            Assert.assertTrue(vit.hasNext());
            Assert.assertEquals(0, vit.next());
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
            IntVar var = new SqrView(VariableFactory.enumerated("b", new int[]{-2, 0, 1, 4}, solver), solver);
            DisposableValueIterator vit = var.getValueIterator(false);
            Assert.assertTrue(vit.hasPrevious());
            Assert.assertEquals(16, vit.previous());
            Assert.assertTrue(vit.hasPrevious());
            Assert.assertEquals(4, vit.previous());
            Assert.assertTrue(vit.hasPrevious());
            Assert.assertEquals(1, vit.previous());
            Assert.assertTrue(vit.hasPrevious());
            Assert.assertEquals(0, vit.previous());
            Assert.assertFalse(vit.hasPrevious());
        }

        @Test
        public void testSqr3() {
            Solver solver = new Solver();
            IntVar var = new SqrView(VariableFactory.enumerated("b", new int[]{-2, 0, 1, 4}, solver), solver);
            DisposableRangeIterator vit = var.getRangeIterator(true);
            Assert.assertTrue(vit.hasNext());
            Assert.assertEquals(0, vit.min());
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
            IntVar var = new SqrView(VariableFactory.enumerated("b", new int[]{-2, 0, 1, 4}, solver), solver);
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
            Assert.assertEquals(0, vit.min());
            Assert.assertEquals(1, vit.max());
            vit.previous();
            Assert.assertFalse(vit.hasPrevious());
        }
}
