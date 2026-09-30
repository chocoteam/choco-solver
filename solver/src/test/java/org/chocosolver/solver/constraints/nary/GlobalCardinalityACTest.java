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

import java.util.Arrays;
import java.util.Random;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.constraints.nary.globalcardinality.GlobalCardinality.reformulate;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the arc-consistency ({@code "AC"}) filtering of the global cardinality constraint,
 * i.e. {@link org.chocosolver.solver.constraints.nary.globalcardinality.PropGcc} with
 * {@link org.chocosolver.solver.constraints.nary.globalcardinality.GlobalCardinality.Consistency#AC}.
 *
 * @author Charles Prud'homme
 */
public class GlobalCardinalityACTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testClosed() throws ContradictionException {
        Model model = new Model();

        IntVar[] vars = model.intVarArray("vars", 6, 0, 3, true);
        IntVar[] card = model.intVarArray("card", 4, 0, 6, true);

        int[] values = new int[4];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }
        model.globalCardinality(vars, values, card, true, "AC").post();

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
    public void testRandomACEnumerated() {
        checkAgainstDecomposition(true, false, false);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testRandomACBounded() {
        checkAgainstDecomposition(false, false, false);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testRandomACClosed() {
        checkAgainstDecomposition(true, true, false);
    }

    /**
     * Domains that spill outside the restricted value list, not closed: a variable can "escape"
     * cardinality accounting entirely by taking such a value. See BEST_PRACTICES.md for why this
     * requires a dense (one node per value) flow network, not just one per restricted value.
     */
    @Test(groups = "10s", timeOut = 60000)
    public void testRandomACExtraValuesNotClosed() {
        checkAgainstDecomposition(true, false, true);
    }

    /**
     * Regression test: when 5 out of 81 variables can each optionally use one restricted value
     * (a "staircase" of growing domains) with no cardinality lower bound at all, AC must not
     * force any of them onto a restricted value: their shared escape value (1, unrestricted)
     * remains valid for each of them individually. A first (unsound) implementation of the
     * escape mechanism, using a single shared "unlimited capacity" node instead of one node per
     * unrestricted value, wrongly forced these variables apart. Found on a real FlatZinc instance
     * ({@code peaceable_queens_n9_q5.fzn}).
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testEscapeValueKeptWhenSafe() throws ContradictionException {
        int n = 81;
        int[] values = {2, 3, 4, 5, 6};
        Model model = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("v" + i, 1); // fixed to the escape value by default
        }
        vars[75] = model.intVar("v75", new int[]{1, 2});
        vars[76] = model.intVar("v76", new int[]{1, 2, 3});
        vars[77] = model.intVar("v77", new int[]{1, 2, 3, 4});
        vars[78] = model.intVar("v78", new int[]{1, 2, 3, 4, 5});
        vars[79] = model.intVar("v79", new int[]{1, 2, 3, 4, 5, 6});
        IntVar[] cards = model.intVarArray("card", values.length, 0, 1, true);
        model.globalCardinality(vars, values, cards, false, "AC").post();
        model.getSolver().propagate();
        for (int i : new int[]{75, 76, 77, 78, 79}) {
            assertTrue(vars[i].contains(1), "v" + i + " should keep its escape value 1: " + vars[i]);
        }
    }

    /**
     * Regression test, the mirror image of {@link #testEscapeValueKeptWhenSafe}: a variable's
     * escape value must be removed when using it would make the rest of the network infeasible
     * (here: 3 variables share the only candidates for values 3, 4 and 6, whose minimums leave no
     * room for one of the three to escape). Found on the same real FlatZinc instance.
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testEscapeValueRemovedWhenNecessary() throws ContradictionException {
        int n = 81;
        int[] values = {2, 3, 4, 5, 6};
        int[][] free = {
                {60, 1, 2}, {63, 1, 3}, {64, 1, 3, 4}, {67, 1, 2}, {70, 1, 3, 4, 5},
                {71, 1, 3, 4, 5, 6}, {72, 1, 3, 4, 5, 6}, {73, 1, 2}, {74, 1, 3, 4, 5, 6},
                {75, 1, 2}, {76, 1, 3, 4, 5, 6}, {77, 1, 2}, {78, 1, 3, 4, 5, 6},
        };
        Model model = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("v" + i, 1);
        }
        for (int[] f : free) {
            int idx = f[0];
            int[] dom = Arrays.copyOfRange(f, 1, f.length);
            vars[idx] = model.intVar("v" + idx, dom);
        }
        IntVar[] cards = model.intVarArray("card", values.length, 2, 5, true);
        model.globalCardinality(vars, values, cards, false, "AC").post();
        model.getSolver().propagate();
        // var63's only restricted candidate is 3: the escape (1) must have been removed, since
        // the other candidates for value3 are all needed elsewhere (see BEST_PRACTICES.md).
        assertTrue(vars[63].isInstantiatedTo(3), "v63 should be forced to 3: " + vars[63]);
    }

    /**
     * Regression test: a warm-started matching that settles every variable on a single,
     * slack-having value (as happens after a first {@code propagate()} with no lower bound
     * requirement at all) must not get stuck when a later {@code propagate()} raises several
     * values' lower bounds at once, on the SAME persistent {@code AlgoGccAC} instance -- even
     * though trivial reassignments exist. The augmenting-path search only knows how to walk a
     * chain back to a free (unmatched) variable; if warm-starting leaves none, it has no entry
     * point, wrongly declaring the (perfectly feasible) deficit unreachable. This mirrors what
     * happens during a real MAXIMIZE search: the objective bound pushes cardinality lower bounds
     * up between search nodes. Found on {@code peaceable_queens_n9_q5.fzn}/
     * {@code peaceable_queens_n8_q3.fzn} under optimization, where AC wrongly proved a bound
     * strictly lower than BC's/the true optimum. See BEST_PRACTICES.md.
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testDeficitAppearingAfterWarmStart() throws ContradictionException {
        int n = 20;
        int[] values = {2, 3, 4, 5, 6};
        Model model = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("v" + i, 1, 6); // nothing forces them off value 1 (yet)
        }
        IntVar[] cards = model.intVarArray("card", values.length, 0, n, true);
        model.globalCardinality(vars, values, cards, false, "AC").post();
        // first propagation: no deficit anywhere, the warm-started matching is free to settle
        // every variable on a single value.
        model.getSolver().propagate();

        // raise every listed value's lower bound to 1 at once, as an objective bound tightening
        // would: a trivial reassignment (move one variable per value away from value 1) exists
        // and must be found.
        for (IntVar c : cards) {
            c.updateLowerBound(1, Null);
        }
        model.getSolver().propagate(); // must not fail
        for (IntVar c : cards) {
            assertTrue(c.getUB() >= 1, c.getName() + " should be able to reach its new minimum");
        }
    }

    private void checkAgainstDecomposition(boolean enumerated, boolean closed, boolean extraValues) {
        Random random = new Random();
        for (int seed = 0; seed < 100; seed++) {
            random.setSeed(seed);
            int n = 1 + random.nextInt(6);
            int m = 1 + random.nextInt(4);
            int[] values = new int[m];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            int ub = extraValues ? m + 1 : m - 1; // extra, unrestricted values m..m+1

            // model under test: GCC with AC filtering
            Model model = new Model(SettingsBuilder.init().setCheckDeclaredConstraints(false));
            IntVar[] vars = model.intVarArray("vars", n, 0, ub, !enumerated);
            IntVar[] cards = model.intVarArray("cards", m, 0, n, true);
            model.globalCardinality(vars, values, cards, closed, "AC").post();
            model.getSolver().setSearch(inputOrderLBSearch(append(vars, cards)));

            // reference model: elementary decomposition
            Model ref = new Model(SettingsBuilder.init().setCheckDeclaredConstraints(false));
            IntVar[] refVars = ref.intVarArray("vars", n, 0, ub, !enumerated);
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
                    "seed=" + seed + ", enumerated=" + enumerated + ", closed=" + closed
                            + ", extraValues=" + extraValues);
        }
    }

    @Test(groups = "10s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "random")
    @Providers.Arguments(values = {"1", "50"})
    public void testSameSolutionsDefaultVsBCvsAC(int seed) {
        Random random = new Random(seed);
        int n = 1 + random.nextInt(6);
        int m = 1 + random.nextInt(4);
        int[] values = new int[m];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }

        long[] counts = new long[3];
        String[] modes = {"DEFAULT", "BC", "AC"};
        for (int c = 0; c < modes.length; c++) {
            Model model = new Model(SettingsBuilder.init().setCheckDeclaredConstraints(false));
            IntVar[] vars = model.intVarArray("vars", n, 0, m - 1, true);
            IntVar[] cards = model.intVarArray("cards", m, 0, n, true);
            model.globalCardinality(vars, values, cards, false, modes[c]).post();
            model.getSolver().setSearch(inputOrderLBSearch(append(vars, cards)));
            while (model.getSolver().solve()) {
                // enumerate
            }
            counts[c] = model.getSolver().getSolutionCount();
        }
        assertEquals(counts[1], counts[0], "BC vs DEFAULT, seed=" + seed);
        assertEquals(counts[2], counts[0], "AC vs DEFAULT, seed=" + seed);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnsat() {
        // 5 variables, all forced to take values in {0, 1}, but the total capacity is only 3
        Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", 5, 0, 1, true);
        IntVar[] cards = model.intVarArray("cards", 2, 0, 1, true); // sum of upper bounds = 2 < 5
        model.globalCardinality(vars, new int[]{0, 1}, cards, true, "AC").post();
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "random")
    @Providers.Arguments(values = {"1", "50"})
    public void testFixpoint(int seed) throws ContradictionException {
        Random random = new Random(seed);
        int n = 2 + random.nextInt(8);
        int m = 1 + random.nextInt(5);
        int[] values = new int[m];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }
        Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", n, 0, m - 1, true);
        IntVar[] cards = model.intVarArray("cards", m, 0, n, true);
        model.globalCardinality(vars, values, cards, false, "AC").post();

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
        assertEquals(after, before, "seed=" + seed);
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
