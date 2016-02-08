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
package org.chocosolver.solver.search.strategy;

import org.chocosolver.solver.Resolver;
import org.chocosolver.solver.objective.ObjectiveStrategy;
import org.chocosolver.solver.objective.OptimizationPolicy;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.selectors.IValSelectorFactory;
import org.chocosolver.solver.search.strategy.selectors.IVarSelectorFactory;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainRandom;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainRandomBound;
import org.chocosolver.solver.search.strategy.selectors.variables.ActivityBased;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;

public interface IIntStrategyFactory extends IVarSelectorFactory, IValSelectorFactory{

    Resolver _me();

    // ************************************************************************************
    // CUSTOM STRATEGIES
    // ************************************************************************************

    /**
     * Builds your own search strategy based on <b>binary</b> decisions.
     *
     * @param varSelector defines how to select a variable to branch on.
     * @param valSelector defines how to select a value in the domain of the selected variable
     * @param decisionOperator defines how to modify the domain of the selected variable with the selected value
     * @param vars         variables to branch on
     * @return a custom search strategy
     */
    default IntStrategy intVarSearch(VariableSelector<IntVar> varSelector,
                                     IntValueSelector valSelector,
                                     DecisionOperator<IntVar> decisionOperator,
                                     IntVar... vars) {
        return new IntStrategy(vars, varSelector, valSelector, decisionOperator);
    }

    /**
     * Builds your own assignment strategy based on <b>binary</b> decisions.
     * Selects a variable X and a value V to make the decision X = V.
     * Note that value assignments are the default decision operators.
     * Therefore, they are not mentioned in the search heuristic name.
     * @param varSelector defines how to select a variable to branch on.
     * @param valSelector defines how to select a value in the domain of the selected variable
     * @param vars         variables to branch on
     * @return a custom search strategy
     */
    default IntStrategy intVarSearch(VariableSelector<IntVar> varSelector,
                                     IntValueSelector valSelector,
                                     IntVar... vars) {
        return intVarSearch(varSelector, valSelector, DecisionOperator.int_eq, vars);
    }

    // ************************************************************************************
    // SOME EXAMPLES OF STRATEGIES YOU CAN BUILD
    // ************************************************************************************

    /**
     * Assigns the first non-instantiated variable to its lower bound.
     * @param vars list of variables
     * @return int strategy based on value assignments
     */
    default IntStrategy firstLBSearch(IntVar... vars) {
        return intVarSearch(firstVarSelector(), minValSelector(), vars);
    }

    /**
     * Assigns the first non-instantiated variable to its upper bound.
     * @param vars list of variables
     * @return assignment strategy
     */
    default IntStrategy firstUBSearch(IntVar... vars) {
        return intVarSearch(firstVarSelector(), maxValSelector(), vars);
    }

    /**
     * Assigns the non-instantiated variable of smallest domain size to its lower bound.
     * @param vars list of variables
     * @return assignment strategy
     */
    default IntStrategy minDomLBSearch(IntVar... vars) {
        return intVarSearch(minDomVarSelector(), minValSelector(), vars);
    }

    /**
     * Assigns the non-instantiated variable of smallest domain size to its upper bound.
     * @param vars list of variables
     * @return assignment strategy
     */
    default IntStrategy minDomUBSearch(IntVar... vars) {
        return intVarSearch(minDomVarSelector(), maxValSelector(), vars);
    }

    /**
     * Create a search strategy which selects the variables with the largest domain and split it
     * @param vars variables to branch on
     * @return a search strategy halving IntVar domains
     */
    default IntStrategy maxDomSplitSearch(IntVar... vars){
        return intVarSearch(maxDomVarSelector(), midValSelector(true), DecisionOperator.int_split, vars);
    }

    // ************************************************************************************
    // BLACK-BOX STRATEGIES
    // ************************************************************************************

    /**
     * Assignment strategy which selects a variable according to <code>DomOverWDeg</code> and assign it to its lower bound
     * @param vars list of variables
     * @return assignment strategy
     */
    default AbstractStrategy<IntVar> domOverWDegSearch(IntVar... vars) {
        return new DomOverWDeg(vars, 0, _me().minValSelector());
    }

    /**
     * Create an Activity based search strategy.
     * <p>
     * <b>"Activity-Based Search for Black-Box Constraint Propagramming Solver"<b/>,
     * Laurent Michel and Pascal Van Hentenryck, CPAIOR12.
     * <br/>
     * Uses default parameters (GAMMA=0.999d, DELTA=0.2d, ALPHA=8, RESTART=1.1d, FORCE_SAMPLING=1)
     *
     * @param vars collection of variables
     * @return an Activity based search strategy.
     */
    default AbstractStrategy<IntVar> activityBasedSearch(IntVar... vars) {
        return new ActivityBased(vars);
    }

    /**
     * Randomly selects a variable and assigns it to a value randomly taken in
     * - the domain in case the variable has an enumerated domain
     * - {LB,UB} (one of the two bounds) in case the domain is bounded
     *
     * @param vars list of variables
     * @param seed a seed for random
     * @return assignment strategy
     */
    default IntStrategy randomSearch(IntVar[] vars, long seed) {
        IntValueSelector value = new IntDomainRandom(seed);
        IntValueSelector bound = new IntDomainRandomBound(seed);
        IntValueSelector selector = (IntValueSelector) var -> {
            if (var.hasEnumeratedDomain()) {
                return value.selectValue(var);
            } else {
                return bound.selectValue(var);
            }
        };
        return intVarSearch(new Random<IntVar>(seed), selector, vars);
    }

    // ************************************************************************************
    // OBJECTIVE STRATEGIES
    // ************************************************************************************


    /**
     * A branching strategy over the objective variable.
     * It is activated on the first solution, and iterates over the domain in increasing order (lower bound first).
     * @param obj integer objective variable
     * @return objective strategy
     */
    default ObjectiveStrategy objectiveDichotomicBranching(IntVar obj){
        return new ObjectiveStrategy(obj, OptimizationPolicy.DICHOTOMIC);
    }
}
