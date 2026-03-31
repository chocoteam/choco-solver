/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.variables.IntVar;

import java.util.function.Consumer;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/08/11
 */
public enum Cause implements ICause {
    Null {
        @Override
        public void forEachIntVar(Consumer<IntVar> action) {

        }
    },

    Sat {
        @Override
        public void forEachIntVar(Consumer<IntVar> action) {

        }
    }
}
