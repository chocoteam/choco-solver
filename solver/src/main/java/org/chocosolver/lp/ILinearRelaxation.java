/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.solver.variables.IntVar;

import java.lang.reflect.InvocationTargetException;

/**
 * A linear relaxation of (part of) a constraint model, built and solved incrementally.
 * <p>
 * The relaxation is built directly on the implementation: there is no intermediate matrix/vector
 * representation. A consumer (typically {@link PropSimplex}) creates an instance sized for {@code nbVars}
 * columns, then declares the objective and the constraints by calling the methods below.
 * <ul>
 * <li>The objective is a single variable, optimised in the direction given to {@link #setObjective(int, boolean)};
 * if it is never called, the relaxation is solved for feasibility only. {@link #getObjectiveValue()} and
 * {@link #getReducedCosts()} are reported in the natural sense of that direction.
 * <li>Constraints are added incrementally, split between equalities ({@code row·x == rhs}) and inequalities
 * ({@code row·x <= rhs}).
 * <li>Variable ranges are updated in place; the underlying solver instance is reused across solves when the
 * implementation supports it.
 * </ul>
 * The actual implementation is chosen at runtime, based on the presence of the backing library on the
 * classpath (see {@link Solver}). The library is an optional dependency of choco-solver: it must be added by the
 * user (with modules, also with {@code --add-modules}), otherwise an exception is raised when the relaxation is
 * created.
 */
public interface ILinearRelaxation {

    /**
     * Writes the coefficients of a single constraint row, by column index.
     */
    interface RowMutator {

        /**
         * Add {@code value} to the coefficient of a column in this row. Coefficients start at 0 and add up, so a
         * variable that appears several times in a constraint gets the sum of its coefficients.
         *
         * @param column the column (variable) index
         * @param value  the value to add to the coefficient of that variable in this row
         */
        void add(int column, double value);
    }

    /**
     * The outcome of {@link #solve()}.
     */
    enum Status {
        /**
         * The relaxation is solved to optimality: the objective value is a valid bound, and the reduced costs
         * and the solution can be used.
         */
        OPTIMAL,
        /**
         * The relaxation is proven to have no solution.
         */
        INFEASIBLE,
        /**
         * Nothing is proven: the solver hit its time limit, failed, or stopped on a point that is not proven
         * optimal. Nothing can be deduced, in particular not that the relaxation has no solution.
         */
        UNKNOWN
    }

    /**
     * The available linear-relaxation backends.
     */
    enum Solver {

        OJALGO("org.ojalgo.optimisation.ExpressionsBasedModel", "org.chocosolver.lp.OjAlgoRelaxation"),
        OJALGO_EBM("org.ojalgo.optimisation.ExpressionsBasedModel", "org.chocosolver.lp.OjAlgoDebugRelaxation");

        private final String canary;
        private final String implementation;

        Solver(String detect, String impl) {
            this.canary = detect;
            this.implementation = impl;
        }

