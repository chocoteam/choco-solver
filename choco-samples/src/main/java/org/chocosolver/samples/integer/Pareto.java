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
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.IntVar;

import java.util.List;

/**
 * Multi-objective optimization illustration to compute pareto solutions
 *
 * @author Jimmy Liang, Jean-Guillaume Fages
 */
public class Pareto extends AbstractProblem {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	IntVar a,b,c;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************


	@Override
	public void buildModel() {
		model = new Model();
		// the problem is to maximize a and b
		a = model.intVar("a", 0, 2, false);
		b = model.intVar("b", 0, 2, false);
		c = model.intVar("c", 0, 2, false);

		model.arithm(a, "+", b, "<", 3).post();
	}

	@Override
	public void configureSearch() {}

	@Override
	public void solve() {
		model.setObjectives(ResolutionPolicy.MAXIMIZE,a,b);
		model.solve();
	}

	@Override
	public void prettyOut() {
		List<Solution> paretoFront = model.getSolutionRecorder().getSolutions();
		System.out.println("The pareto front has "+paretoFront.size()+" solutions : ");
		for(Solution s:paretoFront){
			System.out.println("a = "+s.getIntVal(a)+" and b = "+s.getIntVal(b));
		}
	}

	public static void main(String[] args){
	    new Pareto().execute(args);
	}
}
