/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.spec;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.spec.ModelSpec;

import java.io.PrintStream;
import java.lang.ref.Reference;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

/**
 * Measures, on one instance, the overhead of recording the construction of a model:
 * <ul>
 *     <li>building time: parsing into a plain model ({@code create}) versus into a recorded model
 *     ({@code record});</li>
 *     <li>cost of {@code snapshot} and of {@code instantiate} (replay of the spec), compared to parsing;</li>
 *     <li>memory: the plain model, the recorded model (model and journal), and the spec alone.</li>
 * </ul>
 * Times are medians over {@code spec.reps} repetitions (default 5), after a warm-up.
 * <p>
 * To be launched in its own JVM (see {@code etc/spec-bench.sh}, with {@code SPEC_MAIN} set to this class). Prints one
 * line: {@code RESULT<TAB>status<TAB>instance<TAB>seconds<TAB>metrics}, metrics being {@code key=value} pairs
 * separated by {@code ;}.
 * <p>
 * Usage: {@code SpecOverheadBench <instance> <time limit (s)>}.
 *
 * @author Charles Prud'homme
 */
public class SpecOverheadBench {

    private static final int REPS = Integer.getInteger("spec.reps", 5);
    private static final int WARMUP = Integer.getInteger("spec.warmup", 2);

    /**
     * @return the used heap, in KB, after garbage collection
     */
    private static long heap() {
        for (int i = 0; i < 3; i++) {
            System.gc();
        }
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 10;
    }

    private static double ms(long startNanos) {
        return (System.nanoTime() - startNanos) / 1e6;
    }

    private static double median(double[] values) {
        double[] v = values.clone();
        Arrays.sort(v);
        return v.length % 2 == 1 ? v[v.length / 2] : (v[v.length / 2 - 1] + v[v.length / 2]) / 2;
    }

    public static void main(String[] args) {
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
        StringBuilder metrics = new StringBuilder();
        int status = 0;
        try {
            for (int i = 0; i < WARMUP; i++) {
                SpecParsers.parse(file, 1, false);
                SpecParsers.parse(file, 1, true).getModel().getRecorder().snapshot().instantiate();
            }
            double[] create = new double[REPS];
            double[] record = new double[REPS];
            double[] snapshot = new double[REPS];
            double[] instantiate = new double[REPS];
            int steps = 0;
            int vars = 0;
            int cstrs = 0;
            for (int r = 0; r < REPS; r++) {
                heap();
                long t = System.nanoTime();
                Model plain = SpecParsers.parse(file, 1, false).getModel();
                create[r] = ms(t);
                vars = plain.getNbVars();
                cstrs = plain.getNbCstrs();
                plain = null;

                heap();
                t = System.nanoTime();
                Model recorded = SpecParsers.parse(file, 1, true).getModel();
                record[r] = ms(t);
                t = System.nanoTime();
                ModelSpec spec = recorded.getRecorder().snapshot();
                snapshot[r] = ms(t);
                steps = spec.steps().size();
                recorded = null;

                heap();
                t = System.nanoTime();
                spec.instantiate();
                instantiate[r] = ms(t);
            }
            metrics.append("vars=").append(vars).append(";cstrs=").append(cstrs).append(";steps=").append(steps)
                    .append(String.format(Locale.US, ";create_ms=%.1f;record_ms=%.1f;snapshot_ms=%.2f;instantiate_ms=%.1f",
                            median(create), median(record), median(snapshot), median(instantiate)));

            // memory: each object graph alone
            long base = heap();
            Model plain = SpecParsers.parse(file, 1, false).getModel();
            long createKb = heap() - base;
            Reference.reachabilityFence(plain);
            plain = null;
            base = heap();
            Model recorded = SpecParsers.parse(file, 1, true).getModel();
            long recordKb = heap() - base;
            ModelSpec spec = recorded.getRecorder().snapshot();
            Reference.reachabilityFence(recorded);
            recorded = null;
            long specKb = heap() - base;
            Reference.reachabilityFence(spec);
            metrics.append(";create_kb=").append(createKb).append(";record_kb=").append(recordKb)
                    .append(";spec_kb=").append(specKb);
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
