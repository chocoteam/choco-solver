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
package solver.constraints.nary;

import choco.kernel.common.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Arrays;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/11
 */
public class BoundGlobalCardinlityTest {

    @Test
    public void test0() throws ContradictionException {
        Solver solver = new Solver();

        IntVar[] vars = VariableFactory.boundedArray("vars", 6, 0, 3, solver);
        IntVar[] card = VariableFactory.boundedArray("card", 4, 0, 6, solver);

        solver.post(GlobalCardinality.make(vars, card, 0, solver));

        vars[0].instantiateTo(0, Cause.Null);
        vars[1].instantiateTo(1, Cause.Null);
        vars[2].instantiateTo(3, Cause.Null);
        vars[3].instantiateTo(2, Cause.Null);
        vars[4].instantiateTo(0, Cause.Null);
        vars[5].instantiateTo(0, Cause.Null);

        solver.set(StrategyFactory.presetI(ArrayUtils.append(vars, card), solver.getEnvironment()));
        solver.findAllSolutions();
        Assert.assertTrue(solver.getMeasures().getSolutionCount() > 0);
    }

    @Test
    public void testRandom() {
        Random random = new Random();
//        int seed= 108;{
        for (int seed = 0; seed < 200; seed++) {
            System.out.println(seed);
            random.setSeed(seed);
            int n = 1+ random.nextInt(6);
            int m = 1+ random.nextInt(4);
            //solver 1
            Solver solver = new Solver();
            {
                IntVar[] vars = VariableFactory.boundedArray("vars", n, 0, m-1, solver);
                IntVar[] cards = VariableFactory.boundedArray("cards", m, 0, n, solver);
                solver.post(GlobalCardinality.make(vars, cards, 0, solver));
//                solver.set(StrategyFactory.random(ArrayUtils.append(vars, cards), solver.getEnvironment(), seed));
                solver.set(StrategyFactory.presetI(ArrayUtils.append(vars, cards), solver.getEnvironment()));
            }
            // reformulation
            Solver ref = new Solver();
            {
                IntVar[] vars = VariableFactory.boundedArray("vars", n, 0, m-1, ref);
                IntVar[] cards = VariableFactory.boundedArray("cards", m, 0, n, ref);
                ref.post(GlobalCardinality.reformulate(vars, cards, 0, ref));
                ref.set(StrategyFactory.presetI(ArrayUtils.append(vars, cards), ref.getEnvironment()));
            }
//            SearchMonitorFactory.log(solver, false, true);
            solver.findAllSolutions();
            ref.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount());

        }
    }

    @Test
    public void testRandom2() {
        Random random = new Random();
        for (int seed = 0; seed < 200; seed++) {
            System.out.println(seed);
            random.setSeed(seed);
            int n = 1+ random.nextInt(6);
            int m = 1+ random.nextInt(4);
            int[] min = new int[m];
            int[] max = new int[m];
            Arrays.fill(min, 0);
            Arrays.fill(max, n);
            //solver 1
            Solver solver = new Solver();
            {
                IntVar[] vars = VariableFactory.boundedArray("vars", n, 0, m-1, solver);
                solver.post(GlobalCardinality.make(vars, min, max, 0, GlobalCardinality.Consistency.BC, solver));
//                solver.set(StrategyFactory.random(ArrayUtils.append(vars, cards), solver.getEnvironment(), seed));
                solver.set(StrategyFactory.presetI(vars, solver.getEnvironment()));
            }
            // reformulation
            Solver ref = new Solver();
            {
                IntVar[] vars = VariableFactory.boundedArray("vars", n, 0, m-1, ref);
                ref.post(GlobalCardinality.reformulate(vars, min, max, 0, ref));
                ref.set(StrategyFactory.presetI(vars, ref.getEnvironment()));
            }
//            SearchMonitorFactory.log(solver, false, true);
            solver.findAllSolutions();
            ref.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount());

        }
    }
}
