/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * @author Charles Prud'homme, Jean-Guillaume Fages
 */
public interface ISetDeltaMonitor extends IDeltaMonitor{

    /**
     * Apply 'proc' on each value store
     * @param proc a procedure
     * @param evt event mask
     * @throws ContradictionException if a contradiction occurs
     */
	void forEach(IntProcedure proc, SetEventType evt) throws ContradictionException;
}
