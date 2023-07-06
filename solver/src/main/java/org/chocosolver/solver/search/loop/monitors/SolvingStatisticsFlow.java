/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.Solver;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A class to return the solving statistics as a JSON object.
 * <br/>
 * Such a class is useful to monitor the solving process from a web page, for example.
 * <br/>
 * On each call to {@link #toJSON(Solver)}, a JSON object is returned, with the following structure:
 * <pre>
 *     {
 *     "variables": "123",
 *     "constraints": "456",
 *     "objective": "789",
 *     "solutions": "321",
 *     "nodes": "123456",
 *     "fails": "123456",
 *     "backtracks": "123456",
 *     "backjumps": "123456",
 *     "restarts": "123456",
 *     "fixpoints": "123456",
 *     "depth": "123456",
 *     "time": "123456.789",
 *     "memory": "123456"
 * </pre>
 * <br/>
 * The time is given in seconds, and the memory usage in MB.
 * When the objective is not set, the value is "--".
 * When an error occurs when estimating the memory usage, the value is "-1".
 * <br/>
 * An example of usage is:
 * <pre>
 *     Solver solver = model.getSolver();
 *     Thread printer = new Thread(() -> {
 *         try {
 *             while (true) {
 *                 Thread.sleep(5);
 *                 System.out.printf("%s\n", SolvingStatisticsFlow.toJSON(solver));
 *             }
 *         } catch (InterruptedException e) {}
 *     });
 *     printer.start();
 *     while(solver.solve());
 *     printer.interrupt();
 * </pre>
 *
 * @author Charles Prud'homme
 * @since 06/07/2023
 */
public class SolvingStatisticsFlow {

    private static final HashMap<String, Function<Solver, String>> elements = new HashMap<>();

    static {
        elements.put("variables", solver -> Long.toString(solver.getModel().getNbVars()));
        elements.put("constraints", solver -> Long.toString(solver.getModel().getNbCstrs()));
        elements.put("objective", solver ->
                (solver.hasObjective() ? solver.getBestSolutionValue().toString() : "--"));
        elements.put("solutions", solver -> Long.toString(solver.getSolutionCount()));
        elements.put("nodes", solver -> Long.toString(solver.getNodeCount()));
        elements.put("fails", solver -> Long.toString(solver.getFailCount()));
        elements.put("backtracks", solver -> Long.toString(solver.getBackTrackCount()));
        elements.put("backjumps", solver -> Long.toString(solver.getBackjumpCount()));
        elements.put("restarts", solver -> Long.toString(solver.getRestartCount()));
        elements.put("fixpoints", solver -> Long.toString(solver.getFixpointCount()));
        elements.put("depth", solver -> Long.toString(solver.getMeasures().getCurrentDepth()));
        elements.put("time", solver -> SolvingStatisticsFlow.toHHmmss((long) (solver.getTimeCount() * 1000)));
        elements.put("memory", SolvingStatisticsFlow::memory);
    }

    private final Solver solver;

    public SolvingStatisticsFlow(Solver solver) {
        this.solver = solver;
    }

    /**
     * Return the solving statistics as a JSON object.
     * @return a JSON object
     */
    public String toJSON() {
        return toJSON(this.solver);
    }

    /**
     * Return the solving statistics as a JSON object.
     * @param solver a solver
     * @return a JSON object
     */
    public static String toJSON(Solver solver) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        elements.forEach((k, v) -> {
            sb.append("\"").append(k).append("\":\"").append(v.apply(solver)).append("\",");
        });
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    private static String toHHmmss(long etime) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(etime),
                TimeUnit.MILLISECONDS.toMinutes(etime) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(etime) % TimeUnit.MINUTES.toSeconds(1));
    }

    private static String memory(Solver solver) {
        try {
            long mem = solver.getModel().getEstimatedMemory();
            return String.format("%d", mem);
        } catch (Exception e) {
            return "-1";
        }
    }

}
