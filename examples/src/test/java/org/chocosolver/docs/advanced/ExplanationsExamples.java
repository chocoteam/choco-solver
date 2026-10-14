/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.advanced;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Examples shown in the user documentation, page "Advanced usages / Explanations"
 * (docs/content/docs/Advanced usages/Explanations.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the page.
 *
 * @author Charles Prud'homme
 */
public class ExplanationsExamples {

    @Test(groups = "1s")
    public void lcgSettings() {
        // tag::lcgSettings[]
        Model model = new Model("Problem", SettingsBuilder.init()
                .setLCG(true)
                .setLcgExtractFromVariablesOnSolution(false)
                .setNbMaxLearntClauses(10_000)
                .setReduceLearntClausesBase(1_000)
                .setReduceLearntClausesFactor(100)
                .setSatCCMinMode(2)
                .setReasonManager(2)
                .setSortLitsOnSolution(true)
                .build());
        // end::lcgSettings[]
        IntVar[] x = model.intVarArray("x", 4, 0, 3);
        model.allDifferent(x).post();
        Assert.assertTrue(model.getSolver().isLCG());
        Assert.assertTrue(model.getSolver().solve());
    }
}
