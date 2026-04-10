/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Model;
import org.testng.annotations.BeforeMethod;

/**
 * @author Alexandre LEBRUN
 */
public class BoundedIntVarTest extends IntVarTest {

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        Model model = new Model();
        var = model.intVar(1, 4, true);
    }
}
