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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

public abstract class AbstractTreeFinder {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected final static boolean FILTER = false;
    // INPUT
    protected UndirectedGraph g;    // graph
    protected int n;                // number of nodes
    // OUTPUT
    protected UndirectedGraph Tree;
    protected double treeCost;
    // PROPAGATOR
    protected GraphLagrangianRelaxation propHK;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public AbstractTreeFinder(int nbNodes, GraphLagrangianRelaxation propagator) {
        n = nbNodes;
        Tree = new UndirectedGraph(n, SetType.LINKED_LIST, false);
        propHK = propagator;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public abstract void computeMST(double[][] costMatrix, UndirectedGraph graph) throws ContradictionException;

    public abstract void performPruning(double UB) throws ContradictionException;

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    public UndirectedGraph getMST() {
        return Tree;
    }

    public double getBound() {
        return treeCost;
    }

    public double getRepCost(int from, int to) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
