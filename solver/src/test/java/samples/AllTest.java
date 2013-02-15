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

import common.util.tools.ArrayUtils;
import memory.Environments;
import memory.IEnvironment;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import samples.integer.AbsoluteEvaluation;
import solver.Configuration;
import solver.ISolverProperties;
import solver.Solver;
import solver.explanations.ExplanationFactory;
import solver.propagation.PropagationStrategies;
import solver.search.loop.SearchLoops;
import solver.search.loop.monitors.SearchMonitorFactory;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/11/11
 */
public class AllTest {

    AbstractProblem prob;
    String[] args;
    long nbSol;
    IEnvironment environment;
    ISolverProperties properties;
    PropagationStrategies strat;


    public AllTest() {
//        this(new AllIntervalSeries(), new String[]{"-o", "5"},
        this(new AbsoluteEvaluation(), null,
                Environments.TRAIL.make(),
                new AllSolverProp(
                        SearchLoops.BINARY,
                        ExplanationFactory.RECORDER),
                PropagationStrategies.CONSTRAINT, 6);
    }

    public AllTest(AbstractProblem prob, String[] arguments,
                   IEnvironment env,
                   ISolverProperties properties,
                   PropagationStrategies strat,
                   long nbSol) {
        this.prob = prob;
        this.args = arguments;
        args = ArrayUtils.append(args, new String[]{"-policy", strat.name()});
        this.environment = env;
        this.properties = properties;
        this.strat = strat;
        this.nbSol = nbSol;
        //prob.solver.
    }

    @Test(groups = "1m")
    public void mainTest() {
        if (Configuration.PLUG_EXPLANATION) {
            LoggerFactory.getLogger("test").info(this.toString());
            prob.readArgs(args);
            prob.solver = new Solver(environment, prob.getClass().getSimpleName(), properties); // required for testing, to pass properties
            prob.buildModel();
            prob.configureSearch();
            //  prob.overrideExplanation();
            SearchMonitorFactory.log(prob.solver, true, true);
            prob.solver.findAllSolutions();

            Assert.assertEquals(nbSol, prob.getSolver().getMeasures().getSolutionCount(), "incorrect nb solutions");
        }
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append(prob.getClass().getSimpleName()).append(" ");
        st.append(Arrays.toString(args)).append(" ");
        st.append(environment.getClass().getSimpleName()).append(" ");
        st.append(properties).append(" ");
        return st.toString();
    }
}
