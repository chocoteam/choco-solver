/**
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
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
 */
package samples.real;

import org.slf4j.LoggerFactory;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.real.Ibex;
import solver.constraints.real.RealConstraint;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.selectors.values.RealDomainMiddle;
import solver.search.strategy.selectors.variables.Cyclic;
import solver.search.strategy.strategy.RealStrategy;
import solver.variables.IntVar;
import solver.variables.RealVar;
import solver.variables.VariableFactory;

/**
 * The cyclo hexan problem but hybrids finite/continuous problems
 * <br/>
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 18/07/12
 */
public class HybridCycloHexan extends AbstractProblem {

	RealVar[] vars;
	RealVar x, y, z;
	IntVar intx;

	@Override
	public void printDescription() {
		StringBuilder st = new StringBuilder(24);
		st.append("The CycloHexan problem consists in finding the 3D configuration of a cyclohexane molecule.\n");
		st.append("It is decribed with a system of three non linear equations : \n");
		st.append(" y^2 * (1 + z^2) + z * (z - 24 * y) = -13 \n" +
				" x^2 * (1 + y^2) + y * (y - 24 * x) = -13 \n" +
				" z^2 * (1 + x^2) + x * (x - 24 * z) = -13 \n");
		st.append("This example comes from the Elisa project (LINA) examples. \n");
		st.append("This example restricts x to be integer, as an illustration to hybrid finite/continuous problems. \n");
		st.append("It has no solution. \n");
		LoggerFactory.getLogger("bench").info(st.toString());
	}

	@Override
	public void createSolver() {
		solver = new Solver("HybridCycloHexan");
	}

	@Override
	public void buildModel() {
		double precision = 1.0e-3;
		// finite domain
		intx = VariableFactory.enumerated("x", new int[]{-10,-9,0,2,42}, solver);
		// continuous view
		x = VariableFactory.real(intx,precision);
		y = VariableFactory.real("y", -1.0e8, 1.0e8, precision, solver);
		z = VariableFactory.real("z", -1.0e8, 1.0e8, precision, solver);

		vars = new RealVar[]{x, y, z};
		solver.post(new RealConstraint(
				"CycloHexan",
				"{1}^2 * (1 + {2}^2) + {2} * ({2} - 24 * {1}) = -13;" +
						"{0}^2 * (1 + {1}^2) + {1} * ({1} - 24 * {0}) = -13;" +
						"{2}^2 * (1 + {0}^2) + {0} * ({0} - 24 * {2}) = -13",
				Ibex.HC4_NEWTON,
				vars)
		);
	}

	@Override
	public void configureSearch() {
		solver.set(new RealStrategy(vars, new Cyclic(), new RealDomainMiddle()));
		SearchMonitorFactory.limitTime(solver,10000);
	}

	@Override
	public void solve() {
		solver.findSolution();
	}

	@Override
	public void prettyOut() {
		solver.getIbex().release();
	}

	public static void main(String[] args) {
		new HybridCycloHexan().execute(args);
	}
}
