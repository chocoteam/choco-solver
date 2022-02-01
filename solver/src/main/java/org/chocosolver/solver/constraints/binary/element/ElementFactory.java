/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary.element;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.unary.PropEqualXC;
import org.chocosolver.solver.constraints.unary.PropMember;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

/**
 * A factory that selects the most adapted element propagator.
 * Created by cprudhom on 29/09/15.
 * Project: choco.
 */
public class ElementFactory {
    private ElementFactory() {
    }

    /**
     * Count the number of time the values in TABLE increase or decrease (ie, count picks and valleys).
     *
     * @param TABLE an array of values
     * @return the number of picks and valleys
     */
    private static int sawtooth(int[] TABLE) {
        int i = 0;
        while (i < TABLE.length - 1 && TABLE[i] == TABLE[i + 1]) {
            i++;
        }
        if (i == TABLE.length - 1) {
            return -1;
        }
        boolean up = TABLE[i] < TABLE[i + 1];
        int c = 0;
        i++;
        while (i < TABLE.length - 1) {
            if (up && TABLE[i] > TABLE[i + 1]) {
                c++;
                up = false;
            } else if (!up && TABLE[i] < TABLE[i + 1]) {
                c++;
                up = true;
            }
            i++;
        }
        return c;
    }

    /**
     * Detect and return the most adapted Element propagator wrt to the values in TABLE
     *
     * @param VALUE  the result variable
     * @param TABLE  the array of values
     * @param INDEX  the index variable
     * @param OFFSET the offset
     * @return an Element constraint
     */
    public static Constraint detect(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET) {
        // first chech the variables match
        int st = sawtooth(TABLE);
        if (st == -1) { // all values from TABLE are the same OR TABLE only contains one value
            assert TABLE[0] == TABLE[TABLE.length - 1];
            return new Constraint("FAKE_ELMT",
                    new PropMember(INDEX, new IntIterableRangeSet(OFFSET, OFFSET + TABLE.length - 1)),
                    new PropEqualXC(VALUE, TABLE[0])
            );
        }
        return new Constraint(ConstraintsName.ELEMENT, new PropElement(VALUE, TABLE, INDEX, OFFSET));
    }
}
