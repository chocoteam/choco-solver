/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import java.util.Arrays;
import java.util.Optional;

/**
 * <p>A linear program, equipped with a Simplex method.</p>
 * <p>This is based on "Introduction to Algorithms, Third Edition",
 * By Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest and Clifford Stein,
 * 29.3 The simplex algorithm.</p>
 * <p>The linear program is expected to be given in slack form,
 * that omits the words "maximize" and "subject to",
 * as well as the explicit nonnegativity constraints.</p>
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/07/2022
 */
public class LinearProgram {

    private static class Slack {
        // number of non-basic variables
        private final int n;
        // number of basic variables
        private final int m;
        // a mxn matrix
        private final double[][] A;
        // an m-vector
        private final double[] b;
        // an n-vector
        private final double[] c;
        // an m-vector, the basic variables
        private final int[] B;
        // an n-vector, the non-basic variables
        private final int[] N;
        // an integer
        private double v;

        /**
         * Slack form of a linear program
         *
         * @param a an m.n matrix
         * @param b an m-vector
         * @param c an n-vector
         */
        public Slack(double[][] a, double[] b, double[] c) {
            this.m = b.length;
            this.n = c.length;
            this.A = a.clone();
            this.b = b.clone();
            this.c = c.clone();
            this.N = new int[n];
            this.B = new int[m];
            this.v = 0;
            initialBasicSolution();
        }

        /**
         * Slack form of a linear program with N and B already defined
         *
         * @param a an m.n matrix
         * @param b an m-vector
         * @param c an n-vector
         * @param N an n-vector
         * @param B an m-vector
         * @param v a double
         */
        public Slack(double[][] a, double[] b, double[] c, int[] N, int[] B, double v) {
            this.m = b.length;
            this.n = c.length;
            this.A = a;
            this.b = b;
            this.c = c;
            this.N = N;
            this.B = B;
            this.v = v;
        }

        /**
         * Initialize N and B wrt basic solution
         */
        private void initialBasicSolution() {
            this.v = 0;
            for (int i = 0; i < n; i++) {
                this.N[i] = i;
            }
            for (int i = 0; i < m; i++) {
                this.B[i] = i + n;
            }
        }

        /**
         * The method takes as input a slack form and it modifies in-place A, b, c, v, N and B.
         *
         * @param l index of the leaving variable
         * @param e index of the entering variable
         */
        void pivot(int l, int e, boolean trace) {
            if (trace) System.out.printf("[pivot] e: x%d, l: x%d\n", B[l], N[e]);
            // Compute the coefficients of the equation for new basic variable x_e
            b[l] /= A[l][e];
            for (int j = 0; j < n; j++) {
                if (j != e) {
                    A[l][j] /= A[l][e];
                }
            }
            A[l][e] = 1 / A[l][e];
            // Compute the coefficients of the remaining constraints
            for (int i = 0; i < m; i++) {
                if (i != l) {
                    b[i] -= A[i][e] * b[l];
                    for (int j = 0; j < n; j++) {
                        if (j != e) {
                            A[i][j] = A[i][j] - A[i][e] * A[l][j];
                        }
                    }
                    A[i][e] *= -A[l][e];
                }
            }
            // Compute the objective function
            v += c[e] * b[l];
            for (int j = 0; j < n; j++) {
                if (j != e) {
                    c[j] -= c[e] * A[l][j];
                }
            }
            c[e] *= -A[l][e];

            // Update basic and non-basic variables set
            int x = N[e];
            N[e] = B[l];
            B[l] = x;
        }

        private void setValues(double[] x) {
            for (int i = 0; i < m; ++i) {
                if (B[i] < n) x[B[i]] = b[i];
            }
        }

        @Override
        public String toString() {
            StringBuilder st = new StringBuilder();
            st.append("z = ").append(v);
            for (int i = 0; i < n; i++) {
                st.append(c[i] >= 0. ? " + " : " - ")
                        .append(String.format("%.4f", Math.abs(c[i])))
                        .append(" * x").append(N[i]);
            }
            st.append("\n");
            for (int j = 0; j < m; j++) {
                st.append("x")
                        .append(B[j])
                        .append(" = ")
                        .append(b[j]);
                for (int i = 0; i < n; i++) {
                    st.append(A[j][i] <= 0. ? " + " : " - ")
                            .append(String.format("%.4f", Math.abs(A[j][i])))
                            .append(" * x")
                            .append(N[i]);
                }
                st.append("\n");
            }
            return st.toString();
        }
    }

    public enum Status {
        UNKNOWN,
        FEASIBLE,
        INFEASIBLE,
        UNBOUNDED
    }

    // number of coefficients
    private final int n;
    // number of constraints
    private final int m;
    // a mxn matrix
    private final double[][] A;
    // an m-vector
    private final double[] b;
    // an n-vector
    private final double[] c;
    // an n-vector
    private final double[] x;
    private double z;
    // feasibility of the LP
    private Status status = Status.UNKNOWN;
    // trace the resolution
    private final boolean trace;

