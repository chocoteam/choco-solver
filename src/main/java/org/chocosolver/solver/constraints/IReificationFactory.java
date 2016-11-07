/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
import org.chocosolver.solver.constraints.reification.*;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * Interface to reify constraints
 *
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES
 * @since 4.0.0
 */
public interface IReificationFactory {

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
		ifThenElse(ifCstr.reify(), thenCstr, elseCstr);
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
		ifThen(ifCstr.reify(), thenCstr);
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
			Model s = ifVar.getModel();
			if(ifVar.isInstantiated()) {
				thenCstr.post();
			}else if(thenCstr.isSatisfied() == ESat.FALSE) {
				s.arithm(ifVar, "=", 0).post();
			}
			// END OF PRESOLVE
			else {
				s.arithm(ifVar, "<=", thenCstr.reify()).post();
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
		reification(cstr1.reify(),cstr2);
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
		Model s = var.getModel();
		// PRESOLVE
		ESat entail = cstr.isSatisfied();
		if(var.isInstantiatedTo(1)) {
			cstr.post();
		}else if(var.isInstantiatedTo(0)) {
			cstr.getOpposite().post();
		}else if(entail == ESat.TRUE) {
			s.arithm(var, "=", 1).post();
		}else if(entail == ESat.FALSE) {
			s.arithm(var, "=", 0).post();
		}
		// END OF PRESOLVE
		else {
			cstr.reifyWith(var);
		}
	}

	/**
	 * Posts one constraint that expresses : (x = c) &hArr; b.
	 * Bypasses the reification system.
	 * @param X a integer variable
	 * @param C an int
     * @param B a boolean variable
     */
	default void reifyXeqC(IntVar X, int C, BoolVar B){
		Model model = X.getModel();
		// no check to allow addition during resolution
		model.post(new Constraint("(X = C)<=>B", new PropXeqCReif(X, C, B)));
	}

	/**
	 * Posts one constraint that expresses : (x = y) &hArr; b.
	 * Bypasses the reification system.
	 * @param X an integer variable
	 * @param Y an integer variable
	 * @param B a boolean variable
	 */
	default void reifyXeqY(IntVar X, IntVar Y, BoolVar B){
		Model model = X.getModel();
		// no check to allow addition during resolution
		model.post(new Constraint("(X = Y)<=>B", new PropXeqYReif(X, Y, B)));
	}

	/**
	 * Posts one constraint that expresses : (x &ne; c) &hArr; b.
	 * Bypasses the reification system.
	 * @param X a integer variable
	 * @param C an int
	 * @param B a boolean variable
	 */
	default void reifyXneC(IntVar X, int C, BoolVar B){
		Model model = X.getModel();
		// no check to allow addition during resolution
		model.post(new Constraint("(X != C)<=>B", new PropXneCReif(X, C, B)));
	}

	/**
	 * Posts one constraint that expresses : (x &ne; y) &hArr; b.
	 * Bypasses the reification system.
	 * @param X an integer variable
	 * @param Y an integer variable
	 * @param B a boolean variable
	 */
	default void reifyXneY(IntVar X, IntVar Y, BoolVar B){
		Model model = X.getModel();
		// no check to allow addition during resolution
		model.post(new Constraint("(X = Y)<=>B", new PropXneYReif(X, Y, B)));
	}

	/**
	 * Posts one constraint that expresses : (x < c) &hArr; b.
	 * Bypasses the reification system.
	 * @param X a integer variable
	 * @param C an int
	 * @param B a boolean variable
	 */
	default void reifyXltC(IntVar X, int C, BoolVar B){
		Model model = X.getModel();
		// no check to allow addition during resolution
		model.post(new Constraint("(X < C)<=>B", new PropXltCReif(X, C, B)));
	}

	/**
	 * Posts one constraint that expresses : (x < y) &hArr; b.
	 * Bypasses the reification system.
	 * @param X an integer variable
	 * @param Y an integer variable
	 * @param B a boolean variable
	 */
	default void reifyXltY(IntVar X, IntVar Y, BoolVar B){
		Model model = X.getModel();
		// no check to allow addition during resolution
		model.post(new Constraint("(X < Y)<=>B", new PropXltYReif(X, Y, B)));
	}

	/**
	 * Posts one constraint that expresses : (x < y + c) &hArr; b.
	 * Bypasses the reification system.
	 * @param X an integer variable
	 * @param Y an integer variable
	 * @param C an int
	 * @param B a boolean variable
	 */
	default void reifyXltYC(IntVar X, IntVar Y, int C, BoolVar B){
		Model model = X.getModel();
		// no check to allow addition during resolution
		model.post(new Constraint("(X < Y + C)<=>B", new PropXltYCReif(X, Y, C, B)));
	}

	/**
	 * Posts one constraint that expresses : (x > c) &hArr; b.
	 * Bypasses the reification system.
	 * @param X a integer variable
	 * @param C an int
	 * @param B a boolean variable
	 */
	default void reifyXgtC(IntVar X, int C, BoolVar B){
		Model model = X.getModel();
		// no check to allow addition during resolution
		model.post(new Constraint("(X > C)<=>B", new PropXgtCReif(X, C, B)));
	}



}