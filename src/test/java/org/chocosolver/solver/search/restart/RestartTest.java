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
package org.chocosolver.solver.search.restart;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/05/11
 */
public class RestartTest {

    protected static Model buildQ(int n) {
        Model model = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = model.intVar("Q_" + i, 1, n, false);
        }


        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = model.arithm(vars[i], "!=", vars[j]);
                neq.post();
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        model.getSolver().set(inputOrderLBSearch(vars));
        return model;
    }

    @Test(groups="1s", timeOut=60000)
    public void testGeometricalRestart1() {
        Model model = buildQ(4);
        model.getSolver().setGeometricalRestart(2, 1.1, new NodeCounter(model, 2), 2);
        while (model.solve()) ;
        // not 2, because of restart, that found twice the same solution
        assertEquals(model.getSolver().getSolutionCount(), 2);
        assertEquals(model.getSolver().getRestartCount(), 2);
        assertEquals(model.getSolver().getNodeCount(), 12);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLubyRestart1() {
        Model model = buildQ(4);
        model.getSolver().setLubyRestart(2, 2, new NodeCounter(model, 2), 2);
        while (model.solve()) ;
        // not 2, because of restart, that found twice the same solution
        assertEquals(model.getSolver().getSolutionCount(), 2);
        assertEquals(model.getSolver().getRestartCount(), 2);
        assertEquals(model.getSolver().getNodeCount(), 11);
    }


    public final static int[] LUBY_2 = {1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 16};

    public final static int[] LUBY_3 = {1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9,
            1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9,
            1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9, 27};

    public final static int[] LUBY_4 = {1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16,
            1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16,
            1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16,
            1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16, 64
    };

    public final static int[] GEOMETRIC_1_3 = {1, 2, 2, 3, 3, 4, 5, 7, 9, 11, 14, 18, 24, 31, 40};

    private void checkRestart(AbstractRestartStrategy r, double factor, int[] expected) {
        r.setGeometricalFactor(factor);
        int[] computed = r.getSequenceExample(expected.length);
        Assert.assertEquals(computed, expected);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRestartStrategy() {
        AbstractRestartStrategy r = new LubyRestartStrategy(1, 2);
        checkRestart(r, 2, LUBY_2);
        checkRestart(r, 3, LUBY_3);
        checkRestart(r, 4, LUBY_4);
        r = new GeometricalRestartStrategy(1, 1.3);
        checkRestart(r, 1.3, GEOMETRIC_1_3);
    }

    @Test(groups="10s", timeOut=60000)
    public void test1() {

        for (int j = 1; j < 5; j++) {
            int n = 200;
            Model model = new Model("Test");
            IntVar[] X = model.intVarArray("X", n, 1, n, false);
            IntVar[] Y = model.intVarArray("Y", n, n + 1, 2 * (n + 1), false);
            model.allDifferent(X).post();
            for (int i = 0; i < n; i++) {
                model.arithm(Y[i], "=", X[i], "+", n).post();
            }
            model.getSolver().setRestartOnSolutions();
            model.getSolver().set(inputOrderLBSearch(X));
            model.getSolver().limitSolution(100);
            while (model.solve()) ;
            //System.out.printf("%d - %.3fms \n", n, solver.getTimeCount());
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testGeometricalRestart2() {
        Model model = buildQ(8);
        model.getSolver().setGeometricalRestart(10, 1.2, new FailCounter(model, 10), 2);
        while (model.solve()) ;
        // not 2, because of restart, that found twice the same solution
//        Assert.assertEquals(solver.getSolutionCount(), 92);
        assertEquals(model.getSolver().getRestartCount(), 2);
    }
}
