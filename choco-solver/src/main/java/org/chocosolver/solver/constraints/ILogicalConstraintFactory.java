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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * Interface to make constraints for logical operations and constraint reification
 *
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES (www.cosling.com)
 */
public interface ILogicalConstraintFactory {

	//***********************************************************************************
	// simple logical constraints
	//***********************************************************************************

	/**
	 * Creates an and constraint that is satisfied if all boolean variables in <i>bools</i> are true
	 * @param bools an array of boolean variable
	 * @return a constraint and ensuring that variables in <i>bools</i> are all set to true
	 */
	default Constraint and(BoolVar... bools){
		Solver s = bools[0].getSolver();
		IntVar sum = s.intVar(0, bools.length, true);
		s.post(IntConstraintFactory.sum(bools,"=",sum));
		return IntConstraintFactory.arithm(sum,"=",bools.length);
	}

	/**
	 * Creates an and constraint that is satisfied if at least one boolean variables in <i>bools</i> is true
	 * @param bools an array of boolean variable
	 * @return a constraint that is satisfied if at least one boolean variables in <i>bools</i> is true
	 */
	default Constraint or(BoolVar... bools){
		Solver s = bools[0].getSolver();
		IntVar sum = s.intVar(0, bools.length, true);
		s.post(IntConstraintFactory.sum(bools,"=",sum));
		return IntConstraintFactory.arithm(sum,">=",1);
	}

	/**
	 * Creates an and constraint that is satisfied if all constraints in <i>cstrs</i> are satisfied
	 * BEWARE: this should not be used to post several constraints at once but in a reification context
	 * @param cstrs an array of constraints
	 * @return a constraint and ensuring that all constraints in <i>cstrs</i> are satisfied
	 */
	default Constraint and(Constraint... cstrs){
		BoolVar[] bools = new BoolVar[cstrs.length];
		for(int i=0;i<cstrs.length;i++){
			bools[i] = cstrs[i].reif();
		}
		return and(bools);
	}

	/**
	 * Creates an and constraint that is satisfied if at least one constraint in <i>cstrs</i> are satisfied
	 * @param cstrs an array of constraints
	 * @return a constraint and ensuring that at least one constraint in <i>cstrs</i> are satisfied
	 */
	default Constraint or(Constraint... cstrs){
		BoolVar[] bools = new BoolVar[cstrs.length];
		for(int i=0;i<cstrs.length;i++){
			bools[i] = cstrs[i].reif();
		}
		return or(bools);
	}

	/**
	 * Gets the opposite of a given constraint
	 * Works for any constraint, including globals, but the associated performances might be weak
	 * @param cstr a constraint
	 * @return the opposite constraint of <i>cstr</i>
	 */
	default Constraint not(Constraint cstr){
		return cstr.getOpposite();
	}

	//***********************************************************************************
	// Non-reifiable reification constraints
	//***********************************************************************************

	/**
	 * Posts a constraint ensuring that if <i>ifCstr</i> is satisfied, then <i>thenCstr</i> must be satisfied as well
	 * Otherwise, <i>elseCstr</i> must be satisfied
	 *
	 * <i>ifCstr</i> => <i>ThenCstr</i>
	 * <i>not(ifCstr)</i> => <i>ElseCstr</i>
	 *
	 * BEWARE : it is automatically posted (it cannot be reified)
	 *
	 * @param ifCstr a constraint
	 * @param thenCstr a constraint
	 * @param elseCstr a constraint
	 */
	default void ifThenElse(Constraint ifCstr, Constraint thenCstr, Constraint elseCstr){
		ifThenElse(ifCstr.reif(), thenCstr, elseCstr);
	}

