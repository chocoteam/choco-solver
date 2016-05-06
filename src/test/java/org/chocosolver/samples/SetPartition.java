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
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 18:25
 */

package org.chocosolver.samples;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.ResolutionPolicy.MINIMIZE;

/**
 * Small problem to illustrate how to use set variables
 * finds a partition a universe so that the sum of elements in universe
 * (restricted to the arbitrary interval [12,19]) is minimal
 *
 * @author Jean-Guillaume Fages
 */
public class SetPartition extends AbstractProblem {

	private SetVar x, y, z, universe;
	private IntVar sum;

	@Override
	public void buildModel() {
		model = new Model();

		///////////////
		// VARIABLES //
		///////////////

		// x initial domain
		int[] x_envelope = new int[]{1, 3, 2, 8}; // not necessarily ordered
		int[] x_kernel = new int[]{1};
		x = model.setVar("x", x_kernel, x_envelope);
		// y initial domain
		int[] y_envelope = new int[]{2, 6, 7};
		y = model.setVar("y", new int[]{}, y_envelope);
		// z initial domain
		int[] z_envelope = new int[]{2, 1, 3, 5, 7, 12};
		int[] z_kernel = new int[]{2};
		z = model.setVar("z", z_kernel, z_envelope);
		// universe initial domain (note that the universe is a variable)
		int[] universe_envelope = new int[]{1, 2, 3, 5, 7, 8, 42};
		universe = model.setVar("universe", new int[]{}, universe_envelope);
		// sum variable
		sum = model.intVar("sum of universe", 12, 19, true);

		/////////////////
		// CONSTRAINTS //
		/////////////////

		// partition constraint
		model.partition(new SetVar[]{x, y, z}, universe).post();
		// forbid empty sets
		model.nbEmpty(new SetVar[]{x, y, z, universe}, model.intVar(0)).post();
		// restricts the sum of elements in universe
		model.sum(universe, sum, true).post();
	}

	@Override
	public void solve() {
		// find the optimum
		model.setObjective(MINIMIZE, sum);
		while(model.getSolver().solve()){
			System.out.println("new solution found");
			System.out.println(x);
			System.out.println(y);
			System.out.println(z);
			System.out.println(universe);
			System.out.println(sum);
		}
	}

	@Test(groups = "10s", timeOut = 60000)
	public void test(){
		SetPartition sp = new SetPartition();
		sp.buildModel();
		sp.solve();
		Assert.assertEquals(2,sp.getModel().getSolver().getSolutionCount());
		Assert.assertEquals(13,sp.getModel().getSolver().getObjectiveManager().getBestSolutionValue().intValue());
	}
}
