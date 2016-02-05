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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.reification.PropConditionnal;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.search.loop.monitors.IMonitorOpenNode;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.chocosolver.solver.search.strategy.IntStrategyFactory.lexico_LB;
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
        final Solver solver = new Solver();
        final IntVar X = solver.intVar("X", 1, 2, false);
        final IntVar Y = solver.intVar("Y", 1, 2, false);
        final IntVar Z = solver.intVar("Z", 1, 2, false);
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 8);
    }


    @Test(groups="1s", timeOut=60000)
    public void test1() {
        final Solver solver = new Solver();
        final IntVar X = solver.intVar("X", 1, 2, false);
        final IntVar Y = solver.intVar("Y", 1, 2, false);
        final IntVar Z = solver.intVar("Z", 1, 2, false);

        new Constraint("Conditionnal",
                new PropConditionnal(new IntVar[]{X, Y, Z},
                        new Constraint[]{solver.arithm(X, "=", Y), solver.arithm(Y, "=", Z)},
                        new Constraint[]{}) {
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
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 7);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        final Solver solver = new Solver();
        final IntVar X = solver.intVar("X", 1, 2, false);
        final IntVar Y = solver.intVar("Y", 1, 2, false);
        final IntVar Z = solver.intVar("Z", 1, 2, false);
        solver.plugMonitor(new IMonitorOpenNode() {
            @Override
            public void beforeOpenNode() {
            }

            @Override
            public void afterOpenNode() {
                if (solver.getMeasures().getNodeCount() == 1) {
                    solver.arithm(X, "=", Y).post();
                    solver.arithm(Y, "=", Z).post();
                }
            }
        });
        Chatterbox.showDecisions(solver);
        Chatterbox.showSolutions(solver);
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        final Solver solver = new Solver();
        final IntVar X = solver.intVar("X", 1, 2, false);
        final IntVar Y = solver.intVar("Y", 1, 2, false);
        final IntVar Z = solver.intVar("Z", 1, 2, false);
        Constraint c1 = solver.arithm(X, "=", Y);
        Constraint c2 = solver.arithm(X, "=", Z);
        c1.post();
        c2.post();
        solver.unpost(c2);
        solver.unpost(c1);
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 8);
        assertEquals(solver.getNbCstrs(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        final Solver solver = new Solver();
        final IntVar X = solver.intVar("X", 1, 2, false);
        final IntVar Y = solver.intVar("Y", 1, 2, false);
        final IntVar Z = solver.intVar("Z", 1, 2, false);
        final Constraint c1 = solver.arithm(X, "=", Y);
        final Constraint c2 = solver.arithm(X, "=", Z);
        c1.post();
        c2.post();
        solver.plugMonitor((IMonitorSolution) () -> {
            solver.unpost(c1);
            solver.unpost(c2);
        });
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        assertEquals(solver.getMeasures().getSolutionCount(), 5);
        assertEquals(solver.getNbCstrs(), 0);
    }

    private static void popAll(List<Constraint> stack, Solver solver) {
        stack.forEach(solver::unpost);
    }

    private static void push(Constraint constraint, List<Constraint> stack, Solver solver) {
        stack.add(constraint);
        constraint.post();
    }

    @SuppressWarnings("UnusedDeclaration")
    @Test(groups="1s", timeOut=60000)
    public void testJLpareto() {
        // Objectives are to maximize "a" and maximize "b".
        Solver solver = new Solver();
        IntVar a = solver.intVar("a", 0, 2, false);
        IntVar b = solver.intVar("b", 0, 2, false);
        IntVar c = solver.intVar("c", 0, 2, false);

        solver.arithm(a, "+", b, "<", 3).post();

        // START extra variables/constraints for guided improvement algorithm
        List<Constraint> stack = new ArrayList<>();
        IntVar lbA = solver.intVar("lbA", 0, 2, false);
        IntVar lbB = solver.intVar("lbB", 0, 2, false);
        BoolVar aSBetter = solver.arithm(a, ">", lbA).reify();
        BoolVar bSBetter = solver.arithm(b, ">", lbB).reify();
        BoolVar aBetter = solver.arithm(a, ">=", lbA).reify();
        BoolVar bBetter = solver.arithm(b, ">=", lbB).reify();
        push(solver.arithm(lbA, "=", a), stack, solver);
        push(solver.arithm(lbB, "=", b), stack, solver);
        Constraint strictlyBetter
                = solver.or(
                solver.and(aSBetter, bBetter),
                solver.and(aBetter, bSBetter));
        // END extra variables/constraints for guided improvement algorithm
        solver.set(lexico_LB(a, b, c, lbA, lbB));
        int nbSolution = 0;
        while (solver.findSolution()) {
            int bestA;
            int bestB;
            do {
                bestA = a.getValue();
                bestB = b.getValue();

                popAll(stack, solver);
                push(solver.arithm(lbA, "=", bestA), stack, solver);
                push(solver.arithm(lbB, "=", bestB), stack, solver);
                push(strictlyBetter, stack, solver);
            } while (solver.nextSolution());

            popAll(stack, solver);

            push(solver.arithm(a, "=", bestA), stack, solver);
            push(solver.arithm(b, "=", bestB), stack, solver);
            push(solver.arithm(lbA, "=", bestA), stack, solver);
            push(solver.arithm(lbB, "=", bestB), stack, solver);

            solver.getEngine().flush();
            solver.getSearchLoop().reset();

            if (solver.findSolution()) {
                do {
                    //System.out.println("Found pareto optimal solution: " + a + ", " + b + ", " + c);
                    nbSolution++;
                } while (solver.nextSolution());
            }

            popAll(stack, solver);

            solver.getEngine().flush();
            solver.getSearchLoop().reset();

            solver.or(
                    solver.arithm(a, ">", bestA),
                    solver.arithm(b, ">", bestB)).post();
        }
        assertEquals(9, nbSolution);
    }

    @Test(groups="1s", timeOut=60000)
    public void testIssue214() {
        Solver solver = new Solver();
        IntVar x = solver.intVar("x", 1, 2, false);
        IntVar y = solver.intVar("y", 1, 2, false);
        IntVar z = solver.intVar("z", 1, 2, false);
        Constraint c = solver.or(
                solver.arithm(x, "<", y),
                solver.arithm(x, "<", z));
        c.post();
        solver.findSolution();
        solver.unpost(c);
    }

    @Test(groups="10s", timeOut=60000)
    public void testCostas() {
        Solver s1 = costasArray(7, false);
        Solver s2 = costasArray(7, true);

        s1.findAllSolutions();
        System.out.println(s1.getMeasures().getSolutionCount());

        s2.findAllSolutions();

        System.out.println(s2.getMeasures().getSolutionCount());
        Assert.assertEquals(s1.getMeasures().getSolutionCount(), s2.getMeasures().getSolutionCount());
    }

    private Solver costasArray(int n, boolean dynamic) {
        Solver solver = ProblemMaker.makeCostasArrays(n);
        IntVar[] vectors = (IntVar[]) solver.getHook("vectors");
        solver.set(ISF.domOverWDeg(vectors, 0));

        if (dynamic) {
            // should not change anything (the constraint is already posted)
            solver.plugMonitor((IMonitorSolution) () -> solver.allDifferent(vectors, "BC").post());
        }
        return solver;
    }
}
