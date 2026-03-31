/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.trailing.trail;

import org.chocosolver.memory.IStorage;
import org.chocosolver.memory.trailing.StoredBool;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/04/13
 */
public interface IStoredBoolTrail extends IStorage {

    void savePreviousState(StoredBool v, boolean oldValue, int oldStamp);

}
