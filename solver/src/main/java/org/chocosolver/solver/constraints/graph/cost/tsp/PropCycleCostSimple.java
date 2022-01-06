/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.tsp;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.Iterator;

/**
 * Compute the cost of the graph by summing edge costs
 * Supposes that each node must have two neighbors (cycle)
 * - For minimization problem
 */
public class PropCycleCostSimple extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected UndirectedGraphVar g;
    protected int n;
    protected IntVar sum;
    protected int[][] distMatrix;
    protected int[] replacementCost;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropCycleCostSimple(UndirectedGraphVar graph, IntVar obj, int[][] costMatrix) {
        super(new Variable[]{graph, obj}, PropagatorPriority.LINEAR, false);
        g = graph;
        sum = obj;
        n = g.getNbMaxNodes();
        distMatrix = costMatrix;
        replacementCost = new int[n];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return GraphEventType.REMOVE_EDGE.getMask() + GraphEventType.ADD_EDGE.getMask();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public ESat isEntailed() {
        int minSum = 0;
        int maxSum = 0;
        for (int i = 0; i < n; i++) {
            ISet env = g.getPotentialNeighborsOf(i);
            ISet ker = g.getMandatoryNeighborsOf(i);
            for (int j : env) {
                if (i <= j) {
                    maxSum += distMatrix[i][j];
                    if (ker.contains(j)) {
                        minSum += distMatrix[i][j];
                    }
                }
            }
        }
        if (maxSum < 0) {
            maxSum = Integer.MAX_VALUE;
        }
        if (minSum > sum.getUB() || maxSum < sum.getLB()) {
            return ESat.FALSE;
        }
        if (maxSum == minSum && sum.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int minSum = 0;
        int maxSum = 0;
        for (int i = 0; i < n; i++) {
            minSum += findTwoBest(i);
            maxSum += findTwoWorst(i);
        }
        if (maxSum % 2 != 0) {
            maxSum++;
        }
        if (minSum % 2 != 0) {
            minSum--;
        }
        minSum /= 2;
        maxSum /= 2;
        if (maxSum < 0) {
            maxSum = Integer.MAX_VALUE;
        }
        sum.updateLowerBound(minSum, this);
        sum.updateUpperBound(maxSum, this);
        filter(minSum);
    }

    protected void filter(int minSum) throws ContradictionException {
        ISet succs;
        int delta = sum.getUB() - minSum;
        for (int i = 0; i < n; i++) {
            succs = g.getPotentialNeighborsOf(i);
            for (int j : succs) {
                if (i < j && !g.getMandatoryNeighborsOf(i).contains(j)) {
                    if (replacementCost[i] == -1 || replacementCost[j] == -1) {
                        g.removeEdge(i, j, this);
                    }
                    if ((2 * distMatrix[i][j] - replacementCost[i] - replacementCost[j]) / 2 > delta) {
                        g.removeEdge(i, j, this);
                    }
                }
            }
        }
    }

    protected int findTwoBest(int i) throws ContradictionException {
        if (g.getMandatoryNeighborsOf(i).isEmpty()) {
            int mc1 = getBestNot(i, -2);
            int cost = distMatrix[i][getBestNot(i, mc1)];
            replacementCost[i] = cost;
            return distMatrix[i][mc1] + cost;
        } else {
            Iterator<Integer> it = g.getMandatoryNeighborsOf(i).iterator();
            int mc1 = it.next();
            if (it.hasNext()) {
                int mc2 = it.next();
                replacementCost[i] = -1;
                return distMatrix[i][mc1] + distMatrix[i][mc2];
            }
            int cost = distMatrix[i][getBestNot(i, mc1)];
            replacementCost[i] = cost;
            return distMatrix[i][mc1] + cost;

        }
    }

    protected int getBestNot(int i, int not) throws ContradictionException {
        ISet nei = g.getPotentialNeighborsOf(i);
        int cost = -1;
        int idx = -1;
        for (int j : nei) {
            if (j != not && (idx == -1 || cost > distMatrix[i][j])) {
                idx = j;
                cost = distMatrix[i][j];
            }
        }
        if (idx == -1) {
            fails();
        }
        return idx;
    }

    protected int findTwoWorst(int i) throws ContradictionException {
        if (g.getMandatoryNeighborsOf(i).isEmpty()) {
            int mc1 = getWorstNot(i, -2);
            return distMatrix[i][mc1] + distMatrix[i][getWorstNot(i, mc1)];
        } else {
            Iterator<Integer> it = g.getMandatoryNeighborsOf(i).iterator();
            int mc1 = it.next();
            if (it.hasNext()) {
                return distMatrix[i][mc1] + distMatrix[i][it.next()];
            }
            return distMatrix[i][mc1] + distMatrix[i][getWorstNot(i, mc1)];
        }
    }

    protected int getWorstNot(int i, int not) throws ContradictionException {
        ISet nei = g.getPotentialNeighborsOf(i);
        int cost = -1;
        int idx = -1;
        for (int j : nei) {
            if (j != not && (idx == -1 || cost < distMatrix[i][j])) {
                idx = j;
                cost = distMatrix[i][j];
            }
        }
        if (idx == -1) {
            fails();
        }
        return idx;
    }

}
