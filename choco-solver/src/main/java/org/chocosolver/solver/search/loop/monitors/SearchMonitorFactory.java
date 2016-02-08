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
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.limits.*;
import org.chocosolver.solver.search.restart.GeometricalRestartStrategy;
import org.chocosolver.solver.search.restart.LubyRestartStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.criteria.Criterion;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/05/11
 */
public class SearchMonitorFactory {
    /**
     * Constructor
     */
    SearchMonitorFactory() {
    }

    /**
     * To convert milliseconds in nanoseconds
     */
    private static final long MILLISECONDS_IN_NANOSECONDS = 1000 * 1000;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Branch a luby restart strategy to the model
     *
     * @param model               the model
     * @param scaleFactor          scale factor
     * @param geometricalFactor    increasing factor
     * @param restartStrategyLimit restart trigger
     * @param restartLimit         restart limits (limit of number of restarts)
     */
    public static void luby(Model model, int scaleFactor, int geometricalFactor, ICounter restartStrategyLimit, int restartLimit) {
        model.getResolver().setRestarts(restartStrategyLimit, new LubyRestartStrategy(scaleFactor, geometricalFactor), restartLimit);
    }

    /**
     * Build a geometrical restart strategy
     *
     * @param model               the solver
     * @param scaleFactor          scale factor
     * @param geometricalFactor    increasing factor
     * @param restartStrategyLimit restart trigger
     * @param restartLimit         restart limits (limit of number of restarts)
     */
    public static void geometrical(Model model, int scaleFactor, double geometricalFactor,
                                   ICounter restartStrategyLimit, int restartLimit) {
        model.getResolver().setRestarts(restartStrategyLimit, new GeometricalRestartStrategy(scaleFactor, geometricalFactor), restartLimit);
    }

    /**
     * Defines a limit on the number of nodes allowed in the tree search.
     * When the limit is reached, the resolution is stopped.
     *
     * @param model the solver to instrument
     * @param limit maximal number of nodes to open
     */
    public static void limitNode(Model model, long limit) {
        NodeCounter counter = new NodeCounter(model, limit);
        model.getResolver().addStopCriterion(counter);
    }

    /**
     * Defines a limit over the number of solutions found during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     *
     * @param model the solver to instrument
     * @param limit maximal number of solutions
     */
    public static void limitSolution(Model model, long limit) {
        SolutionCounter counter = new SolutionCounter(model, limit);
        model.getResolver().addStopCriterion(counter);
    }


    /**
     * Defines a limit over the run time.
     * When the limit is reached, the resolution is stopped.
     * <br/>
     * <br/>
     * <b>One must consider also {@code SearchMonitorFactory.limitThreadTime(long)}, that runs the limit in a separated thread.</b>
     *
     * @param model the solver subject to the time limit
     * @param limit  maximal resolution time in millisecond
     */
    public static void limitTime(Model model, long limit) {
        TimeCounter counter = new TimeCounter(model, limit * MILLISECONDS_IN_NANOSECONDS);
        model.getResolver().addStopCriterion(counter);
    }

    /**
     * Defines a limit over the run time.
     * When the limit is reached, the resolution is stopped.
     * <br/>
     * <br/>
     * <b>One must consider also {@code SearchMonitorFactory.limitThreadTime(String)}, that runs the limit in a separated thread.</b>
     * <p>
     * Based on {@code SearchMonitorFactory.convertInMilliseconds(String duration)}
     *
     * @param model the solver to instrument
     * @param duration a String which states the duration like "WWd XXh YYm ZZs".
     * @see SearchMonitorFactory#convertInMilliseconds(String)
     */
    public static void limitTime(Model model, String duration) {
        limitTime(model, convertInMilliseconds(duration));
    }

    /**
     * Pattern for days
     */
    private static Pattern Dp = Pattern.compile("(\\d+)d");

    /**
     * Pattern for hours
     */
    private static Pattern Hp = Pattern.compile("(\\d+)h");

    /**
     * Pattern for minutes
     */
    private static Pattern Mp = Pattern.compile("(\\d+)m");

    /**
     * Pattern for seconds
     */
    private static Pattern Sp = Pattern.compile("(\\d+(\\.\\d+)?)s");


