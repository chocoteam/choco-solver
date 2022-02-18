/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.symmbreaking;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.PairProcedure;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Моклев Вячеслав
 */
public class PropIncrementalAdjacencyUndirectedMatrix extends Propagator<Variable> {

    private final UndirectedGraphVar graph;
    private final IGraphDeltaMonitor gdm;
    private final PairProcedure enforce;
    private final PairProcedure remove;
    private int n;
    private final BoolVar[] t;

    public PropIncrementalAdjacencyUndirectedMatrix(UndirectedGraphVar graphVar, BoolVar[] t) {
        super(ArrayUtils.append(new Variable[]{graphVar}, t), PropagatorPriority.LINEAR, true);
        graph = graphVar;
        gdm = graph.monitorDelta(this);
        enforce = (from, to) -> {
            t[from + to * n].instantiateTo(1, this);
            t[to + from * n].instantiateTo(1, this);
        };
        remove = (from, to) -> {
            t[from + to * n].instantiateTo(0, this);
            t[to + from * n].instantiateTo(0, this);
        };
        n = graphVar.getNbMaxNodes();
        this.t = t;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // first, nonincremental propagation
        for (int i = 0; i < n; i++) {
            t[i + i * n].instantiateTo(0, this);
        }
        propagateGraphChanged();
        propagateTChanged();
        // initializing incremental data-structures
        gdm.startMonitoring();
    }

    private void propagateGraphChanged() throws ContradictionException {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (t[i + j * n].isInstantiatedTo(1)) {
                    graph.enforceEdge(i, j, this);
                }
                if (t[i + j * n].isInstantiatedTo(0)) {
                    graph.removeEdge(i, j, this);
                }
            }
        }
    }

    private void propagateTChanged() throws ContradictionException {
        for (int u = 0; u < n; u++) {
            for (int v : graph.getMandatoryNeighborsOf(u)) {
                t[u + v * n].instantiateTo(1, this);
            }
        }
        for (int u = 0; u < n; u++) {
            Set<Integer> set = new HashSet<>();
            for (int v : graph.getPotentialNeighborsOf(u)) {
                set.add(v);
            }
            for (int v = 0; v < n; v++) {
                if (!set.contains(v)) {
                    t[u + v * n].instantiateTo(0, this);
                }
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        // incremental propagation
        if (idxVarInProp == 0) {
            gdm.forEachEdge(enforce, GraphEventType.ADD_EDGE);
            gdm.forEachEdge(remove, GraphEventType.REMOVE_EDGE);
        } else {
            propagateTChanged();
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return GraphEventType.ADD_EDGE.getMask() | GraphEventType.REMOVE_EDGE.getMask();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            ISet children = graph.getMandatoryNeighborsOf(i);
            for (int j = 0; j < n; j++) {
                if ((t[i + j * n].isInstantiatedTo(0) || t[j + i * n].isInstantiatedTo(0)) && children.contains(j)) {
                    return ESat.FALSE;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            ISet children = graph.getPotentialNeighborsOf(i);
            for (int j = 0; j < n; j++) {
                if ((t[i + j * n].isInstantiatedTo(1) || t[j + i * n].isInstantiatedTo(1)) && !children.contains(j)) {
                    return ESat.FALSE;
                }
            }
        }
        if (graph.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
