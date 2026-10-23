/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.utils.Counter;
import org.chocosolver.solver.constraints.nary.automata.FA.utils.CounterState;
import org.chocosolver.solver.constraints.nary.automata.FA.utils.ICounter;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainBest;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton.makeSingleResource;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
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
        Model model = new Model();

        int n = 10;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = model.intVar("z", 3, 4, true);


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
        model.costRegular(vars, cost, makeSingleResource(auto, costs, cost.getLB(), cost.getUB())).post();
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 9280);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSimpleAutoCostAutomaton() {
        Model model = new Model();

        int n = 10;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = model.intVar("z", 3, 4, true);

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

        model.costRegular(vars, cost, auto).post();
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 9280);
    }

    @Test(groups="10s", timeOut=60000)
    public void ccostregular2() {
        Model model = new Model();

        int n = 28;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = model.intVar("z", 0, 4, true);

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

        model.costRegular(vars, cost, makeSingleResource(auto, costs, cost.getLB(), cost.getUB())).post();
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 229376);
    }

    @Test(groups="10s", timeOut=60000)
    public void ccostregular2WithCostAutomaton() {
        Model model = new Model();

        int n = 28;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = model.intVar("z", 0, 4, true);

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

        model.costRegular(vars, cost, cauto).post();
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 229376);
    }

    @Test(groups="10s", timeOut=60000)
    public void isCorrect() {
        Model model = new Model();

        int n = 12;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = model.intVar("z", 10, 10, true);


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

        model.costRegular(vars, cost, makeSingleResource(auto, costs, cost.getLB(), cost.getUB())).post();
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 67584);
//        assertEquals(124927,s.getNodeCount());

    }

    @Test(groups="10s", timeOut=60000)
    public void isCorrectWithCostAutomaton() {
        Model model = new Model();

        int n = 12;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = model.intVar("z", 10, 10, true);

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

        model.costRegular(vars, cost, auto).post();
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 67584);

