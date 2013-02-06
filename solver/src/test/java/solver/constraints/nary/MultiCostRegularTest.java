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
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.constraints.nary.automata.FA.FiniteAutomaton;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/07/11
 */
public class MultiCostRegularTest {

    /**
     * Model variable for the
     */
    IntVar[] sequence;

    /**
     * Bounds within whoms the accepted schedule must cost.
     */
    IntVar[] bounds;

    /**
     * The cost matrix which gives assgnement cost (used for counters too)
     */
    int[][][][] costMatrix;

    /**
     * Automaton which embeds the work regulations that may be
     * represented by regular expressions
     */
    FiniteAutomaton auto;

    private Solver make(int period, long seed) {
        Solver solver = new Solver();
        this.sequence = VariableFactory.enumeratedArray("x", period, 0, 2, solver);
        this.bounds = new IntVar[4];
        this.bounds[0] = VariableFactory.bounded("z_0", 0, 80, solver);
        this.bounds[1] = VariableFactory.bounded("day", 0, 28, solver);
        this.bounds[2] = VariableFactory.bounded("night", 0, 28, solver);
        this.bounds[3] = VariableFactory.bounded("rest", 0, 28, solver);

        this.auto = new FiniteAutomaton();
        int idx = this.auto.addState();
        this.auto.setInitialState(idx);
        this.auto.setFinal(idx);
        idx = this.auto.addState();
        int DAY = 0;
        this.auto.addTransition(this.auto.getInitialState(), idx, DAY);
        int next = this.auto.addState();
        int NIGHT = 1;
        this.auto.addTransition(idx, next, DAY, NIGHT);
        int REST = 2;
        this.auto.addTransition(next, auto.getInitialState(), REST);
        auto.addTransition(auto.getInitialState(), next, NIGHT);

        int[][][][] csts = new int[period][3][4][this.auto.getNbStates()];
        for (int i = 0; i < csts.length; i++) {
            for (int j = 0; j < csts[i].length; j++) {
                for (int r = 0; r < csts[i][j].length; r++) {
                    if (r == 0) {
                        if (j == DAY)
                            csts[i][j][r] = new int[]{3, 5, 0};
                        else if (j == NIGHT)
                            csts[i][j][r] = new int[]{8, 9, 0};
                        else if (j == REST)
                            csts[i][j][r] = new int[]{0, 0, 2};
                    } else if (r == 1) {
                        if (j == DAY)
                            csts[i][j][r] = new int[]{1, 1, 0};
                    } else if (r == 2) {
                        if (j == NIGHT)
                            csts[i][j][r] = new int[]{1, 1, 0};
                    } else if (r == 3) {
                        if (j != REST)
                            csts[i][j][r] = new int[]{1, 1, 0};
                    }
                }
            }
        }
        this.costMatrix = csts;

        solver.post(IntConstraintFactory.multicost_regular(sequence, bounds, auto, costMatrix));
//        solver.set(StrategyFactory.presetI(ArrayUtils.append(sequence, bounds), solver.getEnvironment()));
        solver.set(IntStrategyFactory.random(ArrayUtils.append(sequence, bounds), solver.getEnvironment(), seed));
        return solver;
    }


    @Test(groups = "10s")
    public void test1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 2000; i++) {
            Solver solver = make(5, i + seed);
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4, "seed:" + (seed + i));
        }
    }

    @Test(groups = "10s")
    public void test2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 2000; i++) {
            Solver solver = make(7, i);
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 6, "seed:" + (seed + i));
        }
    }

    @Test(groups = "10s")
    public void test3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 2000; i++) {
            Solver solver = make(14, i);
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 141, "seed:" + (seed + i));
        }
    }

    @Test(groups = "10s")
    public void test4() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 2000; i++) {
            Solver solver = make(21, i);
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 85, "seed:" + (seed + i));
        }
    }

}
