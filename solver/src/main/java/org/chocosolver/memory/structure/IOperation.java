/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.structure;

/**
 * Generic interface used to undo modifications upon backtracking
 *
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 11/02/11
 */
public interface IOperation {

	/**
     * Method called by the environment upon backtracking, to undo this operation
     */
    void undo();
}
