/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Check the explanations of {@link PropSimplex} on small random linear models: each of them, replayed alone on
 * a fresh model without relaxation, must be a consequence of the constraints.
 *
 * @author Charles Prud'homme
 */
public class PropSimplexExplanationTest {

    /**
     * An explanation: bounds of some variables imply a bound of a target variable, or a failure.
     */
    private static final class Explanation {
        final Map<String, Integer> lowerBounds = new HashMap<>();
        final Map<String, Integer> upperBounds = new HashMap<>();
        String target;
        int nbVars;
        boolean lower;
        int newBound;
    }

    private int nbFailures = 0;
    private int nbShortFailures = 0;
    private long nbFailureBounds = 0;
    private long nbFailureAllBounds = 0;
    private int nbObjectiveBounds = 0;
    private int nbFilteredBounds = 0;

    /**
     * A random model: a few variables, linear constraints over them, and a linear objective.
     *
     * @return the variables, the objective variable being the last one
     */
    private static IntVar[] build(Model model, long seed, boolean[] maximize) {
        Random rnd = new Random(seed);
        int n = 6 + rnd.nextInt(3);
        IntVar[] xs = new IntVar[n];
        for (int i = 0; i < n; i++) {
            int lb = rnd.nextInt(4) - 1;
            xs[i] = model.intVar("x" + i, lb, lb + 2 + rnd.nextInt(5));
        }
        int m = 4 + rnd.nextInt(4);
        for (int c = 0; c < m; c++) {
            int[] coeffs = new int[n];
            int mid = 0;
            for (int i = 0; i < n; i++) {
                coeffs[i] = rnd.nextInt(3) == 0 ? 0 : rnd.nextInt(11) - 5;
                mid += coeffs[i] * (xs[i].getLB() + xs[i].getUB()) / 2;
            }
            String op = new String[]{"<=", ">=", "<=", ">="}[rnd.nextInt(4)];
            model.scalar(xs, coeffs, op, mid + rnd.nextInt(7) - 3).post();
        }
        int[] costs = new int[n];
        for (int i = 0; i < n; i++) {
            costs[i] = rnd.nextInt(13) - 6;
        }
        IntVar obj = model.intVar("obj", -200, 200);
        model.scalar(xs, costs, "=", obj).post();
        maximize[0] = rnd.nextBoolean();
        model.setObjective(maximize[0], obj);
        IntVar[] all = new IntVar[n + 1];
        System.arraycopy(xs, 0, all, 0, n);
        all[n] = obj;
        return all;
    }

    /**
     * @return the optimal value, or {@code null} if there is no solution
     */
    private static Integer optimise(Settings settings, long seed, List<Explanation> explanations) {
        Model model = new Model(settings);
        boolean[] maximize = new boolean[1];
        IntVar[] vars = build(model, seed, maximize);
        if (explanations != null) {
            PropSimplex simplex = PropSimplex.find(model);
            Assert.assertNotNull(simplex);
            simplex.observer = (lpVars, useLB, useUB, target, lower, newBound) -> {
                Explanation e = new Explanation();
                e.nbVars = lpVars.length;
                for (int i = 0; i < lpVars.length; i++) {
                    if (useLB[i]) {
                        e.lowerBounds.put(lpVars[i].getName(), lpVars[i].getLB());
                    }
                    if (useUB[i]) {
                        e.upperBounds.put(lpVars[i].getName(), lpVars[i].getUB());
                    }
                }
                e.target = target == null ? null : target.getName();
                e.lower = lower;
                e.newBound = newBound;
                explanations.add(e);
            };
        }
        IntVar[] decisions = new IntVar[vars.length - 1];
        System.arraycopy(vars, 0, decisions, 0, decisions.length);
        model.getSolver().setSearch(Search.randomSearch(decisions, seed));
        Integer best = null;
        while (model.getSolver().solve()) {
            best = vars[vars.length - 1].getValue();
        }
        return best;
    }

    /**
     * @return whether the explanation holds on the model of that seed
     */
    private static boolean holds(Explanation e, long seed) {
        Model model = new Model();
        IntVar[] vars = build(model, seed, new boolean[1]);
        model.clearObjective();
        for (IntVar var : vars) {
            Integer lb = e.lowerBounds.get(var.getName());
            if (lb != null) {
                model.arithm(var, ">=", lb).post();
            }
            Integer ub = e.upperBounds.get(var.getName());
            if (ub != null) {
                model.arithm(var, "<=", ub).post();
            }
            if (var.getName().equals(e.target)) {
                // look for a solution that satisfies the premises but not the consequence
                model.arithm(var, e.lower ? "<" : ">", e.newBound).post();
            }
        }
        return !model.getSolver().solve();
    }

    @DataProvider
    public Object[][] seeds() {
        Object[][] seeds = new Object[150][];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = new Object[]{(long) i};
        }
        return seeds;
    }

    @Test(groups = "10s", dataProvider = "seeds")
    public void testExplanationsHold(long seed) {
        List<Explanation> explanations = new ArrayList<>();
        optimise(SettingsBuilder.init().setLinearRelaxation(ILinearRelaxation.Solver.OJALGO).build(), seed, explanations);
        for (Explanation e : explanations) {
            if (e.target == null) {
                nbFailures++;
                int used = e.lowerBounds.size() + e.upperBounds.size();
                nbFailureBounds += used;
                nbFailureAllBounds += e.nbVars * 2L;
                if (used < e.nbVars * 2) {
                    nbShortFailures++;
                }
            } else if (e.target.equals("obj")) {
                nbObjectiveBounds++;
            } else {
                nbFilteredBounds++;
            }
            Assert.assertTrue(holds(e, seed), String.format("seed %d: %s >= %s and %s <= %s do not imply %s",
                    seed, e.lowerBounds.keySet(), e.lowerBounds.values(), e.upperBounds.keySet(), e.upperBounds.values(),
                    e.target == null ? "a failure" : e.target + (e.lower ? " >= " : " <= ") + e.newBound));
        }
    }

    /**
     * The three kinds of explanations must have been exercised, otherwise the previous test proves nothing.
     */
    @Test(groups = "10s", dependsOnMethods = "testExplanationsHold")
    public void testExplanationsAreExercised() {
        System.err.printf("explanations checked: %d failures (%d explained by a certificate, %d of %d bounds), "
                        + "%d objective bounds, %d filtered bounds%n",
                nbFailures, nbShortFailures, nbFailureBounds, nbFailureAllBounds, nbObjectiveBounds, nbFilteredBounds);
        Assert.assertTrue(nbFailures > 10, "infeasible LPs: " + nbFailures);
        Assert.assertTrue(nbObjectiveBounds > 20, "bounds on the objective: " + nbObjectiveBounds);
        Assert.assertTrue(nbFilteredBounds > 20, "bounds filtered with reduced costs: " + nbFilteredBounds);
    }

    @Test(groups = "10s", dataProvider = "seeds")
    public void testSameOptimumWithLCG(long seed) {
        Integer expected = optimise(SettingsBuilder.init().build(), seed, null);
        Integer relaxed = optimise(SettingsBuilder.init().setLinearRelaxation(ILinearRelaxation.Solver.OJALGO).build(), seed, null);
        Integer learnt = optimise(SettingsBuilder.init().setLCG(true)
                .setLinearRelaxation(ILinearRelaxation.Solver.OJALGO).build(), seed, null);
        Assert.assertEquals(relaxed, expected, "with the relaxation");
        Assert.assertEquals(learnt, expected, "with the relaxation and clause learning");
    }
}
