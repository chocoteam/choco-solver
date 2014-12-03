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
import org.chocosolver.solver.variables.VF;

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
		IntVar capa = VF.fixed(6,solver);
		int n = 10;
		int max = 1000;
		makespan = VF.bounded("makespan",0,max,solver);
		start = VF.boundedArray("start",n,0,max,solver);
		IntVar[] end = new IntVar[n];
		IntVar[] duration = new IntVar[n];
		IntVar[] height = new IntVar[n];
		Task[] task = new Task[n];
		Random rd = new Random(0);
		for(int i=0;i<n;i++){
			duration[i] = VF.fixed(rd.nextInt(20)+1,solver);
			height[i] = VF.fixed(rd.nextInt(5)+1,solver);
			end[i] = VF.offset(start[i],duration[i].getValue());
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
