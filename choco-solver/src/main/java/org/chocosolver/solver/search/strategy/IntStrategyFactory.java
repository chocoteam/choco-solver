/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.strategy;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.search.strategy.strategy.*;
import org.chocosolver.solver.variables.IntVar;

/**
 * Strategies, Variable selectors and Value selectors factory.
 * Just there to simplify strategies creation.
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 5 juil. 2010
 */
public class IntStrategyFactory {

    IntStrategyFactory() {
    }

    // ************************************************************************************
    // VARIABLE SELECTORS (non-exhaustive list)
    // ************************************************************************************

    /**
     * Selects the first non-instantiated variable, to branch on it.
     *
     * @return a variable selector
     */
    public static VariableSelector<IntVar> lexico_var_selector() {
        return new InputOrder<>();
    }

    /**
     * Selects randomly a non-instantiated variable to branch on
     *
     * @param SEED random seed
     * @return a variable selector
     */
    public static VariableSelector<IntVar> random_var_selector(long SEED) {
        return new Random<>(SEED);
    }

    /**
     * Selects the non-instantiated variable of smallest domain, to branch on it.
     * This heuristic is sometimes called FirstFail.
     *
     * @return a variable selector
     */
    public static VariableSelector<IntVar> minDomainSize_var_selector() {
        return new FirstFail();
    }

    /**
     * Selects the non-instantiated variable of largest domain, to branch on it.
     * This heuristic is sometimes called AntiFirstFail.
     *
     * @return a variable selector
     */
    public static VariableSelector<IntVar> maxDomainSize_var_selector() {
        return new AntiFirstFail();
    }

    /**
     * Selects the non-instantiated variable with the largest difference between the two smallest values in its domain, to branch on it.
     *
     * @return a variable selector
     */
    public static VariableSelector<IntVar> maxRegret_var_selector() {
        return new MaxRegret();
    }

    // ************************************************************************************
    // VALUE SELECTORS
    // ************************************************************************************

    /**
     * Selects the variable lower bound
     *
     * @return a value selector
     */
    public static IntValueSelector min_value_selector() {
        return new IntDomainMin();
    }

    /**
     * Selects a value at the middle between the variable lower and upper bounds
     * <p/>
     * BEWARE: this should not be used within assignments and/or value removals if variables
     * have a bounded domain.
     *
     * @return a value selector
     */
    public static IntValueSelector mid_value_selector() {
        return new IntDomainMiddle();
    }

    /**
     * Selects the variable upper bound
     *
     * @return a value selector
     */
    public static IntValueSelector max_value_selector() {
        return new IntDomainMax();
    }

    /**
     * Selects randomly either the lower bound or the upper bound of the variable
     * Takes an arbitrary value in {LB,UB}
     *
     * @return a value selector
     */
    public static IntValueSelector randomBound_value_selector(long SEED) {
        return new IntDomainRandomBound(SEED);
    }

    /**
     * Selects randomly a value in the variable domain.
     * Takes an arbitrary value in [LB,UB]
     * <p/>
     * BEWARE: this should not be used within assignments and/or value removals if variables
     * have a bounded domain.
     *
     * @return a value selector
     */
    public static IntValueSelector random_value_selector(long SEED) {
        return new IntDomainRandom(SEED);
    }

    // ************************************************************************************
    // OPERATORS
    // ************************************************************************************

    /**
     * Assign the selected variable to the selected value
     * e.g. X = 42
     * If this decision fails, it is automatically negated (i.e. X != 42)
     *
     * @return a decision operator
     */
    public static DecisionOperator<IntVar> assign() {
        return DecisionOperator.int_eq;
    }

    /**
     * Remove the selected value from the selected variable domain
     * e.g. X != 42
     * If this decision fails, it is automatically negated (i.e. X = 42)
     *
     * @return a decision operator
     */
    public static DecisionOperator<IntVar> remove() {
        return DecisionOperator.int_neq;
    }

    /**
     * Split the domain of the selected variable at the selected value, by updating the upper bound.
     * e.g. X <= 42
     * If this decision fails, it is automatically negated (i.e. X > 42)
     *
     * @return a decision operator
     */
    public static DecisionOperator<IntVar> split() {
        return DecisionOperator.int_split;
    }

