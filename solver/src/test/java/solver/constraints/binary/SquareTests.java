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
package solver.constraints.binary;

import choco.checker.DomainBuilder;
import gnu.trove.set.hash.TIntHashSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Arithmetic;
import solver.constraints.IntConstraintFactory;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.Arrays;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/04/12
 */
public class SquareTests {

    @Test
    public void testEnum() {
        Random random = new Random();
        for (int seed = 0; seed < 2000; seed++) {
            random.setSeed(seed);
            int[][] dom = DomainBuilder.buildFullDomains(1, -random.nextInt(15), random.nextInt(15),
                    random, random.nextDouble(), random.nextBoolean());
            Solver view = viewE(dom, seed);
            Solver cons = consE(dom, seed);
            view.findAllSolutions();
            cons.findAllSolutions();
            Assert.assertEquals(view.getMeasures().getSolutionCount(), cons.getMeasures().getSolutionCount());
        }
    }

    private Solver viewE(int[][] dom, int seed) {
        Solver solver = new Solver();
        IntVar A = VariableFactory.enumerated("A", dom[0], solver);
        IntVar B = Views.sqr(A);
        solver.post(new Arithmetic(B, ">", 0, solver));
//        SearchMonitorFactory.log(solver, true, true);
        solver.set(StrategyFactory.random(new IntVar[]{A, B}, solver.getEnvironment(), seed));
        return solver;
    }

    private Solver consE(int[][] dom, int seed) {
        Solver solver = new Solver();
        IntVar A = VariableFactory.enumerated("A", dom[0], solver);
        TIntHashSet values = new TIntHashSet();
        for (int i = 0; i < dom[0].length; i++) {
            values.add(dom[0][i] * dom[0][i]);
        }
        int[] dom2 = values.toArray();
        Arrays.sort(dom2);
        IntVar B = VariableFactory.enumerated("X", dom2, solver);
        solver.post(IntConstraintFactory.square(B, A, solver));
        solver.post(new Arithmetic(B, ">", 0, solver));
//        SearchMonitorFactory.log(solver, true, true);
        solver.set(StrategyFactory.random(new IntVar[]{A, B}, solver.getEnvironment(), seed));
        return solver;
    }

    @Test
    public void testBound() {
        Random random = new Random();
        for (int seed = 2; seed < 2000; seed++) {
            random.setSeed(seed);
            int[][] dom = DomainBuilder.buildFullDomains(1, -random.nextInt(15), random.nextInt(15));
            Solver view = viewB(dom, seed);
            Solver cons = consB(dom, seed);
            view.findAllSolutions();
            cons.findAllSolutions();
            Assert.assertEquals(view.getMeasures().getSolutionCount(), cons.getMeasures().getSolutionCount(), "seed:" + seed);
        }
    }

    private Solver viewB(int[][] dom, int seed) {
        Solver solver = new Solver();
        int n = dom[0].length - 1;
        IntVar A = VariableFactory.bounded("A", dom[0][0], dom[0][n], solver);
        IntVar B = Views.sqr(A);
        solver.post(new Arithmetic(B, ">", 0, solver));
        SearchMonitorFactory.log(solver, true, true);
        solver.set(StrategyFactory.random(new IntVar[]{A, B}, solver.getEnvironment(), seed));
        return solver;
    }

    private Solver consB(int[][] dom, int seed) {
        Solver solver = new Solver();
        int n = dom[0].length - 1;
        IntVar A = VariableFactory.bounded("A", dom[0][0], dom[0][n], solver);
        int[] dom2 = new int[2];
        dom2[0] = dom[0][n] < 0 ? dom[0][n] * dom[0][n] : 0;
        dom2[1] = Math.max(dom[0][0] * dom[0][0], dom[0][n] * dom[0][1]);
        IntVar B = VariableFactory.bounded("B", dom2[0], dom2[1], solver);
        solver.post(IntConstraintFactory.square(B, A, solver));
        solver.post(new Arithmetic(B, ">", 0, solver));
        SearchMonitorFactory.log(solver, true, true);
        solver.set(StrategyFactory.random(new IntVar[]{A, B}, solver.getEnvironment(), seed));
        return solver;
    }


}
