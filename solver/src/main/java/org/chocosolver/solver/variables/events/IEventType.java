/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.events;

/**
 * An interface to define event to categorize the filtering algorithm to apply.
 * <br/>Event can promoted or strengthened (cf. "CHOCO : implementing a CP kernel" -- F. Laburthe, 2000).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/12/11
 */
public interface IEventType {

	int ALL_EVENTS = 255;

    /**
     * Return the value of the mask associated with the event.
     *
     * @return the mask of the event.
     */
    int getMask();
}