    /**
     * Split the domain of the selected variable at the selected value, by updating the lower bound.
     * e.g. X >= 42
     * If this decision fails, it is automatically negated (i.e. X < 42)
     *
     * @return a decision operator
     */
    public static DecisionOperator<IntVar> reverse_split() {
        return DecisionOperator.int_reverse_split;
    }

    // ************************************************************************************
    // CUSTOM STRATEGIES
    // ************************************************************************************

    /**
     * Builds your own search strategy
     *
     * @param VAR_SELECTOR defines how to select a variable to branch on.
     * @param VAL_SELECTOR defines how to select a value in the domain of the selected variable
     * @param DEC_OPERATOR defines how to modify the domain of the selected variable with the selected value
     * @param VARS         variables to branch on
     * @return a custom search strategy
     */
    public static IntStrategy custom(VariableSelector<IntVar> VAR_SELECTOR,
                                                  IntValueSelector VAL_SELECTOR,
                                                  DecisionOperator<IntVar> DEC_OPERATOR,
                                                  IntVar... VARS) {
        return new IntStrategy(VARS, VAR_SELECTOR, VAL_SELECTOR, DEC_OPERATOR);
    }

    /**
     * Builds your own assignment strategy :
     * Selects a variable X and a value V to make the decision X = V.
     * Note that value assignments are the default decision operators.
     * Therefore, they are not mentioned in the search heuristic name.
     *
     * @param VAR_SELECTOR defines how to select a variable to branch on.
     * @param VAL_SELECTOR defines how to select a value in the domain of the selected variable
     * @param VARS         variables to branch on
     * @return a custom search strategy
     */
    public static IntStrategy custom(VariableSelector<IntVar> VAR_SELECTOR,
                                                  IntValueSelector VAL_SELECTOR,
                                                  IntVar... VARS) {
        return custom(VAR_SELECTOR, VAL_SELECTOR, assign(), VARS);
    }

    // ************************************************************************************
    // SOME EXAMPLES OF STRATEGIES YOU CAN BUILD
    // ************************************************************************************

    /**
     * Assigns the first non-instantiated variable to its lower bound.
     *
     * @param VARS list of variables
     * @return int strategy based on value assignments
     */
    public static IntStrategy lexico_LB(IntVar... VARS) {
        return custom(lexico_var_selector(), min_value_selector(), VARS);
    }

    /**
     * Removes the lower bound value from the domain of the first non-instantiated variable
     *
     * @param VARS list of variables
     * @return int strategy based on value removals
     */
    public static IntStrategy lexico_Neq_LB(IntVar... VARS) {
        return custom(lexico_var_selector(), min_value_selector(), remove(), VARS);
    }

    /**
     * Splits the domain of the first non-instantiated variable.
     *
     * @param VARS list of variables
     * @return int strategy based on domain splits
     */
    public static IntStrategy lexico_Split(IntVar... VARS) {
        return custom(lexico_var_selector(), mid_value_selector(), split(), VARS);
    }

    /**
     * Assigns the first non-instantiated variable to its upper bound.
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static IntStrategy lexico_UB(IntVar... VARS) {
        return custom(lexico_var_selector(), max_value_selector(), VARS);
    }

    /**
     * Assigns the non-instantiated variable of smallest domain size to its lower bound.
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static IntStrategy minDom_LB(IntVar... VARS) {
        return custom(minDomainSize_var_selector(), min_value_selector(), VARS);
    }

    /**
     * Assigns the non-instantiated variable of smallest domain size to a value at the middle of its domain.
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static IntStrategy minDom_MidValue(IntVar... VARS) {
        return custom(minDomainSize_var_selector(), mid_value_selector(), VARS);
    }

    /**
     * Splits the domain of the variable of largest domain
     *
     * @param VARS list of variables
     * @return an int strategy based on domain splits
     */
    public static IntStrategy maxDom_Split(IntVar... VARS) {
        return custom(maxDomainSize_var_selector(), mid_value_selector(), split(), VARS);
    }

    /**
     * Assigns the non-instantiated variable of smallest domain size to its upper bound.
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static IntStrategy minDom_UB(IntVar... VARS) {
        return custom(minDomainSize_var_selector(), max_value_selector(), VARS);
    }

    /**
     * Assigns the non-instantiated variable of maximum regret to its lower bound.
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static IntStrategy maxReg_LB(IntVar... VARS) {
        return custom(maxRegret_var_selector(), min_value_selector(), VARS);
    }

    /**
     * Randomly selects a variable and assigns it to a value randomly taken in {LB,UB}
     * i.e. it fixes the variable to one of its bounds
     *
     * @param VARS list of variables
     * @param SEED a seed for random
     * @return assignment strategy
     */
    public static IntStrategy random_bound(IntVar[] VARS, long SEED) {
        return custom(random_var_selector(SEED), randomBound_value_selector(SEED), VARS);
    }

