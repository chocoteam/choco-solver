/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_Y;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Tests of {@link RecordingModel} and {@link Recorder}: a model built directly and the same model recorded,
 * snapshot and instantiated must be identical.
 *
 * @author Charles Prud'homme
 */
public class RecorderTest {

    /**
     * Build the model directly and through a snapshot, check they are identical and return the spec.
     */
    private static ModelSpec roundTrip(Consumer<Model> builder) {
        Model direct = new Model("m");
        builder.accept(direct);
        RecordingModel recorded = new RecordingModel("m");
        builder.accept(recorded);
        ModelSpec spec = recorded.snapshot();
        Model replayed = spec.instantiate().model();
        String expected = SpecTestUtils.structure(direct);
        Assert.assertEquals(SpecTestUtils.structure(recorded), expected, "recording must not change the model");
        Assert.assertEquals(SpecTestUtils.structure(replayed), expected, "replay differs");
        Assert.assertEquals(replayed.getSeed(), direct.getSeed());
        String stats = SpecTestUtils.solve(direct, 100_000);
        Assert.assertEquals(SpecTestUtils.solve(recorded, 100_000), stats);
        Assert.assertEquals(SpecTestUtils.solve(replayed, 100_000), stats);
        return spec;
    }

    //***********************************************************************************
    // SCENARIOS
    //***********************************************************************************

    private static void queens(Model m) {
        int n = 6;
        IntVar[] q = m.intVarArray("q", n, 1, n);
        IntVar[] d1 = new IntVar[n];
        IntVar[] d2 = new IntVar[n];
        for (int i = 0; i < n; i++) {
            d1[i] = m.offset(q[i], i);
            d2[i] = m.offset(q[i], -i);
        }
        m.allDifferent(q, "AC").post();
        m.allDifferent(d1, "BC").post();
        m.allDifferent(d2).post();
    }

    private static void sideEffects(Model m) {
        IntVar[] x = m.intVarArray("x", 5, 0, 4);
        IntVar v = m.intVar("v", 0, 10);
        IntVar i = m.intVar("i", 0, 6);
        m.element(v, new int[]{3, 1, 4, 1, 5}, i, 2).post();
        m.among(m.intVar("n", 0, 5), x, new int[]{1, 3}).post();
        m.globalCardinality(x, new int[]{0, 1}, m.intVarArray("o", 2, 0, 2), false).post();
        m.nValues(x, m.intVar("nv", 2, 3)).post();
        m.sum(x, "<=", m.intVar("s", 0, 12)).post();
        m.increasing(Arrays.copyOf(x, 3), 0).post();
        m.count(2, x, m.intVar("c", 1, 2)).post();
        m.mod(x[3], 3, x[4]).post();
        m.scalar(x, new int[]{1, 2, 3, 4, 5}, ">=", 10).post();
    }

    private static void reification(Model m) {
        IntVar x = m.intVar("x", 0, 5);
        IntVar y = m.intVar("y", 0, 5);
        BoolVar b1 = m.arithm(x, ">", y).reify();
        BoolVar b2 = m.boolVar("b2");
        m.arithm(x, "=", 2).reifyWith(b2);
        BoolVar b3 = m.boolVar("b3");
        m.arithm(y, "=", 0).implies(b3);
        BoolVar b4 = m.boolVar("b4");
        m.arithm(y, "=", 5).impliedBy(b4);
        m.or(m.arithm(x, "=", y), m.arithm(x, "=", 0)).post();
        m.and(b1.not(), b3).post();
        m.ifThen(m.arithm(x, "=", 1), m.arithm(y, "=", 1));
        m.not(m.arithm(x, "=", 4)).post();
        Constraint c = m.arithm(x, "<", 5);
        BoolVar again1 = c.reify();
        BoolVar again2 = c.reify();
        Assert.assertSame(again1, again2);
        m.sum(new BoolVar[]{b1, b2, b4, again1}, ">=", 1).post();
    }

    private static void expressions(Model m) {
        IntVar x = m.intVar("x", 0, 5);
        IntVar y = m.intVar("y", 0, 5);
        IntVar z = m.intVar("z", 0, 12);
        x.add(y).mul(2).eq(z).post();
        x.gt(y).or(z.lt(3)).post();
        x.sub(y).abs().ge(2).extension().post();
        IntVar xy = x.add(y).intVar();
        m.arithm(xy, "<=", 8).post();
        x.mul(y).ne(4).decompose().post();
    }

    private static void views(Model m) {
        IntVar x = m.intVar("x", -3, 3);
        BoolVar b = m.boolVar("b");
        m.arithm(m.intMinusView(x), "<=", 2).post();
        m.arithm(m.intScaleView(x, 2), "!=", 2).post();
        m.arithm(m.intAbsView(x), ">", 0).post();
        BoolVar eq = m.isEq(x, 1);
        m.arithm(eq, "!=", b).post();
        m.arithm(b.not(), "=", m.isLeq(x, 0)).post();
        m.arithm(m.intVar(5), ">", x).post();
        m.arithm(m.intVar(5), "!=", m.intVar("w", 4, 6)).post();
    }

