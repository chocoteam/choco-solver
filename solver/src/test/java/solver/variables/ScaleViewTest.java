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

import choco.kernel.memory.IEnvironment;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.ternary.Times;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.view.Views;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public class ScaleViewTest {

    @Test(groups = "1s")
    public void test1() {
        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        IntVar X = VariableFactory.enumerated("X", 1, 3, s);
        IntVar Y = Views.scale(X, 2);

        IntVar[] vars = {X, Y};

        Constraint[] cstrs = {
                ConstraintFactory.neq(Y, 4, s)
        };

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);

        s.post(cstrs);
        s.set(strategy);
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);
    }


    @Test(groups = "1s")
    public void test2() {
        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        IntVar X = VariableFactory.enumerated("X", 1, 4, s);
        IntVar Y = Views.scale(X, 3);

        IntVar[] vars = {X, Y};

        Constraint[] cstrs = {
                ConstraintFactory.neq(Y, -2, s)
        };

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);

        s.post(cstrs);
        s.set(strategy);
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 4);
    }

    private Solver bijective(int low, int upp, int coeff) {
        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        IntVar X = VariableFactory.enumerated("X", low, upp, s);
        IntVar Y = Views.scale(X, coeff);

        IntVar[] vars = {X, Y};

        Constraint[] cstrs = {
                ConstraintFactory.geq(Y, low + coeff - 1, s),
                ConstraintFactory.leq(Y, upp - coeff - 1, s)
        };

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);

        s.post(cstrs);
        s.set(strategy);
        return s;
    }

    private Solver contraint(int low, int upp, int coeff) {
        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        IntVar X = VariableFactory.enumerated("X", low, upp, s);
        IntVar C = Views.fixed("C", coeff, s);
        IntVar Y = VariableFactory.enumerated("Y", low * coeff, upp * coeff, s);

        IntVar[] vars = {X, Y};

        Constraint[] cstrs = {
                ConstraintFactory.geq(Y, low + coeff - 1, s),
                ConstraintFactory.leq(Y, upp - coeff - 1, s),
                new Times(X, C, Y, s)
        };

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);

        s.post(cstrs);
        s.set(strategy);
        return s;
    }

    @Test(groups = "10s")
    public void testRandom1() {
        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            rand.setSeed(i);
            int low = rand.nextInt(10);
            int upp = low + rand.nextInt(1000);
            int coeff = rand.nextInt(5);

            Solver sb = bijective(low, upp, coeff);
            Solver sc = contraint(low, upp, coeff);
            sb.findAllSolutions();
            sc.findAllSolutions();
            Assert.assertEquals(sc.getMeasures().getSolutionCount(), sb.getMeasures().getSolutionCount());
			//Assert.assertEquals(sc.getMeasures().getNodeCount(), sb.getMeasures().getNodeCount());
        }
    }

    @Test(groups = "1s")
    public void testRandom2() {
        Solver sb = bijective(1, 9999, 3);
        Solver sc = contraint(1, 9999, 3);
        sb.findAllSolutions();
        sc.findAllSolutions();
        Assert.assertEquals(sc.getMeasures().getSolutionCount(), sb.getMeasures().getSolutionCount());
        //Assert.assertEquals(sc.getMeasures().getNodeCount(), sb.getMeasures().getNodeCount());
    }
}
