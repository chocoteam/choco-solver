/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.todo.problems.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.Option;

import static java.util.Arrays.fill;
import static org.chocosolver.solver.ResolutionPolicy.MINIMIZE;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;

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

    @Option(name = "-d", aliases = "--data", usage = "Warehouse location instance.", required = false)
    Data data = Data.med;

    int nWH, nS, cost;
    int[] capacity;
    int[][] c_supply;

    IntVar[] suppliers;
    BoolVar[] open;
    IntVar[] costPerStore;
    IntVar totCost;


    public void setUp() {
        int k = 0;
        nWH = data.data[k++];
        nS = data.data[k++];
        cost = data.data[k++];
        capacity = new int[nWH];
        for (int i = 0; i < nWH; i++) {
            capacity[i] = data.data[k++];
        }
        c_supply = new int[nS][nWH];
        for (int j = 0; j < nS; j++) {
            for (int i = 0; i < nWH; i++) {
                c_supply[j][i] = data.data[k++];
            }
        }
    }


    @Override
    public void buildModel() {
        model = new Model();
        setUp();
        suppliers = model.intVarArray("sup", nS, 0, nWH - 1, false);
        open = model.boolVarArray("o", nWH);
        costPerStore = model.intVarArray("cPs", nS, 0, 9999, true);
        totCost = model.intVar("cost", 0, 99999, true);

        // A warehouse is open, if it supplies to a store
        IntVar ONE = model.intVar(1);
        for (int s = 0; s < nS; s++) {
            model.element(ONE, open, suppliers[s], 0).post();
        }
        // Compute cost for each warehouse
        for (int s = 0; s < nS; s++) {
            model.element(costPerStore[s], c_supply[s], suppliers[s], 0).post();
        }
        for (int w = 0; w < nWH; w++) {
            IntVar tmp = model.intVar("occur_" + w, 0, suppliers.length, true);
            model.count(w, suppliers, tmp).post();
            model.arithm(tmp, ">=", open[w]).post();
        }
        // Do not exceed capacity
        for (int w = 0; w < nWH; w++) {
            IntVar tmp = model.intVar("occur_" + w, 0, capacity[w], true);
            model.count(w, suppliers, tmp).post();
        }

        int[] coeffs = new int[nWH + nS];
        fill(coeffs, 0, nWH, cost);
        fill(coeffs, nWH, nWH + nS, 1);
        model.scalar(append(open, costPerStore), coeffs, "=", totCost).post();
    }

    @Override
    public void configureSearch() {
        Solver r = model.getSolver();
        r.set(inputOrderLBSearch(suppliers), inputOrderLBSearch(costPerStore));
    }

    @Override
    public void solve() {
        model.setObjective(MINIMIZE, totCost);
        while(model.solve()){
            prettyOut();
        }
    }

    private void prettyOut() {
        System.out.println("Warehouse location problem");
        StringBuilder st = new StringBuilder();
        if (model.getSolver().isFeasible() == ESat.TRUE) {
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
        } else {
            st.append("\tINFEASIBLE");
        }

        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new WarehouseLocation().execute(args);
    }


    ////////////////////////////////////////// DATA ////////////////////////////////////////////////////////////////////
    enum Data {
        small(new int[]{
                5, 10, 30, //nWH = 5, nS = 10, cost = 30
                1, 4, 2, 1, 3, // capacity
                // c_supply
                20, 24, 11, 25, 30,
                28, 27, 82, 83, 74,
                74, 97, 71, 96, 70,
                2, 55, 73, 69, 61,
                46, 96, 59, 83, 4,
                42, 22, 29, 67, 59,
                1, 5, 73, 59, 56,
                10, 73, 13, 43, 96,
                93, 35, 63, 85, 46,
                47, 65, 55, 71, 95
        }),
        med(new int[]{
                7, 14, 30,
                1, 4, 2, 1, 3, 3, 1,
                // c_supply
                20, 24, 11, 25, 30, 15, 23,
                28, 27, 82, 83, 74, 24, 11,
                74, 97, 71, 96, 70, 82, 27,
                2, 55, 73, 69, 61, 10, 96,
                46, 96, 59, 83, 4, 36, 58,
                42, 22, 29, 67, 59, 64, 23,
                1, 5, 73, 59, 56, 48, 13,
                10, 73, 13, 43, 96, 1, 82,
                93, 35, 63, 85, 46, 99, 17,
                47, 65, 55, 71, 95, 25, 35,
                67, 59, 42, 22, 2, 46, 96,
                56, 1, 5, 73, 5, 42, 22,
                43, 96, 10, 73, 1, 1, 5,
                85, 46, 93, 35, 6, 10, 73,

        }),
        large(new int[]{
                10, 20, 30,
                1, 4, 2, 1, 3, 1, 4, 2, 1, 3, // capacity
                // c_supply
                20, 24, 11, 25, 30, 20, 24, 11, 25, 30,
                28, 27, 82, 83, 74, 28, 27, 82, 83, 74,
                74, 97, 71, 96, 70, 74, 97, 71, 96, 70,
                2, 55, 73, 69, 61, 2, 55, 73, 69, 61,
                46, 96, 59, 83, 4, 46, 96, 59, 83, 4,
                42, 22, 29, 67, 59, 42, 22, 29, 67, 59,
                1, 5, 73, 59, 56, 1, 5, 73, 59, 56,
                10, 73, 13, 43, 96, 10, 73, 13, 43, 96,
                93, 35, 63, 85, 46, 93, 35, 63, 85, 46,
                47, 65, 55, 71, 95, 47, 65, 55, 71, 95,
                20, 24, 11, 25, 30, 20, 24, 11, 25, 30,
                28, 27, 82, 83, 74, 28, 27, 82, 83, 74,
                74, 97, 71, 96, 70, 74, 97, 71, 96, 70,
                2, 55, 73, 69, 61, 2, 55, 73, 69, 61,
                46, 96, 59, 83, 4, 46, 96, 59, 83, 4,
                42, 22, 29, 67, 59, 42, 22, 29, 67, 59,
                1, 5, 73, 59, 56, 1, 5, 73, 59, 56,
                10, 73, 13, 43, 96, 10, 73, 13, 43, 96,
                93, 35, 63, 85, 46, 93, 35, 63, 85, 46,
                47, 65, 55, 71, 95, 47, 65, 55, 71, 95

        });
        final int[] data;

        Data(int[] data) {
            this.data = data;
        }

        public int get(int i) {
            return data[i];
        }
    }

}
