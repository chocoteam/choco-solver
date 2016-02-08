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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.tree.PropAntiArborescences;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;
import static org.chocosolver.solver.trace.Chatterbox.showShortStatistics;
import static org.testng.Assert.assertEquals;

public class TreeTest {

	@Test(groups="10s", timeOut=60000)
	public void test1() {
		Model s1 = model(true);
		Model s2 = model(false);
		while (s1.solve()) ;
		while (s2.solve()) ;
		assertEquals(s1.getResolver().getMeasures().getSolutionCount(), s2.getResolver().getMeasures().getSolutionCount());
		assertEquals(s1.getResolver().getMeasures().getNodeCount(), s2.getResolver().getMeasures().getNodeCount());
	}

	private Model model(boolean defaultCstr) {
		Model model = new Model();
		IntVar[] VS = model.intVarArray("VS", 6, -1, 6, false);
		IntVar NT = model.intVar("NT", 2, 3, false);
		if (defaultCstr) {
			model.tree(VS, NT, 0).post();
		} else {
			new Constraint("tree",
					new PropAntiArborescences(VS, 0, false),
					new PropKLoops(VS, 0, NT)
			).post();
		}
		model.getResolver().set(randomSearch(VS, 0));
		showShortStatistics(model);
		return model;
	}
}
