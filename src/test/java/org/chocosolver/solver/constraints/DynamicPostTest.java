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
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.reification.PropConditionnal;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import org.chocosolver.solver.propagation.hardcoded.TwoBucketPropagationEngine;
import org.chocosolver.solver.search.loop.monitors.IMonitorOpenNode;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.ProblemMaker;
import org.testng.annotations.Test;

import java.util.ArrayDeque;

import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.domOverWDegSearch;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;
import static org.chocosolver.util.ESat.*;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/12
 */
public class DynamicPostTest {

    PropagationEngineFactory engine;

    public DynamicPostTest(PropagationEngineFactory engine) {
        this.engine = engine;
    }

    public DynamicPostTest() {
        this(PropagationEngineFactory.DEFAULT);
    }

    @SuppressWarnings("UnusedDeclaration")
    @Test(groups="1s", timeOut=60000)
    public void test0() {
        final Model model = new Model();
        final IntVar X = model.intVar("X", 1, 2, false);
        final IntVar Y = model.intVar("Y", 1, 2, false);
        final IntVar Z = model.intVar("Z", 1, 2, false);
        model.getSolver().set(engine.make(model));
        while (model.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 8);
    }


    @Test(groups="1s", timeOut=60000)
    public void test1() {
        final Model model = new Model();
        final IntVar X = model.intVar("X", 1, 2, false);
        final IntVar Y = model.intVar("Y", 1, 2, false);
        final IntVar Z = model.intVar("Z", 1, 2, false);

        new Constraint("Conditionnal",
                new PropConditionnal(new IntVar[]{X, Y, Z},
                        new Constraint[]{model.arithm(X, "=", Y), model.arithm(Y, "=", Z)},
                        new Constraint[]{}) {
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
        model.getSolver().set(engine.make(model));
        while (model.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 7);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        final Model model = new Model();
        final IntVar X = model.intVar("X", 1, 2, false);
        final IntVar Y = model.intVar("Y", 1, 2, false);
        final IntVar Z = model.intVar("Z", 1, 2, false);
        model.getSolver().plugMonitor(new IMonitorOpenNode() {
            @Override
            public void beforeOpenNode() {
            }

            @Override
            public void afterOpenNode() {
                if (model.getSolver().getNodeCount() == 1) {
                    model.arithm(X, "=", Y).post();
                    model.arithm(Y, "=", Z).post();
                }
            }
        });
        model.getSolver().showDecisions();
        model.getSolver().showSolutions();
        model.getSolver().set(engine.make(model));
        while (model.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        final Model model = new Model();
        final IntVar X = model.intVar("X", 1, 2, false);
        final IntVar Y = model.intVar("Y", 1, 2, false);
        final IntVar Z = model.intVar("Z", 1, 2, false);
        Constraint c1 = model.arithm(X, "=", Y);
        Constraint c2 = model.arithm(X, "=", Z);
        c1.post();
        c2.post();
        model.unpost(c2);
        model.unpost(c1);
        model.getSolver().set(engine.make(model));
        while (model.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 8);
        assertEquals(model.getNbCstrs(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        final Model model = new Model();
        final IntVar X = model.intVar("X", 1, 2, false);
        final IntVar Y = model.intVar("Y", 1, 2, false);
        final IntVar Z = model.intVar("Z", 1, 2, false);
        final Constraint c1 = model.arithm(X, "=", Y);
        final Constraint c2 = model.arithm(X, "=", Z);
        c1.post();
        c2.post();
        ArrayDeque<Constraint> cs = new ArrayDeque<>();
        cs.add(c1);
        cs.add(c2);
        model.getSolver().plugMonitor((IMonitorSolution) () -> {
            while(cs.size()>0){
                model.unpost(cs.pop());
            }
        });
        model.getSolver().set(engine.make(model));
        while (model.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 5);
        assertEquals(model.getNbCstrs(), 0);
    }

    private static void popAll(ArrayDeque<Constraint> stack, Model model) {
        while(stack.size()>0) {
            model.unpost(stack.poll());
        }
    }

    private static void push(Constraint constraint, ArrayDeque<Constraint> stack, Model model) {
        stack.add(constraint);
        constraint.post();
    }

    private void pareto(boolean clauses, boolean svnQ){
        // Objectives are to maximize "a" and maximize "b".
        Model model = new Model();
        IntVar a = model.intVar("a", 0, 2, false);
        IntVar b = model.intVar("b", 0, 2, false);
        IntVar c = model.intVar("c", 0, 2, false);

        model.arithm(a, "+", b, "<", 3).post();

        // START extra variables/constraints for guided improvement algorithm
        ArrayDeque<Constraint> stack = new ArrayDeque<>();
        IntVar lbA = model.intVar("lbA", 0, 2, false);
        IntVar lbB = model.intVar("lbB", 0, 2, false);
        BoolVar aSBetter = model.arithm(a, ">", lbA).reify();
        BoolVar bSBetter = model.arithm(b, ">", lbB).reify();
        BoolVar aBetter = model.arithm(a, ">=", lbA).reify();
        BoolVar bBetter = model.arithm(b, ">=", lbB).reify();
        push(model.arithm(lbA, "=", a), stack, model);
        push(model.arithm(lbB, "=", b), stack, model);
        Constraint strictlyBetter
                = model.or(
                model.and(aSBetter, bBetter),
                model.and(aBetter, bSBetter));
        // END extra variables/constraints for guided improvement algorithm
        Solver r = model.getSolver();
        r.set(
                svnQ?
                        new SevenQueuesPropagatorEngine(model):
                        new TwoBucketPropagationEngine(model)
        );
        r.set(inputOrderLBSearch(a, b, c, lbA, lbB));
        int nbSolution = 0;
        while (model.solve()) {
            int bestA;
            int bestB;
            do {
                bestA = a.getValue();
                bestB = b.getValue();

                popAll(stack, model);
                push(model.arithm(lbA, "=", bestA), stack, model);
                push(model.arithm(lbB, "=", bestB), stack, model);
                push(strictlyBetter, stack, model);
            } while (model.solve());

            popAll(stack, model);

            push(model.arithm(a, "=", bestA), stack, model);
            push(model.arithm(b, "=", bestB), stack, model);
            push(model.arithm(lbA, "=", bestA), stack, model);
            push(model.arithm(lbB, "=", bestB), stack, model);

            model.getSolver().getEngine().flush();
            model.getSolver().reset();

            if (model.solve()) {
                do {
                    //System.out.println("Found pareto optimal solution: " + a + ", " + b + ", " + c);
                    nbSolution++;
                } while (model.solve());
            }

            popAll(stack, model);

            model.getSolver().getEngine().flush();
            model.getSolver().reset();

            if(clauses) {
                model.addClausesBoolOrArrayEqualTrue(new BoolVar[]{
                        model.arithm(a, ">", bestA).reify(),
                        model.arithm(b, ">", bestB).reify()
                });
            }else{
                model.or(
                        model.arithm(a, ">", bestA),
                        model.arithm(b, ">", bestB)).post();
            }
        }
        assertEquals(9, nbSolution);
    }

    @SuppressWarnings("UnusedDeclaration")
    @Test(groups="1s", timeOut=60000)
    public void testJLpareto() {
        pareto(false, true);
        pareto(false, false);
        pareto(true, true);
        pareto(true, false);

    }

    @Test(groups="1s", timeOut=60000)
    public void testIssue214() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 2, false);
        IntVar y = model.intVar("y", 1, 2, false);
        IntVar z = model.intVar("z", 1, 2, false);
        Constraint c = model.or(
                model.arithm(x, "<", y),
                model.arithm(x, "<", z));
        c.post();
        model.solve();
        model.unpost(c);
    }

    @Test(groups="10s", timeOut=60000)
    public void testCostas() {
        Model s1 = costasArray(7, false);
        Model s2 = costasArray(7, true);

        while (s1.solve()) ;
        out.println(s1.getSolver().getSolutionCount());

        while (s2.solve()) ;

        out.println(s2.getSolver().getSolutionCount());
        assertEquals(s1.getSolver().getSolutionCount(), s2.getSolver().getSolutionCount());
    }

    private Model costasArray(int n, boolean dynamic) {
        Model model = ProblemMaker.makeCostasArrays(n);
        IntVar[] vectors = (IntVar[]) model.getHook("vectors");
        model.getSolver().set(domOverWDegSearch(vectors));
        if (dynamic) {
            // should not change anything (the constraint is already posted)
            model.getSolver().plugMonitor((IMonitorSolution) () -> model.allDifferent(vectors, "BC").post());
        }
        return model;
    }
}
