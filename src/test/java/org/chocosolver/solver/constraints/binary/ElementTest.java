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
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;
import static org.chocosolver.solver.constraints.binary.element.ElementFactory.detect;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;
import static org.chocosolver.util.tools.ArrayUtils.flatten;
import static org.chocosolver.util.tools.ArrayUtils.toArray;
import static org.testng.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 31/10/11
 * Time: 14:22
 */
public class ElementTest {

	@DataProvider(name = "params")
	public Object[][] data1D(){
		// indicates whether to use explanations or not
		List<Object[]> elt = new ArrayList<>();
		elt.add(new Object[]{true});
		elt.add(new Object[]{false});
		return elt.toArray(new Object[elt.size()][1]);
	}

	private static void model(Model s, IntVar index, int[] values, IntVar var,
							  int offset, int nbSol) {

		s.element(var, values, index, offset).post();

		IntVar[] allvars = toArray(index, var);

		Solver r = s.getSolver();
		r.set(randomSearch(allvars, currentTimeMillis()));
		while (s.solve()) ;
		assertEquals(r.getMeasures().getSolutionCount(), nbSol, "nb sol");
	}


	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void test1(boolean exp) {
		Model s = new Model();
		if(exp){
			s.getSolver().setCBJLearning(false,false);
		}
		int[] values = new int[]{1, 2, 0, 4, 3};
		IntVar index = s.intVar("v_0", -3, 10, false);
		IntVar var = s.intVar("v_1", -20, 20, false);
		model(s, index, values, var, 0, 5);
	}


	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void testNeg(boolean exp) {
		Model s = new Model();
		if(exp){
			s.getSolver().setCBJLearning(false,false);
		}
		int[] values = new int[]{1, 2, 0, 4, 3};
		IntVar index = s.intVar("v_0", -3, 10, false);
		IntVar var = s.intVar("v_1", -20, 20, false);
		BoolVar b = s.element(var, values, index).reify();
		while (s.solve()) ;
	}

	@Test(groups="1s", timeOut=60000)
	public void testProp1() {
		Model s = new Model();
		int[] values = new int[]{1, 2, 0, 4, 3};
		IntVar index = s.intVar(0);
		IntVar var = s.intVar(1);
		Constraint c = s.element(var, values, index);
		assertEquals(ESat.TRUE, c.isSatisfied());
	}

	@Test(groups="1s", timeOut=60000)
	public void testProp2() {
		Model s = new Model();
		int[] values = new int[]{1, 2, 0, 4, 3};
		IntVar index = s.intVar(0);
		IntVar var = s.intVar(2);
		Constraint c = s.element(var, values, index);
		assertEquals(ESat.FALSE, c.isSatisfied());
	}

	@Test(groups="1s", timeOut=60000)
	public void testProp3() {
		Model s = new Model();
		int[] values = new int[]{1, 2, 0, 4, 3};
		IntVar index = s.intVar(-1);
		IntVar var = s.intVar(2);
		Constraint c = s.element(var, values, index);
		assertEquals(ESat.FALSE, c.isSatisfied());
	}

	@Test(groups="1s", timeOut=60000)
	public void testProp4() {
		Model s = new Model();
		int[] values = new int[]{1, 2, 0, 4, 3};
		IntVar index = s.intVar(12);
		IntVar var = s.intVar(2);
		Constraint c = s.element(var, values, index);
		assertEquals(ESat.FALSE, c.isSatisfied());
	}

	@Test(groups="1s", timeOut=60000)
	public void testProp5() {
		Model s = new Model();
		int[] values = new int[]{1, 2, 0, 4, 3};
		IntVar index = s.intVar(-1,8);
		IntVar var = s.intVar(1,2);
		Constraint c = s.element(var, values, index);
		assertEquals(ESat.UNDEFINED, c.isSatisfied());
	}

