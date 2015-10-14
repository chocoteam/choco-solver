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
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.kohsuke.args4j.Option;

/**
 * Simple example which sorts m integers in range [0,m-1]
 * by using an allDifferent constraint and arithmetic constraints (leq)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/03/12
 */
public class BigLeq extends AbstractProblem {
    @Option(name = "-o", usage = "All interval series size.", required = false)
    private int m = 10;

    IntVar[] vars;

    @Override
    public void createSolver() {
        solver = new Solver("BigLeq");
    }

    @Override
    public void buildModel() {
        vars = VariableFactory.enumeratedArray("v", m, 0, m - 1, solver);
        for (int i = 0; i < m - 1; i++) {
            solver.post(IntConstraintFactory.arithm(vars[i], "<=", vars[i + 1]));
        }
        solver.post(IntConstraintFactory.alldifferent(vars, "BC"));
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.minDom_MidValue(true, vars));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        System.out.println(String.format("bigleq(%d)", m));
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < m - 1; i++) {
            st.append(String.format("%d ", vars[i].getValue()));
            if (i % 10 == 9) {
                st.append("\n\t");
            }
        }
        st.append(String.format("%d", vars[m - 1].getValue()));
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new BigLeq().execute(args);
    }
}
