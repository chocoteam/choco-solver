/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

/**
 * An interface to provide an identity to object, using a positive int to caracterize the object.
 * It allows definition of , 2 147 483 647 different objects.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/12/11
 */
public interface Identity {

    int getId();
}
