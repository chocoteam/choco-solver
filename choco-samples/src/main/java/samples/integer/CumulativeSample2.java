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

package samples.integer;

import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.Task;
import solver.variables.VariableFactory;
import util.ESat;

/**
 * @author Gregy4
 */
public class CumulativeSample2 extends AbstractProblem {

	public static int NUM_OF_TASKS = 1000;
	public static int HORIZON = 20000;

	IntVar[] starts, ends;
	IntVar maxEnd;

	@Override
	public void createSolver(){
		solver = new Solver("schedule");
	}

	@Override
	public void buildModel(){
		// build variables
		starts = new IntVar[NUM_OF_TASKS];
		ends = new IntVar[NUM_OF_TASKS];
		IntVar duration = VariableFactory.fixed(10, solver);
		maxEnd = VariableFactory.bounded("maxEnd", 0, HORIZON, solver);
		IntVar[] res = new IntVar[NUM_OF_TASKS];
		Task[] tasks = new Task[NUM_OF_TASKS];
		for (int iTask=0; iTask < NUM_OF_TASKS; ++iTask) {
			starts[iTask] = VariableFactory.bounded("start" + iTask, 0, HORIZON, solver);
			ends[iTask] = VariableFactory.bounded("ends" + iTask, 0, HORIZON, solver);
			tasks[iTask] = VariableFactory.task(starts[iTask], duration, ends[iTask]);
			res[iTask] = VariableFactory.fixed(1 , solver);
		}

		// post a cumulative constraint
		solver.post(IntConstraintFactory.cumulative(tasks, res, VariableFactory.fixed(1, solver), false));

		// maintain makespan
		solver.post(IntConstraintFactory.maximum(maxEnd, ends));

		// add precedences
		int prevIdx = -1;
		for (int iTask=999; iTask >= 0; --iTask) {
			if (prevIdx != -1) {
				if (iTask % 2 == 0 || iTask % 3 == 0) {
					solver.post(IntConstraintFactory.arithm(starts[iTask], ">=", ends[prevIdx]));
				}
			}
			prevIdx = iTask;
		}
	}

	@Override
	public void configureSearch(){
		solver.set(IntStrategyFactory.lexico_LB(starts));
	}

	@Override
	public void solve(){
		solver.findSolution();
	}

	@Override
	public void prettyOut(){
		if (solver.isFeasible() == ESat.TRUE) {
			System.out.println("solution found");
		}
	}

	public static void main(String[] args){
		new CumulativeSample2().execute(args);
	}
}