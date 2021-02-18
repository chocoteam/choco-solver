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
 * @author Jean-Guillaume Fages
 * @since may 2012
 */
public interface PairProcedure  {

    /**
     * Action to execute in a <code>GraphDelta</code> object, within the <code>forEachRemVal</code> method.
     * Used to iterate on a set if arcs
     *
     * @param i tail of the arc
     * @param j arrow of the arc
     * @throws org.chocosolver.solver.exception.ContradictionException
     *          when a incoherence is encountered
     */
    void execute(int i, int j) throws ContradictionException;
}
