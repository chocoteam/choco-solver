/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.restart.*;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.TimeUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Easily configure black-box search strategies.
 * <br/>
 * <p>
 * It lists the most common search strategies and provides a simple way to configure them.
 * <p>
 * The main parameters are defined as enums.
 * <br/>
 * The four main configurations are:
 * <ul>
 *     <li>variable selection</li>
 *     <li>value selection</li>
 *     <li>restart strategy</li>
 *     <li>search limits</li>
 * </ul>
 * <p>
 *
 * @author Charles Prud'homme
 * @since 26/05/2023
 */
public interface SearchParams {
    /**
     * Different restart strategies
     */
    enum Restart {
        NONE, LUBY, GEOMETRIC, ARITHMETIC
    }

    /**
     * Different variable selection strategies
     */
    enum VariableSelection {
        ACTIVITY,
        CHS,
        DOM, /* or */ FIRST_FAIL,

        DOMWDEG,
        DOMWDEG_CACD,
        FLBA,
        FRBA,
        INPUT,
        PICKONDOM0,
        PICKONDOM1,
        PICKONDOM2,
        PICKONDOM3,
        PICKONFIL0,
        PICKONFIL1,
        PICKONFIL2,
        PICKONFIL3,
        RAND,
    }

    /**
     * Different variable selection tie breaking strategies
     */
    enum VariableTieBreaker {
        LEX, SMALLEST_DOMAIN, LARGEST_DOMAIN, SMALLEST_VALUE, LARGEST_VALUE
    }

    /**
     * Different value selection strategies
     */
    enum ValueSelection {
        MIN,
        MAX,
        MED,
        MIDFLOOR,
        MIDCEIL,
        RAND,
    }

    /**
     * A class to configure the restart strategy
     */
    class ResConf {
        final SearchParams.Restart pol;
        final int cutoff;
        final int offset;
        final double geo;
        final boolean resetOnSolution;

        /**
         * Configure the restart strategy
         *
         * @param pol             restart policy
         * @param cutoff          cutoff value
         * @param geo             geometric factor (only for GEOMETRIC policy, ignored otherwise)
         * @param offset          offset value (when to stop restarting)
         * @param resetOnSolution set to <tt>true</tt> to reset the restart policy on solution
         * @implNote The default restart criteria is to restart on fail count.
         * If the policy is NONE, then the restart strategy is disabled.
         */
        public ResConf(SearchParams.Restart pol, int cutoff, double geo, int offset, boolean resetOnSolution) {
            this.pol = pol;
            this.cutoff = cutoff;
            this.offset = offset;
            this.geo = geo;
            this.resetOnSolution = resetOnSolution;
        }

        /**
         * Configure the restart strategy
         *
         * @param pol             restart policy
         * @param cutoff          cutoff value
         * @param offset          offset value (when to stop restarting)
         * @param resetOnSolution set to <tt>true</tt> to reset the restart policy on solution
         * @implNote The default restart criteria is to restart on fail count.
         * If the policy is NONE, then the restart strategy is disabled.
         * If the policy is GEOMETRIC, then the geometric factor is set to 1.05.
         */
        public ResConf(SearchParams.Restart pol, int cutoff, int offset, boolean resetOnSolution) {
            this(pol, cutoff, 1.05d, offset, resetOnSolution);
        }

        /**
         * Create the restart strategy to be used in the search as a function of the solver
         *
         * @return a function which creates the restart strategy
         */
        public Function<Solver, AbstractRestart> make() {
            switch (pol) {
                default:
                case NONE:
                    return s -> AbstractRestart.NO_RESTART;
                case LUBY:
                    return (s) -> new Restarter(new LubyCutoff(cutoff),
                            c -> s.getFailCount() >= c, offset, resetOnSolution);
                case GEOMETRIC:
                    return (s) -> new Restarter(new GeometricalCutoff(cutoff, geo),
                            c -> s.getFailCount() >= c, offset, resetOnSolution);
                case ARITHMETIC:
                    return (s) -> new Restarter(new LinearCutoff(cutoff),
                            c -> s.getFailCount() >= c, offset, resetOnSolution);
            }
        }
    }

