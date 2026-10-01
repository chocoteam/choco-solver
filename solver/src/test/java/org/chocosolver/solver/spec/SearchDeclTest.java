/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.BlackBoxConfigurator;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.SearchParams;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Tests of the search, objective and solutions of a {@link ModelSpec}.
 *
 * @author Charles Prud'homme
 */
public class SearchDeclTest {

    private static final SearchParams.VarSelConf DOMWDEG =
            new SearchParams.VarSelConf(SearchParams.VariableSelection.DOMWDEG, Integer.MAX_VALUE);
    private static final SearchParams.ValSelConf MIN =
            new SearchParams.ValSelConf(SearchParams.ValueSelection.MIN, false, 1, false);
    private static final SearchParams.ResConf LUBY =
            new SearchParams.ResConf(SearchParams.Restart.LUBY, 50, 0, true);

    /**
     * A small optimization problem: returns its decision variables.
     */
    private static IntVar[] build(Model m) {
        int n = 7;
        IntVar[] x = m.intVarArray("x", n, 0, n - 1);
        m.allDifferent(x).post();
        for (int i = 0; i < n - 1; i++) {
            m.arithm(x[i], "!=", x[i + 1], "+", 1).post();
        }
        IntVar obj = m.intVar("obj", 0, 100);
        m.scalar(x, new int[]{1, 2, 3, 4, 5, 6, 7}, "=", obj).post();
        m.setObjective(Model.MINIMIZE, obj);
        return x;
    }

    /**
     * @return solutions, nodes, fails and best objective
     */
    private static String solve(Model m) {
        Solver s = m.getSolver();
        s.limitNode(20_000);
        while (s.solve()) ;
        return s.getSolutionCount() + " sols, " + s.getNodeCount() + " nodes, " + s.getFailCount() + " fails, best="
                + s.getBestSolutionValue();
    }

    /**
     * Configure the direct model with <i>direct</i>, and the instances with <i>decl</i>: they must search alike.
     */
    private static void assertSameSearch(Consumer<IntVar[]> direct, SearchDecl decl) throws Exception {
        Model d = new Model("m");
        IntVar[] x = build(d);
        direct.accept(x);
        String expected = solve(d);

        RecordingModel r = new RecordingModel("m");
        build(r);
        ModelSpec spec = r.snapshot().withSearch(decl);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (int k = 0; k < 4; k++) {
                futures.add(pool.submit(() -> solve(spec.instantiate().model())));
            }
            for (Future<String> f : futures) {
                Assert.assertEquals(f.get(), expected);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testModelBasedSearch() throws Exception {
        assertSameSearch(x -> Search.defaultSearch(x[0].getModel()), SearchDecl.of(Search::defaultSearch));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBlackBoxWithNogoods() throws Exception {
        // nogoods on restarts create the SAT constraint on each instance
        assertSameSearch(x -> BlackBoxConfigurator.forCOP().make(x[0].getModel()),
                SearchDecl.of(m -> BlackBoxConfigurator.forCOP().make(m)));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCatalogSearch() throws Exception {
        RecordingModel r = new RecordingModel("m");
        IntVar[] x = build(r);
        int[] ids = r.getRecorder().idsOf(x);
        assertSameSearch(v -> {
                    Solver s = v[0].getModel().getSolver();
                    s.setSearch(DOMWDEG.make().apply(v, MIN.make().apply(v[0].getModel())));
                    s.addRestarter(LUBY.make().apply(s));
                    s.setSearch(Search.lastConflict(s.getSearch(), 2));
                },
                SearchDecl.intVarSearch(DOMWDEG, MIN, ids)
                        .andThen(SearchDecl.restarts(LUBY))
                        .andThen(SearchDecl.lastConflict(2)));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSolutionsAreSharedByIdentifiers() throws Exception {
        RecordingModel master = new RecordingModel("m");
        IntVar[] mx = build(master);
        int[] ids = master.getRecorder().idsOf(mx);
        ModelSpec spec = master.snapshot().withSearch(SearchDecl.intVarSearch(DOMWDEG, MIN, ids));

        // a worker finds the optimum
        Resolver worker = spec.instantiate();
        SpecSolution best = null;
        while (worker.model().getSolver().solve()) {
            best = SpecSolution.record(worker);
        }
        Assert.assertNotNull(best);
        int objId = master.getRecorder().idOf(master.getObjective());
        int optimum = best.valueOf(objId);
        Assert.assertEquals(optimum, worker.model().getSolver().getBestSolutionValue().intValue());

        // read by the master
        Solution inMaster = best.toSolution(master.getRecorder());
        for (int i = 0; i < mx.length; i++) {
            Assert.assertEquals(inMaster.getIntVal(mx[i]), best.valueOf(ids[i]));
        }
        Assert.assertEquals(inMaster.getIntVal((IntVar) master.getObjective()), optimum);

        // and it is a solution of another instance
        Resolver other = spec.instantiate();
        for (int id : ids) {
            other.model().arithm(other.intVar(id), "=", best.valueOf(id)).post();
        }
        Assert.assertTrue(other.model().getSolver().solve());
        Assert.assertEquals(SpecSolution.record(other).valueOf(objId), optimum);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCustomConstraintCapturingAnotherModelIsDetected() {
        Model master = new Model();
        IntVar foreign = master.intVar("x", 0, 3);
        SpecBuilder b = new SpecBuilder(Header.of("guard"));
        b.call("intVar", "y", 0, 3);
        b.post(b.custom("bad", r -> r.model().arithm(foreign, ">", 1)));
        ModelSpec spec = b.build();
        SpecException e = Assert.expectThrows(SpecException.class, spec::instantiate);
        Assert.assertTrue(e.getCause() instanceof AssertionError, String.valueOf(e.getCause()));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSearchCapturingAnotherModelIsDetected() {
        Model master = new Model();
        IntVar foreign = master.intVar("x", 0, 3);
        SpecBuilder b = new SpecBuilder(Header.of("guard"));
        b.call("intVar", "y", 0, 3);
        ModelSpec spec = b.build().withSearch(r -> r.model().getSolver().setSearch(Search.inputOrderLBSearch(foreign)));
        Assert.expectThrows(AssertionError.class, spec::instantiate);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMasterIsAResolver() {
        RecordingModel master = new RecordingModel("m");
        IntVar[] x = build(master);
        Recorder r = master.getRecorder();
        Assert.assertSame(r.model(), master);
        for (IntVar v : x) {
            Assert.assertSame(r.get(r.idOf(v)), v);
        }
        Assert.assertThrows(SpecException.class, () -> r.idsOf(new Model().intVar("z", 0, 1)));
    }
}
