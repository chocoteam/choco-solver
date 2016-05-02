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


import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.reification.PropConditionnal;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.objective.ObjectiveStrategy;
import org.chocosolver.solver.objective.OptimizationPolicy;
import org.chocosolver.solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

import static java.lang.Math.floorDiv;
import static java.lang.System.nanoTime;
import static org.chocosolver.solver.ResolutionPolicy.MAXIMIZE;
import static org.chocosolver.solver.ResolutionPolicy.MINIMIZE;
import static org.chocosolver.solver.objective.ObjectiveManager.walkingIntVarCutComputer;
import static org.chocosolver.solver.propagation.NoPropagationEngine.SINGLETON;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.minDomLBSearch;
import static org.chocosolver.util.ESat.*;
import static org.chocosolver.util.ProblemMaker.makeGolombRuler;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/07/13
 */
public class ObjectiveTest {

    @Test(groups = "10s", timeOut = 60000)
    public void test1() {
        Model model = new Model();

        IntVar iv = model.intVar("iv", -5, 15, false);
        model.arithm(iv, ">=", 0).post();
        model.arithm(iv, "<=", 10).post();
        Random rnd = new Random();
        for (int i = 0; i < 2000; i++) {
            rnd.setSeed(i);
            int k = rnd.nextInt(4);
            switch (k) {
                case 0:
                    one(model, iv);
                    break;
                case 1:
                    all(model, iv);
                    break;
                case 2:
                    min(model, iv);
                    break;
                case 3:
                    max(model, iv);
                    break;
            }
        }
    }

    @SuppressWarnings("UnusedParameters")
    private void one(Model model, IntVar iv) {
        for (int i = 0; i < 2; i++) {
            model.getSolver().reset();
            model.solve();
            Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
            Assert.assertEquals(model.getSolver().getNodeCount(), 2);
        }
    }

    @SuppressWarnings("UnusedParameters")
    private void all(Model model, IntVar iv) {
        for (int i = 0; i < 2; i++) {
            model.getSolver().reset();
            while (model.solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 11);
            assertEquals(model.getSolver().getNodeCount(), 21);
        }
    }

    private void min(Model model, IntVar iv) {
        for (int i = 0; i < 2; i++) {
            model.getSolver().reset();
            model.setObjective(MINIMIZE, iv);
            while (model.solve()) ;
            assertEquals(model.getSolver().getBestSolutionValue(), 0);
            assertEquals(model.getSolver().getNodeCount(), 2);
        }
    }

