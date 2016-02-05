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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

import static org.chocosolver.util.tools.StringUtils.randomName;

/**
 * CSPLib prob007:<br/>
 * "Given n in N, find a vector s = (s_1, ..., s_n), such that
 * <ul>
 * <li>s is a permutation of Z_n = {0,1,...,n-1};</li>
 * <li>the interval vector v = (|s_2-s_1|, |s_3-s_2|, ... |s_n-s_{n-1}|) is a permutation of Z_n-{0} = {1,2,...,n-1}.</li>
 * </ul>
 * <br/>
 * A vector v satisfying these conditions is called an all-interval series of size n;
 * the problem of finding such a series is the all-interval series problem of size n."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/08/11
 */
public class AllIntervalSeries extends AbstractProblem {
    @Option(name = "-o", usage = "All interval series size.", required = false)
    private int m = 1000;

    @Option(name = "-v", usage = " use views instead of constraints.", required = false)
    private boolean use_views = false;

    IntVar[] vars;
    IntVar[] dist;

    @Override
    public void createSolver() {
        solver = new Solver("AllIntervalSeries");
    }

    @Override
    public void buildModel() {
        vars = solver.intVarArray("v", m, 0, m - 1, false);
        dist = new IntVar[m - 1];

        if (!use_views) {
            dist = solver.intVarArray("dist", m - 1, 1, m - 1, false);
            for (int i = 0; i < m - 1; i++) {
                solver.distance(vars[i + 1], vars[i], "=", dist[i]).post();
            }
        } else {
            for (int i = 0; i < m - 1; i++) {
                IntVar k = solver.intVar(randomName(), -20000, 20000, true);
                solver.sum(new IntVar[]{vars[i], k}, "=", vars[i + 1]).post();
                dist[i] = solver.intAbsView(k);
                solver.member(dist[i], 1, m - 1).post();
            }
        }

        solver.allDifferent(vars, "BC").post();
        solver.allDifferent(dist, "BC").post();

        // break symetries
        solver.arithm(vars[1], ">", vars[0]).post();
        solver.arithm(dist[0], ">", dist[m - 2]).post();
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.minDom_LB(vars));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        System.out.println(String.format("All interval series(%s)", m));
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < m - 1; i++) {
            st.append(String.format("%d <%d> ", vars[i].getValue(), dist[i].getValue()));
            if (i % 10 == 9) {
                st.append("\n\t");
            }
        }
        st.append(String.format("%d", vars[m - 1].getValue()));
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new AllIntervalSeries().execute(args);
    }
}