//        assertEquals(124927, s.getNodeCount());
    }

    @Test(groups="10s", timeOut=60000)
    public void isCorrect2() {
        Model model = new Model();

        int n = 13;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = model.intVar("z", 4, 6, true);

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

        model.costRegular(vars, cost, makeSingleResource(auto, costs, cost.getLB(), cost.getUB())).post();
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 149456);
    }

    @Test(groups="10s", timeOut=60000)
    public void isCorrect2WithCostAutomaton() {

        Model model = new Model();

        int n = 13;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = model.intVar("z", 4, 6, true);

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

        model.costRegular(vars, cost, auto).post();
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 149456);
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

        Model model = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
        }
        IntVar cost = model.intVar("z", n / 2, n / 2 + 1, true);

        model.costRegular(vars, cost, makeSingleResource(auto, c2, cost.getLB(), cost.getUB())).post();
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 64008);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJM1(){
        Model model = new Model();
        int taille = 2;
        ICostAutomaton auto = makeCostAutomaton(taille);
        IntVar[] vars = model.intVarArray("x",taille,0,1);
        IntVar cost = model.intVar("c",0,taille);

        model.costRegular(vars,cost,auto).post();
        model.post(model.arithm(vars[0],"=",1));
        model.post(model.arithm(vars[1],"=",1));
        model.getSolver().setSearch(Search.inputOrderLBSearch(vars));
        model.getSolver().solve();
        Assert.assertTrue(cost.isInstantiated());
    }

    static ICostAutomaton makeCostAutomaton(int taille) {
        FiniteAutomaton fa = new FiniteAutomaton();
        int q0 = fa.addState();
        fa.setInitialState(q0);
        fa.setFinal(q0);
        fa.addTransition(q0, q0, 0,1); // transition 1 de 0 vers 0

        int[][] costmatrix = new int[taille][2];
        for (int i = 0 ; i < taille ; ++i)
            for (int j = 0 ; j < 2 ; ++j)
                costmatrix[i][j] = (j==1 ? 1 : 0);

        return CostAutomaton.makeSingleResource(fa,costmatrix,0,taille);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testJavadocExample() {
        // the example of the Javadoc of CostAutomaton and of the documentation (#305)
        Model model = new Model();
        IntVar[] vars = model.intVarArray("x", 3, 0, 2);
        IntVar cost = model.intVar("cost", 0, 10);
        int[][] costs = new int[3][];
        Arrays.fill(costs, new int[]{0, 1, 5});
        ICostAutomaton auto = CostAutomaton.makeSingleResource(new FiniteAutomaton("(0|1|2)*"), costs,
                cost.getLB(), cost.getUB());
        model.costRegular(vars, cost, auto).post();
        int count = 0;
        while (model.getSolver().solve()) {
            int c = 0;
            for (IntVar x : vars) {
                c += costs[0][x.getValue()];
            }
            assertEquals(cost.getValue(), c);
            count++;
        }
        // brute force: the words of length 3 over {0,1,2} whose cost is at most 10
        int expected = 0;
        for (int w = 0; w < 27; w++) {
            if (costs[0][w % 3] + costs[0][w / 3 % 3] + costs[0][w / 9] <= 10) {
                expected++;
            }
        }
        assertEquals(count, expected);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNegativeValuesInDomains() {
        // #280: values outside [0,65535] are never accepted, instead of raising an exception
        Model model = new Model();
        IntVar[] vars = model.intVarArray("x", 2, -2, 1);
        IntVar cost = model.intVar("cost", 0, 2);
        int[][] costs = {{0, 1}, {0, 1}};
        model.costRegular(vars, cost, makeSingleResource(new FiniteAutomaton("(0|1)*"), costs, 0, 2)).post();
        assertEquals(model.getSolver().findAllSolutions().size(), 4);
    }

    private static long countSolutionsOfRandomCostRegular(long seed, IntValueSelector valueSelector) {
        Random rnd = new Random(seed);
        int n = 4 + rnd.nextInt(4);
        int q = 2 + rnd.nextInt(4);
        FiniteAutomaton auto = new FiniteAutomaton();
        for (int i = 0; i < q; i++) {
            auto.addState();
        }
        auto.setInitialState(0);
        for (int i = 0; i < q; i++) {
            if (rnd.nextInt(3) > 0) {
                auto.setFinal(i);
            }
            for (int v = 0; v < 3; v++) {
                if (rnd.nextInt(4) > 0) {
                    auto.addTransition(i, rnd.nextInt(q), v);
                }
            }
        }
        if (auto.getFinalStates().isEmpty()) {
            auto.setFinal(0);
        }
        int[][] costs = new int[n][3];
        for (int[] row : costs) {
            for (int v = 0; v < 3; v++) {
                row[v] = rnd.nextInt(6);
            }
        }
        Model model = new Model();
        IntVar[] vars = model.intVarArray("x", n, 0, 2);
        int lo = rnd.nextInt(5 * n / 2 + 1);
        int hi = lo + rnd.nextInt(8);
        IntVar cost = model.intVar("z", lo, hi);
        try {
            model.costRegular(vars, cost, makeSingleResource(auto, costs, lo, hi)).post();
        } catch (SolverException e) {
            // no word of length n is accepted
            return -1;
        }
        // another propagator that may fail while arcs are waiting to be removed
        model.sum(vars, "<=", rnd.nextInt(2 * n + 1)).post();
        model.getSolver().setSearch(Search.intVarSearch(new InputOrder<>(model), valueSelector,
                ArrayUtils.append(vars, new IntVar[]{cost})));
        // a constant objective, required by IntDomainBest, with a non-strict cut to enumerate all the solutions
        model.setObjective(Model.MAXIMIZE, model.intVar("obj", 0));
        model.getSolver().getObjectiveManager().setWalkingDynamicCut();
        return model.getSolver().streamSolutions().count();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testLookAhead() {
        // #1163: the look-ahead of IntDomainBest pushes and cancels worlds without counting backtracks,
        // the arcs waiting to be removed in a cancelled world must not be removed in the next one
        for (long seed = 0; seed < 100; seed++) {
            assertEquals(countSolutionsOfRandomCostRegular(seed, new IntDomainBest()),
                    countSolutionsOfRandomCostRegular(seed, new IntDomainMin()), "seed: " + seed);
        }
    }
}
