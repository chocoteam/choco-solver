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
package samples;

import choco.kernel.ResolutionPolicy;
import choco.kernel.common.util.tools.ArrayUtils;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.binary.Element;
import solver.constraints.nary.Count;
import solver.constraints.nary.Sum;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.Arrays;

/**
 * CSPLib prob034:<br/>
 * "In the Warehouse Location problem (WLP), a company considers opening warehouses
 * at some candidate locations in order to supply its existing stores. Each possible warehouse
 * has the same maintenance cost, and a capacity designating the maximum number of stores
 * that it can supply. Each store must be supplied by exactly one open warehouse.
 * The supply cost to a store depends on the warehouse. The objective is to determine which
 * warehouses to open, and which of these warehouses should supply the various stores, such
 * that the sum of the maintenance and supply costs is minimized."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/08/11
 */
public class WarehouseLocation extends AbstractProblem {

    int nWH = 5, nS = 10, cost = 30;
    int[] capacity = {1, 4, 2, 1, 3};
    int[][] c_supply = {
            {20, 24, 11, 25, 30},
            {28, 27, 82, 83, 74},
            {74, 97, 71, 96, 70},
            {2, 55, 73, 69, 61},
            {46, 96, 59, 83, 4},
            {42, 22, 29, 67, 59},
            {1, 5, 73, 59, 56},
            {10, 73, 13, 43, 96},
            {93, 35, 63, 85, 46},
            {47, 65, 55, 71, 95}
    };

    IntVar[] suppliers;
    BoolVar[] open;
    IntVar[] costPerStore;
    IntVar totCost;

    @Override
    public void buildModel() {
        solver = new Solver();
        suppliers = VariableFactory.enumeratedArray("sup", nS, 0, nWH - 1, solver);
        open = VariableFactory.boolArray("o", nWH, solver);
        costPerStore = VariableFactory.boundedArray("cPs", nS, 0, 9999, solver);
        totCost = VariableFactory.bounded("cost", 0, 99999, solver);

        // A warehouse is open, if it supplies to a store
        IntVar ONE = Views.fixed(1, solver);
        for (int s = 0; s < nS; s++) {
            solver.post(new Element(ONE, open, suppliers[s], 0, solver));
        }
        // Compute cost for each warehouse
        for (int s = 0; s < nS; s++) {
            solver.post(new Element(costPerStore[s], c_supply[s], suppliers[s], solver));
        }
        // Do not exceed capacity
        for (int w = 0; w < nWH; w++) {
            IntVar counter = Views.fixed(capacity[w], solver);
            solver.post(new Count(w, suppliers, Count.Relop.LEQ, counter, solver));
        }

        int[] coeffs = new int[nWH + nS];
        Arrays.fill(coeffs, 0, nWH, cost);
        Arrays.fill(coeffs, nWH, nWH + nS, 1);
        solver.post(Sum.eq(ArrayUtils.append(open, costPerStore), coeffs, totCost, 1, solver));
    }

    @Override
    public void configureSolver() {
        StrategiesSequencer strat = new StrategiesSequencer(solver.getEnvironment(),
                StrategyFactory.inputOrderMinVal(suppliers, solver.getEnvironment()),
                StrategyFactory.maxRegMinVal(costPerStore, solver.getEnvironment())
                );
        solver.set(strat);

        //TODO: find a propagation strat

    }

    @Override
    public void solve() {
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totCost);
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Warehouse location problem");
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < nWH; i++) {
            if (open[i].getValue() > 0) {
                st.append(String.format("\tw#%d:\n\t", i));
                for (int j = 0; j < nS; j++) {
                    if (suppliers[j].getValue() == i) {
                        st.append(String.format("%d (%d) ", j, costPerStore[j].getValue()));
                    }
                }
                st.append("\n");
            }
        }
        st.append("\tTotal cost: ").append(totCost.getValue());

        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new WarehouseLocation().execute(args);
    }

    /////////////////////////////////// DATA //////////////////////////////////////////////////


}
