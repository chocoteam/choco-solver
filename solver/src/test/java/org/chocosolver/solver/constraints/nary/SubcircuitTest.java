/*
 * This file is part of choco-solver, http://choco-solver.org/
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
 * Date: 12/06/12
 * Time: 21:29
 */

package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SubcircuitTest {

	@Test(groups="1s", timeOut=60000)
	public void test() {
		// test from Anthony Palmieri
		int[][] listAdj={
				{0,2},//0
				{1,2},  //1
				{0,1,2,3,4},//2
				{2,3},//3
				{2,4,5}};//4
		Model m = new Model();
		IntVar[] nodes = new IntVar[5];
		for (int i = 0; i < listAdj.length; i++) {
			nodes[i]=m.intVar(listAdj[i]);
		}

		IntVar start = m.intVar(0);
		IntVar end = m.intVar(4);
		m.subPath(nodes, start,end, 0, m.intVar(1,14)).post();

		int sol=0;
		while(m.getSolver().solve()){
			sol++;
		}

		m.getSolver().reset();
		int solAfterReset=0;
		while(m.getSolver().solve()){
			solAfterReset++;
		}
		Assert.assertEquals(sol,solAfterReset);
	}

	@Test(groups="1s", timeOut=60000)
	public static void test1() {
		Model model = new Model();
		IntVar[] x = model.intVarArray("x", 10, 0, 20, true);
		model.subCircuit(x, 0, model.intVar("length", 0, x.length - 1, true)).post();
		model.getSolver().solve();
		assertEquals(1, model.getSolver().getSolutionCount());
	}

	@Test(groups="1s", timeOut=60000)
	public static void test11() {
		Model model = new Model();
		IntVar[] x = model.intVarArray("x", 5, 0, 8, true);
		model.subCircuit(x, 0, model.intVar("length", 0, x.length - 1, true)).post();
		model.getSolver().findAllSolutions();
		assertEquals(61, model.getSolver().getSolutionCount());
	}

	@Test(groups="1s", timeOut=60000)
	public static void test2() {
		Model model = new Model();
		IntVar[] x = model.intVarArray("x", 5, 0, 4, true);
		IntVar[] y = model.intVarArray("y", 5, 5, 9, true);
		IntVar[] vars = append(x, y);
		model.subCircuit(vars, 0, model.intVar("length", 0, vars.length - 1, true)).post();
		model.getSolver().solve();
		assertTrue(model.getSolver().getSolutionCount() > 0);
	}

	@Test(groups="1s", timeOut=60000)
	public static void test3() {
		Model model = new Model();
		IntVar[] x = model.intVarArray("x", 5, 0, 4, false);
		IntVar[] y = model.intVarArray("y", 5, 5, 9, false);
		final IntVar[] vars = append(x, y);
		try {
			vars[1].removeValue(1, Null);
			vars[6].removeValue(6, Null);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		model.subCircuit(vars, 0, model.intVar("length", 0, vars.length - 1, true)).post();
		model.getSolver().solve();
		assertTrue(model.getSolver().getSolutionCount() == 0);
	}

	@Test(groups="1s", timeOut=60000)
	public static void test4() {
		Model model = new Model();
		int n = 6;
		int min = 2;
		int max = 4;
		IntVar[] vars = model.intVarArray("x", n, 0, n, true);
		IntVar nb = model.intVar("size", min, max, true);
		model.subCircuit(vars, 0, nb).post();
		model.getSolver().setSearch(Search.inputOrderLBSearch(vars));
		while (model.getSolver().solve()) ;
		int nbSol = 0;
		for (int i = min; i <= max; i++) {
			nbSol += parmi(i, n) * factorial(i - 1);
		}

		assertEquals(model.getSolver().getSolutionCount(), nbSol);
	}

	private static int factorial(int n) {
		if (n <= 1) {
			return 1;
		} else {
			return n * factorial(n - 1);
		}
	}

	private static int parmi(int k, int n) {
		return factorial(n) / (factorial(k) * factorial(n - k));
	}
}
