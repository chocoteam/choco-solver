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

package samples.integer;

import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Random;

/**
 * Example showing how to use absolute constraint
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 31/05/12
 * Time: 11:39
 */
public class AbsoluteEvaluation extends AbstractProblem {

    IntVar[] vars;

    @Option(name = "-s", usage = "random seed", required = false)
    private int seed = 1234;

    @Override
    public void createSolver() {
        solver = new Solver("AbsoluteEvaluation");
    }

    @Override
    public void buildModel() {
        Random rand = new Random(seed);

        int minX = -20 + rand.nextInt(40);
        int maxX = minX + rand.nextInt(40);

        int minY = -20 + rand.nextInt(40);
        int maxY = minY + rand.nextInt(40);

        vars = new IntVar[2];

        vars[0] = VariableFactory.bounded("X", minX, maxX, solver);
        vars[1] = VariableFactory.bounded("Y", minY, maxY, solver);

        Constraint abs = IntConstraintFactory.absolute(vars[0], vars[1]);
        solver.post(abs);
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.firstFail_InDomainMin(vars));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("AbsoluteEvaluation({})");
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < vars.length - 1; i++) {
            st.append(String.format("%d ", vars[i].getValue()));
            if (i % 10 == 9) {
                st.append("\n\t");
            }
        }
        st.append(String.format("%d", vars[vars.length - 1].getValue()));
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new AbsoluteEvaluation().execute(args);
    }
}
