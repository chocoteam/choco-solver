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
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.Count;
import solver.constraints.nary.Sum;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.PropagationStrategies;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.StrategyFactory;
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
    int n = 500;
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

        counts = new Count[n];
        for (int i = 0; i < n; i++) {
            counts[i] = new Count(i, vars, Count.Relop.EQ, vars[i], solver);
            solver.post(counts[i]);
        }
        solver.post(Sum.eq(vars, n, solver)); // cstr redundant 1
        int[] coeff2 = new int[n - 1];
        IntVar[] vs2 = new IntVar[n - 1];
        for (int i = 1; i < n; i++) {
            coeff2[i - 1] = i;
            vs2[i - 1] = vars[i];
        }
        solver.post(Sum.eq(vs2, coeff2, n, solver)); // cstr redundant 1
    }

    @Override
    public void configureSearch() {
        solver.set(StrategyFactory.inputOrderMaxVal(vars, solver.getEnvironment()));
        // default group
    }

    @Override
    public void configureEngine() {
        /*IPropagationEngine pengine = new PropagationEngine(solver.getEnvironment());
        Sort s1 = new Sort( new PVar(pengine, solver.getVars()));
        Sort s2 = new Sort(new Increasing(EvtRecEvaluators.MaxArityC), new PCoarse(pengine, solver.getCstrs()));
        solver.set(pengine.set(new Sort(s1, s2)));*/

        final IPropagationEngine engine = new PropagationEngine(solver.getEnvironment(), solver.getNbVars(), solver.getNbCstrs());
        PropagationStrategies.PRIORITY_QUEUES_WITH_PROPS.make(solver, engine);
        solver.set(engine);
        final IPropagationEngine engine2 = new PropagationEngine(solver.getEnvironment());
        PropagationStrategies.TWO_QUEUES_WITH_VARS.make(solver, engine2);
        solver.getSearchLoop().plugSearchMonitor(new VoidSearchMonitor() {
            @Override
            public void afterInitialPropagation() {

                solver.set(engine2);
                engine2.skipInitialPropagation();
                engine2.init(solver);
            }
        });
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Magic series({})", n);
        StringBuilder st = new StringBuilder();
        if (solver.isFeasible() == Boolean.TRUE) {
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
        for (int i = 0; i < 10; i++) new MagicSeries().execute("-log", "QUIET");
    }
}
