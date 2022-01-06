/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.search.loop.TimeStampedObject;

/**
 * Implementation of graph variable delta
 * Adapted from choco-graph GraphDelta class - original author: Jean-Guillaume Fage.
 */
public class GraphDelta extends TimeStampedObject implements IGraphDelta {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final IEnumDelta[] deltaOfType;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public GraphDelta(IEnvironment environment) {
        super(environment);
        deltaOfType = new IEnumDelta[NB];
        for (int i = 0; i < NB; i++) {
            deltaOfType[i] = new EnumDelta(environment);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public int getSize(int i) {
        return deltaOfType[i].size();
    }

    public void add(int element, int type, ICause cause) {
        lazyClear();
        deltaOfType[type].add(element, cause);
    }

    public void lazyClear() {
        if (needReset()) {
            for (int i = 0; i < NB; i++) {
                deltaOfType[i].lazyClear();
            }
            resetStamp();
        }
    }

    public int get(int index, int type) {
        return deltaOfType[type].get(index);
    }

    public ICause getCause(int index, int type) {
        return deltaOfType[type].getCause(index);
    }
}
