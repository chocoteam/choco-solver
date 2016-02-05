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
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.ESat;

/**
 * @deprecated : logical constraint creation should be done through the {@link Model} object
 * which extends {@link org.chocosolver.solver.constraints.ILogicalConstraintFactory}
 *
 * This class will be removed in versions > 3.4.0
 */
@Deprecated
public class LogicalConstraintFactory {

	//***********************************************************************************
	// simple logical constraints
	//***********************************************************************************

	/**
	 * @deprecated : use {@link Model#and(BoolVar...)} instead
	 * This will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static Constraint and(BoolVar... BOOLS){
		return BOOLS[0].getModel().and(BOOLS);
	}

	/**
	 * @deprecated : use {@link Model#or(BoolVar...)} instead
	 * This will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static Constraint or(BoolVar... BOOLS){
		return BOOLS[0].getModel().or(BOOLS);
	}

	/**
	 * @deprecated : use {@link Model#and(Constraint...)} instead
	 * This will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static Constraint and(Constraint... CONS){
		BoolVar[] bools = new BoolVar[CONS.length];
		for(int i=0;i<CONS.length;i++){
			bools[i] = CONS[i].reify();
		}
		return and(bools);
	}

	/**
	 * @deprecated : use {@link Model#or(Constraint...)} instead
	 * This will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static Constraint or(Constraint... CONS){
		BoolVar[] bools = new BoolVar[CONS.length];
		for(int i=0;i<CONS.length;i++){
			bools[i] = CONS[i].reify();
		}
		return or(bools);
	}

	/**
	 * @deprecated : use {@link Model#not(Constraint)} instead
	 * This will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static Constraint not(Constraint CONS){
		return CONS.getOpposite();
	}

	//***********************************************************************************
	// Non-reifiable reification constraints
	//***********************************************************************************

	/**
	 * @deprecated : use {@link Model#ifThenElse(Constraint, Constraint, Constraint)} instead
	 * This will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static void ifThenElse(Constraint IF, Constraint THEN, Constraint ELSE){
		ifThenElse(IF.reify(), THEN, ELSE);
	}

	/**
	 * @deprecated : use {@link Model#ifThenElse(BoolVar, Constraint, Constraint)} instead
	 * This will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static void ifThenElse(BoolVar BVAR, Constraint THEN, Constraint ELSE) {
		ifThen(BVAR,THEN);
		ifThen(BVAR.not(),ELSE);
	}

	/**
	 * @deprecated : use {@link Model#ifThen(Constraint, Constraint)} instead
	 * This will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static void ifThen(Constraint IF, Constraint THEN) {
		ifThen(IF.reify(), THEN);
	}

	/**
	 * @deprecated : use {@link Model#ifThen(BoolVar, Constraint)} instead
	 * This will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static void ifThen(BoolVar BVAR, Constraint CSTR) {
		BVAR.getModel().ifThen(BVAR,CSTR);
	}

	/**
	 * @deprecated : use {@link Model#reification(BoolVar, Constraint)} instead
	 * This will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static void reification(BoolVar BVAR, Constraint CSTR){
		BVAR.getModel().reification(BVAR,CSTR);
	}



	//***********************************************************************************
	// Reifiable reification constraints
	//***********************************************************************************

	/**
	 * @deprecated : will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static Constraint ifThenElse_reifiable(Constraint IF, Constraint THEN, Constraint ELSE){
		return ifThenElse_reifiable(IF.reif(), THEN, ELSE);
	}

	/**
	 * @deprecated : will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static Constraint ifThenElse_reifiable(BoolVar BVAR, Constraint THEN, Constraint ELSE) {
		return and(ifThen_reifiable(BVAR,THEN),ifThen_reifiable(BVAR.not(),ELSE));
	}

	/**
	 * @deprecated : will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static Constraint ifThen_reifiable(Constraint IF, Constraint THEN) {
		return ifThen_reifiable(IF.reif(), THEN);
	}

	/**
	 * @deprecated : will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static Constraint ifThen_reifiable(BoolVar BVAR, Constraint CSTR) {
		Model s = BVAR.getModel();
		// PRESOLVE
		ESat entail = CSTR.isSatisfied();
		if (BVAR.isInstantiatedTo(0) || (BVAR.isInstantiatedTo(1) && entail == ESat.TRUE)) {
			return s.TRUE();
		}else if (BVAR.isInstantiatedTo(1) && entail == ESat.FALSE) {
			return s.FALSE();
		}
		// END PRESOLVE
		return ICF.arithm(BVAR, "<=", CSTR.reif());
	}

	/**
	 * @deprecated : will be removed in versions > 3.4.0
	 */
	@Deprecated
	public static Constraint reification_reifiable(BoolVar BVAR, Constraint CSTR) {
		Model s = BVAR.getModel();
		// PRESOLVE
		ESat entail = CSTR.isSatisfied();
		if (BVAR.isInstantiated() && entail != ESat.UNDEFINED) {
			if ((BVAR.getValue() == 1 && entail == ESat.TRUE)
					|| (BVAR.getValue() == 0 && entail == ESat.FALSE)) {
				return s.TRUE();
			} else {
				return s.FALSE();
			}
		}
		// END PRESOLVE
		else {
			return ICF.arithm(BVAR, "=", CSTR.reify());
		}
	}
}
