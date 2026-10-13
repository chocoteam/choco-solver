/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Tests of {@link ModelSpec} built by hand, without recorder.
 *
 * @author Charles Prud'homme
 */
public class ModelSpecTest {

    /**
     * Enumerate all solutions with a deterministic search.
     *
     * @return solutions, nodes and fails
     */
    private static String solveAll(Model model, IntVar... decisions) {
        Solver s = model.getSolver();
        s.setSearch(Search.inputOrderLBSearch(decisions));
        while (s.solve()) ;
        return s.getSolutionCount() + " sols, " + s.getNodeCount() + " nodes, " + s.getFailCount() + " fails";
    }

    private record Queens(ModelSpec spec, int[] q) {
    }

    private static Queens queensSpec(int n) {
        SpecBuilder b = new SpecBuilder(Header.of("queens"));
        int[] q = b.callArray(n, "intVarArray", "q", n, 1, n);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                b.post(b.call("arithm", b.ref(q[i]), "!=", b.ref(q[j])));
                b.post(b.call("arithm", b.ref(q[i]), "!=", b.ref(q[j]), "+", j - i));
                b.post(b.call("arithm", b.ref(q[i]), "!=", b.ref(q[j]), "-", j - i));
            }
        }
        return new Queens(b.build(), q);
    }

    private static String queensDirect(int n) {
        Model m = new Model("queens");
        IntVar[] q = m.intVarArray("q", n, 1, n);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                m.arithm(q[i], "!=", q[j]).post();
                m.arithm(q[i], "!=", q[j], "+", j - i).post();
                m.arithm(q[i], "!=", q[j], "-", j - i).post();
            }
        }
        return solveAll(m, q);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testQueensSameSearchAsDirectModel() {
        Queens qs = queensSpec(8);
        Resolver i1 = qs.spec().instantiate();
        Resolver i2 = qs.spec().instantiate();
        Assert.assertNotSame(i1.model(), i2.model());
        Assert.assertEquals(i1.model().getNbVars(), 8);
        Assert.assertEquals(i1.model().getNbCstrs(), 3 * 28);
        String direct = queensDirect(8);
        Assert.assertEquals(solveAll(i1.model(), i1.intVars(qs.q())), direct);
        Assert.assertEquals(solveAll(i2.model(), i2.intVars(qs.q())), direct);
        Assert.assertTrue(direct.startsWith("92 sols"));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testInstancesAreIndependent() {
        Queens qs = queensSpec(4);
        Resolver i1 = qs.spec().instantiate();
        Resolver i2 = qs.spec().instantiate();
        for (int id : qs.q()) {
            IntVar v1 = i1.intVar(id);
            IntVar v2 = i2.intVar(id);
            Assert.assertNotSame(v1, v2);
            Assert.assertSame(v1.getModel(), i1.model());
            Assert.assertSame(v2.getModel(), i2.model());
            Assert.assertEquals(i1.idOf(v1), id);
            Assert.assertEquals(i1.idOf(v2), -1);
        }
    }

    @Test(groups = "10s", timeOut = 120000)
    public void testConcurrentInstantiations() throws Exception {
        Queens qs = queensSpec(8);
        String direct = queensDirect(8);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (int k = 0; k < 16; k++) {
                futures.add(pool.submit(() -> {
                    Resolver inst = qs.spec().instantiate();
                    return solveAll(inst.model(), inst.intVars(qs.q()));
                }));
            }
            for (Future<String> f : futures) {
                Assert.assertEquals(f.get(), direct);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testHeader() {
        Header h = Header.of("h").withSeed(42);
        Model m = new ModelSpec(h, List.of()).instantiate().model();
        Assert.assertEquals(m.getName(), "h");
        Assert.assertEquals(m.getSeed(), 42L);
        Assert.assertSame(m.getSettings(), h.settings());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testVarargsAndAllDifferent() {
        SpecBuilder b = new SpecBuilder(Header.of("ad"));
        int[] x = b.callArray(3, "intVarArray", "x", 3, 1, 3);
        b.post(b.call("allDifferent", b.refs(IntVar.class, x)));
        Resolver inst = b.build().instantiate();
        Assert.assertEquals(solveAll(inst.model(), inst.intVars(x)).split(",")[0], "6 sols");
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testReificationSteps() {
        // direct
        Model m = new Model();
        IntVar x = m.intVar("x", 0, 5);
        Constraint c1 = m.arithm(x, ">", 2);
        BoolVar b1 = c1.reify();
        BoolVar b2 = m.boolVar("b2");
        m.arithm(x, "<", 4).reifyWith(b2);
        BoolVar b3 = m.boolVar("b3");
        m.arithm(x, "=", 0).implies(b3);
        BoolVar b4 = m.boolVar("b4");
        m.arithm(x, "=", 5).impliedBy(b4);
        m.sum(new BoolVar[]{b1, b2}, "=", 1).post();
        String direct = solveAll(m, x, b1, b2, b3, b4);
        // spec
        SpecBuilder b = new SpecBuilder(Header.of("reif"));
        int ix = b.call("intVar", "x", 0, 5);
        int ib1 = b.reify(b.call("arithm", b.ref(ix), ">", 2));
        int ib2 = b.call("boolVar", "b2");
        b.reifyWith(b.call("arithm", b.ref(ix), "<", 4), ib2);
        int ib3 = b.call("boolVar", "b3");
        b.implies(b.call("arithm", b.ref(ix), "=", 0), ib3);
        int ib4 = b.call("boolVar", "b4");
        b.impliedBy(b.call("arithm", b.ref(ix), "=", 5), ib4);
        b.post(b.call("sum", b.refs(BoolVar.class, ib1, ib2), "=", 1));
        Resolver inst = b.build().instantiate();
        Assert.assertEquals(inst.model().getNbCstrs(), m.getNbCstrs());
        Assert.assertEquals(solveAll(inst.model(), inst.intVars(ix, ib1, ib2, ib3, ib4)), direct);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnpost() {
        SpecBuilder b = new SpecBuilder(Header.of("unpost"));
        int x = b.call("intVar", "x", 0, 3);
        int c = b.call("arithm", b.ref(x), ">", 1);
        b.post(c).unpost(c);
        Resolver inst = b.build().instantiate();
        Assert.assertEquals(inst.model().getNbCstrs(), 0);
        Assert.assertEquals(solveAll(inst.model(), inst.intVar(x)).split(",")[0], "4 sols");
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testTableSharesFrozenTuples() {
        Tuples tuples = new Tuples(true);
        tuples.add(1, 2);
        tuples.add(2, 1);
        tuples.add(3, 3);
        SpecBuilder b = new SpecBuilder(Header.of("table"));
        int[] x = b.callArray(2, "intVarArray", "x", 2, 1, 3);
        b.post(b.call("table", b.refs(IntVar.class, x), tuples));
        ModelSpec spec = b.build();
        Assert.assertTrue(tuples.isFrozen());
        Assert.assertThrows(SolverException.class, () -> tuples.add(1, 1));
        Assert.assertThrows(SolverException.class, tuples::sort);
        for (int k = 0; k < 2; k++) {
            Resolver inst = spec.instantiate();
            Assert.assertEquals(solveAll(inst.model(), inst.intVars(x)).split(",")[0], "3 sols");
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testArraysAreCopied() {
        int[] coeffs = {1, 2, 3};
        SpecBuilder b = new SpecBuilder(Header.of("scalar"));
        int[] x = b.callArray(3, "intVarArray", "x", 3, 0, 3);
        b.post(b.call("scalar", b.refs(IntVar.class, x), coeffs, "=", 6));
        ModelSpec spec = b.build();
        coeffs[0] = 100;
        Model m = new Model();
        IntVar[] y = m.intVarArray("x", 3, 0, 3);
        m.scalar(y, new int[]{1, 2, 3}, "=", 6).post();
        Resolver inst = spec.instantiate();
        Assert.assertEquals(solveAll(inst.model(), inst.intVars(x)), solveAll(m, y));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAutomatonIsCopied() {
        FiniteAutomaton fa = new FiniteAutomaton("(1|2)3*1");
        SpecBuilder b = new SpecBuilder(Header.of("regular"));
        int[] x = b.callArray(4, "intVarArray", "x", 4, 0, 3);
        b.post(b.call("regular", b.refs(IntVar.class, x), fa));
        ModelSpec spec = b.build();
        Step.Call call = (Step.Call) spec.steps().get(1);
        Object captured = ((Arg.Const) call.args()[1]).value();
        Assert.assertNotSame(captured, fa);
        Assert.assertNotSame(((Arg.Const) call.args()[1]).value(), captured);
        Model m = new Model();
        IntVar[] y = m.intVarArray("x", 4, 0, 3);
        m.regular(y, fa).post();
        String direct = solveAll(m, y);
        Assert.assertTrue(direct.startsWith("2 sols"));
        Resolver inst = spec.instantiate();
        Assert.assertEquals(solveAll(inst.model(), inst.intVars(x)), direct);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAutomatonCloneDoesNotShareBuffer() throws Exception {
        FiniteAutomaton fa = new FiniteAutomaton("(1|2)3*1");
        FiniteAutomaton copy = fa.clone();
        Field nexts = FiniteAutomaton.class.getDeclaredField("nexts");
        nexts.setAccessible(true);
        Assert.assertNotSame(nexts.get(copy), nexts.get(fa));
        Assert.assertEquals(copy.isDeterministic(), fa.isDeterministic());
        Assert.assertEquals(copy.getTransitions().size(), fa.getTransitions().size());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMatrixArgumentAndOutput() {
        Model m = new Model();
        IntVar[][] y = m.intVarMatrix("m", 2, 3, 0, 2);
        m.lexChainLess(y).post();
        String direct = solveAll(m, Arrays.stream(y).flatMap(Arrays::stream).toArray(IntVar[]::new));
        SpecBuilder b = new SpecBuilder(Header.of("lex"));
        int[] ids = b.callArray(6, "intVarMatrix", "m", 2, 3, 0, 2);
        int[][] rows = {Arrays.copyOfRange(ids, 0, 3), Arrays.copyOfRange(ids, 3, 6)};
        b.post(b.call("lexChainLess", b.refs(IntVar.class, rows)));
        Resolver inst = b.build().instantiate();
        Assert.assertEquals(solveAll(inst.model(), inst.intVars(ids)), direct);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testTasksAndCumulative() {
        int[] durations = {2, 3, 1};
        int[] heights = {1, 2, 2};
        Model m = new Model();
        IntVar[] s = new IntVar[3];
        Task[] tasks = new Task[3];
        IntVar[] h = new IntVar[3];
        for (int i = 0; i < 3; i++) {
            s[i] = m.intVar("s" + i, 0, 4);
            IntVar e = m.intVar("e" + i, 0, 6);
            tasks[i] = new Task(s[i], m.intVar(durations[i]), e);
            h[i] = m.intVar(heights[i]);
        }
        m.cumulative(tasks, h, m.intVar(3)).post();
        String direct = solveAll(m, s);

        SpecBuilder b = new SpecBuilder(Header.of("cumulative"));
        int[] is = new int[3];
        int[] it = new int[3];
        int[] ih = new int[3];
        for (int i = 0; i < 3; i++) {
            is[i] = b.call("intVar", "s" + i, 0, 4);
            int ie = b.call("intVar", "e" + i, 0, 6);
            it[i] = b.newTask(is[i], b.call("intVar", durations[i]), ie);
            ih[i] = b.call("intVar", heights[i]);
        }
        b.post(b.call("cumulative", b.refs(Task.class, it), b.refs(IntVar.class, ih), b.ref(b.call("intVar", 3))));
        Resolver inst = b.build().instantiate();
        Assert.assertEquals(inst.model().getNbVars(), m.getNbVars());
        Assert.assertEquals(solveAll(inst.model(), inst.intVars(is)), direct);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testClauses() {
        Model m = new Model();
        BoolVar[] x = m.boolVarArray("b", 3);
        m.addClauses(LogOp.or(LogOp.and(x[0], x[1]), LogOp.nor(x[2])));
        String direct = solveAll(m, x);
        SpecBuilder b = new SpecBuilder(Header.of("clauses"));
        int[] ix = b.callArray(3, "boolVarArray", "b", 3);
        b.effect("addClauses", Arg.or(Arg.and(b.ref(ix[0]), b.ref(ix[1])), new Arg.Logic(false, true, new Arg[]{b.ref(ix[2])})));
        Resolver inst = b.build().instantiate();
        Assert.assertEquals(solveAll(inst.model(), inst.intVars(ix)), direct);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCustom() {
        SpecBuilder b = new SpecBuilder(Header.of("custom"));
        int x = b.call("intVar", "x", 0, 3);
        b.post(b.custom("gt1", r -> r.model().arithm(r.intVar(x), ">", 1)));
        Resolver inst = b.build().instantiate();
        Assert.assertEquals(solveAll(inst.model(), inst.intVar(x)).split(",")[0], "2 sols");
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMostSpecificOverload() {
        SpecBuilder b = new SpecBuilder(Header.of("overload"));
        int[] x = b.callArray(3, "boolVarArray", "b", 3);
        // sum(IntVar[], ...) and sum(BoolVar[], ...) both apply: the latter is chosen
        b.post(b.call("sum", b.refs(BoolVar.class, x), "=", 2));
        ModelSpec spec = b.build();
        Step.Call call = (Step.Call) spec.steps().get(1);
        Assert.assertEquals(call.method().parameterTypes()[0], BoolVar[].class);
        Resolver inst = spec.instantiate();
        Assert.assertEquals(solveAll(inst.model(), inst.intVars(x)).split(",")[0], "3 sols");
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCachedConstantsAreAliased() {
        SpecBuilder b = new SpecBuilder(Header.of("alias"));
        int c1 = b.call("intVar", 5);
        int c2 = b.call("intVar", 5);
        Resolver inst = b.build().instantiate();
        Assert.assertSame(inst.get(c1), inst.get(c2));
        Assert.assertEquals(inst.idOf(inst.get(c2)), c1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDivergenceIsDetected() {
        FactoryMethod intVar = FactoryMethod.of("intVar", String.class, int.class, int.class);
        Arg[] args = {new Arg.Const("x"), new Arg.Const(0), new Arg.Const(1)};
        ModelSpec spec = new ModelSpec(Header.of("div"), List.of(
                new Step.Call(new int[]{0}, intVar, args),
                new Step.Call(new int[]{0}, intVar, args)));
        SpecException e = Assert.expectThrows(SpecException.class, spec::instantiate);
        Assert.assertTrue(e.getMessage().contains("Divergence"), e.getMessage());
        Assert.assertTrue(e.getMessage().startsWith("Step #1"), e.getMessage());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testIllFormedSpecs() {
        // undefined identifier
        Assert.assertThrows(SpecException.class, () -> new ModelSpec(Header.of("e"), List.of(new Step.Post(new int[]{3}))));
        // wrong number of arguments
        FactoryMethod intVar = FactoryMethod.of("intVar", String.class, int.class, int.class);
        Assert.assertThrows(SpecException.class, () -> new Step.Call(new int[]{0}, intVar, new Arg[]{new Arg.Const("x")}));
        // not a factory method
        Assert.assertThrows(SpecException.class, () -> FactoryMethod.of("getSolver"));
        // unsupported (mutable) constant; an object without field is immutable, hence supported
        Assert.assertThrows(SpecException.class, () -> new Arg.Const(new StringBuilder()));
        new Arg.Const(new Object());
        // a variable where a constraint is expected
        SpecBuilder b = new SpecBuilder(Header.of("e"));
        int x = b.call("intVar", "x", 0, 1);
        ModelSpec spec = b.post(x).build();
        Assert.assertThrows(SpecException.class, spec::instantiate);
        // a constraint where a variable is expected
        FactoryMethod arithm = FactoryMethod.of("arithm", org.chocosolver.solver.variables.IntVar.class, String.class, int.class);
        SpecBuilder b2 = new SpecBuilder(Header.of("e"));
        int y = b2.call("intVar", "y", 0, 1);
        int c = b2.call("arithm", b2.ref(y), ">", 0);
        b2.call(arithm, b2.ref(c), ">", 0);
        Assert.assertThrows(SpecException.class, () -> b2.build().instantiate());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNoSharedMutableObject() {
        Queens qs = queensSpec(5);
        Resolver i1 = qs.spec().instantiate();
        Resolver i2 = qs.spec().instantiate();
        Set<Object> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        seen.addAll(Arrays.asList(i1.model().getVars()));
        seen.addAll(Arrays.asList(i1.model().getCstrs()));
        for (Object o : i2.model().getVars()) Assert.assertFalse(seen.contains(o));
        for (Object o : i2.model().getCstrs()) Assert.assertFalse(seen.contains(o));
        Assert.assertEquals(new HashSet<>(Arrays.asList(i1.model().getVars())).size(), 5);
    }
}
