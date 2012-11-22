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

package solver.constraints.propagators.gary.tsp.directed.lagrangianRelaxation;

import choco.kernel.ESat;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.GraphLagrangianRelaxation;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.DirectedGraph;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.setDataStructures.ISet;

public class PropLagr_MST_BSTdual extends Propagator implements GraphLagrangianRelaxation {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected DirectedGraphVar g;
    protected IntVar obj;
    protected int source, sink;
    protected int n;
    protected int[][] originalCosts;
    protected double[][] costs;
    protected double[] inPenalities, outPenalities;
    protected double totalPenalities;
    protected DirectedGraph mst;
    protected TIntArrayList mandatoryArcsList;
    protected double step;
    protected AbstractMSTFinder HKfilter, HK;
    public long nbRem;
    protected boolean waitFirstSol;
    protected int nbSprints;
    private boolean activeBST;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * MST based HK
     */
    protected PropLagr_MST_BSTdual(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
        super(new Variable[]{graph, cost}, solver, constraint, PropagatorPriority.CUBIC);
        g = graph;
        n = g.getEnvelopGraph().getNbNodes();
        obj = cost;
        source = from;
        sink = to;
        originalCosts = costMatrix;
        costs = new double[n][n];
        totalPenalities = 0;
        inPenalities = new double[n];
        outPenalities = new double[n];
        mandatoryArcsList = new TIntArrayList();
        nbRem = 0;
        nbSprints = 30;
        OFF_BST = obj.getUB();
        System.out.println(OFF_BST);
    }

    /**
     * MST based HK
     */
    public static PropLagr_MST_BSTdual mstBasedRelaxation(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
        PropLagr_MST_BSTdual phk = new PropLagr_MST_BSTdual(graph, from, to, cost, costMatrix, constraint, solver);
        phk.HKfilter = new KruskalMST_GAC(phk.n, phk);
        phk.HK = new PrimMSTFinder(phk.n, phk);
        return phk;
    }

    protected IStateInt[] sccOf;
    protected IStateInt nr;

    /**
     * BST based HK
     */
    public static PropLagr_MST_BSTdual bstBasedRelaxation(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver, IStateInt nR, IStateInt[] sccOf, ISet[] outArcs) {
        PropLagr_MST_BSTdual phk = mstBasedRelaxation(graph, from, to, cost, costMatrix, constraint, solver);
        phk.sccOf = sccOf;
        phk.nr = nR;
        return phk;
    }

    //***********************************************************************************
    // HK Algorithm(s)
    //***********************************************************************************

    double OFF_BST = 120000;

    public void initAndRun() throws ContradictionException {
        double t = 0;
        for (int i = 0; i < n; i++) {
            t += inPenalities[i] + outPenalities[i];
        }
        this.totalPenalities = t;
        if (waitFirstSol && solver.getMeasures().getSolutionCount() == 0) {
            return;//the UB does not allow to prune
        }
        resetMA();
        activeBST = false;
        updateCostMatrix();
        lagrangianRelaxation();
        activeBST = true;
        t = 0;
        for (int i = 0; i < n; i++) {
            t += inPenalities[i] + outPenalities[i];
        }
        this.totalPenalities = t;
        updateCostMatrix();
        lagrangianRelaxation();
    }

    protected void lagrangianRelaxation() throws ContradictionException {
        double hkb;
        double alpha = 2;
        double beta = 0.5;
        double bestHKB;
        bestHKB = 0;
        HKfilter.computeMST(costs, g.getEnvelopGraph());
        hkb = HKfilter.getBound() - totalPenalities;
        bestHKB = hkb;
        mst = HKfilter.getMST();
        if (hkb - Math.floor(hkb) < 0.001) {
            hkb = Math.floor(hkb);
        }
        obj.updateLowerBound((int) Math.ceil(hkb), aCause);
        HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
        double totalSave;
        do {
            totalSave = bestHKB;
            double saveBest;
            do {
                saveBest = bestHKB;
                for (int i = nbSprints; i > 0; i--) {
                    HK.computeMST(costs, g.getEnvelopGraph());
                    hkb = HK.getBound() - totalPenalities;
                    if (hkb > bestHKB + 1) {
                        bestHKB = hkb;
                    }
                    mst = HK.getMST();
                    if (hkb - Math.floor(hkb) < 0.001) {
                        hkb = Math.floor(hkb);
                    }
                    obj.updateLowerBound((int) Math.ceil(hkb), aCause);
                    //	DO NOT FILTER HERE TO FASTEN CONVERGENCE (not always true)
//				HK.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
                    updateStep(hkb, alpha);
                    HKPenalities();
                    updateCostMatrix();
                }
                HKfilter.computeMST(costs, g.getEnvelopGraph());
                hkb = HKfilter.getBound() - totalPenalities;
                if (hkb > bestHKB + 1) {
                    bestHKB = hkb;
                }
                mst = HKfilter.getMST();
                if (hkb - Math.floor(hkb) < 0.001) {
                    hkb = Math.floor(hkb);
                }
                obj.updateLowerBound((int) Math.ceil(hkb), aCause);
//			if(tourFound()){// TODO attention si contraintes autres que TSP ca devient faux
//				forceTourInstantiation();
//				return;
//			}
                HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
                updateStep(hkb, alpha);
                HKPenalities();
                updateCostMatrix();
            } while (bestHKB > 1 + saveBest);
            alpha *= beta;
        } while ((bestHKB > totalSave + 0.1) && activeBST);
    }

