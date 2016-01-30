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
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/07/11
 */
public class MultiCostRegularTest {

    private Solver make(int period, long seed) {

        Solver solver = new Solver();
        IntVar[] sequence = VariableFactory.enumeratedArray("x", period, 0, 2, solver);
        IntVar[] bounds = new IntVar[4];
        bounds[0] = VariableFactory.bounded("z_0", 0, 80, solver);
        bounds[1] = VariableFactory.bounded("day", 0, 28, solver);
        bounds[2] = VariableFactory.bounded("night", 0, 28, solver);
        bounds[3] = VariableFactory.bounded("rest", 0, 28, solver);

        FiniteAutomaton auto = new FiniteAutomaton();
        int idx = auto.addState();
        auto.setInitialState(idx);
        auto.setFinal(idx);
        idx = auto.addState();
        int DAY = 0;
        auto.addTransition(auto.getInitialState(), idx, DAY);
        int next = auto.addState();
        int NIGHT = 1;
        auto.addTransition(idx, next, DAY, NIGHT);
        int REST = 2;
        auto.addTransition(next, auto.getInitialState(), REST);
        auto.addTransition(auto.getInitialState(), next, NIGHT);

        int[][][][] costMatrix = new int[period][3][4][auto.getNbStates()];
        for (int i = 0; i < costMatrix.length; i++) {
            for (int j = 0; j < costMatrix[i].length; j++) {
                for (int r = 0; r < costMatrix[i][j].length; r++) {
                    if (r == 0) {
                        if (j == DAY)
                            costMatrix[i][j][r] = new int[]{3, 5, 0};
                        else if (j == NIGHT)
                            costMatrix[i][j][r] = new int[]{8, 9, 0};
                        else if (j == REST)
                            costMatrix[i][j][r] = new int[]{0, 0, 2};
                    } else if (r == 1) {
                        if (j == DAY)
                            costMatrix[i][j][r] = new int[]{1, 1, 0};
                    } else if (r == 2) {
                        if (j == NIGHT)
                            costMatrix[i][j][r] = new int[]{1, 1, 0};
                    } else if (r == 3) {
                        if (j != REST)
                            costMatrix[i][j][r] = new int[]{1, 1, 0};
                    }
                }
            }
        }
        ICostAutomaton costAutomaton = CostAutomaton.makeMultiResources(auto, costMatrix, bounds);
        solver.post(IntConstraintFactory.multicost_regular(sequence, bounds, costAutomaton));
//        solver.set(StrategyFactory.presetI(ArrayUtils.append(sequence, bounds), solver.getEnvironment()));
        solver.set(IntStrategyFactory.random_bound(ArrayUtils.append(sequence, bounds), seed));
        return solver;
    }


    @Test(groups = "10s", timeOut=10000)
    public void test1() {
        long seed = 0;
        for (int i = 0; i < 2000; i++) {
            Solver solver = make(5, i + seed);
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4, "seed:" + (seed + i));
        }
    }

    @Test(groups = "10s", timeOut=10000)
    public void test2() {
        long seed = 0;
        for (int i = 0; i < 2000; i++) {
            Solver solver = make(7, i);
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 6, "seed:" + (seed + i));
        }
    }

    @Test(groups = "1m", timeOut=60000)
    public void test3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 2000; i++) {
            Solver solver = make(14, i);
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 141, "seed:" + (seed + i));
        }
    }

    @Test(groups = "1m", timeOut=60000)
    public void test4() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 2000; i++) {
            Solver solver = make(21, i);
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 85, "seed:" + (seed + i));
        }
    }

}
