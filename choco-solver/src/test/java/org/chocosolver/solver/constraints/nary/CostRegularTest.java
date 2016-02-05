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
import org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.utils.Counter;
import org.chocosolver.solver.constraints.nary.automata.FA.utils.CounterState;
import org.chocosolver.solver.constraints.nary.automata.FA.utils.ICounter;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton.makeSingleResource;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.lexico_LB;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class CostRegularTest {

    @Test(groups="1s", timeOut=60000)
    public void testSimpleAuto() {
        Solver solver = new Solver();

        int n = 10;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = solver.intVar("z", 3, 4, true);


        FiniteAutomaton auto = new FiniteAutomaton();
        int start = auto.addState();
        int end = auto.addState();
        auto.setInitialState(start);
        auto.setFinal(start);
        auto.setFinal(end);

        auto.addTransition(start, start, 0, 1);
        auto.addTransition(start, end, 2);

        auto.addTransition(end, start, 2);
        auto.addTransition(end, start, 0, 1);

        int[][][] costs = new int[n][3][2];
        for (int i = 0; i < costs.length; i++) {
            costs[i][0][1] = 1;
            costs[i][1][1] = 1;
        }
        solver.costRegular(vars, cost, makeSingleResource(auto, costs, cost.getLB(), cost.getUB())).post();
        solver.set(lexico_LB(vars));

        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 9280);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSimpleAutoCostAutomaton() {
        Solver solver = new Solver();

        int n = 10;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = solver.intVar("z", 3, 4, true);

        CostAutomaton auto = new CostAutomaton();
        int start = auto.addState();
        int end = auto.addState();
        auto.setInitialState(start);
        auto.setFinal(start);
        auto.setFinal(end);

        auto.addTransition(start, start, 0, 1);
        auto.addTransition(start, end, 2);

        auto.addTransition(end, start, 2);
        auto.addTransition(end, start, 0, 1);

        int[][][] costs = new int[n][3][2];
        for (int i = 0; i < costs.length; i++) {
            costs[i][0][1] = 1;
            costs[i][1][1] = 1;
        }

        ICounter c = new CounterState(costs, 3, 4);

        auto.addCounter(c);

        solver.costRegular(vars, cost, auto).post();
        solver.set(lexico_LB(vars));

        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 9280);
    }

    @Test(groups="10s", timeOut=60000)
    public void ccostregular2() {
        Solver solver = new Solver();

        int n = 28;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = solver.intVar("z", 0, 4, true);

        // different rules are formulated as patterns that must NOT be matched by x
        List<String> forbiddenRegExps = new ArrayList<>();
        // do not end with '00' if start with '11'
        forbiddenRegExps.add("11(0|1|2)*00");
        // at most three consecutive 0
        forbiddenRegExps.add("(0|1|2)*0000(0|1|2)*");
        // no pattern '112' at position 5
        forbiddenRegExps.add("(0|1|2){4}112(0|1|2)*");
        // pattern '12' after a 0 or a sequence of 0
        forbiddenRegExps.add("(0|1|2)*02(0|1|2)*");
        forbiddenRegExps.add("(0|1|2)*01(0|1)(0|1|2)*");
        // at most three 2 on consecutive even positions
        forbiddenRegExps.add("(0|1|2)((0|1|2)(0|1|2))*2(0|1|2)2(0|1|2)2(0|1|2)*");

        // a unique automaton is built as the complement language composed of all the forbidden patterns
        FiniteAutomaton auto = new FiniteAutomaton();
        for (String reg : forbiddenRegExps) {
            FiniteAutomaton a = new FiniteAutomaton(reg);
            auto = auto.union(a);
            auto.minimize();
        }
        auto = auto.complement();
        auto.minimize();
        assertEquals(auto.getNbStates(), 54);
        // costs
        int[][] costs = new int[vars.length][3];
        for (int i = 1; i < costs.length; i += 2) {
            costs[i][0] = 1;
            costs[i][1] = 1;
        }

        solver.costRegular(vars, cost, makeSingleResource(auto, costs, cost.getLB(), cost.getUB())).post();
        solver.set(lexico_LB(vars));

        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 229376);
    }

    @Test(groups="10s", timeOut=60000)
    public void ccostregular2WithCostAutomaton() {
        Solver solver = new Solver();

        int n = 28;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = solver.intVar("z", 0, 4, true);

        // different rules are formulated as patterns that must NOT be matched by x
        List<String> forbiddenRegExps = new ArrayList<>();
        // do not end with '00' if start with '11'
        forbiddenRegExps.add("11(0|1|2)*00");
        // at most three consecutive 0
        forbiddenRegExps.add("(0|1|2)*0000(0|1|2)*");
        // no pattern '112' at position 5
        forbiddenRegExps.add("(0|1|2){4}112(0|1|2)*");
        // pattern '12' after a 0 or a sequence of 0
        forbiddenRegExps.add("(0|1|2)*02(0|1|2)*");
        forbiddenRegExps.add("(0|1|2)*01(0|1)(0|1|2)*");
        // at most three 2 on consecutive even positions
        forbiddenRegExps.add("(0|1|2)((0|1|2)(0|1|2))*2(0|1|2)2(0|1|2)2(0|1|2)*");

        // a unique automaton is built as the complement language composed of all the forbidden patterns
        FiniteAutomaton auto = new FiniteAutomaton();
        for (String reg : forbiddenRegExps) {
            FiniteAutomaton a = new FiniteAutomaton(reg);
            auto = auto.union(a);
            auto.minimize();
        }
        auto = auto.complement();
        auto.minimize();
        assertEquals(auto.getNbStates(), 54);
        // costs
        int[][] costs = new int[vars.length][3];
        for (int i = 1; i < costs.length; i += 2) {
            costs[i][0] = 1;
            costs[i][1] = 1;
        }

        ICounter c = new Counter(costs, 0, 4);
        CostAutomaton cauto = new CostAutomaton(auto, c);

        solver.costRegular(vars, cost, cauto).post();
        solver.set(lexico_LB(vars));

        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 229376);
    }

    @Test(groups="10s", timeOut=60000)
    public void isCorrect() {
        Solver solver = new Solver();

        int n = 12;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = solver.intVar("z", 10, 10, true);


        FiniteAutomaton auto = new FiniteAutomaton();
        int start = auto.addState();
        int end = auto.addState();
        auto.setInitialState(start);
        auto.setFinal(start);
        auto.setFinal(end);

        auto.addTransition(start, start, 0, 1);
        auto.addTransition(start, end, 2);

        auto.addTransition(end, start, 2);
        auto.addTransition(end, start, 0, 1);

        int[][][] costs = new int[n][3][2];
        for (int i = 0; i < costs.length; i++) {
            for (int k = 0; k < 2; k++) {
                costs[i][0][k] = 1;
                costs[i][1][k] = 1;
            }
        }

        solver.costRegular(vars, cost, makeSingleResource(auto, costs, cost.getLB(), cost.getUB())).post();
        solver.set(lexico_LB(vars));

        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 67584);