    //***********************************************************************************
    // DETAILS
    //***********************************************************************************

    protected void resetMA() {
        mandatoryArcsList.clear();
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = g.getKernelGraph().getSuccessorsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                mandatoryArcsList.add(i * n + (j));
            }
        }
    }

    protected void updateStep(double hkb, double alpha) throws ContradictionException {
        double nb2viol = 0;
        double target = obj.getUB();
        if (target - hkb < 0) {
            if (hkb > target + 0.1) {
                throw new UnsupportedOperationException();
            }
            target = hkb + 0.1;
        }
        int inDeg, outDeg;
        for (int i = 0; i < n; i++) {
            inDeg = mst.getPredecessorsOf(i).getSize();
            outDeg = mst.getSuccessorsOf(i).getSize();
            if (i == source) {
                nb2viol += (1 - outDeg) * (1 - outDeg);
            } else if (i == sink) {
                nb2viol += (1 - inDeg) * (1 - inDeg);
            } else {
                nb2viol += (1 - inDeg) * (1 - inDeg) + (1 - outDeg) * (1 - outDeg);
            }
        }
        if (nb2viol == 0) {
            step = 0;
            return;
        }
        step = alpha * (target - hkb) / nb2viol;
        if (step < 0.000001) {
            step = 0;
        }
        if (step > 100) {
            step = 100;
        }
    }

    protected void HKPenalities() {
        if (step == 0) {
            return;
        }
        double totalPenalities = 0;
        int inDeg, outDeg;
//		double max = 10000;
        for (int i = 0; i < n; i++) {
            inDeg = mst.getPredecessorsOf(i).getSize();
            outDeg = mst.getSuccessorsOf(i).getSize();
            inPenalities[i] += (inDeg - 1) * step;
//			inPenalities[i] = Math.min(inPenalities[i], max);
//			inPenalities[i] = Math.max(inPenalities[i],-max);
            outPenalities[i] += (outDeg - 1) * step;
//			outPenalities[i] = Math.min(outPenalities[i], max);
//			outPenalities[i] = Math.max(outPenalities[i],-max);
            totalPenalities += inPenalities[i] + outPenalities[i];
        }
        totalPenalities += 2 * step;
        inPenalities[source] = outPenalities[sink] = 0;
        this.totalPenalities = totalPenalities;
        double t = 0;
        for (int i = 0; i < n; i++) {
            t += inPenalities[i] + outPenalities[i];
        }
        if (t != totalPenalities) {
            this.totalPenalities = t;
        }
    }

    protected void updateCostMatrix() {
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = g.getEnvelopGraph().getSuccessorsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                costs[i][j] = originalCosts[i][j] + outPenalities[i] + inPenalities[j];
                if (activeBST && sccOf != null)
                    if (sccOf[i].get() != sccOf[j].get()) {
                        costs[i][j] += OFF_BST;
                        costs[j][i] = costs[i][j];
                    }
            }
        }
        if (activeBST && sccOf != null) {
            totalPenalities += OFF_BST * (nr.get() - 1);
        }
    }

    protected boolean tourFound() {
        if (mst.getSuccessorsOf(source).getSize() * mst.getPredecessorsOf(sink).getSize() != 1) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (i != sink && i != source && mst.getSuccessorsOf(i).getSize() * mst.getPredecessorsOf(i).getSize() != 1) {
                return false;
            }
        }
        return true;
    }

    protected void forceTourInstantiation() throws ContradictionException {
        int next;
        for (int i = 0; i < n; i++) {
            next = mst.getSuccessorsOf(i).getFirstElement();
            if (next != -1) {
                g.enforceArc(i, next, aCause);
            }
        }
    }

    //***********************************************************************************
    // INFERENCE
    //***********************************************************************************

    public void remove(int from, int to) throws ContradictionException {
        g.removeArc(from, to, aCause);
        nbRem++;
    }

    public void enforce(int from, int to) throws ContradictionException {
        g.enforceArc(from, to, aCause);
    }

    public void contradiction() throws ContradictionException {
        contradiction(g, "mst failure");
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
    	return ESat.TRUE; //not implemented
    }

    public double getMinArcVal() {
        return -(((double) obj.getUB()) + totalPenalities);
    }

    @Override
    public boolean contains(int i, int j) {
        if (mst == null) {
            throw new UnsupportedOperationException("no relaxation computed yet");
        }
        return mst.arcExists(i, j);
    }

    public boolean isMandatory(int i, int j) {
        return g.getKernelGraph().arcExists(i, j);
    }

    public TIntArrayList getMandatoryArcsList() {
        return mandatoryArcsList;
    }

    public void waitFirstSolution(boolean b) {
        waitFirstSol = b;
    }

    public DirectedGraph getSupport() {
        return HKfilter.getMST();
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
