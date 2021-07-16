/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN, Jean-Guillaume FAGES
 */
public class SumTest {


	@Test(groups = "1s", timeOut=60000)
	public void testNominal() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5});
		IntVar sum = model.intVar(0, 100);
		model.sum(setVar, sum).post();
		checkSolutions(model, setVar, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSum() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{1, 5, 7, 8});
		IntVar sum = model.intVar(0, 100);
		model.sum(setVar, sum).post();
		checkSolutions(model, setVar, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSumNeg() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{-10, -2}, new int[]{-10, -5, -2});
		IntVar sum = model.intVar(-20,10);
		model.sum(setVar, sum).post();
		checkSolutions(model, setVar, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSumNeg2() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{}, new int[]{-10, -5, -2, 0, 3, 6, 10});
		IntVar sum = model.intVar(-7);
		model.sum(setVar, sum).post();
		checkSolutions(model, setVar, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSumNegWrong() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{-10, -5, -8});
		IntVar sum = model.intVar(0, 100);
		model.sum(setVar, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}

	@Test(groups = "1s", timeOut=60000)
	public void testEmptySetKo() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{});
		IntVar sum = model.intVar(1, 100);
		model.sum(setVar, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}

	@Test(groups = "1s", timeOut=60000)
	public void testEmptySetOk() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{});
		IntVar sum = model.intVar(0);
		model.sum(setVar, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
		checkSolutions(model, setVar, sum);
	}


	@Test(groups = "1s", timeOut=60000)
	public void testWrongBounds() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 5});
		IntVar intVar = model.intVar(9, 100);
		model.sum(setVar, intVar).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}

    /* *******************************************
     * Helpers
    ********************************************/

	private void checkSolutions(Model model, SetVar var, IntVar sum) {
		int nbSol = 0;
		while(model.getSolver().solve()) {
			nbSol++;
			int computedSum = 0;
			for (Integer value : var.getValue()) {
				computedSum += value;
			}
			assertEquals(sum.getValue(), computedSum);
		}
		assertTrue(nbSol > 0);
	}
}
