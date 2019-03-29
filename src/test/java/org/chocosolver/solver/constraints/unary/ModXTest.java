/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

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
public class ModXTest {

	@Test(groups="1s", timeOut=60000)
	public void test2() {
		Model model = new Model();
		IntVar res = model.intVar("r", 1, 2, true);
		model.mod(res, 2, 1).post();
		try {
			model.getSolver().propagate();
			assertTrue(res.isInstantiatedTo(1));
		} catch (ContradictionException e) {
			fail();
		}
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
}
