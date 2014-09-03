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

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.ESat;
import util.objects.graphs.UndirectedGraph;
import util.objects.setDataStructures.SetType;
import util.tools.ArrayUtils;

/**
 * TSP Lagrangian relaxation
 * Inspired from the work of Held & Karp
 * and Benchimol et. al. (Constraints 2012)
 *
 * @author Jean-Guillaume Fages
 */
public class PropLagr_OneTree_IntVar extends PropLagr_OneTree {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected IntVar[] succ;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropLagr_OneTree_IntVar(IntVar[] graph, IntVar cost, int[][] costMatrix, boolean waitFirstSol) {
        super(ArrayUtils.append(graph, new IntVar[]{cost}), costMatrix);
        this.succ = graph;
        g = new UndirectedGraph(n, SetType.SWAP_ARRAY, true);
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
        succ[from].removeValue(to, aCause);
        succ[to].removeValue(from, aCause);
    }

    @Override
    public void enforce(int from, int to) throws ContradictionException {
        if (!succ[from].contains(to)) {
            succ[to].instantiateTo(from, aCause);
        }
        if (!succ[to].contains(from)) {
            succ[from].instantiateTo(to, aCause);
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

    public static boolean checkSymmetry(int[][] costMatrix) {
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

    @Override
    public void duplicate(Solver solver, THashMap identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length - 1;
            IntVar[] aVars = new IntVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (IntVar) identitymap.get(this.vars[i]);
            }
            this.vars[size].duplicate(solver, identitymap);
            IntVar aVar = (IntVar) identitymap.get(this.vars[size]);
            identitymap.put(this, new PropLagr_OneTree_IntVar(aVars, aVar, this.originalCosts, waitFirstSol));
        }
    }
}