    /**
     * Randomly selects a variable and assigns it to a value randomly taken in {LB,UB}
     * i.e. it fixes the variable to one of its bounds
     * Uses fixed seed 0 by default
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static IntStrategy random_bound(IntVar[] VARS) {
        return random_bound(VARS, 0);
    }

    /**
     * Randomly selects a variable and assigns it to a value randomly taken in [LB,UB]
     *
     * @param VARS list of variables
     * @param SEED a seed for random
     * @return assignment strategy
     */
    public static IntStrategy random_value(IntVar[] VARS, long SEED) {
        return custom(random_var_selector(SEED), random_value_selector(SEED), VARS);
    }

    /**
     * Randomly selects a variable and assigns it to a value randomly taken in [LB,UB]
     * Uses fixed seed 0 by default
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static IntStrategy random_value(IntVar[] VARS) {
        return random_value(VARS, 0);
    }

    /**
     * Build a sequence of <code>AbstractStrategy</code>.
     * The first strategy in parameter is first called to compute a decision, if possible.
     * Otherwise, the second strategy is called, ...
     * And so on, until the last one.
     *
     * @param strategies a list of strategies
     * @return a strategy sequencer
     */
    public static AbstractStrategy sequencer(AbstractStrategy... strategies) {
        return new StrategiesSequencer(strategies);
    }

    // ************************************************************************************
    // BLACK-BOX STRATEGIES
    // ************************************************************************************

    /**
     * Assignment strategy which selects a variable according to <code>DomOverWDeg</code>
     * and assign it to the selected value
     *
     * @param VARS         list of variables
     * @param SEED         random seed
     * @param VAL_SELECTOR heuristic to selected the value to assign to the selected variable
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> domOverWDeg(IntVar[] VARS, long SEED, IntValueSelector VAL_SELECTOR) {
        return new DomOverWDeg(VARS, SEED, VAL_SELECTOR);
    }

    /**
     * Assignment strategy which selects a variable according to <code>DomOverWDeg</code>
     * and assign it to its lower bound
     *
     * @param VARS list of variables
     * @param SEED random seed
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> domOverWDeg(IntVar[] VARS, long SEED) {
        return domOverWDeg(VARS, SEED, min_value_selector());
    }

    /**
     * Create an Activity based search strategy.
     * <p/>
     * <b>"Activity-Based Search for Black-Box Constraint Propagramming Solver"<b/>,
     * Laurent Michel and Pascal Van Hentenryck, CPAIOR12.
     * <br/>
     *
     * @param VARS           collection of variables
     * @param GAMMA          aging parameters
     * @param DELTA          for interval domain size estimation
     * @param ALPHA          forget parameter
     * @param RESTART        restart parameter
     * @param FORCE_SAMPLING minimal number of iteration for sampling phase
     * @param SEED           the seed for random
     */
    public static AbstractStrategy<IntVar> activity(IntVar[] VARS, double GAMMA, double DELTA, int ALPHA,
                                                    double RESTART, int FORCE_SAMPLING, long SEED) {
        return new ActivityBased(VARS[0].getSolver(), VARS, GAMMA, DELTA, ALPHA, RESTART, FORCE_SAMPLING, SEED);
    }

    /**
     * Create an Activity based search strategy.
     * <p/>
     * <b>"Activity-Based Search for Black-Box Constraint Propagramming Solver"<b/>,
     * Laurent Michel and Pascal Van Hentenryck, CPAIOR12.
     * <br/>
     * Uses default parameters (GAMMA=0.999d, DELTA=0.2d, ALPHA=8, RESTART=1.1d, FORCE_SAMPLING=1)
     *
     * @param VARS collection of variables
     * @param SEED the seed for random
     */
    public static AbstractStrategy<IntVar> activity(IntVar[] VARS, long SEED) {
        return activity(VARS, 0.999d, 0.2d, 8, 1.1d, 1, SEED);
    }

