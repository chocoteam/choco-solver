/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;

/**
 * A factory dedicated to problems creation.
 * Created by cprudhom on 20/11/2015.
 * Project: choco.
 */
public class ProblemMaker {

    /**
     * Creates a n-Queen problem with only binary constraints.
     * n queens must be placed on a nxn chessboard.
     * The variables can be accessed though the hook name "vars".
     * @param n number of queens (or size of the chessboard)
     * @return a solve-ready solver.
     */
    @SuppressWarnings("Duplicates")
    public static Model makeNQueenWithBinaryConstraints(int n){
        Model model = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = model.intVar("Q_" + i, 1, n, false);
        }
        model.addHook("vars", vars);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                model.arithm(vars[i], "!=", vars[j]).post();
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        return model;
    }

    /**
     * Creates a n-Queen problem with one allDifferent constraint and binary constraints.
     * n queens must be placed on a nxn chessboard.
     * The variables can be accessed though the hook name "vars".
     * @param n number of queens (or size of the chessboard)
     * @return a solve-ready solver.
     */
    @SuppressWarnings("Duplicates")
    public static Model makeNQueenWithOneAlldifferent(int n) {
        Model model = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = model.intVar("Q_" + i, 1, n, false);
        }
        model.addHook("vars", vars);
        model.allDifferent(vars, "AC").post();
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        return model;
    }

    /**
     * Creates a Costas array problem of size n.
     * Two AllDifferent constraints are used, on achieving AC, the other BC.
     * The variables can be accessed though the hook name "vars" and "vectors".
     * @param n size of the array.
     * @return a solve-ready solver
     */
    @SuppressWarnings("Duplicates")
    public static Model makeCostasArrays(int n) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("v", n, 0, n - 1, false);
        IntVar[] vectors = new IntVar[(n * (n - 1)) / 2];
        IntVar[][] diff = new IntVar[n][n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                IntVar k = model.intVar(model.generateName(), -n, n, false);
                model.arithm(k, "!=", 0).post();
                model.sum(new IntVar[]{vars[i], k}, "=", vars[j]).post();
                vectors[idx] = model.intOffsetView(k, 2 * n * (j - i));
                diff[i][j] = k;
                idx++;
            }
        }
        model.addHook("vars", vars);
        model.addHook("vectors", vectors);

        model.allDifferent(vars, "AC").post();
        model.allDifferent(vectors, "BC").post();

        // symmetry-breaking
        model.arithm(vars[0], "<", vars[n - 1]).post();
        return model;
    }

    /**
     * Creates a Golomb ruler problem of size m.
     * The variables can be accessed though the hook name "ticks" and "diffs".
     * @param m size of the rule
     * @return a solve-ready solver
     */
    @SuppressWarnings("Duplicates")
    public static Model makeGolombRuler(int m) {
        Model model = new Model();
        IntVar[] ticks = model.intVarArray("a", m, 0, (m < 31) ? (1 << (m + 1)) - 1 : 9999, false);
        model.addHook("ticks", ticks);
        IntVar[] diffs = model.intVarArray("d", (m * m - m) / 2, 0, (m < 31) ? (1 << (m + 1)) - 1 : 9999, false);
        model.addHook("diffs", diffs);
        model.arithm(ticks[0], "=", 0).post();

        for (int i = 0; i < m - 1; i++) {
            model.arithm(ticks[i + 1], ">", ticks[i]).post();
        }

        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                // d[k] is m[j]-m[i] and must be at least sum of first j-i integers
                model.arithm(ticks[j], "-", ticks[i], "=", diffs[k]).post();
                model.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2).post();
                model.arithm(diffs[k], "-", ticks[m - 1], "<=", -((m - 1 - j + i) * (m - j + i)) / 2).post();
                model.arithm(diffs[k], "<=", ticks[m - 1], "-", ((m - 1 - j + i) * (m - j + i)) / 2).post();
            }
        }
        model.allDifferent(diffs, "BC").post();
        // break symetries
        if (m > 2) {
            model.arithm(diffs[0], "<", diffs[diffs.length - 1]).post();
        }
        model.addHook("objective", ticks[m - 1]);
        model.setObjective(Model.MINIMIZE,ticks[m - 1]);
        return model;
    }

    public static Model makeEq5(){
        Model model = new Model("Eq5");
        IntVar[] vars = new IntVar[15];
        vars[0] = model.intVar("A90397", -20, 20, false);
        vars[1] = model.intVar("A90429", -20, 20, false);
        vars[2] = model.intVar("A90461", -20, 20, false);
        vars[3] = model.intVar("A90493", -20, 20, false);
        vars[4] = model.intVar("A90525", -20, 20, false);
        vars[5] = model.intVar("A90557", -20, 20, false);
        vars[6] = model.intVar("A90589", -20, 20, false);
        vars[7] = model.intVar("A90621", -20, 20, false);
        vars[8] = model.intVar("A90653", -20, 20, false);
        vars[9] = model.intVar("A90685", -20, 20, false);
        vars[10] = model.intVar("A90717", -20, 20, false);
        vars[11] = model.intVar("A90749", -20, 20, false);
        vars[12] = model.intVar("A90781", -20, 20, false);
        vars[13] = model.intVar("A90813", -20, 20, false);
        vars[14] = model.intVar("A90845", -20, 20, false);

        model.scalar(vars, new int[]{1, 3, 9, 27, 81, 1, 3, 9, 27, 1, 3, 9, 1, 3, 1}, "=", 380).post();
        model.scalar(vars, new int[]{81, 108, 144, 192, 256, 27, 36, 48, 64, 9, 12, 16, 3, 4, 1}, "=", 1554).post();
        model.scalar(vars, new int[]{1296, 1080, 900, 750, 625, 216, 180, 150, 125, 36, 30, 25, 6, 5, 1}, "=", 4392).post();
        model.scalar(vars, new int[]{16, 56, 496, 686, 2401, 8, 28, 98, 343, 4, 14, 49, 2, 7, 1}, "=", 16510).post();
        model.scalar(vars, new int[]{194481, 55566, 15876, 4536, 1296, 9261, 2646, 756, 216, 441, 126, 36, 21, 6, 1}, "=", 12012).post();

        model.getSolver().setSearch(new IntStrategy(vars, new InputOrder<>(model), new IntDomainMiddle(true)));

        return model;
    }

    public static Model makeContrived(){
        Model model = new Model("Contrived");
        int l = 100;
        int d = l + 1;
        IntVar[] v = model.intVarArray("v", 5, 1, 50, true);
        IntVar[] w = model.intVarArray("w", l, 1, d, true);

        model.allDifferent(v, "BC").post();
        model.allDifferent(w, "BC").post();
        model.arithm(v[3], "=", v[4]).post();
        model.arithm(v[0], "=", w[0]).post();
        model.arithm(v[1], "=", w[1]).post();
        model.arithm(v[2], "=", w[2]).post();
        model.arithm(v[3], "=", w[3]).post();

        return model;
    }
}
