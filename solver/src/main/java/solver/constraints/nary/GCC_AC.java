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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 26/07/12
 * Time: 18:05
 */

package solver.constraints.nary;

import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.nary.globalcardinality.PropGCC_AC_Cards;
import solver.constraints.propagators.nary.globalcardinality.PropGCC_AC_LowUp;
import solver.variables.IntVar;

/**
 * GCC constraint performing Arc Consistency
 * @author Jean-Guillaume Fages
 * @since July, 2012
 */
public class GCC_AC extends Constraint<IntVar,Propagator<IntVar>>{

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Global Cardinality Constraint (GCC) for integer variables
	 * foreach i, |{v = value[i] | for any v in vars}|=cards[i]
	 * Does not run incrementally
	 *
	 * @param vars
	 * @param value
	 * @param cards
	 * @param solver
	 */
	public GCC_AC(IntVar[] vars, int[] value, IntVar[] cards, Solver solver) {
		super(ArrayUtils.append(vars,cards), solver);
		addPropagators(new PropGCC_AC_Cards(vars,value,cards,this,solver));
	}

	/**
	 * Global Cardinality Constraint (GCC) for integer variables
	 * foreach i, low[i]<=|{v = value[i] | for any v in vars}|<=up[i]
	 * Runs incrementally
	 *
	 * @param vars
	 * @param value
	 * @param low
	 * @param up
	 * @param solver
	 */
	public GCC_AC(IntVar[] vars, int[] value, int[] low, int[] up, Solver solver) {
		super(vars, solver);
		addPropagators(new PropGCC_AC_LowUp(vars,value,low,up,this,solver));
	}
}
