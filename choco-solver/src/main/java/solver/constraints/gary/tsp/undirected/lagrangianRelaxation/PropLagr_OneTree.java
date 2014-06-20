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

package solver.constraints.gary.tsp.undirected.lagrangianRelaxation;

import gnu.trove.list.array.TIntArrayList;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.constraints.gary.GraphLagrangianRelaxation;
import solver.constraints.gary.trees.lagrangianRelaxation.AbstractTreeFinder;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.UndirectedGraphVar;
import util.ESat;
import util.objects.graphs.UndirectedGraph;
import util.objects.setDataStructures.ISet;

/**
 * TSP Lagrangian relaxation
 * Inspired from the work of Held & Karp
 * and Benchimol et. al. (Constraints 2012)
 *
 * @author Jean-Guillaume Fages
 */
public class PropLagr_OneTree extends Propagator implements GraphLagrangianRelaxation {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

	protected UndirectedGraph g;
	protected UndirectedGraphVar gV;
    protected IntVar obj;
    protected int n;
    protected int[][] originalCosts;
    protected double[][] costs;
    protected double[] penalities;
    protected double totalPenalities;
    protected UndirectedGraph mst;
    protected TIntArrayList mandatoryArcsList;
    protected double step;
    protected AbstractTreeFinder HKfilter, HK;
    protected boolean waitFirstSol;
    protected int nbSprints;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

	protected PropLagr_OneTree(Variable[] vars, int[][] costMatrix) {
		super(vars, PropagatorPriority.CUBIC, false);
		originalCosts = costMatrix;
		n = originalCosts.length;
		costs = new double[n][n];
		totalPenalities = 0;
		penalities = new double[n];
		mandatoryArcsList = new TIntArrayList();
		nbSprints = 30;
		HK = new PrimOneTreeFinder(n, this);
		HKfilter = new KruskalOneTree_GAC(n, this);
	}

    public PropLagr_OneTree(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix) {
        this(new Variable[]{graph, cost}, costMatrix);
        g = graph.getEnvelopGraph();
        obj = cost;
    }

    //***********************************************************************************
    // HK Algorithm(s)
    //***********************************************************************************

	public void propagate(int evtmask) throws ContradictionException {
        if (waitFirstSol && solver.getMeasures().getSolutionCount() == 0) {
            return;//the UB does not allow to prune
        }
        // initialisation
        rebuild();
        setCosts();
        int lb;
        do {
            lb = obj.getLB();
            lagrangianRelaxation();
        } while (lb < obj.getLB());
    }

    protected void lagrangianRelaxation() throws ContradictionException {
        double hkb;
        double alpha = 2;
        double beta = 0.5;
        double bestHKB;
        bestHKB = 0;
        HKfilter.computeMST(costs, g);
        hkb = HKfilter.getBound() - totalPenalities;
        bestHKB = hkb;
        mst = HKfilter.getMST();
        if (hkb - Math.floor(hkb) < 0.001) {
            hkb = Math.floor(hkb);
        }
        obj.updateLowerBound((int) Math.ceil(hkb), aCause);
        HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
        for (int iter = 5; iter > 0; iter--) {
            for (int i = nbSprints; i > 0; i--) {
                HK.computeMST(costs, g);
                hkb = HK.getBound() - totalPenalities;
                if (hkb > bestHKB + 1) {
                    bestHKB = hkb;
                }
                mst = HK.getMST();
                if (hkb - Math.floor(hkb) < 0.001) {
                    hkb = Math.floor(hkb);
                }
                obj.updateLowerBound((int) Math.ceil(hkb), aCause);
                // HK.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
                //	DO NOT FILTER HERE TO SPEED UP CONVERGENCE (not always true)
                updateStep(hkb, alpha);
                HKPenalities();
                updateCostMatrix();
            }
            HKfilter.computeMST(costs, g);
            hkb = HKfilter.getBound() - totalPenalities;
            if (hkb > bestHKB + 1) {
                bestHKB = hkb;
            }
            mst = HKfilter.getMST();
            if (hkb - Math.floor(hkb) < 0.001) {
                hkb = Math.floor(hkb);
            }
            obj.updateLowerBound((int) Math.ceil(hkb), aCause);
            HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
            updateStep(hkb, alpha);
            HKPenalities();
            updateCostMatrix();
            alpha *= beta;
            beta /= 2;
        }
    }

    //***********************************************************************************
    // DETAILS
    //***********************************************************************************

    protected void rebuild() {
        mandatoryArcsList.clear();
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = gV.getKernelGraph().getNeighborsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (i < j) {
                    mandatoryArcsList.add(i * n + j);
                }
            }
        }
    }

    protected void setCosts() {
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = g.getNeighborsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (i < j) {
                    costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
                    costs[j][i] = costs[i][j];
                }
            }
        }
    }

    protected void updateStep(double hkb, double alpha) {
        double nb2viol = 0;
        double target = obj.getUB();
        if (target - hkb < 0) {
            target = hkb + 0.1;
        }
        int deg;
        for (int i = 0; i < n; i++) {
            deg = mst.getNeighborsOf(i).getSize();
            nb2viol += (2 - deg) * (2 - deg);
        }
        if (nb2viol == 0) {
            step = 0;
        } else {
            step = alpha * (target - hkb) / nb2viol;
        }
    }

    protected void HKPenalities() {
        if (step == 0) {
            return;
        }
        double sumPenalities = 0;
        int deg;
        for (int i = 0; i < n; i++) {
            deg = mst.getNeighborsOf(i).getSize();
            penalities[i] += (deg - 2) * step;
			assert !(penalities[i] > Double.MAX_VALUE / (n - 1) || penalities[i] < -Double.MAX_VALUE / (n - 1)) :
					"Extreme-value lagrangian multipliers. Numerical issue may happen";
            sumPenalities += penalities[i];
        }
        this.totalPenalities = 2 * sumPenalities;
    }

    protected void updateCostMatrix() {
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = g.getNeighborsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (i < j) {
                    costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
                    costs[j][i] = costs[i][j];
                }
            }
        }
    }

    //***********************************************************************************
    // INFERENCE
    //***********************************************************************************

    public void remove(int from, int to) throws ContradictionException {
        gV.removeArc(from, to, aCause);
    }

    public void enforce(int from, int to) throws ContradictionException {
        gV.enforceArc(from, to, aCause);
    }

    public void contradiction() throws ContradictionException {
        contradiction(obj, "mst failure");
    }

    //***********************************************************************************
    // PROP METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.DECUPP.mask + EventType.INCLOW.mask + EventType.INSTANTIATE.mask;
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;// it is just implied filtering
    }

    public double getMinArcVal() {
        return -(((double) obj.getUB()) + totalPenalities);
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