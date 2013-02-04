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

    /* Possible contraction strategies. */
    public static final int COMPO = 0;
    public static final int HC4 = 1;
    public static final int HC4_NEWTON = 2;

    /* Constants for the status of a contraction. */
    public static final int FAIL = 0;
    public static final int ENTAILED = 1;
    public static final int CONTRACT = 2;
    public static final int NOTHING = 3;

    /* Constants for describing a boolean domain (by an integer). */
    public static final int FALSE = 0;
    public static final int TRUE = 1;
    public static final int FALSE_OR_TRUE = 2;

    static {
        System.loadLibrary("ibex-java");
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
     * @return The status of contraction or fail/entailment test. Note that the name of the constant
     *         in return refers to the constraint c, not R. Hence "FAIL" means that no tuple satisfies c (should
     *         R be satisfiable or not).
     *         <p/>
     *         FAIL     - No tuple satisfies c. If reif==FALSE, the bounds of x may have been impacted (the part
     *         of the domain inside c has been removed and the remaining part has been proven to be
     *         outside c). If reif==TRUE, the bounds have not been impacted but we have to consider
     *         that the domain has been reduced to the empty set. If reif==FALSE_OR_TRUE, bounds have
     *         not been impacted.
     *         <p/>
     *         ENTAILED - All the tuples satisfy the constraint. If reif==FALSE, the bounds have not been impacted
     *         but we have to consider that the domain has been reduced to the empty set. If reif==TRUE,
     *         the bounds of x may have been impacted (the part of the domain outside c has been removed
     *         and the remaining part has been proven to be inside c). If reif==FALSE_OR_TRUE, bounds have
     *         not been impacted.
     *         <p/>
     *         CONTRACT - This value can only be returned if reif==FALSE or reif==TRUE. At least one bound of x has
     *         been reduced by more than 0.1%. If reif==FALSE, the removed part of the domain is inside c.
     *         If reif==TRUE, the removed part is outside.
     *         <p/>
     *         NOTHING  - No bound has been reduced and nothing could be proven.
     */
    public native int contract(int i, double bounds[], int reif);


    /**
     * Same as contract(int, double bounds[], int reif) with reif=TRUE.
     */
    public native int contract(int i, double bounds[]);


    /**
     * Free IBEX structures from memory
     */
    public native void release();
}
