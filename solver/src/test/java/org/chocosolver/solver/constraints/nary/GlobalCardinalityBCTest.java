/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Providers;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.constraints.nary.globalcardinality.GlobalCardinality.reformulate;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the bound-consistency ({@code "BC"}) filtering of the global cardinality constraint,
 * i.e. {@link org.chocosolver.solver.constraints.nary.globalcardinality.PropGccBC}.
 *
 * @author Charles Prud'homme
 */
public class GlobalCardinalityBCTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testClosed() throws ContradictionException {
        Model model = new Model();

        IntVar[] vars = model.intVarArray("vars", 6, 0, 3, true);
        IntVar[] card = model.intVarArray("card", 4, 0, 6, true);

        int[] values = new int[4];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }
        model.globalCardinality(vars, values, card, true, "BC").post();

        vars[0].instantiateTo(0, Null);
        vars[1].instantiateTo(1, Null);
        vars[2].instantiateTo(3, Null);
        vars[3].instantiateTo(2, Null);
        vars[4].instantiateTo(0, Null);
        vars[5].instantiateTo(0, Null);

        model.getSolver().setSearch(inputOrderLBSearch(append(vars, card)));
        while (model.getSolver().solve()) {
            // enumerate
        }
        assertTrue(model.getSolver().getSolutionCount() > 0);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testRandomBCEnumerated() {
        checkAgainstDecomposition(true, false);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testRandomBCBounded() {
        checkAgainstDecomposition(false, false);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testRandomBCClosed() {
        checkAgainstDecomposition(true, true);
    }

    private void checkAgainstDecomposition(boolean enumerated, boolean closed) {
        Random random = new Random();
        for (int seed = 0; seed < 100; seed++) {
            random.setSeed(seed);
            int n = 1 + random.nextInt(6);
            int m = 1 + random.nextInt(4);
            int[] values = new int[m];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            // model under test: GCC with BC filtering
            Model model = new Model(SettingsBuilder.init().setCheckDeclaredConstraints(false));
            IntVar[] vars = model.intVarArray("vars", n, 0, m - 1, !enumerated);
            IntVar[] cards = model.intVarArray("cards", m, 0, n, true);
            model.globalCardinality(vars, values, cards, closed, "BC").post();
            model.getSolver().setSearch(inputOrderLBSearch(append(vars, cards)));

            // reference model: elementary decomposition
            Model ref = new Model(SettingsBuilder.init().setCheckDeclaredConstraints(false));
            IntVar[] refVars = ref.intVarArray("vars", n, 0, m - 1, !enumerated);
            IntVar[] refCards = ref.intVarArray("cards", m, 0, n, true);
            reformulate(refVars, refCards, ref).post();
            if (closed) {
                for (IntVar v : refVars) {
                    ref.member(v, values).post();
                }
            }
            ref.getSolver().setSearch(inputOrderLBSearch(append(refVars, refCards)));

            while (model.getSolver().solve()) {
                // enumerate
            }
            while (ref.getSolver().solve()) {
                // enumerate
            }
            assertEquals(model.getSolver().getSolutionCount(), ref.getSolver().getSolutionCount(),
                    "seed=" + seed + ", enumerated=" + enumerated + ", closed=" + closed);
        }
    }

    @Test(groups = "10s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "random")
    @Providers.Arguments(values = {"1", "50"})
    public void testSameSolutionsDefaultVsBC(int seed) {
        Random random = new Random(seed);
        int n = 1 + random.nextInt(6);
        int m = 1 + random.nextInt(4);
        int[] values = new int[m];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }

        Model defaultModel = new Model(SettingsBuilder.init().setCheckDeclaredConstraints(false));
        IntVar[] defaultVars = defaultModel.intVarArray("vars", n, 0, m - 1, true);
        IntVar[] defaultCards = defaultModel.intVarArray("cards", m, 0, n, true);
        defaultModel.globalCardinality(defaultVars, values, defaultCards, false, "DEFAULT").post();
        defaultModel.getSolver().setSearch(inputOrderLBSearch(append(defaultVars, defaultCards)));

        Model bcModel = new Model(SettingsBuilder.init().setCheckDeclaredConstraints(false));
        IntVar[] bcVars = bcModel.intVarArray("vars", n, 0, m - 1, true);
        IntVar[] bcCards = bcModel.intVarArray("cards", m, 0, n, true);
        bcModel.globalCardinality(bcVars, values, bcCards, false, "BC").post();
        bcModel.getSolver().setSearch(inputOrderLBSearch(append(bcVars, bcCards)));

        while (defaultModel.getSolver().solve()) {
            // enumerate
        }
        while (bcModel.getSolver().solve()) {
            // enumerate
        }
        assertEquals(bcModel.getSolver().getSolutionCount(), defaultModel.getSolver().getSolutionCount(),
                "seed=" + seed);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnsat() {
        // 5 variables, all forced to take values in {0, 1}, but the total capacity is only 3
        Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", 5, 0, 1, true);
        IntVar[] cards = model.intVarArray("cards", 2, 0, 1, true); // sum of upper bounds = 2 < 5
        model.globalCardinality(vars, new int[]{0, 1}, cards, true, "BC").post();
        assertFalse(model.getSolver().solve());
    }

    /**
     * Regression test: a variable fixed to a value whose maximum occurrence is 0 (all other
     * variables free) must be detected as infeasible, not hang. Found via a real FlatZinc
     * instance ({@code blocks_16-4-5.fzn}): with several values having {@code maxOcc == 0}, a
     * variable landing exactly on a value with an already-exhausted (from initialization, not
     * from a decrement) capacity slot made {@code AlgoGccBC}'s union-find structure cycle
     * forever in {@code pathset} instead of failing.
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testForcedValueWithZeroCapacityFailsFast() {
        int[] fixedVals = {4, 0, 9, 3, 7, 1, 11, 13, 8, 14, 0, 0, 0, 16, 6, 5};
        int[] minOcc = {4, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1};
        int[] maxOcc = {4, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1}; // value 6: max 0
        int[] values = new int[maxOcc.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }
        Model model = new Model();
        IntVar[] vars = new IntVar[fixedVals.length];
        for (int i = 0; i < fixedVals.length; i++) {
            vars[i] = model.intVar("v" + i, fixedVals[i]);
        }
        IntVar[] cards = new IntVar[values.length];
        for (int i = 0; i < values.length; i++) {
            cards[i] = model.intVar("c" + i, minOcc[i], maxOcc[i]);
        }
        model.globalCardinality(vars, values, cards, false, "BC").post();
        // variable 14 is fixed to 6, a value with maxOcc == 0: infeasible.
        assertFalse(model.getSolver().solve());
    }

    /**
     * Regression test: a run of consecutive values with {@code maxOcc == 0} must not cause BC to
     * wrongly declare failure for an unrelated, genuinely feasible, wide-domain variable landing
     * on that same (already-exhausted-from-initialization) slot. Found via a real FlatZinc
     * instance ({@code handball_handball8.fzn}): the naive fail-fast fix for the hang above
     * (failing whenever a capacity slot goes negative) was over-eager and rejected feasible
     * states, silently proving a worse "optimum" than {@code "DEFAULT"}/{@code "AC"}.
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testZeroCapacityRunsDoNotOverFilter() throws ContradictionException {
        int[] minOcc = {2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2};
        int[] maxOcc = {2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2};
        int[] lb = {9, 9, 9, 9, 9, 9, 0, 0, 9, 0, 9, 0, 9, 0};
        int[] ub = {9, 19, 19, 19, 9, 19, 19, 19, 19, 19, 19, 19, 19, 19};
        int[] values = new int[minOcc.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }
        Model model = new Model();
        IntVar[] vars = new IntVar[lb.length];
        for (int i = 0; i < lb.length; i++) {
            vars[i] = model.intVar("v" + i, lb[i], ub[i], false);
        }
        IntVar[] cards = new IntVar[values.length];
        for (int i = 0; i < values.length; i++) {
            cards[i] = model.intVar("c" + i, minOcc[i], maxOcc[i]);
        }
        model.globalCardinality(vars, values, cards, false, "BC").post();
        // must not throw: values 9, 11, 13, 15, 17, 19 each need exactly 2 occurrences (12
        // needed), the 12 free variables (all but v0/v4, already fixed to 9) can cover them.
        model.getSolver().propagate();
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "random")
    @Providers.Arguments(values = {"1", "50"})
    public void testFixpoint(int seed) throws ContradictionException {
        Random random = new Random(seed);
        int n = 2 + random.nextInt(8);
        int m = 1 + random.nextInt(5);
        boolean bounded = random.nextBoolean();
        int[] values = new int[m];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }
        Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", n, 0, m - 1, bounded);
        IntVar[] cards = model.intVarArray("cards", m, 0, n, true);
        model.globalCardinality(vars, values, cards, false, "BC").post();

        // apply a few random domain reductions, then check that a second propagation
        // does not change anything anymore (fixpoint reached), as required by
        // BEST_PRACTICES.md 3.4.
        for (int i = 0; i < n; i++) {
            if (random.nextInt(3) == 0) {
                int v = values[random.nextInt(m)];
                if (vars[i].contains(v) && vars[i].getDomainSize() > 1) {
                    vars[i].removeValue(v, Null);
                }
            }
        }
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            return; // a failure is a valid outcome
        }
        int[] before = snapshot(vars, cards);
        model.getSolver().propagate();
        int[] after = snapshot(vars, cards);
        assertEquals(after, before, "seed=" + seed + ", bounded=" + bounded);
    }

    private static int[] snapshot(IntVar[] vars, IntVar[] cards) {
        IntVar[] all = append(vars, cards);
        int[] res = new int[2 * all.length];
        for (int i = 0; i < all.length; i++) {
            res[2 * i] = all[i].getLB();
            res[2 * i + 1] = all[i].getUB();
        }
        return res;
    }
}
