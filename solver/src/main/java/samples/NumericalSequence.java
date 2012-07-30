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

import org.kohsuke.args4j.Option;
import solver.Solver;
import solver.constraints.Arithmetic;
import solver.constraints.binary.Element;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.propagation.hardcoded.VariableEngine;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/07/12
 */
public class NumericalSequence extends AbstractProblem {

    @Option(name = "-n", usage = "Max value.", required = false)
    int n = 20;
    IntVar[] U;

    @Override
    public void createSolver() {
        solver = new Solver("Suite (" + n + ")");
    }

    @Override
    public void buildModel() {
        U = new IntVar[n];
        U[0] = Views.fixed("U_0", n, solver);
        U[n - 1] = Views.fixed("U_" + (n - 1), 1, solver);
        for (int i = 1; i < n - 1; i++) {
            U[i] = VariableFactory.enumerated("U_" + i, 1, n + 1, solver);
        }
        for (int i = 1; i < n - 1; i++) {
            // U[i+1] = U[U[i]-1]-1
            solver.post(new Element(Views.offset(U[i], 1), U, Views.offset(U[i - 1], -1), 1, solver));
        }
        for (int i = 1; i < n / 2; i++) {
            // U[n + 1 - i] = n+ 1 - U[i]
            solver.post(new Arithmetic(U[n - 1 - i], "+", U[i], "=", n + 1, solver));
        }
        solver.post(new AllDifferent(U, solver));
    }

    @Override
    public void configureSearch() {
        solver.set(StrategyFactory.inputOrderMinVal(U, solver.getEnvironment()));
    }

    @Override
    public void configureEngine() {
        solver.set(new VariableEngine(solver));
    }

    @Override
    public void solve() {
        solver.getSearchLoop().plugSearchMonitor(new VoidSearchMonitor() {
            @Override
            public void onSolution() {
                StringBuilder st = new StringBuilder();
                st.append("{").append(U[0].getValue());
                for (int i = 1; i < U.length; i++) {
                    st.append(",").append(U[i].getValue());
                }
                st.append("}");
                System.out.printf("%s\n", st.toString());
            }
        });
        System.out.printf("M = %d\n", n);
        solver.findAllSolutions();
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new NumericalSequence().execute(args);
    }
}
