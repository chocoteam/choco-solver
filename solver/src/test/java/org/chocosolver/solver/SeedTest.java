/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.CmdLineException;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests that the seed declared in the settings reaches the model and the search strategies (#1207).
 *
 * @author Charles Prud'homme
 * @since 14/10/2026
 */
public class SeedTest {

    @Test(groups = "1s")
    public void testDefaultSeed() {
        assertEquals(SettingsBuilder.init().getSeed(), 0L);
        assertEquals(new Model().getSeed(), 0L);
    }

    @Test(groups = "1s")
    public void testSetSeed() {
        Model model = new Model(SettingsBuilder.init().setSeed(42L));
        assertEquals(model.getSettings().getSeed(), 42L);
        assertEquals(model.getSeed(), 42L);
    }

    @Test(groups = "1s")
    public void testSeedFromProperties() {
        Properties props = new Properties();
        props.setProperty(SettingsBuilder.SEED, "42");
        assertEquals(SettingsBuilder.init().fromProperties(props).getSeed(), 42L);
    }

    @Test(groups = "1s")
    public void testSeedFromArgs() throws CmdLineException {
        assertEquals(SettingsBuilder.init().fromArgs(new String[]{"-seed", "42"}).getSeed(), 42L);
    }

    @SuppressWarnings("deprecation")
    @Test(groups = "1s")
    public void testDeprecatedSetSeed() {
        Model model = new Model(SettingsBuilder.init().setSeed(42L));
        model.setSeed(7L);
        assertEquals(model.getSeed(), 7L);
        assertEquals(model.getSettings().getSeed(), 42L);
    }

    /**
     * Initially, all variables have the same score for dom/wdeg: the seed breaks the ties.
     */
    private static int[] firstSolution(long seed) {
        Model model = new Model(SettingsBuilder.init().setSeed(seed));
        IntVar[] vars = model.intVarArray("x", 6, 0, 5);
        model.allDifferent(vars).post();
        model.getSolver().setSearch(Search.domOverWDegSearch(vars));
        assertTrue(model.getSolver().solve());
        return Arrays.stream(vars).mapToInt(IntVar::getValue).toArray();
    }

    @Test(groups = "1s")
    public void testSeedReachesSearch() {
        // same seed, same search
        assertEquals(firstSolution(3L), firstSolution(3L));
        // different seeds, different tie-breaking
        Set<String> solutions = new HashSet<>();
        for (long seed = 0; seed < 10; seed++) {
            solutions.add(Arrays.toString(firstSolution(seed)));
        }
        assertTrue(solutions.size() > 1, "the seed should change the search: " + solutions);
    }
}
