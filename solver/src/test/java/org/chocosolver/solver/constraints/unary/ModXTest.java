/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * <br/>
 *
 * @author Arthur Godet
 * @since 29/03/2019
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

	@Test(groups="1s", timeOut=60000, expectedExceptions = SolverException.class)
	public void testMod1VarZeroDiv() {
		Model model = new Model("model");
		IntVar x = model.intVar("x", 0,9);
		model.mod(x, 0, 1).post();
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod3VarsIntoMember() {
		Model model = new Model("model");
		IntVar x = model.intVar("x", 0, 9);
		Constraint cstr = model.mod(x, 3, 0);
		Assert.assertTrue(cstr.getPropagator(0) instanceof PropMember);
	}
}
