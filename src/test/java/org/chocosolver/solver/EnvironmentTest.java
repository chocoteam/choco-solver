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

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.domOverWDegSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.chocosolver.util.tools.StringUtils.randomName;

public class EnvironmentTest {

	@DataProvider(name = "env")
	public Object[][] getEnvs(){
		return new EnvironmentTrailing[][]{
				{new EnvironmentBuilder().fromFlat().build()},
				{new EnvironmentBuilder().fromChunk().build()},
				{new EnvironmentBuilder().fromUnsafe().build()}
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
					IntVar k = model.intVar(randomName(), -20000, 20000, true);
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
}