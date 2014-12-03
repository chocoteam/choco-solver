/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * @author Jean-Guillaume Fages
 * @since 15/05/2013
 */
public class LogicalConstraintFactory {

	//***********************************************************************************
	// simple logical constraints
	//***********************************************************************************

	/**
	 * Make a and constraint
	 * @param BOOLS an array of boolean variable
	 * @return a constraint and ensuring that variables in BOOLS are all set to true
	 */
	public static Constraint and(BoolVar... BOOLS){
		Solver s = BOOLS[0].getSolver();
		IntVar sum = VariableFactory.bounded(StringUtils.randomName(),0,BOOLS.length,s);
		s.post(IntConstraintFactory.sum(BOOLS,sum));
		return IntConstraintFactory.arithm(sum,"=",BOOLS.length);
	}

	/**
	 * Make an or constraint
	 * @param BOOLS an array of boolean variable
	 * @return a constraint or ensuring that at least one variables in BOOLS is set to true
	 */
	public static Constraint or(BoolVar... BOOLS){
		Solver s = BOOLS[0].getSolver();
		IntVar sum = VariableFactory.bounded(StringUtils.randomName(),0,BOOLS.length,s);
		s.post(IntConstraintFactory.sum(BOOLS,sum));
		return IntConstraintFactory.arithm(sum,">=",1);
	}

	/**
	 * Make a and constraint
	 * @param CONS an array of constraints
	 * @return a constraint and ensuring that constraints in CONS are all satisfied
	 */
	public static Constraint and(Constraint... CONS){
		BoolVar[] bools = new BoolVar[CONS.length];
		for(int i=0;i<CONS.length;i++){
			bools[i] = CONS[i].reif();
		}
		return and(bools);
	}

	/**
	 * Make an or constraint
	 * @param CONS an array of constraints
	 * @return a constraint or ensuring that at least one constraint in CONS is satisfied
	 */
	public static Constraint or(Constraint... CONS){
		BoolVar[] bools = new BoolVar[CONS.length];
		for(int i=0;i<CONS.length;i++){
			bools[i] = CONS[i].reif();
		}
		return or(bools);
	}

	/**
	 * Get the opposite of a constraint
	 * Works for any constraint, but the associated performances might be weak
	 * @param CONS a constraint
	 * @return the opposite constraint of CONS
	 */
	public static Constraint not(Constraint CONS){
		return CONS.getOpposite();
	}

	//***********************************************************************************
	// Non-reifiable reification constraints
	//***********************************************************************************

	/**
	 * Post a constraint ensuring that if IF is satisfied, then THEN is satisfied as well
	 * Otherwise, ELSE must be satisfied
	 *
	 * BEWARE : it is automatically posted (it cannot be reified)
	 *
	 * @param IF a constraint
	 * @param THEN a constraint
	 * @param ELSE a constraint
	 */
	public static void ifThenElse(Constraint IF, Constraint THEN, Constraint ELSE){
		ifThenElse(IF.reif(), THEN, ELSE);
	}

	/**
	 * Implication constraint: BVAR => THEN && not(B) => ELSE.
	 * <br/>
	 * Ensures:
	 * <p/>- BVAR = 1 =>  THEN is satisfied, <br/>
	 * <p/>- BVAR = 0 =>  ELSE is satisfied, <br/>
	 * <p/>- THEN is not satisfied => BVAR = 0 <br/>
	 * <p/>- ELSE is not satisfied => BVAR = 1 <br/>
	 * <p/>
	 * <p/> In order to have BVAR <=> THEN, use reification
	 *
	 * BEWARE : it is automatically posted (it cannot be reified)
	 *
	 * @param BVAR     variable of reification
	 * @param THEN     the constraint to be satisfied when BVAR = 1
	 * @param ELSE     the constraint to be satisfied when BVAR = 0
	 */
	public static void ifThenElse(BoolVar BVAR, Constraint THEN, Constraint ELSE) {
		ifThen(BVAR,THEN);
		ifThen(BVAR.not(),ELSE);
	}

	/**
	 * Post a constraint ensuring that if IF is satisfied, then THEN is satisfied as well
	 *
	 * BEWARE : it is automatically posted (it cannot be reified)
	 *
	 * @param IF a constraint
	 * @param THEN a constraint
	 */
	public static void ifThen(Constraint IF, Constraint THEN) {
		ifThen(IF.reif(), THEN);
	}

