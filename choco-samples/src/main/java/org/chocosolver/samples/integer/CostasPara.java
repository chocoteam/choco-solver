/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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
 * Date: 18/09/13
 * Time: 23:10
 */

package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Portfolio;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.SolverFactory;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.StringUtils;

public class CostasPara extends AbstractProblem {

    private static int n = 14;
    IntVar[] vars, vectors;

    Portfolio prtfl;

    @Override
    public void createSolver() {
        prtfl = SolverFactory.makePortelio("CostasPara", 3);
        solver = prtfl._fes_();
    }

    @Override
    public void buildModel() {
        vars = VariableFactory.enumeratedArray("v", n, 0, n - 1, solver);
        vectors = new IntVar[n * n - n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    IntVar k = VariableFactory.bounded(StringUtils.randomName(), -20000, 20000, solver);
                    solver.post(IntConstraintFactory.sum(new IntVar[]{vars[i], k}, vars[j]));
                    vectors[idx] = VariableFactory.offset(k, 2 * n * (j - i));
                    idx++;
                }
            }
        }
        solver.post(IntConstraintFactory.alldifferent(vars, "AC"));
        solver.post(IntConstraintFactory.alldifferent(vectors, "BC"));
    }

    @Override
    public void configureSearch() {
        prtfl.carbonCopy();
        Solver[] solvers = prtfl.workers;
        solvers[0].set(ISF.activity(prtfl.retrieveVarIn(0, vars), 0));
        solvers[1].set(ISF.lexico_LB(prtfl.retrieveVarIn(1, vars)));
        solvers[2].set(ISF.minDom_LB(prtfl.retrieveVarIn(2, vars)));
    }


    @Override
    public void solve() {
        prtfl.findSolution();
    }

    @Override
    public void prettyOut() {
        String s = "";
        for (int i = 0; i < n; i++) {
            s += "|";
            for (int j = 0; j < n; j++) {
                if (j == vars[i].getValue()) {
                    s += "x|";
                } else {
                    s += "-|";
                }
            }
            s += "\n";
        }
        System.out.println(s);
    }

    public static void main(String[] args) {
        new CostasPara().execute(args);
    }
}
