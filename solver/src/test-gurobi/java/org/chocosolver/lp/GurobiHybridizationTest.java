/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import org.testng.annotations.DataProvider;

import java.util.function.IntFunction;

/**
 * The tests of {@link HybridizationTest}, with Gurobi as the relaxation backend.
 * <p>
 * Gurobi is not a dependency of choco-solver: this class and {@link GurobiRelaxation} are only compiled and run
 * with the {@code gurobi} Maven profile, which needs a local installation of Gurobi (the {@code com.gurobi:gurobi}
 * artifact) and a licence.
 *
 * @author Charles Prud'homme
 */
public class GurobiHybridizationTest extends HybridizationTest {

    @Override
    @DataProvider
    public Object[][] algo() {
        return new Object[][]{
                {"GUROBI", (IntFunction<ILinearRelaxation>) GurobiRelaxation::new},
        };
    }
}
