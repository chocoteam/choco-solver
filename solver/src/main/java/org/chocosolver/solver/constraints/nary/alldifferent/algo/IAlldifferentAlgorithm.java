/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.algo;

import org.chocosolver.solver.exception.ContradictionException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/10/2024
 */
public interface IAlldifferentAlgorithm {
    boolean propagate() throws ContradictionException;
}
