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
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.SafeIntProcedure;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/05/12
 */
public interface IIntDeltaMonitor extends IDeltaMonitor {

    void forEachRemVal(SafeIntProcedure proc);

    void forEachRemVal(IntProcedure proc) throws ContradictionException;

	/**
     * @return a rough estimation of the number of removed values 
     */
    int sizeApproximation();

    enum Default implements IIntDeltaMonitor {
        NONE() {
            @Override
           	public int sizeApproximation(){
           		return 0;
           	}
            @Override
            public void freeze() {}

            @Override
            public void unfreeze() {}

            @Override
            public void forEachRemVal(SafeIntProcedure proc) {}

            @Override
            public void forEachRemVal(IntProcedure proc) throws ContradictionException {}
        }
    }
}
