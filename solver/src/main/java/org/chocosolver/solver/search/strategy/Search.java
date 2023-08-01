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

import org.chocosolver.solver.search.restart.GeometricalCutoff;
import org.chocosolver.solver.search.restart.LinearCutoff;
import org.chocosolver.solver.search.restart.LubyCutoff;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.objective.ObjectiveStrategy;
import org.chocosolver.solver.objective.OptimizationPolicy;
import org.chocosolver.solver.search.loop.monitors.IMonitorOpenNode;
import org.chocosolver.solver.search.restart.MonotonicCutoff;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IbexDecision;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.values.graph.edge.GraphEdgeSelector;
import org.chocosolver.solver.search.strategy.selectors.values.graph.edge.GraphLexEdge;
import org.chocosolver.solver.search.strategy.selectors.values.graph.edge.GraphRandomEdge;
import org.chocosolver.solver.search.strategy.selectors.values.graph.node.GraphLexNode;
import org.chocosolver.solver.search.strategy.selectors.values.graph.node.GraphNodeSelector;
import org.chocosolver.solver.search.strategy.selectors.values.graph.node.GraphRandomNode;
import org.chocosolver.solver.search.strategy.selectors.values.graph.priority.GraphNodeOrEdgeSelector;
import org.chocosolver.solver.search.strategy.selectors.values.graph.priority.GraphNodeThenEdges;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.search.strategy.strategy.*;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.bandit.MOSS;
import org.chocosolver.util.bandit.Static;
import org.chocosolver.util.tools.VariableUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.ToDoubleBiFunction;

public class Search {

    // ************************************************************************************
    // GENERIC PATTERNS
    // ************************************************************************************

    /**
     * Use the last conflict heuristic as a pluggin to improve a former search heuristic Should be
     * set after specifying a search strategy.
     *
     * @return last conflict strategy
     */
    public static <V extends Variable> AbstractStrategy<V> lastConflict(
            AbstractStrategy<V> formerSearch) {
        return lastConflict(formerSearch, 1);
    }

    /**
     * Search heuristic combined with a constraint performing strong consistency on the next
     * decision variable and branching on the value with the best objective bound (for optimization)
     * and branches on the lower bound for SAT problems.
     * <p>
     * BEWARE: ONLY FOR INTEGERS (lets the former search work for other variable types)
     *
     * @param formerSearch default search to branch on variables (defines the variable selector and
     *                     the value selector when this does not hold)
     * @return best bound strategy
     */
    public static AbstractStrategy<IntVar> bestBound(AbstractStrategy<IntVar> formerSearch) {
        if (formerSearch == null) {
            throw new UnsupportedOperationException(
                    "the search strategy in parameter cannot be null! Consider using Search.defaultSearch(model)");
        }
        return new BoundSearch(formerSearch);
    }

    /**
     * Use the last conflict heuristic as a pluggin to improve a former search heuristic Should be
     * set after specifying a search strategy.
     *
     * @param k the maximum number of conflicts to store
     * @return last conflict strategy
     */
    public static <V extends Variable> AbstractStrategy<V> lastConflict(
            AbstractStrategy<V> formerSearch, int k) {
        if (formerSearch == null) {
            throw new UnsupportedOperationException(
                    "the search strategy in parameter cannot be null! Consider using Search.defaultSearch(model)");
        }
        return new LastConflict<>(formerSearch.getVariables()[0].getModel(), formerSearch, k);
    }

    /**
     * Use the conflict ordering search as a pluggin to improve a former search heuristic Should be
     * set after specifying a search strategy.
     *
     * @return last conflict strategy
     */
    public static <V extends Variable> AbstractStrategy<V> conflictOrderingSearch(
            AbstractStrategy<V> formerSearch) {
        return new ConflictOrderingSearch<>(formerSearch.getVariables()[0].getModel(),
                formerSearch);
    }

    /**
     * Use the Generating Partial Assignment procedure as a plug-in to improve a former search heuristic
     * for COPs.
     * <br/>
     * The aim of the approach is to find promising partial assignments that have a higher possibility of being optimal,
     * or can be extended to a high-quality solution whose objective is close to that of an optimal one.
     * </br>
     * This meta-heuristic is described in:
     * "Finding Good Partial Assignments During Restart-based Branch and Bound Search, AAAI'23".
     *
     * @param ivars      variables to generate partial assignment from
     * @param maxSolNum  maximum number of solutions to store
     * @param largerCutoff whether to use larger cutoff when looking for solutions
     * @param formerSearch former search heuristic
     * @return good partial assignment strategy to plug in as first strategy
     */
    public static AbstractStrategy<?> generatePartialAssignment(IntVar[] ivars,
                                                                int maxSolNum,
                                                                boolean largerCutoff,
                                                                AbstractStrategy<?> formerSearch) {
        return new PartialAssignmentGenerator<>(ivars, maxSolNum, largerCutoff, formerSearch);
    }

    /**
     * Make the input search strategy greedy, that is, decisions can be applied but not refuted.
     *
     * @param search a search heuristic building branching decisions
     * @return a greedy form of search
     */
    public static AbstractStrategy<?> greedySearch(AbstractStrategy<?> search) {
        return new GreedyBranching(search);
    }

