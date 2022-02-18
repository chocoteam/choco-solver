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
 * Fine events for a graph variable
 * @author Jean-Guillaume Fages, Charles Prud'homme
 */
public enum GraphEventType implements IEventType {

    VOID(0),
    REMOVE_NODE(1),
    ADD_NODE(2),
    REMOVE_EDGE(4),
    ADD_EDGE(8);

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int mask;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    GraphEventType(int mask) {
        this.mask = mask;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public int getMask() {
        return mask;
    }

}
