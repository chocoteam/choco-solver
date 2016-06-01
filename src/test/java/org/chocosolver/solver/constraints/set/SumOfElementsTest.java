/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
		SetVar setVar = model.setVar(new int[]{0,1,2,4});
		IntVar sum = model.intVar(0, 100);
		int[] weights = new int[]{2,2,2,3,2};
		model.sumElements(setVar, weights, sum).post();
		checkSolutions(model, setVar, weights, 0, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSumOffset() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{-1,0,1,3});
		IntVar sum = model.intVar(8);
		int[] weights = new int[]{2,2,2,3,2};
		model.sumElements(setVar, weights, -1, sum).post();
		checkSolutions(model, setVar, weights, -1, sum);
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSumOffsetKo() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{-1,0,1,3});
		IntVar sum = model.intVar(8);
		int[] weights = new int[]{2,2,2,3,2};
		model.sumElements(setVar, weights, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}

	@Test(groups = "1s", timeOut=60000)
	public void testFixedSumOffsetKo2() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{-1,0,1,2,4});
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
		SetVar setVar = model.setVar(new int[]{0,1});
		IntVar sum = model.intVar(0, 100);
		int[] weights = new int[]{-2,-3};
		model.sumElements(setVar, weights, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}

	@Test(groups = "1s", timeOut=60000)
	public void testEmptySetKo() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{});
		IntVar sum = model.intVar(1, 100);
		int[] weights = new int[]{2,-2};
		model.sumElements(setVar, weights, sum).post();

		assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
		assertFalse(model.getSolver().solve());
	}

	@Test(groups = "1s", timeOut=60000)
	public void testEmptySetOk() {
		Model model = new Model();
		SetVar setVar = model.setVar(new int[]{});
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
