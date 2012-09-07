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

package solver.constraints.propagators.gary.trees.lagrangianRelaxation;

import choco.kernel.ESat;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.HeldKarp;
import solver.constraints.propagators.gary.trees.AbstractTreeFinder;
import solver.constraints.propagators.gary.trees.PrimMSTFinder;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * Lagrangian relaxation of the DCMST problem without subgradient optimization
 */
public class PropBoundMST extends Propagator implements HeldKarp {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected UndirectedGraphVar g;
    protected IntVar obj;
    protected int n;
    protected int[][] originalCosts;
    protected double[][] costs;
    protected int[] penalities;
    protected int totalPenalities;
    protected UndirectedGraph mst;
    protected TIntArrayList mandatoryArcsList;
    protected AbstractTreeFinder HK;
    protected boolean waitFirstSol;
    protected int[] maxDegree;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * MST based HK
     */
    protected PropBoundMST(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
        super(new Variable[]{graph, cost}, solver, constraint, PropagatorPriority.CUBIC);
        g = graph;
        n = g.getEnvelopGraph().getNbNodes();
        obj = cost;
        originalCosts = costMatrix;
        costs = new double[n][n];
        penalities = new int[n];
        mandatoryArcsList = new TIntArrayList();
        this.maxDegree = maxDegree;
    }

    /**
     * ONE TREE based HK
     */
    public static PropBoundMST mstBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[] maxDegree, int[][] costMatrix, Constraint constraint, Solver solver) {
        PropBoundMST phk = new PropBoundMST(graph, cost, maxDegree, costMatrix, constraint, solver);
        phk.HK = new PrimMSTFinder(phk.n, phk);
        return phk;
    }

    //***********************************************************************************
    // HK Algorithm(s)
    //***********************************************************************************

    public void HK_algorithm() throws ContradictionException {
        if (waitFirstSol && solver.getMeasures().getSolutionCount() == 0) {
            return;//the UB does not allow to prune
        }
        // initialisation
        mandatoryArcsList.clear();
        INeighbors nei;
        for (int i = 0; i < n; i++) {
            nei = g.getKernelGraph().getSuccessorsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (i < j) {
                    mandatoryArcsList.add(i * n + j);
                }
            }
            nei = g.getEnvelopGraph().getSuccessorsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (i < j) {
                    costs[j][i] = costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
                    if (costs[i][j] < 0) {
                        throw new UnsupportedOperationException();
                    }
                }
            }
        }
        HK_Pascals();
    }

    protected void HK_Pascals() throws ContradictionException {
        double hkb = 0;
        double oldhkb = -1;
        while (oldhkb + 0.01 < hkb) {
            oldhkb = hkb;
            HK.computeMST(costs, g.getEnvelopGraph());
            mst = HK.getMST();
            hkb = HK.getBound() - totalPenalities;
            if (hkb - Math.floor(hkb) < 0.001) {
                hkb = Math.floor(hkb);
            }
            obj.updateLowerBound((int) Math.ceil(hkb), aCause);
            if (updateHKPenalities()) break;
        }
    }

    protected boolean updateHKPenalities() throws ContradictionException {
        int deg, envDeg;
        int sumPenalities = 0;
        int max = 2 * obj.getUB();
        boolean found = true;
        for (int i = 0; i < n; i++) {
            deg = mst.getNeighborsOf(i).neighborhoodSize();
            penalities[i] += (deg - maxDegree[i]);
            if (deg > maxDegree[i]) {
                found = false;
            }
            envDeg = g.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize();
            if (penalities[i] < 0
                    || envDeg <= maxDegree[i]) {
                penalities[i] = 0;
            }
            if (penalities[i] > max) {
                penalities[i] = max;
            }
            sumPenalities += penalities[i] * maxDegree[i];
        }
        this.totalPenalities = sumPenalities;
        INeighbors nei;
        for (int i = 0; i < n; i++) {
            nei = g.getEnvelopGraph().getSuccessorsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (i < j) {
                    costs[j][i] = costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
                }
            }
        }
        return found;
    }

    //***********************************************************************************
    // INFERENCE
    //***********************************************************************************

    public void remove(int from, int to) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public void enforce(int from, int to) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public void contradiction() throws ContradictionException {
        contradiction(g, "mst failure");
    }

    //***********************************************************************************
    // PROP METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        HK_algorithm();
        System.out.println("current lower bound : " + obj.getLB());

    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.VOID.mask;
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }

    public double getMinArcVal() {
        return -1;
    }

    public TIntArrayList getMandatoryArcsList() {
        return mandatoryArcsList;
    }

    public boolean isMandatory(int i, int j) {
        return g.getKernelGraph().edgeExists(i, j);
    }

    public void waitFirstSolution(boolean b) {
        throw new UnsupportedOperationException();
    }

    public boolean contains(int i, int j) {
        if (mst == null) {
            return true;
        }
        return mst.edgeExists(i, j);
    }

    public UndirectedGraph getMST() {
        return mst;
    }

    public double getReplacementCost(int from, int to) {
        throw new UnsupportedOperationException();
    }

    public double getMarginalCost(int from, int to) {
        throw new UnsupportedOperationException();
    }
}