    /**
     * Apply sequentialy enumeration strategies. Strategies are considered in input order. When
     * strategy <i>i</i> returns null (all variables are instantiated) the <i>i+1</i> ones is
     * activated.
     *
     * @param searches ordered set of enumeration strategies
     * @throws IllegalArgumentException when the array of strategies is either null or empty.
     */
    public static AbstractStrategy sequencer(AbstractStrategy... searches) {
        if (searches == null || searches.length == 0) {
            throw new IllegalArgumentException("The array of strategies cannot be null or empty");
        }
        return new StrategiesSequencer(searches);
    }

    // ************************************************************************************
    // SETVAR STRATEGIES
    // ************************************************************************************

    /**
     * Generic strategy to branch on set variables
     *
     * @param varS         variable selection strategy
     * @param valS         integer  selection strategy
     * @param enforceFirst branching order true = enforce first; false = remove first
     * @param sets         SetVar array to branch on
     * @return a strategy to instantiate sets
     * @throws IllegalArgumentException when the array of variables is either null or empty.
     */
    public static SetStrategy setVarSearch(VariableSelector<SetVar> varS, SetValueSelector valS,
                                           boolean enforceFirst, SetVar... sets) {
        if (sets == null || sets.length == 0) {
            throw new IllegalArgumentException("The array of variables cannot be null or empty");
        }
        return new SetStrategy(sets, varS, valS, enforceFirst);
    }

    /**
     * strategy to branch on sets by choosing the first unfixed variable and forcing its first
     * unfixed value
     *
     * @param sets variables to branch on
     * @return a strategy to instantiate sets
     */
    public static SetStrategy setVarSearch(SetVar... sets) {
        return setVarSearch(new GeneralizedMinDomVarSelector<>(), new SetDomainMin(), true, sets);
    }

    /**
     * Assignment strategy which selects a variable according to <code>DomOverWDeg</code> and assign
     * it to its lower bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote This is based on "Boosting Systematic Search by Weighting Constraints."
     * Boussemart et al. ECAI 2004.
     * <a href="https://dblp.org/rec/conf/ecai/BoussemartHLS04">https://dblp.org/rec/conf/ecai/BoussemartHLS04</a>
     */
    public static AbstractStrategy<SetVar> domOverWDegSearch(SetVar... vars) {
        return setVarSearch(new DomOverWDeg<>(vars, 0), new SetDomainMin(), true, vars);
    }

    /**
     * Assignment strategy which selects a variable according to <code>refined DomOverWDeg</code> and assign
     * it to its lower bound, where the weight incrementer is "ca.cd".
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote This is based on "Refining Constraint Weighting." Wattez et al. ICTAI 2019.
     * <a href="https://dblp.org/rec/conf/ictai/WattezLPT19">https://dblp.org/rec/conf/ictai/WattezLPT19</a>
     */
    public static AbstractStrategy<SetVar> domOverWDegRefSearch(SetVar... vars) {
        return setVarSearch(new DomOverWDegRef<>(vars, 0), new SetDomainMin(), true, vars);
    }

    /**
     * Assignment strategy which selects a variable according to <code>Conflict History</code>
     * and assigns it to its lower bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote This is based on "Conflict history based search for constraint satisfaction problem."
     * Habet et al. SAC 2019.
     * <a href="https://dblp.org/rec/conf/sac/HabetT19">https://dblp.org/rec/conf/sac/HabetT19</a>
     */
    public static AbstractStrategy<SetVar> conflictHistorySearch(SetVar... vars) {
        return setVarSearch(new ConflictHistorySearch<>(vars, 0), new SetDomainMin(), true, vars);
    }

    /**
     * Assignment strategy which selects a variable according to <code>Failure rate based</code>
     * variable ordering and assigns it to its lower bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote This is based on "Failure Based Variable Ordering Heuristics for Solving CSPs."
     * H. Li, M. Yin, and Z. Li, CP 2021.
     * <a href="https://dblp.org/rec/conf/cp/LiYL21">https://dblp.org/rec/conf/cp/LiYL21</a>
     */
    public static AbstractStrategy<SetVar> failureRateBasedSearch(SetVar... vars) {
        return setVarSearch(new FailureBased<>(vars, 0, 2), new SetDomainMin(), true, vars);
    }

    /**
     * Assignment strategy which selects a variable according to <code>Failure length based</code>
     * variable ordering and assigns it to its lower bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote This is based on "Failure Based Variable Ordering Heuristics for Solving CSPs."
     * H. Li, M. Yin, and Z. Li, CP 2021.
     * <a href="https://dblp.org/rec/conf/cp/LiYL21">https://dblp.org/rec/conf/cp/LiYL21</a>
     */
    public static AbstractStrategy<SetVar> failureLengthBasedSearch(SetVar... vars) {
        return setVarSearch(new FailureBased<>(vars, 0, 4), new SetDomainMin(), true, vars);
    }

    // ************************************************************************************
    // GRAPHVAR STRATEGIES
    // ************************************************************************************

