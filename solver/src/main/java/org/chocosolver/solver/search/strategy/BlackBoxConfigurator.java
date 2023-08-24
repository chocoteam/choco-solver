/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.restart.AbstractRestart;
import org.chocosolver.solver.search.restart.GeometricalCutoff;
import org.chocosolver.solver.search.restart.Restarter;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMax;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.tools.VariableUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A blackbox declaration assistant.
 * Taking a model as parameter, it analyses the variables declared
 * and create a black box strategy.
 * <br/>
 * The set of variables is first separated with respect to their type (integers, sets, ...).
 * Then, for each type, a search strategy is declared: a combination of variable selector
 * and value selector.
 * The order can be revised.
 * <br/>
 * Then, a restart policy is applied, together with meta-strategy (like last conflict).
 *
 * @author Charles Prud'homme
 * @since 10/05/2023
 */
@SuppressWarnings("UnusedReturnValue")
public class BlackBoxConfigurator {

    /**
     * Strategy to use on integer variables (default is {@link Search#intVarSearch(IntVar...)}
     */
    Function<IntVar[], AbstractStrategy<IntVar>> intVarStrategy;

    /**
     * Strategy to use on integer objective.
     */
    BiFunction<IntVar, Boolean, AbstractStrategy<IntVar>> intObjVarStrategy;

    /**
     * Strategy to use on set variables (default is {@link Search#setVarSearch(SetVar...)}
     */
    Function<SetVar[], AbstractStrategy<SetVar>> setVarStrategy;

    /**
     * Strategy to use on graph variables (default is {@link Search#graphVarSearch(GraphVar...)}
     */
    Function<GraphVar<?>[], AbstractStrategy<GraphVar<?>>> graphVarSearch;

    /**
     * Strategy to use on real variables (default is {@link Search#realVarSearch(RealVar...)}
     */
    Function<RealVar[], AbstractStrategy<RealVar>> realVarStrategy;

    /**
     * Strategy to use on real objective.
     */
    BiFunction<RealVar, Boolean, AbstractStrategy<RealVar>> realObjVarStrategy;
    /**
     * Meta strategy to use (default is {@link Search#lastConflict(AbstractStrategy)}
     */
    Function<AbstractStrategy<?>, AbstractStrategy<?>> metaStrategy;

    /**
     * Restart strategy (default is "no restart")
     */
    Function<Solver, AbstractRestart> restartPolicy;

    /**
     * Set nogood recording in restart (default is true)
     */
    boolean nogoodOnRestart;

    /**
     * Add Generating Partial Assignment procedure (default is false)
     */
    boolean generatePartialAssignment;

    /**
     * Force restart on solution
     */
    boolean restartOnSolution;

    /**
     * Set to <i>true</i> to exclude views in the search strategy scope (default is <i>false</i>)
     */
    boolean excludeViews;

    /**
     * Exclude the objective variable, if any, from the search strategy.
     */
    boolean excludeObjective;


    private BlackBoxConfigurator() {
    }

    /**
     * Initialize the black box builder with default settings.
     *
     * @return return a pre-configured black box configurator
     */
    public static BlackBoxConfigurator init() {
        return new BlackBoxConfigurator()
                .setIntVarStrategy(Search::intVarSearch)
                .setIntObjVarStrategy((obj, max) -> max ? Search.minDomUBSearch(obj) : Search.minDomLBSearch(obj))
                .setSetVarStrategy(Search::setVarSearch)
                .setGraphVarSearch(Search::graphVarSearch)
                .setRealVarStrategy(Search::realVarSearch)
                .setRealObjVarStrategy((obj, max) -> Search.realVarSearch(new Cyclic<>(), max ? new RealDomainMax() : new RealDomainMin(),
                        !max, obj))
                .setMetaStrategy(Search::lastConflict)
                .setRestartPolicy(solver -> AbstractRestart.NO_RESTART)
                .setRestartOnSolution(false)
                .setNogoodOnRestart(true)
                .setExcludeViews(false)
                .setExcludeObjective(true);
    }

    /**
     * Build a black box strategy from the model, using default settings.
     * The resolution policy is used to select the appropriate combination of strategies.
     *
     * @param model the model to use
     * @return a black box strategy configured for the given model
     */
    public static BlackBoxConfigurator prepare(Model model) {
        if (model.getSolver().getObjectiveManager().getPolicy() == ResolutionPolicy.SATISFACTION) {
            return forCSP();
        } else {
            return forCOP();
        }
    }

