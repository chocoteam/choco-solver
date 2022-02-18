/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.procedure;

import org.chocosolver.solver.exception.ContradictionException;



/**
 * A class that permits to execute a embeded "function"
 *
 * @param <E>
 */
public interface Procedure<E>  {
    /**
     * Action to execute in a <code>Delta</code> object, within the <code>forEachRemVal</code> method.
     *
     * @param e object to deal with in the execution
     * @throws org.chocosolver.solver.exception.ContradictionException
     *          if a contradiction occurs
     */
    void execute(E e) throws ContradictionException;
}