	/**
	 * Imply a constraint: BVAR => CSTR
	 * Also called half reification constraint
	 * Ensures:<br/>
	 * <p/>- BVAR = 1 =>  CSTR is satisfied, <br/>
	 * <p/>- CSTR is not satisfied => BVAR = 0 <br/>
	 * <p/>
	 * Example : <br/>
	 * - <code>ifThen(b1, arithm(v1, "=", 2));</code>:
	 * b1 is equal to 1 => v1 = 2, so v1 != 2 => b1 is equal to 0
	 * But if b1 is equal to 0, nothing happens
	 *
	 * BEWARE : it is automatically posted (it cannot be reified)
	 *
	 * Note that its implementation relies on reification
	 * (it will not be faster)
	 * <p/>
	 *
	 * @param BVAR variable of reification
	 * @param CSTR the constraint to be satisfied when BVAR = 1
	 */
	public static void ifThen(BoolVar BVAR, Constraint CSTR) {
		Solver s = BVAR.getSolver();
		// PRESOLVE
		if(BVAR.contains(1)){
			if(BVAR.isInstantiated()){
				s.post(CSTR);
			}else if(CSTR.isSatisfied() == ESat.FALSE){
				s.post(ICF.arithm(BVAR,"=",0));
			}
			// END OF PRESOLVE
			else {
				s.post(ICF.arithm(BVAR, "<=", CSTR.reif()));
			}
		}
	}

	/**
	 * Reify a constraint with a boolean variable: BVAR <=> CSTR
	 *
	 * Ensures:<br/>
	 * <p/>- BVAR = 1 <=>  CSTR is satisfied, <br/>
	 *
	 * BEWARE : it is automatically posted (it cannot be reified)
	 *
	 * @param BVAR variable of reification
	 * @param CSTR the constraint to be satisfied when BVAR = 1
	 */
	public static void reification(BoolVar BVAR, Constraint CSTR){
		Solver s = BVAR.getSolver();
		// PRESOLVE
		ESat entail = CSTR.isSatisfied();
		if(BVAR.isInstantiatedTo(1)){
			s.post(CSTR);
		}else if(BVAR.isInstantiatedTo(0)) {
			s.post(not(CSTR));
		}else if(entail == ESat.TRUE) {
			s.post(ICF.arithm(BVAR,"=",1));
		}else if(entail == ESat.FALSE) {
			s.post(ICF.arithm(BVAR,"=",0));
		}
		// END OF PRESOLVE
		else {
			CSTR.reifyWith(BVAR);
		}
	}

	//***********************************************************************************
	// Reifiable reification constraints
	//***********************************************************************************

	/**
	 * Same as ifThenElse, but:
	 * + can be reified
	 * - may be slower
	 */
	public static Constraint ifThenElse_reifiable(Constraint IF, Constraint THEN, Constraint ELSE){
		return ifThenElse_reifiable(IF.reif(), THEN, ELSE);
	}

	/**
	 * Same as ifThenElse, but:
	 * + can be reified
	 * - may be slower
	 */
	public static Constraint ifThenElse_reifiable(BoolVar BVAR, Constraint THEN, Constraint ELSE) {
		return and(ifThen_reifiable(BVAR,THEN),ifThen_reifiable(BVAR.not(),ELSE));
	}

	/**
	 * Same as ifThen, but:
	 * + can be reified
	 * - may be slower
	 */
	public static Constraint ifThen_reifiable(Constraint IF, Constraint THEN) {
		return ifThen_reifiable(IF.reif(), THEN);
	}

	/**
	 * Same as ifThen, but:
	 * + can be reified
	 * - may be slower
	 */
	public static Constraint ifThen_reifiable(BoolVar BVAR, Constraint CSTR) {
		Solver s = BVAR.getSolver();
		// PRESOLVE
		ESat entail = CSTR.isSatisfied();
		if (BVAR.isInstantiatedTo(0) || (BVAR.isInstantiatedTo(1) && entail == ESat.TRUE)) {
			return s.TRUE;
		}else if (BVAR.isInstantiatedTo(1) && entail == ESat.FALSE) {
			return s.FALSE;
		}
		// END PRESOLVE
		return ICF.arithm(BVAR, "<=", CSTR.reif());
	}

	/**
	 * Same as reification, but:
	 * + can be reified
	 * - may be slower
	 */
	public static Constraint reification_reifiable(BoolVar BVAR, Constraint CSTR) {
		Solver s = BVAR.getSolver();
		// PRESOLVE
		ESat entail = CSTR.isSatisfied();
		if (BVAR.isInstantiated() && entail != ESat.UNDEFINED) {
			if ((BVAR.getValue() == 1 && entail == ESat.TRUE)
					|| (BVAR.getValue() == 0 && entail == ESat.FALSE)) {
				return s.TRUE;
			} else {
				return s.FALSE;
			}
		}
		// END PRESOLVE
		else {
			return ICF.arithm(BVAR, "=", CSTR.reif());
		}
	}
}
