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

package choco.strategy;

import memory.IEnvironment;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.ICF;
import solver.constraints.set.SetConstraintsFactory;
import solver.exception.ContradictionException;
import solver.search.strategy.ISF;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.selectors.IntValueSelector;
import solver.search.strategy.selectors.VariableEvaluator;
import solver.search.strategy.selectors.VariableSelector;
import solver.search.strategy.selectors.values.IntDomainMin;
import solver.search.strategy.selectors.variables.*;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.Once;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.VF;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 oct. 2010
 */
public class StrategyTest {

    @Test(groups = "1s")
    public void AssignmentTest() {
        int n = 100;

        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        IntVar[] variables = new IntVar[n];
        for (int i = 0; i < n; i++) {
            variables[i] = VariableFactory.enumerated("V" + i, i, n + i, s);
        }
        AbstractStrategy asg = IntStrategyFactory.inputOrder_InDomainMin(variables);

        s.set(asg);

        env.worldPush();
        Decision decision = asg.getDecision();
        for (int i = 0; i < n; i++) {
            decision.buildNext();
            try {
                decision.apply();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertTrue(variables[i].isInstantiated());
            Assert.assertEquals(variables[i].getValue(), i);
            Decision tmp = decision;
            decision = asg.getDecision();
            if (decision != null) {
                decision.setPrevious(tmp);
            } else {
                decision = tmp;
            }
            env.worldPush();
        }
        env.worldPop();
        for (int i = n - 1; i >= 0; i--) {
            env.worldPop();
            decision.buildNext();
            try {
                decision.apply();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertFalse(variables[i].isInstantiated());
            Assert.assertFalse(variables[i].contains(i));
            decision = decision.getPrevious();
        }
    }

    @Test(groups = "1s")
    public void AssignmentTest2() {
        int n = 100;

        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        AbstractStrategy[] asgs = new AbstractStrategy[n];

        IntVar[] variables = new IntVar[n];
        for (int i = 0; i < n; i++) {
            variables[i] = VariableFactory.enumerated("V" + i, i, n + i, s);
            asgs[i] = IntStrategyFactory.inputOrder_InDomainMin(new IntVar[]{variables[i]});
        }

        AbstractStrategy sts = ISF.sequencer(asgs);

        s.set(sts);

        env.worldPush();
        Decision decision = sts.getDecision();
        for (int i = 0; i < n; i++) {
            decision.buildNext();
            try {
                decision.apply();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertTrue(variables[i].isInstantiated());
            Assert.assertEquals(variables[i].getValue(), i);
            Decision tmp = decision;
            decision = sts.getDecision();
            if (decision != null) {
                decision.setPrevious(tmp);
            } else {
                decision = tmp;
            }
            env.worldPush();
        }
        env.worldPop();
        for (int i = n - 1; i >= 0; i--) {
            env.worldPop();
            decision.buildNext();
            try {
                decision.apply();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertFalse(variables[i].isInstantiated());
            Assert.assertFalse(variables[i].contains(i));
            decision = decision.getPrevious();
        }


    }


    @Test
    public void testOnce() {
        Solver solver = new Solver("OnceTest");
        IntVar x = VariableFactory.enumerated("x", 1, 2, solver);
        IntVar[] v = {x};
        VariableSelector varsel = new InputOrder<>();
        IntValueSelector valsel = new IntDomainMin();
        DecisionOperator assgnt = DecisionOperator.int_eq;
        solver.set(new Once(v, varsel, valsel, assgnt));
        solver.findSolution();
        Assert.assertTrue(x.getValue() == 1);
    }

    @Test
    public void testNoScope() {
        Solver solver = new Solver("OnceTest");
        IntVar[] x = VariableFactory.enumeratedArray("x", 5, 1, 6, solver);
        SetVar y = VariableFactory.set("y", 1, 10, solver);
        solver.post(ICF.alldifferent(x));
        solver.post(SetConstraintsFactory.member(x[0], y));
        solver.findSolution();
        AbstractStrategy strat = solver.getStrategy();
        Assert.assertTrue(strat instanceof StrategiesSequencer);
    }

    @Test
    public void testFirstFail1() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        IntVar v2 = VF.enumerated("v2", 3, 4, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new FirstFail();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v2, va);
    }

    @Test
    public void testAntiFirstFail1() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        IntVar v2 = VF.enumerated("v2", 3, 4, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new AntiFirstFail();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test
    public void testLargest1() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        IntVar v2 = VF.enumerated("v2", 3, 4, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new Largest();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test
    public void testSmallest1() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        IntVar v2 = VF.enumerated("v2", 3, 4, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new Smallest();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test
    public void testOccurrence1() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        IntVar v2 = VF.enumerated("v2", 3, 4, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        solver.post(ICF.member(v1, 2, 3));
        solver.post(ICF.member(v1, 3, 4));
        VariableSelector<IntVar> eval = new Occurrence<>();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test
    public void testMaxRegret1() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", new int[]{1, 5}, solver);
        IntVar v2 = VF.enumerated("v2", new int[]{3, 4}, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new MaxRegret();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test
    public void testInputOrder() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", new int[]{1, 5}, solver);
        IntVar v2 = VF.enumerated("v2", new int[]{3, 4}, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new InputOrder<>();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test
    public void testMinDelta1() {
        Solver solver = new Solver();
        SetVar v1 = VF.set("v1", 1, 5, solver);
        VariableEvaluator<SetVar> eval = new MinDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(5.0, va);
    }

    @Test
    public void testMaxDelta1() {
        Solver solver = new Solver();
        SetVar v1 = VF.set("v1", 1, 5, solver);
        VariableEvaluator<SetVar> eval = new MaxDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }

    @Test
    public void testFirstFail2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        VariableEvaluator<IntVar> eval = new FirstFail();
        double va = eval.evaluate(v1);
        Assert.assertEquals(5.0, va);
    }

    @Test
    public void testAntiFirstFail2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        VariableEvaluator<IntVar> eval = new AntiFirstFail();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }

    @Test
    public void testLargest2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        VariableEvaluator<IntVar> eval = new Largest();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }

    @Test
    public void testSmallest2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        VariableEvaluator<IntVar> eval = new Smallest();
        double va = eval.evaluate(v1);
        Assert.assertEquals(1.0, va);
    }

    @Test
    public void testOccurrence2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        solver.post(ICF.member(v1, 2, 3));
        solver.post(ICF.member(v1, 3, 4));
        VariableEvaluator<IntVar> eval = new Occurrence<>();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-2.0, va);
    }

    @Test
    public void testMaxRegret2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", new int[]{1, 5}, solver);
        VariableEvaluator<IntVar> eval = new MaxRegret();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-4.0, va);
    }

    @Test
    public void testMinDelta2() {
        Solver solver = new Solver();
        SetVar v1 = VF.set("v1", 1, 5, solver);
        VariableEvaluator<SetVar> eval = new MinDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(5.0, va);
    }

    @Test
    public void testMaxDelta2() {
        Solver solver = new Solver();
        SetVar v1 = VF.set("v1", 1, 5, solver);
        VariableEvaluator<SetVar> eval = new MaxDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }
}
