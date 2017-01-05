/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * @author Charles Prud'homme, Jean-Guillaume Fages
 */
public interface ISetDeltaMonitor extends IDeltaMonitor {

	void forEach(IntProcedure proc, SetEventType evt) throws ContradictionException;

	enum Default implements ISetDeltaMonitor {
		NONE() {
			@Override
			public void freeze() {}
			@Override
			public void unfreeze() {}
			@Override
			public void forEach(IntProcedure proc, SetEventType eventType) throws ContradictionException {}
		}
	}
}
