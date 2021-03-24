/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.tsp.lagrangian;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * TSP Lagrangian relaxation
 * Inspired from the work of Held & Karp
 * and Benchimol et. al. (Constraints 2012)
 *
 * @author Jean-Guillaume Fages
 */
public class PropLagrOneTreeIntVar extends PropLagrOneTree {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar[] succ;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropLagrOneTreeIntVar(IntVar[] graph, IntVar cost, int[][] costMatrix, boolean waitFirstSol) {
        super(ArrayUtils.append(graph, new IntVar[]{cost}), costMatrix);
        this.succ = graph;
        g = new UndirectedGraph(n, SetType.BIPARTITESET, true);
        obj = cost;
        this.waitFirstSol = waitFirstSol;
        assert checkSymmetry(costMatrix) : "TSP matrix should be symmetric";
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    protected void rebuild() {
        mandatoryArcsList.clear();
        for (int i = 0; i < n; i++) {
            g.getNeighborsOf(i).clear();
            if (succ[i].isInstantiated()) {
                int j = succ[i].getValue();
                mandatoryArcsList.add(i * n + j); // todo check no need to have i < j
            }
        }
        for (int i = 0; i < n; i++) {
            IntVar v = succ[i];
            int ub = v.getUB();
            for (int j = v.getLB(); j <= ub; j = v.nextValue(j)) {
                g.addEdge(i, j);
            }
        }
    }

    @Override
    public void remove(int from, int to) throws ContradictionException {
        succ[from].removeValue(to, this);
        succ[to].removeValue(from, this);
    }

    @Override
    public void enforce(int from, int to) throws ContradictionException {
        if (!succ[from].contains(to)) {
            succ[to].instantiateTo(from, this);
        }
        if (!succ[to].contains(from)) {
            succ[from].instantiateTo(to, this);
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;// it is just implied filtering
    }

    @Override
    public boolean isMandatory(int i, int j) {
        return succ[i].isInstantiatedTo(j) || succ[j].isInstantiatedTo(i);
    }

    private static boolean checkSymmetry(int[][] costMatrix) {
        int n = costMatrix.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (costMatrix[i][j] != costMatrix[j][i]) {
                    return false;
                }
            }
        }
        return true;
    }
}