	@Test(groups="1s", timeOut=60000)
	public void testProp6() {
		Model s = new Model();
		int[] values = new int[]{1, 2, 0, 4};
		IntVar index = s.intVar(0,8);
		IntVar var = s.intVar(3);
		Constraint c = s.element(var, values, index);
		assertEquals(ESat.FALSE, c.isSatisfied());
	}

	@Test(groups="1s", timeOut=60000)
	public void testProp7() {
		Model s = new Model();
		int[] values = new int[]{1, 2, 0, 4};
		IntVar index = s.intVar(new int[]{-1,0,2,3});
		IntVar var = s.intVar(new int[]{-1,2,3,6});
		Constraint c = s.element(var, values, index);
		assertEquals(ESat.FALSE, c.isSatisfied());
	}

	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void test2(boolean exp) {
		Model s = new Model();
		if(exp){
			s.getSolver().setCBJLearning(false,false);
		}
		int[] values = new int[]{1, 2, 0, 4, 3};
		IntVar index = s.intVar("v_0", 2, 10, false);
		IntVar var = s.intVar("v_1", -20, 20, false);
		model(s, index, values, var, 0, 3);
	}

	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void test3(boolean exp) {
		for (int j = 0; j < 100; j++) {
			Random r = new Random(j);
			Model s = new Model();
			if(exp){
				s.getSolver().setCBJLearning(false,false);
			}
			IntVar index = s.intVar("v_0", 23, 25, false);
			IntVar val = s.intVar("v_1", 0, 1, true);
			int[] values = new int[24];
			for (int i = 0; i < values.length; i++) {
				values[i] = r.nextInt(2);
			}
			model(s, index, values, val, 0, 1);
		}
	}

	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void test4(boolean exp) {
		Model s = new Model();
		if(exp){
			s.getSolver().setCBJLearning(false,false);
		}
		int[] values = new int[]{0, 0, 1};
		IntVar index = s.intVar("v_0", 1, 3, false);
		IntVar var = s.intVar("v_1", 0, 1, false);
		model(s, index, values, var, 1, 3);
	}

	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void test5(boolean exp) {
		Model s = new Model();
		if(exp){
			s.getSolver().setCBJLearning(false,false);
		}

		Random r = new Random(125);
		int[] values = new int[10];
		for (int i = 0; i < values.length; i++) {
			values[i] = r.nextInt(5);
		}

		IntVar[] vars = new IntVar[3];
		IntVar[] indices = new IntVar[3];

		for (int i = 0; i < vars.length; i++) {
			vars[i] = s.intVar("v_" + i, 0, 10, false);
			indices[i] = s.intVar("i_" + i, 0, values.length - 1, false);
			s.element(vars[i], values, indices[i], 0).post();
		}

		for (int i = 0; i < vars.length - 1; i++) {
			s.arithm(vars[i], ">", vars[i + 1]).post();
		}

		while (s.solve()) ;
		assertEquals(s.getSolver().getSolutionCount(), 58, "nb sol");
	}