        /**
         * Create a relaxation instance for this backend, sized for {@code nbVars} columns.
         *
         * @param nbVars the number of variables (columns) in the relaxation
         * @return a fresh, empty relaxation ready to be populated
         * @throws RuntimeException if the backing library is not on the classpath
         */
        public ILinearRelaxation create(int nbVars) {
            try {
                // 1. Check the backing library is present in the classpath
                Class.forName(canary);
                // 2. Instantiate the implementation
                Class<?> impl = Class.forName(implementation);
                return (ILinearRelaxation) impl.getDeclaredConstructor(int.class).newInstance(nbVars);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Linear relaxation backend '" + name() + "' is not available: "
                        + "add its library (for OJALGO: org.ojalgo:ojalgo) to the classpath, or to the module path "
                        + "with --add-modules (for OJALGO: --add-modules ojalgo), since choco-solver only declares "
                        + "it as an optional dependency.", e);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error initializing linear relaxation backend '" + name() + "': " + e.getMessage(), e);
            }
        }
    }

    /**
     * Add the linear relaxation of {@code b => sum(coeffs[i] * vars[i]) <= rhs}, with a big-M computed from the
     * current bounds of the variables: {@code sum(coeffs[i] * vars[i]) + M * b <= rhs + M}, where {@code M} is the
     * largest value the left-hand side can exceed {@code rhs} by. Nothing is added when the inequality always holds.
     * <p>
     * The row relies on the current bounds: it is only valid where they hold, which is the whole search when the
     * relaxation is built at the root node, as {@link PropSimplex} does.
     *
     * @param lp     the relaxation
     * @param vmap   the column of each variable
     * @param b      the condition, a boolean variable
     * @param vars   the variables of the inequality
     * @param coeffs their coefficients
     * @param rhs    the right-hand side
     * @return the number of rows added: 0 or 1
     */
    static int addImpliedInequality(ILinearRelaxation lp, TObjectIntHashMap<IntVar> vmap, IntVar b,
                                    IntVar[] vars, int[] coeffs, long rhs) {
        long max = 0;
        for (int i = 0; i < vars.length; i++) {
            max += coeffs[i] > 0 ? (long) coeffs[i] * vars[i].getUB() : (long) coeffs[i] * vars[i].getLB();
        }
        long bigM = max - rhs;
        if (bigM <= 0) {
            return 0;
        }
        RowMutator row = lp.addInequalityConstraint((double) (rhs + bigM));
        for (int i = 0; i < vars.length; i++) {
            row.add(vmap.get(vars[i]), coeffs[i]);
        }
        row.add(vmap.get(b), (double) bigM);
        return 1;
    }

    /**
     * Add the convex hull of a finite set of points {@code (x, y)}: the relation between two variables that can
     * only take these pairs of values (for instance {@code y = table[x]}). One row is added for each edge of the
     * lower and of the upper hull; the vertical edges are left out, since they are bounds of {@code x}.
     * <p>
     * The points are usually computed from the current domains: the rows are then only valid where these
     * domains hold, which is the whole search when the relaxation is built at the root node, as
     * {@link PropSimplex} does.
     *
     * @param lp   the relaxation
     * @param vmap the column of each variable
     * @param x    the first variable
     * @param y    the second variable
     * @param px   the first coordinate of the points
     * @param py   the second coordinate of the points
     * @param n    the number of points
     * @return the number of rows added
     */
    static int addConvexHull(ILinearRelaxation lp, TObjectIntHashMap<IntVar> vmap, IntVar x, IntVar y,
                             long[] px, long[] py, int n) {
        if (n < 2) {
            return 0;
        }
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        java.util.Arrays.sort(order, (i, j) -> px[i] != px[j] ? Long.compare(px[i], px[j]) : Long.compare(py[i], py[j]));
        int rows = 0;
        // lower hull from left to right (counterclockwise turns), upper hull from right to left
        for (int pass = 0; pass < 2; pass++) {
            int[] hull = new int[n];
            int size = 0;
            for (int k = 0; k < n; k++) {
                int p = order[pass == 0 ? k : n - 1 - k];
                while (size >= 2 && cross(px, py, hull[size - 2], hull[size - 1], p) <= 0) {
                    size--;
                }
                hull[size++] = p;
            }
            for (int k = 0; k + 1 < size; k++) {
                int p = hull[k], q = hull[k + 1];
                long dx = px[q] - px[p], dy = py[q] - py[p];
                if (dx == 0) {
                    continue;
                }
                // the points lie on the left of the edge p -> q: (q - p) x (point - p) >= 0, that is
                // dy * x - dx * y <= dy * px[p] - dx * py[p]
                RowMutator row = lp.addInequalityConstraint((double) dy * px[p] - (double) dx * py[p]);
                row.add(vmap.get(x), (double) dy);
                row.add(vmap.get(y), (double) -dx);
                rows++;
            }
        }
        return rows;
    }

    private static long cross(long[] px, long[] py, int o, int a, int b) {
        return (px[a] - px[o]) * (py[b] - py[o]) - (py[a] - py[o]) * (px[b] - px[o]);
    }

    /**
     * Declare the objective as a single variable to optimise. If this method is never called, the relaxation
     * is solved for feasibility only.
     *
     * @param var      the column (variable) index of the objective
     * @param maximise {@code true} if the original problem maximises this variable, {@code false} to minimise
     */
    void setObjective(int var, boolean maximise);

    /**
     * Add an equality constraint {@code row·x == rhs} and return its row mutator.
     *
     * @param rhs the right-hand side
     * @return the mutator used to set the row coefficients
     */
    RowMutator addEqualityConstraint(double rhs);

    /**
     * Add an inequality constraint {@code row·x <= rhs} and return its row mutator.
     *
     * @param rhs the right-hand side
     * @return the mutator used to set the row coefficients
     */
    RowMutator addInequalityConstraint(double rhs);

    /**
     * Update the bounds of a variable. Updates are applied in place on the solver instance when possible.
     *
     * @param var   the column (variable) index
     * @param lower the new lower bound
     * @param upper the new upper bound
     */
    void updateRange(int var, int lower, int upper);

    /**
     * Solve the relaxation. The objective value, the reduced costs and the solution are only available when
     * the outcome is {@link Status#OPTIMAL}.
     *
     * @return the outcome of the resolution
     */
    Status solve();

    /**
     * A proof that the LP is infeasible, available after {@link #solve()} returned {@link Status#INFEASIBLE}: a
     * row {@code g·x <= r} implied by the constraints, that no point within the current ranges satisfies.
     * Only the ranges it relies on matter: the lower bound of each column {@code j} with {@code g[j] > 0}, the
     * upper bound of each column with {@code g[j] < 0}; the ranges of the columns with {@code g[j] == 0} play
     * no role.
     *
     * @return the coefficients {@code g} of the aggregated row, indexed by column, or {@code null} if no
     *         certificate is available
     */
    default double[] getInfeasibilityCertificate() {
        return null;
    }

    /**
     * @return the optimal objective value of the last solve, expressed in the natural sense of the original
     *         problem
     */
    double getObjectiveValue();

    /**
     * @return the reduced costs of the variables after the last solve, expressed in the natural sense of the
     *         original problem. The returned array is indexed by column.
     */
    double[] getReducedCosts();

    /**
     * The values the variables take in the solution of the last solve. The returned array is indexed by
     * column, and its contents are only valid until the next {@link #solve()}.
     * <p>
     * Useful to skip redundant solves: if the previous solution is still within the current variable ranges,
     * <em>and</em> those ranges are contained in the ones that were in force when it was computed, then it is
     * still optimal for the current relaxation and there is nothing to gain from solving again. Note that
     * both conditions are needed - on backtracking the ranges widen, and a solution that was optimal for the
     * narrower ranges need not be optimal for the wider ones.
     *
     * @return the solution of the last solve, indexed by column
     */
    double[] getSolution();
}
