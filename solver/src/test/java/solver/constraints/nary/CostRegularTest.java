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

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.nary.automaton.CostRegular;
import solver.constraints.nary.automaton.FA.CostAutomaton;
import solver.constraints.nary.automaton.FA.FiniteAutomaton;
import solver.constraints.nary.automaton.FA.utils.Counter;
import solver.constraints.nary.automaton.FA.utils.CounterState;
import solver.constraints.nary.automaton.FA.utils.ICounter;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class CostRegularTest {

    @Test(groups = "1s")
    public void testSimpleAuto() {
        Solver solver = new Solver();

        int n = 10;
        IntVar[] vars = new IntVar[n + 1];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        vars[n] = VariableFactory.bounded("z", 3, 4, solver);


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
        solver.post(new CostRegular(vars, auto, costs, solver));
        solver.set(StrategyFactory.presetI(vars, solver.getEnvironment()));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 9280);
    }

    @Test(groups = "1s")
    public void testSimpleAutoCostAutomaton() {
        Solver solver = new Solver();

        int n = 10;
        IntVar[] vars = new IntVar[n + 1];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        vars[n] = VariableFactory.bounded("z", 3, 4, solver);

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

        solver.post(new CostRegular(vars, auto, solver));
        solver.set(StrategyFactory.presetI(vars, solver.getEnvironment()));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 9280);
    }

    @Test(groups = "1s")
    public void ccostregular2() {
        Solver solver = new Solver();

        int n = 28;
        IntVar[] vars = new IntVar[n + 1];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        vars[n] = VariableFactory.bounded("z", 0, 4, solver);

        // different rules are formulated as patterns that must NOT be matched by x
        List<String> forbiddenRegExps = new ArrayList<String>();
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
        Assert.assertEquals(auto.getNbStates(), 54);
        // costs
        int[][] costs = new int[vars.length][3];
        for (int i = 1; i < costs.length; i += 2) {
            costs[i][0] = 1;
            costs[i][1] = 1;
        }

        solver.post(new CostRegular(vars, auto, costs, solver));
        solver.set(StrategyFactory.presetI(vars, solver.getEnvironment()));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 229376);
    }

    @Test(groups = "1s")
    public void ccostregular2WithCostAutomaton() {
        Solver solver = new Solver();

        int n = 28;
        IntVar[] vars = new IntVar[n + 1];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        vars[n] = VariableFactory.bounded("z", 0, 4, solver);

        // different rules are formulated as patterns that must NOT be matched by x
        List<String> forbiddenRegExps = new ArrayList<String>();
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
        Assert.assertEquals(auto.getNbStates(), 54);
        // costs
        int[][] costs = new int[vars.length][3];
        for (int i = 1; i < costs.length; i += 2) {
            costs[i][0] = 1;
            costs[i][1] = 1;
        }

        ICounter c = new Counter(costs, 0, 4);
        CostAutomaton cauto = new CostAutomaton(auto, c);

        solver.post(new CostRegular(vars, cauto, solver));
        solver.set(StrategyFactory.presetI(vars, solver.getEnvironment()));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 229376);
    }

    @Test(groups = "1s")
    public void isCorrect() {
        Solver solver = new Solver();

        int n = 12;
        IntVar[] vars = new IntVar[n + 1];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        vars[n] = VariableFactory.bounded("z", 10, 10, solver);


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

        solver.post(new CostRegular(vars, auto, costs, solver));
        solver.set(StrategyFactory.presetI(vars, solver.getEnvironment()));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 67584);
//        assertEquals(124927,s.getNodeCount());

    }

    @Test(groups = "1s")
    public void isCorrectWithCostAutomaton() {
        Solver solver = new Solver();

        int n = 12;
        IntVar[] vars = new IntVar[n + 1];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        vars[n] = VariableFactory.bounded("z", 10, 10, solver);

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

        solver.post(new CostRegular(vars, auto, solver));
        solver.set(StrategyFactory.presetI(vars, solver.getEnvironment()));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 67584);

//        assertEquals(124927, s.getNodeCount());
    }

    @Test(groups = "1s")
    public void isCorrect2() {
        Solver solver = new Solver();

        int n = 13;
        IntVar[] vars = new IntVar[n + 1];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        vars[n] = VariableFactory.bounded("z", 4, 6, solver);

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

        solver.post(new CostRegular(vars, auto, costs, solver));
        solver.set(StrategyFactory.presetI(vars, solver.getEnvironment()));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 149456);
    }

    @Test(groups = "1s")
    public void isCorrect2WithCostAutomaton() {

        Solver solver = new Solver();

        int n = 13;
        IntVar[] vars = new IntVar[n + 1];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        vars[n] = VariableFactory.bounded("z", 4, 6, solver);

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

        solver.post(new CostRegular(vars, auto, solver));
        solver.set(StrategyFactory.presetI(vars, solver.getEnvironment()));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 149456);
    }

    @Test(groups = "1s")
    public void compareVersionSpeedNew() {
        int n = 14;
        FiniteAutomaton auto = new FiniteAutomaton("(0|1|2)*(0|1)(0|1)(0|1)(0|1|2)*");

        int[][] c1 = new int[n][3];
        int[][][] c2 = new int[n][3][auto.getNbStates()];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < auto.getNbStates(); k++) {
                c1[i][0] = 1;
                c1[i][1] = 2;

                c2[i][0][k] = 1;
                c2[i][1][k] = 2;
            }
        }

        Solver solver = new Solver();
        IntVar[] vars = new IntVar[n + 1];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        vars[n] = VariableFactory.bounded("z", n / 2, n / 2 + 1, solver);

        solver.post(new CostRegular(vars, auto, c2, solver));
        solver.set(StrategyFactory.presetI(vars, solver.getEnvironment()));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 64008);
    }
}
