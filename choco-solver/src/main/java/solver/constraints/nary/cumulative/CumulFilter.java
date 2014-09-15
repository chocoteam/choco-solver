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

	protected Propagator aCause;
	protected int nbMaxTasks;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * An object which can filter subset of tasks for the cumulative constraint
	 * @param nbMaxTasks	maximum number of tasks
	 * @param cause			a cumulative propagator
	 */
	public CumulFilter(int nbMaxTasks, Propagator cause){
		this.nbMaxTasks = nbMaxTasks;
		this.aCause = cause;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	/**
	 * Filters the cumulative constraint over the subset of tasks induced by tasks
	 * @param s		start variables
	 * @param d		duration variables
	 * @param e		end variables
	 * @param h		height variables
	 * @param capa	maximum capacity variable
	 * @param tasks	subset of tasks to filter
	 * @throws ContradictionException
	 */
	public abstract void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks) throws ContradictionException;
}