	public void nasty(int seed, int nbvars, int nbsols, boolean exp) {

		Random r = new Random(seed);
		int[] values = new int[nbvars];
		for (int i = 0; i < values.length; i++) {
			values[i] = r.nextInt(nbvars);
		}


		Model ref = new Model();
		IntVar[] varsr = new IntVar[nbvars];
		IntVar[] indicesr = new IntVar[nbvars];

		for (int i = 0; i < varsr.length; i++) {
			varsr[i] = ref.intVar("v_" + i, 0, nbvars, false);
			indicesr[i] = ref.intVar("i_" + i, 0, nbvars, false);
		}
		IntVar[] allvarsr = flatten(toArray(varsr, indicesr));
		ref.getSolver().set(randomSearch(allvarsr, seed));

		for (int i = 0; i < varsr.length - 1; i++) {
			ref.element(varsr[i], values, indicesr[i], 0).post();
			ref.arithm(varsr[i], "+", indicesr[i + 1], "=", 2 * nbvars / 3).post();
		}
		if(exp){
			ref.getSolver().setCBJLearning(false,false);
		}

		while (ref.solve()) ;

		assertEquals(ref.getSolver().getSolutionCount(), nbsols);
	}


	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void testBUG(boolean exp) {
		nasty(153, 15, 192, exp);
	}


	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void testInc1(boolean exp) {
		for (int i = 0; i < 20; i++) {
			Model model = new Model();
			IntVar I = model.intVar("I", 0, 5, false);
			IntVar R = model.intVar("R", 0, 10, false);
			model.element(R, new int[]{0, 2, 4, 6, 7}, I).post();
			model.getSolver().set(randomSearch(new IntVar[]{I, R}, i));
			if(exp){
				model.getSolver().setCBJLearning(false,false);
			}
			while (model.solve()) ;
			assertEquals(model.getSolver().getSolutionCount(), 5);
		}
	}

	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void testDec1(boolean exp) {
		for (int i = 0; i < 20; i++) {
			Model model = new Model();
			IntVar I = model.intVar("I", 0, 5, false);
			IntVar R = model.intVar("R", 0, 10, false);
			model.element(R, new int[]{7, 6, 4, 2, 0}, I).post();
			model.getSolver().set(randomSearch(new IntVar[]{I, R}, i));
			if(exp){
				model.getSolver().setCBJLearning(false,false);
			}
			while (model.solve()) ;
			assertEquals(model.getSolver().getSolutionCount(), 5);
		}
	}

	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void testReg1(boolean exp) {
		for (int i = 0; i < 20; i++) {
			Model model = new Model();
			IntVar I = model.intVar("I", 0, 13, false);
			IntVar R = model.intVar("R", 0, 21, false);
			model.element(R, new int[]{1, 6, 20, 4, 15, 13, 9, 3, 19, 12, 17, 7, 17, 5}, I).post();
			model.getSolver().set(randomSearch(new IntVar[]{I, R}, i));
			if(exp){
				model.getSolver().setCBJLearning(false,false);
			}
			while (model.solve()) ;
			assertEquals(model.getSolver().getSolutionCount(), 14);
		}
	}

	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void testTAR1(boolean exp) {
		for (int i = 1; i < 20; i++) {
			Model model = new Model();
			IntVar I = model.intVar("I", 0, 3, true);
			IntVar R = model.intVar("R", -1, 0, false);
			model.element(R, new int[]{-1, -1, -1, 0, -1}, I, -1).post();
			model.getSolver().set(randomSearch(new IntVar[]{I, R}, i));
			if(exp){
				model.getSolver().setCBJLearning(false,false);
			}
			while (model.solve()) ;
			assertEquals(model.getSolver().getSolutionCount(), 4);
		}
	}
	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void testModelOrMin(boolean exp) {
		Model s = new Model();
		IntVar val = s.intVar("v", 0, 9, true);
		// b=> val={5,6,7,8}[2]
		Constraint el = detect(val, new int[]{5, 6, 7, 8}, s.intVar(2), 0);
		s.or(el.reify()).post();
		if(exp){
			s.getSolver().setCBJLearning(false,false);
		}
		// s.post(el);// works instead of previous post
		while (s.solve()) ;
		assertEquals(s.getSolver().getSolutionCount(), 1L);
	}


	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void testModelOrFull(boolean exp) {
		Model s = new Model();
		BoolVar b = s.boolVar("b");
		IntVar val = s.intVar("v", 0, 9, true);
		// b=> val={5,6,7,8}[2]
		Constraint el = detect(val, new int[]{5, 6, 7, 8}, s.intVar(2), 0);
		s.or(b.not(), el.reify()).post();
		// !b=> val=2
		Constraint affect = s.arithm(val, "=", 2);
		s.or(b, affect.reify()).post();
		if(exp){
			s.getSolver().setCBJLearning(false,false);
		}
		while (s.solve()) ;
		assertEquals(s.getSolver().getSolutionCount(), 2L);
	}

}