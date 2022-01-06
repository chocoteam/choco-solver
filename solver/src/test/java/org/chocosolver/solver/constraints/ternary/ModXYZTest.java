/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.binary.PropModXY;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;

/**
 * <br/>
 *
 * @author Arthur Godet
 * @since 29/03/2019
 */
public class ModXYZTest extends AbstractTernaryTest {

	@Override
	protected int validTuple(int vx, int vy, int vz) {
		return (vy != 0 && vz == vx%vy) ? 1 : 0;
	}

	@Override
	protected Constraint make(IntVar[] vars, Model s) {
		return s.mod(vars[0], vars[1], vars[2]);
	}

	@Override
	public Model modeler(int[][] domains, boolean bounded, long seed) {
		Model s = new Model();
		IntVar[] vars = new IntVar[3];
		for (int i = 0; i < 3; i++) {
			if (bounded) {
				vars[i] = s.intVar("x_" + i, domains[i][0], domains[i][1], true);
			} else {
				vars[i] = s.intVar("x_" + i, domains[i]);
			}
		}
		if(!vars[1].isInstantiatedTo(0)) {
			Constraint div = make(vars, s);
			div.post();
		} else {
			s.arithm(vars[1], "!=", 0).post();
		}
		s.getSolver().setSearch(randomSearch(vars,seed));
		return s;
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod3VarNegValues() throws ContradictionException {
		Model model = new Model("model");
		IntVar x = model.intVar("x", -5, 5);
		IntVar y = model.intVar("y", -3, 3);
		IntVar z = model.intVar("z", -5,5);
		model.post(new Constraint("X MOD Y = Z", new PropModXYZ(x, y, z)));
		model.getSolver().propagate();
		Assert.assertEquals(z.getDomainSize(), 5);
		Assert.assertEquals(y.getDomainSize(), 6);
		Assert.assertEquals(x.getDomainSize(), 11);
		for(int i = -5; i<=5; i++) {
			Assert.assertTrue(x.contains(i));
		}
		for(int j = -2; j<=2; j++) {
			Assert.assertTrue(z.contains(j));
		}
		for(int k = -3; k<=3; k++) {
			if(k != 0) {
				Assert.assertTrue(y.contains(k));
			} else {
				Assert.assertFalse(y.contains(k));
			}
		}
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod3VarNegValues2() throws ContradictionException {
		Model model = new Model("model");
		IntVar x = model.intVar("x", -5, 0);
		IntVar y = model.intVar("y", -3, 3);
		IntVar z = model.intVar("z", -5,5);
		model.mod(x, y, z).post();
		model.getSolver().propagate();
		Assert.assertEquals(z.getDomainSize(), 3);
		Assert.assertEquals(x.getDomainSize(), 6);
		Assert.assertEquals(y.getDomainSize(), 6);
		for(int i = -5; i<=0; i++) {
			Assert.assertTrue(x.contains(i));
		}
		for(int j = -2; j<=0; j++) {
			Assert.assertTrue(z.contains(j));
		}
		for(int k = -3; k<=3; k++) {
			if(k == 0) {
				Assert.assertFalse(y.contains(k));
			} else {
				Assert.assertTrue(y.contains(k));
			}
		}
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod3VarNegValues3() throws ContradictionException {
		Model model = new Model("model");
		IntVar x = model.intVar("x", -5, 5);
		IntVar y = model.intVar("y", new int[]{-3, -2, 0, 2, 3});
		IntVar z = model.intVar("z", -5,0);
		model.mod(x, y, z).post();
		model.getSolver().propagate();
		Assert.assertEquals(z.getDomainSize(), 3);
		Assert.assertEquals(x.getDomainSize(), 9);
		Assert.assertEquals(y.getDomainSize(), 4);
		for(int i = -5; i<=0; i++) {
			Assert.assertTrue(x.contains(i));
		}
		Assert.assertTrue(x.contains(2));
		Assert.assertTrue(x.contains(3));
		Assert.assertTrue(x.contains(4));
		for(int j = -2; j<=0; j++) {
			Assert.assertTrue(z.contains(j));
		}
		for(int k = -3; k<=3; k++) {
			if(k==0 || k==-1 || k==1) {
				Assert.assertFalse(y.contains(k));
			} else {
				Assert.assertTrue(y.contains(k));
			}
		}
	}

	@Test(groups="1s", timeOut=60000, expectedExceptions = SolverException.class)
	public void testMod3VarsZeroDiv() {
		Model model = new Model("model");
		IntVar x = model.intVar("x", 0,9);
		IntVar y = model.intVar("y", 0, 0);
		IntVar z = model.intVar("z", 0, 0);
		model.mod(x, y, z).post();
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod3VarsIntoMod2VarsMod() {
		Model model = new Model(Settings.init().setEnableTableSubstitution(false));
		System.out.printf("%s\n", model.getClass());
		IntVar x = model.intVar("x", 0,9);
		IntVar y = model.intVar("y", 5);
		IntVar z = model.intVar("z", 0, 9);
		Constraint cstr = model.mod(x, y, z);
		Assert.assertTrue(cstr.getPropagator(0) instanceof PropModXY);
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod3VarsTable() {
		Model model = new Model("model");
		IntVar x = model.intVar("x", 0,9);
		IntVar y = model.intVar("y", 0, 9);
		IntVar z = model.intVar("z", 0, 9);
		model.mod(x, y, z).post();
		Assert.assertEquals(model.getNbCstrs(), 1);
		Constraint constraint = model.getCstrs()[0];
		Assert.assertTrue(constraint.getName().equals(ConstraintsName.TABLE));
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod3VarsPropMod() {
		Model model = new Model("model");
		IntVar x = model.intVar("x", 0,10_000);
		IntVar y = model.intVar("y", 0, 10_000);
		IntVar z = model.intVar("z", 0, 10_000);
		model.mod(x, y, z).post();
		Assert.assertEquals(model.getNbCstrs(), 1);
		Constraint constraint = model.getCstrs()[0];
		Assert.assertEquals(constraint.getPropagators().length, 1);
		Assert.assertTrue(constraint.getPropagators()[0].getClass() == PropModXYZ.class);
	}

	@Test(groups="1s", timeOut=60000)
	public void testMod3VarsBoundedPropag() throws ContradictionException {
		Model model = new Model("model");
		IntVar x = model.intVar("x", 0,100_000);
		IntVar y = model.intVar("y", 0, 9);
		IntVar z = model.intVar("z", -10, 10);
		model.mod(x, y, z).post();
		model.getSolver().propagate();

		Assert.assertTrue(z.getLB() == 0);
		Assert.assertTrue(z.getUB() == 8);

		Assert.assertTrue(y.getLB() == 1);
	}
}
