/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.RealVar;

/**
 * A constraint on real variables, solved using IBEX.
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 18/07/12
 */
public class RealConstraint extends Constraint {

	//***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

	/**
	 * Make a new RealConstraint defined as a set of RealPropagators
	 *
	 * @param name        name of the constraint
	 * @param propagators set of propagators defining the constraint
	 */
	public RealConstraint(String name, RealPropagator... propagators) {
		super(name, propagators);
	}

	/**
	 * Make a new RealConstraint to model one or more continuous functions, separated with semi-colon ";"
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
	 * @param name		name of the constraint
	 * @param functions	list of functions, separated by a semi-colon
	 * @param option    propagation option index (Ibex.COMPO is DEFAULT)
	 * @param rvars     a list of real variables
	 */
	public RealConstraint(String name, String functions, int option, RealVar... rvars) {
		this(name, createPropagator(functions, option, rvars));
	}

	/**
	 * Make a new RealConstraint to model one or more continuous functions, separated with semi-colon ";"
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
	 * @param name		name of the constraint
	 * @param functions	list of functions, separated by a semi-colon
	 * @param rvars     a list of real variables
	 */
	public RealConstraint(String name, String functions, RealVar... rvars) {
		this(name, functions, Ibex.COMPO, rvars);
	}

	/**
	 * Make a new RealConstraint to model one or more continuous functions, separated with semi-colon ";"
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
	 * @param functions	list of functions, separated by a semi-colon
	 * @param rvars     a list of real variables
	 */
	public RealConstraint(String functions, RealVar... rvars) {
		this("RealConstraint", functions, rvars);
	}

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    /**
     * Creates a RealPropagator to propagate one or more continuous functions, separated with semi-colon ";"
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
	 * @return a RealPropagator to propagate the given functions over given variable domains
     */
    private static RealPropagator createPropagator(String functions, int option, RealVar... rvars) {
    	return new RealPropagator(functions, rvars, option);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
		if(propagators.length == 0)throw new UnsupportedOperationException("Empty RealConstraint");
		propagators[0].getModel().getIbex().release();
    }
}
