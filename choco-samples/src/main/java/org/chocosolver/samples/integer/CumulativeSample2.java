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
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.ESat;

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
	public void buildModel() {
		// build variables
		starts = new IntVar[NUM_OF_TASKS];
		ends = new IntVar[NUM_OF_TASKS];
		IntVar duration = solver.intVar(10);
		maxEnd = solver.intVar("maxEnd", 0, HORIZON, true);
		IntVar[] res = new IntVar[NUM_OF_TASKS];
		Task[] tasks = new Task[NUM_OF_TASKS];
		for (int iTask = 0; iTask < NUM_OF_TASKS; ++iTask) {
			starts[iTask] = solver.intVar("start" + iTask, 0, HORIZON, true);
			ends[iTask] = solver.intVar("ends" + iTask, 0, HORIZON, true);
			tasks[iTask] = new Task(starts[iTask], duration, ends[iTask]);
			res[iTask] = solver.intVar(1);
		}

		// post a cumulative constraint
		solver.cumulative(tasks, res, solver.intVar(1), false).post();

		// maintain makespan
		solver.max(maxEnd, ends).post();

		// add precedences
		int prevIdx = -1;
		for (int iTask = 999; iTask >= 0; --iTask) {
			if (prevIdx != -1) {
				if (iTask % 2 == 0 || iTask % 3 == 0) {
					solver.arithm(starts[iTask], ">=", ends[prevIdx]).post();
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
