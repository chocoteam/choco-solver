/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.trees;

import org.testng.annotations.Test;

public class DcmtsTest {

    @Test(groups = "10s", timeOut = 60000)
    public void testDcmts() {
        String inst = getClass().getResource("r123_300_1").getPath();
        new DCMST(inst).solve();
    }

}
