/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainBest;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.variables.Largest;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.MathUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jean-Guillaume FAGES (cosling)
 * @since 05/04/2017.
 */
public class KnapsackTest {
	int N=50;
	int M=5;
	int[] c = new int[]{560, 1125, 300, 620, 2100, 431, 68, 328, 47, 122, 322, 196, 41, 25, 425, 4260, 416, 115, 82, 22, 631, 132, 420, 86, 42, 103, 215, 81, 91, 26, 49, 420, 316, 72, 71, 49, 108, 116, 90, 738, 1811, 430, 3060, 215, 58, 296, 620, 418, 47, 81};
	int[] b = new int[]{800, 650, 550, 550, 650};
	int[][] a = new int[][]{
		{40, 91, 10, 30, 160, 20, 3, 12, 3, 18, 9, 25, 1, 1, 10, 280, 10, 8, 1, 1, 49, 8, 21, 6, 1, 5, 10, 8, 2, 1, 0, 10, 42, 6, 4, 8, 0, 10, 1, 40, 86, 11, 120, 8, 3, 32, 28, 13, 2, 4},
		{16, 92, 41, 16, 150, 23, 4, 18, 6, 0, 12, 8, 2, 1, 0, 200, 20, 6, 2, 1, 70, 9, 22, 4, 1, 5, 10, 6, 4, 0, 4, 12, 8, 4, 3, 0, 10, 0, 6, 28, 93, 9, 30, 22, 0, 36, 45, 13, 2, 2},
		{38, 39, 32, 71, 80, 26, 5, 40, 8, 12, 30, 15, 0, 1, 23, 100, 0, 20, 3, 0, 40, 6, 8, 0, 6, 4, 22, 4, 6, 1, 5, 14, 8, 2, 8, 0, 20, 0, 0, 6, 12, 6, 80, 13, 6, 22, 14, 0, 1, 2},
		{8, 71, 30, 60, 200, 18, 6, 30, 4, 8, 31, 6, 3, 0, 18, 60, 21, 4, 0, 2, 32, 15, 31, 2, 2, 7, 8, 2, 8, 0, 2, 8, 6, 7, 1, 0, 0, 20, 8, 14, 20, 2, 40, 6, 1, 14, 20, 12, 0, 1},
		{38, 52, 30, 42, 170, 9, 7, 20, 0, 3, 21, 4, 1, 2, 14, 310, 8, 4, 6, 1, 18, 15, 38, 10, 4, 8, 6, 0, 0, 3, 0, 10, 6, 1, 3, 0, 3, 5, 4, 0, 30, 12, 16, 18, 3, 16, 22, 30, 4, 0}
	};

	@Test(groups="10s", timeOut=60000)
	public void knapsackTest() {
		Model m = new Model();
		BoolVar[] x = m.boolVarArray(N);
		IntVar[] bVar = new IntVar[M];
		for(int i=0;i<M;i++){
			bVar[i] = m.intVar(0,b[i]);
		}
		IntVar objective = m.intVar(0, N*MathUtils.max(c));
		m.setObjective(Model.MAXIMIZE,objective);
		m.scalar(x,c,"=",objective).post();
		for(int i=0;i<M;i++){
			m.knapsack(x,bVar[i],objective,a[i],c).post();
		}
		Solver s = m.getSolver();
		IntVar[] xCost = new IntVar[N];
		for(int i=0;i<N;i++)xCost[i] = m.intScaleView(x[i],c[i]);
		s.setSearch(Search.intVarSearch(new Largest(),new IntDomainMax(),xCost));
		while (s.solve());
		s.printShortStatistics();
		Assert.assertEquals(16537,s.getBestSolutionValue());
	}

	@Test(groups="10s", timeOut=60000)
	public void knapsackTestBestValue() {
		Model m = new Model();
		BoolVar[] x = m.boolVarArray(N);
		IntVar[] bVar = new IntVar[M];
		for(int i=0;i<M;i++){
			bVar[i] = m.intVar(0,b[i]);
		}
		IntVar objective = m.intVar(0, N*MathUtils.max(c));
		m.setObjective(Model.MAXIMIZE,objective);
		m.scalar(x,c,"=",objective).post();
		for(int i=0;i<M;i++){
			m.knapsack(x,bVar[i],objective,a[i],c).post();
		}
		Solver s = m.getSolver();
		IntVar[] xCost = new IntVar[N];
		for(int i=0;i<N;i++)xCost[i] = m.intScaleView(x[i],c[i]);
		s.setSearch(Search.intVarSearch(new Largest(),new IntDomainBest(),xCost));
		while (s.solve());
		s.printShortStatistics();
		Assert.assertEquals(16537,s.getBestSolutionValue());
	}
}
