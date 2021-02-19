/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.ImpactBased;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.function.Function;

/**
 * @author Jean-Guillaume Fages
 * @since 22/04/15
 * Created by IntelliJ IDEA.
 */
public class BlackBoxTest {

	@DataProvider
	public Object[][] strategies(){
		return new Object[][]{
				{(Function<IntVar[], AbstractStrategy<IntVar>>) vars -> new ImpactBased(vars, 2,3,10,0,true)},
				{(Function<IntVar[], AbstractStrategy<IntVar>>) Search::activityBasedSearch},
				{(Function<IntVar[], AbstractStrategy<IntVar>>) Search::domOverWDegSearch},
				{(Function<IntVar[], AbstractStrategy<IntVar>>) Search::conflictHistorySearch},
				{(Function<IntVar[], AbstractStrategy<IntVar>>) Search::domOverWDegRefSearch},
		};
	}

	@Test(groups="10s", timeOut=60000, dataProvider = "strategies")
	public void testCostas(Function<IntVar[], AbstractStrategy<IntVar>> strat) {
		Model model = ProblemMaker.makeCostasArrays(7);
		IntVar[] vars = model.retrieveIntVars(true);
		Solver solver = model.getSolver();
		solver.setSearch(strat.apply(vars));
		solver.setGeometricalRestart(vars.length * 3, 1.1d, new FailCounter(model, 0), 1000);
		solver.setNoGoodRecordingFromSolutions(vars);
		solver.findAllSolutions();
		solver.printShortStatistics();
		Assert.assertEquals(solver.getSolutionCount(), 100);
	}

	@Test(groups="10s", timeOut=60000, dataProvider = "strategies")
		public void testGolombRuler(Function<IntVar[], AbstractStrategy<IntVar>> strat) {
		Model model = ProblemMaker.makeGolombRuler(8);
		IntVar[] vars = model.retrieveIntVars(true);
		Solver solver = model.getSolver();
		solver.setSearch(strat.apply(vars));
		solver.setGeometricalRestart(vars.length * 3, 1.1d, new FailCounter(model, 0), 1000);
		solver.setNoGoodRecordingFromSolutions(vars);
		solver.findOptimalSolution((IntVar) model.getHook("objective"), false);
		solver.printShortStatistics();
		Assert.assertEquals(solver.getObjectiveManager().getBestSolutionValue(), 34);
	}

	@Test(groups="10s", timeOut=60000, dataProvider = "strategies")
	public void testNQueen(Function<IntVar[], AbstractStrategy<IntVar>> strat) {
		Model model = ProblemMaker.makeNQueenWithBinaryConstraints(10);
		IntVar[] vars = model.retrieveIntVars(true);
		Solver solver = model.getSolver();
		solver.setSearch(strat.apply(vars));
		solver.setGeometricalRestart(vars.length * 3, 1.1d, new FailCounter(model, 0), 1000);
		solver.setNoGoodRecordingFromSolutions(vars);
		solver.findAllSolutions();
		solver.printShortStatistics();
		Assert.assertEquals(solver.getSolutionCount(), 724);
	}
}
