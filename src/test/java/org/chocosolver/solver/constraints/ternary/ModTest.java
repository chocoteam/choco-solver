/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.VariableUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/07/12
 */
public class ModTest extends AbstractTernaryTest {

	@Override
	protected int validTuple(int vx, int vy, int vz) {
		return (vy != 0 && vz == vx - vy * (vx / vy)) ? 1 : 0;
	}

	@Override
	protected Constraint make(IntVar[] vars, Model s) {
		return s.mod(vars[0], vars[1], vars[2]);
	}

	@Test(groups="1s", timeOut=60000)
	public void test2() {
		Model model = new Model();
		IntVar res = model.intVar("r", 1, 2, true);
		model.mod(res, model.intVar(2), model.intVar(1)).post();
		try {
			model.getSolver().propagate();
			assertTrue(res.isInstantiatedTo(1));
		} catch (ContradictionException e) {
			fail();
		}
	}

	@Test(groups="1s", timeOut=60000)
	public void testJL() {
		Model s = new Model();
		IntVar dividend = s.intVar("dividend", 2, 3, false);
		IntVar divisor = s.intVar(1);
		IntVar remainder = s.intVar("remainder", 1, 2, false);
		s.mod(dividend, divisor, remainder).getOpposite().post();
		Solver r = s.getSolver();
		r.setSearch(inputOrderLBSearch(dividend, divisor, remainder));
		s.getSolver().solve();
	}

	@Test(groups="1s", timeOut=60000)
	public void testJT1(){
		Model model = new Model("model");
		IntVar a = model.intVar("a", 2,6);
		IntVar b = model.intVar("b", 2);
		int[] newVarBounds;
		newVarBounds = VariableUtils.boundsForModulo(a, b);
//		System.out.println("newVarBounds[0] = " + newVarBounds[0] + ", newVarBounds[1]="+ newVarBounds[1]);
		IntVar c = model.intVar("c", newVarBounds[0],newVarBounds[1]);
		model.mod(a, b, c).post();
		Assert.assertEquals(model.getSolver().findAllSolutions().size(), 5);
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod1Var(){
		Model model = new Model("model");
		IntVar x = model.intVar("x", 0,9);
		model.mod(x, 2, 1).post();
		Assert.assertEquals(model.getSolver().findAllSolutions().size(), 5);
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod1VarPropag() throws ContradictionException {
		Model model = new Model("model");
		IntVar x = model.intVar("x", 0,9);
		model.mod(x, 4, 1).post();
		model.getSolver().propagate();
		Assert.assertEquals(x.getDomainSize(), 3);
		Assert.assertEquals(x.getLB(), 1);
		assertTrue(x.contains(5));
		Assert.assertEquals(x.getUB(), 9);
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod2Var(){
		Model model = new Model("model");
		IntVar x = model.intVar("x", 0,9);
		IntVar z = model.intVar("z", 0, 9);
		model.mod(x, 2, z).post();
		Assert.assertEquals(model.getSolver().findAllSolutions().size(), 10);
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod2Var2(){
		Model model = new Model("model");
		IntVar x = model.intVar("x", 0,9);
		IntVar z = model.intVar("z", 0, 9);
		model.mod(x, 2, z).post();
		model.mod(z, 2, 1).post();
		Assert.assertEquals(model.getSolver().findAllSolutions().size(), 5);
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod2VarPropag() throws ContradictionException {
		Model model = new Model("model");
		IntVar x = model.intVar("x", new int[]{0, 2, 3, 5});
		IntVar z = model.intVar("z", 1,3);
		model.mod(x, 3, z).post();
		model.getSolver().propagate();
		Assert.assertTrue(z.isInstantiatedTo(2));
		Assert.assertEquals(x.getDomainSize(), 2);
		Assert.assertEquals(x.getLB(), 2);
		Assert.assertEquals(x.getUB(), 5);
	}
}
