/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.trees.lagrangian;

import gnu.trove.list.array.TIntArrayList;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.graph.cost.GraphLagrangianRelaxation;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Lagrangian relaxation of the DCMST problem
 */
public class PropLagrDCMST extends Propagator<Variable> implements GraphLagrangianRelaxation {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final UndirectedGraphVar gV;
    private UndirectedGraph g;
    private final IntVar obj;
    private final int n;
    private final int[][] originalCosts;
    private final double[][] costs;
    private final double[] penalities;
    private double totalPenalities;
    private UndirectedGraph mst;
    private final TIntArrayList mandatoryArcsList;
    private final AbstractTreeFinder HKfilter, HK;
    private boolean waitFirstSol;
    private int nbSprints;
    private final int[] maxDegree;
    private boolean firstPropag = true;
    private long nbSols = 0;
    private int objUB = -1;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator performing the Lagrangian relaxation of the Degree Constrained Minimum Spanning Tree Problem
     */
    public PropLagrDCMST(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, boolean waitFirstSol) {
        super(new Variable[]{graph, cost}, PropagatorPriority.CUBIC, false);
        gV = graph;
        n = gV.getNbMaxNodes();
        obj = cost;
        originalCosts = costMatrix;
        costs = new double[n][n];
        penalities = new double[n];
        totalPenalities = 0;
        mandatoryArcsList = new TIntArrayList();
        nbSprints = 30;
        this.maxDegree = maxDegree;
        HK = new PrimMSTFinder(n, this);
        HKfilter = new KruskalMSTGAC(n, this);
        this.waitFirstSol = waitFirstSol;
        g = new UndirectedGraph(n, SetType.BITSET, true);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                g.addEdge(i, j);
            }
        }
    }

    //***********************************************************************************
    // HK Algorithm(s)
    //***********************************************************************************

    private void lagrangianRelaxation() throws ContradictionException {
        int lb = obj.getLB();
        nbSprints = 30;
        if (nbSols != model.getSolver().getSolutionCount()
                || obj.getUB() < objUB
                || (firstPropag && !waitFirstSol)) {
            nbSols = model.getSolver().getSolutionCount();
            objUB = obj.getUB();
            convergeAndFilter();
            firstPropag = false;
            g = gV.getUB();
        } else {
            fastRun(2);
        }
        if (lb < obj.getLB()) {
            lagrangianRelaxation();
        }
    }

    private void fastRun(double coef) throws ContradictionException {
        convergeFast(coef);
        HKfilter.computeMST(costs, g);
        double hkb = HKfilter.getBound() - totalPenalities;
        mst = HKfilter.getMST();
        if (hkb - Math.floor(hkb) < 0.001) {
            hkb = Math.floor(hkb);
        }
        obj.updateLowerBound((int) Math.ceil(hkb), this);
        HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
    }

    private void convergeAndFilter() throws ContradictionException {
        double hkb;
        double alpha = 2;
        double beta = 0.5;
        double besthkb = -9999998;
        double oldhkb = -9999999;
        while (oldhkb + 0.001 < besthkb || alpha > 0.01) {
            oldhkb = besthkb;
            convergeFast(alpha);
            HKfilter.computeMST(costs, g);
            hkb = HKfilter.getBound() - totalPenalities;
            if (hkb > besthkb) {
                besthkb = hkb;
            }
            mst = HKfilter.getMST();
            if (hkb - Math.floor(hkb) < 0.00001) {
                hkb = Math.floor(hkb);
            }
            obj.updateLowerBound((int) Math.ceil(hkb), this);
            HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
            alpha *= beta;
        }
    }

    private void convergeFast(double alpha) throws ContradictionException {
        double besthkb = 0;
        double oldhkb = -20;
        while (oldhkb + 0.1 < besthkb) {
            oldhkb = besthkb;
            for (int i = 0; i < nbSprints; i++) {
                HK.computeMST(costs, g);
                mst = HK.getMST();
                double hkb = HK.getBound() - totalPenalities;
                if (hkb - Math.floor(hkb) < 0.001) {
                    hkb = Math.floor(hkb);
                }
                if (hkb > besthkb) {
                    besthkb = hkb;
                }
                obj.updateLowerBound((int) Math.ceil(hkb), this);
                if (updateStep(hkb, alpha)) return;
            }
        }
    }

    private boolean updateStep(double hkb, double alpha) {
        double nb2viol = 0;
        double target = obj.getUB();
        assert (target - hkb >= 0);
        if (target - hkb < 0.001) {
            target = hkb + 0.001;
        }
        int deg;
        for (int i = 0; i < n; i++) {
            deg = mst.getNeighborsOf(i).size();
            if (deg > maxDegree[i] || penalities[i] > 0) {
                nb2viol += (maxDegree[i] - deg) * (maxDegree[i] - deg);
            }
        }
        double step;
        if (nb2viol == 0) {
            return true;
        } else {
            step = alpha * (target - hkb) / nb2viol;
        }
        if (step < 0.0001) {
            return true;
        }
        double maxPen = 2 * obj.getUB();
        totalPenalities = 0;
        for (int i = 0; i < n; i++) {
            deg = mst.getNeighborsOf(i).size();
            penalities[i] += (deg - maxDegree[i]) * step;
            if (penalities[i] < 0 || g.getNeighborsOf(i).size() <= maxDegree[i]) {
                penalities[i] = 0;
            }
            if (penalities[i] > maxPen) {
                penalities[i] = maxPen;
            }
            assert !(penalities[i] > Double.MAX_VALUE / (n - 1) || penalities[i] < 0);
            totalPenalities += penalities[i] * maxDegree[i];
        }
        assert !(totalPenalities > Double.MAX_VALUE / (n - 1) || totalPenalities < 0);
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = g.getNeighborsOf(i);
            for (int j : nei) {
                if (i < j) {
                    costs[j][i] = costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
                }
            }
        }
        return false;
    }

    //***********************************************************************************
    // INFERENCE
    //***********************************************************************************

    public void remove(int from, int to) throws ContradictionException {
        gV.removeEdge(from, to, this);
        if (firstPropag) {
            g.removeEdge(from, to);
        }
    }

    public void enforce(int from, int to) throws ContradictionException {
        gV.enforceEdge(from, to, this);
    }

    public void contradiction() throws ContradictionException {
        fails();
    }

    //***********************************************************************************
    // PROP METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (waitFirstSol && model.getSolver().getSolutionCount() == 0) {
            return;//the UB does not allow to prune
        }
        // initialisation
        mandatoryArcsList.clear();
        ISet nei;
        totalPenalities = 0;
        for (int i = 0; i < n; i++) {
            totalPenalities += penalities[i] * maxDegree[i];
            nei = gV.getMandatoryNeighborsOf(i);
            for (int j : nei) {
                if (i < j) {
                    mandatoryArcsList.add(i * n + j);
                }
            }
            nei = g.getNeighborsOf(i);
            for (int j : nei) {
                if (i < j) {
                    costs[j][i] = costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
                    if (costs[i][j] < 0) {
                        throw new UnsupportedOperationException();
                    }
                }
            }
        }
        lagrangianRelaxation();
    }

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
        return ESat.TRUE;
    }

    public double getMinArcVal() {
        return -1;
    }

    public TIntArrayList getMandatoryArcsList() {
        return mandatoryArcsList;
    }

    public boolean isMandatory(int i, int j) {
        return gV.getMandatoryNeighborsOf(i).contains(j);
    }

    public void waitFirstSolution(boolean b) {
        waitFirstSol = b;
    }

    public boolean contains(int i, int j) {
        return mst == null || mst.containsEdge(i, j);
    }

    public UndirectedGraph getSupport() {
        return mst;
    }

    public double getReplacementCost(int from, int to) {
        return HKfilter.getRepCost(from, to);
    }

    public double getMarginalCost(int from, int to) {
        return HKfilter.getRepCost(from, to);
    }
}
