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
package org.chocosolver.solver.search;


import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.reification.PropConditionnal;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.objective.ObjectiveStrategy;
import org.chocosolver.solver.objective.OptimizationPolicy;
import org.chocosolver.solver.propagation.NoPropagationEngine;
import org.chocosolver.solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

import static java.lang.System.nanoTime;
import static org.chocosolver.solver.ResolutionPolicy.MINIMIZE;
import static org.chocosolver.solver.propagation.NoPropagationEngine.SINGLETON;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.lexico_LB;
import static org.chocosolver.util.ESat.*;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/07/13
 */
public class ObjectiveTest {

    @Test(groups="10s", timeOut=60000)
    public void test1() {
        Solver solver = new Solver();

        IntVar iv = solver.intVar("iv", -5, 15, false);
        solver.arithm(iv, ">=", 0).post();
        solver.arithm(iv, "<=", 10).post();
        Random rnd = new Random();
        for (int i = 0; i < 2000; i++) {
            rnd.setSeed(i);
            int k = rnd.nextInt(4);
            switch (k) {
                case 0:
                    one(solver, iv);
                    break;
                case 1:
                    all(solver, iv);
                    break;
                case 2:
                    min(solver, iv);
                    break;
                case 3:
                    max(solver, iv);
                    break;
            }
        }
    }

    @SuppressWarnings("UnusedParameters")
    private void one(Solver solver, IntVar iv) {
        for (int i = 0; i < 2; i++) {
            solver.getSearchLoop().reset();
            solver.findSolution();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
            Assert.assertEquals(solver.getMeasures().getNodeCount(), 2);
        }
    }

    @SuppressWarnings("UnusedParameters")
    private void all(Solver solver, IntVar iv) {
        for (int i = 0; i < 2; i++) {
            solver.getSearchLoop().reset();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 11);
            Assert.assertEquals(solver.getMeasures().getNodeCount(), 21);
        }
    }

    private void min(Solver solver, IntVar iv) {
        for (int i = 0; i < 2; i++) {
            solver.getSearchLoop().reset();
            solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, iv);
            Assert.assertEquals(solver.getMeasures().getBestSolutionValue(), 0);
            Assert.assertEquals(solver.getMeasures().getNodeCount(), 2);
        }
    }

    private void max(Solver solver, IntVar iv) {
        for (int i = 0; i < 2; i++) {
            solver.getSearchLoop().reset();
            solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, iv);
            Assert.assertEquals(solver.getMeasures().getBestSolutionValue(), 10);
            Assert.assertEquals(solver.getMeasures().getNodeCount(), 21);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Solver solver = new Solver();
        IntVar iv = solver.intVar("iv", 0, 10, false);
        solver.arithm(iv, ">=", 2).post();

        solver.findOptimalSolution(MINIMIZE, iv);
        assertEquals(solver.getSolutionRecorder().getLastSolution().getIntVal(iv).intValue(), 2);

        solver.getSearchLoop().reset();

        solver.findOptimalSolution(MINIMIZE, iv);
        assertEquals(solver.getSolutionRecorder().getLastSolution().getIntVal(iv).intValue(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        final Solver solver = new Solver();
        final IntVar iv = solver.intVar("iv", 0, 10, false);
        solver.arithm(iv, ">=", 2).post();

        new Constraint("Conditionnal",
                new PropConditionnal(new IntVar[]{iv},
                        new Constraint[]{solver.arithm(iv, ">=", 4)},
                        new Constraint[]{solver.TRUE()}) {
                    @Override
                    public ESat checkCondition() {
                        int nbNode = (int) solver.getMeasures().getNodeCount();
                        switch (nbNode) {
                            case 0:
                            case 1:
                                return UNDEFINED;
                            case 2:
                                return TRUE;
                            default:
                                return FALSE;
                        }

                    }
                }).post();
        solver.findSolution();
        assertEquals(iv.getValue(), 2);

        solver.getSearchLoop().reset();
        solver.plugMonitor((IMonitorSolution) () -> solver.arithm(iv, ">=", 6).post());
        solver.findSolution();
        assertEquals(iv.getValue(), 2);

        solver.getSearchLoop().reset();
        solver.findSolution();
        assertEquals(iv.getValue(), 6);
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Solver solver = new Solver();
        IntVar iv = solver.intVar("iv", 0, 10, false);
        BoolVar v = solver.arithm(iv, "<=", 2).reify();

        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, v);
//        System.out.println("Minimum1: " + iv + " : " + solver.isSatisfied());
        solver.getSearchLoop().reset();

        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, v);
//        System.out.println("Minimum2: " + iv + " : " + solver.isSatisfied());
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL1() {
        Solver solver = new Solver();
        BoolVar b1 = solver.boolVar("b1");
        BoolVar b2 = solver.boolVar("b2");
        solver.arithm(b1, "<=", b2).post();
//        SMF.log(solver, true, true);
        solver.set(new ObjectiveManager<IntVar, Integer>(b1, MINIMIZE, true));
        //search.plugSearchMonitor(new LastSolutionRecorder(new Solution(), true, solver));
        if (solver.getEngine() == SINGLETON) {
            solver.set(new SevenQueuesPropagatorEngine(solver));
        }
        solver.getMeasures().setReadingTimeCount(nanoTime());
        solver.getSearchLoop().launch(false);
//        System.out.println(b1 + " " + b2);
        int bestvalue = b1.getValue();
        solver.getSearchLoop().reset();
        solver.arithm(b1, "=", bestvalue).post();
        solver.set(lexico_LB(new BoolVar[]{b1, b2}));
        int count = 0;
        if (solver.findSolution()) {
            do {
                count++;
//                System.out.println(b1 + " " + b2);
            } while (solver.nextSolution());
        }
        assertEquals(count, 2);
    }

	@Test(groups="1s", timeOut=60000)
	public void testJL2() {
		Solver solver = new Solver();
        IntVar a = solver.intVar("a", -2, 2, false);

		solver.set(
				new ObjectiveStrategy(a,OptimizationPolicy.TOP_DOWN),
				ISF.minDom_LB(a));
		SMF.nogoodRecordingOnSolution(new IntVar[]{a});

		solver.findAllOptimalSolutions(ResolutionPolicy.MAXIMIZE, a, false);
		Assert.assertEquals(solver.hasReachedLimit(),false);
	}
}
