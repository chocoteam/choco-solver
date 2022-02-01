/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.tree;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.StrongConnectivityFinder;

import java.util.BitSet;

/**
 * Arborescence constraint (simplification from tree constraint) based on dominators
 *
 * @author Jean-Guillaume Fages
 */
public class PropArborescence extends PropArborescences {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected final int root;
    protected final BitSet visited;
    protected final int[] fifo;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************


    public PropArborescence(DirectedGraphVar graph, int root) {
        this(graph, root, false);
    }

    public PropArborescence(DirectedGraphVar graph, int root, boolean simple) {
        super(graph, simple);
        this.root = root;
        this.visited = new BitSet(n);
        this.fifo = new int[n];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evt) throws ContradictionException {
        g.enforceNode(root, this);
        // explore the graph from the root
        explore();
        // remove unreachable nodes
        for (int o = visited.nextClearBit(0); o < n; o = visited.nextClearBit(o + 1)) {
            g.removeNode(o, this);
        }
        super.propagate(evt);
    }

    @Override
    protected void reset() {
        // reset data structure
        for (int i = 0; i < n + 1; i++) {
            connectedGraph.getSuccessorsOf(i).clear();
            connectedGraph.getPredecessorsOf(i).clear();
        }
        for (int i = 0; i < n; i++) {
            for (int y : g.getPotentialPredecessorOf(i)) {
                connectedGraph.addEdge(y, i);
            }
            if (!g.getPotentialNodes().contains(i)) {
                connectedGraph.addEdge(n, i);
            }
        }
        connectedGraph.addEdge(n, root);
    }

    protected void explore() {
        visited.clear();
        int first = 0;
        int last = 0;
        int i = root;
        fifo[last++] = i;
        visited.set(i);
        while (first < last) {
            i = fifo[first++];
            for (int j : g.getPotentialSuccessorsOf(i)) {
                if (!visited.get(j)) {
                    visited.set(j);
                    fifo[last++] = j;
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        // Assert that the root is in UB
        if (!g.getPotentialNodes().contains(root)) {
            return ESat.FALSE;
        }
        // First assert that the graph is acyclic, which is equivalent to asserting that the number of strongly
        // connected components is equal to the number of mandatory nodes
        StrongConnectivityFinder scfinder = new StrongConnectivityFinder(g.getLB());
        scfinder.findAllSCC();
        if (g.getMandatoryNodes().size() - scfinder.getNbSCC() > 0) {
            return ESat.FALSE;
        }
        // Assert that the root has no mandatory predecessor
        if (g.getMandatoryPredecessorsOf(root).size() > 0) {
            return ESat.FALSE;
        }
        // Then assert that mandatory nodes (except root) have no more that one mandatory predecessor
        // and at least one potential predecessor.
        for (int i : g.getMandatoryNodes()) {
            if (i != root && g.getMandatoryPredecessorsOf(i).size() > 1 && g.getPotentialPredecessorOf(i).size() == 0) {
                return ESat.FALSE;
            }
        }
        if (g.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
