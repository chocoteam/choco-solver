/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cycles;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Simple NoSubtour of Caseau-Laburthe adapted to the undirected case
 *
 * @author Jean-Guillaume Fages
 */
public class PropCycle extends Propagator<UndirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final UndirectedGraphVar g;
    private final IGraphDeltaMonitor gdm;
    private final int n;
    private final IStateInt[] e1;
    private final IStateInt[] e2;
    private final IStateInt[] size;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************


    public PropCycle(UndirectedGraphVar graph) {
        super(new UndirectedGraphVar[]{graph}, PropagatorPriority.LINEAR, true);
        g = graph;
        gdm = g.monitorDelta(this);
        this.n = g.getNbMaxNodes();
        e1 = new IStateInt[n];
        e2 = new IStateInt[n];
        size = new IStateInt[n];
        IEnvironment environment = graph.getEnvironment();
        for (int i = 0; i < n; i++) {
            e1[i] = environment.makeInt(i);
            e2[i] = environment.makeInt(i);
            size[i] = environment.makeInt(1);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            if (g.getMandatoryNodes().size() > 1) {
                for (int i = 0; i < n; i++) {
                    e1[i].set(i);
                    e2[i].set(i);
                    size[i].set(1);
                    g.removeEdge(i, i, this);
                }
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
            gdm.startMonitoring();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.forEachEdge(this::enforce, GraphEventType.ADD_EDGE);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.ADD_EDGE.getMask();
    }

    @Override
    public ESat isEntailed() {
        ISet nodes = g.getMandatoryNodes();
        // Graph with one node and a loop case
        if (g.isInstantiated() && nodes.size() == 1 && g.getMandatoryNeighborsOf(nodes.toArray()[0]).size() == 1) {
            return ESat.TRUE;
        }
        for (int i : nodes) {
            if (g.getMandatoryNeighborsOf(i).size() > 2 || g.getPotentialNeighborsOf(i).size() < 2) {
                return ESat.FALSE;
            }
        }
        if (g.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    private void enforce(int i, int j) throws ContradictionException {
        int ext1 = getExt(i);
        int ext2 = getExt(j);
        int t = size[ext1].get() + size[ext2].get();
        setExt(ext1, ext2);
        setExt(ext2, ext1);
        size[ext1].set(t);
        size[ext2].set(t);
        if (t > 2) {
            if (t < g.getMandatoryNodes().size()) {
                g.removeEdge(ext1, ext2, this);
            } else if (g.getMandatoryNodes().size() == g.getPotentialNodes().size()) {
                g.enforceEdge(ext1, ext2, this);
            }
        }
    }

    private int getExt(int i) {
        return (e1[i].get() == i) ? e2[i].get() : e1[i].get();
    }

    private void setExt(int i, int ext) {
        if (e1[i].get() == i) {
            e2[i].set(ext);
        } else {
            e1[i].set(ext);
        }
    }
}
