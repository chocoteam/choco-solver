/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.constraints.real.PropScalarMixed;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.Variable;

/**
 * Interface to make constraints over RealVar
 *
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES
 * @since 4.0.0
 */
public interface IRealConstraintFactory {

	/**
	 * Creates a RealConstraint to model one or more continuous functions, separated with semi-colon ";"
	 * <br/>
	 * A function is a string declared using the following format:
	 * <br/>- the '{i}' tag defines a variable, where 'i' is an explicit index the array of variables <code>vars</code>,
	 * <br/>- one or more operators :'+,-,*,/,=,<,>,<=,>=,exp( ),ln( ),max( ),min( ),abs( ),cos( ), sin( ),...'
	 * <br/> A complete list is available in the documentation of IBEX.
	 * <p/>
	 *
	 * Example to express the system:
	 * <br/>x*y + sin(x) = 1;
	 * <br/>ln(x)+[-0.1,0.1] >=2.6;
	 * <br/>
	 * <br/>realIbexGenericConstraint("({0}*{1})+sin({0})=1.0;ln({0}+[-0.1,0.1])>=2.6", x,y);
	 *
	 * @param functions	list of functions, separated by a semi-colon
	 * @param rvars     a list of real variables
	 */
	default RealConstraint realIbexGenericConstraint(String functions, Variable... rvars) {
		return new RealConstraint(functions, rvars);
	}

	/**
	 * Creates a RealConstraint to model one or more continuous functions, separated with semi-colon ";"
	 * <br/>
	 * A function is a string declared using the following format:
	 * <br/>- the '{i}' tag defines a variable, where 'i' is an explicit index the array of variables <code>vars</code>,
	 * <br/>- one or more operators :'+,-,*,/,=,<,>,<=,>=,exp( ),ln( ),max( ),min( ),abs( ),cos( ), sin( ),...'
	 * <br/> A complete list is available in the documentation of IBEX.
	 * <p/>
	 *
	 * Example to express the system:
	 * <br/>x*y + sin(x) = 1;
	 * <br/>ln(x)+[-0.1,0.1] >=2.6;
	 * <br/>
	 * <br/>realIbexGenericConstraint("({0}*{1})+sin({0})=1.0;ln({0}+[-0.1,0.1])>=2.6", x,y);
	 *
	 * @param functions	list of functions, separated by a semi-colon
	 * @param rvars     a list of real variables
	 */
	default RealConstraint ibex(String functions, Variable... rvars) {
		return realIbexGenericConstraint(functions, rvars);
	}

	/**
	 * Creates a linear equation constraint over RealVar, IntVar or BoolVar
     * which ensures that Sum(vars[i]*coeffs[i]) op bound
	 *
	 * @param vars     a collection of variable (RealVar, IntVar, BoolVar are accepted)
	 * @param coeffs   a collection of double, for which |vars|=|coeffs|
	 * @param op an operator in {"=", ">=", "<="}
	 * @param bound   a double
	 * @return a scalar constraint
	 */
	default Constraint scalar(Variable[] vars, double[] coeffs, String op, double bound){
		return new Constraint(ConstraintsName.MIXEDSCALAR,
				new PropScalarMixed(vars, coeffs, Operator.get(op), bound));
	}
}