    /**
     * Generic strategy to branch on graph variables
     *
     * @param varS         Variable selection strategy
     * @param nodeOrEdgeS  Node or edge selection (defines if whenever a decision must be on nodes or edges)
     * @param nodeS        Node selector (defines which node to enforce/remove if decision is on nodes)
     * @param edgeS        Edge selector (defines which edge to enforce/remove if decision is on edges)
     * @param enforceFirst branching order true = enforce first; false = remove first
     * @param graphs       GraphVar array to branch on
     * @return a search strategy on GraphVar
     */
    public static GraphStrategy graphVarSearch(VariableSelector<GraphVar> varS, GraphNodeOrEdgeSelector nodeOrEdgeS,
                                               GraphNodeSelector nodeS, GraphEdgeSelector edgeS, boolean enforceFirst,
                                               GraphVar... graphs) {
        if (graphs == null || graphs.length == 0) {
            throw new IllegalArgumentException("The set of variables cannot be null or empty");
        }
        return new GraphStrategy(graphs, varS, nodeOrEdgeS, nodeS, edgeS, enforceFirst);
    }

    /**
     * Default graph var search.
     * <p>
     * Variable selection: input order.
     * Node or edges selection: nodes first then edges.
     * Node selection: lexicographic order.
     * Edge selection lexicographic order.
     * Enforce first.
     *
     * <br> node branching:
     * Let i be the first node such that
     * i in envelope(g) and i not in kernel(g).
     * The decision adds i to the kernel of g.
     * It is fails, then i is removed from the envelope of g.
     * <br>
     * edge branching:
     * <br> node branching:
     * Let (i,j) be the first edge such that
     * (i,j) in envelope(g) and (i,j) not in kernel(g).
     * The decision adds (i,j) to the kernel of g.
     * It is fails, then (i,j) is removed from the envelope of g
     *
     * @param graphs graph variables to branch on
     */
    public static GraphStrategy graphVarSearch(GraphVar... graphs) {
        return graphVarSearch(
                new InputOrder<>(graphs[0].getModel()),
                new GraphNodeThenEdges(),
                new GraphLexNode(),
                new GraphLexEdge(),
                true,
                graphs
        );
    }

    /**
     * Random graph var search.
     * <p>
     * Variable selection: random.
     * Node or edges selection: nodes first then edges.
     * Node selection: random.
     * Edge selection random.
     * Enforce first.
     *
     * @param seed   the seed for random selection
     * @param graphs graph variables to branch on
     * @return a randomized graph variables search strategy
     */
    public static GraphStrategy randomGraphVarSearch(long seed, GraphVar... graphs) {
        return graphVarSearch(
                new Random<>(seed),
                new GraphNodeThenEdges(),
                new GraphRandomNode(seed),
                new GraphRandomEdge(seed),
                true,
                graphs
        );
    }

    // ************************************************************************************
    // REALVAR STRATEGIES
    // ************************************************************************************

    /**
     * Generic strategy to branch on real variables, based on domain splitting. A real decision is
     * like:
     * <ul>
     * <li>left branch: X &le; v</li>
     * <li>right branch: X &ge; v + e</li>
     * </ul>
     * where 'e' is given by epsilon.
     * </p>
     *
     * @param varS      variable selection strategy
     * @param valS      strategy to select where to split domains
     * @param epsilon   gap for refutation
     * @param rvars     RealVar array to branch on
     * @param leftFirst select left range first
     * @return a strategy to instantiate reals
     * @throws IllegalArgumentException when the array of variables is either null or empty.
     */
    public static RealStrategy realVarSearch(VariableSelector<RealVar> varS, RealValueSelector valS,
                                             double epsilon, boolean leftFirst, RealVar... rvars) {
        if (rvars == null || rvars.length == 0) {
            throw new IllegalArgumentException("The array of variables cannot be null or empty");
        }
        return new RealStrategy(rvars, varS, valS, epsilon, leftFirst);
    }

    /**
     * strategy to branch on real variables by choosing sequentially the next variable domain to
     * split in two, wrt the middle value. A real decision is like:
     * <ul>
     * <li>left branch: X &le; v</li>
     * <li>right branch: X &ge; v + e</li>
     * </ul>
     * where 'e' is given by epsilon.
     * </p>
     *
     * @param epsilon gap for refutation
     * @param reals   variables to branch on
     * @return a strategy to instantiate real variables
     */
    public static RealStrategy realVarSearch(double epsilon, RealVar... reals) {
        return realVarSearch(new Cyclic<>(), new RealDomainMiddle(), epsilon, true, reals);
    }

    /**
     * Generic strategy to branch on real variables, based on domain splitting.
     * <p>
     * A real decision is like:
     * <ul>
     * <li>left branch: X &le; v</li>
     * <li>right branch: X &ge; v + epsilon</li>
     * </ul>
     * where epsilon is given or equal to the smallest precision among rvars divide by 10.
     * </p>
     *
     * @param varS      variable selection strategy
     * @param valS      strategy to select where to split domains
     * @param leftFirst select left range first
     * @param rvars     RealVar array to branch on
     * @return a strategy to instantiate reals
     */
    public static RealStrategy realVarSearch(VariableSelector<RealVar> varS, RealValueSelector valS,
                                             boolean leftFirst, RealVar... rvars) {
        return realVarSearch(varS, valS, Double.NaN, leftFirst, rvars);
    }

    /**
     * strategy to branch on real variables by choosing sequentially the next variable domain to
     * split in two, wrt the middle value.
     * <p>
     * A real decision is like:
     * <ul>
     * <li>left branch: X &le; v</li>
     * <li>right branch: X &ge; v + {@link Double#MIN_VALUE}</li>
     * </ul>
     * </p>
     *
     * @param reals variables to branch on
     * @return a strategy to instantiate real variables
     */
    public static RealStrategy realVarSearch(RealVar... reals) {
        return realVarSearch(new Cyclic<>(), new RealDomainMiddle(), true, reals);
    }

