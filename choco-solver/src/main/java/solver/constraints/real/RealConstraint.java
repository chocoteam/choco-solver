/*
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
package solver.constraints.real;

import solver.Solver;
import solver.constraints.Constraint;
import solver.variables.BoolVar;
import solver.variables.RealVar;

/**
 * A constraint on real variables, solved using IBEX.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/12
 */
public class RealConstraint extends Constraint {

    public final Ibex ibex;
    public int contractors;

    /**
     * Create a constraint on real variables.
     * This is propagated using IBEX.
     *
     * @param solver the solver
     */
    public RealConstraint(Solver solver) {
        super(solver);
        ibex = new Ibex();
        contractors = 0;
    }

    /**
     * add one or more functions, separated with semi-colon ";" to <code>this</code>.
     * <br/>
     * A function is a string declared using the following format:
     * <br/>- the '{i}' tag defines a variable, where 'i' is an explicit index the array of variables <code>vars</code>,
     * <br/>- one or more operators :'+,-,*,/,=,<,>,<=,>=,exp( ),ln( ),max( ),min( ),abs( ),cos( ), sin( ),...'
     * <br/> A complete list is available in the documentation of IBEX.
     * <p/>
     * <p/>
     * <blockquote><pre>
     * RealConstraint rc = new RealConstraint(solver);
     * rc.addFunction("({0}*{1})+sin({0})=1.0;ln({0}+[-0.1,0.1])>=2.6", Ibex.HC4, x,y);
     * </pre>
     * </blockquote>
     *
     * @param functions list of functions, separated by a semi-colon
     * @param option    propagation option index (Ibex.COMPO is DEFAULT)
     * @param rvars     a list of real variables
     */
    public void addFunction(String functions, int option, RealVar... rvars) {
        addPropagators(new RealPropagator(ibex, contractors++, functions, rvars, option));
    }

    /**
     * add one or more functions, separated with semi-colon ";" to <code>this</code>.
     * <br/>
     * A function is a string declared using the following format:
     * <br/>- the '{i}' tag defines a variable, where 'i' is an explicit index the array of variables <code>vars</code>,
     * <br/>- one or more operators :'+,-,*,/,=,<,>,<=,>=,exp( ),ln( ),max( ),min( ),abs( ),cos( ), sin( ),...'
     * <br/> A complete list is available in the documentation of IBEX.
     * <p/>
     * <p/>
     * <blockquote><pre>
     * RealConstraint rc = new RealConstraint(solver);
     * rc.addFunction("({0}*{1})+sin({0})=1.0;ln({0}+[-0.1,0.1])>=2.6", x,y);
     * <p/>
     * </pre>
     * </blockquote>
     *
     * @param functions list of functions, separated by a semi-colon
     * @param rvars     a list of real variables
     */
    public void addFunction(String functions, RealVar... rvars) {
        addPropagators(new RealPropagator(ibex, contractors++, functions, rvars, Ibex.COMPO));
    }

    /**
     * Reified one or more functions, separated with semi-colon ";" to <code>this</code>.
     * <br/>
     * A function is a string declared using the following format:
     * <br/>- the '{i}' tag defines a variable, where 'i' is an explicit index the array of variables <code>vars</code>,
     * <br/>- one or more operators :'+,-,*,/,=,<,>,<=,>=,exp( ),ln( ),max( ),min( ),abs( ),cos( ), sin( ),...'
     * <br/> A complete list is available in the documentation of IBEX.
     * <p/>
     * The reified variable <code>bvar</code> is valid for the entire list of functions (as a conjunction).
     * Its value is TRUE if the list of functions is satisfied, FALSE if the list of functions is unsatisfied.
     * <p/>
     * <blockquote><pre>
     * RealConstraint rc = new RealConstraint(solver);
     * rc.addReifiedFunction(bv, "({0}*{1})+sin({0})=1.0;ln({0}+[-0.1,0.1])>=2.6", Ibex.HC4, x,y);
     * </pre>
     * </blockquote>
     *
     * @param bvar      a boolean variable stating the status of the functions
     * @param functions list of functions, separated by a semi-colon
     * @param option    propagation option index (Ibex.COMPO is DEFAULT)
     * @param rvars     a list of real variables
     */
    public void addReifiedFunction(BoolVar bvar, String functions, int option, RealVar... rvars) {
        addPropagators(new RealReifiedPropagator(ibex, contractors++, functions, bvar, rvars, option));
    }

    /**
     * add a function to <code>this</code>.
     * <br/>
     * A function is a string declared using the following format:
     * <br/>- the '{i}' tag defines a variable, where 'i' is an explicit index the array of variables <code>vars</code>,
     * <br/>- one or more operators :'+,-,*,/,=,<,>,<=,>=,exp( ),ln( ),max( ),min( ),abs( ),cos( ), sin( ),...'
     * <br/> A complete list is available in the documentation of IBEX.
     * <p/>
     * The reified variable <code>bvar</code> is valid for the entire list of functions (as a conjunction).
     * Its value is TRUE if the list of functions is satisfied, FALSE if the list of functions is unsatisfied.
     * <p/>
     * <p/>
     * <blockquote><pre>
     * RealConstraint rc = new RealConstraint(solver);
     * rc.addReifiedFunction(bv, "({0}*{1})+sin({0})=1.0;ln({0}+[-0.1,0.1])>=2.6", x,y);
     * <p/>
     * </pre>
     * </blockquote>
     *
     * @param bvar      a boolean variable stating the status of the functions
     * @param functions list of functions, separated by a semi-colon
     * @param rvars     a list of real variables
     */
    public void addReifiedFunction(String functions, BoolVar bvar, RealVar... rvars) {
        addPropagators(new RealReifiedPropagator(ibex, contractors++, functions, bvar, rvars, Ibex.COMPO));
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        ibex.release();
    }
}
