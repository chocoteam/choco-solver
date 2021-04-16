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

import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

/**
 * Updated by Dimitri Justeau-Allaire 16/04/2021: graph events were not scheduled before.
 */
public class GraphEvtScheduler implements EvtScheduler<GraphEventType> {

    private final int[] DIS = new int[] {

            0, 1, 2, 3, 6, 8, 10, 12, 13, 15, -1, // N-
            1, 3, 8, 13, 14, 15, -1, // N+
            3, 4, 5, 7, 8, 9, 10, 11, 12, 15, -1, // E-
            4, 6, 7, 8, 9, 10, 11, 15, -1, // E+
            0, 15, -1, // ALL EVENTS

            0, 3, 6, 15, -1, // N+- // 42
            0, 1, 2, 4, 5, 9, 10, 15, - 1, // N- E- // 46
            1, 3, 4, 6, 7, 15, -1, // N+ E+ // 55
            1, 4, 5, 7, 8, 15, -1, // N+ E- // 62
            0, 1, 2, 3, 4, 8, 9, 15, -1, // N- E+ // 68

            // REMAINING : 7, 12, 13, 14,
    };

    private int i = 0;

    private static final int[] IDX = new int[] {
            -1, 0, 11, 42, 18, 46, 62, 38, 29, 68, 55, 38, 38, 38, 38, 38
//            -1, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38
    };

    @Override
    public void init(int mask) {
        ISet s = SetFactory.makeConstantSet(new int[] {1, 2, 3, 4, 5, 6, 8, 9, 10});
        if (!s.contains(mask)) {
            System.out.println(mask);
        }
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
