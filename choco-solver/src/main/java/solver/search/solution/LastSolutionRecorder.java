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

package solver.search.solution;

import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.IMonitorClose;
import solver.search.loop.monitors.IMonitorSolution;

import java.util.LinkedList;
import java.util.List;

/**
 * Recorder for the last (presumably best) solution found so far
 * The Solution object is updated on each solution
 *
 * @author Jean-Guillaume Fages
 * @since 2013
 */
public class LastSolutionRecorder implements ISolutionRecorder {

	Solution solution;
	Solver solver;
	boolean restoreOnClose;

	public LastSolutionRecorder(final Solution solution, boolean restoreOnClose, final Solver solver){
		this.solver = solver;
		this.solution = solution;
		this.restoreOnClose = restoreOnClose;
		solver.plugMonitor(new IMonitorSolution() {
			@Override
			public void onSolution() {
				solution.record(solver);
			}
		});
		if(restoreOnClose){
			solver.plugMonitor(new IMonitorClose() {
				@Override
				public void beforeClose() {
					if(solution.hasBeenFound()){
						try{
							solver.getSearchLoop().restoreRootNode();
							solver.getEnvironment().worldPush();
							solution.restore();
						}catch (ContradictionException e){
							throw new UnsupportedOperationException("restoring the last solution ended in a failure");
						}
						solver.getEngine().flush();
					}
				}
				@Override
				public void afterClose() {}
			});
		}
	}

	@Override
	public Solution getLastSolution(){
		return solution.hasBeenFound()?solution:null;
	}

	@Override
	public List<Solution> getSolutions() {
		List<Solution> l = new LinkedList<>();
		if(solution.hasBeenFound()){
			l.add(solution);
		}
		return l;
	}
}
