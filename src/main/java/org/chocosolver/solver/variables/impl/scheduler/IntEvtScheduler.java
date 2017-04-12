/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl.scheduler;

import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.iterators.EvtScheduler;

/**
 * Created by cprudhom on 17/06/15.
 * Project: choco.
 */
public class IntEvtScheduler implements EvtScheduler<IntEventType> {

    private static final int[] DIS = new int[]{
            8, 9, -1, //REMOVE
            2,3, 6,7, 8,9, -1,// INCLOW
            4,5, 6,7, 8,9, -1, // DECUPP
            2,3, 4,5, 6,7, 8,9, -1, // BOUND
            0,1, 2,3, 4,5, 6,7, 8,9, -1, // INSTANTIATE
    };
    private static final int[] IDX = new int[]{-1, 0, 3, 10, 17, 26};
    private int i = 0;

    public void init(IntEventType evt) {
        i = IDX[evt.ordinal()];
    }

    @Override
    public int select(int mask) {
        int b = Integer.lowestOneBit(mask);
        switch (b) {
            case 8: // INSTANTIATE
                return 0;
            case 4: // DECUPP and more
                return 4;
            case 2: // INCLOW (and DECUPP) or more
                b = Integer.lowestOneBit(mask >> 2);
                if (b == 1) { // DECUPP too
                    return 6;
                } else {
                    return 2;
                }
            default:
            case 1:  // REMOVE or more
                return 8;
        }
    }

    @Override
    public boolean hasNext() {
        return DIS[i] > -1;
    }

    @Override
    public int next() {
        return DIS[i++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