    /**
     * Build a black box strategy from the model adapted to constraint satisfaction problems (CSP)
     *
     * @return a black box strategy configured for CSP
     */
    public static BlackBoxConfigurator forCSP() {
        BlackBoxConfigurator bb = init();
        // search strategy
        SearchParams.ValSelConf defaultValSel = new SearchParams.ValSelConf(
                SearchParams.ValueSelection.MIN, false, 16, true);
        SearchParams.VarSelConf defaultVarSel = new SearchParams.VarSelConf(
                SearchParams.VariableSelection.DOMWDEG_CACD, 32);
        bb.setIntVarStrategy((vars) -> defaultVarSel.make().apply(vars, defaultValSel.make().apply(vars[0].getModel())));
        // restart policy
        bb.setRestartPolicy(s -> new Restarter(new GeometricalCutoff(5, 1.05),
                                    c -> s.getFailCount() >= c, 50_000, true));
        // complementary settings
        bb.setNogoodOnRestart(true)
                .setRestartOnSolution(false)
                .setExcludeObjective(true)
                .setExcludeViews(false)
                .setMetaStrategy(m -> Search.lastConflict(m, 4));
        return bb;
    }

    /**
     * Build a black box strategy from the model adapted to constraint optimization problems (COP)
     *
     * @return a black box strategy configured for COP
     */
    public static BlackBoxConfigurator forCOP() {
        BlackBoxConfigurator bb = init();
        // search strategy
        SearchParams.ValSelConf defaultValSel = new SearchParams.ValSelConf(
                SearchParams.ValueSelection.MIN, true, 16, true);
        SearchParams.VarSelConf defaultVarSel = new SearchParams.VarSelConf(
                SearchParams.VariableSelection.DOMWDEG, 32);
        bb.setIntVarStrategy((vars) -> defaultVarSel.make().apply(vars, defaultValSel.make().apply(vars[0].getModel())));
        // restart policy
        SearchParams.ResConf defaultResConf = new SearchParams.ResConf(
                SearchParams.Restart.GEOMETRIC, 10, 1.05, 50_000, true);
        bb.setRestartPolicy(defaultResConf.make());
        // complementary settings
        bb.setNogoodOnRestart(true)
                .setRestartOnSolution(true)
                .setExcludeObjective(true)
                .setExcludeViews(false)
                .setMetaStrategy(m -> Search.lastConflict(m, 1));
        return bb;
    }

    /**
     * Creates a default search strategy for the given model. This heuristic is complete (handles
     * IntVar, BoolVar, SetVar, GraphVar, and RealVar)
     *
     * @param model a model requiring a default search strategy
     */
    public void make(Model model) {
        complete(model, null);
    }

    /**
     * Complete a declared strategy with a default search strategy for the given model.
     * This heuristic is complete (handles IntVar, BoolVar, SetVar, GraphVar, and RealVar)
     *
     * @param model            a model requiring a default search strategy
     * @param declaredStrategy a declared strategy to complete with this default strategy (can be null)
     */
    public void complete(Model model, AbstractStrategy<Variable> declaredStrategy) {
        Solver solver = model.getSolver();
        // 1. retrieve variables, keeping the declaration order, and put them in four groups:
        List<IntVar> livars = new ArrayList<>(); // integer and boolean variables
        List<SetVar> lsvars = new ArrayList<>(); // set variables
        List<GraphVar<?>> lgvars = new ArrayList<>(); // graph variables
        List<RealVar> lrvars = new ArrayList<>();// real variables.
        Variable[] variables = model.getVars();
        Variable objective = null;
        for (Variable var : variables) {
            if (VariableUtils.isConstant(var)) continue;
            if (VariableUtils.isView(var) && excludeViews) continue;
            int type = var.getTypeAndKind();
            int kind = type & Variable.KIND;
            switch (kind) {
                case Variable.BOOL:
                case Variable.INT:
                    livars.add((IntVar) var);
                    break;
                case Variable.SET:
                    lsvars.add((SetVar) var);
                    break;
                case Variable.GRAPH:
                    lgvars.add((GraphVar<?>) var);
                    break;
                case Variable.REAL:
                    lrvars.add((RealVar) var);
                    break;
                default:
                    break; // do not throw exception to allow ad hoc variable kinds
            }
        }

        // 2. extract the objective variable if any (to avoid branching on it)
        if (solver.getObjectiveManager().isOptimization() && excludeObjective) {
            objective = solver.getObjectiveManager().getObjective();
            if ((objective.getTypeAndKind() & Variable.REAL) != 0) {
                //noinspection SuspiciousMethodCalls
                lrvars.remove(objective);// real var objective
            } else {
                assert (objective.getTypeAndKind() & Variable.INT) != 0;
                //noinspection SuspiciousMethodCalls
                livars.remove(objective);// bool/int var objective
            }
        }

        // 3. Creates a default search strategy for each variable kind
        ArrayList<AbstractStrategy<?>> strats = new ArrayList<>();
        if (declaredStrategy != null) {
            strats.add(declaredStrategy);
        }
        if (livars.size() > 0) {
            strats.add(intVarStrategy.apply(livars.toArray(new IntVar[0])));
        }
        if (lsvars.size() > 0) {
            strats.add(setVarStrategy.apply(lsvars.toArray(new SetVar[0])));
        }
        if (lgvars.size() > 0) {
            strats.add(graphVarSearch.apply(lgvars.toArray(new GraphVar[0])));
        }
        if (lrvars.size() > 0) {
            strats.add(realVarStrategy.apply(lrvars.toArray(new RealVar[0])));
        }

        // 4. lexico LB/UB branching for the objective variable
        if (objective != null) {
            boolean max = solver.getObjectiveManager().getPolicy() == ResolutionPolicy.MAXIMIZE;
            if ((objective.getTypeAndKind() & Variable.REAL) != 0) {
                strats.add(realObjVarStrategy.apply((RealVar) objective, max));
            } else {
                strats.add(intObjVarStrategy.apply((IntVar) objective, max));
            }
        }

        // 5. avoid null pointers in case all variables are instantiated
        if (strats.isEmpty()) {
            strats.add(Search.minDomLBSearch(model.boolVar(true)));
        }
        AbstractStrategy<?> main = Search.sequencer(strats.toArray(new AbstractStrategy[0]));
        solver.addRestarter(restartPolicy.apply(solver));
        if (nogoodOnRestart) {
            solver.setNoGoodRecordingFromRestarts();
        }
        // 6. add meta strats
        AbstractStrategy<?> strat = metaStrategy.apply(main);
        if (generatePartialAssignment) {
            strat = Search.generatePartialAssignment(
                    livars.toArray(new IntVar[0]),
                    20, false,
                    strat);
        }
        solver.setSearch(strat);
    }

