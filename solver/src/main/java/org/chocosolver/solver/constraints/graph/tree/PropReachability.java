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

/**
 * Every vertex is reachable from the root
 * Removes unreachable nodes
 * Enforces dominator nodes
 * Enforces dominator arc
 *
 * @author Jean-Guillaume Fages
 */
public class PropReachability extends PropArborescence {

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropReachability(DirectedGraphVar graph, int root) {
        super(graph, root);
    }

    public PropReachability(DirectedGraphVar graph, int root, boolean simple) {
        super(graph, root, simple);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    protected void remBackArcs() throws ContradictionException {
        // reachability allows circuits
        // it does not have to remove backward arcs
    }

    @Override
    public ESat isEntailed() {
        if (!g.getPotentialNodes().contains(root)) {
            return ESat.FALSE;
        }
        // Test reachability from root to mandatory nodes through potential edges with a DFS
        boolean[] visited = new boolean[g.getNbMaxNodes()];
        int[] parent = new int[g.getNbMaxNodes()];
        visited[root] = true;
        parent[root] = root;
        int[] stack = new int[g.getNbMaxNodes()];
        int last = 0;
        stack[last] = root;
        while (last >= 0) {
            int current = stack[last--];
            for (int j : g.getPotentialSuccessorsOf(current)) {
                if (!visited[j]) {
                    visited[j] = true;
                    parent[j] = current;
                    stack[++last] = j;
                }
            }
        }
        for (int i : g.getMandatoryNodes()) {
            if (!visited[i]) {
                return ESat.FALSE;
            }
        }
        if (g.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
