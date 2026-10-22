/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link org.chocosolver.solver.constraints.IDecompositionFactory#globalCardinalityDec},
 * whose values are given by variables. The solutions are compared to the ones found by brute force.
 *
 * @author Charles Prud'homme
 */
public class GlobalCardinalityDecTest {

    @DataProvider
    public Object[][] params() {
        return new Object[][]{
                // closed, lcg
                {false, false}, {true, false}, {false, true}, {true, true},
        };
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "params")
    public void testValuesAsVariables(boolean closed, boolean lcg) {
        // vars: 3 variables in [0,2], values: 2 variables in [0,2], occurrences: 2 variables in [0,3]
        Model model = new Model(SettingsBuilder.init().setLCG(lcg));
        IntVar[] vars = model.intVarArray("x", 3, 0, 2);
        IntVar[] values = model.intVarArray("v", 2, 0, 2);
        IntVar[] occs = model.intVarArray("o", 2, 0, 3);
        model.globalCardinalityDec(vars, values, occs, closed);
        IntVar[] all = new IntVar[]{vars[0], vars[1], vars[2], values[0], values[1], occs[0], occs[1]};

        // default search, on all the variables of the model (#1260)
        Solver solver = model.getSolver();
        Set<String> found = new HashSet<>();
        int count = 0;
        while (solver.solve()) {
            found.add(Arrays.toString(Arrays.stream(all).mapToInt(IntVar::getValue).toArray()));
            count++;
        }
        assertEquals(count, found.size(), "solutions found several times");
        assertEquals(found, expected(closed));
    }

    private static Set<String> expected(boolean closed) {
        Set<String> sols = new HashSet<>();
        int[] s = new int[7];
        for (int i = 0; i < 3 * 3 * 3 * 3 * 3; i++) {
            int k = i;
            for (int j = 0; j < 5; j++) {
                s[j] = k % 3;
                k /= 3;
            }
            int o0 = 0, o1 = 0;
            boolean in = true;
            for (int j = 0; j < 3; j++) {
                o0 += s[j] == s[3] ? 1 : 0;
                o1 += s[j] == s[4] ? 1 : 0;
                in &= s[j] == s[3] || s[j] == s[4];
            }
            if (!closed || in) {
                s[5] = o0;
                s[6] = o1;
                sols.add(Arrays.toString(s));
            }
        }
        return sols;
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testClosedWithDisjointDomains() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("x", 2, 3, 4);
        IntVar[] values = model.intVarArray("v", 1, 0, 2);
        IntVar[] occs = model.intVarArray("o", 1, 0, 2);
        model.globalCardinalityDec(vars, values, occs, true);
        assertEquals(model.getSolver().findAllSolutions().size(), 0);
    }
}
