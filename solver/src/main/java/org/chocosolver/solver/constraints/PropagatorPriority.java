/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Priority;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11 aug. 2010
 */
public enum PropagatorPriority implements Priority {

    UNARY(1), BINARY(2), TERNARY(3), LINEAR(4), QUADRATIC(5), CUBIC(6), VERY_SLOW(7);// 8 = PROPAGATE;

    public final int priority;

    PropagatorPriority(int prio) {
        this.priority = prio;
    }

    public static PropagatorPriority get(int prio) {
        switch (prio) {
            case 1:
                return UNARY;
            case 2:
                return BINARY;
            case 3:
                return TERNARY;
            case 4:
                return LINEAR;
            case 5:
                return QUADRATIC;
            case 6:
                return CUBIC;
            case 7:
            default:
                return VERY_SLOW;
        }
    }

    public final int getValue() {
        return priority;
    }


}
