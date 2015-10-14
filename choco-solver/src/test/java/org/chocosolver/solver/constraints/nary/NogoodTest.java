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
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.limits.BacktrackCounter;
import org.chocosolver.solver.search.loop.SDF;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.loop.plm.SearchDriver;
import org.chocosolver.solver.search.restart.GeometricalRestartStrategy;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/06/13
 */
public class NogoodTest {

    @Test(groups = "1s")
    public void test1() {
        final Solver solver = new Solver();
        IntVar[] vars = VF.enumeratedArray("vars", 3, 0, 2, solver);
        SearchDriver searchDriver =
                SDF.learnNogoodFromRestarts(
                        SDF.restart(SDF.dfs(solver, ISF.random_value(vars, 29091981L)),
                                limit -> solver.getMeasures().getBackTrackCount() >= limit,
                                new GeometricalRestartStrategy(2,1.1),
                                20));
        searchDriver.setStopCriterion(() -> solver.getMeasures().getTimeCount() > 200000);
        solver.set(searchDriver);
//        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 110);
        Assert.assertEquals(solver.getMeasures().getBackTrackCount(), 182);
    }

    @Test(groups = "1s")
    public void test2() {
        final Solver solver = new Solver();
        IntVar[] vars = VF.enumeratedArray("vars", 3, 0, 3, solver);
        SMF.nogoodRecordingFromRestarts(solver);
        solver.set(ISF.random_value(vars, 29091981L));
        final BacktrackCounter sc = new BacktrackCounter(32);
        sc.setAction(() -> {
            solver.getSearchLoop().restart();
            sc.reset();
        });
        solver.getSearchLoop().plugSearchMonitor(sc);
        SMF.limitTime(solver, 2000);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 75);
        Assert.assertEquals(solver.getMeasures().getBackTrackCount(), 137);
    }


    @Test(groups = "1s")
    public void test3() throws ContradictionException {
        Solver solver = new Solver();
        IntVar[] dv = VF.enumeratedArray("d", 7, 1, 2, solver);
        SMF.nogoodRecordingFromRestarts(solver);

        IntDecision d1 = new IntDecision(null);
        d1.set(dv[0], 1, DecisionOperator.int_eq);
        d1.buildNext();
        d1.apply();
        d1.setPrevious(solver.getSearchLoop().getLastDecision());

        IntDecision d2 = new IntDecision(null);
        d2.set(dv[1], 1, DecisionOperator.int_eq);
        d2.buildNext();
        d2.buildNext();
        d2.apply();
        d2.setPrevious(d1);

        IntDecision d3 = new IntDecision(null);
        d3.set(dv[2], 1, DecisionOperator.int_eq);
        d3.buildNext();
        d3.buildNext();
        d3.apply();
        d3.setPrevious(d2);


        IntDecision d4 = new IntDecision(null);
        d4.set(dv[3], 1, DecisionOperator.int_eq);
        d4.buildNext();
        d4.apply();
        d4.setPrevious(d3);

        IntDecision d5 = new IntDecision(null);
        d5.set(dv[4], 1, DecisionOperator.int_eq);
        d5.buildNext();
        d5.buildNext();
        d5.apply();
        d5.setPrevious(d4);

        IntDecision d6 = new IntDecision(null);
        d6.set(dv[5], 1, DecisionOperator.int_eq);
        d6.buildNext();
        d6.buildNext();
        d6.apply();
        d6.setPrevious(d5);

        solver.getSearchLoop().setLastDecision(d6);

        solver.getSearchLoop().getSMList().beforeRestart();

    }

}