    public BlackBoxConfigurator setIntVarStrategy(Function<IntVar[], AbstractStrategy<IntVar>> intVarStrategy) {
        this.intVarStrategy = intVarStrategy;
        return this;
    }

    public BlackBoxConfigurator setIntObjVarStrategy(BiFunction<IntVar, Boolean, AbstractStrategy<IntVar>> intObjVarStrategy) {
        this.intObjVarStrategy = intObjVarStrategy;
        return this;
    }

    public BlackBoxConfigurator setSetVarStrategy(Function<SetVar[], AbstractStrategy<SetVar>> setVarStrategy) {
        this.setVarStrategy = setVarStrategy;
        return this;
    }

    public BlackBoxConfigurator setGraphVarSearch(Function<GraphVar<?>[], AbstractStrategy<GraphVar<?>>> graphVarSearch) {
        this.graphVarSearch = graphVarSearch;
        return this;
    }

    public BlackBoxConfigurator setRealVarStrategy(Function<RealVar[], AbstractStrategy<RealVar>> realVarStrategy) {
        this.realVarStrategy = realVarStrategy;
        return this;
    }

    public BlackBoxConfigurator setRealObjVarStrategy(BiFunction<RealVar, Boolean, AbstractStrategy<RealVar>> realObjVarStrategy) {
        this.realObjVarStrategy = realObjVarStrategy;
        return this;
    }

    public BlackBoxConfigurator setMetaStrategy(Function<AbstractStrategy<?>, AbstractStrategy<?>> metaStrategy) {
        this.metaStrategy = metaStrategy;
        return this;
    }

    public BlackBoxConfigurator setRestartPolicy(SearchParams.Restart pol, int cutoff, double geo, int offset, boolean resetOnSolution) {
        SearchParams.ResConf conf = new SearchParams.ResConf(pol, cutoff, geo, offset, resetOnSolution);
        this.restartPolicy = conf.make();
        return this;
    }

    public BlackBoxConfigurator setRestartPolicy(Function<Solver, AbstractRestart> restartPolicy) {
        this.restartPolicy = restartPolicy;
        return this;
    }

    public BlackBoxConfigurator setNogoodOnRestart(boolean nogoodOnRestart) {
        this.nogoodOnRestart = nogoodOnRestart;
        return this;
    }

    public BlackBoxConfigurator setRestartOnSolution(boolean restartOnSolution) {
        this.restartOnSolution = restartOnSolution;
        return this;
    }

    public void setRefinedPartialAssignmentGeneration(boolean rgpa) {
        this.generatePartialAssignment = rgpa;
    }

    public BlackBoxConfigurator setExcludeViews(boolean excludeViews) {
        this.excludeViews = excludeViews;
        return this;
    }

    public BlackBoxConfigurator setExcludeObjective(boolean excludeObjective) {
        this.excludeObjective = excludeObjective;
        return this;
    }

    @Override
    public String toString() {
        return "BlackBoxConfigurator{" +
                "intVarStrategy=" + intVarStrategy +
                ", intObjVarStrategy=" + intObjVarStrategy +
                ", setVarStrategy=" + setVarStrategy +
                ", graphVarSearch=" + graphVarSearch +
                ", realVarStrategy=" + realVarStrategy +
                ", realObjVarStrategy=" + realObjVarStrategy +
                ", metaStrategy=" + metaStrategy +
                ", restartPolicy=" + restartPolicy +
                ", nogoodOnRestart=" + nogoodOnRestart +
                ", restartOnSolution=" + restartOnSolution +
                ", rgpa=" + generatePartialAssignment +
                ", excludeViews=" + excludeViews +
                ", excludeObjective=" + excludeObjective +
                '}';
    }
}
