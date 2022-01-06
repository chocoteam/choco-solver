/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.search.strategy.selectors.variables.RandomVar;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jean-Guillaume FAGES (cosling)
 * @since 06/04/2019.
 */
public class RandomVarSelectorTest {

	@Test(groups = "10s", timeOut = 60000)
	public void test() throws Exception {
		int n = 2000;
		modelAndSolveAll(true,n);// to avoid JVM effect on result
		long time = System.currentTimeMillis();
		long nbSols1 = modelAndSolveAll(true,n);
		System.out.println("new random runtime : "+(System.currentTimeMillis()-time)+"ms");
		time = System.currentTimeMillis();
		long nbSols2 = modelAndSolveAll(false,n);
		System.out.println("old random runtime : "+(System.currentTimeMillis()-time)+"ms");
		System.out.println("enumerating "+nbSols1+" solutions");
		Assert.assertEquals(nbSols1,nbSols2);

	}

	private long modelAndSolveAll(boolean newRandom, int n){
		Model m = new Model();
		IntVar[] X = m.intVarArray(n,0,3,true);
		m.sum(X,"=",2*n).post();
		Solver s = m.getSolver();
		VariableSelector rdvs = newRandom?new RandomVar(0,X):new Random(0);
		s.setSearch(Search.intVarSearch(rdvs,new IntDomainMin(),X));
		s.setRestartOnSolutions();
		s.limitSolution(300);
		while (s.solve());
		return s.getSolutionCount();
	}
}
