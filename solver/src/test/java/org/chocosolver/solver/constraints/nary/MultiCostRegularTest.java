/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainBest;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton.makeMultiResources;
import static org.chocosolver.solver.search.strategy.Search.intVarSearch;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
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
        model.getSolver().setSearch(randomSearch(append(sequence, bounds), seed));
        return model;
    }

    @Test(groups="10s", timeOut=60000)
    public void test0() {
		// used to fail when freeze/foreach/unfreeze was done during initial propagation (before graph initialization)
        for (int i = 0; i < 2000; i++) {
            Model model = make(7, i);
			IntVar[] vars = model.retrieveIntVars(false);
			model.arithm(vars[0],"=",0).post();
            while (model.getSolver().solve()) ;
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test1() {
        long seed = 0;
        for (int i = 0; i < 200; i++) {
            Model model = make(5, i + seed);
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 4, "seed:" + (seed + i));
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test2() {
        long seed = 0;
        for (int i = 0; i < 200; i++) {
            Model model = make(7, i);
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 6, "seed:" + (seed + i));
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void test3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            Model model = make(14, i);
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 141, "seed:" + (seed + i));
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void test4() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            Model model = make(21, i);
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 85, "seed:" + (seed + i));
        }
    }

    /**
     * Words of length 6 made of 1s but a single 0, at position 1, 2 or 3 (issue #1163).
     */
    private static FiniteAutomaton automaton1163() {
        FiniteAutomaton auto = new FiniteAutomaton();
        int start = auto.addState();
        int end = auto.addState();
        int[] before0 = new int[5];
        int[] after0 = new int[5];
        int[] zero = new int[5];
        for (int i = 0; i < 5; i++) {
            before0[i] = auto.addState();
            after0[i] = auto.addState();
            zero[i] = auto.addState();
        }
        auto.setInitialState(start);
        auto.addTransition(start, before0[0], 1);
        for (int i = 0; i < 2; i++) {
            auto.addTransition(before0[i], before0[i + 1], 1);
        }
        for (int i = 0; i < 3; i++) {
            auto.addTransition(before0[i], zero[i + 1], 0);
        }
        for (int i = 1; i < 4; i++) {
            auto.addTransition(zero[i], after0[i + 1], 1);
        }
        for (int i = 2; i < 4; i++) {
            auto.addTransition(after0[i], after0[i + 1], 1);
        }
        auto.addTransition(after0[4], end, 1);
        auto.setFinal(end);
        return auto;
    }

    private static void post1163(Model model, IntVar[] periods) {
        IntVar[] occurrences = new IntVar[]{
                model.intVar("occZero", 1, 1),
                model.intVar("occOne", 5, 5)};
        // each resource counts the occurrences of one value
        int[][][] costs = new int[periods.length][2][2];
        for (int i = 0; i < periods.length; i++) {
            costs[i][0][0] = 1;
            costs[i][1][1] = 1;
        }
        ICostAutomaton cauto = makeMultiResources(automaton1163(), costs, new int[]{1, 5}, new int[]{1, 5});
        model.multiCostRegular(periods, occurrences, cauto).post();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1163AllSolutions() {
        Model model = new Model();
        IntVar[] periods = model.intVarArray("p", 6, 0, 1);
        post1163(model, periods);
        assertEquals(model.getSolver().streamSolutions().count(), 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1163Optimization() {
        int[] gains = {33, 34, 21, 1, 49, 1};
        Model model = new Model();
        IntVar[] periods = model.intVarArray("p", 6, 0, 1);
        post1163(model, periods);
        IntVar[] gained = new IntVar[periods.length];
        for (int i = 0; i < periods.length; i++) {
            gained[i] = model.intVar("g" + i, 0, 1000);
            model.element(gained[i], new int[]{0, gains[i]}, periods[i]).post();
        }
        IntVar sum = model.intVar("sum", 0, 100000);
        model.sum(gained, "=", sum).post();
        // the look-ahead of IntDomainBest tries p[3] = 0 then p[3] = 1 by pushing and cancelling a world,
        // without counting a backtrack: the propagator must not keep the path information of the second try
        model.getSolver().setSearch(intVarSearch(new InputOrder<>(model), new IntDomainBest(),
                periods[3], periods[1], periods[2]));
        Solution best = model.getSolver().findOptimalSolution(sum, true);
        assertEquals(best.getIntVal(sum), 138);
        assertEquals(best.getIntVal(periods[3]), 0);
    }
}