    /**
     * Create an Impact-based search strategy.
     * <p/>
     * <b>"Impact-Based Search Strategies for Constraint Programming",
     * Philippe Refalo, CP2004.</b>
     *
     * @param VARS       variables of the problem (should be integers)
     * @param ALPHA      aging parameter
     * @param SPLIT      split parameter for subdomains computation
     * @param NODEIMPACT force update of impacts every <code>nodeImpact</code> nodes. Set value to 0 to avoid using it.
     * @param SEED       a seed for random
     * @param INITONLY   only apply the initialisation phase, do not update impact thereafter
     */
    public static AbstractStrategy<IntVar> impact(IntVar[] VARS, int ALPHA, int SPLIT, int NODEIMPACT, long SEED, boolean INITONLY) {
        return new ImpactBased(VARS, ALPHA, SPLIT, NODEIMPACT, SEED, INITONLY);
    }

    /**
     * Create an Impact-based search strategy.
     * <p/>
     * <b>"Impact-Based Search Strategies for Constraint Programming",
     * Philippe Refalo, CP2004.</b>
     * Uses default parameters (ALPHA=2,SPLIT=3,NODEIMPACT=10,INITONLY=true)
     *
     * @param VARS variables of the problem (should be integers)
     * @param SEED a seed for random
     */
    public static AbstractStrategy<IntVar> impact(IntVar[] VARS, long SEED) {
        return impact(VARS, 2, 3, 10, SEED, true);
    }

    /**
     * Use the last conflict heuristic as a pluggin to improve a former search heuristic STRAT
     *
     * @param SOLVER the solver
     * @return last conflict strategy
     */
    public static AbstractStrategy lastConflict(Solver SOLVER) {
        return lastConflict(SOLVER, SOLVER.getStrategy());
    }

    /**
     * Use the last conflict heuristic as a pluggin to improve a former search heuristic STRAT
     *
     * @param SOLVER the solver
     * @param STRAT  the main strategy
     * @return last conflict strategy
     */
    public static AbstractStrategy lastConflict(Solver SOLVER, AbstractStrategy STRAT) {
        return lastKConflicts(SOLVER, 1, STRAT);
    }

    /**
     * Use the last conflict heuristic as a pluggin to improve a former search heuristic STRAT
     * Considers the K last conflicts
     *
     * @param SOLVER the solver
     * @param STRAT  the main strategy
     * @return last conflict strategy
     */
    public static AbstractStrategy lastKConflicts(Solver SOLVER, int K, AbstractStrategy STRAT) {
        return new LastConflict(SOLVER, STRAT, K);
    }

    /**
     * Create a Generate-And-Test search strategy which evaluate each remaining complete instantiation and
     * check its satisfiability. This does not rely on propagation but only on propagator entailment.
     * <br/>
     * This can be smoothly combined with other (IntVar) strategies, but they should be used first because this one
     * does not let non instantiated int var.
     * <br/>
     *
     * @param SOLVER the solver
     * @return a Generate-And-Test search strategy
     * @see org.chocosolver.solver.search.strategy.IntStrategyFactory#generateAndTest(org.chocosolver.solver.Solver, org.chocosolver.solver.search.strategy.strategy.AbstractStrategy, int)
     */
    public static AbstractStrategy<IntVar> generateAndTest(Solver SOLVER) {
        return new GenerateAndTest(SOLVER);
    }

    /**
     * Create a Generate-And-Test search strategy which evaluate each remaining complete instantiation and
     * check its satisfiability. This does not rely on propagation but only on propagator entailment.
     * <br/>
     * This can be smoothly combined with other (IntVar) strategies, but they should be used first because this one
     * does not let non instantiated int var.
     * <br/>
     * This signature enables to trigger Generate-And-Test only when the search space is small enough (below {@code searchSpaceLimit},
     * and a main strategy otherwise.
     *
     * @param SOLVER           the solver
     * @param mainStrategy     the strategy to apply first
     * @param searchSpaceLimit the size of search space which triggers the Generate-And-Test strategy.
     * @return a Generate-And-Test search strategy
     * @see org.chocosolver.solver.search.strategy.IntStrategyFactory#generateAndTest(org.chocosolver.solver.Solver)
     */
    public static AbstractStrategy<IntVar> generateAndTest(Solver SOLVER, AbstractStrategy<IntVar> mainStrategy, int searchSpaceLimit) {
        return new GenerateAndTest(SOLVER, mainStrategy, searchSpaceLimit);
    }
}
