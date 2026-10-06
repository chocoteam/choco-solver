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
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.SearchParams;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Tests of {@link Variant}.
 *
 * @author Charles Prud'homme
 */
public class VariantTest {

    private static final int N = 7;

    /**
     * Queens with three allDifferent, and a table on the first two queens.
     */
    private static IntVar[] queens(Model m) {
        IntVar[] q = m.intVarArray("q", N, 1, N);
        IntVar[] d1 = new IntVar[N];
        IntVar[] d2 = new IntVar[N];
        for (int i = 0; i < N; i++) {
            d1[i] = m.offset(q[i], i);
            d2[i] = m.offset(q[i], -i);
        }
        m.allDifferent(q).post();
        m.allDifferent(d1, "AC").post();
        m.allDifferent(d2).post();
        Tuples t = new Tuples(false);
        t.add(1, 3);
        t.add(3, 1);
        m.table(new IntVar[]{q[0], q[1], q[2]}, tuples3()).post();
        m.table(q[0], q[1], t).post();
        return q;
    }

    private static Tuples tuples3() {
        Tuples t = new Tuples(true);
        for (int a = 1; a <= N; a++) {
            for (int b = 1; b <= N; b++) {
                for (int c = 1; c <= N; c++) {
                    if (a != 2 || b != 4) {
                        t.add(a, b, c);
                    }
                }
            }
        }
        return t;
    }

    private static ModelSpec spec() {
        return ModelSpec.record("queens", org.chocosolver.solver.SettingsBuilder.init().build(), VariantTest::queens);
    }

    private static List<String> propagators(Model m, String constraintName) {
        return Arrays.stream(m.getCstrs()).filter(c -> c.getName().equals(constraintName))
                .flatMap(c -> Arrays.stream(c.getPropagators())).map(p -> p.getClass().getSimpleName())
                .collect(Collectors.toList());
    }

