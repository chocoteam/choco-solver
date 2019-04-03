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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
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
		return (vy != 0 && vz == vx - vy * (vx / vy)) ? 1 : 0;
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
}
