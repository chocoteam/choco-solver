/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
public class SumOfElementsTest {

	@Test(groups = "1s", timeOut=60000)
	public void testNominal() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5});
		int[] weights = new int[]{0, 10, 20, 3, 12, 9};
		IntVar sum = model.intVar(0, 100);
		model.sumElements(setVar, weights, sum).post();
		checkSolutions(model, setVar, weights, 0, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSum() {
		Model model = new Model();
		SetVar setVar = model.setVar(0,1,2,4);
		IntVar sum = model.intVar(0, 100);
		int[] weights = new int[]{2,2,2,3,2};
		model.sumElements(setVar, weights, sum).post();
		checkSolutions(model, setVar, weights, 0, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSumOffset() {
		Model model = new Model();
		SetVar setVar = model.setVar(-1,0,1,3);
		IntVar sum = model.intVar(8);
		int[] weights = new int[]{2,2,2,3,2};
		model.sumElements(setVar, weights, -1, sum).post();
		checkSolutions(model, setVar, weights, -1, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSumOffsetKo() {
		Model model = new Model();
		SetVar setVar = model.setVar(-1,0,1,3);
		IntVar sum = model.intVar(8);
		int[] weights = new int[]{2,2,2,3,2};
		model.sumElements(setVar, weights, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSumOffsetKo2() {
		Model model = new Model();
		SetVar setVar = model.setVar(-1,0,1,2,4);
		IntVar sum = model.intVar(8);
		int[] weights = new int[]{2,2,2,3,2};
		model.sumElements(setVar, weights, -1, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSumNeg() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{}, new int[]{0,1,2,3});
		IntVar sum = model.intVar(-7);
		int[] weights = new int[]{-5,-2,0,1};
		model.sumElements(setVar, weights, sum).post();
		checkSolutions(model, setVar, weights, 0, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSumNegWrong() {
		Model model = new Model();
		SetVar setVar = model.setVar(0,1);
		IntVar sum = model.intVar(0, 100);
		int[] weights = new int[]{-2,-3};
		model.sumElements(setVar, weights, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}

	@Test(groups = "1s", timeOut=60000)
	public void testEmptySetKo() {
		Model model = new Model();
		SetVar setVar = model.setVar();
		IntVar sum = model.intVar(1, 100);
		int[] weights = new int[]{2,-2};
		model.sumElements(setVar, weights, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}

	@Test(groups = "1s", timeOut=60000)
	public void testEmptySetOk() {
		Model model = new Model();
		SetVar setVar = model.setVar();
		IntVar sum = model.intVar(0);
		int[] weights = new int[]{0, 10, 20, 3, 12, 9};
		model.sumElements(setVar, weights, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
		checkSolutions(model, setVar, weights, 0, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testEmptySetOk2() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{}, new int[]{1,2});
		IntVar sum = model.intVar(0);
		int[] weights = new int[]{10, 10, 20, 3, 12, 9};
		model.sumElements(setVar, weights, sum).post();

		checkSolutions(model, setVar, weights, 0, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testEmptySetOk3() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{}, new int[]{1,2});
		IntVar sum = model.intVar(0);
		int[] weights = new int[]{};
		model.sumElements(setVar, weights, sum).post();

		checkSolutions(model, setVar, weights, 0, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testEmptySetKo3() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{1}, new int[]{1,2});
		IntVar sum = model.intVar(0);
		int[] weights = new int[]{};
		model.sumElements(setVar, weights, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}


	@Test(groups = "1s", timeOut=60000)
	public void testWrongBounds() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2});
		IntVar sum = model.intVar(-9, -1);
		int[] weights = new int[]{0, 10, 20};
		model.sumElements(setVar, weights, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}

       /* *******************************************
        * Helpers
       ********************************************/

	private void checkSolutions(Model model, SetVar var, int[] weights, int offset, IntVar sum) {
		int nbSol = 0;
		while(model.getSolver().solve()) {
			nbSol++;
			int computedSum = 0;
			for (Integer value : var.getValue()) {
				computedSum += weights[value-offset];
			}
			assertEquals(sum.getValue(), computedSum);
		}
		assertTrue(nbSol > 0);
	}
}
