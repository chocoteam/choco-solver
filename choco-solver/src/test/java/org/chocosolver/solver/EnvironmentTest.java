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

import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.annotations.Test;

import static org.chocosolver.util.tools.StringUtils.randomName;

public class EnvironmentTest {

	@Test(groups="10s", timeOut=60000)
	public void testSize(){
		int n = 14;
		IntVar[] vars, vectors;
		Solver solver = new Solver("CostasArrays");
		vars = solver.makeIntVarArray("v", n, 0, n - 1, false);
		vectors = new IntVar[n * n - n];
		int idx = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
					IntVar k = solver.makeIntVar(randomName(), -20000, 20000, true);
					solver.post(IntConstraintFactory.sum(new IntVar[]{vars[i],k},"=",vars[j]));
					// just to create many variables
					IntConstraintFactory.sum(new IntVar[]{vars[i], k}, "=", vars[j]).reif();
					vectors[idx] = VariableFactory.offset(k, 2 * n * (j - i));
					idx++;
				}
			}
		}
		solver.post(IntConstraintFactory.alldifferent(vars, "AC"));
		solver.post(IntConstraintFactory.alldifferent(vectors, "BC"));
		solver.set(ISF.domOverWDeg(ArrayUtils.append(vectors, vars), 0));
		solver.findSolution();
	}
}
