/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.tree;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.DirectedGraphVar;

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
}
