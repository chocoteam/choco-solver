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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.testng.annotations.Test;

import java.util.Random;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;
import static org.testng.Assert.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public class ViewSumXYTest {


    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Model model = new Model();

        IntVar X = model.intVar("X", 1, 10, false);
        IntVar Y = model.intVar("Y", 3, 8, false);
        IntVar Z = model.intVar("Z", 0, 200, false);
        model.sum(new IntVar[]{X, Y}, "=", Z).post();

        try {
            model.getSolver().propagate();
            assertFalse(Z.isInstantiated());
            assertEquals(Z.getLB(), 4);
            assertEquals(Z.getUB(), 18);
            assertTrue(Z.contains(10));
            assertEquals(Z.nextValue(3), 4);
            assertEquals(Z.nextValue(10), 11);
            assertEquals(Z.nextValue(18), MAX_VALUE);
            assertEquals(Z.previousValue(19), 18);
            assertEquals(Z.previousValue(10), 9);
            assertEquals(Z.previousValue(4), MIN_VALUE);

            Z.updateLowerBound(12, Null);
            model.getSolver().propagate();
            assertEquals(X.getLB(), 4);
            assertEquals(X.getUB(), 10);
            assertEquals(Y.getLB(), 3);
            assertEquals(Y.getUB(), 8);

            Y.updateUpperBound(-2, Null);
            model.getSolver().propagate();
            assertEquals(Y.getUB(), -2);
            assertEquals(X.getLB(), 2);

            Y.removeValue(-4, Null);
            model.getSolver().propagate();
            assertFalse(Y.contains(-4));
            assertFalse(X.contains(4));

            Y.removeInterval(-8, -6, Null);
            model.getSolver().propagate();
            assertFalse(Y.contains(-8));
            assertFalse(Y.contains(-7));
            assertFalse(Y.contains(-6));
            assertFalse(X.contains(6));
            assertFalse(X.contains(7));
            assertFalse(X.contains(8));

            assertEquals(X.getDomainSize(), 4);
            assertEquals(Y.getDomainSize(), 4);

            Y.instantiateTo(-5, Null);
            model.getSolver().propagate();
            assertTrue(X.isInstantiated());
            assertTrue(Y.isInstantiated());
            assertEquals(X.getValue(), 5);
            assertEquals(Y.getValue(), -5);

        } catch (ContradictionException ignored) {
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test2() {
        Random random = new Random();
        for (int seed = 0; seed < 2000; seed++) {
            random.setSeed(seed);
            Model ref = new Model();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = ref.intVar("x", 1, 5, true);
                xs[1] = ref.intVar("y", 1, 5, true);
                xs[2] = ref.intVar("z", 2, 10, true);
                ref.scalar(xs, new int[]{1, 1, -1}, "=", 0).post();
                ref.getSolver().set(randomSearch(xs, seed));
            }
            Model model = new Model();
            {
                IntVar[] xs = new IntVar[2];
                xs[0] = model.intVar("x", 1, 5, true);
                xs[1] = model.intVar("y", 1, 5, true);
                IntVar Z = model.intVar("Z", 0, 200, false);
                model.sum(xs, "=", Z).post();
//                SearchMonitorFactory.log(solver, true, true);
                model.getSolver().set(randomSearch(xs, seed));
            }
            while (ref.solve()) ;
            while (model.solve()) ;
            assertEquals(model.getSolver().getMeasures().getSolutionCount(), ref.getSolver().getMeasures().getSolutionCount(), "seed:" + seed);

        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test3() {
        Random random = new Random();
        for (int seed = 0; seed < 2000; seed++) {
            random.setSeed(seed);
            Model ref = new Model();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = ref.intVar("x", 1, 5, false);
                xs[1] = ref.intVar("y", 1, 5, false);
                xs[2] = ref.intVar("z", 2, 10, false);
                ref.scalar(xs, new int[]{1, 1, -1}, "=", 0).post();
                ref.getSolver().set(randomSearch(xs, seed));
            }
            Model model = new Model();
            {
                IntVar[] xs = new IntVar[2];
                xs[0] = model.intVar("x", 1, 5, false);
                xs[1] = model.intVar("y", 1, 5, false);
                IntVar Z = model.intVar("Z", 0, 200, false);
                model.sum(xs, "=", Z).post();
//                SearchMonitorFactory.log(solver, true, true);
                model.getSolver().set(randomSearch(xs, seed));
            }
            while (ref.solve()) ;
            while (model.solve()) ;
            assertEquals(model.getSolver().getMeasures().getSolutionCount(), ref.getSolver().getMeasures().getSolutionCount(), "seed:" + seed);

        }
    }

}
