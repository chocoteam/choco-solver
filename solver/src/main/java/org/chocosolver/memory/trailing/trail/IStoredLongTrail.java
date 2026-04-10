/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.trailing.trail;

import org.chocosolver.memory.IStorage;
import org.chocosolver.memory.trailing.StoredLong;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/04/13
 */
public interface IStoredLongTrail extends IStorage {

    void savePreviousState(StoredLong v, long oldValue, int oldStamp);

}