    /**
     * A class to configure the search limits.
     * A limit can be defined as:
     * <ul>
     *     <li>time limit (in ms)</li>
     *     <li>number of solutions</li>
     *     <li>number of runs (or restarts)</li>
     */
    class LimConf {
        final long time; // in ms
        final int sols;
        final int runs;

        /**
         * Configure the search limits
         *
         * @param timeInMS time limit in milliseconds, -1 to ignore
         * @param sols     number of solutions, -1 to ignore
         * @param runs     number of runs (or restarts), -1 to ignore
         */
        public LimConf(long timeInMS, int sols, int runs) {
            this.time = timeInMS;
            this.sols = sols;
            this.runs = runs;
        }

        /**
         * Configure the search limits
         *
         * @param duration time limit as a string (see {@link TimeUtils#convertInMilliseconds(String)}), -1 to ignore
         * @param sols     number of solutions, -1 to ignore
         * @param runs     number of runs (or restarts), -1 to ignore
         */
        @SuppressWarnings("unused")
        public LimConf(String duration, int sols, int runs) {
            this.time = TimeUtils.convertInMilliseconds(duration);
            this.sols = sols;
            this.runs = runs;
        }

        /**
         * Get the time limit in milliseconds
         *
         * @return the time limit in milliseconds
         */
        public long getTime() {
            return time;
        }

        /**
         * Get the limit on the number of solutions
         *
         * @return the limit on the number of solutions
         */
        public int getSols() {
            return sols;
        }

        /**
         * Get the limit on the number of runs (or restarts)
         *
         * @return the limit on the number of runs (or restarts)
         */
        public int getRuns() {
            return runs;
        }
    }

    /**
     * A class to configure the variable selection strategy
     */
    class VarSelConf {
        final SearchParams.VariableSelection varsel;
        final int flushRate;

        /**
         * Configure the variable selection strategy
         *
         * @param varsel    variable selection strategy
         * @param flushRate number of restarts before flushing the scores of the variables
         */
        public VarSelConf(SearchParams.VariableSelection varsel, int flushRate) {
            this.varsel = varsel;
            this.flushRate = flushRate;
        }

        /**
         * Create the variable selection strategy to be used in the search as a function of the variables in scope.
         *
         * @return the variable selection strategy as a function of variables
         */
        public BiFunction<IntVar[], IntValueSelector, AbstractStrategy<IntVar>> make() {
            switch (varsel) {
                case ACTIVITY:
                    return (vars, vsel) -> new ActivityBased(vars[0].getModel(), vars, vsel,
                            0.999d, 0.2d, 8, 1, 0);
                case CHS:
                    return (vars, vsel) -> Search.intVarSearch(new ConflictHistorySearch<>(vars, 0, flushRate), vsel, vars);
                case DOM:
                case FIRST_FAIL:
                    return (vars, vsel) -> Search.intVarSearch(new FirstFail(vars[0].getModel()), vsel, vars);
                default:
                case DOMWDEG:
                    return (vars, vsel) -> Search.intVarSearch(new DomOverWDeg<>(vars, 0, flushRate), vsel, vars);
                case DOMWDEG_CACD:
                    return (vars, vsel) -> Search.intVarSearch(new DomOverWDegRef<>(vars, 0, flushRate), vsel, vars);
                case FLBA:
                    return (vars, vsel) -> Search.intVarSearch(new FailureBased<>(vars, 0, 4), vsel, vars);
                case FRBA:
                    return (vars, vsel) -> Search.intVarSearch(new FailureBased<>(vars, 0, 2), vsel, vars);
                case INPUT:
                    return (vars, vsel) -> Search.intVarSearch(new InputOrder<>(vars[0].getModel()), vsel, vars);
                case PICKONDOM0:
                    return (vars, vsel) -> Search.intVarSearch(new PickOnDom<>(vars, 0, flushRate), vsel, vars);
                case PICKONDOM1:
                    return (vars, vsel) -> Search.intVarSearch(new PickOnDom<>(vars, 1, flushRate), vsel, vars);
                case PICKONDOM2:
                    return (vars, vsel) -> Search.intVarSearch(new PickOnDom<>(vars, 2, flushRate), vsel, vars);
                case PICKONDOM3:
                    return (vars, vsel) -> Search.intVarSearch(new PickOnDom<>(vars, 3, flushRate), vsel, vars);
                case PICKONFIL0:
                    return (vars, vsel) -> Search.intVarSearch(new PickOnFil<>(vars, 0, flushRate), vsel, vars);
                case PICKONFIL1:
                    return (vars, vsel) -> Search.intVarSearch(new PickOnFil<>(vars, 1, flushRate), vsel, vars);
                case PICKONFIL2:
                    return (vars, vsel) -> Search.intVarSearch(new PickOnFil<>(vars, 2, flushRate), vsel, vars);
                case PICKONFIL3:
                    return (vars, vsel) -> Search.intVarSearch(new PickOnFil<>(vars, 3, flushRate), vsel, vars);
                case RAND:
                    return (vars, vsel) -> Search.intVarSearch(new Random<>(vars[0].getModel().getSeed()), vsel, vars);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof VarSelConf) {
                VarSelConf other = (VarSelConf) obj;
                return varsel == other.varsel && flushRate == other.flushRate;
            }
            return false;
        }
    }

