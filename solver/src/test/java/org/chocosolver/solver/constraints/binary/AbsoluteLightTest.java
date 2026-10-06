/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 21/08/2026
 */
public class AbsoluteLightTest extends AbsoluteTest {

    @Override
    protected Constraint make(IntVar[] vars, Model model) {
        return new Constraint(ConstraintsName.ABSOLUTE, new PropAbsoluteLight(vars[0], vars[1]));
    }
}
