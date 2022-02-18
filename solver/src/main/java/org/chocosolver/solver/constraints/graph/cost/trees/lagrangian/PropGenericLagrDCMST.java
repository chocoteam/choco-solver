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

import gnu.trove.list.array.TIntArrayList;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.graph.cost.GraphLagrangianRelaxation;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Lagrangian relaxation of the DCMST problem
 */
public class PropGenericLagrDCMST extends Propagator<Variable> implements GraphLagrangianRelaxation {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final UndirectedGraphVar gV;
    private UndirectedGraph g;
    private final IntVar obj;
    private final int n;
    private final int[][] originalCosts;
    private final double[][] costs;
    private UndirectedGraph mst;
    private final TIntArrayList mandatoryArcsList;
    private final AbstractTreeFinder HKfilter, HK;
    private boolean waitFirstSol;
    private int nbSprints;
    private final IntVar[] D;
    private final int[] Dmax;
    private final int[] Dmin;
    private final double[] lambdaMin, lambdaMax;
    private double C;
    private boolean firstPropag = true;
    private long nbSols = 0;
    private int objUB = -1;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator performing the Lagrangian relaxation of the Degree Constrained Minimum Spanning Tree Problem
     */
    public PropGenericLagrDCMST(UndirectedGraphVar graph, IntVar cost, IntVar[] degrees, int[][] costMatrix, boolean waitFirstSol) {
        super(new Variable[]{graph, cost}, PropagatorPriority.CUBIC, false);
        gV = graph;
        n = gV.getNbMaxNodes();
        obj = cost;
        originalCosts = costMatrix;
        costs = new double[n][n];
        lambdaMin = new double[n];
        lambdaMax = new double[n];
        mandatoryArcsList = new TIntArrayList();
        nbSprints = 30;
        this.D = degrees;
        this.Dmin = new int[n];
        this.Dmax = new int[n];
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
        double hkb = HKfilter.getBound() - C;
        mst = HKfilter.getMST();
        if (hkb - Math.floor(hkb) < 0.001) {
            hkb = Math.floor(hkb);
        }
        obj.updateLowerBound((int) Math.ceil(hkb), this);
        HKfilter.performPruning((double) (obj.getUB()) + C + 0.001);
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
            hkb = HKfilter.getBound() - C;
            if (hkb > besthkb) {
                besthkb = hkb;
            }
            mst = HKfilter.getMST();
            if (hkb - Math.floor(hkb) < 0.00001) {
                hkb = Math.floor(hkb);
            }
            obj.updateLowerBound((int) Math.ceil(hkb), this);
            HKfilter.performPruning((double) (obj.getUB()) + C + 0.001);
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
                double hkb = HK.getBound() - C;
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
            if (deg > Dmax[i] || lambdaMax[i] != 0) {
                nb2viol += (Dmax[i] - deg) * (Dmax[i] - deg);
            }
            if (deg < Dmin[i] || lambdaMin[i] != 0) {
                nb2viol += (Dmin[i] - deg) * (Dmin[i] - deg);
            }
        }
        double K;
        if (nb2viol == 0) {
            return true;
        } else {
            K = alpha * (target - hkb) / nb2viol;
        }
        if (K < 0.0001) {
            return true;
        }
        double maxPen = 2 * obj.getUB();
        for (int i = 0; i < n; i++) {
            deg = mst.getNeighborsOf(i).size();
            lambdaMin[i] += (deg - Dmin[i]) * K;
            lambdaMax[i] += (deg - Dmax[i]) * K;
            if (lambdaMin[i] > 0) {
                lambdaMin[i] = 0;
            }
            lambdaMin[i] = 0;
            if (lambdaMax[i] < 0) {
                lambdaMax[i] = 0;
            }
            if (gV.getPotentialNeighborsOf(i).size() <= Dmax[i]) {
                lambdaMax[i] = 0;
            }
            if (gV.getMandatoryNeighborsOf(i).size() >= Dmin[i] || Dmin[i] <= 1) {
                lambdaMin[i] = 0;
            }
            if (lambdaMin[i] < -maxPen) {
                lambdaMin[i] = -maxPen;
            }
            if (lambdaMax[i] > maxPen) {
                lambdaMax[i] = maxPen;
            }
            assert !(lambdaMax[i] > Double.MAX_VALUE / (n - 1) || lambdaMax[i] < 0);
            assert !(lambdaMin[i] < -Double.MAX_VALUE / (n - 1) || lambdaMin[i] > 0);
        }
        updateCosts();
        return false;
    }

    private void updateCosts() {
        C = 0;
        for (int i = 0; i < n; i++) {
            C += Dmax[i] * lambdaMax[i];
            C += Dmin[i] * lambdaMin[i];
            ISet nei = g.getNeighborsOf(i);
            for (int j : nei) {
                if (i < j) {
                    costs[j][i] = costs[i][j] = originalCosts[i][j] + lambdaMin[i] + lambdaMin[j] + lambdaMax[i] + lambdaMax[j];
                    assert costs[j][i] >= 0;
                }
            }
        }
        assert C > -Double.MAX_VALUE / (n - 1) && C < Double.MAX_VALUE / (n - 1);
    }

    //***********************************************************************************
    // INFERENCE
    //***********************************************************************************

    @Override
    public void remove(int from, int to) throws ContradictionException {
        gV.removeEdge(from, to, this);
        if (firstPropag) {
            g.removeEdge(from, to);
        }
    }

    @Override
    public void enforce(int from, int to) throws ContradictionException {
        gV.enforceEdge(from, to, this);
    }

    @Override
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
        for (int i = 0; i < n; i++) {
            Dmin[i] = D[i].getLB();
            Dmax[i] = D[i].getUB();
        }
        for (int i = 0; i < n; i++) {
            ISet nei = gV.getMandatoryNeighborsOf(i);
            for (int j : nei) {
                if (i < j) {
                    mandatoryArcsList.add(i * n + j);
                }
            }
        }
        updateCosts();
        lagrangianRelaxation();
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }

    @Override
    public double getMinArcVal() {
        return Integer.MIN_VALUE / 10;
    }

    @Override
    public TIntArrayList getMandatoryArcsList() {
        return mandatoryArcsList;
    }

    @Override
    public boolean isMandatory(int i, int j) {
        return gV.getMandatoryNeighborsOf(i).contains(j);
    }

    @Override
    public void waitFirstSolution(boolean b) {
        waitFirstSol = b;
    }

    @Override
    public boolean contains(int i, int j) {
        return mst == null || mst.containsEdge(i, j);
    }

    @Override
    public UndirectedGraph getSupport() {
        return mst;
    }

    @Override
    public double getReplacementCost(int from, int to) {
        return HKfilter.getRepCost(from, to);
    }

    @Override
    public double getMarginalCost(int from, int to) {
        return HKfilter.getRepCost(from, to);
    }
}
