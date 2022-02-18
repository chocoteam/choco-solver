/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.Smallest;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelectorWithTies;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;

import java.util.Arrays;

import static org.chocosolver.util.tools.ArrayUtils.append;

/**
 * CSPLib prob034:<br/>
 * "In the Warehouse Location problem (WLP), a company considers opening warehouses
 * at some candidate locations in order to supply its existing stores. Each possible warehouse
 * has the same maintenance C, and a K designating the maximum number of stores
 * that it can supply. Each store must be supplied by exactly one open warehouse.
 * The supply C to a store depends on the warehouse. The objective is to determine which
 * warehouses to open, and which of these warehouses should supply the various stores, such
 * that the sum of the maintenance and supply costs is minimized."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/08/11
 */
public class WarehouseLocation extends AbstractProblem {

    @Option(name = "-d", aliases = "--data", usage = "Warehouse location instance.", required = false)
    Data data = Data.small;

    int W, S, C;
    int[] K;
    int[][] P;

    IntVar[] supplier;
    BoolVar[] open;
    IntVar[] cost;
    IntVar tot_cost;


    public void setUp() {
        int k = 0;
        W = data.data[k++];
        S = data.data[k++];
        C = data.data[k++];
        K = new int[W];
        for (int i = 0; i < W; i++) {
            K[i] = data.data[k++];
        }
        P = new int[S][W];
        for (int j = 0; j < S; j++) {
            for (int i = 0; i < W; i++) {
                P[j][i] = data.data[k++];
            }
        }
    }


    @Override
    public void buildModel() {
        model = new Model();
        setUp();
        supplier = model.intVarArray("sup", S, 1, W, false);
        open = model.boolVarArray("o", W);
        cost = model.intVarArray("cPs", S, 1, 96, true);
        tot_cost = model.intVar("C", 0, 99999, true);

        for (int s = 0; s < S; s++) {
            // A warehouse is open, if it supplies to a store
            model.element(model.intVar(1), open, supplier[s], 1).post();
            // Compute C for each warehouse
            model.element(cost[s], P[s], supplier[s], 1).post();
        }
        for (int w = 0; w < W; w++) {
            IntVar occ = model.intVar("occur_" + w, 0, K[w], true);
            model.count(w + 1, supplier, occ).post();
            occ.ge(open[w]).post();
//            occ.le(open[w].mul(100)).post();
        }
        int[] coeffs = new int[W + S];
        Arrays.fill(coeffs, 0, W, C);
        Arrays.fill(coeffs, W, W + S, 1);
        model.scalar(ArrayUtils.append(open, cost), coeffs, "=", tot_cost).post();
    }

    @Override
    public void configureSearch() {
        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(
                new VariableSelectorWithTies<>(
                        new FirstFail(model),
                        new Smallest()),
                new IntDomainMiddle(false),
                append(supplier, cost, open)));
    }

    @Override
    public void solve() {
        Solver solver = model.getSolver();
//        solver.setCBJLearning(false, false);
        solver.plugMonitor((IMonitorSolution) () -> prettyPrint());
        solver.showShortStatistics();
        solver.getObjectiveManager().setCutComputer(obj -> obj);
        solver.findOptimalSolution(tot_cost, false);
//        model.setObjective(ResolutionPolicy.MINIMIZE, tot_cost);
//        while (solver.solve()) {
//             do something on solution
//        }


    }

    private void prettyOut() {
        System.out.println("Warehouse location problem");
        if (model.getSolver().isFeasible() == ESat.TRUE) {
            prettyPrint();
        }

    }

    private void prettyPrint() {
        StringBuilder st = new StringBuilder();
        st.append("Solution #").append(model.getSolver().getSolutionCount()).append("\n");
        for (int i = 0; i < W; i++) {
            if (open[i].getValue() > 0) {
                st.append(String.format("\tWarehouse %d supplies customers : ", (i + 1)));
                for (int j = 0; j < S; j++) {
                    if (supplier[j].getValue() == (i + 1)) {
                        st.append(String.format("%d ", (j + 1)));
                    }
                }
                st.append("\n");
            }
        }
        st.append("\tTotal C: ").append(tot_cost.getValue());
        System.out.println(st);
    }

    public static void main(String[] args) {
        new WarehouseLocation().execute(args);
    }


    ////////////////////////////////////////// DATA ////////////////////////////////////////////////////////////////////
    enum Data {
        small(new int[]{
                5, 10, 30, //W = 5, S = 10, C = 30
                1, 4, 2, 1, 3, // K
                // P
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
                // P
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
                1, 4, 2, 1, 3, 1, 4, 2, 1, 3, // K
                // P
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
