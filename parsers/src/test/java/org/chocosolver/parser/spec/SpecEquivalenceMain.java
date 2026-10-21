/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.spec;

import org.testng.SkipException;

import java.io.PrintStream;
import java.nio.file.Paths;

/**
 * Runs the equivalence check of {@link SpecEquivalenceTest} on a single instance, to be launched in its own JVM
 * (see {@code etc/spec-bench.sh}): a large instance can neither exhaust the memory of the others nor survive its
 * time limit.
 * <p>
 * Prints one line: {@code RESULT<TAB>status<TAB>instance<TAB>seconds<TAB>detail}, with status among OK, FAIL,
 * SKIP (not journalizable), ERROR, OOM and TIMEOUT.
 * <p>
 * Usage: {@code SpecEquivalenceMain <instance> [time limit in seconds]}; the system properties of
 * {@link SpecEquivalenceTest} apply.
 *
 * @author Charles Prud'homme
 */
public class SpecEquivalenceMain {

    public static void main(String[] args) {
        String file = args[0];
        long limit = args.length > 1 ? Long.parseLong(args[1]) : 600;
        String name = Paths.get(file).getFileName().toString();
        System.setProperty("spec.progress", "false");
        PrintStream out = System.out;
        // the parsers and the solver may print: keep stdout for the result only
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
        SpecEquivalenceTest test = new SpecEquivalenceTest();
        int status = 0;
        try {
            test.testEquivalence(name, file);
            result(out, "OK", name, start, test.usageSummary());
        } catch (SkipException e) {
            result(out, "SKIP", name, start, e.getMessage());
        } catch (AssertionError e) {
            result(out, "FAIL", name, start, e.getMessage());
            status = 1;
        } catch (OutOfMemoryError e) {
            result(out, "OOM", name, start, "");
            status = 2;
        } catch (Throwable e) {
            result(out, "ERROR", name, start, e.toString());
            status = 1;
        }
        watchdog.interrupt();
        out.flush();
        Runtime.getRuntime().halt(status);
    }

    private static void result(PrintStream out, String status, String name, long start, String detail) {
        String d = detail == null ? "" : detail.replace('\t', ' ').replace('\n', ' ');
        if (d.length() > 500) {
            d = d.substring(0, 500) + "...";
        }
        out.printf("RESULT\t%s\t%s\t%.1f\t%s%n", status, name, (System.currentTimeMillis() - start) / 1000.0, d);
        out.flush();
    }
}
