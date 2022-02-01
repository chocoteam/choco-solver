/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.trees.lagrangian;

import org.chocosolver.solver.constraints.graph.cost.GraphLagrangianRelaxation;
import org.chocosolver.solver.constraints.graph.cost.tsp.heap.FastSimpleHeap;
import org.chocosolver.solver.constraints.graph.cost.tsp.heap.ISimpleHeap;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.BitSet;

public class PrimMSTFinder extends AbstractTreeFinder {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected double[][] costs;
    protected ISimpleHeap heap;
    protected BitSet inTree;
    protected int[] mate;
    protected int tSize;
    protected double minVal;
    protected double maxTArc;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PrimMSTFinder(int nbNodes, GraphLagrangianRelaxation propagator) {
        super(nbNodes, propagator);
        heap = new FastSimpleHeap(nbNodes);
//		heap = new FastArrayHeap(nbNodes);
        inTree = new BitSet(n);
        mate = new int[n];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void computeMST(double[][] costs, UndirectedGraph graph) throws ContradictionException {
        g = graph;
        for (int i = 0; i < n; i++) {
            Tree.getNeighborsOf(i).clear();
        }
        this.costs = costs;
        heap.clear();
        inTree.clear();
        treeCost = 0;
        tSize = 0;
        prim();
    }

    protected void prim() throws ContradictionException {
        minVal = propHK.getMinArcVal();
        if (FILTER) {
            maxTArc = minVal;
        }
        addNode(0);
        int from, to;
        while (tSize < n - 1 && !heap.isEmpty()) {
            to = heap.removeFirstElement();
            from = mate[to];
            addArc(from, to);
        }
        if (tSize != n - 1) {
            propHK.contradiction();
        }
    }

    protected void addArc(int from, int to) {
        if (Tree.containsEdge(from, to)) {
            throw new UnsupportedOperationException();
        }
        Tree.addEdge(from, to);
        treeCost += costs[from][to];
        if (FILTER && !propHK.isMandatory(from, to)) {
            maxTArc = Math.max(maxTArc, costs[from][to]);
        }
        tSize++;
        addNode(to);
    }

    protected void addNode(int i) {
        if (!inTree.get(i)) {
            inTree.set(i);
            ISet nei = g.getNeighborsOf(i);
            for (int j : nei) {
                if (!inTree.get(j)) {
                    if (propHK.isMandatory(i, j)) {
                        heap.addOrUpdateElement(j, Integer.MIN_VALUE);
                        mate[j] = i;
                    } else {
                        if (heap.addOrUpdateElement(j, costs[i][j])) {
                            mate[j] = i;
                        }
                    }
                }
            }
        }
    }

    public void performPruning(double UB) throws ContradictionException {
        if (FILTER) {
            double delta = UB - treeCost;
            ISet nei;
            for (int i = 0; i < n; i++) {
                nei = g.getNeighborsOf(i);
                for (int j : nei) {
                    if (i < j && (!Tree.containsEdge(i, j)) && costs[i][j] - maxTArc > delta) {
                        propHK.remove(i, j);
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException("bound computation only, no filtering!");
        }
    }
}
