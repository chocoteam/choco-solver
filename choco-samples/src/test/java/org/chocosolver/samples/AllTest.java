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
package org.chocosolver.samples;

import org.chocosolver.memory.Environments;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.samples.integer.AllIntervalSeries;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

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
    PropagationEngineFactory strat;
    ExplanationFactory efact;


    public AllTest() {
        this(new AllIntervalSeries(), new String[]{"-o", "5"},
                Environments.TRAIL.make(),
                PropagationEngineFactory.TWOBUCKETPROPAGATIONENGINE,
                ExplanationFactory.NONE,
                2);
    }

    public AllTest(AbstractProblem prob, String[] arguments,
                   IEnvironment env,
                   PropagationEngineFactory strat,
                   ExplanationFactory e,
                   long nbSol) {
        this.prob = prob;
        this.args = arguments;
        if (args == null) {
            args = new String[0];
        }
        this.environment = env;
        this.strat = strat;
        this.nbSol = nbSol;
        this.efact = e;
    }

    @Test(groups = "1m")
    public void mainTest() {
        prob.readArgs(args);
        prob.solver = new Solver(environment, prob.getClass().getSimpleName()); // required for testing, to pass properties
        prob.solver.set(new Settings() {
            @Override
            public boolean plugExplanationIn() {
                return true;
            }
        });
        prob.buildModel();
        prob.configureSearch();
        efact.plugin(prob.solver, false, false);
        prob.solver.findAllSolutions();

        Assert.assertEquals(nbSol, prob.getSolver().getMeasures().getSolutionCount(), "incorrect nb solutions");
    }

    @Override
    public String toString() {
        return prob.getClass().getSimpleName() + " " + Arrays.toString(args) + " " + environment.getClass().getSimpleName() + " ";
    }
}
