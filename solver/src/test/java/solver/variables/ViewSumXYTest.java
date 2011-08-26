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
import solver.constraints.nary.Sum;
import solver.exception.ContradictionException;
import solver.search.strategy.StrategyFactory;
import solver.variables.view.Views;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public class ViewSumXYTest {


    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver();

        IntVar X = VariableFactory.enumerated("X", 1, 10, solver);
        IntVar Y = VariableFactory.enumerated("Y", 3, 8, solver);
        IntVar Z = Views.sum(X, Y);

        try {
            solver.propagate();
            Assert.assertFalse(Z.instantiated());
            Assert.assertEquals(Z.getLB(), 4);
            Assert.assertEquals(Z.getUB(), 18);
            Assert.assertTrue(Z.contains(10));
            Assert.assertEquals(Z.nextValue(3), 4);
            Assert.assertEquals(Z.nextValue(10), 11);
            Assert.assertEquals(Z.nextValue(18), Integer.MAX_VALUE);
            Assert.assertEquals(Z.previousValue(19), 18);
            Assert.assertEquals(Z.previousValue(10), 9);
            Assert.assertEquals(Z.previousValue(4), Integer.MIN_VALUE);

            Z.updateLowerBound(12, null);
            Assert.assertEquals(X.getLB(), 4);
            Assert.assertEquals(X.getUB(), 10);
            Assert.assertEquals(Y.getLB(), 3);
            Assert.assertEquals(Y.getUB(), 8);

            Y.updateUpperBound(-2, null);
            Assert.assertEquals(Y.getUB(), -2);
            Assert.assertEquals(X.getLB(), 2);

            Y.removeValue(-4, null);
            Assert.assertFalse(Y.contains(-4));
            Assert.assertFalse(X.contains(4));

            Y.removeInterval(-8, -6, null);
            Assert.assertFalse(Y.contains(-8));
            Assert.assertFalse(Y.contains(-7));
            Assert.assertFalse(Y.contains(-6));
            Assert.assertFalse(X.contains(6));
            Assert.assertFalse(X.contains(7));
            Assert.assertFalse(X.contains(8));

            Assert.assertEquals(X.getDomainSize(), 4);
            Assert.assertEquals(Y.getDomainSize(), 4);

            Y.instantiateTo(-5, null);
            Assert.assertTrue(X.instantiated());
            Assert.assertTrue(Y.instantiated());
            Assert.assertEquals(X.getValue(), 5);
            Assert.assertEquals(Y.getValue(), -5);

        } catch (ContradictionException ex) {

        }
    }

    @Test(groups = "10s")
    public void test2() {
        Random random = new Random();
        for (int seed = 0; seed < 2000; seed++) {
            random.setSeed(seed);
            Solver ref = new Solver();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = VariableFactory.bounded("x", 1, 5, ref);
                xs[1] = VariableFactory.bounded("y", 1, 5, ref);
                xs[2] = VariableFactory.bounded("z", 2, 10, ref);
                ref.post(Sum.eq(xs, new int[]{1, 1, -1}, 0, ref));
                ref.set(StrategyFactory.random(xs, ref.getEnvironment(), seed));
            }
            Solver solver = new Solver();
            {
                IntVar[] xs = new IntVar[2];
                xs[0] = VariableFactory.bounded("x", 1, 5, solver);
                xs[1] = VariableFactory.bounded("y", 1, 5, solver);
                IntVar sum = Views.sum(xs[0], xs[1]);
//                SearchMonitorFactory.log(solver, true, true);
                solver.set(StrategyFactory.random(xs, solver.getEnvironment(), seed));
            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount());

        }
    }

}
