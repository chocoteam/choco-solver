/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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
package samples;

import common.ESat;
import common.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * CSPLib prob019:<br/>
 * "A magic sequence of length n is a sequence of integers x0 . . xn-1 between 0 and n-1, such that
 * for all i in 0 to n-1, the number i occurs exactly xi times in the sequence."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class MagicSeries extends AbstractProblem {

    @Option(name = "-n", usage = "Magic series size.", required = false)
    int n = 1000;
    IntVar[] vars;

    Constraint[] counts;

    @Override
    public void createSolver() {
        solver = new Solver("Magic series");
    }

    @Override
    public void buildModel() {
        vars = new IntVar[n];

        vars = VariableFactory.boundedArray("var", n, 0, n - 1, solver);

        counts = new Constraint[n];
        for (int i = 0; i < n; i++) {
            counts[i] = IntConstraintFactory.count(i, vars, VariableFactory.eq(vars[i]));
            solver.post(counts[i]);
        }
        solver.post(IntConstraintFactory.sum(vars, VariableFactory.fixed(n, solver))); // cstr redundant 1
        int[] coeff2 = new int[n - 1];
        IntVar[] vs2 = new IntVar[n - 1];
        for (int i = 1; i < n; i++) {
            coeff2[i - 1] = i;
            vs2[i - 1] = vars[i];
        }
        solver.post(IntConstraintFactory.scalar(vs2, coeff2, VariableFactory.fixed(n, solver))); // cstr redundant 1
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.inputOrder_InDomainMax(vars));
        // default group
    }

    @Override
    public void configureEngine() {
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Magic series({})", n);
        StringBuilder st = new StringBuilder();
        if (solver.isFeasible() == ESat.TRUE) {
            st.append("\t");
            for (int i = 0; i < n; i++) {
                st.append(vars[i].getValue()).append(" ");
                if (i % 10 == 9) {
                    st.append("\n\t");
                }
            }
        } else {
            st.append("\tINFEASIBLE");
        }
        LoggerFactory.getLogger("bench").info(st.toString());

    }

    public static void main(String[] args) {
        new MagicSeries().execute(ArrayUtils.append(args, new String[]{"-log", "QUIET"}));
    }
}
