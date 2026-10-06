/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.spec;

import org.chocosolver.parser.RegParser;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ParallelPortfolio;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.spec.ModelSpec;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Compares, on one instance, the ways of building the <i>n</i> models of a portfolio:
 * <ul>
 *     <li>(a) as the parsers currently do: <i>n</i> parsings, one after the other;</li>
 *     <li>(a') <i>n</i> parsings run concurrently;</li>
 *     <li>(b) with a {@link ModelSpec}: one recorded parsing (the master, which also solves), then <i>n</i>-1 models
 *     instantiated concurrently.</li>
 * </ul>
 * For each: the building time and the heap used by the <i>n</i> models. Optionally, (a) and (b) are solved with a
 * {@link ParallelPortfolio} under the same time limit.
 * <p>
 * To be launched in its own JVM (see {@code etc/spec-bench.sh}, with {@code SPEC_MAIN} set to this class). Prints one
 * line: {@code RESULT<TAB>status<TAB>instance<TAB>seconds<TAB>metrics}, metrics being
 * {@code key=value} pairs separated by {@code ;}.
 * <p>
 * Usage: {@code SpecPortfolioBench <instance> <time limit (s)>}; system properties: {@code spec.workers} (default 4),
 * {@code spec.solve} (time limit of each resolution in seconds, default 0: no resolution).
 *
 * @author Charles Prud'homme
 */
public class SpecPortfolioBench {

    private static final int N = Integer.getInteger("spec.workers", 4);
    private static final int SOLVE = Integer.getInteger("spec.solve", 0);

    /**
     * @return the used heap, in MB, after garbage collection
     */
    private static long heap() {
        for (int i = 0; i < 3; i++) {
            System.gc();
        }
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 20;
    }

    private static long ms(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    /**
     * Solve with a portfolio under the time limit.
     *
     * @return "time;best;complete"
     */
    private static String solve(ParallelPortfolio portfolio) {
        for (Model m : portfolio.getModels()) {
            m.getSolver().limitTime(SOLVE + "s");
        }
        long t = System.nanoTime();
        Number best = null;
        boolean found = false;
        while (portfolio.solve()) {
            found = true;
            Model m = portfolio.getBestModel();
            if (m.getResolutionPolicy() == ResolutionPolicy.SATISFACTION) {
                break;
            }
            best = m.getSolver().getBestSolutionValue();
        }
        // a model which ended by itself (not stopped by a limit or by another model) completed the search
        boolean complete = portfolio.getModels().stream()
                .anyMatch(m -> m.getSolver().getSearchState() == SearchState.TERMINATED);
        return String.format(Locale.US, "%.2f;%s;%s", ms(t) / 1000., found ? (best == null ? "sat" : best) : "none",
                complete);
    }

    public static void main(String[] args) throws Exception {
        String file = args[0];
        long limit = args.length > 1 ? Long.parseLong(args[1]) : 1800;
        String name = Paths.get(file).getFileName().toString();
        PrintStream out = System.out;
        System.setOut(new PrintStream(java.io.OutputStream.nullOutputStream()));
        long start = System.currentTimeMillis();
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(limit * 1000);
                result(out, "TIMEOUT", name, start, "exceeds " + limit + " s");
                Runtime.getRuntime().halt(3);
            } catch (InterruptedException ignored) {
                // done in time
            }
        });
        watchdog.setDaemon(true);
        watchdog.start();
        StringBuilder metrics = new StringBuilder("workers=" + N);
        int status = 0;
        try {
            // warm-up of both paths (class loading, JIT), not measured
            Model warm = SpecParsers.parse(file, 1, true).getModel();
            ParallelPortfolio.of(warm.getRecorder().snapshot(), 1);
            SpecParsers.parse(file, 1, false);
            warm = null;

            long base = heap();
            // (a) n parsings, one after the other, as the parsers do
            long t = System.nanoTime();
            RegParser a = SpecParsers.parse(file, N, false);
            long buildA = ms(t);
            long heapA = heap() - base;
            List<Model> modelsA = new ArrayList<>(a.getModels());
            metrics.append(";a_ms=").append(buildA).append(";a_mb=").append(heapA)
                    .append(";vars=").append(modelsA.get(0).getNbVars())
                    .append(";cstrs=").append(modelsA.get(0).getNbCstrs());
            if (SOLVE > 0) {
                ParallelPortfolio pa = new ParallelPortfolio();
                modelsA.forEach(pa::addModel);
                metrics.append(";a_solve=").append(solve(pa));
            }
            a = null;
            modelsA = null;

            // (a') n parsings run concurrently
            base = heap();
            ExecutorService pool = Executors.newFixedThreadPool(N);
            t = System.nanoTime();
            List<Future<RegParser>> futures = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                futures.add(pool.submit(() -> SpecParsers.parse(file, 1, false)));
            }
            List<RegParser> parsers = new ArrayList<>();
            for (Future<RegParser> f : futures) {
                parsers.add(f.get());
            }
            long buildA2 = ms(t);
            pool.shutdownNow();
            long heapA2 = heap() - base;
            metrics.append(";a2_ms=").append(buildA2).append(";a2_mb=").append(heapA2);
            parsers = null;

            // (b) one recorded parsing, then n-1 instances built concurrently
            base = heap();
            t = System.nanoTime();
            RegParser b = SpecParsers.parse(file, 1, true);
            long parseB = ms(t);
            Model master = b.getModel();
            long t2 = System.nanoTime();
            ModelSpec spec = master.getRecorder().snapshot();
            long snapB = ms(t2);
            t2 = System.nanoTime();
            ParallelPortfolio pb = ParallelPortfolio.of(spec, N - 1);
            long instB = ms(t2);
            long buildB = ms(t);
            pb.addModel(master);
            long heapB = heap() - base;
            metrics.append(";b_ms=").append(buildB).append(";b_parse_ms=").append(parseB)
                    .append(";b_snapshot_ms=").append(snapB).append(";b_instantiate_ms=").append(instB)
                    .append(";b_mb=").append(heapB).append(";steps=").append(spec.steps().size());
            if (SOLVE > 0) {
                metrics.append(";b_solve=").append(solve(pb));
            }
            result(out, "OK", name, start, metrics.toString());
        } catch (OutOfMemoryError e) {
            result(out, "OOM", name, start, metrics.toString());
            status = 2;
        } catch (Throwable e) {
            result(out, "ERROR", name, start, metrics + ";error=" + e);
            status = 1;
        }
        watchdog.interrupt();
        out.flush();
        Runtime.getRuntime().halt(status);
    }

    private static void result(PrintStream out, String status, String name, long start, String detail) {
        String d = detail.replace('\t', ' ').replace('\n', ' ');
        out.printf(Locale.US, "RESULT\t%s\t%s\t%.1f\t%s%n", status, name, (System.currentTimeMillis() - start) / 1000.0, d);
        out.flush();
    }
}
