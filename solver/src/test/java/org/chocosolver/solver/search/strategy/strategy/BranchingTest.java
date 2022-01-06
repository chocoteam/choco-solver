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
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jean-Guillaume FAGES (cosling)
 * @since 18/01/2019.
 */
public class BranchingTest {

	@Test(groups="1s", timeOut=60000)
	public void testDefault(){
		Model m = new Model();
		IntVar x = m.intVar(1,3, true);
		IntVar y = m.intVar(1,3, true);
		Solver s = m.getSolver();
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 9);
	}

	/* Assertion removed because of portfolio (bound update at any time)
	@Test(groups="1s", timeOut=60000, expectedExceptions = AssertionError.class)
	public void testBoundedEqOut(){
		Model m = new Model();
		IntVar x = m.intVar(1,3, true);
		IntVar y = m.intVar(1,3, true);
		Solver s = m.getSolver();
		s.setSearch(Search.intVarSearch(new InputOrder<>(m),
				i -> 0,
				DecisionOperatorFactory.makeIntEq(),
				x, y));
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 9);
	}*/

	@Test(groups="1s", timeOut=60000)
	public void testBoundedEqLB(){
		Model m = new Model();
		IntVar x = m.intVar(1,3, true);
		IntVar y = m.intVar(1,3, true);
		Solver s = m.getSolver();
		s.setSearch(Search.intVarSearch(new InputOrder<>(m),
				IntVar::getLB,
				DecisionOperatorFactory.makeIntEq(),
				x, y));
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 9);
	}

	/*
	@Test(groups="1s", timeOut=60000, expectedExceptions = AssertionError.class)
	public void testBoundedEqMiddle(){
		Model m = new Model();
		IntVar x = m.intVar(1,3, true);
		IntVar y = m.intVar(1,3, true);
		Solver s = m.getSolver();
		s.setSearch(Search.intVarSearch(new InputOrder<>(m),
				var -> (var.getLB()+var.getUB())/2,
				DecisionOperatorFactory.makeIntEq(),
				x, y));
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 9);
	}*/

	@Test(groups="1s", timeOut=60000)
	public void testBoundedNeqLB(){
		Model m = new Model();
		IntVar x = m.intVar(1,3, true);
		IntVar y = m.intVar(1,3, true);
		Solver s = m.getSolver();
		s.setSearch(Search.intVarSearch(new InputOrder<>(m),
				IntVar::getLB,
				DecisionOperatorFactory.makeIntNeq(),
				x, y));
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 9);
	}

	@Test(groups="1s", timeOut=60000, expectedExceptions = AssertionError.class)
	public void testBoundedNeqMiddle(){
		Model m = new Model();
		IntVar x = m.intVar(1,3, true);
		IntVar y = m.intVar(1,3, true);
		Solver s = m.getSolver();
		s.setSearch(Search.intVarSearch(new InputOrder<>(m),
				var -> (var.getLB()+var.getUB())/2,
				DecisionOperatorFactory.makeIntNeq(),
				x, y));
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 9);
	}

	@Test(groups="1s", timeOut=60000)
	public void testSplitOk(){
		Model m = new Model();
		IntVar x = m.intVar(1,3);
		IntVar y = m.intVar(1,3);
		Solver s = m.getSolver();
		s.setSearch(Search.intVarSearch(new InputOrder<>(m),
				var -> (var.getLB()+var.getUB())/2,
				DecisionOperatorFactory.makeIntSplit(),
				x, y));
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 9);
	}

	@Test(groups="1s", timeOut=60000, expectedExceptions = AssertionError.class)
	public void testSplitKo(){
		Model m = new Model();
		IntVar x = m.intVar(1,3);
		IntVar y = m.intVar(1,3);
		Solver s = m.getSolver();
		s.setSearch(Search.intVarSearch(new InputOrder<>(m),
				var -> (int)Math.ceil((var.getLB()+var.getUB())/2.0),
				DecisionOperatorFactory.makeIntSplit(),
				x, y));
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 9);
	}

	@Test(groups="1s", timeOut=60000)
	public void testRevSplitOk(){
		Model m = new Model();
		IntVar x = m.intVar(1,3);
		IntVar y = m.intVar(1,3);
		Solver s = m.getSolver();
		s.setSearch(Search.intVarSearch(new InputOrder<>(m),
				var -> (int)Math.ceil((var.getLB()+var.getUB())/2.0),
				DecisionOperatorFactory.makeIntReverseSplit(),
				x, y));
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 9);
	}

	@Test(groups="1s", timeOut=60000, expectedExceptions = AssertionError.class)
	public void testRevSplitKo(){
		Model m = new Model();
		IntVar x = m.intVar(1,3);
		IntVar y = m.intVar(1,3);
		Solver s = m.getSolver();
		s.setSearch(Search.intVarSearch(new InputOrder<>(m),
				var -> (var.getLB()+var.getUB())/2,
				DecisionOperatorFactory.makeIntReverseSplit(),
				x, y));
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 9);
	}

	@Test(groups="1s", timeOut=60000, expectedExceptions = AssertionError.class)
	public void testSetForceOut(){
		Model m = new Model();
		SetVar x = m.setVar(new int[0], ArrayUtils.array(1,3));
		Solver s = m.getSolver();
		s.setSearch(Search.setVarSearch(new InputOrder<>(m),
				v -> 0,
				true,
				x));
		while (s.solve());
	}

	@Test(groups="1s", timeOut=60000, expectedExceptions = AssertionError.class)
	public void testSetForceAlready(){
		Model m = new Model();
		SetVar x = m.setVar(new int[]{1}, ArrayUtils.array(1,3));
		Solver s = m.getSolver();
		s.setSearch(Search.setVarSearch(new InputOrder<>(m),
				v -> 1,
				true,
				x));
		while (s.solve());
	}

	@Test(groups="1s", timeOut=60000)
	public void testSetForceOk(){
		Model m = new Model();
		SetVar x = m.setVar(new int[0], ArrayUtils.array(1,3));
		Solver s = m.getSolver();
		s.setSearch(Search.setVarSearch(new InputOrder<>(m),
				v -> {
					for(int i:v.getUB()) if (!v.getLB().contains(i)) return i;
					throw new UnsupportedOperationException();
				},
				true, x));
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 8);
	}

	@Test(groups="1s", timeOut=60000, expectedExceptions = AssertionError.class)
	public void testSetRemoveeOut(){
		Model m = new Model();
		SetVar x = m.setVar(new int[0], ArrayUtils.array(1,3));
		Solver s = m.getSolver();
		s.setSearch(Search.setVarSearch(new InputOrder<>(m),
				v -> 0,
				false,
				x));
		while (s.solve());
	}

	@Test(groups="1s", timeOut=60000, expectedExceptions = AssertionError.class)
	public void testSetRemoveAlready(){
		Model m = new Model();
		SetVar x = m.setVar(new int[]{1}, ArrayUtils.array(1,3));
		Solver s = m.getSolver();
		s.setSearch(Search.setVarSearch(new InputOrder<>(m),
				v -> 1,
				false,
				x));
		while (s.solve());
	}

	@Test(groups="1s", timeOut=60000)
	public void testSetRemoveOk(){
		Model m = new Model();
		SetVar x = m.setVar(new int[0], ArrayUtils.array(1,3));
		Solver s = m.getSolver();
		s.setSearch(Search.setVarSearch(new InputOrder<>(m),
				v -> {
					for(int i:v.getUB()) if (!v.getLB().contains(i)) return i;
					throw new UnsupportedOperationException();
				},
				false, x));
		while (s.solve());
		Assert.assertEquals(s.getSolutionCount(), 8);
	}
}
