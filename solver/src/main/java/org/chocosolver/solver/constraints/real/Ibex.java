/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

//============================================================================
//                                  I B E X
// File        : Ibex.java.in
// Author      : Gilles Chabert
// Copyright   : IMT Atlantique (France)
// License     : See the LICENSE file
// Created     : Jul 18, 2012
// Last Update : Nov 02, 2017
//============================================================================
public class Ibex {

    /* A contraction is considered as
     * significant when at least 1% of a
     * domain has been reduced */
    public static final double RATIO      = 0.01;

    /* Default value for preserve rounding of java
     * when switching back from ibex */
    public static final boolean PRESERVE_ROUNDING = false;

    /* Constants for the status of a contraction. */
    public static final int FAIL             = 0;
    public static final int ENTAILED         = 1;
    public static final int CONTRACT         = 2;
    public static final int NOTHING          = 3;

    /* Constants for the status of an inflation. */
    public static final int NOT_SIGNIFICANT  = 4;
    public static final int INFLATE          = 5;
    public static final int FULL_INFLATE     = 6;
    public static final int BAD_POINT        = 7;
    public static final int UNKNOWN_POINT    = 8;

    /* Return values of start_solve. */
    public static final int STARTED  = 0;
    public static final int DISCRETE_NOT_INSTANCIATED = 1;

    /* Return values of next_solution. */
    public static final int UNKNOWN         = 0;
    public static final int SOLUTION        = 1;
    public static final int SEARCH_OVER     = 2;
    public static final int NOT_STARTED     = 3;

    /* Constants for describing a boolean domain (by an integer). */
    public static final int FALSE         = 0;
    public static final int TRUE          = 1;
    public static final int FALSE_OR_TRUE = 2;

    /* Other error codes */
    public static final int BAD_DOMAIN    = -2;
    public static final int NOT_BUILT     = -3;


    static {
        System.loadLibrary("ibex-java");
    }

    /**
     * Create a new Ibex object.
     *
     * An IBEX (and only one) object has to be created for each different CSP.
     * The IBEX object gathers all the constraints.
     *
     * @param prec              - An array of n double (where n is the total number of variables of the CSP).
     *                            Each double indicates whether a variable is integral or not, and in the
     *                            case of a real variable, the precision required. More precisely:
     *                                prec[i]==-1 => the ith variable is integral.
     *                                prec[i]>=0  => the ith variable is real and the precision is prec[i].
     *
     * @param preserve_rounding - Under Linux/MacOS, Ibex (if linked with Gaol) does not use the standard
     *                            rounding mode of the FPU (round-to-nearest) but the upward rounding mode, in order
     *                            to get better performances. If preserve_rounding is true, Ibex will activate the
     *                            upward rounding mode at each function call (contract, etc.) and restore the default
     *                            rounding mode in return, which is transparent from Java but which also mean a loss
     *                            of efficiency. If set to false, the rounding mode is only activated once by the
     *                            construtor. In this case, the rounding mode is also changed on Java side.
     */
    public Ibex(double[] prec, boolean preserve_rounding) {
        init(prec, preserve_rounding);
    }

    /**
     * Same as previous constructor with preserve_rounding=false.
     * For backward compatibility.
     */
    public Ibex(double[] prec) {
        init(prec, PRESERVE_ROUNDING);
    }

    /**
     * Add a new constraint.
     *
     * Important: The "build()" method has to be called once
     *            all constraints are added.
     *
     * Example: add_ctr({0}={1}) will add the constraint x=y.
     *
     * @param syntax - The constraint
     *
     * @return
     *
     *   true     - OK (success)
     *
     *   false    - error: build() has already been called.
     */
    public native boolean add_ctr(String syntax);

    /**
     * Build the object (with all constraints added via add_ctr(...))
     *
     * @return
     *
     *   true     - OK (success)
     *
     *   false    - error: one constraint has not been parsed successfully.
     */
    public native boolean build();

    /**
     * Call the contractor associated to a constraint or its negation.
     *
     * We consider here the reified constraint R(b,c) : b<=>c(x_1,...,x_n).
     *
     * @param i       - Number of the constraint (in the order of creation)
     * @param bounds  - The bounds of domains under the following form:
     *                  (x1-,x1+,x2-,x2+,...,xn-,xn+), where xi- (resp. xi+) is the
     *                  lower (resp. upper) bound of the domain of x_i.
     * @param reif    - Domain of the reification variable b with the following accepted values:
     *                  FALSE, TRUE, FALSE_OR_TRUE.
     * @param rel_eps - Threshold under which a contraction is considered as unsufficient and
     *                  therefore ignored.
     *                  If we denote by w_i the width of the initial domain of the ith variable
     *                  and by w_i' the width of the contracted domain, all contractions
     *                  will be discarded (and the return status will be NOTHING) if for all i,
     *                  (w_i - w_i') < rel_eps * w_i.
     *
     * @return        The status of contraction or fail/entailment test. Note that the name of the
     *                constant in return refers to the constraint c, not R. Hence "FAIL" means that
     *                no tuple satisfies c (should  R be satisfiable or not).
     *
     *   FAIL            - No tuple satisfies c. If reif==FALSE, the bounds of x may have been
     *                     impacted (the part of the domain inside c has been removed and the
     *                     remaining part has been proven to be outside c). If reif==TRUE, the
     *                     bounds have not been impacted but we have to consider that the domain
     *                     has been reduced to the empty set. If reif==FALSE_OR_TRUE, bounds have
     *                     not been impacted.
     *
     *   ENTAILED        - All the tuples satisfy the constraint. If reif==FALSE, the bounds have
     *                     not been impacted but we have to consider that the domain has been
     *                     reduced to the empty set. If reif==TRUE, the bounds of x may have been
     *                     impacted (the part of the domain outside c has been removed and the
     *                     remaining part has been proven to be inside c). If reif==FALSE_OR_TRUE,
     *                     bounds have not been impacted.
     *
     *   CONTRACT        - This value can only be returned if reif==FALSE or reif==TRUE. At least
     *                     one bound of x has been reduced by more than RATIO. If reif==FALSE, the
     *                     removed part of the domain is inside c. If reif==TRUE, the removed part
     *                     is outside.
     *
     *   NOTHING         - No bound has been significantly reduced and nothing could be proven.
     *
     *   BAD_DOMAIN      - The domain has not the expected number of dimensions.
     *
     *   NOT_BUILT       - Object not built (build() must be called before)
     */
    public native int contract(int i, double[] bounds, int reif, double rel_eps);

