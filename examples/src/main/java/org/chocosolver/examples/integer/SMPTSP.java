/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitialize;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.Search.minDomLBSearch;

/**
 * simple CP model to solve a toy SMPTSP instance
 * (see Fages and Lap&egrave;gue, CP'13 or Artificial Intelligence journal)
 * Enumeration of all optimal solutions
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public class SMPTSP extends AbstractProblem {

	// ***********************************************************************************
	// VARIABLES
	// ***********************************************************************************

	//input
	private int nbTasks;
	private int nbAvailableShifts;
	private int bestObj;

	// model
	private IntVar nbValues;
	private IntVar[] assignment;
	List<Solution> solutions = new ArrayList<>();

	// ***********************************************************************************
	// METHODS
	// ***********************************************************************************


	@Override
	public void buildModel() {
		model = new Model();
		// Input
		nbTasks = 5;
		nbAvailableShifts = 5;
		int[][] skilledShifts = new int[][]{{2, 3, 4}, {1, 2, 3}, {1, 3}, {3, 4, 5}, {1, 2, 5}};
		final boolean[][] taskOverlaps = new boolean[][]{
				{true, true, true, true, false},
				{true, true, true, false, false},
				{true, true, true, true, false},
				{true, false, true, true, true},
				{false, false, false, true, true},
		};

		// Variables
		nbValues = model.intVar("nb shifts", 0, nbAvailableShifts, true);
		assignment = new IntVar[nbTasks];
		for (int i = 0; i < nbTasks; i++) {
			assignment[i] = model.intVar("t" + (i + 1), skilledShifts[i]);
		}

		// Constraints
		for (int t1 = 0; t1 < nbTasks; t1++) {
			for (int t2 = t1 + 1; t2 < nbTasks; t2++) {
				if (taskOverlaps[t1][t2]) {
					model.arithm(assignment[t1], "!=", assignment[t2]).post();
				}
			}
		}
		model.nValues(assignment, nbValues).post();
	}

	@Override
	public void configureSearch() {
		// bottom-up optimisation, then classical branching
		Solver r = model.getSolver();
		r.setSearch(inputOrderLBSearch(nbValues), minDomLBSearch(assignment));
		// displays the root lower bound
		r.plugMonitor(new IMonitorInitialize() {
			@Override
			public void afterInitialize(boolean correct) {
				out.println("bound after initial propagation : " + nbValues);
			}
		});
		r.plugMonitor((IMonitorSolution) () -> {
			bestObj = nbValues.getValue();
			out.println("Solution found! Objective = " + bestObj);
		});
		// searches for all optimal solutions (non-strict optimization)
		model.setObjective(true, nbValues);
	}

	@Override
	public void solve() {
		while (model.getSolver().solve()){
            solutions.add(new Solution(model).record());
        }

		int nb = 1;
		for(Solution s: solutions){
			System.out.println("Optimal solution : "+nb);
			for(int i=0;i<5;i++){
				System.out.println(assignment[i].getName()+" = "+s.getIntVal(assignment[i]));
			}nb++;
		}
	}

	public static void main(String[] args){
		new SMPTSP().execute(args);
	}
}
