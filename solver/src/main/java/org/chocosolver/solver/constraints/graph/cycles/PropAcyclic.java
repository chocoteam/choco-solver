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

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.StrongConnectivityFinder;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.BitSet;

/**
 * Propagator for the no-cycle constraint (general case)
 *
 * @author Jean-Guillaume Fages
 */
public class PropAcyclic extends Propagator<GraphVar<?>> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final GraphVar<?> g;
    private final IGraphDeltaMonitor gdm;
    private final int n;
    private final BitSet rfFrom;
    private final BitSet rfTo;
    private final int[] fifo;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropAcyclic(GraphVar<?> g) {
        super(new GraphVar[]{g}, PropagatorPriority.LINEAR, true);
        this.g = g;
        this.n = g.getNbMaxNodes();
        this.fifo = new int[n];
        this.rfFrom = new BitSet(n);
        this.rfTo = new BitSet(n);
        this.gdm = g.monitorDelta(this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int idx) {
        return GraphEventType.ADD_EDGE.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            g.removeEdge(i, i, this);
            if (g.getMandatorySuccessorsOf(i).size() > 0) {
                for (int j = 0; j < n; j++) {
                    if (g.getMandatorySuccessorsOf(i).contains(j)) {
                        propagateIJ(i, j);
                    }
                }
            }
        }
        gdm.startMonitoring();
    }

    @Override
    public void propagate(int idx, int mask) throws ContradictionException {
        gdm.forEachEdge(this::propagateIJ, GraphEventType.ADD_EDGE);
    }


    private void propagateIJ(int from, int to) throws ContradictionException {
        if (g.isDirected()) {
            g.removeEdge(to, from, this);
        }
        int first, last, ik;
        // mark reachable from 'To'
        first = 0;
        last = 0;
        ik = to;
        rfTo.clear();
        fifo[last++] = ik;
        rfTo.set(ik);
        while (first < last) {
            ik = fifo[first++];
            ISet nei = g.getMandatorySuccessorsOf(ik);
            for (int j : nei) {
                if (j != from && !rfTo.get(j)) {
                    rfTo.set(j);
                    fifo[last++] = j;
                }
            }
        }
        // mark reachable from 'From'
        first = 0;
        last = 0;
        ik = from;
        rfFrom.clear();
        fifo[last++] = ik;
        rfFrom.set(ik);
        while (first < last) {
            ik = fifo[first++];
            ISet nei = g.getMandatoryPredecessorsOf(ik);
            for (int j : nei) {
                if (j != to && !rfFrom.get(j)) {
                    rfFrom.set(j);
                    fifo[last++] = j;
                }
            }
        }
        // filter arcs that would create a circuit
        for (int i : g.getPotentialNodes()) {
            if (rfTo.get(i)) {
                ISet nei = g.getPotentialSuccessorsOf(i);
                for (int j : nei) {
                    if (rfFrom.get(j) && (i != from || j != to) && (i != to || j != from)) {
                        g.removeEdge(i, j, this);
                    }
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        // If g is undirected, detect cycles in undirected graph variable LB with a DFS from each node
        if (g instanceof UndirectedGraphVar) {
            for (int root : g.getMandatoryNodes()) {
                boolean[] visited = new boolean[g.getNbMaxNodes()];
                int[] parent = new int[g.getNbMaxNodes()];
                visited[root] = true;
                parent[root] = root;
                int[] stack = new int[g.getNbMaxNodes()];
                int last = 0;
                stack[last] = root;
                while (last >= 0) {
                    int current = stack[last--];
                    for (int j : g.getMandatorySuccessorsOf(current)) {
                        if (visited[j]) {
                            if (j == current || j != parent[current]) {
                                return ESat.FALSE;
                            }
                        } else {
                            visited[j] = true;
                            parent[j] = current;
                            stack[++last] = j;
                        }
                    }
                }
            }
        } else {
            // If g is directed, assert that the number of strongly connected components
            // is equal to the number of mandatory nodes
            StrongConnectivityFinder scfinder = new StrongConnectivityFinder((DirectedGraph) g.getLB());
            scfinder.findAllSCC();
            if (g.getMandatoryNodes().size() - scfinder.getNbSCC() > 0) {
                return ESat.FALSE;
            }
        }
        if (!isCompletelyInstantiated()) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }
}
