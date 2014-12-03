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

package org.chocosolver.solver.search.solution;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorClose;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;

/**
 * Class to store the pareto front (multi-objective optimization).
 * Worse solutions are dynamically removed from the solution set.
 *
 * @author Jean-Guillaume Fages
 */
public class ParetoSolutionsRecorder extends AllSolutionsRecorder {

	ResolutionPolicy policy;
	IntVar[] objectives;
	int n;

	public ParetoSolutionsRecorder(final ResolutionPolicy policy, final IntVar[] objectives){
		super(objectives[0].getSolver());
		this.objectives = objectives;
		this.n = objectives.length;
		this.policy = policy;
		solver.plugMonitor(new IMonitorClose() {
			@Override
			public void beforeClose() {
				Solution last = getLastSolution();
				if(last!=null){
					try{
						solver.getSearchLoop().restoreRootNode();
						solver.getEnvironment().worldPush();
						last.restore();
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

	@Override
	protected IMonitorSolution createRecMonitor() {
		return new IMonitorSolution() {
			@Override
			public void onSolution() {
				int[] vals = new int[n];
				for(int i=0;i<n;i++){
					vals[i] = objectives[i].getValue();
				}
				// update solution set
				for(int i=solutions.size()-1;i>=0;i--){
					if(dominatedSolution(solutions.get(i),vals)){
						solutions.remove(i);
					}
				}
				// store current solution
				Solution solution = new Solution();
				solution.record(solver);
				solutions.add(solution);
				// aim at better solutions
				Constraint[] better = new Constraint[n];
				Operator symbol = Operator.GT;
				if(policy==ResolutionPolicy.MINIMIZE){
                    symbol = Operator.LT;
				}
				for(int i=0;i<n;i++){
					better[i] = ICF.arithm(objectives[i],symbol.toString(),vals[i]);
				}
				solver.post(LCF.or(better));
			}
		};
	}

	private boolean dominatedSolution(Solution solution, int[] vals) {
		for(int i=0;i<n;i++){
			int delta = solution.getIntVal(objectives[i])-vals[i];
			if((delta>0 && policy==ResolutionPolicy.MAXIMIZE)||(delta<0 && policy==ResolutionPolicy.MINIMIZE)){
				return false;
			}
		}
		return true;
	}
}
