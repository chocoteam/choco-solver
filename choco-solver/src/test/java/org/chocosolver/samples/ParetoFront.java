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
 * @since 21/03/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.samples;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.search.solution.ParetoSolutionsRecorder;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Trivial multi-objective optimization computing pareto solutions
 *
 * @author Jimmy Liang, Jean-Guillaume Fages
 */
public class ParetoFront {

	@Test(groups = "1s", timeOut = 60000)
	public void testPareto(){
		// simple model
		Model model = new Model();
		IntVar a = model.intVar("a", 0, 2, false);
		IntVar b = model.intVar("b", 0, 2, false);
		IntVar c = model.intVar("c", 0, 2, false);
		model.arithm(a, "+", b, "=", c).post();

		// create an object that will store the best solutions and dynamically add constraints to remove dominated ones
		ParetoSolutionsRecorder paretoRecorder = new ParetoSolutionsRecorder(ResolutionPolicy.MAXIMIZE,a,b);
		model.getSolver().plugMonitor(paretoRecorder);

		// optimization
		while(model.solve());

		// retrieve the pareto front
		List<Solution> paretoFront = paretoRecorder.getSolutions();
		System.out.println("The pareto front has "+paretoFront.size()+" solutions : ");
		Assert.assertEquals(3, paretoFront.size());
		for(Solution s:paretoFront){
			System.out.println("a = "+s.getIntVal(a)+" and b = "+s.getIntVal(b));
			Assert.assertEquals(2,(int)s.getIntVal(c));
		}
	}
}
