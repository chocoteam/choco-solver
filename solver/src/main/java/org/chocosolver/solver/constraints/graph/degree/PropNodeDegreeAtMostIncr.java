/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.degree;

import gnu.trove.stack.array.TIntArrayStack;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.IncidentSet;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.Orientation;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * Propagator that ensures that a node has at most N successors/predecessors/neighbors
 *
 * @author Jean-Guillaume Fages
 */
public class PropNodeDegreeAtMostIncr extends Propagator<GraphVar<?>> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final GraphVar<?> g;
    private final int[] degrees;
    private final IncidentSet target;
    private final IGraphDeltaMonitor gdm;
    private final PairProcedure proc;
    private final IntProcedure nodeProc;
    private final TIntArrayStack stack = new TIntArrayStack();

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNodeDegreeAtMostIncr(DirectedGraphVar graph, Orientation setType, int degree) {
        this(graph, setType, buildArray(degree, graph.getNbMaxNodes()));
    }

    public PropNodeDegreeAtMostIncr(DirectedGraphVar graph, Orientation setType, int[] degrees) {
        super(new DirectedGraphVar[]{graph}, PropagatorPriority.BINARY, true);
        g = graph;
        gdm = g.monitorDelta(this);
        this.degrees = degrees;
        switch (setType) {
            case SUCCESSORS:
                target = new IncidentSet.SuccessorsSet();
                proc = (i, j) -> stack.push(i);
                break;
            case PREDECESSORS:
                target = new IncidentSet.PredecessorsSet();
                proc = (i, j) -> stack.push(j);
                break;
            default:
                throw new UnsupportedOperationException("wrong parameter: use either PREDECESSORS or SUCCESSORS");
        }
        nodeProc = stack::push;
    }

    public PropNodeDegreeAtMostIncr(UndirectedGraphVar graph, int degree) {
        this(graph, buildArray(degree, graph.getNbMaxNodes()));
    }

    public PropNodeDegreeAtMostIncr(final UndirectedGraphVar graph, int[] degrees) {
        super(new UndirectedGraphVar[]{graph}, PropagatorPriority.BINARY, true);
        target = new IncidentSet.SuccessorsSet();
        g = graph;
        gdm = g.monitorDelta(this);
        this.degrees = degrees;
        proc = (i, j) -> {
            stack.push(i);
            stack.push(j);
        };
        nodeProc = stack::push;
    }

    private static int[] buildArray(int degree, int n) {
        int[] degrees = new int[n];
        for (int i = 0; i < n; i++) {
            degrees[i] = degree;
        }
        return degrees;
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            ISet act = g.getPotentialNodes();
            for (int node : act) {
                stack.push(node);
            }
            gdm.startMonitoring();
        }
        try {
            while (stack.size() > 0) {
                checkAtMost(stack.pop());
            }
        } finally {
            stack.clear();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.forEachNode(nodeProc, GraphEventType.REMOVE_NODE);
        gdm.forEachEdge(proc, GraphEventType.ADD_EDGE);
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.ADD_EDGE.getMask() + GraphEventType.REMOVE_NODE.getMask();
    }

    @Override
    public ESat isEntailed() {
        ISet act = g.getMandatoryNodes();
        for (int i : act) {
            if (target.getPotentialSet(g, i).size() > degrees[i]) {
                return ESat.FALSE;
            }
        }
        if (!g.isInstantiated()) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    /**
     * When a node has more than N successors/predecessors/neighbors then it must be removed,
     * (which results in a failure)
     * If it has N successors/predecessors/neighbors in the kernel then other incident edges
     * should be removed
     */
    private void checkAtMost(int i) throws ContradictionException {
        ISet pot = target.getPotentialSet(g, i);
        ISet ker = target.getMandatorySet(g, i);
        int kerSize = ker.size();
        if (kerSize > degrees[i]) {
            for (int s : g.getPotentialPredecessorOf(i)) {
                stack.push(s);
            }
            for (int s : g.getPotentialSuccessorsOf(i)) {
                stack.push(s);
            }
            g.removeNode(i, this);
        } else if (kerSize == degrees[i] && pot.size() > kerSize) {
            for (int s : pot) {
                if (!ker.contains(s)) {
                    target.remove(g, i, s, this);
                    stack.push(s);
                }
            }
        }
    }
}
