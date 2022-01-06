/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.real.PropMixed;
import org.chocosolver.solver.constraints.real.PropMixedElement;
import org.chocosolver.solver.constraints.real.PropScalarMixed;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;

/**
 * Interface to make constraints over RealVar
 *
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES
 * @since 4.0.0
 */
public interface IRealConstraintFactory extends ISelf<Model> {

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
	 * @param contractionRatio defines the domain contraction significance
	 * @param rvars     a list of real variables
	 */
	default RealConstraint realIbexGenericConstraint(String functions, double contractionRatio, Variable... rvars) {
		return new RealConstraint(functions, contractionRatio, rvars);
	}

	default RealConstraint realIbexGenericConstraint(String functions, Variable... rvars) {
		return realIbexGenericConstraint(functions, ref().getSettings().getIbexContractionRatio(), rvars);
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

	/**
	 * An arithmetical constraint : x = Y
	 * @param x a real variable
	 * @param y an integer variable
	 * @return an arithmetical constraint
	 */
	default Constraint eq(RealVar x, IntVar y){
		return new Constraint(ConstraintsName.ARITHM,
				new PropMixed(x, y));
	}

	/**
	  * Let 'index' be an integer variable with 'n' values
	 * and 'value' be a real variable. Given 'n' constant values a1 to an,
	  * this constraint ensures that:
	  * <p/>
	  * <code>x = i iff v = ai</code>
	  * <p/>
	  * a1... an sequence is supposed to be ordered (a1&lt;a2&lt;... an)
	  * <br/>
	 * @param value a real variable
	 * @param table a sequence of double values
	 * @param index an integer variable
	 * @return an element constraint
	 */
	default Constraint element(RealVar value, double[] table, IntVar index){
		for(int i = 0; i < table.length-2; i++){
			if(table[i]>=table[i+1]){
				throw new SolverException("element requires 'table' to be strictly ordered.");
			}
		}
		return new Constraint(ConstraintsName.ELEMENT,
				new PropMixedElement(value, index, table));
	}
}