    /**
     * A class to configure the value selection strategy, to be combined with a variable selection strategy
     */
    class ValSelConf {
        final SearchParams.ValueSelection valsel;
        final boolean best;
        final int bestFreq;
        final boolean last;

        /**
         * Configure the value selection strategy
         *
         * @param valH     value selection strategy
         * @param best     set to <tt>true</tt> to apply the best value selection strategy, <tt>false</tt> otherwise
         * @param bestFreq if <tt>best</tt> is set to <tt>true</tt>, the frequency at which the best value selection strategy is applied.
         *                 For example, when set to 12, the best value selection is applied at the first run and then every 12 restarts.
         * @param last     set to <tt>true</tt> to apply the last value selection strategy (or phase saving), <tt>false</tt> otherwise
         */
        public ValSelConf(SearchParams.ValueSelection valH, boolean best, int bestFreq, boolean last) {
            this.valsel = valH;
            this.best = best;
            this.bestFreq = bestFreq;
            this.last = last;
        }

        /**
         * Create the value selection strategy to be used in the search
         *
         * @return the value selection strategy
         * @implNote The model is required to monitor the solutions found by the search if the last value selection strategy is used.
         */
        public Function<Model, IntValueSelector> make() {
            final Function<Model, IntValueSelector> fn0;
            switch (valsel) {
                default:
                case MIN:
                    fn0 = m -> new IntDomainMin();
                    break;
                case MAX:
                    fn0 = m -> new IntDomainMax();
                    break;
                case MED:
                    fn0 = m -> new IntDomainMedian();
                    break;
                case MIDCEIL:
                    fn0 = m -> new IntDomainMiddle(false);
                    break;
                case MIDFLOOR:
                    fn0 = m -> new IntDomainMiddle(true);
                    break;
                case RAND:
                    fn0 = m -> new IntDomainRandom(m.getSeed());
                    break;
            }
            final Function<Model, IntValueSelector> fn1;
            if (best) {
                fn1 = m -> new IntDomainBest(fn0.apply(m), v -> m.getSolver().getRestartCount() % bestFreq == 0);
            } else {
                fn1 = fn0;
            }
            final Function<Model, IntValueSelector> fn2;
            if (last) {
                fn2 = m -> {
                    m.getSolver().attach(m.getSolver().defaultSolution());
                    return new IntDomainLast(m.getSolver().defaultSolution(), fn1.apply(m), null);
                };
            } else {
                fn2 = fn1;
            }
            return fn2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ValSelConf) {
                ValSelConf vsc = (ValSelConf) obj;
                return valsel == vsc.valsel && best == vsc.best && bestFreq == vsc.bestFreq && last == vsc.last;
            }
            return false;
        }
    }
}
