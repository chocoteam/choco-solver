/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.objective.ObjectiveStrategy;
import org.chocosolver.solver.objective.OptimizationPolicy;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IbexDecision;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.ActivityBased;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.GeneralizedMinDomVarSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.ConflictOrderingSearch;
import org.chocosolver.solver.search.strategy.strategy.GreedyBranching;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.search.strategy.strategy.LastConflict;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.search.strategy.strategy.SetStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

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
     *
     * BEWARE: ONLY FOR INTEGERS (lets the former search work for other variable types)
     *
     * @param formerSearch default search to branch on variables (defines the variable selector and
     * the value selector when this does not hold)
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
        return new LastConflict(formerSearch.getVariables()[0].getModel(), formerSearch, k);
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
     * Make the input search strategy greedy, that is, decisions can be applied but not refuted.
     *
     * @param search a search heuristic building branching decisions
     * @return a greedy form of search
     */
    public static AbstractStrategy greedySearch(AbstractStrategy search) {
        return new GreedyBranching(search);
    }

    /**
     * Apply sequentialy enumeration strategies. Strategies are considered in input order. When
     * strategy <i>i</i> returns null (all variables are instantiated) the <i>i+1</i> ones is
     * activated.
     *
     * @param searches ordered set of enumeration strategies
     */
    public static AbstractStrategy sequencer(AbstractStrategy... searches) {
        return new StrategiesSequencer(searches);
    }

    // ************************************************************************************
    // SETVAR STRATEGIES
    // ************************************************************************************

    /**
     * Generic strategy to branch on set variables
     *
     * @param varS variable selection strategy
     * @param valS integer  selection strategy
     * @param enforceFirst branching order true = enforce first; false = remove first
     * @param sets SetVar array to branch on
     * @return a strategy to instantiate sets
     */
    public static SetStrategy setVarSearch(VariableSelector<SetVar> varS, SetValueSelector valS,
        boolean enforceFirst, SetVar... sets) {
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
        return setVarSearch(new GeneralizedMinDomVarSelector(), new SetDomainMin(), true, sets);
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
     * @param varS variable selection strategy
     * @param valS strategy to select where to split domains
     * @param epsilon gap for refutation
     * @param rvars RealVar array to branch on
     * @param leftFirst select left range first
     * @return a strategy to instantiate reals
     */
    public static RealStrategy realVarSearch(VariableSelector<RealVar> varS, RealValueSelector valS,
        double epsilon, boolean leftFirst, RealVar... rvars) {
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
     * @param reals variables to branch on
     * @return a strategy to instantiate real variables
     */
    public static RealStrategy realVarSearch(double epsilon, RealVar... reals) {
        return realVarSearch(new Cyclic<>(), new RealDomainMiddle(), epsilon, true, reals);
    }

    /**
     * Generic strategy to branch on real variables, based on domain splitting.
     *
     * A real decision is like:
     * <ul>
     * <li>left branch: X &le; v</li>
     * <li>right branch: X &ge; v + epsilon</li>
     * </ul>
     * where epsilon is given or equal to the smallest precision among rvars divide by 10.
     * </p>
     *
     * @param varS variable selection strategy
     * @param valS strategy to select where to split domains
     * @param leftFirst select left range first
     * @param rvars RealVar array to branch on
     * @return a strategy to instantiate reals
     */
    public static RealStrategy realVarSearch(VariableSelector<RealVar> varS, RealValueSelector valS,
        boolean leftFirst, RealVar... rvars) {
        return realVarSearch(varS, valS, Double.NaN, leftFirst, rvars);
    }

    /**
     * strategy to branch on real variables by choosing sequentially the next variable domain to
     * split in two, wrt the middle value.
     *
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
     * @param varSelector defines how to select a variable to branch on.
     * @param valSelector defines how to select a value in the domain of the selected variable
     * @param decisionOperator defines how to modify the domain of the selected variable with the
     * selected value
     * @param vars variables to branch on
     * @return a custom search strategy
     */
    public static IntStrategy intVarSearch(VariableSelector<IntVar> varSelector,
        IntValueSelector valSelector,
        DecisionOperator<IntVar> decisionOperator,
        IntVar... vars) {
        return new IntStrategy(vars, varSelector, valSelector, decisionOperator);
    }

    /**
     * Builds your own assignment strategy based on <b>binary</b> decisions. Selects a variable X
     * and a value V to make the decision X = V. Note that value assignments are the public static
     * decision operators. Therefore, they are not mentioned in the search heuristic name.
     *
     * @param varSelector defines how to select a variable to branch on.
     * @param valSelector defines how to select a value in the domain of the selected variable
     * @param vars variables to branch on
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
            Solution lastSolution = new Solution(model, vars);
            model.getSolver().attach(lastSolution);
            valueSelector = new IntDomainLast(lastSolution, valueSelector, null);
        }
        return new DomOverWDeg(vars, 0, valueSelector);
    }

    /**
     * Assignment strategy which selects a variable according to <code>DomOverWDeg</code> and assign
     * it to its lower bound
     *
     * @param vars list of variables
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> domOverWDegSearch(IntVar... vars) {
        return new DomOverWDeg(vars, 0, new IntDomainMin());
    }

    /**
     * Create an Activity based search strategy.
     * <p>
     * <b>"Activity-Based Search for Black-Box Constraint Propagramming Solver"<b/>,
     * Laurent Michel and Pascal Van Hentenryck, CPAIOR12. <br/> Uses public static parameters
     * (GAMMA=0.999d, DELTA=0.2d, ALPHA=8, RESTART=1.1d, FORCE_SAMPLING=1)
     *
     * @param vars collection of variables
     * @return an Activity based search strategy.
     */
    public static AbstractStrategy<IntVar> activityBasedSearch(IntVar... vars) {
        return new ActivityBased(vars);
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
     * Assigns the non-instantiated variable of smallest domain size to its lower bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     */
    public static IntStrategy minDomLBSearch(IntVar... vars) {
        return intVarSearch(new FirstFail(vars[0].getModel()), new IntDomainMin(), vars);
    }

    /**
     * Assigns the non-instantiated variable of smallest domain size to its upper bound.
     *
     * @param vars list of variables
     * @return assignment strategy
     */
    public static IntStrategy minDomUBSearch(IntVar... vars) {
        return intVarSearch(new FirstFail(vars[0].getModel()), new IntDomainMax(), vars);
    }

    /**
     *
     * @param vars
     * @param selected
     * @param initial
     * @param strategy
     * @return
     */
    public static AbstractStrategy<IntVar> intNeighbourhoodSearch(IntVar[] vars, boolean[] selected, int[] initial, IntStrategy strategy) {
        List<Integer> initialValues = new LinkedList<>();
        for(int i = 0; i < vars.length; i++){
            if(selected[i]){
                initialValues.add(i, initial[i]);
            }
        }
        IntNeighbourhood valueSelector = new IntNeighbourhood(strategy.getValSelector(), initialValues);
        VariableSelector<IntVar>  variableSelector = strategy.getVarSelector();
        return intVarSearch(variableSelector, valueSelector, vars);
    }


    // ************************************************************************************
    // DEFAULT STRATEGY (COMPLETE)
    // ************************************************************************************

    /**
     * Creates a default search strategy for the given model. This heuristic is complete (handles
     * IntVar, BoolVar, SetVar and RealVar)
     *
     * @param model a model requiring a default search strategy
     */
    public static AbstractStrategy defaultSearch(Model model) {
        Solver r = model.getSolver();

        // 1. retrieve variables, keeping the declaration order, and put them in four groups:
        List<IntVar> livars = new ArrayList<>(); // integer and boolean variables
        List<SetVar> lsvars = new ArrayList<>(); // set variables
        List<RealVar> lrvars = new ArrayList<>();// real variables.
        Variable[] variables = model.getVars();
        Variable objective = null;
        for (Variable var : variables) {
            int type = var.getTypeAndKind();
            if ((type & Variable.CSTE) == 0) {
                int kind = type & Variable.KIND;
                switch (kind) {
                    case Variable.BOOL:
                    case Variable.INT:
                        livars.add((IntVar) var);
                        break;
                    case Variable.SET:
                        lsvars.add((SetVar) var);
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
    public static AbstractStrategy ibexSolving(Model model) {
        return new AbstractStrategy<Variable>(model.getVars()) {
            IbexDecision dec = new IbexDecision(model);

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