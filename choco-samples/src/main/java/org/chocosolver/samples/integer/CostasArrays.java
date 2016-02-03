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
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.kohsuke.args4j.Option;

import static org.chocosolver.util.tools.StringUtils.randomName;

/**
 * Costas Arrays
 * "Given n in N, find an array s = [s_1, ..., s_n], such that
 * <ul>
 * <li>s is a permutation of Z_n = {0,1,...,n-1};</li>
 * <li>the vectors v(i,j) = (j-i)x + (s_j-s_i)y are all different </li>
 * </ul>
 * <br/>
 * An array v satisfying these conditions is called a Costas array of size n;
 * the problem of finding such an array is the Costas Array problem of size n."
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 25/01/11
 */
public class CostasArrays extends AbstractProblem {

    @Option(name = "-o", usage = "Costas array size.", required = false)
    private static int n = 14;  // should be <15 to be solved quickly

    IntVar[] vars, vectors;

    @Override
    public void createSolver() {
        solver = new Solver("CostasArrays");
    }

	@Override
	public void buildModel() {
		vars = solver.makeIntVarArray("v", n, 0, n - 1, false);
		vectors = new IntVar[(n*(n-1))/2];
		for (int i = 0, k = 0; i < n; i++) {
			for (int j = i+1; j < n; j++, k++) {
				IntVar d = solver.makeIntVar(randomName(), -n, n, false);
				solver.post(ICF.arithm(d,"!=",0));
				solver.post(IntConstraintFactory.sum(new IntVar[]{vars[i],d},"=",vars[j]));
				vectors[k] = VariableFactory.offset(d, 2 * n * (j - i));
			}
		}
		solver.post(IntConstraintFactory.alldifferent(vars, "AC"));
		solver.post(IntConstraintFactory.alldifferent(vectors, "BC"));

		// symmetry-breaking
		solver.post(ICF.arithm(vars[0],"<",vars[n-1]));
	}

	@Override
	public void configureSearch() {
		SMF.limitTime(solver,"20s");
		solver.set(ISF.lexico_LB(vectors));
	}

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            s.append("|");
            for (int j = 0; j < n; j++) {
                if (j == vars[i].getValue()) {
                    s.append("x|");
                } else {
                    s.append("-|");
                }
            }
            s.append("\n");
        }
        System.out.println(s);
    }

    public static void main(String[] args) {
        new CostasArrays().execute(args);
    }
}
