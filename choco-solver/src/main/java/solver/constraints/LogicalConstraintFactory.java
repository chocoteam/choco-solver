/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package solver.constraints;

import solver.Solver;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.tools.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * @author Jean-Guillaume Fages
 * @since 15/05/2013
 */
public class LogicalConstraintFactory {

	//***********************************************************************************
	// BoolVar-based constraints
	//***********************************************************************************

	/**
	 * Make a and constraint
	 * @param BOOLS an array of boolean variable
	 * @return a constraint and ensuring that variables in BOOLS are all set to true
	 */
	public static Constraint and(BoolVar... BOOLS){
		Solver s = BOOLS[0].getSolver();
		// The domain of the fixed var enforces that sum(BOOLS) == BOOLS.length
		return IntConstraintFactory.sum(BOOLS, VariableFactory.fixed(BOOLS.length, s));
	}

	/**
	 * Make an or constraint
	 * @param BOOLS an array of boolean variable
	 * @return a constraint or ensuring that at least one variables in BOOLS is set to true
	 */
	public static Constraint or(BoolVar... BOOLS){
		Solver s = BOOLS[0].getSolver();
		IntVar sum = VariableFactory.bounded(StringUtils.randomName(),1,BOOLS.length,s);
		// The domain of sum var enforces that 1 <= sum(BOOLS) <= BOOLS.length
		return IntConstraintFactory.sum(BOOLS,sum);
	}

	//***********************************************************************************
	// Constraint-based constraints
	//***********************************************************************************

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
	 * @param CONS a constraint
	 * @return the opposite constraint of CONS
	 */
	public static Constraint not(Constraint CONS){
		return CONS.getOpposite();
	}

	/**
	 * If Then constraint
	 * @param IF a constraint
	 * @param THEN a constraint
	 * @return a constraint ensuring that if IF is satisfied, then THEN is satisfied as well
	 */
	public static Constraint ifThen(Constraint IF, Constraint THEN){
		return ifThen(IF.reif(), THEN);
	}

	/**
	 * If Then Else constraint
	 * @param IF a constraint
	 * @param THEN a constraint
	 * @param ELSE a constraint
	 * @return a constraint ensuring that if IF is satisfied, then THEN is satisfied as well
	 * Otherwise, ELSE must be satisfied
	 */
	public static Constraint ifThenElse(Constraint IF, Constraint THEN, Constraint ELSE){
		return ifThenElse(IF.reif(), THEN, ELSE);
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
	 * <p/> In order to have BVAR <=> THEN, make sure ELSE is the opposite constraint of THEN
	 *
	 * @param BVAR     variable of reification
	 * @param THEN     the constraint to be satisfied when BVAR = 1
	 * @param ELSE the constraint to be satisfied when BVAR = 0
	 */
	public static Constraint ifThenElse(BoolVar BVAR, Constraint THEN, Constraint ELSE) {
		return and(ifThen(BVAR,THEN),ifThen(BVAR.not(),ELSE));
//		return new ImplicationConstraint(BVAR, THEN, ELSE);
	}

	/**
	 * Implication constraint: BVAR => CSTR
	 * Also called half reification constraint
	 * Ensures:<br/>
	 * <p/>- BVAR = 1 =>  CSTR is satisfied, <br/>
	 * <p/>- CSTR is not satisfied => BVAR = 0 <br/>
	 * <p/>
	 * Example : <br/>
	 * - <code>ifThen(b1, arithm(v1, "=", 2));</code>:
	 * b1 is equal to 1 => v1 = 2, so v1 != 2 => b1 is equal to 0
	 * But if b1 is equal to 0, nothing happens
	 * <p/>
	 *
	 * @param BVAR variable of reification
	 * @param CSTR the constraint to be satisfied when BVAR = 1
	 */
	public static Constraint ifThen(BoolVar BVAR, Constraint CSTR) {
//		return ifThenElse(BVAR, CSTR, CSTR.getSolver().TRUE);
		return ICF.arithm(BVAR,"<=",CSTR.reif());
	}

	public static Constraint reification(BoolVar BVAR, Constraint CSTR){
		return ICF.arithm(BVAR,"=",CSTR.reif());
	}
}
