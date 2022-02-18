/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 29/11/13
 * Time: 15:11
 */

package org.chocosolver.examples.integer;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.Random;

import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.Search.lastConflict;
import static org.chocosolver.solver.search.strategy.Search.minDomLBSearch;

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
		r.setSearch(lastConflict(minDomLBSearch(start)));
	}

	@Override
	public void solve() {
		model.setObjective(false, makespan);
		while (model.getSolver().solve()) {
			out.println("New solution found : " + makespan);
		}
		model.getSolver().printStatistics();
	}

	public static void main(String[] args){
		new CumulativeSample().execute();
	}
}
