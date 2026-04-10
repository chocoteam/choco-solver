/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.procedure;



/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29 sept. 2010
 */
public interface SafeIntProcedure  {

    /**
     * Action to execute in a <code>Delta</code> object, within the <code>forEachRemVal</code> method.
     *
     * @param i index
     */
    void execute(int i);
}
