/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

/******************************************************************************
 *  This file is adapted from:
 *  <a href="https://algs4.cs.princeton.edu/65reductions/TwoPhaseSimplex.java.html">https://algs4.cs.princeton.edu/65reductions/TwoPhaseSimplex.java.html</a>
 * and is here for testing purposes only.
 * 
 *  Compilation:  javac TwoPhaseSimplex.java
 *  Execution:    java TwoPhaseSimplex
 *  Dependencies: System.out.java
 *
 *  Given an m-by-n matrix A, an m-length vector b, and an
 *  n-length vector c, solve the  LP { max cx : Ax <= b, x >= 0 }.
 *  Unlike Simplex.java, this version does not assume b >= 0,
 *  so it needs to find a basic feasible solution in Phase I.
 *
 *  Creates an (m+1)-by-(n+m+1) simplex tableaux with the
 *  RHS in column m+n, the objective function in row m, and
 *  slack variables in columns m through m+n-1.
 *
 ******************************************************************************/

public class TwoPhaseSimplex {
    private static final double EPSILON = 1.0E-8;

    public enum Status {
        UNKNOWN,
        FEASIBLE,
        INFEASIBLE,
        UNBOUNDED,
        IN_PROGRESS
    }

    private final double[][] a;   // tableaux
    // row m   = objective function
    // row m+1 = artificial objective function
    // column n to n+m-1 = slack variables
    // column n+m to n+m+m-1 = artificial variables

    private final int m;          // number of constraints
    private final int n;          // number of original variables

    private final int[] basis;    // basis[i] = basic variable corresponding to row i

    private final boolean trace;

    public TwoPhaseSimplex(LinearProgram lp) {
        this(lp.getA(), lp.getB(), lp.getC(), false);
    }

    public TwoPhaseSimplex(LinearProgram lp, boolean trace) {
        this(lp.getA(), lp.getB(), lp.getC(), trace);
    }

    public TwoPhaseSimplex(double[][] A, double[] b, double[] c) {
        this(A, b, c, false);
    }

    // sets up the simplex tableaux
    public TwoPhaseSimplex(double[][] A, double[] b, double[] c, boolean trace) {
        this.trace = trace;
        m = b.length;
        n = c.length;
        a = new double[m + 2][n + m + m + 1];

        for (int i = 0; i < m; i++)
            System.arraycopy(A[i], 0, a[i], 0, n);

        for (int i = 0; i < m; i++)
            a[i][n + i] = 1.0;

        for (int i = 0; i < m; i++)
            a[i][n + m + m] = b[i];

        System.arraycopy(c, 0, a[m], 0, n);

        // if negative RHS, multiply by -1
        for (int i = 0; i < m; i++) {
            if (b[i] < 0) {
                a[i][n + m + m] = -b[i];
                for (int j = 0; j <= n; j++)
                    a[i][j] = -a[i][j];
                a[i][n + i] = -1.0;
            }
        }


        // artificial variables form initial basis
        for (int i = 0; i < m; i++)
            a[i][n + m + i] = 1.0;
        for (int i = 0; i < m; i++)
            a[m + 1][n + m + i] = -1.0;
        for (int i = 0; i < m; i++)
            pivot(i, n + m + i);

        basis = new int[m];
        for (int i = 0; i < m; i++)
            basis[i] = n + m + i;


    }

    public Status solve() {
        show();
        Status status = phase1();
        if (status.equals(Status.FEASIBLE)) {
            status = phase2();
        }
//        assert check(A, b, c);
        show();
        return status;
    }

    // run phase I simplex algorithm to find basic feasible solution
    private Status phase1() {
        while (true) {

            // find entering column q
            int q = bland1();
            if (q == -1) break;  // optimal

            // find leaving row p
            int p = minRatioRule(q);
            assert p != -1 : "Entering column = " + q;

            // pivot
            pivot(p, q);
            // update basis
            basis[p] = q;
            show();
        }
        if (a[m + 1][n + m + m] > EPSILON) {
            return Status.INFEASIBLE;
        }
        return Status.FEASIBLE;
    }


    // run simplex algorithm starting from initial basic feasible solution
    private Status phase2() {
        while (true) {

            // find entering column q
            int q = bland2();
            if (q == -1) break;  // optimal

            // find leaving row p
            int p = minRatioRule(q);
            if (p == -1) {
                return Status.UNBOUNDED;
            }

            // pivot
            pivot(p, q);

            // update basis
            basis[p] = q;
            show();
        }
        return Status.FEASIBLE;
    }

    // lowest index of a non-basic column with a positive cost - using artificial objective function
    private int bland1() {
        for (int j = 0; j < n + m; j++)
            if (a[m + 1][j] > EPSILON) return j;
        return -1;  // optimal
    }