    /**
     * Same as contract(int, double bounds[], int reif) with reif=TRUE.
     */
    public native int contract(int i, double[] bounds, double ratio);

    /**
     * Same as contract(int, double bounds[], int reif, double rel_eps) with rel_eps=RATIO.
     */
    public native int contract(int i, double[] bounds, int reify);

    /**
     * Same as contract(int, double bounds[], int reif, double rel_eps) with reif=TRUE and rel_eps=RATIO.
     */
    public native int contract(int i, double[] bounds);

    /**
     * Inflate a point to a box with respect to a constraint or its negation.
     *
     * Given a constraint "c", we say that a point is "inside" (resp. "outside") if it satisfies
     * (resp. does not satisfy) c. A box is said to be "inside"/"outside" if all its points are
     * inside/outside c.
     *
     * This method takes an initial point "p" and an enclosing box "x". It tries to inflate p
     * inside x with respect to a constraint "c" or its negation. That is, it builds a box "y",
     * containing p and contained in "x":
     * <br/>
     *                       p &isin; y &sube; x
     * <br/>
     * If in==TRUE, y must be inside c. Otherwise, it must be outside.
     *
     * @param i      - Number of the constraint c (in the order of creation)
     *
     * @param p      - The coordinates of the point to inflate: (p1,...pn)
     *
     * @param bounds - The bounds of the enclosing box x under the following form:
     *                 (x1-,x1+,x2-,x2+,...,xn-,xn+), where xi- (resp. xi+) is the
     *                 lower (resp. upper) bound of the domain of x_i.
     *
     * @param in     - TRUE if the box has to be inflated inside c (-> inner region),
     *                 FALSE if it has to be inflated outside c (-> forbidden region).
     *
     * @return The status of inflation. If in==TRUE (resp. FALSE):
     *
     *   NOT_SIGNIFICANT - The point p has been proven to be inside (resp. outside). However, it
     *                     could not be inflated to a "significant" box y. A box y is considered to
     *                     be significant if, on <b>each</b> of its dimension, the width of the
     *                     interval y_i is at least RATIO times the width of x_i.
     *
     *   INFLATE         - The point p has been inflated to a significant box y that is inside
     *                     (reps. outside) the constraint.
     *
     *   FULL_INFLATE    - The whole box x has been proven to be inside (resp. outside).
     *
     *   BAD_POINT       - No inflation was possible because p has been proven to be outside (resp.
     *                     inside).
     *
     *   UNKWOWN_POINT   - No inflation at all could be done and it could even not be decided
     *                     whether p is inside or outside the constraint.
     *
     *   BAD_DOMAIN      - The domain has not the expected number of dimensions.
     *
     *   NOT_BUILT       - Object not built (build() must be called before)
     */
    public native int inflate(int i, double[] p, double[] bounds, boolean in);

    /**
     * Let IBEX terminates the solving process for the CSP, once all the integer
     * variables have been instanciated.
     *
     * This function initializes the solving process. Each solution is then retrieved
     * in turn via a call to next_solution(...).
     *
     * @param bounds                - the domain in which all solutions will be searched
     *                               (include all variables, real and integer ones).
     *
     * @return
     *
     *   SUCCESS                    - OK
     *
     *   DISCRETE_NOT_INSTANCIATED  - One discrete variable is not instanciated
     *
     *   BAD_DOMAIN                 - The domain has not the expected number of dimensions.
     *
     *   NOT_BUILT                  - Object not built (build() must be called before))
     */
    public native int start_solve(double[] bounds);

    /**
     * Look up for the next solution.
     *
     * The first call to solution(...) in a given solving process must be preceded by a
     * call to start_solve(...).
     *
     *   domains     - (output argument): array in which the solution will
     *                 be stored (if any)
     * @return
     *
     *   SOLUTION    - A certified solution has been found
     *
     *   UNKNOWN     - An uncertified solution has been found
     *
     *   SEARCH_OVER - No more solution
     *
     *   BAD_DOMAIN  - The domain has not the expected number of dimensions.
     *
     *   NOT_BUILT   - Object not built (build() must be called before))
     */
    public native int next_solution(double[] sol);

    /**
     * Free IBEX structures from memory
     */
    public native void release();

    /**
     * Initialize IBEX variables.
     *
     * This method is automatically called by the constructor.
     */
    private native void init(double[] prec, boolean preserve_rounding);

    // Internal: do not modify!
    // This is a pointer to native c++ data
    private long data;
}
