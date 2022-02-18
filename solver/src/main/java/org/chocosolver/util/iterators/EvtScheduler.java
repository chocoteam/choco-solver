/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.iterators;

import org.chocosolver.solver.variables.events.IEventType;

/**
 * Created by cprudhom on 17/06/15.
 * Project: choco.
 */
public interface EvtScheduler<E extends IEventType> extends IntIterator {

    /**
     * Initializes the scheduler with an event defined by 'mask'.
     * @param mask event's mask received
     */
    void init(int mask);

    int select(int mask);

}
