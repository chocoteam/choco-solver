/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Corner cases of {@link PropSimplex}: models the linear relaxation sees little or nothing of.
 *
 * @author Charles Prud'homme
 */
public class PropSimplexTest {

    private static Model model(boolean relaxation) {
        return new Model(SettingsBuilder.init()
                .setLinearRelaxation(relaxation ? ILinearRelaxation.Solver.OJALGO : null).build());
    }

    private static int optimum(Model model, IntVar objective) {
        Integer best = null;
        while (model.getSolver().solve()) {
            best = objective.getValue();
        }
        Assert.assertNotNull(best, "no solution");
        return best;
    }

    /**
     * The objective variable only belongs to a half-reified constraint, which the LP leaves out.
     */
    @Test(groups = "1s")
    public void testObjectiveOutOfTheRelaxation() {
        int[] optima = new int[2];
        for (int i = 0; i < 2; i++) {
            Model model = model(i == 1);
            IntVar x = model.intVar("x", 0, 5);
            IntVar y = model.intVar("y", 0, 5);
            IntVar obj = model.intVar("obj", 0, 20);
            model.arithm(x, "+", y, "<=", 7).post();
            BoolVar b = model.boolVar("b");
            model.scalar(new IntVar[]{x, y}, new int[]{2, 1}, "=", obj).impliedBy(b);
            model.arithm(b, "=", 1).post();
            model.setObjective(Model.MAXIMIZE, obj);
            optima[i] = optimum(model, obj);
        }
        Assert.assertEquals(optima[0], 12);
        Assert.assertEquals(optima[1], optima[0]);
    }

    /**
     * No constraint of the model has a linear form.
     */
    @Test(groups = "1s")
    public void testNothingToRelax() {
        int[] optima = new int[2];
        for (int i = 0; i < 2; i++) {
            Model model = model(i == 1);
            IntVar x = model.intVar("x", 1, 5);
            IntVar y = model.intVar("y", 1, 5);
            IntVar obj = model.intVar("obj", 0, 30);
            model.times(x, y, obj).post();
            model.arithm(x, "!=", y).post();
            model.setObjective(Model.MAXIMIZE, obj);
            optima[i] = optimum(model, obj);
        }
        Assert.assertEquals(optima[0], 20);
        Assert.assertEquals(optima[1], optima[0]);
    }

    /**
     * A satisfaction model: the LP only detects infeasibility.
     */
    @Test(groups = "1s")
    public void testSatisfaction() {
        long[] counts = new long[2];
        for (int i = 0; i < 2; i++) {
            Model model = model(i == 1);
            IntVar[] xs = model.intVarArray("x", 4, 0, 3);
            model.sum(xs, "=", 6).post();
            model.scalar(xs, new int[]{3, -2, 1, 2}, "<=", 4).post();
            while (model.getSolver().solve()) {
                counts[i]++;
            }
            if (i == 1) {
                Assert.assertNotNull(PropSimplex.find(model).getRelaxation());
            }
        }
        Assert.assertTrue(counts[0] > 0);
        Assert.assertEquals(counts[1], counts[0]);
    }

    /**
     * A small maximisation model, and two constraints that can be posted or unposted afterwards.
     */
    private static final class Knapsack {
        final Model model;
        final IntVar[] xs;
        final IntVar obj;
        final Constraint capacity;
        final Constraint cut;

        Knapsack(boolean relaxation) {
            model = model(relaxation);
            xs = model.intVarArray("x", 5, 0, 3);
            obj = model.intVar("obj", 0, 200);
            model.scalar(xs, new int[]{7, 5, 9, 4, 6}, "=", obj).post();
            model.scalar(xs, new int[]{3, 2, 5, 2, 3}, "<=", 17).post();
            capacity = model.scalar(xs, new int[]{4, 1, 3, 5, 2}, "<=", 12);
            cut = model.sum(xs, "<=", 4);
            model.setObjective(Model.MAXIMIZE, obj);
        }

        int solve() {
            model.getSolver().reset();
            return optimum(model, obj);
        }
    }

    /**
     * Constraints are posted and unposted between two resolutions: the LP must follow.
     * <p>
     * Without clause learning: as of today, solving a model a second time after {@code Solver#reset()} fails with
     * it, whatever the linear relaxation.
     */
    @Test(groups = "1s")
    public void testConstraintsChangeBetweenResolutions() {
        Knapsack plain = new Knapsack(false);
        Knapsack relaxed = new Knapsack(true);
        for (Knapsack k : new Knapsack[]{plain, relaxed}) {
            k.capacity.post();
        }
        int first = plain.solve();
        Assert.assertEquals(relaxed.solve(), first);
        // a constraint is added
        plain.cut.post();
        relaxed.cut.post();
        int second = plain.solve();
        Assert.assertTrue(second < first);
        Assert.assertEquals(relaxed.solve(), second);
        // a constraint is removed: its row must leave the LP
        plain.model.unpost(plain.cut);
        relaxed.model.unpost(relaxed.cut);
        Assert.assertEquals(plain.solve(), first);
        Assert.assertEquals(relaxed.solve(), first);
        // one constraint replaces another: the number of constraints does not change
        for (Knapsack k : new Knapsack[]{plain, relaxed}) {
            k.model.unpost(k.capacity);
            k.cut.post();
        }
        int third = plain.solve();
        Assert.assertEquals(relaxed.solve(), third);
        // nothing changes: the LP is only built again because the resolution starts again
        Assert.assertEquals(relaxed.solve(), third);
        Assert.assertTrue(PropSimplex.find(relaxed.model).getStatistics().contains("builds=5,"),
                PropSimplex.find(relaxed.model).getStatistics());
    }

