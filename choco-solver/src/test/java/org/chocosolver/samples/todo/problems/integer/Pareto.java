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
package org.chocosolver.samples.todo.problems.integer;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

/**
 * Multi-objective optimization illustration to compute pareto solutions
 *
 * @author Jimmy Liang, Jean-Guillaume Fages
 */
public class Pareto {

	public static void main(String[] args){
		Model model = new Model();
		IntVar a = model.intVar("a", 0, 2, false);
		IntVar b = model.intVar("b", 0, 2, false);
		IntVar c = model.intVar("c", 0, 2, false);

		model.arithm(a, "+", b, "<", 3).post();

		// the problem is to maximize a and b
		/*model.setObjective(ResolutionPolicy.MAXIMIZE,a,b);

		List<Solution> solutions = new ArrayList<>();
		while(model.solve()){
			Solution sol = new Solution();
			sol.record(model);
			solutions.add(sol);
		}

		System.out.println("The pareto front has "+solutions.size()+" solutions : ");
		for(Solution s:solutions){
			System.out.println("a = "+s.getIntVal(a)+" and b = "+s.getIntVal(b));
		}*/
		throw new UnsupportedOperationException("Pareto not supported anymore");
	}
}
