/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.trace;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/12/12
 */
public interface IMessage {
    /**
     * Define the solution format
     *
     * @return a String
     */
    String print();
}