    // lowest index of a non-basic column with a positive cost
    private int bland2() {
        for (int j = 0; j < n + m; j++)
            if (a[m][j] > EPSILON) return j;
        return -1;  // optimal
    }


    // find row p using min ratio rule (-1 if no such row)
    private int minRatioRule(int q) {
        int p = -1;
        for (int i = 0; i < m; i++) {
            // if (a[i][q] <= 0) continue;
            if (a[i][q] <= EPSILON) continue;
            else if (p == -1) p = i;
            else if ((a[i][n + m + m] / a[i][q]) < (a[p][n + m + m] / a[p][q])) p = i;
        }
        return p;
    }

    // pivot on entry (p, q) using Gauss-Jordan elimination
    private void pivot(int p, int q) {
//        System.out.printf("2PS[pivot] l: x%d, e: x%d, %.4f\n", p, q, value());
        // everything but row p and column q
        for (int i = 0; i <= m + 1; i++)
            for (int j = 0; j <= n + m + m; j++)
                if (i != p && j != q) a[i][j] -= a[p][j] * (a[i][q] / a[p][q]);

        // zero out column q
        for (int i = 0; i <= m + 1; i++)
            if (i != p) a[i][q] = 0.0;

        // scale row p
        for (int j = 0; j <= n + m + m; j++)
            if (j != q) a[p][j] /= a[p][q];
        a[p][q] = 1.0;
    }

    // return optimal objective value
    public double value() {
        return -a[m][n + m + m];
    }

    // return primal solution vector
    public double[] primal() {
        double[] x = new double[n];
        for (int i = 0; i < m; i++)
            if (basis[i] < n) x[basis[i]] = a[i][n + m + m];
        return x;
    }

    // return dual solution vector
    public double[] dual() {
        double[] y = new double[m];
        for (int i = 0; i < m; i++) {
            y[i] = -a[m][n + i];
            if (y[i] == -0.0) y[i] = 0.0;
        }
        return y;
    }


    // is the solution primal feasible?
    private boolean isPrimalFeasible(double[][] A, double[] b) {
        double[] x = primal();

        // check that x >= 0
        for (int j = 0; j < x.length; j++) {
            if (x[j] < -EPSILON) {
                System.out.println("x[" + j + "] = " + x[j] + " is negative");
                return false;
            }
        }

        // check that Ax <= b
        for (int i = 0; i < m; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                sum += A[i][j] * x[j];
            }
            if (sum > b[i] + EPSILON) {
                System.out.println("not primal feasible");
                System.out.println("b[" + i + "] = " + b[i] + ", A_i x = " + sum);
                return false;
            }
        }
        return true;
    }

    // is the solution dual feasible?
    private boolean isDualFeasible(double[][] A, double[] c) {
        double[] y = dual();

        // check that y >= 0
        for (int i = 0; i < y.length; i++) {
            if (y[i] < -EPSILON) {
                System.out.println("y[" + i + "] = " + y[i] + " is negative");
                return false;
            }
        }

        // check that yA >= c
        for (int j = 0; j < n; j++) {
            double sum = 0.0;
            for (int i = 0; i < m; i++) {
                sum += A[i][j] * y[i];
            }
            if (sum < c[j] - EPSILON) {
                System.out.println("not dual feasible");
                System.out.println("c[" + j + "] = " + c[j] + ", y A_j = " + sum);
                return false;
            }
        }
        return true;
    }

    // check that optimal value = cx = yb
    private boolean isOptimal(double[] b, double[] c) {
        double[] x = primal();
        double[] y = dual();
        double value = value();

        // check that value = cx = yb
        double value1 = 0.0;
        for (int j = 0; j < x.length; j++)
            value1 += c[j] * x[j];
        double value2 = 0.0;
        for (int i = 0; i < y.length; i++)
            value2 += y[i] * b[i];
        if (Math.abs(value - value1) > EPSILON || Math.abs(value - value2) > EPSILON) {
            System.out.println("value = " + value + ", cx = " + value1 + ", yb = " + value2);
            return false;
        }

        return true;
    }

    private boolean check(double[][] A, double[] b, double[] c) {
        return isPrimalFeasible(A, b) && isDualFeasible(A, c) && isOptimal(b, c);
    }

    // print tableaux
    public void show() {
        if (!trace) return;
        System.out.println("m = " + m);
        System.out.println("n = " + n);
        for (int i = 0; i <= m /*+ 1*/; i++) {
            for (int j = 0; j < n + m/* + m*/; j++) {
                System.out.printf("%7.2f ", a[i][j]);
                if (j == n + m - 1 || j == n + m + m - 1) System.out.print(" |");
            }
            System.out.printf("%7.2f ", a[i][n + m + m]);
            System.out.println();
        }
        System.out.print("basis = ");
        for (int i = 0; i < m; i++)
            System.out.print(basis[i] + " ");
        System.out.println();
        System.out.println();
    }

}