    // ************************************************************************************
    // INTVAR STRATEGIES
    // ************************************************************************************

    /**
     * Builds your own search strategy based on <b>binary</b> decisions.
     *
     * @param varSelector      defines how to select a variable to branch on.
     * @param valSelector      defines how to select a value in the domain of the selected variable
     * @param decisionOperator defines how to modify the domain of the selected variable with the
     *                         selected value
     * @param vars             variables to branch on
     * @return a custom search strategy
     * @throws IllegalArgumentException when the array of variables is either null or empty.
     */
    public static IntStrategy intVarSearch(VariableSelector<IntVar> varSelector,
                                           IntValueSelector valSelector,
                                           DecisionOperator<IntVar> decisionOperator,
                                           IntVar... vars) {
        if (vars == null || vars.length == 0) {
            throw new IllegalArgumentException("The array of variables cannot be null or empty");
        }
        return new IntStrategy(vars, varSelector, valSelector, decisionOperator);
    }

    /**
     * Builds your own assignment strategy based on <b>binary</b> decisions. Selects a variable X
     * and a value V to make the decision X = V. Note that value assignments are the public static
     * decision operators. Therefore, they are not mentioned in the search heuristic name.
     *
     * @param varSelector defines how to select a variable to branch on.
     * @param valSelector defines how to select a value in the domain of the selected variable
     * @param vars        variables to branch on
     * @return a custom search strategy
     */
    public static IntStrategy intVarSearch(VariableSelector<IntVar> varSelector,
                                           IntValueSelector valSelector,
                                           IntVar... vars) {
        return intVarSearch(varSelector, valSelector, DecisionOperatorFactory.makeIntEq(), vars);
    }

    /**
     * Builds a default search heuristics of integer variables Variable selection relies on {@link
     * #domOverWDegSearch(IntVar...)} Value selection relies on InDomainBest for optimization and
     * InDomainMin for satisfaction
     *
     * @param vars variables to branch on
     * @return a default search strategy
     */
    public static AbstractStrategy<IntVar> intVarSearch(IntVar... vars) {
        Model model = vars[0].getModel();
        IntValueSelector valueSelector;
        if (model.getResolutionPolicy() == ResolutionPolicy.SATISFACTION
                || !(model.getObjective() instanceof IntVar)) {
            valueSelector = new IntDomainMin();
        } else {
            valueSelector = new IntDomainBest();
            Solution solution;
            if (model.getSolver().defaultSolutionExists()) {
                solution = model.getSolver().defaultSolution(); // already attached
            } else {
                solution = new Solution(model, vars);
                model.getSolver().attach(solution);
            }
            valueSelector = new IntDomainLast(solution, valueSelector, null);
        }
        return intVarSearch(new DomOverWDeg<>(vars, 0), valueSelector, vars);
    }

    /**
     * Assignment strategy which selects a variable according to <code>DomOverWDeg</code> and assign
     * it to its lower bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote This is based on "Boosting Systematic Search by Weighting Constraints."
     * Boussemart et al. ECAI 2004.
     * <a href="https://dblp.org/rec/conf/ecai/BoussemartHLS04">https://dblp.org/rec/conf/ecai/BoussemartHLS04</a>
     */
    public static AbstractStrategy<IntVar> domOverWDegSearch(IntVar... vars) {
        return intVarSearch(new DomOverWDeg<>(vars, 0), new IntDomainMin(), vars);
    }

    /**
     * Assignment strategy which selects a variable according to <code>refined DomOverWDeg</code> and assign
     * it to its lower bound, where the weight incrementer is "ca.cd".
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote This is based on "Refining Constraint Weighting." Wattez et al. ICTAI 2019.
     * <a href="https://dblp.org/rec/conf/ictai/WattezLPT19">https://dblp.org/rec/conf/ictai/WattezLPT19</a>
     */
    public static AbstractStrategy<IntVar> domOverWDegRefSearch(IntVar... vars) {
        return intVarSearch(new DomOverWDegRef<>(vars, 0), new IntDomainMin(), vars);
    }

    /**
     * Create an Activity based search strategy.
     * <p>
     * <br/> Uses public static parameters
     * (GAMMA=0.999d, DELTA=0.2d, ALPHA=8, RESTART=1.1d, FORCE_SAMPLING=1)
     *
     * @param vars collection of variables
     * @return an Activity based search strategy.
     * @implNote This is based on "Activity-Based Search for Black-Box Constraint Programming Solvers."
     * Michel et al. CPAIOR 2012.
     * <a href="https://dblp.org/rec/conf/cpaior/MichelH12">https://dblp.org/rec/conf/cpaior/MichelH12</a>
     * @throws IllegalArgumentException when the array of variables is either null or empty.
     */
    public static AbstractStrategy<IntVar> activityBasedSearch(IntVar... vars) {
        if (vars == null || vars.length == 0) {
            throw new IllegalArgumentException("The array of variables cannot be null or empty");
        }
        return new ActivityBased(vars);
    }

    /**
     * Assignment strategy which selects a variable according to <code>Conflict History</code>
     * and assigns it to its lower bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote This is based on "Conflict history based search for constraint satisfaction problem."
     * Habet et al. SAC 2019.
     * <a href="https://dblp.org/rec/conf/sac/HabetT19">https://dblp.org/rec/conf/sac/HabetT19</a>
     */
    public static AbstractStrategy<IntVar> conflictHistorySearch(IntVar... vars) {
        return intVarSearch(new ConflictHistorySearch<>(vars, 0), new IntDomainMin(), vars);
    }