    private static void scheduling(Model m) {
        IntVar[] s = m.intVarArray("s", 3, 0, 5);
        Task t0 = new Task(s[0], 2);
        Task t1 = m.taskVar(s[1], 3);
        Task t2 = new Task(s[2], m.intVar("d2", 1, 2), m.intVar("e2", 0, 8));
        OptionalTask t3 = new OptionalTask(m.intVar("s3", 0, 5), 2);
        m.cumulative(new Task[]{t0, t1, t2, t3}, m.intVarArray("h", 4, 1, 1), m.intVar(2)).post();
        m.cumulative(s, new int[]{1, 1, 2}, new int[]{1, 1, 1}, 2).post();
    }

    private static void sat(Model m) {
        BoolVar[] b = m.boolVarArray("b", 4);
        m.addClauses(LogOp.or(LogOp.and(b[0], b[1]), LogOp.nor(b[2])));
        m.addClauseTrue(b[3]);
        m.addClausesBoolOrArrayEqVar(new BoolVar[]{b[0], b[2]}, b[1]);
        m.addClauses(new BoolVar[]{b[0]}, new BoolVar[]{b[2]});
    }

    private static void data(Model m) {
        IntVar[] x = m.intVarArray("x", 3, 0, 3);
        Tuples t = new Tuples(true);
        t.add(0, 1, 2);
        t.add(1, 2, 3);
        t.add(3, 3, 3);
        m.table(x, t, "CT+").post();
        m.table(new IntVar[]{x[0], x[1]}, new Tuples(new int[][]{{0, 1}, {1, 2}, {3, 3}}, false, java.util.OptionalInt.empty())).post();
        IntVar[] y = m.intVarArray("y", 4, 0, 3);
        m.regular(y, new FiniteAutomaton("(1|2)3*1")).post();
        FiniteAutomaton fa = new FiniteAutomaton("(0|1)*");
        int[][] costs = new int[4][4];
        for (int[] c : costs) Arrays.fill(c, 1);
        costs[1][1] = 0;
        m.costRegular(m.intVarArray("z", 4, 0, 1), m.intVar("cost", 0, 3),
                CostAutomaton.makeSingleResource(fa, costs, 0, 3)).post();
        m.lexChainLess(m.intVarMatrix("mat", 2, 2, 0, 1)).post();
    }

    private static void objectiveHooksSeed(Model m) {
        m.addHook("CUMULATIVE", "GLB");
        IntVar[] x = m.intVarArray("x", 4, 0, 5);
        m.setSeed(42);
        m.allDifferent(x).post();
        IntVar obj = m.intVar("obj", 0, 20);
        m.sum(x, "=", obj).post();
        m.setObjective(Model.MINIMIZE, obj);
        Constraint tmp = m.arithm(x[0], ">", 3);
        tmp.post();
        m.unpost(tmp);
    }

    private static void custom(Model m) {
        IntVar[] x = m.intVarArray("x", 2, 0, 3);
        BoolVar[] b = m.boolVarArray("b", 2);
        m.custom("geq", x, PropGreaterOrEqualX_Y::new).post();
        // the propagator is built from an array of the recorded type
        m.custom("geqBool", b, PropGreaterOrEqualX_Y::new).post();
        m.custom("geqLambda", new IntVar[]{x[1], b[0]}, vs -> new PropGreaterOrEqualX_Y(vs)).post();
    }

    private static void nameAndHooks(Model m) {
        m.setName("renamed");
        m.addHook("CUMULATIVE", "GLB");
        m.removeAllHooks();
        m.addHook("other", 1);
        m.allDifferent(m.intVarArray("x", 3, 0, 2)).post();
    }

    private static void groups(Model m) {
        IntVar[] x = m.intVarArray("x", 4, 0, 3);
        IntVar[] y = m.intVarArray("y", 3, 0, 3);
        m.addAsGroup("x", x);
        m.addAsGroup("y", y);
        m.allDifferent(x).post();
        m.sum(y, "=", x[0]).post();
    }

