/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.inclusion;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * @author Jean-Guillaume Fages
 */
public class PropInclusion extends Propagator<GraphVar<?>> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final GraphVar<?>[] g; // g[0] in g[1]
    private final IGraphDeltaMonitor[] gdm;
    private final IntProcedure[] prNode;
    private final PairProcedure[] prArc;
    private final GraphEventType[] etNode;
    private final GraphEventType[] etArcs;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropInclusion(GraphVar<?> g1, GraphVar<?> g2) {
        super(new GraphVar[]{g1, g2}, PropagatorPriority.LINEAR, true);
        g = new GraphVar[]{g1, g2};
        gdm = new IGraphDeltaMonitor[]{g1.monitorDelta(this), g2.monitorDelta(this)};
        prNode = new IntProcedure[]{
                i -> g[1].enforceNode(i, this),
                i -> {
                    if (i < g[0].getNbMaxNodes()) {
                        g[0].removeNode(i, this);
                    }
                }
        };
        prArc = new PairProcedure[]{
                (i, j) -> g[1].enforceEdge(i, j, this),
                (i, j) -> {
                    if (i < g[0].getNbMaxNodes() && j < g[0].getNbMaxNodes()) {
                        g[0].removeEdge(i, j, this);
                    }
                },
        };
        etNode = new GraphEventType[]{GraphEventType.ADD_NODE, GraphEventType.REMOVE_NODE};
        etArcs = new GraphEventType[]{GraphEventType.ADD_EDGE, GraphEventType.REMOVE_EDGE};
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (g[0].getNbMaxNodes() != g[1].getNbMaxNodes()) {
            for (int i = g[1].getNbMaxNodes(); i < g[0].getNbMaxNodes(); i++) {
                g[0].removeNode(i, this);
            }
        }
        ISet set = g[0].getMandatoryNodes();
        for (int i : set) {
            g[1].enforceNode(i, this);
            ISet suc = g[0].getMandatorySuccessorsOf(i);
            for (int j : suc) {
                g[1].enforceEdge(i, j, this);
            }
        }
        set = g[0].getPotentialNodes();
        for (int i : set) {
            if (!g[1].getPotentialNodes().contains(i)) {
                g[0].removeNode(i, this);
            } else {
                ISet suc = g[0].getPotentialSuccessorsOf(i);
                for (int j : suc) {
                    if (!g[1].getPotentialSuccessorsOf(i).contains(j)) {
                        g[1].removeEdge(i, j, this);
                    }
                }
            }
        }
        gdm[0].startMonitoring();
        gdm[1].startMonitoring();
    }

    @Override
    public void propagate(int vIdx, int evtmask) throws ContradictionException {
        gdm[vIdx].forEachNode(prNode[vIdx], etNode[vIdx]);
        gdm[vIdx].forEachEdge(prArc[vIdx], etArcs[vIdx]);
    }

    @Override
    public ESat isEntailed() {
        for (int i : g[0].getMandatoryNodes()) {
            if (!g[1].getPotentialNodes().contains(i)) {
                return ESat.FALSE;
            }
            for (int j : g[0].getPotentialSuccessorsOf(i)) {
                if (!g[1].getPotentialSuccessorsOf(i).contains(j)) {
                    return ESat.FALSE;
                }
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
