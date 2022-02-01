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

import org.chocosolver.solver.ICause;

/**
 * Interface of graph variable delta
 * Adapted from choco-graph GraphDelta class - original authors: Jean-Guillaume Fages and Charles Prud'homme.
 */
public interface IGraphDelta extends IDelta {

    int NODE_REMOVED = 0;
    int NODE_ENFORCED = 1;
    int EDGE_REMOVED_TAIL = 2;
    int EDGE_REMOVED_HEAD = 3;
    int EDGE_ENFORCED_TAIL = 4;
    int EDGE_ENFORCED_HEAD = 5;
    int NB = 6;

    int getSize(int i);

    void add(int element, int type, ICause cause);

    int get(int index, int type);

    ICause getCause(int index, int type);
}
