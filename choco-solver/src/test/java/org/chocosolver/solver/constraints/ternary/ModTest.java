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
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/07/12
 */
public class ModTest extends AbstractTernaryTest {

	@Override
	protected int validTuple(int vx, int vy, int vz) {
		return (vy != 0 && vz == vx - vy * (vx / vy)) ? 1 : 0;
	}

	@Override
	protected Constraint make(IntVar[] vars, Solver s) {
		return IntConstraintFactory.mod(vars[0], vars[1], vars[2]);
	}

	@Test(groups="1s", timeOut=60000)
	public void test2() {
		Solver solver = new Solver();
		IntVar res = solver.makeIntVar("r", 1, 2, true);
		solver.post(IntConstraintFactory.mod(res, solver.makeIntVar(2), solver.makeIntVar(1)));
		try {
			solver.propagate();
			Assert.assertTrue(res.isInstantiatedTo(1));
		} catch (ContradictionException e) {
			Assert.fail();
		}
	}

	@Test(groups="1s", timeOut=60000)
	public void testJL() {
		Solver s = new Solver();
		IntVar dividend = s.makeIntVar("dividend", 2, 3, false);
		IntVar divisor = s.makeIntVar(1);
		IntVar remainder = s.makeIntVar("remainder", 1, 2, false);
		s.post(ICF.mod(dividend, divisor, remainder).getOpposite());
		s.set(ISF.lexico_LB(dividend, divisor, remainder));
		s.findSolution();
	}
}
