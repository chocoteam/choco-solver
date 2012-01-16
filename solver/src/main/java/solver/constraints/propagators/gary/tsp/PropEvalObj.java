/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.gary.tsp;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;

import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * Compute the cost of the graph by summing arcs costs
 * BEWARE - Assume that the last node has no successor
 * - For minimization problem
 * */
public class PropEvalObj<V extends Variable> extends GraphPropagator<V> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    DirectedGraphVar g;
    int n;
    IntVar sum;
    int[][] distMatrix;
    IStateInt[] minCostSucc;
    IntProcedure arcEnforced, arcRemoved;
    IStateInt minSum;
    TIntArrayList toCompute;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that obj=SUM{costMatrix[i][j], (i,j) in arcs of graph}
     * BEWARE - Assume that the last node has no successor
     * - For minimization problem
     *
     * @param graph
     * @param obj
     * @param costMatrix
     * @param constraint
     * @param solver
     */
    public PropEvalObj(DirectedGraphVar graph, IntVar obj, int[][] costMatrix, Constraint<V, Propagator<V>> constraint, Solver solver) {
        super((V[]) new Variable[]{graph, obj}, solver, constraint, PropagatorPriority.LINEAR);
        g = graph;
        sum = obj;
        n = g.getEnvelopGraph().getNbNodes();
        distMatrix = costMatrix;
        arcEnforced = new EnfArc(this);
        arcRemoved = new RemArc(this);
        minSum = environment.makeInt(0);
        toCompute = new TIntArrayList();
        minCostSucc = new IStateInt[n];
        for (int i = 0; i < n; i++) {
            minCostSucc[i] = environment.makeInt(-1);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        INeighbors succ;
        for (int i = 0; i < n - 1; i++) {
            succ = g.getEnvelopGraph().getSuccessorsOf(i);
            int min = succ.getFirstElement();
            int minC = distMatrix[i][min];
            for (int s = min; s >= 0; s = succ.getNextElement()) {
                if (distMatrix[i][s] < minC) {
                    minC = distMatrix[i][s];
                    min = s;
                }
            }
            minSum.add(minC);
            minCostSucc[i].set(min);
        }
        sum.updateLowerBound(minSum.get(), this);
        // filter the graph
        INeighbors succs;
        int delta = minSum.get() - sum.getUB();
        int curMin;
        for (int i = 0; i < n - 1; i++) {
            succs = g.getEnvelopGraph().getSuccessorsOf(i);
            curMin = distMatrix[i][minCostSucc[i].get()];
            for (int j = succs.getFirstElement(); j >= 0; j = succs.getNextElement()) {
                if (delta > curMin - distMatrix[i][j]) {
                    g.removeArc(i, j, this);
                }
            }
        }
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        toCompute.clear();
        int oldMin = minSum.get();
        Variable variable = vars[idxVarInProp];
        if (variable.getType() == Variable.GRAPH) {
            if ((mask & EventType.ENFORCEARC.mask) != 0) {
                eventRecorder.getDeltaMonitor(g).forEach(arcEnforced, EventType.ENFORCEARC);
            }
            if ((mask & EventType.REMOVEARC.mask) != 0) {
                eventRecorder.getDeltaMonitor(g).forEach(arcRemoved, EventType.REMOVEARC);
            }
            for (int i = toCompute.size() - 1; i >= 0; i--) {
                findMin(toCompute.get(i));
            }
            sum.updateLowerBound(minSum.get(), this);
        }
        if ((minSum.get() > oldMin) || ((mask & EventType.DECUPP.mask) != 0)) {
            // filter the graph
            INeighbors succs;
            int delta = minSum.get() - sum.getUB();
            int curMin;
            for (int i = 0; i < n - 1; i++) {
                succs = g.getEnvelopGraph().getSuccessorsOf(i);
                curMin = distMatrix[i][minCostSucc[i].get()];
                for (int j = succs.getFirstElement(); j >= 0; j = succs.getNextElement()) {
                    if (delta > curMin - distMatrix[i][j]) {
                        g.removeArc(i, j, this);
                    }
                }
            }
        }
    }

    private void findMin(int i) throws ContradictionException {
        INeighbors succ = g.getEnvelopGraph().getSuccessorsOf(i);
        int min = succ.getFirstElement();
        if (min == -1) {
            System.out.println(g.getEnvelopGraph());
            System.out.println(i);
            System.exit(0);
        }
        int minC = distMatrix[i][min];
        for (int s = min; s >= 0; s = succ.getNextElement()) {
            if (distMatrix[i][s] < minC) {
                minC = distMatrix[i][s];
                min = s;
            }
        }
        minSum.add(minC - distMatrix[i][minCostSucc[i].get()]);
        minCostSucc[i].set(min);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.DECUPP.mask;
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    private class EnfArc implements IntProcedure {
        private GraphPropagator p;

        private EnfArc(GraphPropagator p) {
            this.p = p;
        }
        @Override
        public void execute(int i) throws ContradictionException {
            int from = i / n - 1;
            int to = i % n;
            if (to != minCostSucc[from].get()) {
                minSum.add(distMatrix[from][to] - distMatrix[from][minCostSucc[from].get()]);
                minCostSucc[from].set(to);
            }
        }
    }

    private class RemArc implements IntProcedure {
        private GraphPropagator p;

        private RemArc(GraphPropagator p) {
            this.p = p;
        }
        @Override
        public void execute(int i) throws ContradictionException {
            int from = i / n - 1;
            int to = i % n;
            if (to == minCostSucc[from].get()) {
                toCompute.add(from);
            }
        }
    }
}