    /**
     * Create a LinearProgram instance that take a linear program in standard form as input.
     *
     * @param matA is an mxn matrix
     * @param vecB is an m-vector
     * @param vecC is n-vector
     */
    public LinearProgram(double[][] matA, double[] vecB, double[] vecC) {
        this(matA, vecB, vecC, false);
    }

    /**
     * Create a LinearProgram instance that take a linear program in standard form as input.
     *
     * @param matA  is an mxn matrix
     * @param vecB  is an m-vector
     * @param vecC  is n-vector
     * @param trace set to <i>true</i> to trace the resolution
     */
    public LinearProgram(double[][] matA, double[] vecB, double[] vecC, boolean trace) {
        this.m = vecB.length;
        this.n = vecC.length;
        this.A = matA.clone();
        this.b = vecB.clone();
        this.c = vecC.clone();
        this.x = new double[n];
        this.trace = trace;
    }


    /**
     * Apply the Simplex algorithm on this linear program.
     * <p>If the problem is infeasible, this method terminates.
     * Otherwise, the optimal solution of this linear program is computed and values of the variables
     * can be read calling {@link #value(int)}.
     * </p>
     *
     * @return <i>true</i> is this LP is feasible, <i>false</i> otherwise.
     */
    public Status simplex() {
        // slack form of the LP
        Optional<Slack> opt = initialize();
        if (opt.isPresent()) {
            Slack slack = opt.get();
            if (Status.FEASIBLE.equals(status = solve(slack))) {
                slack.setValues(x);
                this.z = slack.v;
            }
        }
        return status;
    }

    /**
     * Main loop of the simplex.
     *
     * @param lipr a linear program, under slack form
     * @return the status of the resolution
     */
    private Status solve(Slack lipr) {
        if (trace) System.out.printf("%s", lipr);
        int e = enteringVariable(lipr);
        while (e < Integer.MAX_VALUE) {
            int l = leavingVariable(lipr, e);
            if (l < 0) {
                return Status.UNBOUNDED;
            }
            lipr.pivot(l, e, trace);
            e = enteringVariable(lipr);
            if (trace) System.out.printf("%s", lipr);
        }
        return Status.FEASIBLE;
    }

    /**
     * Determines that the linear program is infeasible
     * or returns a slack form for which the basic solution is feasible
     *
     * @return optional slack form of the linear program defined is this
     */
    private Optional<Slack> initialize() {
        this.status = Status.UNKNOWN;
        Arrays.fill(this.x, 0.);
        this.z = 0.;
        int k = argmin(b);
        if (b[k] >= 0) {
            if (trace) System.out.println("Initial basic solution feasible");
            return Optional.of(new Slack(A, b, c));
        }
        if (trace) System.out.println("Formulate an auxiliary linear program, adding x0");
        int x0 = 0;
        Slack laux = auxiliaryLinearProgram(A, b, c);
        if (trace) System.out.print(laux);
        laux.pivot(k, x0, trace);
        // The basic solution is now feasible for laux
        this.status = Status.INFEASIBLE;
        if (Status.FEASIBLE.equals(solve(laux))) {
            double[] x = new double[laux.n];
            laux.setValues(x);
            if (x[x0] == 0.) { // if the optimal solution to laux sets x0 to 0
                // Since this solution has x0 = 0, we know that our initial problem was feasible
                this.status = Status.FEASIBLE;
                for (int i = 0; i < laux.m; i++) {
                    if (laux.B[i] == x0) { // if x0 is basic
                        // perform one (degenerate) pivot to make it nonbasic
                        // using any e in N such that a0e != 0
                        int e = -1;
                        for (int j = 0; j < laux.n; j++) {
                            if (laux.A[i][j] != 0) {
                                e = j;
                                break;
                            }
                        }
                        if (e == -1) throw new UnsupportedOperationException();
                        if (trace) System.out.println("Perform one (degenerate) pivot to make it nonbasic");
                        laux.pivot(i, e, trace);
                        break;
                    }
                }
                // from the final slack form of laux
                if (isFeasible()) {
                    if (trace) System.out.println("Modified final slack form");
                    return Optional.of(finalSlack(laux, this.c));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Determine the index of the constraint with the smallest b.
     *
     * @param b right-hand side coefficients
     * @return the index of the smallest b
     */
    private int argmin(double[] b) {
        int k = 0;
        double bk = b[k];
        for (int i = 1; i < m; i++) {
            if (bk > b[i]) {
                bk = b[i];
                k = i;
            }
        }
        return k;
    }

    /**
     * Form the auxiliary linear program by adding -x0 to the LHS of each constraint
     * and setting the objective function to -x0.
     *
     * @return the resulting slack form of L_aux
     */
    private static Slack auxiliaryLinearProgram(double[][] A, double[] b, double[] c) {
        int n = c.length;
        int m = b.length;
        double[][] A2 = new double[m][n + 1];
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, A2[i], 1, n);
            A2[i][0] = -1;
        }
        double[] c2 = new double[n + 1];
        c2[0] = -1;
        return new Slack(A2, b, c2);
    }

    /**
     * remove x0 from the constraints and restore the original objective function of L,
     * but replace each basic variable in this objective function by the right-hand side of its associated constraint.
     *
     * @param laux the auxiliary linear program to transform
     * @return final slack form of laux
     */
    private static Slack finalSlack(Slack laux, double[] oriC) {
        int n = laux.n - 1;
        int m = laux.m;
        // since x0 = 0, we can just remove it from the set of constraints
        double[][] A2 = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0, k = 0; j <= n; j++) {
                if (laux.N[j] > 0) {
                    A2[i][k++] = laux.A[i][j];
                }
            }
        }
        double[] b2 = new double[m];
        System.arraycopy(laux.b, 0, b2, 0, m);
        // We then restore the original objective function,
        double[] ct = new double[n + m + 1];
        System.arraycopy(oriC, 0, ct, 1, n);
        // with appropriate substitutions made to include only nonbasic variables
        double z = 0.;
        for (int j = 0; j < laux.m; j++) {
            // for every nonbasic variables
            int nx = laux.B[j];
            if (nx <= n) {
                double cx = ct[nx];
                ct[nx] = 0.;
                // do the substitutions
                z += cx * laux.b[j];
                for (int i = 0; i < laux.n; i++) {
                    ct[laux.N[i]] -= cx * laux.A[j][i];
                }
            }
        }
        double[] c2 = new double[n];
        int[] N2 = new int[n];
        for (int i = 0, j = 0; i < laux.n; i++) {
            if (laux.N[i] > 0) {
                c2[j] = ct[laux.N[i]];
                N2[j++] = laux.N[i] - 1;
            }
        }
        int[] B2 = new int[m];
        for (int i = 0; i < laux.m; i++) {
            B2[i] = laux.B[i] - 1;
        }
        return new Slack(A2, b2, c2, N2, B2, z);
    }

