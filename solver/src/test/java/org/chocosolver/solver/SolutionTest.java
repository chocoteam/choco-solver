/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SolutionTest {

    @Test
    public void testCopyEmptySolutionSuccess() {
        final Solution emptySolution = new Solution(null);
        Assert.assertNotSame(emptySolution, emptySolution.copySolution());
    }
}
