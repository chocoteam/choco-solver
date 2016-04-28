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
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.SearchStrategyFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.VariableEvaluator;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.LastConflict;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayDeque;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.intVarSearch;
import static org.chocosolver.solver.search.strategy.assignments.DecisionOperator.int_reverse_split;
import static org.chocosolver.solver.search.strategy.assignments.DecisionOperator.int_split;
import static org.chocosolver.solver.search.strategy.selectors.ValSelectorFactory.midIntVal;
import static org.chocosolver.solver.search.strategy.selectors.VarSelectorFactory.minDomIntVar;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 oct. 2010
 */
public class StrategyTest {

    @Test(groups="1s", timeOut=60000)
    public void AssignmentTest() {
        int n = 100;

        Model s = new Model();
        IEnvironment env = s.getEnvironment();

        IntVar[] variables = new IntVar[n];
        for (int i = 0; i < n; i++) {
            variables[i] = s.intVar("V" + i, i, n + i, false);
        }
        Solver r = s.getSolver();
        r.set(inputOrderLBSearch(variables));

        env.worldPush();
        Decision decision = r.getStrategy().getDecision();
        ArrayDeque<Decision> stack = new ArrayDeque<>();
        stack.push(decision);
        testStrat(stack, variables, n, r);
    }

    private void testStrat(ArrayDeque<Decision> stack, IntVar[] variables, int n, Solver r) {
        IEnvironment env = r.getEnvironment();
        Decision decision = stack.peek();
        for (int i = 0; i < n; i++) {
            decision.buildNext();
            try {
                decision.apply();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertTrue(variables[i].isInstantiated());
            Assert.assertEquals(variables[i].getValue(), i);
            decision = r.getStrategy().getDecision();
            if(decision!=null)stack.push(decision);
            env.worldPush();
        }
        env.worldPop();
        for (int i = n - 1; i >= 0; i--) {
            decision = stack.pop();
            env.worldPop();
            decision.buildNext();
            try {
                decision.apply();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertFalse(variables[i].isInstantiated());
            Assert.assertFalse(variables[i].contains(i));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void AssignmentTest2() {
        int n = 100;

        Model s = new Model();
        Solver r = s.getSolver();
        IEnvironment env = s.getEnvironment();

        AbstractStrategy[] asgs = new AbstractStrategy[n];

        IntVar[] variables = new IntVar[n];
        for (int i = 0; i < n; i++) {
            variables[i] = s.intVar("V" + i, i, n + i, false);
            asgs[i] = inputOrderLBSearch(variables[i]);
        }
        r.set(asgs);

        env.worldPush();
        Decision decision = r.getStrategy().getDecision();
        ArrayDeque<Decision> stack = new ArrayDeque<>();
        stack.push(decision);
        testStrat(stack, variables, n, r);
    }


    @Test(groups="1s", timeOut=60000)
    public void testOnce() {
        Model model = new Model("OnceTest");
        IntVar x = model.intVar("x", 1, 2, false);
        IntVar[] v = {x};
        model.getSolver().set(SearchStrategyFactory.greedySearch(SearchStrategyFactory.inputOrderLBSearch(v)));
        model.solve();
        Assert.assertTrue(x.getValue() == 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNoScope() {
        Model model = new Model("OnceTest");
        IntVar[] x = model.intVarArray("x", 5, 1, 6, false);
        SetVar y = model.setVar("y", new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        model.allDifferent(x).post();
        model.member(x[0], y).post();
        model.solve();
        AbstractStrategy strat = model.getSolver().getStrategy();
        assertTrue(strat instanceof LastConflict);
    }

    @Test(groups="1s", timeOut=60000)
    public void testFirstFail1() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", 1, 5, false);
        IntVar v2 = model.intVar("v2", 3, 4, false);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new FirstFail(model);
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v2, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAntiFirstFail1() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", 1, 5, false);
        IntVar v2 = model.intVar("v2", 3, 4, false);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new AntiFirstFail();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLargest1() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", 1, 5, false);
        IntVar v2 = model.intVar("v2", 3, 4, false);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new Largest();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSmallest1() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", 1, 5, false);
        IntVar v2 = model.intVar("v2", 3, 4, false);
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new Smallest();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testOccurrence1() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", 1, 5, false);
        IntVar v2 = model.intVar("v2", 3, 4, false);
        IntVar[] vs = new IntVar[]{v1, v2};
        model.member(v1, 2, 3).post();
        model.member(v1, 3, 4).post();
        VariableSelector<IntVar> eval = new Occurrence<>();
        IntVar va = eval.getVariable(vs);
        assertEquals(v1, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMaxRegret1() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", new int[]{1, 5});
        IntVar v2 = model.intVar("v2", new int[]{3, 4});
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new MaxRegret();
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testInputOrder() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", new int[]{1, 5});
        IntVar v2 = model.intVar("v2", new int[]{3, 4});
        IntVar[] vs = new IntVar[]{v1, v2};
        VariableSelector<IntVar> eval = new InputOrder<>(model);
        IntVar va = eval.getVariable(vs);
        Assert.assertEquals(v1, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMinDelta1() {
        Model model = new Model();
        SetVar v1 = model.setVar("v1", new int[]{}, new int[]{1,2,3,4,5});
        VariableEvaluator<SetVar> eval = new MinDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(5.0, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMaxDelta1() {
        Model model = new Model();
        SetVar v1 = model.setVar("v1", new int[]{}, new int[]{1,2,3,4,5});
        VariableEvaluator<SetVar> eval = new MaxDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testFirstFail2() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", 1, 5, false);
        VariableEvaluator<IntVar> eval = new FirstFail(model);
        double va = eval.evaluate(v1);
        Assert.assertEquals(5.0, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAntiFirstFail2() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", 1, 5, false);
        VariableEvaluator<IntVar> eval = new AntiFirstFail();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLargest2() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", 1, 5, false);
        VariableEvaluator<IntVar> eval = new Largest();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSmallest2() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", 1, 5, false);
        VariableEvaluator<IntVar> eval = new Smallest();
        double va = eval.evaluate(v1);
        Assert.assertEquals(1.0, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testOccurrence2() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", 1, 5, false);
        model.member(v1, 2, 3).post();
        model.member(v1, 3, 4).post();
        VariableEvaluator<IntVar> eval = new Occurrence<>();
        double va = eval.evaluate(v1);
        assertEquals(-2.0, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMaxRegret2() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", new int[]{1, 5});
        VariableEvaluator<IntVar> eval = new MaxRegret();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-4.0, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMinDelta2() {
        Model model = new Model();
        SetVar v1 = model.setVar("v1", new int[]{}, new int[]{1,2,3,4,5});
        VariableEvaluator<SetVar> eval = new MinDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(5.0, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMaxDelta2() {
        Model model = new Model();
        SetVar v1 = model.setVar("v1", new int[]{}, new int[]{1,2,3,4,5});
        VariableEvaluator<SetVar> eval = new MaxDelta();
        double va = eval.evaluate(v1);
        Assert.assertEquals(-5.0, va);
    }

    @Test(groups="1s", timeOut=60000)
    public void testFH3321() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 2, 0, 2, false);
        Solver r = model.getSolver();
        r.set(intVarSearch(minDomIntVar(r.getModel()), midIntVal(true), int_split, X));
        model.getSolver().showDecisions();
        while (model.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 9);
    }

    @Test(groups="1s", timeOut=60000)
    public void testFH3322() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 2, 0, 2, false);
        Solver r = model.getSolver();
        r.set(intVarSearch(minDomIntVar(r.getModel()), midIntVal(false), int_reverse_split, X));
        model.getSolver().showDecisions();
        while (model.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 9);
    }


    @Test(groups="1s", timeOut=60000)
    public void testFH33232() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 2, 0, 2, false);
        Solver r = model.getSolver();
        r.set(intVarSearch(minDomIntVar(r.getModel()), midIntVal(true), int_split, X));
        model.getSolver().showDecisions();
        while (model.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 9);
    }

    @Test(groups="1s", timeOut=60000)
    public void testFH3324() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 2, 0, 2, false);
        Solver r = model.getSolver();
        r.set(intVarSearch(minDomIntVar(r.getModel()), midIntVal(false), int_reverse_split, X));
        model.getSolver().showDecisions();
        while (model.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 9);
    }
}
