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

package samples.sandbox.lns.parallel;

import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.gary.GraphConstraintFactory;
import solver.objective.ObjectiveStrategy;
import solver.objective.OptimizationPolicy;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.search.strategy.strategy.graph.GraphStrategies;
import solver.thread.AbstractParallelMaster;
import solver.thread.AbstractParallelSlave;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.UndirectedGraphVar;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetType;

public class TSPslave extends AbstractParallelSlave {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    //model
    private int[] fragToReal, outputFragment;
    private int[][] distMatrix;
    private int n, ub, outputCost;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public TSPslave(AbstractParallelMaster master, int id, int size) {
        super(master, id);
        n = size + 1;
        distMatrix = new int[n][n];
        fragToReal = new int[n - 1];
        outputFragment = new int[n - 1];
    }

    //***********************************************************************************
    // INITIALIZATION
    //***********************************************************************************

    public void set(int[] frag, int[][] bigMatrix) {
        fragToReal = frag;
        ub = 0;
        for (int i = 0; i < n - 1; i++) {
            outputFragment[i] = fragToReal[i];
            for (int j = i + 1; j < n - 1; j++) {
                distMatrix[j][i] = distMatrix[i][j] = bigMatrix[fragToReal[i]][fragToReal[j]];
            }
            if (i < n - 2) {
                ub += bigMatrix[frag[i]][frag[i + 1]];
            }
        }
        outputCost = ub;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void work() {
        final Solver solver = new Solver();
        // variables
        final IntVar totalCost = VariableFactory.bounded("obj", 0, ub, solver);
        final UndirectedGraphVar undi = new UndirectedGraphVar("G", solver, n, SetType.ENVELOPE_BEST, SetType.LINKED_LIST, true);
        for (int i = 0; i < n; i++) {
            undi.getKernelGraph().activateNode(i);
            for (int j = i + 1; j < n - 1; j++) {
                undi.getEnvelopGraph().addEdge(i, j);
            }
        }
        undi.getEnvelopGraph().addEdge(0, n - 1);
        undi.getEnvelopGraph().addEdge(n - 2, n - 1);
        undi.getKernelGraph().addEdge(0, n - 1);
        undi.getKernelGraph().addEdge(n - 2, n - 1);
        // constraints
        solver.post(GraphConstraintFactory.tsp(undi, totalCost, distMatrix, 1));
        // config
        GraphStrategies strategy = new GraphStrategies(undi, distMatrix, null);
        strategy.configure(GraphStrategies.MAX_COST, true);
        solver.set(new StrategiesSequencer(new ObjectiveStrategy(totalCost, OptimizationPolicy.BOTTOM_UP), strategy));
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
        //output
        if (solver.getMeasures().getSolutionCount() == 0) {
            throw new UnsupportedOperationException();
        }
        if (!undi.instantiated()) {
            throw new UnsupportedOperationException();
        }
        outputCost = totalCost.getValue();
        if (outputCost > ub) {
            throw new UnsupportedOperationException(outputCost + ">" + ub);
        }
        int x = 0;
        ISet nei = undi.getEnvelopGraph().getNeighborsOf(x);
        int y = nei.getFirstElement();
        if (y == n - 1) {
            y = nei.getNextElement();
        }
        int tmp;
        for (int i = 0; i < n - 1; i++) {
            outputFragment[i] = fragToReal[x];
            tmp = x;
            x = y;
            nei = undi.getEnvelopGraph().getNeighborsOf(x);
            y = nei.getFirstElement();
            if (y == tmp) {
                y = nei.getNextElement();
            }
        }
        if (outputFragment[0] != fragToReal[0] || outputFragment[n - 2] != fragToReal[n - 2]) {
            throw new UnsupportedOperationException();
        }
    }

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    public int[] getInputFragment() {
        return fragToReal;
    }

    public int[] getOutputFragment() {
        return outputFragment;
    }

    public int getOutputCost() {
        return outputCost;
    }
}
