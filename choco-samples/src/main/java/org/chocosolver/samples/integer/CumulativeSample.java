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

package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.Random;

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
	public void createSolver() {
		solver = new Solver("Cumulative example: makespan minimisation");
	}

	@Override
	public void buildModel() {
		IntVar capa = solver.intVar(6);
		int n = 10;
		int max = 1000;
		makespan = solver.intVar("makespan", 0, max, true);
		start = solver.intVarArray("start", n, 0, max, true);
		IntVar[] end = new IntVar[n];
		IntVar[] duration = new IntVar[n];
		IntVar[] height = new IntVar[n];
		Task[] task = new Task[n];
		Random rd = new Random(0);
		for(int i=0;i<n;i++){
			duration[i] = solver.intVar(rd.nextInt(20) + 1);
			height[i] = solver.intVar(rd.nextInt(5) + 1);
			end[i] = solver.intOffsetView(start[i],duration[i].getValue());
			task[i] = new Task(start[i],duration[i],end[i]);
		}
		solver.post(ICF.cumulative(task,height,capa,true));
		solver.post(ICF.maximum(makespan,end));
	}

	@Override
	public void configureSearch() {
		solver.set(ISF.minDom_LB(start));
		solver.set(ISF.lastConflict(solver,solver.getStrategy()));
	}

	@Override
	public void solve() {
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE,makespan);
	}

	@Override
	public void prettyOut() {}

	public static void main(String[] args){
		new CumulativeSample().execute();
	}
}
