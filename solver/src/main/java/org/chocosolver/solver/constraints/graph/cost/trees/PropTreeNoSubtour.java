/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 03/10/11
 * Time: 19:56
 */

package org.chocosolver.solver.constraints.graph.cost.trees;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.PairProcedure;

import java.util.BitSet;

/**
 * Simple NoSubtour applied to (undirected) tree/forest
 */
public class PropTreeNoSubtour extends Propagator<UndirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private UndirectedGraphVar g;
    private IGraphDeltaMonitor gdm;
    private int n;
    private PairProcedure arcEnforced;
    private IStateInt[] color, size;
    // list
    private int[] fifo;
    private int[] mate;
    private BitSet in;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that graph has no cycle
     * runs in O(n) per instantiation event
     *
     * @param graph
     */
    public PropTreeNoSubtour(UndirectedGraphVar graph) {
        super(new UndirectedGraphVar[]{graph}, PropagatorPriority.LINEAR, true);
        g = graph;
        gdm = g.monitorDelta(this);
        this.n = g.getNbMaxNodes();
        arcEnforced = new EnfArc();
        fifo = new int[n];
        mate = new int[n];
        in = new BitSet(n);
        color = new IStateInt[n];
        size = new IStateInt[n];
        IEnvironment environment = graph.getEnvironment();
        for (int i = 0; i < n; i++) {
            color[i] = environment.makeInt(i);
            size[i] = environment.makeInt(1);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            color[i].set(i);
            size[i].set(1);
            mate[i] = -1;
        }
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = g.getMandatoryNeighborsOf(i);
            for (int j : nei) {
                if (i < j) {
                    enforce(i, j);
                }
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.forEachEdge(arcEnforced, GraphEventType.ADD_EDGE);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.ADD_EDGE.getMask();
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; //not implemented
    }

    private void enforce(int i, int j) throws ContradictionException {
        if (size[color[i].get()].get() > size[color[j].get()].get()) {
            enforce(j, i);
            return;
        }
        if (i == j) {
            throw new UnsupportedOperationException();
        }
        int ci = color[i].get();
        int cj = color[j].get();
        if (ci == cj) {
            fails();
        }
        int idxFirst = 0;
        int idxLast = 0;
        in.clear();
        in.set(i);
        fifo[idxLast++] = i;
        int x, ck;
        mate[i] = j;
        while (idxFirst < idxLast) {
            x = fifo[idxFirst++];
            for (int k : g.getPotentialNeighborsOf(x)) {
                if (k != mate[x]) {
                    ck = color[k].get();
                    if (ck == cj) {
                        g.removeEdge(x, k, this);
                    } else {
                        if (ck == ci && !in.get(k)) {
                            in.set(k);
                            fifo[idxLast++] = k;
                            mate[k] = x;
                        }
                    }
                }
            }
            color[x].set(cj);
        }
        size[cj].add(size[ci].get());
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    private class EnfArc implements PairProcedure {
        @Override
        public void execute(int i, int j) throws ContradictionException {
            enforce(i, j);
        }
    }
}
