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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 29/11/13
 * Time: 15:11
 */

package org.chocosolver.samples.todo.problems.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.Random;

import static org.chocosolver.solver.ResolutionPolicy.MINIMIZE;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.lastConflict;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.minDomLBSearch;

public class CumulativeSample extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	IntVar[] start;
	IntVar makespan;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		model = new Model("Cumulative example: makespan minimisation");
		IntVar capa = model.intVar(6);
		int n = 10;
		int max = 1000;
		makespan = model.intVar("makespan", 0, max, true);
		start = model.intVarArray("start", n, 0, max, true);
		IntVar[] end = new IntVar[n];
		IntVar[] duration = new IntVar[n];
		IntVar[] height = new IntVar[n];
		Task[] task = new Task[n];
		Random rd = new Random(0);
		for (int i = 0; i < n; i++) {
			duration[i] = model.intVar(rd.nextInt(20) + 1);
			height[i] = model.intVar(rd.nextInt(5) + 1);
			end[i] = model.intOffsetView(start[i], duration[i].getValue());
			task[i] = new Task(start[i], duration[i], end[i]);
		}
		model.cumulative(task, height, capa).post();
		model.max(makespan, end).post();
	}

	@Override
	public void configureSearch() {
		Solver r = model.getSolver();
		r.set(lastConflict(minDomLBSearch(start)));
	}

	@Override
	public void solve() {
		model.setObjective(MINIMIZE, makespan);
		while(model.solve()){
			System.out.println("New solution found : "+makespan);
		}
		model.getSolver().printStatistics();
	}

	public static void main(String[] args){
		new CumulativeSample().execute();
	}
}
