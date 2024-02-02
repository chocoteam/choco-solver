/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;

import static org.chocosolver.lp.LinearProgram.Status.FEASIBLE;

/**
 * An extension of {@link LinearProgram} class that deals with mixed-integer linear program.
 * This class makes possible to declare integer variables and Boolean variables,
 * whose values must be integral in any solution.
 * <br/>
 * <p>
 * There are many ways to declare a MILP, very similar to LinearProgram.
 * But it is possible to indicates, using {@link BitSet}s which variables are integers or Booleans.
 * </p>
 * <p> Next, a call to {@link #branchAndBound()} runs a basic branch-and-bound algorithm and returns the status of the resolution.
 * If the status is {@link Status#FEASIBLE}
 * then the value assigned to each variable is accessible with {@code lp.lp.value(i);}
 * and the value of the objective function with {@code lp.objective();}.
 * </p>
 * <p>Note that calling {@link #simplex()} will solve the relaxed linear program.
 * </p>
 *
 * @author Charles Prud'homme
 * @since 01/03/2023
 */
public class MILP extends LinearProgram {

    // bits set to true indicate integer variables
    private final BitSet integers;
    // bits set to true indicate Boolean variables
    // note that Boolean variables are also integer variables
    private final BitSet booleans;

    /**
     * Create a Mixed-Integer Linear Program instance that takes a mixed-integer linear program in standard form as input.
     *
     * @param matA     is a mxn matrix
     * @param vecB     is an m-vector
     * @param vecC     is n-vector
     * @param integers bitset of integer variables
     * @param booleans bitset of Boolean variables
     * @param trace    set to <i>true</i> to trace the resolution
     */
    public MILP(double[][] matA, double[] vecB, double[] vecC,
                BitSet integers, BitSet booleans,
                boolean trace) {
        super(matA, vecB, vecC, trace);
        this.integers = integers;
        this.booleans = booleans;
        this.integers.or(booleans);
    }

    /**
     * Create a Mixed-Integer Linear Program instance that takes a mixed-integer linear program in standard form as input.
     *
     * @param matA     is a mxn matrix
     * @param vecB     is an m-vector
     * @param vecC     is n-vector
     * @param integers bitset of integer variables
     * @param booleans bitset of Boolean variables
     */
    public MILP(double[][] matA, double[] vecB, double[] vecC,
                BitSet integers, BitSet booleans) {
        this(matA, vecB, vecC, integers, booleans, false);
    }


    /**
     * Initialize a Mixed-Integer Linear Program instance.
     *
     * @param trace set to <i>true</i> to trace the resolution
     */
    public MILP(boolean trace) {
        this(new double[0][0], new double[0], new double[0], new BitSet(), new BitSet(), trace);
    }

    /**
     * Initialize a Mixed-Integer Linear Program instance.
     */
    public MILP() {
        this(false);
    }

    /**
     * Declare a new Boolean variable.
     *
     * @return the index of the variable
     */
    public int makeBoolean() {
        if (m > 0) {
            throw new UnsupportedOperationException("Some constraints are already declared");
        }
        integers.set(this.n);
        booleans.set(this.n);
        return n++;
    }


    /**
     * Declare <i>n</i> new Boolean variables
     */
    public void makeBooleans(int n) {
        if (m > 0) {
            throw new UnsupportedOperationException("Some constraints are already declared");
        }
        integers.set(this.n, this.n + n);
        booleans.set(this.n, this.n + n);
        this.n += n;
    }

    /**
     * Declare a new integer variable.
     * A variable is supposed to be non-negative (&ge; 0).
     *
     * @return the index of the variable
     */
    public int makeInteger() {
        if (m > 0) {
            throw new UnsupportedOperationException("Some constraints are already declared");
        }
        integers.set(this.n);
        return n++;
    }


    /**
     * Declare <i>n</i> new integer variables
     */
    public void makeIntegers(int n) {
        if (m > 0) {
            throw new UnsupportedOperationException("Some constraints are already declared");
        }
        integers.set(this.n, this.n + n);
        this.n += n;
    }


    /**
     * Check that all integer variables (including Boolean variables) take integral values.
     *
     * @return <code>true</code> if the solution is integral, <code>false</code> otherwise.
     */
    private boolean isIntegral() {
        boolean integral = true;
        for (int i = integers.nextSetBit(0); i > -1 && integral; i = integers.nextSetBit(i + 1)) {
            integral = isIntegral(i);
        }
        return integral;
    }

    /**
     * Check that an integer variable take integral value.
     *
     * @param i index of the variable
     * @return <code>true</code> if the variable takes integral value, <code>false</code> otherwise.
     */
    private boolean isIntegral(int i) {
        assert integers.get(i) : "non integer variable";
        return Math.rint(x[i]) == x[i] && (!booleans.get(i) || !(x[i] > 1.));
    }


    /**
     * Drop the last <code>m</code> declared constraints.
     *
     * @param m number of constraints to drop
     */
    private void dropUntil(int m) {
        while (this.m > m) {
            dropLast();
        }
    }

    /**
     * This method solves MILP by branching on integer variables that are not integral and
     * bounding to eliminate sub-problems that cannot contain the optimal solution.
     * <p>If the problem is infeasible, this method terminates.
     * Otherwise, the optimal solution of this mixed integer linear program is computed and values of the variables
     * can be read calling {@link #value(int)}.
     * </p>
     *
     * @return the resolution status
     */
    public Status branchAndBound() {
        return branchAndBound((i, v) -> 1.);
    }

