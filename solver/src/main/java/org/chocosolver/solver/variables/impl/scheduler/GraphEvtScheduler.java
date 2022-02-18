/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl.scheduler;

import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.iterators.EvtScheduler;

/**
 * Updated by Dimitri Justeau-Allaire 16/04/2021: graph events were not scheduled before.
 */
public class GraphEvtScheduler implements EvtScheduler<GraphEventType> {

    private final int[] DIS = new int[] {

            0, 1, 2, 3, 6, 8, 10, 12, 13, 15, -1, // N- (mask = 1) // IDX[1] = 0
            1, 3, 8, 13, 14, 15, -1, // N+ (mask = 2) // IDX[2] = 11
            3, 4, 5, 7, 8, 9, 10, 11, 12, 15, -1, // E- (mask = 4) // IDX[4] = 18
            4, 6, 7, 8, 9, 10, 11, 15, -1, // E+ (mask = 8) // IDX[8] = 29
            0, 15, -1, // ALL EVENTS // IDX[15] = 38

            0, 3, 6, 15, -1, // N+- (mask = 3) // IDX[3] = 41
            0, 1, 2, 4, 5, 9, 10, 15, - 1, // N- E- (mask = 5) // IDX[5] = 46
            1, 3, 4, 6, 7, 15, -1, // N+ E+ (mask = 10) // IDX[10] = 55
            1, 4, 5, 7, 8, 15, -1, // N+ E- (mask = 6) // IDX[6] = 62
            0, 1, 2, 3, 4, 8, 9, 15, -1, // N- E+ (mask = 9) // IDX[9] = 69
            0, 4, 5, 15, -1, // N+- E- (mask = 7) // IDX[7] = 78
            3, 15, -1, // E+- (mask = 12) // IDX[12] = 83
            0, 1, 2, 15, -1, // N- E+- (mask = 13) // IDX[13] = 86
            1, 15, -1, // N+ E+- (mask = 14) // IDX[14] = 91
            0, 3, 4, 15, -1 // N+- E+ (mask = 11) // IDX[11] = 94
    };

    private int i = 0;

    private static final int[] IDX = new int[] {
            -1, 0, 11, 41, 18, 46, 62, 78, 29, 69, 55, 94, 83, 86, 91, 38
    };

    @Override
    public void init(int mask) {
        i = IDX[mask];
    }

    @Override
    public int select(int mask) {
        switch (mask) {
            case 1: // N- (REMOVE NODE)
                return 0;
            case 2: // N+ (ADD NODE)
                return 1;
            case 3: // N+- (ADD OR REMOVE NODE)
                return 2;
            case 4: // E- (REMOVE EDGE)
                return 3;
            case 8: // E+ (ADD EDGE)
                return 4;
            case 12: // E+- (ADD OR REMOVE EDGE)
                return 5;
            case 5: // N- E- (REMOVE NODE OR EDGE)
                return 6;
            case 9: // N- E+ (REMOVE NODE OR ADD EDGE)
                return 7;
            case 6: // N+ E- (ADD NODE OR REMOVE EDGE)
                return 8;
            case 10: // N+ E+ (ADD NODE OR EDGE)
                return 9;
            case 7: // N+- E- (REMOVE NODE OR EDGE OR ADD NODE)
                return 10;
            case 11: // N+- E+ (ADD NODE OR EDGE OR REMOVE NODE)
                return 11;
            case 14: // N+ E+- ADD NODE OR EDGE OR REMOVE EDGE
                return 12;
            case 13: // N- E+- REMOVE NODE OR EDGE OR ADD EDGE
                return 13;
            case 15: // ALL EVENTS
            case 255:
                return 14;
            case 0:
                return 15;
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
