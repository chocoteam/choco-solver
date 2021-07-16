/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public enum ESat {

    TRUE, FALSE, UNDEFINED;


    public static ESat eval(boolean b) {
        if (b) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    public static ESat not(ESat sat) {
        switch (sat) {
            case TRUE:
                return FALSE;
            case FALSE:
                return TRUE;
            default:
                return sat;
        }
    }

}
