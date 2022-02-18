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
 * An enum defining the real variable event types:
 * <ul>
 * <li><code>INCLOW</code>: lower bound increase event,</li>
 * <li><code>DECUPP</code>: upper bound decrease event,</li>
 * <li><code>BOUND</code>: lower bound increase and/or upper bound decrease event,</li>
 * </ul>
 * <p/>
 * @author Charles Prud'homme, Jean-Guillaume Fages
 */
public enum RealEventType implements IEventType {

    VOID(0),
    INCLOW(1),
    DECUPP(2),
    BOUND(3);

    private final int mask;

	RealEventType(int mask) {
        this.mask = mask;
    }

    @Override
    public int getMask() {
        return mask;
    }

    //******************************************************************************************************************
    //******************************************************************************************************************

    public static boolean isBound(int mask) {
        return (mask & BOUND.mask) != 0;
    }

    public static boolean isInclow(int mask) {
        return (mask & INCLOW.mask) != 0;
    }

    public static boolean isDecupp(int mask) {
        return (mask & DECUPP.mask) != 0;
    }
}