    /**
     * Assignment strategy which selects a variable according to <code>Failure rate based</code>
     * variable ordering and assigns it to its lower bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote This is based on "Failure Based Variable Ordering Heuristics for Solving CSPs."
     * H. Li, M. Yin, and Z. Li, CP 2021.
     * <a href="https://dblp.org/rec/conf/cp/LiYL21">https://dblp.org/rec/conf/cp/LiYL21</a>
     */
    public static AbstractStrategy<IntVar> failureRateBasedSearch(IntVar... vars) {
        return intVarSearch(new FailureBased<>(vars, 0, 2), new IntDomainMin(), vars);
    }

    /**
     * Assignment strategy which selects a variable according to <code>Failure length based</code>
     * variable ordering and assigns it to its lower bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote This is based on "Failure Based Variable Ordering Heuristics for Solving CSPs."
     * H. Li, M. Yin, and Z. Li, CP 2021.
     * <a href="https://dblp.org/rec/conf/cp/LiYL21">https://dblp.org/rec/conf/cp/LiYL21</a>
     */
    public static AbstractStrategy<IntVar> failureLengthBasedSearch(IntVar... vars) {
        return intVarSearch(new FailureBased<>(vars, 0, 4), new IntDomainMin(), vars);
    }

    /**
     * Randomly selects a variable and assigns it to a value randomly taken in - the domain in case
     * the variable has an enumerated domain - {LB,UB} (one of the two bounds) in case the domain is
     * bounded
     *
     * @param vars list of variables
     * @param seed a seed for random
     * @return assignment strategy
     */
    public static IntStrategy randomSearch(IntVar[] vars, long seed) {
        IntValueSelector value = new IntDomainRandom(seed);
        IntValueSelector bound = new IntDomainRandomBound(seed);
        IntValueSelector selector = var -> {
            if (var.hasEnumeratedDomain()) {
                return value.selectValue(var);
            } else {
                return bound.selectValue(var);
            }
        };
        return intVarSearch(new Random<>(seed), selector, vars);
    }

    /**
     * Defines a branching strategy over the objective variable Note that it is only activated after
     * a first solution. This should be completed with another strategy with a larger scope.
     *
     * @param objective objective variable
     * @param optPolicy policy to adopt for the optimization process
     * @return a assignment strategy
     */
    public static AbstractStrategy<IntVar> objectiveStrategy(IntVar objective,
                                                             OptimizationPolicy optPolicy) {
        return new ObjectiveStrategy(objective, optPolicy);
    }

    // ************************************************************************************
    // SOME EXAMPLES OF STRATEGIES YOU CAN BUILD
    // ************************************************************************************

    /**
     * Assigns the first non-instantiated variable to its lower bound.
     *
     * @param vars list of variables
     * @return int strategy based on value assignments
     */
    public static IntStrategy inputOrderLBSearch(IntVar... vars) {
        return intVarSearch(new InputOrder<>(vars[0].getModel()), new IntDomainMin(), vars);
    }

    /**
     * Assigns the first non-instantiated variable to its upper bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     */
    public static IntStrategy inputOrderUBSearch(IntVar... vars) {
        return intVarSearch(new InputOrder<>(vars[0].getModel()), new IntDomainMax(), vars);
    }

    /**
     * Assigns the non-instantiated variable of the smallest domain size to its lower bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     */
    public static IntStrategy minDomLBSearch(IntVar... vars) {
        return intVarSearch(new FirstFail(vars[0].getModel()), new IntDomainMin(), vars);
    }

    /**
     * Assigns the non-instantiated variable of the smallest domain size to its upper bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     */
    public static IntStrategy minDomUBSearch(IntVar... vars) {
        return intVarSearch(new FirstFail(vars[0].getModel()), new IntDomainMax(), vars);
    }

    // ************************************************************************************
    // DEFAULT STRATEGY (COMPLETE)
    // ************************************************************************************

