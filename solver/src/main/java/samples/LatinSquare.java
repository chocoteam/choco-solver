/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package samples;

import choco.kernel.common.util.tools.StringUtils;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.GCC_AC;
import solver.constraints.nary.GlobalCardinality;
import solver.constraints.propagators.PropDomSize;
import solver.propagation.hardcoded.ConstraintEngine;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.text.MessageFormat;

/**
 * <a href="http://en.wikipedia.org/wiki/Latin_square">wikipedia</a>:<br/>
 * "A Latin square is an n x n array filled with n different Latin letters,
 * each occurring exactly once in each row and exactly once in each column"
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/06/11
 */
public class LatinSquare extends AbstractProblem {

	@Option(name = "-n", usage = "Latin square size.", required = false)
	int m = 20;
	IntVar[] vars;

	@Override
	public void createSolver() {
		solver = new Solver("Latin square");
	}

	@Override
	public void buildModel() {
		vars = new IntVar[m * m];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < m; j++) {
//                vars[i * m + j] = VariableFactory.bounded("C" + i + "_" + j, 0, m - 1, solver);
				vars[i * m + j] = VariableFactory.enumerated("C" + i + "_" + j, 0, m - 1, solver);
			}
		}
		int[] values = new int[m];
		for(int v=0;v<m;v++){
			values[v] = v;
		}

		// Constraints
//		Constraint check = new Constraint(solver);
		for (int i = 0; i < m; i++) {
			int[] low = new int[m];
			int[] up = new int[m];
			IntVar[] row = new IntVar[m];
			IntVar[] col = new IntVar[m];
			for (int x = 0; x < m; x++) {
				row[x] = vars[i * m + x];
				col[x] = vars[x * m + i];
				low[x] = 0;
				up[x] = 1;
			}
//            solver.post(GlobalCardinality.make(row, low, up, 0, GlobalCardinality.Consistency.BC, solver));
//            solver.post(GlobalCardinality.make(col, low, up, 0, GlobalCardinality.Consistency.BC, solver));
			solver.post(new GCC_AC(row, values, low, up, solver));
			solver.post(new GCC_AC(col, values, low, up, solver));
//			check.addPropagators(new PropDomSize(row, check, solver));
//			check.addPropagators(new PropDomSize(col,check,solver));
		}
//		solver.post(check);
	}

	@Override
	public void configureSearch() {
		solver.set(StrategyFactory.inputOrderMinVal(vars, solver.getEnvironment()));
		//SearchMonitorFactory.log(solver, true, true);
		/*IPropagationEngine engine = solver.getEngine();
				engine.addGroup(Group.buildQueue(
						Predicates.member(vars), Policy.FIXPOINT
				));*/
	}

	@Override
	public void configureEngine() {
		//solver.set(new ConstraintEngine(solver));
	}

	@Override
	public void solve() {
//		SearchMonitorFactory.log(solver, true, true);
		solver.findSolution();
	}

	@Override
	public void prettyOut() {
		StringBuilder st = new StringBuilder();
		String line = "+";
		for (int i = 0; i < m; i++) {
			line += "----+";
		}
		line += "\n";
		st.append(line);
		for (int i = 0; i < m; i++) {
			st.append("|");
			for (int j = 0; j < m; j++) {
				st.append(StringUtils.pad((char)(vars[i * m + j].getValue()+97) + "", -3, " ")).append(" |");
			}
			st.append(MessageFormat.format("\n{0}", line));
		}
		st.append("\n\n\n");
		LoggerFactory.getLogger("bench").info(st.toString());
	}

	public static void main(String[] args) {
		new LatinSquare().execute(args);
	}
}
