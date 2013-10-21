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

package solver.constraints.nary.cumulative;

import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.objects.setDataStructures.ISet;
import java.io.Serializable;

/**
 * Class able to filter a subset of tasks for the cumulative constraint
 * @author Jean-Guillaume Fages
 */
public abstract class CumulFilter implements Serializable{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected IntVar[] s, e, d, h;	// activities variables
	protected IntVar capamax;					// capacity
	protected Propagator aCause;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * An object which can filter subset of tasks for the cumulative constraint
	 * @param st	start variables
	 * @param du	duration variables
	 * @param en	end variables
	 * @param he	height variables
	 * @param capa	maximum capacity variable
	 * @param cause	a cumulative propagator
	 */
	public CumulFilter(IntVar[] st, IntVar[] du, IntVar[] en, IntVar[] he, IntVar capa, Propagator cause){
		int n = st.length;
		assert (n==du.length && n==en.length && n==he.length);
		this.capamax = capa;
		this.s = st;
		this.d = du;
		this.e = en;
		this.h = he;
		this.aCause = cause;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	/**
	 * Filters the cumulative constraint over the subset of tasks induced by tasks
	 * @param tasks
	 * @throws ContradictionException
	 */
	public abstract void filter(ISet tasks) throws ContradictionException;
}