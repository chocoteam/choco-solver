/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.explanations.store;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;

/**
 * Created by cprudhom on 13/11/14.
 * Project: choco.
 */
public interface IEventStore {

    void pushEvent(IntVar var, ICause cause, IEventType mask, int one, int two, int three);

    int getSize();

    IntVar getVariable(int evt);

    IEventType getEventType(int evt);

    ICause getCause(int evt);

    int getFirstValue(int evt);

    int getSecondValue(int evt);

    int getThirdValue(int evt);

}
