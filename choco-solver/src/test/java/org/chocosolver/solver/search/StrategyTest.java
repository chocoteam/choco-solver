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

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableEvaluator;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.LastConflict;
import org.chocosolver.solver.search.strategy.strategy.Once;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        AbstractStrategy asg = IntStrategyFactory.lexico_LB(variables);

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
            asgs[i] = IntStrategyFactory.lexico_LB(variables[i]);
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


    @Test(groups = "1s")
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

    @Test(groups = "1s")
    public void testNoScope() {
        Solver solver = new Solver("OnceTest");
        IntVar[] x = VariableFactory.enumeratedArray("x", 5, 1, 6, solver);
        SetVar y = VariableFactory.set("y", 1, 10, solver);
        solver.post(ICF.alldifferent(x));
        solver.post(SetConstraintsFactory.member(x[0], y));
        solver.findSolution();
        AbstractStrategy strat = solver.getStrategy();
        Assert.assertTrue(strat instanceof LastConflict);
    }

    @Test(groups = "1s")
    public void testFirstFail1() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        IntVar v2 = VF.enumerated("v2", 3, 4, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new FirstFail();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v2, va);
    }

    @Test(groups = "1s")
    public void testAntiFirstFail1() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        IntVar v2 = VF.enumerated("v2", 3, 4, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new AntiFirstFail();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test(groups = "1s")
    public void testLargest1() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        IntVar v2 = VF.enumerated("v2", 3, 4, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new Largest();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test(groups = "1s")
    public void testSmallest1() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        IntVar v2 = VF.enumerated("v2", 3, 4, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new Smallest();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test(groups = "1s")
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

    @Test(groups = "1s")
    public void testMaxRegret1() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", new int[]{1, 5}, solver);
        IntVar v2 = VF.enumerated("v2", new int[]{3, 4}, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new MaxRegret();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test(groups = "1s")
    public void testInputOrder() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", new int[]{1, 5}, solver);
        IntVar v2 = VF.enumerated("v2", new int[]{3, 4}, solver);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new InputOrder<>();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test(groups = "1s")
    public void testMinDelta1() {
        Solver solver = new Solver();
        SetVar v1 = VF.set("v1", 1, 5, solver);
        VariableEvaluator<SetVar> eval = new MinDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(5.0, va);
    }

    @Test(groups = "1s")
    public void testMaxDelta1() {
        Solver solver = new Solver();
        SetVar v1 = VF.set("v1", 1, 5, solver);
        VariableEvaluator<SetVar> eval = new MaxDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }

    @Test(groups = "1s")
    public void testFirstFail2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        VariableEvaluator<IntVar> eval = new FirstFail();
        double va = eval.evaluate(v1);
        Assert.assertEquals(5.0, va);
    }

    @Test(groups = "1s")
    public void testAntiFirstFail2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        VariableEvaluator<IntVar> eval = new AntiFirstFail();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }

    @Test(groups = "1s")
    public void testLargest2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        VariableEvaluator<IntVar> eval = new Largest();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }

    @Test(groups = "1s")
    public void testSmallest2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        VariableEvaluator<IntVar> eval = new Smallest();
        double va = eval.evaluate(v1);
        Assert.assertEquals(1.0, va);
    }

    @Test(groups = "1s")
    public void testOccurrence2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", 1, 5, solver);
        solver.post(ICF.member(v1, 2, 3));
        solver.post(ICF.member(v1, 3, 4));
        VariableEvaluator<IntVar> eval = new Occurrence<>();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-2.0, va);
    }

    @Test(groups = "1s")
    public void testMaxRegret2() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", new int[]{1, 5}, solver);
        VariableEvaluator<IntVar> eval = new MaxRegret();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-4.0, va);
    }

    @Test(groups = "1s")
    public void testMinDelta2() {
        Solver solver = new Solver();
        SetVar v1 = VF.set("v1", 1, 5, solver);
        VariableEvaluator<SetVar> eval = new MinDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(5.0, va);
    }

    @Test(groups = "1s")
    public void testMaxDelta2() {
        Solver solver = new Solver();
        SetVar v1 = VF.set("v1", 1, 5, solver);
        VariableEvaluator<SetVar> eval = new MaxDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }

    @Test(groups = "1s")
    public void testFH3321() {
        Solver solver = new Solver();
        IntVar[] X = VF.enumeratedArray("X", 2, 0, 2, solver);
        solver.set(ISF.custom(ISF.minDomainSize_var_selector(), new IntDomainMiddle(true), ISF.split(), X));
        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 9);
    }

    @Test(groups = "1s")
    public void testFH3322() {
        Solver solver = new Solver();
        IntVar[] X = VF.enumeratedArray("X", 2, 0, 2, solver);
        solver.set(ISF.custom(ISF.minDomainSize_var_selector(), new IntDomainMiddle(false), ISF.reverse_split(), X));
        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 9);
    }


    @Test(groups = "1s")
    public void testFH33232() {
        Solver solver = new Solver();
        IntVar[] X = VF.enumeratedArray("X", 2, 0, 2, solver);
        solver.set(ISF.dichotomic(ISF.minDomainSize_var_selector(), true, X));
        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 9);
    }

    @Test(groups = "1s")
    public void testFH3324() {
        Solver solver = new Solver();
        IntVar[] X = VF.enumeratedArray("X", 2, 0, 2, solver);
        solver.set(ISF.dichotomic(ISF.minDomainSize_var_selector(), false, X));
        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 9);
    }
}