    /**
     * Creates a default search strategy for the given model. This heuristic is complete (handles
     * IntVar, BoolVar, SetVar, GraphVar, and RealVar)
     *
     * @param model a model requiring a default search strategy
     */
    public static AbstractStrategy defaultSearch(Model model) {
        Solver r = model.getSolver();

        // 1. retrieve variables, keeping the declaration order, and put them in four groups:
        List<IntVar> livars = new ArrayList<>(); // integer and boolean variables
        List<SetVar> lsvars = new ArrayList<>(); // set variables
        List<GraphVar<?>> lgvars = new ArrayList<>(); // graph variables
        List<RealVar> lrvars = new ArrayList<>();// real variables.
        Variable[] variables = model.getVars();
        Variable objective = null;
        for (Variable var : variables) {
            int type = var.getTypeAndKind();
            if ((type & (Variable.CSTE)) == 0) {
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
        }

        // 2. extract the objective variable if any (to avoid branching on it)
        if (r.getObjectiveManager().isOptimization()) {
            objective = r.getObjectiveManager().getObjective();
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
        ArrayList<AbstractStrategy> strats = new ArrayList<>();
        if (livars.size() > 0) {
            strats.add(intVarSearch(livars.toArray(new IntVar[0])));
        }
        if (lsvars.size() > 0) {
            strats.add(setVarSearch(lsvars.toArray(new SetVar[0])));
        }
        if (lgvars.size() > 0) {
            strats.add(graphVarSearch(lgvars.toArray(new GraphVar[0])));
        }
        if (lrvars.size() > 0) {
            strats.add(realVarSearch(lrvars.toArray(new RealVar[0])));
        }

        // 4. lexico LB/UB branching for the objective variable
        if (objective != null) {
            boolean max = r.getObjectiveManager().getPolicy() == ResolutionPolicy.MAXIMIZE;
            if ((objective.getTypeAndKind() & Variable.REAL) != 0) {
                strats.add(
                        realVarSearch(new Cyclic<>(), max ? new RealDomainMax() : new RealDomainMin(),
                                !max, (RealVar) objective));
            } else {
                strats.add(
                        max ? minDomUBSearch((IntVar) objective) : minDomLBSearch((IntVar) objective));
            }
        }

        // 5. avoid null pointers in case all variables are instantiated
        if (strats.isEmpty()) {
            strats.add(minDomLBSearch(model.boolVar(true)));
        }

        // 6. add last conflict
        return lastConflict(sequencer(strats.toArray(new AbstractStrategy[0])));
    }

    /**
     * <p>
     * Create a strategy which lets Ibex terminates the solving process for the CSP,
     * <b>once all integer variables have been instantiated</b>.
     * </p><p>
     * Note that if the system is not constrained enough, there can be an infinite number of
     * solutions.
     * </p><p>
     * For example, solving the function <br/> x,y in [0.0,1.0] with <br/> x + y = 1.0 <br/> will
     * return x,y in [0.0,1.0] and not a single solution.
     * </p><p>
     * If one wants a unique solution, calling {@link #realVarSearch(RealVar...)} should be
     * considered.
     * </p>
     *
     * @param model declaring model
     * @return a strategy that lets Ibex terminates the solving process.
     */
    public static AbstractStrategy<Variable> ibexSolving(Model model) {
        //noinspection unchecked
        return new AbstractStrategy(model.getVars()) {
            final IbexDecision dec = new IbexDecision(model);

            @Override
            public Decision<Variable> getDecision() {
                if (dec.inUse()) {
                    return null;
                } else {
                    return dec;
                }
            }
        };
    }

    /**
     * Enum for commonly used variable selectors.
     *
     * <p>To declare a variable selector to be part of a search strategy,
     * use the following code:
     * <pre>
     *     {@code
     *     AbstractStrategy<IntVar> strat = Search.VarH.CHS.make(solver, vars, Search.VarH.MIN, true);
     *     solver.setSearch(strat);
     * </pre>
     */
    public enum VarH {
        /**
         * To select variables according Activity-based Search.
         * {@code valueSelector} parameter is ignored.
         *
         * @see ActivityBased
         */
        ABS {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                return ACTIVITY.make(solver, vars, Search.ValH.DEFAULT, flushThs, last);
            }
        },
        /**
         * To select variables according to {@link #ABS}
         * Values can be selected with another heuristic.
         *
         * @see ActivityBased
         */
        ACTIVITY {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                Model model = solver.getModel();
                return new ActivityBased(model,
                        vars,
                        valueSelector == Search.ValH.DEFAULT ? null : valueSelector.make(solver, last),
                        0.999d,
                        0.2d,
                        8,
                        1,
                        model.getSeed());
            }
        },
        /**
         * To select variables according to Conflict History-based Search.
         *
         * @see ConflictHistorySearch
         */
        CHS {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                return new IntStrategy(vars,
                        new ConflictHistorySearch<>(vars, solver.getModel().getSeed(), flushThs),
                        valueSelector.make(solver, last));
            }
        },
        /**
         * To select variables according to the size of their current domain.
         *
         * @see FirstFail
         */
        DOM {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                return Search.intVarSearch(
                        new FirstFail(solver.getModel()),
                        valueSelector.make(solver, last),
                        vars);
            }
        },
        /**
         * To select variables to constraint weighting.
         *
         * @see DomOverWDeg
         */
        DOMWDEG {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                return new IntStrategy(vars,
                        new DomOverWDeg<>(vars, solver.getModel().getSeed(), flushThs),
                        valueSelector.make(solver, last));
            }
        },
        /**
         * To select variables to refined constraint weighting.
         *
         * @see DomOverWDegRef
         */
        DOMWDEGR {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                return new IntStrategy(vars,
                        new DomOverWDegRef<>(vars, solver.getModel().getSeed(), flushThs),
                        valueSelector.make(solver, last));
            }
        },
        /**
         * To select {@link Search#defaultSearch(Model)}
         */
        DEFAULT {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                //noinspection unchecked
                return defaultSearch(solver.getModel());
            }
        },
        /**
         * To select variables according to Failure rate based variable ordering with decaying factor.
         */
        FRBA {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                return new IntStrategy(vars,
                        new FailureBased<>(vars, solver.getModel().getSeed(), 2),
                        valueSelector.make(solver, last));
            }
        },
        /**
         * To select variables according to Failure length based variable ordering with decaying factor.
         */
        FLBA {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                return new IntStrategy(vars,
                        new FailureBased<>(vars, solver.getModel().getSeed(), 4),
                        valueSelector.make(solver, last));
            }
        },
        /**
         * To select variables according to Impact-based Search.
         * {@code valueSelector} parameter is ignored.
         *
         * @see ImpactBased
         */
        IBS {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                return IMPACT.make(solver, vars, Search.ValH.DEFAULT, flushThs, last);
            }
        },
        /**
         * To select variables according to Impact-based Search.
         * Values can be selected with another heuristic.
         *
         * @see ImpactBased
         */
        IMPACT {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                return new ImpactBased(vars,
                        valueSelector == Search.ValH.DEFAULT ? null : valueSelector.make(solver, last),
                        2,
                        512,
                        2048,
                        solver.getModel().getSeed(),
                        false);
            }
        },
        /**
         * To select variables according to their order in {@code vars}.
         */
        INPUT {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                return Search.intVarSearch(
                        new InputOrder<>(solver.getModel()),
                        valueSelector.make(solver, last),
                        vars);
            }
        },
        /**
         * To select variables randomly.
         */
        RAND {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                return Search.intVarSearch(
                        new Random<>(solver.getModel().getSeed()),
                        valueSelector.make(solver, last),
                        vars);
            }
        },
        MAB_CHS_DWDEG_STATIC {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                //noinspection unchecked
                return new MultiArmedBanditSequencer<IntVar>(
                        new AbstractStrategy[]{
                                CHS.make(solver, vars, valueSelector, flushThs, last),
                                DOMWDEG.make(solver, vars, valueSelector, flushThs, last)
                        },
                        new Static(new double[]{.7, .3}, new java.util.Random(solver.getModel().getSeed())),
                        (a, t) -> 0.d
                );
            }
        },
        MAB_CHS_DWDEG_MOSS {
            @Override
            public AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last) {
                final long[] pat = {0, 0};
                final HashSet<IntVar> selected = new HashSet<>();
                ToDoubleBiFunction<Integer, Integer> reward = (a, t) -> {
                    double r = Math.log(solver.getNodeCount() - pat[0]) /
                            Math.log(VariableUtils.searchSpaceSize(selected.iterator()))
                            //+ solver.getSolutionCount() - pat[1]
                            ;
                    pat[0] = solver.getNodeCount();
                    pat[1] = solver.getSolutionCount();
                    selected.clear();
                    return r;
                };
                solver.plugMonitor(new IMonitorOpenNode() {
                    @Override
                    public void afterOpenNode() {
                        selected.add((IntVar) solver.getDecisionPath().getLastDecision().getDecisionVariable());
                    }
                });
                //noinspection unchecked
                return new MultiArmedBanditSequencer<IntVar>(
                        new AbstractStrategy[]{
                                CHS.make(solver, vars, valueSelector, flushThs, last),
                                DOMWDEG.make(solver, vars, valueSelector, flushThs, last)
                        },
                        new MOSS(2),
                        reward
                );
            }
        };

        /**
         * Declare the search strategy based on parameters
         *
         * @param solver        target solver
         * @param vars          array of integer variables
         * @param valueSelector the value selector enum
         * @param flushThs      flush threshold, when reached, it flushes scores
         * @param last          set to {@code true} to use {@link IntDomainLast} meta value strategy.
         * @return a search strategy on {@code IntVar[]}
         */
        public abstract AbstractStrategy<IntVar> make(Solver solver, IntVar[] vars, Search.ValH valueSelector, int flushThs, boolean last);
    }

    /**
     * Enum for commonly used value selectors.
     *
     * <p>To declare a value selector to be part of a search strategy,
     * use the following code:
     * <pre>
     *     {@code
     *     AbstractStrategy<IntVar> strat = Search.VarH.CHS.declare(solver, vars, Search.VarH.MIN, true);
     *     solver.setSearch(strat);
     * </pre>
     */
    public enum ValH {
        /**
         * To select the best value according to the best objective bound.
         *
         * @see IntDomainBest
         */
        BEST {
            @Override
            public IntValueSelector make(Solver solver, boolean last) {
                if (solver.getModel().getResolutionPolicy() == ResolutionPolicy.SATISFACTION) {
                    return MIN.make(solver, last);
                }
                return last(solver, new IntDomainBest(), last);
            }
        },
        /**
         * To select the best value according to the best objective bound when looking for
         * the first solution, then return the lowest bound.
         *
         * @see IntDomainBest
         * @see IntDomainMin
         */
        BMIN {
            @Override
            public IntValueSelector make(Solver solver, boolean last) {
                if (solver.getModel().getResolutionPolicy() == ResolutionPolicy.SATISFACTION) {
                    return MIN.make(solver, last);
                }


                return last(solver,
                        new IntValueSelector() {
                            IntValueSelector sel = new IntDomainBest();

                            @Override
                            public int selectValue(IntVar var) {
                                if (var.getModel().getSolver().getSolutionCount() > 0) {
                                    sel = new IntDomainMin();
                                }
                                return sel.selectValue(var);
                            }
                        }, last);
            }
        },
        /**
         * To select the best value according to the best objective bound.
         *
         * @see IntDomainBest
         */
        BLAST {
            @Override
            public IntValueSelector make(Solver solver, boolean last) {
                if (solver.getModel().getResolutionPolicy() == ResolutionPolicy.SATISFACTION) {
                    return MIN.make(solver, last);
                }
                Solution lastSol = solver.defaultSolution();
                return last(solver, new IntDomainBest((v, i) -> lastSol.exists() && lastSol.getIntVal(v) == i), last);
            }
        },
        /**
         * Return {@link #BEST}.
         */
        DEFAULT {
            @Override
            public IntValueSelector make(Solver solver, boolean last) {
                return BEST.make(solver, last);
            }
        },
        /**
         * To select the maximal value in the current domain of the selected variable.
         *
         * @see IntDomainMax
         */
        MAX {
            @Override
            public IntValueSelector make(Solver solver, boolean last) {
                return last(solver, new IntDomainMax(), last);
            }
        },
        /**
         * To select the median value in the current domain of the selected variable.
         *
         * @see IntDomainMedian
         */
        MED {
            @Override
            public IntValueSelector make(Solver solver, boolean last) {
                return last(solver, new IntDomainMedian(), last);
            }
        },
        /**
         * To select the middle value in the current domain of the selected variable with floor rounding.
         *
         * @see IntDomainMiddle
         */
        MIDFLOOR {
            @Override
            public IntValueSelector make(Solver solver, boolean last) {
                return last(solver, new IntDomainMiddle(true), last);
            }
        },
        /**
         * To select the middle value in the current domain of the selected variable with ceil rouding.
         *
         * @see IntDomainMiddle
         */
        MIDCEIL {
            @Override
            public IntValueSelector make(Solver solver, boolean last) {
                return last(solver, new IntDomainMiddle(false), last);
            }
        },
        /**
         * To select the minimal value in the current domain of the selected variable.
         *
         * @see IntDomainMin
         */
        MIN {
            @Override
            public IntValueSelector make(Solver solver, boolean last) {
                return last(solver, new IntDomainMin(), last);
            }
        },
        /**
         * To select values randomly.
         *
         * @see IntDomainRandom
         */
        RAND {
            @Override
            public IntValueSelector make(Solver solver, boolean last) {
                return last(solver, new IntDomainRandom(solver.getModel().getSeed()), last);
            }
        };

        /**
         * Build the value selector
         *
         * @param solver solver to use in
         * @param last   set to {@code true} to use meta value selector based on last solution found.
         * @return a value selector
         */
        public abstract IntValueSelector make(Solver solver, boolean last);

        /**
         * If {@code last} is set to {@code true}, add {@link IntDomainLast} meta value selector.
         *
         * @param solver   the solver to record solutions from
         * @param selector the defined value selector
         * @param last     use meta value selector.
         * @return a value selector
         */
        IntValueSelector last(Solver solver, IntValueSelector selector, boolean last) {
            if (last) {
                // default
                Model model = solver.getModel();
                if (model.getResolutionPolicy() == ResolutionPolicy.SATISFACTION) {
                    return selector;
                }
                return new IntDomainLast(model.getSolver().defaultSolution(), selector, null);
            } else {
                return selector;
            }
        }
    }

    /**
     * Enum for commonly used value restarting policies.
     *
     * <p>To declare a value selector to be part of a search strategy,
     * use the following code:
     * <pre>
     *     {@code
     *     Search.Restarts.LUBY.declare(solver, 50, 5000);
     * </pre>
     */
    public enum Restarts {
        /**
         * Define no restart strategy.
         *
         * @apiNote Does not remove or erase previously defined restart policy
         */
        NONE {
            @Override
            public void declare(Solver solver, int cutoff, double factor, int offset) {
                // nothing to do
            }
        },
        /**
         * To use a monotonic restart strategy.
         * <p>This policy will restart every {@code cutoff} failures, until {@code offset} restarts occur.
         *
         * @implNote {@code factor} is ignored.
         * @see MonotonicCutoff
         */
        MONOTONIC {
            @Override
            public void declare(Solver solver, int cutoff, double factor, int offset) {
                solver.setRestarts(
                        count -> solver.getFailCount() >= count,
                        new MonotonicCutoff(cutoff),
                        offset
                );
                solver.setNoGoodRecordingFromRestarts();
            }
        },
        /**
         * To use a linear restart strategy.
         *
         * @implNote {@code factor} is ignored.
         * @see LinearCutoff
         */
        LINEAR {
            @Override
            public void declare(Solver solver, int cutoff, double factor, int offset) {
                solver.setRestarts(
                        count -> solver.getFailCount() >= count,
                        new LinearCutoff(cutoff),
                        offset
                );
                solver.setNoGoodRecordingFromRestarts();
            }
        },
        /**
         * To use a Luby restart strategy.
         *
         * @implNote {@code factor} is ignored.
         * @see LubyCutoff
         */
        LUBY {
            @Override
            public void declare(Solver solver, int cutoff, double factor, int offset) {
                solver.setRestarts(
                        count -> solver.getFailCount() >= count,
                        new LubyCutoff(cutoff),
                        offset
                );
                solver.setNoGoodRecordingFromRestarts();
            }
        },
        /**
         * To use a geometric restart strategy.
         *
         * @see GeometricalCutoff
         */
        GEOMETRIC {
            @Override
            public void declare(Solver solver, int cutoff, double factor, int offset) {
                solver.setRestarts(
                        count -> solver.getFailCount() >= count,
                        new GeometricalCutoff(cutoff, factor),
                        offset
                );
                solver.setNoGoodRecordingFromRestarts();
            }
        };

        public abstract void declare(Solver solver, int cutoff, double factor, int offset);
    }
}