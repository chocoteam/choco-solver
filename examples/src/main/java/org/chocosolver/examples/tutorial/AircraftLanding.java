/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.tutorial;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * <p>
 * Project: samples.
 * @author Charles Prud'homme
 * @since 26/09/2016.
 */
public class AircraftLanding {

    public void modelAndSolve() {
        // number of planes
        int N = 10;
        // Times per plane:
        // {earliest landing time, target landing time, latest landing time}
        int[][] LT = {
                {129, 155, 559},
                {195, 258, 744},
                {89, 98, 510},
                {96, 106, 521},
                {110, 123, 555},
                {120, 135, 576},
                {124, 138, 577},
                {126, 140, 573},
                {135, 150, 591},
                {160, 180, 657}};
        // penalty cost penalty cost per unit of time per plane:
        // {for landing before target, after target}
        int[][] PC = {
                {10, 10},
                {10, 10},
                {30, 30},
                {30, 30},
                {30, 30},
                {30, 30},
                {30, 30},
                {30, 30},
                {30, 30},
                {30, 30}};
        // Separation time required after i lands before j can land
        int[][] ST = {
                {99999, 3, 15, 15, 15, 15, 15, 15, 15, 15},
                {3, 99999, 15, 15, 15, 15, 15, 15, 15, 15},
                {15, 15, 99999, 8, 8, 8, 8, 8, 8, 8},
                {15, 15, 8, 99999, 8, 8, 8, 8, 8, 8},
                {15, 15, 8, 8, 99999, 8, 8, 8, 8, 8},
                {15, 15, 8, 8, 8, 99999, 8, 8, 8, 8},
                {15, 15, 8, 8, 8, 8, 99999, 8, 8, 8},
                {15, 15, 8, 8, 8, 8, 8, 999999, 8, 8},
                {15, 15, 8, 8, 8, 8, 8, 8, 99999, 8},
                {15, 15, 8, 8, 8, 8, 8, 8, 8, 99999}};

        Model model = new Model("Aircraft landing");
        // Variables declaration
        IntVar[] planes = IntStream
                .range(0, N)
                .mapToObj(i -> model.intVar("plane #" + i, LT[i][0], LT[i][2], false))
                .toArray(IntVar[]::new);
        IntVar[] earliness = IntStream
                .range(0, N)
                .mapToObj(i -> model.intVar("earliness #" + i, 0, LT[i][1] - LT[i][0], false))
                .toArray(IntVar[]::new);
        IntVar[] tardiness = IntStream
                .range(0, N)
                .mapToObj(i -> model.intVar("tardiness #" + i, 0, LT[i][2] - LT[i][1], false))
                .toArray(IntVar[]::new);
        IntVar tot_dev = model.intVar("tot_dev", 0, IntVar.MAX_INT_BOUND);
        // Constraint posting
        // one plane per runway at a time:
        model.allDifferent(planes).post();
        // for each plane 'i'
        for (int i = 0; i < N; i++) {
            // maintain earliness
            earliness[i].eq((planes[i].neg().add(LT[i][1])).max(0)).post();
            // and tardiness
            tardiness[i].eq((planes[i].sub(LT[i][1])).max(0)).post();
            // disjunctions: 'i' lands before 'j' or 'j' lands before 'i'
            for (int j = i + 1; j < N; j++) {
                Constraint iBeforej = model.arithm(planes[i], "<=", planes[j], "-", ST[i][j]);
                Constraint jBeforei = model.arithm(planes[j], "<=", planes[i], "-", ST[j][i]);
                model.addClausesBoolNot(iBeforej.reify(), jBeforei.reify()); // no need to post
            }
        }
        // prepare coefficients of the scalar product
        int[] cs = new int[N * 2];
        for (int i = 0; i < N; i++) {
            cs[i] = PC[i][0];
            cs[i + N] = PC[i][1];
        }
        model.scalar(ArrayUtils.append(earliness, tardiness), cs, "=", tot_dev).post();
        // Resolution process
        Solver solver = model.getSolver();
        solver.plugMonitor((IMonitorSolution) () -> {
            for (int i = 0; i < N; i++) {
                System.out.printf("%s lands at %d (%d)\n",
                        planes[i].getName(),
                        planes[i].getValue(),
                        planes[i].getValue() - LT[i][1]);
            }
            System.out.printf("Deviation cost: %d\n", tot_dev.getValue());
        });
        Map<IntVar, Integer> map = IntStream
                .range(0, N)
                .boxed()
                .collect(Collectors.toMap(i -> planes[i], i -> LT[i][1]));
        solver.setSearch(Search.intVarSearch(
                variables -> Arrays.stream(variables)
                        .filter(v -> !v.isInstantiated())
                        .min((v1, v2) -> closest(v2, map) - closest(v1, map))
                        .orElse(null),
                var -> closest(var, map),
                DecisionOperatorFactory.makeIntEq(),
                planes
        ));
        solver.showShortStatistics();
        solver.findOptimalSolution(tot_dev, false);
    }

    private static int closest(IntVar var, Map<IntVar, Integer> map) {
        int target = map.get(var);
        if (var.contains(target)) {
            return target;
        } else {
            int p = var.previousValue(target);
            int n = var.nextValue(target);
            return Math.abs(target - p) < Math.abs(n - target) ? p : n;
        }
    }

    public static void main(String[] args) {
        new AircraftLanding().modelAndSolve();
    }
}
