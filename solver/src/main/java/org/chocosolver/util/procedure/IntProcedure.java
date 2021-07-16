/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.procedure;

import org.chocosolver.solver.exception.ContradictionException;



/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29 sept. 2010
 */
public interface IntProcedure  {

    /**
     * Action to execute in a <code>Delta</code> object, within the <code>forEachRemVal</code> method.
     *
     * @param i index
     * @throws org.chocosolver.solver.exception.ContradictionException
     *          when a incoherence is encountered
     */
    void execute(int i) throws ContradictionException;
}
