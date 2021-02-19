/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.DefaultSettings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.reification.PropConditional;
import org.chocosolver.solver.search.loop.monitors.IMonitorOpenNode;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.annotations.Test;

import java.util.ArrayDeque;

import static org.chocosolver.solver.search.strategy.Search.domOverWDegSearch;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/12
 */
public class DynamicPostTest {

    @SuppressWarnings("UnusedDeclaration")
    @Test(groups = "1s", timeOut = 60000)
    public void test0() {
        final Model model = new Model();
        final IntVar X = model.intVar("X", 1, 2, false);
        final IntVar Y = model.intVar("Y", 1, 2, false);
        final IntVar Z = model.intVar("Z", 1, 2, false);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 8);
    }


    @Test(groups = "1s", timeOut = 60000)
    public void test1() {
        final Model model = new Model();
        final IntVar X = model.intVar("X", 1, 2, false);
        final IntVar Y = model.intVar("Y", 1, 2, false);
        final IntVar Z = model.intVar("Z", 1, 2, false);

        model.conditional(
                new IntVar[]{X, Y, Z},
                vars -> vars[0]
                        .getModel()
                        .getSolver()
                        .getNodeCount() > 1,
                () -> model.getSolver().getNodeCount()==2?
                        new Constraint[]{model.arithm(X, "=", Y), model.arithm(Y, "=", Z)}:
                        null).post();
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 7);
    }

    @Test(groups = "1s", timeOut = 60000)
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


        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
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
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 8);
        assertEquals(model.getNbCstrs(), 0);
    }

    @Test(groups = "1s", timeOut = 60000)
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
            while (cs.size() > 0) {
                model.unpost(cs.pop());
            }
        });
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 5);
        assertEquals(model.getNbCstrs(), 0);
    }

    private static void popAll(ArrayDeque<Constraint> stack, Model model) {
        while (stack.size() > 0) {
            model.unpost(stack.poll());
        }
    }

    private static void push(Constraint constraint, ArrayDeque<Constraint> stack, Model model) {
        stack.add(constraint);
        constraint.post();
    }

    private void pareto(boolean clauses) {
        // Objectives are to maximize "a" and maximize "b".
        Model model = new Model(new DefaultSettings().setSwapOnPassivate(false));
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
        r.setSearch(inputOrderLBSearch(a, b, c, lbA, lbB));
        int nbSolution = 0;
        while (model.getSolver().solve()) {
            int bestA;
            int bestB;
            strictlyBetter.post();
            do {
                bestA = a.getValue();
                bestB = b.getValue();

                popAll(stack, model);
                push(model.arithm(lbA, "=", bestA), stack, model);
                push(model.arithm(lbB, "=", bestB), stack, model);
            } while (model.getSolver().solve());

            popAll(stack, model);
            push(model.arithm(a, "=", bestA), stack, model);
            push(model.arithm(b, "=", bestB), stack, model);
            push(model.arithm(lbA, "=", bestA), stack, model);
            push(model.arithm(lbB, "=", bestB), stack, model);

            model.getSolver().getEngine().flush();
            model.getSolver().reset();
            model.unpost(strictlyBetter);

            if (model.getSolver().solve()) {
                do {
                    //System.out.println("Found pareto optimal solution: " + a + ", " + b + ", " + c);
                    nbSolution++;
                } while (model.getSolver().solve());
            }

            popAll(stack, model);

            model.getSolver().getEngine().flush();
            model.getSolver().reset();

            if (clauses) {
                model.addClausesBoolOrArrayEqualTrue(new BoolVar[]{
                        model.arithm(a, ">", bestA).reify(),
                        model.arithm(b, ">", bestB).reify()
                });
            } else {
                model.or(
                        model.arithm(a, ">", bestA),
                        model.arithm(b, ">", bestB)).post();
            }
        }
        assertEquals(9, nbSolution);
    }

    @SuppressWarnings("UnusedDeclaration")
    @Test(groups = "1s", timeOut = 60000)
    public void testJLpareto() {
        pareto(false);
        pareto(true);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testIssue214() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 2, false);
        IntVar y = model.intVar("y", 1, 2, false);
        IntVar z = model.intVar("z", 1, 2, false);
        Constraint c = model.or(
                model.arithm(x, "<", y),
                model.arithm(x, "<", z));
        c.post();
        model.getSolver().solve();
        model.unpost(c);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testCostas() {
        Model s1 = costasArray(7, false);
        Model s2 = costasArray(7, true);

        while (s1.getSolver().solve()) ;

        while (s2.getSolver().solve()) ;

        assertEquals(s1.getSolver().getSolutionCount(), s2.getSolver().getSolutionCount());
    }

    private Model costasArray(int n, boolean dynamic) {
        Model model = ProblemMaker.makeCostasArrays(n);
        IntVar[] vectors = (IntVar[]) model.getHook("vectors");
        model.getSolver().setSearch(domOverWDegSearch(vectors));
        if (dynamic) {
            // should not change anything (the constraint is already posted)
            model.getSolver().plugMonitor((IMonitorSolution) () -> model.allDifferent(vectors, "BC").post());
        }
        return model;
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test11() {
        final Model model = new Model();
        final IntVar X = model.intVar("X", 3, 5, false);
        final IntVar Y = model.intVar("Y", 3, 5, false);
        model.conditional(
                new IntVar[]{X, Y},
                PropConditional.ALL_INSTANTIATED,
                () -> {
                    if (X.isInstantiatedTo(3)) {
                        return new Constraint[]{model.arithm(Y, "=", 4)};
                    } else if (X.isInstantiatedTo(4)) {
                        return new Constraint[]{model.arithm(Y, "=", 3)};
                    } else {
                        return null;
                    }
                }
        ).post();
        while (model.getSolver().solve());
        assertEquals(model.getSolver().getSolutionCount(), 5);
    }
}
