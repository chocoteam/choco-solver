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
package org.chocosolver.solver.search.solution;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;

import java.io.Serializable;
import java.util.List;

/**
 * Interface to record and store solutions of a problem
 */
public interface ISolutionRecorder extends Serializable{

	/** @return the last recorded solution, presumably the best one. Returns null if no solution has been found */
	Solution getLastSolution();
	/** @return a list of all recorded solutions */
	List<Solution> getSolutions();

	/**
	 * Restores the last solution found (if any) in this solver.
	 * That is, after calling this method:
	 * <ol>
	 * <li>the search backtracks to the ROOT node in order to restore the initial state of variables, constraints and any other backtrackable structures</li>
	 * <li>the initial state is then saved (by calling : {@code this.getEnvironment().worldPush();}).</li>
	 * <li>each variable is then instantiated to its value in the last recorded solution.</li>
	 * </ol>
	 *
	 * The input state can be rollbacked by calling :  {@code this.getEnvironment().worldPop();}.
	 * @return <tt>true</tt> if a solution exists and has been successfully restored in this solver, <tt>false</tt> otherwise.
	 * @throws ContradictionException when inconsistency is detected while restoring the solution.
	 */
	default void restoreLastSolution() throws ContradictionException {
		restoreSolution(getLastSolution());
	}

	/**
	 * Restores a given solution in this solver.
	 * That is, after calling this method:
	 * <ol>
	 * <li>the search backtracks to the ROOT node in order to restore the initial state of variables, constraints and any other backtrackable structures</li>
	 * <li>the initial state is then saved (by calling : {@code this.getEnvironment().worldPush();}).</li>
	 * <li>each variable is then instantiated to its value in the solution.</li>
	 * </ol>
	 *
	 * The input state can be rolled-back by calling :  {@code this.getEnvironment().worldPop();}.
	 * @param solution the solution to restore
	 * @return <tt>true</tt> if a solution exists and has been successfully restored in this solver, <tt>false</tt> otherwise.
	 * @throws ContradictionException when inconsistency is detected while restoring the solution.
	 */
	default void restoreSolution(Solution solution){
		if(solution!=null){
			try{
				getModel().getSolver().restoreRootNode();
				getModel().getEnvironment().worldPush();
				solution.restore(getModel());
			}catch (ContradictionException e){
				throw new UnsupportedOperationException("restoring the solution ended in a failure");
			}
			getModel().getSolver().getEngine().flush();
		}
	}

	Model getModel();
}
