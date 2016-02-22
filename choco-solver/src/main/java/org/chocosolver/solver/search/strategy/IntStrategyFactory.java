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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.objective.ObjectiveStrategy;
import org.chocosolver.solver.objective.OptimizationPolicy;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.ValSelectorFactory;
import org.chocosolver.solver.search.strategy.selectors.VarSelectorFactory;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.search.strategy.strategy.*;
import org.chocosolver.solver.variables.IntVar;

/**
 * @deprecated : search strategies for integer variables should be done through
 * {@link SearchStrategyFactory}, {@link VarSelectorFactory} and {@link ValSelectorFactory}
 *
 * This class will be removed in versions > 3.4.0
 */
@Deprecated
public class IntStrategyFactory {

    /**
     * Private constructor of this factory
     */
    IntStrategyFactory() {}

    // ************************************************************************************
    // VARIABLE SELECTORS (non-exhaustive list)
    // ************************************************************************************

    /**
     * @deprecated : use {@link SearchStrategyFactory#} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static VariableSelector<IntVar> lexico_var_selector() {
        return new InputOrder<>();
    }

    /**
     * @deprecated : use {@link VarSelectorFactory#randomVar()} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static VariableSelector<IntVar> random_var_selector(long SEED) {
        return new Random<>(SEED);
    }

    /**
     * @deprecated : use {@link VarSelectorFactory#minDomIntVar()} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static VariableSelector<IntVar> minDomainSize_var_selector() {
        return new FirstFail();
    }

    /**
     * @deprecated : use {@link VarSelectorFactory#maxDomIntVar()} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static VariableSelector<IntVar> maxDomainSize_var_selector() {
        return new AntiFirstFail();
    }

    /**
     * @deprecated : use new MaxRegret instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static VariableSelector<IntVar> maxRegret_var_selector() {
        return new MaxRegret();
    }

    // ************************************************************************************
    // VALUE SELECTORS
    // ************************************************************************************

    /**
     * @deprecated : use {@link ValSelectorFactory#minIntVal()} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntValueSelector min_value_selector() {
        return new IntDomainMin();
    }

    /**
     * @deprecated : use {@link ValSelectorFactory#midIntVal(boolean)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntValueSelector mid_value_selector(boolean floor) {
        return new IntDomainMiddle(floor);
    }

    /**
     * @deprecated : use {@link ValSelectorFactory#maxIntVal()} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntValueSelector max_value_selector() {
        return new IntDomainMax();
    }

    /**
     * @deprecated : use {@link ValSelectorFactory#randomIntVal(long)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntValueSelector randomBound_value_selector(long SEED) {
        return new IntDomainRandomBound(SEED);
    }

    /**
     * @deprecated : use {@link ValSelectorFactory#randomIntVal(long)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntValueSelector random_value_selector(long SEED) {
        return new IntDomainRandom(SEED);
    }

    // ************************************************************************************
    // OPERATORS
    // ************************************************************************************

    /**
     * @deprecated : use directly {@link DecisionOperator} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static DecisionOperator<IntVar> assign() {
        return DecisionOperator.int_eq;
    }

    /**
     * @deprecated : use directly {@link DecisionOperator} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static DecisionOperator<IntVar> remove() {
        return DecisionOperator.int_neq;
    }

    /**
     * @deprecated : use directly {@link DecisionOperator} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static DecisionOperator<IntVar> split() {
        return DecisionOperator.int_split;
    }

    /**
     * @deprecated : use directly {@link DecisionOperator} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static DecisionOperator<IntVar> reverse_split() {
        return DecisionOperator.int_reverse_split;
    }

    // ************************************************************************************
    // CUSTOM STRATEGIES
    // ************************************************************************************

    /**
     * @deprecated : use {@link SearchStrategyFactory#intVarSearch(VariableSelector, IntValueSelector, DecisionOperator, IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy custom(VariableSelector<IntVar> VAR_SELECTOR,
                                     IntValueSelector VAL_SELECTOR,
                                     DecisionOperator<IntVar> DEC_OPERATOR,
                                     IntVar... VARS) {
        return new IntStrategy(VARS, VAR_SELECTOR, VAL_SELECTOR, DEC_OPERATOR);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#intVarSearch(VariableSelector, IntValueSelector, DecisionOperator, IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy custom(VariableSelector<IntVar> VAR_SELECTOR,
                                     IntValueSelector VAL_SELECTOR,
                                     IntVar... VARS) {
        return custom(VAR_SELECTOR, VAL_SELECTOR, assign(), VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#intVarSearch(VariableSelector, IntValueSelector, DecisionOperator, IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy dichotomic(VariableSelector<IntVar> VAR_SELECTOR, boolean LOWERFIRST, IntVar... VARS){
        if(LOWERFIRST){
            return custom(VAR_SELECTOR, ISF.mid_value_selector(LOWERFIRST), DecisionOperator.int_split, VARS);
        }else{
            return custom(VAR_SELECTOR, ISF.mid_value_selector(LOWERFIRST), DecisionOperator.int_reverse_split, VARS);
        }
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#greedySearch(AbstractStrategy)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy once(VariableSelector<IntVar> VAR_SELECTOR,
                                   IntValueSelector VAL_SELECTOR,
                                   DecisionOperator<IntVar> DEC_OPERATOR,
                                   IntVar... VARS){
        return new Once(VARS, VAR_SELECTOR, VAL_SELECTOR, DEC_OPERATOR);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#greedySearch(AbstractStrategy)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy once(VariableSelector<IntVar> VAR_SELECTOR,
                                   IntValueSelector VAL_SELECTOR,
                                   IntVar... VARS){
        return new Once(VARS, VAR_SELECTOR, VAL_SELECTOR, assign());
    }
    // ************************************************************************************
    // SOME EXAMPLES OF STRATEGIES YOU CAN BUILD
    // ************************************************************************************

    /**
     * @deprecated : use {@link SearchStrategyFactory#intVarSearch(VariableSelector, IntValueSelector, DecisionOperator, IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static ObjectiveStrategy objective_dichotomic(IntVar OBJECTIVE){
        return new ObjectiveStrategy(OBJECTIVE, OptimizationPolicy.DICHOTOMIC);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#inputOrderLBSearch(IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy lexico_LB(IntVar... VARS) {
        return custom(lexico_var_selector(), min_value_selector(), VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#intVarSearch(VariableSelector, IntValueSelector, DecisionOperator, IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy lexico_Neq_LB(IntVar... VARS) {
        return custom(lexico_var_selector(), min_value_selector(), remove(), VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#intVarSearch(VariableSelector, IntValueSelector, DecisionOperator, IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy lexico_Split(IntVar... VARS) {
        return dichotomic(ISF.lexico_var_selector(), true, VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#inputOrderUBSearch(IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy lexico_UB(IntVar... VARS) {
        return custom(lexico_var_selector(), max_value_selector(), VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#minDomLBSearch(IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy minDom_LB(IntVar... VARS) {
        return custom(minDomainSize_var_selector(), min_value_selector(), VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#intVarSearch(VariableSelector, IntValueSelector, DecisionOperator, IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy minDom_MidValue(boolean floor, IntVar... VARS) {
        return custom(minDomainSize_var_selector(), mid_value_selector(floor), VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#intVarSearch(VariableSelector, IntValueSelector, DecisionOperator, IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy maxDom_Split(IntVar... VARS) {
        return dichotomic(maxDomainSize_var_selector(), true, VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#minDomUBSearch(IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy minDom_UB(IntVar... VARS) {
        return custom(minDomainSize_var_selector(), max_value_selector(), VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#intVarSearch(VariableSelector, IntValueSelector, DecisionOperator, IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy maxReg_LB(IntVar... VARS) {
        return custom(maxRegret_var_selector(), min_value_selector(), VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#inputOrderLBSearch(IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static ObjectiveStrategy objective_bottom_up(IntVar OBJECTIVE){
        return new ObjectiveStrategy(OBJECTIVE, OptimizationPolicy.BOTTOM_UP);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#inputOrderUBSearch(IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static ObjectiveStrategy objective_top_bottom(IntVar OBJECTIVE){
        return new ObjectiveStrategy(OBJECTIVE, OptimizationPolicy.TOP_DOWN);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#randomSearch(IntVar[], long)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy random(IntVar[] VARS, long SEED) {
        IntValueSelector value = random_value_selector(SEED);
        IntValueSelector bound = randomBound_value_selector(SEED);
        IntValueSelector selector = (IntValueSelector) var -> {
            if (var.hasEnumeratedDomain()) {
                return value.selectValue(var);
            } else {
                return bound.selectValue(var);
            }
        };
        return custom(random_var_selector(SEED), selector, VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#randomSearch(IntVar[], long)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy random_bound(IntVar[] VARS, long SEED) {
        return custom(random_var_selector(SEED), randomBound_value_selector(SEED), VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#randomSearch(IntVar[], long)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy random_bound(IntVar[] VARS) {
        return random_bound(VARS, 0);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#randomSearch(IntVar[], long)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy random_value(IntVar[] VARS, long SEED) {
        for (IntVar v : VARS) {
            if (!v.hasEnumeratedDomain()) {
                throw new UnsupportedOperationException("Some variables have bounded domains, " +
                        "please use random heuristic instead");
            }
        }
        return custom(random_var_selector(SEED), random_value_selector(SEED), VARS);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#randomSearch(IntVar[], long)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntStrategy random_value(IntVar[] VARS) {
        return random_value(VARS, 0);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#sequencer(AbstractStrategy[])} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static AbstractStrategy sequencer(AbstractStrategy... strategies) {
        return new StrategiesSequencer(strategies);
    }

    // ************************************************************************************
    // BLACK-BOX STRATEGIES
    // ************************************************************************************

    /**
     * @deprecated : use {@link SearchStrategyFactory#domOverWDegSearch(IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static AbstractStrategy<IntVar> domOverWDeg(IntVar[] VARS, long SEED, IntValueSelector VAL_SELECTOR) {
        return new DomOverWDeg(VARS, SEED, VAL_SELECTOR);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#domOverWDegSearch(IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static AbstractStrategy<IntVar> domOverWDeg(IntVar[] VARS, long SEED) {
        return domOverWDeg(VARS, SEED, min_value_selector());
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#activityBasedSearch(IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static AbstractStrategy<IntVar> activity(IntVar[] VARS, double GAMMA, double DELTA, int ALPHA, int FORCE_SAMPLING, long SEED) {
        return new ActivityBased(VARS[0].getModel(), VARS, GAMMA, DELTA, ALPHA, FORCE_SAMPLING, SEED);
    }

    /**
     * @deprecated : use {@link SearchStrategyFactory#activityBasedSearch(IntVar...)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static AbstractStrategy<IntVar> activity(IntVar[] VARS, long SEED) {
        return activity(VARS, 0.999d, 0.2d, 8, 1, SEED);
    }

    /**
     * @deprecated : use directly new ImpactBased instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static AbstractStrategy<IntVar> impact(IntVar[] VARS, int ALPHA, int SPLIT, int NODEIMPACT, long SEED, boolean INITONLY) {
        return new ImpactBased(VARS, ALPHA, SPLIT, NODEIMPACT, SEED, INITONLY);
    }

    /**
     * @deprecated : use constructor new ImpactBased instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static AbstractStrategy<IntVar> impact(IntVar[] VARS, long SEED) {
        return impact(VARS, 2, 3, 10, SEED, true);
    }

    /**
     * @deprecated : use directly {@link SearchStrategyFactory#lastConflict(AbstractStrategy)} instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static AbstractStrategy lastConflict(Model Model) {
        return lastConflict(Model, Model.getSolver().getStrategy());
    }

    /**
     * @deprecated : use directly {@link SearchStrategyFactory#lastConflict(AbstractStrategy)}  instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static AbstractStrategy lastConflict(Model Model, AbstractStrategy STRAT) {
        return lastKConflicts(Model, 1, STRAT);
    }

    /**
     * @deprecated : use directly {@link SearchStrategyFactory#lastConflict(AbstractStrategy)}  instead
     * Will be removed in versions > 3.4.0
     */
    @Deprecated
    public static AbstractStrategy lastKConflicts(Model Model, int K, AbstractStrategy STRAT) {
        return new LastConflict(Model, STRAT, K);
    }
}
