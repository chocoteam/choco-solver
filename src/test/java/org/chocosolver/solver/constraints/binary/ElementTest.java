/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;
import static org.chocosolver.solver.constraints.binary.element.ElementFactory.detect;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
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
		r.setSearch(randomSearch(allvars, currentTimeMillis()));
		while (s.getSolver().solve()) ;
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
		while (s.getSolver().solve()) ;
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
	public void testMemberReasoning(){
		Model model = new Model();
		int[] table = new int[]{15, 50, 700};
		IntVar indexVar = model.intVar("index", 0, 2000, false);
		IntVar valueVar = model.intVar("value", 1, 2000, false); // enumerated
		model.element(valueVar, table, indexVar).post();
		System.out.println("before filtering : "+valueVar);
		try {
			model.getSolver().propagate();
		} catch(ContradictionException e) {
			e.printStackTrace();
		}
		System.out.println("after filtering : "+valueVar);
		Assert.assertEquals(valueVar.getDomainSize(),3);
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

	@Test(groups="1s", timeOut=60000)
	public void testProp8() {
		Model s = new Model();
		int[] values = new int[]{1, 2, 1, 1};
		IntVar index = s.intVar(new int[]{0,2,3});
		IntVar var = s.intVar(new int[]{1});
		Constraint c = s.element(var, values, index);
		assertEquals(ESat.TRUE, c.isSatisfied());
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

		while (s.getSolver().solve()) ;
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
		ref.getSolver().setSearch(randomSearch(allvarsr, seed));

		for (int i = 0; i < varsr.length - 1; i++) {
			ref.element(varsr[i], values, indicesr[i], 0).post();
			ref.arithm(varsr[i], "+", indicesr[i + 1], "=", 2 * nbvars / 3).post();
		}
		if(exp){
			ref.getSolver().setCBJLearning(false,false);
		}

		while (ref.getSolver().solve()) ;

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
			model.getSolver().setSearch(randomSearch(new IntVar[]{I, R}, i));
			if(exp){
				model.getSolver().setCBJLearning(false,false);
			}
			while (model.getSolver().solve()) ;
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
			model.getSolver().setSearch(randomSearch(new IntVar[]{I, R}, i));
			if(exp){
				model.getSolver().setCBJLearning(false,false);
			}
			while (model.getSolver().solve()) ;
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
			model.getSolver().setSearch(randomSearch(new IntVar[]{I, R}, i));
			if(exp){
				model.getSolver().setCBJLearning(false,false);
			}
			while (model.getSolver().solve()) ;
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
			model.getSolver().setSearch(randomSearch(new IntVar[]{I, R}, i));
			if(exp){
				model.getSolver().setCBJLearning(false,false);
			}
			while (model.getSolver().solve()) ;
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
		while (s.getSolver().solve()) ;
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
		while (s.getSolver().solve()) ;
		assertEquals(s.getSolver().getSolutionCount(), 2L);
	}

	/**
	 * In this case, the element factory maps the whole element constraint to
	 * an arithmetic constraint (ElementFactory, line: 68) but forgets to care
	 * about the value of `Index`.
	 *
	 * This could be fixed in constant time ensuring that
	 * 0 <= index.getLB() - offset
	 *
	 * @throws ContradictionException never
	 */
	@Test
	public void improveElement1() throws ContradictionException {
		Model choco = new Model();

		IntVar[] values = new IntVar[]{
				choco.intVar(new int[]{3})
		};

		IntVar   index  = choco.intVar(new int[]{-4, 0});
		IntVar   value  = choco.intVar(new int[]{-1, 0, 3});

		choco.element(value, values, index, 0).post();
		choco.getSolver().propagate();

		System.out.println("values = " + Arrays.toString(values));
		System.out.println("index  = " + index);
		System.out.println("value  = " + value);

		// FAILS !
		Assert.assertTrue(index.isInstantiatedTo(0));
	}

	/**
	 * This test is similar to `improveElement1` but shows that the upper
	 * bound of the `index` variable is not taken into account either.
	 *
	 * This could be fixed in constant time ensuring that
	 * values.length > index.getUB() - offset
	 *
	 * @throws ContradictionException never
	 */
	@Test
	public void improveElement2() throws ContradictionException {
		Model choco = new Model();

		IntVar[] values = new IntVar[]{choco.intVar(3)};
		IntVar   index  = choco.intVar(new int[]{0, 45});
		IntVar   value  = choco.intVar(new int[]{-1, 0, 3});

		choco.element(value, values, index, 0).post();
		choco.getSolver().propagate();

		System.out.println("values = " + Arrays.toString(values));
		System.out.println("index  = " + index);
		System.out.println("value  = " + value);

		// FAILS !
		Assert.assertTrue(index.isInstantiatedTo(0));
	}

	/**
	 * This test is similar to the above but shows that problem also occurs
	 * when the `values` array consists of one single primitive int.
	 *
	 * This could be fixed in constant time ensuring that
	 * 0 <= index.getLB() - offset
	 * and
	 * values.length > index.getUB() - offset
	 *
	 * @throws ContradictionException never
	 */
	@Test
	public void improveElement3() throws ContradictionException {
		Model choco = new Model();

		int[]   values = new int[]{3};
		IntVar   index  = choco.intVar(new int[]{-7, 0, 45});
		IntVar   value  = choco.intVar(new int[]{-1, 0, 3});

		choco.element(value, values, index, 0).post();
		choco.getSolver().propagate();

		System.out.println("values = " + Arrays.toString(values));
		System.out.println("index  = " + index);
		System.out.println("value  = " + value);

		// FAILS !
		Assert.assertTrue(index.isInstantiatedTo(0));
	}

	/**
	 * This test is similar to the above but shows that problem also occurs
	 * when the `values` array consists of one single primitive int.
	 *
	 * This could be fixed in constant time ensuring that
	 * 0 <= index.getLB() - offset
	 * and
	 * values.length > index.getUB() - offset
	 *
	 * @throws ContradictionException never
	 */
	@Test
	public void itIsAlreadyDoneWhenThereIsMoreThanOnePrimitiveInt() throws ContradictionException {
		Model choco = new Model();

		int[]   values = new int[]{3, 56};
		IntVar   index  = choco.intVar(new int[]{-7, 0, 45});
		IntVar   value  = choco.intVar(new int[]{-1, 0, 3});

		choco.element(value, values, index, 0).post();
		choco.getSolver().propagate();

		System.out.println("values = " + Arrays.toString(values));
		System.out.println("index  = " + index);
		System.out.println("value  = " + value);

		Assert.assertTrue(index.isInstantiatedTo(0));
	}

}