    /**
     * A constraint is posted during the search: the LP, which is only built at the root node, does not see it but
     * remains a relaxation of the model.
     */
    @Test(groups = "1s")
    public void testConstraintPostedDuringSearch() {
        long[] counts = new long[2];
        for (int i = 0; i < 2; i++) {
            Model model = model(i == 1);
            IntVar[] xs = model.intVarArray("x", 5, 0, 5);
            // its rows are computed from the bounds of the variables
            model.allDifferent(xs).post();
            model.scalar(xs, new int[]{2, -1, 3, 1, -2}, "<=", 9).post();
            boolean[] posted = {false};
            model.getSolver().plugMonitor((IMonitorSolution) () -> {
                if (!posted[0]) {
                    posted[0] = true;
                    // always holds
                    model.sum(xs, ">=", -100).post();
                }
            });
            model.getSolver().setSearch(Search.inputOrderLBSearch(xs));
            while (model.getSolver().solve()) {
                counts[i]++;
            }
            Assert.assertTrue(posted[0]);
            if (i == 1) {
                String statistics = PropSimplex.find(model).getStatistics();
                Assert.assertTrue(statistics.contains("builds=1,"), statistics);
                Assert.assertTrue(statistics.contains("stale=0,"), statistics);
            }
        }
        Assert.assertTrue(counts[0] > 1);
        Assert.assertEquals(counts[1], counts[0]);
    }