    private void max(Model model, IntVar iv) {
        for (int i = 0; i < 2; i++) {
            model.getSolver().reset();
            model.setObjective(MAXIMIZE, iv);
            while (model.solve()) ;
            assertEquals(model.getSolver().getBestSolutionValue(), 10);
            assertEquals(model.getSolver().getNodeCount(), 21);
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() {
        Model model = new Model();
        IntVar iv = model.intVar("iv", 0, 10, false);
        model.arithm(iv, ">=", 2).post();

        model.setObjective(MINIMIZE, iv);
        int value = 11;
        while (model.solve()) {
            value = iv.getValue();
        }
        assertEquals(value, 2);

        model.getSolver().reset();

        value = 11;
        model.setObjective(MINIMIZE, iv);
        while (model.solve()) {
            value = iv.getValue();
        }
        assertEquals(value, 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3() {
        final Model model = new Model();
        final IntVar iv = model.intVar("iv", 0, 10, false);
        model.arithm(iv, ">=", 2).post();

        new Constraint("Conditionnal",
                new PropConditionnal(new IntVar[]{iv},
                        new Constraint[]{model.arithm(iv, ">=", 4)},
                        new Constraint[]{model.TRUE()}) {
                    @Override
                    public ESat checkCondition() {
                        int nbNode = (int) this.model.getSolver().getNodeCount();
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
        model.solve();
        assertEquals(iv.getValue(), 2);

        model.getSolver().reset();
        model.getSolver().plugMonitor((IMonitorSolution) () -> model.arithm(iv, ">=", 6).post());
        model.solve();
        assertEquals(iv.getValue(), 2);

        model.getSolver().reset();
        model.solve();
        assertEquals(iv.getValue(), 6);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test4() {
        Model model = new Model();
        IntVar iv = model.intVar("iv", 0, 10, false);
        BoolVar v = model.arithm(iv, "<=", 2).reify();

        model.setObjective(MINIMIZE, v);
        while (model.solve()) ;
//        System.out.println("Minimum1: " + iv + " : " + solver.isSatisfied());
        model.getSolver().reset();

        model.setObjective(MINIMIZE, v);
        while (model.solve()) ;
//        System.out.println("Minimum2: " + iv + " : " + solver.isSatisfied());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testJL1() {
        Model model = new Model();
        BoolVar b1 = model.boolVar("b1");
        BoolVar b2 = model.boolVar("b2");
        model.arithm(b1, "<=", b2).post();
        model.setObjective(MINIMIZE, b1);
        Solver r = model.getSolver();
//        SMF.log(solver, true, true);
        //search.plugSearchMonitor(new LastSolutionRecorder(new Solution(), true, solver));
        if (r.getEngine() == SINGLETON) {
            r.set(new SevenQueuesPropagatorEngine(model));
        }
        r.getMeasures().setReadingTimeCount(nanoTime());
        while (model.solve()) ;
//        System.out.println(b1 + " " + b2);
        int bestvalue = b1.getValue();
        r.reset();
        r.set(ObjectiveManager.SAT());
        model.arithm(b1, "=", bestvalue).post();
        r.set(inputOrderLBSearch(new BoolVar[]{b1, b2}));
        int count = 0;
        if (model.solve()) {
            do {
                count++;
//                System.out.println(b1 + " " + b2);
            } while (model.solve());
        }
        assertEquals(count, 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testJL2() {
        Model model = new Model();
        IntVar a = model.intVar("a", -2, 2, false);
        Solver r = model.getSolver();
        model.setObjective(MAXIMIZE, a);
        r.set(new ObjectiveStrategy(a, OptimizationPolicy.TOP_DOWN), minDomLBSearch(a));
        r.setNoGoodRecordingFromSolutions(a);
        while (model.solve()) ;
        Assert.assertEquals(model.getSolver().isStopCriterionMet(), false);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testAdaptiveCut1() {
        Model model = makeGolombRuler(8);
        IntVar objective = (IntVar) model.getHook("objective");
        final int[] ends = {5, 2, 1};
        model.setObjective(MINIMIZE, objective);
        ObjectiveManager<IntVar, Integer> oman = model.getSolver().getObjectiveManager();
        oman.setCutComputer(n -> n - 10);
        int best = objective.getUB();
        for (int i = 0; i < 4; i++) {
            while (model.solve()) {
                best = objective.getValue();
            }
            model.getSolver().reset();
            final int finalI = i;
            oman.setCutComputer(n -> n - ends[finalI]);
            oman.updateBestUB(best);
        }
        assertEquals(best, 34);
        assertEquals(model.getSolver().getSolutionCount(), 0); // the last resolution fails at finding solutions
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testAdaptiveCut2() {
        Model model = makeGolombRuler(8);
        IntVar objective = (IntVar) model.getHook("objective");
        final int[] ends = {10};
        model.setObjective(MINIMIZE, objective);
        ObjectiveManager<IntVar, Integer> oman = model.getSolver().getObjectiveManager();
        oman.setCutComputer(n -> n - ends[0]);
        int best = objective.getUB();
        for (int i = 0; i < 4; i++) {
            while (model.solve()) {
                best = objective.getValue();
            }
            model.getSolver().reset();
            ends[0] = floorDiv(ends[0], 2);
            oman.updateBestUB(best);
        }
        assertEquals(best, 34);
        assertEquals(model.getSolver().getSolutionCount(), 0); // the last resolution fails at finding solutions
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testAdaptiveCut3() {
        Model model = makeGolombRuler(8);
        IntVar objective = (IntVar) model.getHook("objective");
        model.setObjective(MINIMIZE, objective);
        ObjectiveManager<IntVar, Integer> oman = model.getSolver().getObjectiveManager();
        oman.setCutComputer(walkingIntVarCutComputer());
        int best = objective.getUB();
        for (int i = 0; i < 4; i++) {
            while (model.solve()) {
                best = objective.getValue();
            }
        }
        assertEquals(best, 34);
        assertEquals(model.getSolver().getSolutionCount(), 21);
    }
}
