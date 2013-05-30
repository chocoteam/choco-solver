package solver.constraints.propagators.gary.trees.lagrangianRelaxation;
/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import gnu.trove.list.array.TIntArrayList;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.GraphLagrangianRelaxation;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.UndirectedGraphVar;
import util.ESat;
import util.objects.graphs.UndirectedGraph;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetType;

/**
 * Lagrangian relaxation of the DCMST problem
 */
public class PropLagr_DCMST extends Propagator implements GraphLagrangianRelaxation {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected UndirectedGraphVar gV;
    protected UndirectedGraph g;
    protected IntVar obj;
    protected int n;
    protected int[][] originalCosts;
    protected double[][] costs;
    protected double[] penalities;
    protected double totalPenalities;
    protected UndirectedGraph mst;
    protected TIntArrayList mandatoryArcsList;
    protected AbstractTreeFinder HKfilter, HK;
    protected long nbRem;
    protected boolean waitFirstSol;
    protected int nbSprints;
    protected int[] maxDegree;
    protected double step;
    protected boolean firstPropag = true;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator performing the Lagrangian relaxation of the Degree Constrained Minimum Spanning Tree Problem
     */
    public PropLagr_DCMST(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, boolean waitFirstSol) {
        super(new Variable[]{graph, cost}, PropagatorPriority.CUBIC,false);
        gV = graph;
        n = gV.getEnvelopGraph().getNbNodes();
        obj = cost;
        originalCosts = costMatrix;
        costs = new double[n][n];
        penalities = new double[n];
        totalPenalities = 0;
        mandatoryArcsList = new TIntArrayList();
        nbRem = 0;
        nbSprints = 30;
        this.maxDegree = maxDegree;
        HK = new PrimMSTFinder(n, this);
        HKfilter = new KruskalMST_GAC(n, this);
        this.waitFirstSol = waitFirstSol;
        g = new UndirectedGraph(n, SetType.BITSET, true);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                g.addEdge(i, j);
            }
        }
    }

    //***********************************************************************************
    // HK Algorithm(s)
    //***********************************************************************************

    public void initAndRun() throws ContradictionException {
        if (waitFirstSol && solver.getMeasures().getSolutionCount() == 0) {
            return;//the UB does not allow to prune
        }
        // initialisation
        mandatoryArcsList.clear();
        ISet nei;
        totalPenalities = 0;
        for (int i = 0; i < n; i++) {
            totalPenalities += penalities[i] * maxDegree[i];
            nei = gV.getKernelGraph().getNeighborsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (i < j) {
                    mandatoryArcsList.add(i * n + j);
                }
            }
            nei = g.getNeighborsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
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

    private long nbSols = 0;
    private int objUB = -1;

    protected void lagrangianRelaxation() throws ContradictionException {
        int lb = obj.getLB();
        nbSprints = 30;
        if (nbSols != solver.getMeasures().getSolutionCount()
                || obj.getUB() < objUB
                || (firstPropag && !waitFirstSol)) {
            nbSols = solver.getMeasures().getSolutionCount();
            objUB = obj.getUB();
            convergeAndFilter();
            firstPropag = false;
            g = gV.getEnvelopGraph();
        } else {
            fastRun(2);
        }
        if (lb < obj.getLB()) {
            lagrangianRelaxation();
        }
    }

    protected void fastRun(double coef) throws ContradictionException {
        convergeFast(coef);
        HKfilter.computeMST(costs, g);
        double hkb = HKfilter.getBound() - totalPenalities;
        mst = HKfilter.getMST();
        if (hkb - Math.floor(hkb) < 0.001) {
            hkb = Math.floor(hkb);
        }
        obj.updateLowerBound((int) Math.ceil(hkb), aCause);
        HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
    }

    protected void convergeAndFilter() throws ContradictionException {
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
            obj.updateLowerBound((int) Math.ceil(hkb), aCause);
            HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
            alpha *= beta;
        }
    }

    protected void convergeFast(double alpha) throws ContradictionException {
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
                obj.updateLowerBound((int) Math.ceil(hkb), aCause);
                if (updateStep(hkb, alpha)) return;
            }
        }
    }

    protected boolean updateStep(double hkb, double alpha) throws ContradictionException {
        double nb2viol = 0;
        double target = obj.getUB();
        assert (target - hkb >= 0);
        if (target - hkb < 0.001) {
            target = hkb + 0.001;
        }
        int deg;
        for (int i = 0; i < n; i++) {
            deg = mst.getNeighborsOf(i).getSize();
            if (deg > maxDegree[i] || penalities[i] > 0) {
                nb2viol += (maxDegree[i] - deg) * (maxDegree[i] - deg);
            }
        }
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
            deg = mst.getNeighborsOf(i).getSize();
            penalities[i] += (deg - maxDegree[i]) * step;
            if (penalities[i] < 0 || g.getNeighborsOf(i).getSize() <= maxDegree[i]) {
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
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
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
        gV.removeArc(from, to, aCause);
        if (firstPropag) {
            g.removeEdge(from, to);
        }
        nbRem++;
    }

    public void enforce(int from, int to) throws ContradictionException {
        gV.enforceArc(from, to, aCause);
    }

    public void contradiction() throws ContradictionException {
        contradiction(gV, "mst failure");
    }

    //***********************************************************************************
    // PROP METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        initAndRun();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        initAndRun();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.DECUPP.mask + EventType.INCLOW.mask + EventType.INSTANTIATE.mask;
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
        return gV.getKernelGraph().edgeExists(i, j);
    }

    public void waitFirstSolution(boolean b) {
        waitFirstSol = b;
    }

    public boolean contains(int i, int j) {
        if (mst == null) {
            return true;
        }
        return mst.edgeExists(i, j);
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