	/**
	 * Posts an implication constraint: <i>ifVar</i> => <i>thenCstr</i> && not(<i>ifVar</i>) => <i>elseCstr</i>.
	 * <br/>
	 * Ensures:
	 * <p/>- <i>ifVar</i> = 1 =>  <i>thenCstr</i> is satisfied, <br/>
	 * <p/>- <i>ifVar</i> = 0 =>  <i>elseCstr</i> is satisfied, <br/>
	 * <p/>- <i>thenCstr</i> is not satisfied => <i>ifVar</i> = 0 <br/>
	 * <p/>- <i>elseCstr</i> is not satisfied => <i>ifVar</i> = 1 <br/>
	 * <p/>
	 * <p/> In order to get <i>ifVar</i> <=> <i>thenCstr</i>, use reification
	 *
	 * BEWARE : it is automatically posted (it cannot be reified)
	 *
	 * @param ifVar     variable of reification
	 * @param thenCstr     the constraint to be satisfied when <i>ifVar</i> = 1
	 * @param elseCstr     the constraint to be satisfied when <i>ifVar</i> = 0
	 */
	default void ifThenElse(BoolVar ifVar, Constraint thenCstr, Constraint elseCstr) {
		ifThen(ifVar,thenCstr);
		ifThen(ifVar.not(),elseCstr);
	}

	/**
	 * Posts a constraint ensuring that if <i>ifCstr</i> is satisfied, then <i>thenCstr</i> is satisfied as well
	 *
	 * BEWARE : it is automatically posted (it cannot be reified)
	 *
	 * @param ifCstr a constraint
	 * @param thenCstr a constraint
	 */
	default void ifThen(Constraint ifCstr, Constraint thenCstr) {
		ifThen(ifCstr.reif(), thenCstr);
	}

	/**
	 * Posts an implication constraint: <i>ifVar</i> => <i>thenCstr</i>
	 * Also called half reification constraint
	 * Ensures:<br/>
	 * <p/>- <i>ifVar</i> = 1 =>  <i>thenCstr</i> is satisfied, <br/>
	 * <p/>- <i>thenCstr</i> is not satisfied => <i>ifVar</i> = 0 <br/>
	 * <p/>
	 * Example : <br/>
	 * - <code>ifThen(b1, arithm(v1, "=", 2));</code>:
	 * b1 is equal to 1 => v1 = 2, so v1 != 2 => b1 is equal to 0
	 * But if b1 is equal to 0, nothing happens
	 *
	 * BEWARE : it is automatically posted (it cannot be reified)
	 * <p/>
	 *
	 * @param ifVar variable of reification
	 * @param thenCstr the constraint to be satisfied when <i>ifVar</i> = 1
	 */
	default void ifThen(BoolVar ifVar, Constraint thenCstr) {
		// PRESOLVE
		if(ifVar.contains(1)){
			Solver s = ifVar.getSolver();
			if(ifVar.isInstantiated()){
				s.post(thenCstr);
			}else if(thenCstr.isSatisfied() == ESat.FALSE){
				s.post(ICF.arithm(ifVar,"=",0));
			}
			// END OF PRESOLVE
			else {
				s.post(ICF.arithm(ifVar, "<=", thenCstr.reif()));
			}
		}
	}

	/**
	 * Posts an equivalence constraint stating that
	 * <i>cstr1</i> is satisfied <=>  <i>cstr2</i> is satisfied,
	 *
	 * BEWARE : it is automatically posted (it cannot be reified)
	 *
	 * @param cstr1 a constraint to be satisfied if and only if <i>cstr2</i> is satisfied
	 * @param cstr2 a constraint to be satisfied if and only if <i>cstr1</i> is satisfied
	 */
	default void ifOnlyIf(Constraint cstr1, Constraint cstr2){
		reification(cstr1.reif(),cstr2);
	}

	/**
	 * Reify a constraint with a boolean variable:
	 * <i>var</i> = 1 <=>  <i>cstr</i> is satisfied,
	 *
	 * Equivalent to ifOnlyIf
	 *
	 * BEWARE : it is automatically posted (it cannot be reified)
	 *
	 * @param var variable of reification
	 * @param cstr the constraint to be satisfied if and only if <i>var</i> = 1
	 */
	default void reification(BoolVar var, Constraint cstr){
		Solver s = var.getSolver();
		// PRESOLVE
		ESat entail = cstr.isSatisfied();
		if(var.isInstantiatedTo(1)){
			s.post(cstr);
		}else if(var.isInstantiatedTo(0)) {
			s.post(not(cstr));
		}else if(entail == ESat.TRUE) {
			s.post(ICF.arithm(var,"=",1));
		}else if(entail == ESat.FALSE) {
			s.post(ICF.arithm(var,"=",0));
		}
		// END OF PRESOLVE
		else {
			cstr.reifyWith(var);
		}
	}
}
