/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta.monitor;

import org.chocosolver.solver.variables.delta.IGraphDelta;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.TimeStampedObject;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/12/11
 */
public class GraphDeltaMonitor extends TimeStampedObject implements IGraphDeltaMonitor {

    private final IGraphDelta delta;
    private int[] first;
    private int[] last;
    private ICause propagator;

    public GraphDeltaMonitor(IGraphDelta delta, ICause propagator) {
        super(delta.getEnvironment());
        this.delta = delta;
        this.first = new int[4];
        this.last = new int[4];
        this.propagator = propagator;
    }

    private void freeze() {
        if (needReset()) {
            delta.lazyClear();
            for (int i = 0; i < 4; i++) {
                first[i] = 0;
            }
            resetStamp();
        }
        assert this.getTimeStamp() == ((TimeStampedObject)delta).getTimeStamp()
                :"Delta and monitor desynchronized. deltamonitor.freeze() is called " +
                "but no value has been removed since the last call.";
        for (int i = 0; i < 3; i++) {
            this.last[i] = delta.getSize(i);
        }
        this.last[3] = delta.getSize(IGraphDelta.EDGE_ENFORCED_TAIL);
    }

    /**
     * Applies proc to every vertex which has just been removed or enforced, depending on evt.
     * @param proc    an incremental procedure over vertices
     * @param evt    either ENFORCENODE or REMOVENODE
     * @throws ContradictionException if a failure occurs
     */
    public void forEachNode(IntProcedure proc, GraphEventType evt) throws ContradictionException {
        freeze();
        int type;
        if (evt == GraphEventType.REMOVE_NODE) {
            type = IGraphDelta.NODE_REMOVED;
        } else if (evt == GraphEventType.ADD_NODE) {
            type = IGraphDelta.NODE_ENFORCED;
        } else {
            throw new UnsupportedOperationException("The event in parameter should be ADD_NODE or REMOVE_NODE");
        }
        while (first[type] < last[type]) {
            if (delta.getCause(first[type], type) != propagator) {
                proc.execute(delta.get(first[type], type));
            }
            first[type]++;
        }
    }

    /**
     * Applies proc to every edge which has just been removed or enforced, depending on evt.
     * @param proc    an incremental procedure over edges
     * @param evt    either ENFORCE_EDGE or REMOVE_EDGE
     * @throws ContradictionException if a failure occurs
     */
    public void forEachEdge(PairProcedure proc, GraphEventType evt) throws ContradictionException {
        freeze();
        int idx;
        int tailType;
        int headType;
        if (evt == GraphEventType.REMOVE_EDGE) {
            idx = 2;
            tailType = IGraphDelta.EDGE_REMOVED_TAIL;
            headType = IGraphDelta.EDGE_REMOVED_HEAD;
        } else if (evt == GraphEventType.ADD_EDGE) {
            idx = 3;
            tailType = IGraphDelta.EDGE_ENFORCED_TAIL;
            headType = IGraphDelta.EDGE_ENFORCED_HEAD;
        } else {
            throw new UnsupportedOperationException("The event in parameter should be ADD_EDGE or REMOVE_EDGE");
        }
        while (first[idx] < last[idx]) {
            if (delta.getCause(first[idx], tailType) != propagator) {
                proc.execute(delta.get(first[idx], tailType), delta.get(first[idx], headType));
            }
            first[idx]++;
        }
    }
}
