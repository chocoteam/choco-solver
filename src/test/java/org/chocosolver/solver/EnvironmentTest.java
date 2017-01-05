/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
/**
 * @author Jean-Guillaume Fages
 * @since 07/04/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver;


import org.chocosolver.memory.EnvironmentBuilder;
import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.chocosolver.memory.trailing.trail.chunck.ChunckedIntTrail;
import org.chocosolver.memory.trailing.trail.flatten.StoredDoubleTrail;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.domOverWDegSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;

public class EnvironmentTest {

	@DataProvider(name = "env")
	public Object[][] getEnvs(){
		return new EnvironmentTrailing[][]{
				{new EnvironmentBuilder().fromFlat().build()},
				{new EnvironmentBuilder().fromChunk().build()}
		};
	}


	@Test(groups="10s", timeOut=60000, dataProvider = "env")
	public void testSize(EnvironmentTrailing env) {
		int n = 14;
		IntVar[] vars, vectors;
		Model model = new Model(env, "CostasArrays");
		vars = model.intVarArray("v", n, 0, n - 1, false);
		vectors = new IntVar[n * n - n];
		int idx = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
					IntVar k = model.intVar(model.generateName(), -20000, 20000, true);
					model.sum(new IntVar[]{vars[i], k}, "=", vars[j]).post();
					// just to create many variables
					model.sum(new IntVar[]{vars[i], k}, "=", vars[j]).reify();
					vectors[idx] = model.intOffsetView(k, 2 * n * (j - i));
					idx++;
				}
			}
		}
		model.allDifferent(vars, "AC").post();
		model.allDifferent(vectors, "BC").post();
		Solver r = model.getSolver();
		r.setSearch(domOverWDegSearch(append(vectors, vars)));
		model.getSolver().solve();
	}

	@Test(groups="1s", timeOut=60000)
	public void testBuilder(){
		ChunckedIntTrail it = new ChunckedIntTrail(1000, 3, 1.4);
		EnvironmentTrailing eb = new EnvironmentBuilder()
				.setTrail(it)
				.build();

		Assert.assertEquals(eb.getIntTrail(), it);
		Assert.assertTrue(eb.getDoubleTrail() instanceof StoredDoubleTrail);
//
	}

	@Test(groups="1s", timeOut=60000)
	public void testJL01() {
		Model m = new Model();
		m.boolVarArray("b", 200);
		m.getEnvironment().makeIntVector(100, 0);
		System.out.println(m.getSolver().solve());
	}

	@Test(groups="1s", timeOut=60000)
	public void testJL02() {
		Model m = new Model();
		m.boolVarArray("b", 200);
		m.getEnvironment().makeDoubleVector(100, 0.0);
		System.out.println(m.getSolver().solve());
	}
}