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
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.objective.ObjectiveStrategy;
import org.chocosolver.solver.objective.OptimizationPolicy;
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
     * @param ivars        variables to generate partial assignment from
     * @param maxSolNum    maximum number of solutions to store
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
     * @throws IllegalArgumentException when the array of variables is either null or empty.
     * @implNote This is based on "Activity-Based Search for Black-Box Constraint Programming Solvers."
     * Michel et al. CPAIOR 2012.
     * <a href="https://dblp.org/rec/conf/cpaior/MichelH12">https://dblp.org/rec/conf/cpaior/MichelH12</a>
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
     * Assignment strategy which selects a variable according to <code>PickOnDom</code> and assign.
     * This version is based on the variables involved in the propagation.
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote Based on "Guiding Backtrack Search by Tracking Variables During Constraint Propagation", C. Lecoutre et al., CP 2023.
     * <br/>[DOI]:<a href="https://drops.dagstuhl.de/entities/document/10.4230/LIPIcs.CP.2023.9">10.4230/LIPIcs.CP.2023.9</a>
     */
    public static AbstractStrategy<IntVar> pickOnDom(IntVar... vars) {
        return intVarSearch(new PickOnDom<>(vars), new IntDomainMin(), vars);
    }

    /**
     * Assignment strategy which selects a variable according to <code>PickOnDom</code> and assign.
     * This version is based on the constraints involved in the propagation.
     *
     * @param vars list of variables
     * @return assignment strategy
     * @implNote Based on "Guiding Backtrack Search by Tracking Variables During Constraint Propagation", C. Lecoutre et al., CP 2023.
     * <br/>[DOI]:<a href="https://drops.dagstuhl.de/entities/document/10.4230/LIPIcs.CP.2023.9">10.4230/LIPIcs.CP.2023.9</a>
     */
    public static AbstractStrategy<IntVar> pickOnFil(IntVar... vars) {
        return intVarSearch(new PickOnDom<>(vars), new IntDomainMin(), vars);
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
     * Set the default search strategy for the given model. This heuristic is complete (handles
     * IntVar, BoolVar, SetVar, GraphVar, and RealVar)
     *
     * @param model a model requiring a default search strategy
     */
    public static void defaultSearch(Model model) {
        BlackBoxConfigurator.init().make(model);
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
}