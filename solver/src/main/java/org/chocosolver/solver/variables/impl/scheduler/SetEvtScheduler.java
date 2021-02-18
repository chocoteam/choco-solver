/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl.scheduler;

import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.iterators.EvtScheduler;

/**
 * Created by cprudhom on 17/06/15.
 * Project: choco.
 */
public class SetEvtScheduler implements EvtScheduler<SetEventType> {

    private final int[] DIS = new int[]{
            0, 1, 2, 3, -1, // ADD_TO_KER
            1, 3, -1, // REM_FROM_ENV
            0, 3, -1, // ADD_TO_KER and REM_FROM_ENV
    };
    private int i = 0;
    private static final int[] IDX = new int[]{-1, 0, 5, 8};

    @Override
    public void init(int mask) {
        assert mask > 0 && mask < 4;
        i = IDX[mask];
    }

    @Override
    public int select(int mask) {
        switch (mask) {
            case 1: // instantiate
                return 0;
            case 2: // lb or more
                return 1;
            case 3:
            case 255: // all
                return 2;
            case 0:
                return 3;
            default:
                throw new UnsupportedOperationException("Unknown case");
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