//        assertEquals(124927,s.getNodeCount());

    }

    @Test(groups="10s", timeOut=60000)
    public void isCorrectWithCostAutomaton() {
        Solver solver = new Solver();

        int n = 12;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = solver.intVar("z", 10, 10, true);

        CostAutomaton auto = new CostAutomaton();
        int start = auto.addState();
        int end = auto.addState();
        auto.setInitialState(start);
        auto.setFinal(start);
        auto.setFinal(end);

        auto.addTransition(start, start, 0, 1);
        auto.addTransition(start, end, 2);

        auto.addTransition(end, start, 2);
        auto.addTransition(end, start, 0, 1);

        int[][][] costs = new int[n][3][2];
        for (int i = 0; i < costs.length; i++) {
            for (int k = 0; k < 2; k++) {
                costs[i][0][k] = 1;
                costs[i][1][k] = 1;
            }
        }

        auto.addCounter(new CounterState(costs, 10, 10));

        solver.costRegular(vars, cost, auto).post();
        solver.set(lexico_LB(vars));

        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 67584);

//        assertEquals(124927, s.getNodeCount());
    }

    @Test(groups="10s", timeOut=60000)
    public void isCorrect2() {
        Solver solver = new Solver();

        int n = 13;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = solver.intVar("z", 4, 6, true);

        FiniteAutomaton auto = new FiniteAutomaton();
        int start = auto.addState();
        int end = auto.addState();
        auto.setInitialState(start);
        auto.setFinal(start);
        auto.setFinal(end);

        auto.addTransition(start, start, 0, 1);
        auto.addTransition(start, end, 2);

        auto.addTransition(end, start, 2);
        auto.addTransition(end, start, 0, 1);

        int[][][] costs = new int[n][3][2];
        for (int i = 0; i < costs.length; i++) {
            costs[i][0][1] = 1;
            costs[i][1][1] = 1;
        }

        solver.costRegular(vars, cost, makeSingleResource(auto, costs, cost.getLB(), cost.getUB())).post();
        solver.set(lexico_LB(vars));

        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 149456);
    }

    @Test(groups="10s", timeOut=60000)
    public void isCorrect2WithCostAutomaton() {

        Solver solver = new Solver();

        int n = 13;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = solver.intVar("z", 4, 6, true);

        CostAutomaton auto = new CostAutomaton();
        int start = auto.addState();
        int end = auto.addState();
        auto.setInitialState(start);
        auto.setFinal(start);
        auto.setFinal(end);

        auto.addTransition(start, start, 0, 1);
        auto.addTransition(start, end, 2);

        auto.addTransition(end, start, 2);
        auto.addTransition(end, start, 0, 1);

        int[][][] costs = new int[n][3][2];
        for (int i = 0; i < costs.length; i++) {
            costs[i][0][1] = 1;
            costs[i][1][1] = 1;
        }

        auto.addCounter(new CounterState(costs, 4, 6));

        solver.costRegular(vars, cost, auto).post();
        solver.set(lexico_LB(vars));

        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 149456);
    }

    @Test(groups="10s", timeOut=60000)
    public void compareVersionSpeedNew() {
        int n = 14;
        FiniteAutomaton auto = new FiniteAutomaton("(0|1|2)*(0|1)(0|1)(0|1)(0|1|2)*");

//        int[][] c1 = new int[n][3];
        int[][][] c2 = new int[n][3][auto.getNbStates()];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < auto.getNbStates(); k++) {
//                c1[i][0] = 1;
//                c1[i][1] = 2;
                c2[i][0][k] = 1;
                c2[i][1][k] = 2;
            }
        }

        Solver solver = new Solver();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = solver.intVar("z", n / 2, n / 2 + 1, true);

        solver.costRegular(vars, cost, makeSingleResource(auto, c2, cost.getLB(), cost.getUB())).post();
        solver.set(lexico_LB(vars));

        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 64008);
    }
}
