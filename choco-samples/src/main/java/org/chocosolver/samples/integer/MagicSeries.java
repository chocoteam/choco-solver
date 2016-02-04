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
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.Option;

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
        vars = solver.intVarArray("var", n, 0, n - 1, true);
        counts = new Constraint[n];
        for (int i = 0; i < n; i++) {
            counts[i] = IntConstraintFactory.count(i, vars, vars[i]);
            solver.post(counts[i]);
        }
        solver.post(IntConstraintFactory.sum(vars, "=", n)); // cstr redundant 1
        int[] coeff2 = new int[n - 1];
        IntVar[] vs2 = new IntVar[n - 1];
        for (int i = 1; i < n; i++) {
            coeff2[i - 1] = i;
            vs2[i - 1] = vars[i];
        }
        solver.post(IntConstraintFactory.scalar(vs2, coeff2, "=", n)); // cstr redundant 1
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.lexico_UB(vars));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        System.out.println(String.format("Magic series(%d)", n));
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
        System.out.println(st.toString());

    }

    public static void main(String[] args) {
        new MagicSeries().execute(args);
    }
}
