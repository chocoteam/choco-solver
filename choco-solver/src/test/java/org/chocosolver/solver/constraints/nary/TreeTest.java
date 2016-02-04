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
 * @since 19/09/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.nary.tree.PropAntiArborescences;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TreeTest {

	@Test(groups="10s", timeOut=60000)
	public void test1() {
        Solver s1 = model(true);
		Solver s2 = model(false);
		s1.findAllSolutions();
		s2.findAllSolutions();
		Assert.assertEquals(s1.getMeasures().getSolutionCount(),s2.getMeasures().getSolutionCount());
		Assert.assertEquals(s1.getMeasures().getNodeCount(),s2.getMeasures().getNodeCount());
    }

	private Solver model(boolean defaultCstr) {
		Solver solver = new Solver();
		IntVar[] VS = solver.intVarArray("VS", 6, -1, 6, false);
		IntVar NT = solver.intVar("NT", 2, 3, false);
		if(defaultCstr) {
			solver.post(ICF.tree(VS, NT, 0));
		}else{
			solver.post(new Constraint("tree",
					new PropAntiArborescences(VS, 0, false),
					new PropKLoops(VS, 0, NT)
			));
		}
		solver.set(ISF.random(VS,0));
		Chatterbox.showShortStatistics(solver);
		return solver;
	}
}
