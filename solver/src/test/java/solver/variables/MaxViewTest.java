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
import solver.Cause;
import solver.Solver;
import solver.constraints.ternary.MaxXYZ;
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
public class MaxViewTest {


    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver();

        IntVar X = VariableFactory.bounded("X", -4, 12, solver);
        IntVar Y = VariableFactory.bounded("Y", -22, 10, solver);
        IntVar Z = Views.max(X, Y);

        try {
            solver.propagate();
            Assert.assertFalse(Z.instantiated());
            Assert.assertEquals(Z.getLB(), -4);
            Assert.assertEquals(Z.getUB(), 12);
            Assert.assertTrue(Z.contains(0));
            Assert.assertEquals(Z.nextValue(-5), -4);
            Assert.assertEquals(Z.nextValue(-4), -3);
            Assert.assertEquals(Z.nextValue(0), 1);
            Assert.assertEquals(Z.nextValue(12), Integer.MAX_VALUE);
            Assert.assertEquals(Z.previousValue(13), 12);
            Assert.assertEquals(Z.previousValue(12), 11);
            Assert.assertEquals(Z.previousValue(1), 0);
            Assert.assertEquals(Z.previousValue(-4), Integer.MIN_VALUE);

            Z.updateLowerBound(2, Cause.Null, false);
            Assert.assertEquals(X.getLB(), -4);
            Assert.assertEquals(X.getUB(), 12);
            Assert.assertEquals(Y.getLB(), -22);
            Assert.assertEquals(Y.getUB(), 10);

            Z.updateUpperBound(9, Cause.Null, false);
            Assert.assertEquals(X.getUB(), 9);
            Assert.assertEquals(Y.getUB(), 9);

            Z.removeInterval(7, 9, Cause.Null, false);
            Assert.assertEquals(X.getUB(), 6);
            Assert.assertEquals(Y.getUB(), 6);

            Assert.assertEquals(X.getDomainSize(), 11);
            Assert.assertEquals(Y.getDomainSize(), 29);

            Z.instantiateTo(5, Cause.Null, false);
            Assert.assertEquals(X.getUB(), 5);
            Assert.assertEquals(Y.getUB(), 5);

        } catch (ContradictionException ex) {
            Assert.fail();
        }
    }

    @Test(groups = "10s")
    public void test2() {
        Random random = new Random();
        for (int seed = 0; seed < 2000; seed++) {
            random.setSeed(seed);
            int lX = random.nextInt(5);
            int uX = lX + random.nextInt(15);
            int lY = random.nextInt(5);
            int uY = lY + random.nextInt(15);

            Solver ref = new Solver();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = VariableFactory.bounded("x", lX, uX, ref);
                xs[1] = VariableFactory.bounded("y", lY, uY, ref);
                xs[2] = VariableFactory.bounded("z", Math.min(lX, lY), Math.max(uX,uY), ref);
                ref.post(new MaxXYZ(xs[2], xs[0], xs[1], ref));
//                SearchMonitorFactory.log(ref, true, true);
                ref.set(StrategyFactory.random(xs, ref.getEnvironment(), seed));
            }
            Solver solver = new Solver();
            {
                IntVar[] xs = new IntVar[2];
                xs[0] = VariableFactory.bounded("x", lX, uX, solver);
                xs[1] = VariableFactory.bounded("y", lY, uY, solver);
                IntVar max = Views.max(xs[0], xs[1]);
//                SearchMonitorFactory.log(solver, true, true);
                solver.set(StrategyFactory.random(xs, solver.getEnvironment(), seed));
            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount());
//            Assert.assertEquals(solver.getMeasures().getNodeCount(), ref.getMeasures().getNodeCount());
            System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
                    solver.getMeasures().getTimeCount(),
                    ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
        }
    }
}