    /**
     * This method solves MILP by branching on integer variables that are not integral and
     * bounding to eliminate sub-problems that cannot contain the optimal solution.
     * <p>If the problem is infeasible, this method terminates.
     * Otherwise, the optimal solution of this mixed integer linear program is computed and values of the variables
     * can be read calling {@link #value(int)}.
     * </p>
     *
     * @return the resolution status
     * @implNote This method assumes that the objective is to be maximized
     */
    public Status branchAndBound(Score score) {
        // 1. add equations to bound Boolean variables
        int lastm = this.m;
        for (int i = booleans.nextSetBit(0); i > -1; i = booleans.nextSetBit(i + 1)) {
            addLeq(i, 1., 1.);
        }
        // 2. check if the Simplex returns an integral solution (or claims that no solution exists)
        Status relaxProb = simplex();
        if (!relaxProb.equals(FEASIBLE)) {
            // 2a. if no solution exists, terminate
            // remove Boolean bounds
            dropUntil(lastm);
            return relaxProb;
        }
        if (isIntegral()) {
            // 2b. if solution is integral, thus optimal, terminate
            // remove Boolean bounds
            dropUntil(lastm);
            return relaxProb;
        }
        System.out.printf("%s\n", Arrays.toString(x));
        // 3. look for integral optimal solution
        double bestObjective = Double.NEGATIVE_INFINITY;
        double[] bestX = null;
        Deque<Branching> branchings = new ArrayDeque<>();
        // 3a. partition the pb in two
        // this is expressed as binary decision
        partition(branchings, score);
        while (!branchings.isEmpty()) {
            Branching branch = branchings.getLast();
            // 3b. deal with backtrack
            switch (branch.getBranch()) {
                case 2:
                    // if the top decision cannot be refuted, then remove it
                    branchings.removeLast();
                    dropLast();
                    continue;
                case 1:
                    // if the top decision can be refuted, then refute it
                    dropLast();
                    break;
                default:
                case 0:
                    // otherwise do nothing
                    break;
            }
            // 3c. restrict the search space
            branch.apply(this);
            if (trace) System.out.println("Branch on :" + branch);
            // 3d. check if the Simplex returns an integral solution
            relaxProb = simplex();
            if (!relaxProb.equals(FEASIBLE)) {
                // if the current search contains no solution, then backtrack
                continue;
            }
            double currentObjectiveValue = objective();
            if (currentObjectiveValue <= bestObjective) {
                // if the current solution is not better, then backtrack
                continue;
            }
            if (isIntegral()) {
                // if the solution is integral (and better), then store it
                bestObjective = currentObjectiveValue;
                bestX = this.x.clone();
                if (trace) System.out.println("Integral better solution found");
                continue;
            }
            // otherwise, partition the sub problem in two
            partition(branchings, score);
        }
        // 4. prepare result
        if (bestObjective > Double.NEGATIVE_INFINITY) {
            // if an integral optimal solution were found, then restore it
            this.status = Status.FEASIBLE;
            System.arraycopy(bestX, 0, this.x, 0, n);
            this.z = bestObjective;
        } else {
            // if no solution were found
            this.status = Status.INFEASIBLE;
        }
        // remove Boolean bounds
        dropUntil(lastm);
        return status;
    }

    /**
     * Partition heuristic, compute a score for all integer variables not integral and select the one with the smallest score
     * to partition the problem.
     * <p>
     * The first branch decreases the upper bound of the selected variable,
     * the second (and last) branch increases the lower bound of the selected variable.
     * </p>
     *
     * @param branchings the branching queue to fill
     * @param score      the scoring function
     */
    private void partition(Deque<Branching> branchings, Score score) {
        double scoring = Double.POSITIVE_INFINITY;
        int idx = -1;
        for (int i = integers.nextSetBit(0); i > -1; i = integers.nextSetBit(i + 1)) {
            if (!isIntegral(i)) {
                double d = score.evaluate(i, value(i));
                if (d < scoring) {
                    scoring = d;
                    idx = i;
                }

            }
        }
        if (idx > -1) {
            int val = (int) value(idx);
            if (booleans.get(idx)) {
                val = 0;
            }
            branchings.addLast(new Branching(idx, val));
        }
    }

    /**
     * Interface to define score for variables
     */
    public interface Score {
        double evaluate(int var, double val);
    }

    /**
     * Class to define branching object.
     * <br/>
     * A Branching object reduces the domain of a variable <i>var</i> with respect to an integer value <i>val</i>.
     * <br/>
     * It has four states, denoted by <i>branch</i>:
     * <ul>
     *     <li>0: the branching is created, but not applied</li>
     *     <li>1: (var &le; val) is added to the MILP</li>
     *     <li>2: (var &ge; val +1) is added to the MILP</li>
     *     <li>3: the branching is unavailable</li>
     * </ul>
     */
    private static class Branching {
        private final int var;
        private final int val;
        private int branch;

        public Branching(int var, int val) {
            this.var = var;
            this.val = val;
            this.branch = 0;
        }

        public int getBranch() {
            return branch;
        }

        void apply(MILP milp) {
            branch++;
            switch (branch) {
                case 1:
                    milp.addLeq(var, 1, val);
                    break;
                case 2:
                    milp.addGeq(var, 1, val + 1);
                    break;
            }
        }

        @Override
        public String toString() {
            String st = "";
            switch (branch) {
                default:
                case 0:
                    st += "init ";
                case 1:
                    st += "x_" + (var + 1) + " <= " + val;
                    break;
                case 3:
                    st += "end ";
                case 2:
                    st += "x_" + (var + 1) + " >= " + (val + 1);
                    break;
            }
            return st;
        }
    }
}
