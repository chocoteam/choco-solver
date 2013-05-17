package solver.constraints.real;/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import solver.exception.SolverException;

/**
 * A link to Ibex library.
 * The following option should be passed to the VM:
 * <pre>
 *     -Djava.library.path=/path/to/ibex/dynlib
 * </pre>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/07/12
 */
public class Ibex {

    /* A contraction is considered as
     * significant when at least 1% of a
     * domain has been reduced */
    public static final double RATIO = 0.01;

    /* Possible contraction strategies. */
    public static final int COMPO = 0;
    public static final int HC4 = 1;
    public static final int HC4_NEWTON = 2;

    /* Constants for the status of a contraction. */
    public static final int FAIL = 0;
    public static final int ENTAILED = 1;
    public static final int CONTRACT = 2;
    public static final int NOTHING = 3;

    /* Constants for the status of a inflation. */
    public static final int NOT_SIGNIFICANT = 4;
    public static final int INFLATE = 5;
    public static final int FULL_INFLATE = 6;
    public static final int BAD_POINT = 7;
    public static final int UNKNOWN_POINT = 8;

    /* Constants for describing a boolean domain (by an integer). */
    public static final int FALSE = 0;
    public static final int TRUE = 1;
    public static final int FALSE_OR_TRUE = 2;

    static {
        try{
            System.loadLibrary("ibex-java");
        }catch (UnsatisfiedLinkError e){
            throw new SolverException("Ibex is not correctly installed (see http://www.emn.fr/z-info/ibex/).");
        }
    }

    /**
     * Create a new IBEX constraint with a default contractor.
     * <p/>
     * The default contractor is COMPO.
     * <p/>
     * Example: add_ctr(2,{0}={1}) will add the constraint x=y.
     *
     * @param nb_var - Number of variables.
     * @param syntax - The constraint
     */
    public native void add_ctr(int nb_var, String syntax);

    /**
     * Same as add_ctr except that a specific contractor is used.
     *
     * @param nb_var - Number of variables
     * @param syntax - The constraint
     * @param option - A value between COMPO, HC4 or HC4_NEWTON.
     */
    public native void add_ctr(int nb_var, String syntax, int option);

    /**
     * Constraint nb_var variables to be integer variables.
     *
     * @param nb_var - Number of variables
     *               /////@param mask   - Set whether a variable is integral or not.
     *               /////                mask[i]==true <=> the ith variable is integral.
     */
    public native void add_int_ctr(int nb_var);


    /**
     * Call the contractor associated to a constraint or its negation.
     * <p/>
     * We consider here the reified constraint R(b,c) : b<=>c(x_1,...,x_n).
     *
     * @param i      - Number of the constraint (in the order of creation)
     * @param bounds - The bounds of domains under the following form:
     *               (x1-,x1+,x2-,x2+,...,xn-,xn+), where xi- (resp. xi+) is the
     *               lower (resp. upper) bound of the domain of x_i.
     * @param reif   - Domain of the reification variable b with the following accepted values:
     *               FALSE, TRUE, FALSE_OR_TRUE.
     * @return The status of contraction or fail/entailment test. Note that the name of the
     *         constant in return refers to the constraint c, not R. Hence "FAIL" means that
     *         no tuple satisfies c (should  R be satisfiable or not).
     *         <p/>
     *         FAIL            - No tuple satisfies c. If reif==FALSE, the bounds of x may have been
     *         impacted (the part of the domain inside c has been removed and the
     *         remaining part has been proven to be outside c). If reif==TRUE, the
     *         bounds have not been impacted but we have to consider that the domain
     *         has been reduced to the empty set. If reif==FALSE_OR_TRUE, bounds have
     *         not been impacted.
     *         <p/>
     *         ENTAILED        - All the tuples satisfy the constraint. If reif==FALSE, the bounds have
     *         not been impacted but we have to consider that the domain has been
     *         reduced to the empty set. If reif==TRUE, the bounds of x may have been
     *         impacted (the part of the domain outside c has been removed and the
     *         remaining part has been proven to be inside c). If reif==FALSE_OR_TRUE,
     *         bounds have not been impacted.
     *         <p/>
     *         CONTRACT        - This value can only be returned if reif==FALSE or reif==TRUE. At least
     *         one bound of x has been reduced by more than RATIO. If reif==FALSE, the
     *         removed part of the domain is inside c. If reif==TRUE, the removed part
     *         is outside.
     *         <p/>
     *         NOTHING         - No bound has been reduced and nothing could be proven.
     */
    public native int contract(int i, double bounds[], int reif);


    /**
     * Inflate a point to a box with respect to a constraint or its negation.
     * <p/>
     * Given a constraint "c", we say that a point is "inside" (resp. "outside") if it satisfies
     * (resp. does not satisfy) c. A box is said to be "inside"/"outside" if all its points are
     * inside/outside c.
     * <p/>
     * This method takes an initial point "p" and an enclosing box "x". It tries to inflate p
     * inside x with respect to a constraint "c" or its negation. That is, it builds a box "y",
     * containing p and contained in "x":
     * <br/>
     * p &isin; y &sube; x
     * <br/>
     * If in==TRUE, y must be inside c. Otherwise, it must be outside.
     *
     * @param i      - Number of the constraint c (in the order of creation)
     * @param p      - The coordinates of the point to inflate: (p1,...pn)
     * @param bounds - The bounds of the enclosing box x under the following form:
     *               (x1-,x1+,x2-,x2+,...,xn-,xn+), where xi- (resp. xi+) is the
     *               lower (resp. upper) bound of the domain of x_i.
     * @param in     - TRUE if the box has to be inflated inside c (-> inner region),
     *               FALSE if it has to be inflated outside c (-> forbidden region).
     * @return The status of inflation. If in==TRUE (resp. FALSE):
     *         <p/>
     *         NOT_SIGNIFICANT - The point p has been proven to be inside (resp. outside). However, it
     *         could not be inflated to a "significant" box y. A box y is considered to
     *         be significant if, on <b>each</b> of its dimension, the width of the
     *         interval y_i is at least RATIO times the width of x_i.
     *         <p/>
     *         INFLATE         - The point p has been inflated to a significant box y that is inside
     *         (reps. outside) the constraint.
     *         <p/>
     *         FULL_INFLATE    - The whole box x has been proven to be inside (resp. outside).
     *         <p/>
     *         BAD_POINT       - No inflation was possible because p has been proven to be outside (resp.
     *         inside).
     *         <p/>
     *         UNKWOWN_POINT   - No inflation at all could be done and it could even not be decided
     *         whether p is inside or outside the constraint.
     */
    public native int inflate(int i, double p[], double bounds[], boolean in);


    /**
     * Same as contract(int, double bounds[], int reif) with reif=TRUE.
     */
    public native int contract(int i, double bounds[]);


    /**
     * Free IBEX structures from memory
     */
    public native void release();
}

