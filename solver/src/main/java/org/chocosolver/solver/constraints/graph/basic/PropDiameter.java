/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.basic;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;

import java.util.BitSet;

/**
 * Propagator for the diameter constraint
 *
 * @author Jean-Guillaume Fages
 *
 * UPDATED 16/03/2021 by Dimitri Justeau-Allaire: implementation of isEntailed() method an loose upper bound
 * for diameter, as the previous implementation was incorrect and finding a tight bound implies finding the
 * largest path in the upper bound graph, which is a NP-Complete problem.
 */
public class PropDiameter extends Propagator<GraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final GraphVar g;
    private final IntVar diameter;
    private final BitSet visited;
    private final TIntArrayList set;
    private final TIntArrayList nextSet;


    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropDiameter(GraphVar graph, IntVar maxDiam) {
        super(new GraphVar[]{graph}, PropagatorPriority.LINEAR, false);
        this.g = graph;
        this.diameter = maxDiam;
        visited = new BitSet(g.getNbMaxNodes());
        set = new TIntArrayList();
        nextSet = new TIntArrayList();
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int minDiam = computeLB();
        if (g.isInstantiated()) {
            diameter.instantiateTo(minDiam, this);
        }
        else {
            int nbEdges = nbEdgesUB(); // trivial upper bound for diameter, getting a tight bound implies finding the
                                       // largest path in the upper bound graph in the general case, which is a
                                       // NP-Complete problem.
            diameter.updateLowerBound(minDiam, this);
            diameter.updateUpperBound(nbEdges, this);
        }
    }

    private int computeLB() {
        if (g.getMandatoryNodes().size() <= 1) {
            return 0;
        }
        return bfsLB();
    }

    private int bfsLB() {
        int maxDepth = 0;
        for (int source : g.getMandatoryNodes()) {
            boolean[] visited = new boolean[g.getNbMaxNodes()];
            int[] queue = new int[g.getNbMaxNodes()];
            int front = 0;
            int rear = 0;
            int current;
            int[] depth = new int[g.getNbMaxNodes()];
            depth[source] = 0;
            queue[front] = source;
            rear++;
            while (front != rear) {
                current = queue[front++];
                visited[current] = true;
                for (int i : g.getPotentialSuccessorsOf(current)) {
                    if (!visited[i]) {
                        depth[i] = depth[current] + 1;
                        if (g.getMandatoryNodes().contains(i)) {
                            maxDepth = Math.max(maxDepth, depth[i]);
                        }
                        queue[rear++] = i;
                        visited[i] = true;
                    }
                }
            }
        }
        return maxDepth;
    }

    private int nbEdgesUB() {
        int nbEdges = 0;
        for (int i : g.getPotentialNodes()) {
            nbEdges += g.getPotentialSuccessorsOf(i).size();
        }
        if (g instanceof UndirectedGraphVar) {
            nbEdges /= 2;
        }
        return nbEdges;
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        int minDiam = computeLB();
        int nbEdges = nbEdgesUB();
        if (minDiam > diameter.getUB() || nbEdges < diameter.getLB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            if (minDiam == diameter.getValue()) {
                return ESat.TRUE;
            }
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }
}