    private static long solutions(Model m) {
        Solver s = m.getSolver();
        s.setSearch(Search.inputOrderLBSearch(m.retrieveIntVars(true)));
        return s.findAllSolutions().size();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testIdentity() {
        ModelSpec spec = spec();
        Assert.assertSame(spec.with(Variant.IDENTITY), spec);
        Assert.assertEquals(solutions(spec.instantiate(Variant.IDENTITY).model()), solutions(spec.instantiate().model()));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAllDifferentConsistency() {
        ModelSpec spec = spec();
        long expected = solutions(spec.instantiate().model());
        Model bc = spec.instantiate(Variant.allDifferent("BC")).model();
        List<String> props = propagators(bc, "ALLDIFFERENT");
        Assert.assertEquals(props.stream().filter(p -> p.contains("BC")).count(), 3, props.toString());
        Assert.assertTrue(props.stream().noneMatch(p -> p.contains("AC")), props.toString());
        Assert.assertEquals(solutions(bc), expected);
        Model neqs = spec.instantiate(Variant.allDifferent("NEQS")).model();
        Assert.assertTrue(propagators(neqs, "ALLDIFFERENT").isEmpty() || !propagators(neqs, "ALLDIFFERENT").contains("PropAllDiffBC"));
        Assert.assertEquals(solutions(neqs), expected);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testTableAlgorithms() {
        ModelSpec spec = spec();
        long expected = solutions(spec.instantiate().model());
        Model str2 = spec.instantiate(Variant.tableAlgorithm("STR2+")).model();
        Assert.assertTrue(propagators(str2, "TABLE").contains("PropTableStr2"), propagators(str2, "TABLE").toString());
        Assert.assertEquals(solutions(str2), expected);
        Model ac3 = spec.instantiate(Variant.binaryTableAlgorithm("AC3rm")).model();
        Assert.assertTrue(propagators(ac3, "TABLE").stream().anyMatch(p -> p.contains("AC3rm")), propagators(ac3, "TABLE").toString());
        Assert.assertEquals(solutions(ac3), expected);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSeedOverridesRecordedSeeds() {
        Model recorded = Model.record("seeded");
        recorded.setSeed(42);
        recorded.allDifferent(recorded.intVarArray("x", 3, 0, 2)).post();
        ModelSpec spec = recorded.getRecorder().snapshot();
        Assert.assertEquals(spec.instantiate().model().getSeed(), 42L);
        Assert.assertEquals(spec.instantiate(Variant.seed(7)).model().getSeed(), 7L);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSeedAndSearch() {
        ModelSpec spec = spec();
        long expected = solutions(spec.instantiate().model());
        Model m = spec.instantiate(Variant.seed(123)).model();
        Assert.assertEquals(m.getSeed(), 123L);
        Assert.assertEquals(solutions(m), expected);
        SearchParams.VarSelConf dom = new SearchParams.VarSelConf(SearchParams.VariableSelection.DOM, Integer.MAX_VALUE);
        SearchParams.ValSelConf max = new SearchParams.ValSelConf(SearchParams.ValueSelection.MAX, false, 1, false);
        int[] q = new int[N];
        for (int i = 0; i < N; i++) {
            q[i] = i; // intVarArray is the first call: its outputs are 0..N-1
        }
        Model searched = spec.instantiate(Variant.search(SearchDecl.intVarSearch(dom, max, q))).model();
        Assert.assertTrue(searched.getSolver().solve());
        Assert.assertEquals(searched.getSolver().getSearch().getClass().getSimpleName(), "IntStrategy");
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testExtendAndUnpost() {
        RecordingModel master = new RecordingModel("queens");
        IntVar[] q = queens(master);
        int[] ids = master.getRecorder().idsOf(q);
        ModelSpec spec = master.snapshot();
        long expected = solutions(spec.instantiate().model());
        int nbCstrs = spec.instantiate().model().getNbCstrs();

        // redundant constraint: the sum of the queens
        Variant redundant = Variant.extend(b -> b.post(b.call("sum", b.refs(IntVar.class, ids), "=", N * (N + 1) / 2)));
        Model r = spec.instantiate(redundant).model();
        Assert.assertEquals(r.getNbCstrs(), nbCstrs + 1);
        Assert.assertEquals(solutions(r), expected);

        // symmetry breaking: q[0] < q[N-1]
        Variant symmetry = Variant.extend(b -> b.post(b.call("arithm", b.ref(ids[0]), "<", b.ref(ids[N - 1]))));
        Assert.assertTrue(solutions(spec.instantiate(symmetry).model()) < expected);

        // removal of the tables
        Model relaxed = spec.instantiate(Variant.unpostIf(c -> c.method().name().equals("table"))).model();
        Assert.assertEquals(relaxed.getNbCstrs(), nbCstrs - 2);
        Assert.assertTrue(solutions(relaxed) > expected);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testComposedVariantsInParallel() throws Exception {
        ModelSpec spec = spec();
        long expected = solutions(spec.instantiate().model());
        List<Variant> variants = List.of(
                Variant.IDENTITY,
                Variant.allDifferent("BC"),
                Variant.allDifferent("AC").andThen(Variant.tableAlgorithm("STR2+")).andThen(Variant.seed(7)),
                Variant.binaryTableAlgorithm("FC").andThen(Variant.search(SearchDecl.of(Search::defaultSearch))));
        List<ModelSpec> specs = variants.stream().map(spec::with).collect(Collectors.toList());
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<Long>> futures = new ArrayList<>();
            for (ModelSpec s : specs) {
                futures.add(pool.submit(() -> solutions(s.instantiate().model())));
            }
            for (Future<Long> f : futures) {
                Assert.assertEquals(f.get().longValue(), expected);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testVariantKeepsIdentifiers() {
        ModelSpec spec = spec();
        ModelSpec v = spec.with(Variant.allDifferent("BC"));
        Assert.assertEquals(v.steps().size(), spec.steps().size());
        Resolver i = v.instantiate();
        for (int id = 0; id < N; id++) {
            Assert.assertEquals(i.intVar(id).getName(), "q[" + id + "]");
        }
        Constraint[] cs = i.model().getCstrs();
        Assert.assertEquals(cs.length, spec.instantiate().model().getNbCstrs());
    }
}