    /**
     * The resolution helpers that solve the model several times, posting and unposting constraints in between.
     */
    @Test(groups = "1s")
    public void testAllOptimalSolutions() {
        List<List<String>> found = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Knapsack k = new Knapsack(i == 1);
            k.capacity.post();
            k.model.clearObjective();
            // a coarse objective, to get several optimal solutions
            IntVar coarse = k.model.intVar("coarse", 0, 20);
            k.model.arithm(k.obj, "/", k.model.intVar(10), "=", coarse).post();
            List<String> solutions = new ArrayList<>();
            for (Solution s : k.model.getSolver().findAllOptimalSolutions(coarse, true)) {
                solutions.add(s.getIntVal(coarse) + ":" + Arrays.toString(Arrays.stream(k.xs).mapToInt(s::getIntVal).toArray()));
            }
            Collections.sort(solutions);
            found.add(solutions);
            // the model is back to its initial state and can be solved again
            k.model.setObjective(Model.MAXIMIZE, k.obj);
            solutions.add("then " + k.solve());
        }
        Assert.assertTrue(found.get(0).size() > 2, found.get(0).toString());
        Assert.assertEquals(found.get(1), found.get(0));
    }

    @Test(groups = "1s")
    public void testStreamOptimalSolutions() {
        long[] counts = new long[2];
        for (int i = 0; i < 2; i++) {
            Knapsack k = new Knapsack(i == 1);
            k.capacity.post();
            k.model.clearObjective();
            IntVar coarse = k.model.intVar("coarse", 0, 20);
            k.model.arithm(k.obj, "/", k.model.intVar(10), "=", coarse).post();
            counts[i] = k.model.getSolver().streamOptimalSolutions(coarse, true).count();
        }
        Assert.assertTrue(counts[0] > 1);
        Assert.assertEquals(counts[1], counts[0]);
    }

    @Test(groups = "1s")
    public void testLexOptimalSolution() {
        String[] found = new String[2];
        for (int i = 0; i < 2; i++) {
            Knapsack k = new Knapsack(i == 1);
            k.capacity.post();
            k.model.clearObjective();
            IntVar weight = k.model.intVar("weight", 0, 100);
            k.model.scalar(k.xs, new int[]{1, 3, 1, 2, 2}, "=", weight).post();
            Solution s = k.model.getSolver().findLexOptimalSolution(new IntVar[]{weight, k.obj}, true);
            Assert.assertNotNull(s);
            found[i] = s.getIntVal(weight) + ", " + s.getIntVal(k.obj);
        }
        Assert.assertEquals(found[1], found[0]);
    }

    @Test(groups = "1s")
    public void testParetoFront() {
        List<List<String>> found = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Knapsack k = new Knapsack(i == 1);
            k.capacity.post();
            k.model.clearObjective();
            IntVar weight = k.model.intVar("weight", -100, 0);
            k.model.scalar(k.xs, new int[]{-1, -3, -1, -2, -2}, "=", weight).post();
            List<String> front = new ArrayList<>();
            for (Solution s : k.model.getSolver().findParetoFront(new IntVar[]{weight, k.obj}, true)) {
                front.add(s.getIntVal(weight) + ", " + s.getIntVal(k.obj));
            }
            Collections.sort(front);
            found.add(front);
        }
        Assert.assertTrue(found.get(0).size() > 2, found.get(0).toString());
        Assert.assertEquals(found.get(1), found.get(0));
    }

    /**
     * QuickXPlain disables constraints between resolutions: a disabled constraint must not contribute to the LP,
     * otherwise a satisfiable subset is found inconsistent.
     */
    @Test(groups = "1s")
    public void testMinimumConflictingSetWithDisabledConstraints() {
        List<List<String>> found = new ArrayList<>();
        for (boolean relaxation : new boolean[]{false, true}) {
            Model model = model(relaxation);
            IntVar x = model.intVar("x", 0, 10);
            IntVar y = model.intVar("y", 0, 10);
            List<Constraint> conflicting = List.of(
                    model.arithm(x, "+", y, ">=", 15),
                    model.arithm(x, "<=", y),
                    model.arithm(y, "<=", 6),
                    model.arithm(x, "+", y, "<=", 20));
            conflicting.forEach(Constraint::post);
            List<String> mcs = new ArrayList<>();
            for (Constraint c : model.getSolver().findMinimumConflictingSet(conflicting)) {
                mcs.add(String.valueOf(conflicting.indexOf(c)));
            }
            Collections.sort(mcs);
            found.add(mcs);
        }
        Assert.assertEquals(found.get(0), List.of("0", "1", "2"));
        Assert.assertEquals(found.get(1), found.get(0));
    }

    @Test(groups = "1s")
    public void testOptimalSolutionWithBounds() {
        int[] best = new int[2];
        for (int i = 0; i < 2; i++) {
            Knapsack k = new Knapsack(i == 1);
            k.capacity.post();
            int[] value = {-1};
            boolean found = k.model.getSolver().findOptimalSolutionWithBounds(k.obj,
                    () -> new int[]{k.obj.getValue() + 1, 200},
                    (init, last) -> init,
                    new NodeCounter(k.model, 100_000),
                    run -> run >= 2,
                    () -> value[0] = k.obj.getValue());
            Assert.assertTrue(found);
            best[i] = value[0];
        }
        Assert.assertEquals(best[1], best[0]);
        Knapsack reference = new Knapsack(false);
        reference.capacity.post();
        Assert.assertEquals(best[0], reference.solve());
    }

    /**
     * The objective variable is a real one: the LP is solved for feasibility only.
     */
    @Test(groups = "1s")
    public void testRealObjective() {
        int[] best = new int[2];
        for (int i = 0; i < 2; i++) {
            Knapsack k = new Knapsack(i == 1);
            k.capacity.post();
            k.model.clearObjective();
            RealVar real = k.model.realIntView(k.obj, 0.1);
            k.model.setObjective(Model.MAXIMIZE, real);
            k.model.getSolver().setSearch(Search.inputOrderUBSearch(k.xs));
            best[i] = -1;
            while (k.model.getSolver().solve()) {
                best[i] = k.obj.getValue();
            }
        }
        Assert.assertTrue(best[0] > 0);
        Assert.assertEquals(best[1], best[0]);
    }

    /**
     * With the backoff frequency (the default), the LP is solved less often but the results do not change.
     */
    @Test(groups = "1s")
    public void testBackoff() {
        System.clearProperty(PropSimplex.FREQUENCY_PROPERTY);
        try {
            // a loose LP, which deduces nothing: most propagations are skipped
            long[] counts = new long[2];
            String statistics = null;
            for (int i = 0; i < 2; i++) {
                Model model = model(i == 1);
                IntVar[] xs = model.intVarArray("x", 6, 0, 3);
                model.sum(xs, "<=", 30).post();
                model.allDifferent(new IntVar[]{xs[0], xs[1], xs[2]}).post();
                model.getSolver().setSearch(Search.inputOrderLBSearch(xs));
                while (model.getSolver().solve()) {
                    counts[i]++;
                }
                if (i == 1) {
                    statistics = PropSimplex.find(model).getStatistics();
                }
            }
            Assert.assertEquals(counts[1], counts[0]);
            Assert.assertFalse(statistics.contains("backedOff=0,"), statistics);
            // a tight LP: the optima do not change
            Knapsack plain = new Knapsack(false);
            Knapsack relaxed = new Knapsack(true);
            plain.capacity.post();
            relaxed.capacity.post();
            Assert.assertEquals(relaxed.solve(), plain.solve());
        } finally {
            System.clearProperty(PropSimplex.FREQUENCY_PROPERTY);
        }
    }

    @Test(groups = "1s")
    public void testTolerance() {
        Assert.assertEquals(PropSimplex.tolerance(0.), 1e-8);
        Assert.assertEquals(PropSimplex.tolerance(-3.), 1e-8);
        Assert.assertEquals(PropSimplex.tolerance(-1e12), 1e3, 1e-6);
    }
}