    /**
     * Convert a string which represents a duration. It can be composed of days, hours, minutes and seconds.
     * Examples:
     * <p>
     * - "1d2h3m4.5s": one day, two hours, three minutes, four seconds and 500 milliseconds<p/>
     * - "2h30m": two hours and 30 minutes<p/>
     * - "30.5s": 30 seconds and 500 ms<p/>
     * - "180s": three minutes
     *
     * @param duration a String which describes the duration
     * @return the duration in milliseconds
     */
    public static long convertInMilliseconds(String duration) {
        long milliseconds = 0;
        duration = duration.replaceAll("\\s+", "");
        Matcher matcher = Dp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 1) {
            int days = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
        }
        matcher = Hp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 1) {
            int hours = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
        }
        matcher = Mp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 1) {
            int minutes = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
        }
        matcher = Sp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 2) {
            double seconds = Double.parseDouble(matcher.group(1));
            milliseconds += (int) (seconds * 1000);
        }
        if (milliseconds == 0) {
            milliseconds = Long.parseLong(duration);
        }
        return milliseconds;
    }

    /**
     * Convert a string which represents a duration. It can be composed of days, hours, minutes and seconds.
     * Examples:
     * <p>
     * - "1d2h3m4.5s": one day, two hours, three minutes, four seconds and 500 milliseconds<p/>
     * - "2h30m": two hours and 30 minutes<p/>
     * - "30.5s": 30 seconds and 500 ms<p/>
     * - "180s": three minutes
     *
     * @param duration a String which describes the duration
     * @return the duration in seconds
     */
    public static long convertInSeconds(String duration) {
        long milliseconds = 0;
        duration = duration.replaceAll("\\s+", "");
        Matcher matcher = Dp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 1) {
            int days = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.SECONDS.convert(days, TimeUnit.DAYS);
        }
        matcher = Hp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 1) {
            int hours = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.SECONDS.convert(hours, TimeUnit.HOURS);
        }
        matcher = Mp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 1) {
            int minutes = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.SECONDS.convert(minutes, TimeUnit.MINUTES);
        }
        matcher = Sp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 2) {
            double seconds = Double.parseDouble(matcher.group(1));
            milliseconds += (int) (seconds);
        }
        if (milliseconds == 0) {
            milliseconds = Long.parseLong(duration);
        }
        return milliseconds;
    }

    /**
     * Defines a limit over the number of fails allowed during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     * @param model the solver to instrument
     * @param limit maximal number of fails
     */

    public static void limitFail(Model model, long limit) {
        FailCounter counter = new FailCounter(model, limit);
        model.getResolver().addStopCriterion(counter);
    }

    /**
     * Defines a limit over the number of backtracks allowed during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     *
     * @param model the solver to instrument
     * @param limit maximal number of backtracks
     */
    public static void limitBacktrack(Model model, long limit) {
        BacktrackCounter counter = new BacktrackCounter(model, limit);
        model.getResolver().addStopCriterion(counter);
    }

    /**
     * Limit the exploration of the search space with the help of a <code>aStopCriterion</code>.
     * When the condition depicted in the criterion is met,
     * the search stops.
     *
     * @param model the solver to instrument
     * @param aStopCriterion the stop criterion which, when met, stops the search.
     */
    public static void limitSearch(Model model, Criterion aStopCriterion) {
        model.getResolver().addStopCriterion(aStopCriterion);
    }

    /**
     * Record nogoods from solution, that is, anytime a solution is found, a nogood is produced to prevent from
     * finding the same solution later during the search.
     * <code>vars</code> are the decision variables (to reduce ng size).
     *
     * @param vars array of decision variables
     */
    public static void nogoodRecordingOnSolution(IntVar[] vars) {
        vars[0].getModel().getResolver().plugMonitor(new NogoodFromSolutions(vars));
    }

    /**
     * * Record nogoods from restart, that is, anytime the search restarts, a nogood is produced, based on the decision path, to prevent from
     * scanning the same sub-search tree.
     *
     * @param model the solver to observe
     */
    public static void nogoodRecordingFromRestarts(final Model model) {
        model.getResolver().plugMonitor(new NogoodFromRestarts(model));
    }

    /**
     * @deprecated Use instead {@link org.chocosolver.solver.ParallelResolution} which centralizes parallel resolution process
     * and offers more services.
     */
    @Deprecated
    public static void prepareForParallelResolution(List<Model> models) {
        if (models.get(0).getObjectives() != null &&
                models.get(0).getObjectives().length == 1) {
            // share the best known bound
            models.stream().forEach(s -> s.getResolver().plugMonitor(
                    (IMonitorSolution) () -> {
                        synchronized (s.getResolver().getObjectiveManager()) {
                            switch (s.getResolver().getObjectiveManager().getPolicy()) {
                                case MAXIMIZE:
                                    int lb = s.getResolver().getObjectiveManager().getBestSolutionValue().intValue();
                                    models.forEach(s1 -> s1.getResolver().getObjectiveManager().updateBestLB(lb));
                                    break;
                                case MINIMIZE:
                                    int ub = s.getResolver().getObjectiveManager().getBestSolutionValue().intValue();
                                    models.forEach(s1 -> s1.getResolver().getObjectiveManager().updateBestUB(ub));
                                    break;
                            }
                        }
                    }
            ));
        }
        AtomicInteger finishers = new AtomicInteger(0);
        models.stream().forEach(s -> s.getResolver().addStopCriterion(()->finishers.get()>0));
        models.stream().forEach(s -> s.getResolver().plugMonitor(new IMonitorClose() {
            @Override
            public void afterClose() {
                int count = finishers.addAndGet(1);
                if(count == models.size()){
                    finishers.set(0); //reset the counter to 0
                }
            }
        }));
    }

    /**
     * Connect and send data to <a href="https://github.com/cp-profiler/cp-profiler">cp-profiler</a>.
     * This requires to have installed the library and to start it before launching the resolution.
     * @param aModel solver to visualize
     */
    @SuppressWarnings("unused")
    public static void connectocpprofiler(Model aModel){
        aModel.getResolver().plugMonitor(new CPProfiler(aModel));
    }

}
