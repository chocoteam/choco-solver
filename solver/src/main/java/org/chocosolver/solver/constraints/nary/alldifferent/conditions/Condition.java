/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 19/07/13
 * Time: 11:06
 */

package org.chocosolver.solver.constraints.nary.alldifferent.conditions;

import org.chocosolver.solver.variables.IntVar;

/**
 * A condition over integer variables
 */
public interface Condition {

	/**
	 * @param x an Integer Variable
	 * @return true iff x satisfies this condition
	 */
	boolean holdOnVar(IntVar x);

	/** True condition, always satisfied */
	Condition TRUE = new Condition() {
		@Override
		public boolean holdOnVar(IntVar x) {
			return true;
		}
		@Override
		public String toString(){
			return "";
		}
	};
	/** Satisfied iff the variable cannot take value 0*/
	Condition EXCEPT_0 = new Condition() {
		@Override
		public boolean holdOnVar(IntVar x) {
			return !x.contains(0);
		}
		@Override
		public String toString(){
			return "_except_0";
		}
	};
}
