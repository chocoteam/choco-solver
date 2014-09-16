/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


/**
 * @author Jean-Guillaume Fages
 * @since 21/03/14
 * Created by IntelliJ IDEA.
 */
package samples.integer;

import samples.AbstractProblem;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.ICF;
import solver.search.solution.Solution;
import solver.variables.IntVar;
import solver.variables.VF;

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
	public void createSolver() {
		solver = new Solver("Pareto sample");
	}

	@Override
	public void buildModel() {
		// the problem is to maximize a and b
		a = VF.enumerated("a", 0, 2, solver);
		b = VF.enumerated("b", 0, 2, solver);
		c = VF.enumerated("c", 0, 2, solver);

		solver.post(ICF.arithm(a, "+", b, "<", 3));
	}

	@Override
	public void configureSearch() {}

	@Override
	public void solve() {
		solver.findParetoFront(ResolutionPolicy.MAXIMIZE,a,b);
	}

	@Override
	public void prettyOut() {
		List<Solution> paretoFront = solver.getSolutionRecorder().getSolutions();
		System.out.println("The pareto front has "+paretoFront.size()+" solutions : ");
		for(Solution s:paretoFront){
			System.out.println("a = "+s.getIntVal(a)+" and b = "+s.getIntVal(b));
		}
	}

	public static void main(String[] args){
	    new Pareto().execute(args);
	}
}
