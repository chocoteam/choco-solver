/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

/**
 * An interface dedicated to encapsulate a given type
 * <p>
 * Project: choco.
 * @author Charles Prud'homme
 * @since 02/03/2016.
 */
public interface ISelf<V> {

    /**
     * @return the encapsulated type
     */
    V ref();
}
