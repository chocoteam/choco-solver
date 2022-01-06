/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.trees;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * Compute the cost of the graph by summing edge costs
 * - For minimization problem
 */
public class PropTreeCostSimple extends Propagator<UndirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected UndirectedGraphVar g;
    private final IGraphDeltaMonitor gdm;
    private final PairProcedure edgeEnf;
    private final PairProcedure edgeRem;
    protected int n;
    protected IntVar sum;
    protected int[][] distMatrix;
    private final IStateInt minSum;
    private final IStateInt maxSum;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropTreeCostSimple(UndirectedGraphVar graph, IntVar obj, int[][] costMatrix) {
        super(new UndirectedGraphVar[]{graph}, PropagatorPriority.LINEAR, true);
        g = graph;
        sum = obj;
        n = g.getNbMaxNodes();
        distMatrix = costMatrix;
        IEnvironment environment = graph.getEnvironment();
        minSum = environment.makeInt(0);
        maxSum = environment.makeInt(0);
        gdm = g.monitorDelta(this);
        edgeEnf = (i, j) -> minSum.add(distMatrix[i][j]);
        edgeRem = (i, j) -> maxSum.add(-distMatrix[i][j]);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int min = 0;
        int max = 0;
        for (int i = 0; i < n; i++) {
            ISet nei = g.getPotentialNeighborsOf(i);
            for (int j : nei) {
                if (i <= j) {
                    max += distMatrix[i][j];
                    if (g.getMandatoryNeighborsOf(i).contains(j)) {
                        min += distMatrix[i][j];
                    }
                }
            }
        }
        minSum.set(min);
        maxSum.set(max);
        sum.updateLowerBound(min, this);
        sum.updateUpperBound(max, this);
        gdm.startMonitoring();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.forEachEdge(edgeEnf, GraphEventType.ADD_EDGE);
        gdm.forEachEdge(edgeRem, GraphEventType.REMOVE_EDGE);
        sum.updateLowerBound(minSum.get(), this);
        sum.updateUpperBound(maxSum.get(), this);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.REMOVE_EDGE.getMask() + GraphEventType.ADD_EDGE.getMask();
    }

    @Override
    public ESat isEntailed() {
        int min = 0;
        int max = 0;
        for (int i = 0; i < n; i++) {
            ISet nei = g.getPotentialNeighborsOf(i);
            for (int j : nei) {
                if (i <= j) {
                    max += distMatrix[i][j];
                    if (g.getMandatoryNeighborsOf(i).contains(j)) {
                        min += distMatrix[i][j];
                    }
                }
            }
        }
        if (min > sum.getUB() || max < sum.getLB()) {
            return ESat.FALSE;
        }
        if (min == max) {
            return ESat.TRUE;
        } else {
            return ESat.UNDEFINED;
        }
    }
}
