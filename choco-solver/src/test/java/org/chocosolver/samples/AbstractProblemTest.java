/**
 * Copyright (c) 1999-2011, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Ecole des Mines de Nantes nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 01/02/2016
 * Time: 11:49
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 01/02/2016
 * Time: 11:49
 */

package org.chocosolver.samples;

import org.chocosolver.solver.Solver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AbstractProblemTest {

	boolean model, search, solve, pretty;

	@Test(groups="1s", timeOut=60000)
	public void testAP() {
		test(true);
		test(false);
	}

	private void test(boolean silent){
		model = false;
		search = false;
		solve = false;
		pretty = false;
		AbstractProblem ap = new AbstractProblem() {
			@Override
			public void createSolver() {
				if(silent){
					level = Level.SILENT;
				}
				solver = new Solver("test");
			}
			@Override
			public void buildModel() {
				model = true;
				Assert.assertTrue(solver != null && solver.getName().equals("test"));
				Assert.assertFalse(search || solve || pretty);
			}
			@Override
			public void configureSearch() {
				search = true;
				Assert.assertTrue(model);
				Assert.assertFalse(solve || pretty);
			}
			@Override
			public void solve() {
				solve = true;
				Assert.assertTrue(model && search);
				Assert.assertFalse(pretty);

			}
			@Override
			public void prettyOut() {
				Assert.assertTrue(model && search && solve);
				pretty = true;
			}
		};
		ap.execute();
		Assert.assertTrue(model && search && solve);
		Assert.assertEquals(pretty, !silent);
	}
}
