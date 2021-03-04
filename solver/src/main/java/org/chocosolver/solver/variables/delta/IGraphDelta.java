/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;

import org.chocosolver.solver.ICause;

/**
 * Interface of graph variable delta
 * Adapted from choco-graph GraphDelta class - original authors: Jean-Guillaume Fages and Charles Prud'homme.
 */
public interface IGraphDelta extends IDelta {

    //NR NE AR AE : NodeRemoved NodeEnforced ArcRemoved ArcEnforced
    public final static int NR = 0;
    public final static int NE = 1;
    public final static int AR_TAIL = 2;
    public final static int AR_HEAD = 3;
    public final static int AE_TAIL = 4;
    public final static int AE_HEAD = 5;
    public final static int NB = 6;

    int getSize(int i);

    void add(int element, int type, ICause cause);

    int get(int index, int type);

    ICause getCause(int index, int type);
}