    @DataProvider
    public Object[][] scenarios() {
        return new Object[][]{
                {"queens", (Consumer<Model>) RecorderTest::queens},
                {"sideEffects", (Consumer<Model>) RecorderTest::sideEffects},
                {"reification", (Consumer<Model>) RecorderTest::reification},
                {"expressions", (Consumer<Model>) RecorderTest::expressions},
                {"views", (Consumer<Model>) RecorderTest::views},
                {"scheduling", (Consumer<Model>) RecorderTest::scheduling},
                {"sat", (Consumer<Model>) RecorderTest::sat},
                {"data", (Consumer<Model>) RecorderTest::data},
                {"objectiveHooksSeed", (Consumer<Model>) RecorderTest::objectiveHooksSeed},
                {"groups", (Consumer<Model>) RecorderTest::groups},
                {"custom", (Consumer<Model>) RecorderTest::custom},
                {"nameAndHooks", (Consumer<Model>) RecorderTest::nameAndHooks},
        };
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "scenarios")
    public void testRoundTrip(String name, Consumer<Model> builder) {
        roundTrip(builder);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testHooksSeedAndObjectiveAreReplayed() {
        ModelSpec spec = roundTrip(RecorderTest::objectiveHooksSeed);
        Model m = spec.instantiate().model();
        Assert.assertEquals(m.getHook("CUMULATIVE"), "GLB");
        Assert.assertEquals(m.getSeed(), 42L);
        Assert.assertNotNull(m.getObjective());
        Assert.assertEquals(m.getObjective().getName(), "obj");
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNameAndHooksAreReplayed() {
        ModelSpec spec = roundTrip(RecorderTest::nameAndHooks);
        Assert.assertEquals(spec.header().name(), "renamed");
        Model m = spec.instantiate().model();
        Assert.assertEquals(m.getName(), "renamed");
        Assert.assertNull(m.getHook("CUMULATIVE"));
        Assert.assertEquals(m.getHook("other"), 1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCapturingCustomConstraintIsReported() {
        RecordingModel m = new RecordingModel("m");
        IntVar[] x = m.intVarArray("x", 2, 0, 3);
        m.custom("captures", x, vs -> new PropGreaterOrEqualX_Y(x)).post();
        SpecException e = Assert.expectThrows(SpecException.class, m::snapshot);
        Assert.assertTrue(e.getMessage().contains("capturing lambda"), e.getMessage());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testOnlyTopLevelCallsAreRecorded() {
        RecordingModel m = new RecordingModel("m");
        IntVar[] x = m.intVarArray("x", 50, 0, 5);            // 1 call
        m.sum(x, "=", 30, 5).post();                          // 1 call + 1 post, despite the decomposition
        m.globalCardinality(x, new int[]{0, 1}, m.intVarArray("o", 2, 0, 50), true).post(); // 2 calls + 1 post
        List<Step> steps = m.snapshot().steps();
        Assert.assertEquals(steps.size(), 6, steps.toString());
        Assert.assertTrue(m.getNbVars() > 52, "decomposition should create intermediate variables");
        Assert.assertTrue(steps.get(0) instanceof Step.Call);
        Assert.assertEquals(((Step.Call) steps.get(0)).out().length, 50);
        Assert.assertTrue(steps.get(2) instanceof Step.Post);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCustomConstraintIsReported() {
        RecordingModel m = new RecordingModel("m");
        IntVar[] x = m.intVarArray("x", 2, 0, 3);
        new Constraint("MyCustom", new PropGreaterOrEqualX_Y(x)).post();
        Assert.assertEquals(m.getRecorder().unsupported().size(), 1);
        SpecException e = Assert.expectThrows(SpecException.class, m::snapshot);
        Assert.assertTrue(e.getMessage().contains("MyCustom"), e.getMessage());
        Assert.assertTrue(e.getMessage().contains("Model.custom"), e.getMessage());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnsupportedArgumentsAreReported() {
        RecordingModel m = new RecordingModel("m");
        IntVar[] x = m.intVarArray("x", 3, 0, 3);
        // a capturing lambda cannot be journalized (it may capture objects of the recorded model)
        int threshold = x.length - 2;
        m.allDifferentUnderCondition(x, v -> v.getUB() > threshold, true).post();
        // a variable of another model
        Model other = new Model();
        try {
            m.arithm(x[0], "=", other.intVar("o", 0, 1)).post();
        } catch (SolverException | AssertionError ignored) {
            // the model itself rejects it (assertion of Model.post)
        }
        List<Recorder.Unsupported> u = m.getRecorder().unsupported();
        Assert.assertTrue(u.size() >= 2, u.toString());
        Assert.assertTrue(u.get(0).operation().startsWith("allDifferentUnderCondition"), u.toString());
        Assert.assertThrows(SpecException.class, m::snapshot);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSnapshotAfterSolveFails() {
        RecordingModel m = new RecordingModel("m");
        m.arithm(m.intVar("x", 0, 3), ">", 1).post();
        m.getSolver().solve();
        Assert.assertThrows(SpecException.class, m::snapshot);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testTuplesAreFrozenByRecording() {
        RecordingModel m = new RecordingModel("m");
        Tuples t = new Tuples(true);
        t.add(0, 1);
        m.table(m.intVarArray("x", 2, 0, 1), t).post();
        Assert.assertTrue(t.isFrozen());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSnapshotIsReusableConcurrently() throws Exception {
        RecordingModel master = new RecordingModel("m");
        sideEffects(master);
        reification(master);
        ModelSpec spec = master.snapshot();
        String expected = SpecTestUtils.solve(spec.instantiate().model(), 100_000);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (int k = 0; k < 8; k++) {
                futures.add(pool.submit(() -> SpecTestUtils.solve(spec.instantiate().model(), 100_000)));
            }
            for (Future<String> f : futures) {
                Assert.assertEquals(f.get(), expected);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPlainModelHasNoRecorder() {
        Model m = new Model();
        Assert.assertNull(m.getRecorder());
        BoolVar b = m.arithm(m.intVar("x", 0, 3), ">", 1).reify();
        Assert.assertNotNull(b);
    }
}
