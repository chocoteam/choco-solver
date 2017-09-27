/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.RealVar;

import static org.chocosolver.solver.constraints.ConstraintsName.REALCONSTRAINT;

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
	private RealConstraint(String name, RealPropagator... propagators) {
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
	 * @param functions	list of functions, separated by a semi-colon
	 * @param option    propagation option index (Ibex.COMPO is DEFAULT)
	 * @param rvars     a list of real variables
	 */
	public RealConstraint(String functions, int option, RealVar... rvars) {
		this(REALCONSTRAINT, createPropagator(functions, option, rvars));
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
	 * @deprecated see {@link #RealConstraint(String, int, RealVar...)} instead
	 */
	@Deprecated
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
	 * @deprecated see {@link #RealConstraint(String, RealVar...)} instead
	 */
	@Deprecated
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
		this(REALCONSTRAINT, functions, Ibex.COMPO, rvars);
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
}
