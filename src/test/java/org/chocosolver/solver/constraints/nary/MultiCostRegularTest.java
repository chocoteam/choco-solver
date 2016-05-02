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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton.makeMultiResources;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/07/11
 */
public class MultiCostRegularTest {

    private Model make(int period, long seed) {

        Model model = new Model();
        IntVar[] sequence = model.intVarArray("x", period, 0, 2, false);
        IntVar[] bounds = new IntVar[4];
        bounds[0] = model.intVar("z_0", 0, 80, true);
        bounds[1] = model.intVar("day", 0, 28, true);
        bounds[2] = model.intVar("night", 0, 28, true);
        bounds[3] = model.intVar("rest", 0, 28, true);

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
        ICostAutomaton costAutomaton = makeMultiResources(auto, costMatrix, bounds);
        model.multiCostRegular(sequence, bounds, costAutomaton).post();
//        solver.set(StrategyFactory.presetI(ArrayUtils.append(sequence, bounds), solver.getEnvironment()));
        model.getSolver().set(randomSearch(append(sequence, bounds), seed));
        return model;
    }


    @Test(groups="10s", timeOut=60000)
    public void test1() {
        long seed = 0;
        for (int i = 0; i < 2000; i++) {
            Model model = make(5, i + seed);
            while (model.solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 4, "seed:" + (seed + i));
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test2() {
        long seed = 0;
        for (int i = 0; i < 2000; i++) {
            Model model = make(7, i);
            while (model.solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 6, "seed:" + (seed + i));
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void test3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 2000; i++) {
            Model model = make(14, i);
            while (model.solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 141, "seed:" + (seed + i));
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void test4() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 2000; i++) {
            Model model = make(21, i);
            while (model.solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 85, "seed:" + (seed + i));
        }
    }

}