    /**
     * @return <i>true</i> if the application of the Simplex algorithm computed the (optimal) solution.
     */
    public boolean isFeasible() {
        return status == Status.FEASIBLE;
    }

    /**
     * Return the value of the ith variable in the linear program.
     * <p>
     * If this is infeasible, return {@code -1.}, otherwise the value is returned.
     * </p>
     *
     * @param i index of the variable.
     * @return the value assigned the ith variable in this linear program.
     */
    public double value(int i) {
        if (isFeasible()) {
            return x[i];
        } else return -1.;
    }

    /**
     * Return the value of the objective function defined in this linear program.
     * <p>
     * If this is not feasible, returns {@link Double#NEGATIVE_INFINITY},
     * otherwise, the optimal value is returnd.
     * </p>
     *
     * @return the value of the objective function.
     */
    public double objective() {
        if (isFeasible()) {
            return z;
        } else return Double.NEGATIVE_INFINITY;
    }

    /**
     * Choose the entering variable with c > 0
     * and break ties by always choosing the variable
     * with the smallest index (Bland's rule).
     *
     * @return a value between [0, n) if an entering variable is found, {@link Integer#MAX_VALUE} otherwise.
     */
    private static int enteringVariable(Slack s) {
        int j = Integer.MAX_VALUE;
        for (int i = 0; i < s.n; i++) {
            if (s.c[i] > 0. && s.N[i] < j) {
                j = i;
            }
        }
        return j;
    }


    /**
     * Choose the leaving variable, the one with the smallest ratio b(i) / A(i,e).
     * Ties are broken with index value (Bland's rule)
     *
     * @param s the slack
     * @param e index of the entering variable
     * @return index of the leaving variable
     */
    private static int leavingVariable(Slack s, int e) {
        double min_v = Double.POSITIVE_INFINITY;
        int l = -1;
        for (int i = 0; i < s.m; i++) {
            if (s.A[i][e] > 0) {
                double d = s.b[i] / s.A[i][e];
                if (min_v > d) {
                    l = i;
                    min_v = d;
                }
            }
        }
        return l;
    }

    @Override
    public String toString() {
        return toLP(A, b, c, 0.);
    }

    private static String toLP(double[][] A, double[] b, double[] c, double v) {
        StringBuilder st = new StringBuilder();
        st.append("Maximize").append('\n');
        st.append("\\ v = ").append(v).append("\n");
        st.append(" obj:");
        for (int i = 0; i < c.length; i++) {
            st.append(c[i] >= 0 ? " +" : " ")
                    .append(c[i])
                    .append(" x")
                    .append(i + 1);
        }
        st.append("\nSubject to\n");
        for (int j = 0; j < b.length; j++) {
            st.append(" c").append(j + 1).append(": ");
            st.append(A[j][0]).append(" x1");
            for (int i = 1; i < c.length; i++) {
                st.append(A[j][i] >= 0 ? " +" : " ")
                        .append(A[j][i])
                        .append(" x")
                        .append(i + 1);
            }
            st.append(" <= ").append(b[j]).append('\n');
        }
        st.append("End");
        return st.toString();
    }
}
