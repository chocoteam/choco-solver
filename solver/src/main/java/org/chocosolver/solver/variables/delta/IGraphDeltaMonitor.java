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

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * Interface for Graph Delta Monitor.
 * Adapted from choco-graph. Original authors: Jean-Guillaume Fages and Charles Prud'homme.
 */
public interface IGraphDeltaMonitor extends IDeltaMonitor{

    /**
     * Applies proc to every vertex which has just been removed or enforced, depending on evt.
     *
     * @param proc an incremental procedure over vertices
     * @param evt  either ENFORCENODE or REMOVENODE
     * @throws ContradictionException if a failure occurs
     */
    void forEachNode(IntProcedure proc, GraphEventType evt) throws ContradictionException;

    /**
     * Applies proc to every edge which has just been removed or enforced, depending on evt.
     *
     * @param proc an incremental procedure over edges
     * @param evt  either ENFORCE_EDGE or REMOVE_EDGE
     * @throws ContradictionException if a failure occurs
     */
    void forEachEdge(PairProcedure proc, GraphEventType evt) throws ContradictionException;
}