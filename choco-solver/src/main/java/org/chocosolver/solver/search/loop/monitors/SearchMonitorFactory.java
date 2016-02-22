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
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.*;
import org.chocosolver.solver.search.restart.GeometricalRestartStrategy;
import org.chocosolver.solver.search.restart.LubyRestartStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.criteria.Criterion;
import org.chocosolver.util.tools.TimeUtils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @deprecated use {@link Solver}, which extends {@link ISearchMonitorFactory}, instead
 * Will be removed after version 3.4.0
 */
@Deprecated
public class SearchMonitorFactory {
    SearchMonitorFactory() {}

    /**
     * @deprecated use {@link TimeUtils} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    private static final long MILLISECONDS_IN_NANOSECONDS = 1000 * 1000;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @deprecated use {@link Solver#setLubyRestart(int, int, ICounter, int)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void luby(Model model, int scaleFactor, int geometricalFactor, ICounter restartStrategyLimit, int restartLimit) {
        model.getSolver().setRestarts(restartStrategyLimit, new LubyRestartStrategy(scaleFactor, geometricalFactor), restartLimit);
    }

    /**
     * @deprecated use {@link Solver#setGeometricalRestart(int, double, ICounter, int)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void geometrical(Model model, int scaleFactor, double geometricalFactor,
                                   ICounter restartStrategyLimit, int restartLimit) {
        model.getSolver().setRestarts(restartStrategyLimit, new GeometricalRestartStrategy(scaleFactor, geometricalFactor), restartLimit);
    }

    /**
     * @deprecated use {@link Solver#limitNode(long)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void limitNode(Model model, long limit) {
        NodeCounter counter = new NodeCounter(model, limit);
        model.getSolver().addStopCriterion(counter);
    }

    /**
     * @deprecated use {@link Solver#limitSolution(long)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void limitSolution(Model model, long limit) {
        SolutionCounter counter = new SolutionCounter(model, limit);
        model.getSolver().addStopCriterion(counter);
    }

    /**
     * @deprecated use {@link Solver#limitTime(long)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void limitTime(Model model, long limit) {
        TimeCounter counter = new TimeCounter(model, limit * MILLISECONDS_IN_NANOSECONDS);
        model.getSolver().addStopCriterion(counter);
    }

    /**
     * @deprecated use {@link Solver#limitTime(String)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void limitTime(Model model, String duration) {
        model.getSolver().limitTime(convertInMilliseconds(duration));
    }

    /**
     * @deprecated use {@link TimeUtils} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    private static Pattern Dp = Pattern.compile("(\\d+)d");

    /**
     * @deprecated use {@link TimeUtils} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    private static Pattern Hp = Pattern.compile("(\\d+)h");

    /**
     * @deprecated use {@link TimeUtils} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    private static Pattern Mp = Pattern.compile("(\\d+)m");

    /**
     * @deprecated use {@link TimeUtils} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    private static Pattern Sp = Pattern.compile("(\\d+(\\.\\d+)?)s");

    /**
     * @deprecated use {@link TimeUtils} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
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
     * @deprecated use {@link TimeUtils} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
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
     * @deprecated use {@link Solver#limitFail(long)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void limitFail(Model model, long limit) {
        FailCounter counter = new FailCounter(model, limit);
        model.getSolver().addStopCriterion(counter);
    }

    /**
     * @deprecated use {@link Solver#limitBacktrack(long)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void limitBacktrack(Model model, long limit) {
        BacktrackCounter counter = new BacktrackCounter(model, limit);
        model.getSolver().addStopCriterion(counter);
    }

    /**
     * @deprecated use {@link Solver#limitSearch(Criterion)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void limitSearch(Model model, Criterion aStopCriterion) {
        model.getSolver().addStopCriterion(aStopCriterion);
    }

    /**
     * @deprecated use {@link Solver#setNoGoodRecordingFromSolutions(IntVar...)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void nogoodRecordingOnSolution(IntVar[] vars) {
        vars[0].getModel().getSolver().plugMonitor(new NogoodFromSolutions(vars));
    }

    /**
     * @deprecated use {@link Solver#setNoGoodRecordingFromRestarts()} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void nogoodRecordingFromRestarts(final Model model) {
        model.getSolver().plugMonitor(new NogoodFromRestarts(model));
    }

    /**
     * @deprecated use {@link Solver#connectocpprofiler()} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void connectocpprofiler(Model aModel){
        aModel.getSolver().plugMonitor(new CPProfiler(aModel));
    }

}
