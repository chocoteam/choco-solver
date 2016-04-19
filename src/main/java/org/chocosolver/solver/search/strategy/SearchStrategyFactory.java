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
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.RealValueSelector;
import org.chocosolver.solver.search.strategy.selectors.SetValueSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.ActivityBased;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.strategy.*;
import org.chocosolver.solver.variables.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.chocosolver.solver.search.strategy.selectors.ValSelectorFactory.*;
import static org.chocosolver.solver.search.strategy.selectors.VarSelectorFactory.*;

public class SearchStrategyFactory {

    // ************************************************************************************
   	// GENERIC PATTERNS
   	// ************************************************************************************

    /**
   	 * Use the last conflict heuristic as a pluggin to improve a former search heuristic
   	 * Should be set after specifying a search strategy.
   	 * @return last conflict strategy
   	 */
    public static AbstractStrategy lastConflict(AbstractStrategy formerSearch) {
   		return new LastConflict(formerSearch.getVariables()[0].getModel(), formerSearch,1);
   	}

   	/**
   	 * Make the input search strategy greedy, that is, decisions can be applied but not refuted.
   	 * @param search a search heuristic building branching decisions
   	 * @return a greedy form of search
   	 */
    public static AbstractStrategy greedySearch(AbstractStrategy search){
   		return new GreedyBranching(search);
   	}

    public static AbstractStrategy sequencer(AbstractStrategy... searches){
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
     */
    public static SetStrategy setVarSearch(VariableSelector<SetVar> varS, SetValueSelector valS, boolean enforceFirst, SetVar... sets) {
        return new SetStrategy(sets, varS, valS, enforceFirst);
    }

    /**
     * strategy to branch on sets by choosing the first unfixed variable and forcing its first unfixed value
     *
     * @param sets variables to branch on
     * @return a strategy to instantiate sets
     */
    public static SetStrategy setVarSearch(SetVar... sets) {
        return setVarSearch(minDomVar(), firstSetVal(), true, sets);
    }

    // ************************************************************************************
    // REALVAR STRATEGIES
    // ************************************************************************************

    /**
     * Generic strategy to branch on real variables, based on domain splitting
     * @param varS  variable selection strategy
     * @param valS  strategy to select where to split domains
     * @param rvars RealVar array to branch on
     * @return a strategy to instantiate reals
     */
    public static RealStrategy realVarSearch(VariableSelector<RealVar> varS, RealValueSelector valS, RealVar... rvars) {
        return new RealStrategy(rvars, varS, valS);
    }

    /**
     * strategy to branch on real variables by choosing sequentially the next variable domain
     * to split in two, wrt the middle value
     * @param reals variables to branch on
     * @return a strategy to instantiate real variables
     */
    public static RealStrategy realVarSearch(RealVar... reals) {
        return realVarSearch(roundRobinVar(), midRealVal(), reals);
    }

    // ************************************************************************************
    // INTVAR STRATEGIES
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
    public static IntStrategy intVarSearch(VariableSelector<IntVar> varSelector,
                                     IntValueSelector valSelector,
                                     DecisionOperator<IntVar> decisionOperator,
                                     IntVar... vars) {
        return new IntStrategy(vars, varSelector, valSelector, decisionOperator);
    }

    /**
     * Builds your own assignment strategy based on <b>binary</b> decisions.
     * Selects a variable X and a value V to make the decision X = V.
     * Note that value assignments are the public static decision operators.
     * Therefore, they are not mentioned in the search heuristic name.
     * @param varSelector defines how to select a variable to branch on.
     * @param valSelector defines how to select a value in the domain of the selected variable
     * @param vars         variables to branch on
     * @return a custom search strategy
     */
    public static IntStrategy intVarSearch(VariableSelector<IntVar> varSelector,
                                     IntValueSelector valSelector,
                                     IntVar... vars) {
        return intVarSearch(varSelector, valSelector, DecisionOperator.int_eq, vars);
    }

    /**
     * Builds a default search heuristics of integer variables
     * Relies on {@link #domOverWDegSearch(IntVar...)}
     * @param vars         variables to branch on
     * @return a default search strategy
     */
    public static AbstractStrategy<IntVar> intVarSearch(IntVar... vars) {
        boolean satOrMin = vars[0].getModel().getResolutionPolicy()!= ResolutionPolicy.MAXIMIZE;
        return new DomOverWDeg(vars, 0, satOrMin?minIntVal():maxIntVal());
    }

    /**
     * Assignment strategy which selects a variable according to <code>DomOverWDeg</code> and assign it to its lower bound
     * @param vars list of variables
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> domOverWDegSearch(IntVar... vars) {
        return new DomOverWDeg(vars, 0, minIntVal());
    }

    /**
     * Create an Activity based search strategy.
     * <p>
     * <b>"Activity-Based Search for Black-Box Constraint Propagramming Solver"<b/>,
     * Laurent Michel and Pascal Van Hentenryck, CPAIOR12.
     * <br/>
     * Uses public static parameters (GAMMA=0.999d, DELTA=0.2d, ALPHA=8, RESTART=1.1d, FORCE_SAMPLING=1)
     *
     * @param vars collection of variables
     * @return an Activity based search strategy.
     */
    public static AbstractStrategy<IntVar> activityBasedSearch(IntVar... vars) {
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
    public static IntStrategy randomSearch(IntVar[] vars, long seed) {
        IntValueSelector value = randomIntVal(seed);
        IntValueSelector bound = randomIntBound(seed);
        IntValueSelector selector = var -> {
            if (var.hasEnumeratedDomain()) {
                return value.selectValue(var);
            } else {
                return bound.selectValue(var);
            }
        };
        return intVarSearch(randomVar(seed), selector, vars);
    }

    // ************************************************************************************
    // SOME EXAMPLES OF STRATEGIES YOU CAN BUILD
    // ************************************************************************************

    /**
     * Assigns the first non-instantiated variable to its lower bound.
     * @param vars list of variables
     * @return int strategy based on value assignments
     */
    public static IntStrategy inputOrderLBSearch(IntVar... vars) {
        return intVarSearch(inputOrderVar(vars[0].getModel()), minIntVal(), vars);
    }

    /**
     * Assigns the first non-instantiated variable to its upper bound.
     * @param vars list of variables
     * @return assignment strategy
     */
    public static IntStrategy inputOrderUBSearch(IntVar... vars) {
        return intVarSearch(inputOrderVar(vars[0].getModel()), maxIntVal(), vars);
    }

    /**
     * Assigns the non-instantiated variable of smallest domain size to its lower bound.
     * @param vars list of variables
     * @return assignment strategy
     */
    public static IntStrategy minDomLBSearch(IntVar... vars) {
        return intVarSearch(minDomIntVar(), minIntVal(), vars);
    }

    /**
     * Assigns the non-instantiated variable of smallest domain size to its upper bound.
     * @param vars list of variables
     * @return assignment strategy
     */
    public static IntStrategy minDomUBSearch(IntVar... vars) {
        return intVarSearch(minDomIntVar(), maxIntVal(), vars);
    }

    // ************************************************************************************
    // DEFAULT STRATEGY (COMPLETE)
    // ************************************************************************************

    /**
     * Creates a default search strategy for the given model.
     * This heuristic is complete (handles IntVar, BoolVar, SetVar and RealVar)
     *
     * @param model a model requiring a default search strategy
     */
    public static AbstractStrategy defaultSearch(Model model){
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
                    case Variable.INT: livars.add((IntVar) var); break;
                    case Variable.SET: lsvars.add((SetVar) var); break;
                    case Variable.REAL: lrvars.add((RealVar) var); break;
                    default: break; // do not throw exception to allow ad hoc variable kinds
                }
            }
        }

        // 2. extract the objective variable if any (to avoid branching on it)
        if (r.getObjectiveManager().isOptimization()) {
            objective = r.getObjectiveManager().getObjective();
            if((objective.getTypeAndKind() & Variable.REAL) != 0){
                lrvars.remove(objective);// real var objective
            }else{
                assert (objective.getTypeAndKind() & Variable.INT) != 0;
                livars.remove(objective);// bool/int var objective
            }
        }

        // 3. Creates a default search strategy for each variable kind
        int nb = 0;
        AbstractStrategy[] strats = new AbstractStrategy[4];
        if (livars.size() > 0) {
            strats[nb++] = intVarSearch(livars.toArray(new IntVar[livars.size()]));
        }
        if (lsvars.size() > 0) {
            strats[nb++] = setVarSearch(lsvars.toArray(new SetVar[lsvars.size()]));
        }
        if (lrvars.size() > 0) {
            strats[nb++] = realVarSearch(lrvars.toArray(new RealVar[lrvars.size()]));
        }

        // 4. lexico LB/UB branching for the objective variable
        if (objective != null) {
            boolean max = r.getObjectiveManager().getPolicy() == ResolutionPolicy.MAXIMIZE;
            if((objective.getTypeAndKind() & Variable.REAL) != 0){
                strats[nb++] = realVarSearch(roundRobinVar(), max?maxRealVal():minRealVal(), (RealVar) objective);
            }else{
                strats[nb++] = max ? minDomUBSearch((IntVar) objective) : minDomLBSearch((IntVar) objective);
            }
        }

        // 5. add last conflict
        if (nb == 0) {// simply to avoid null pointers in case all variables are instantiated
            strats[nb++] = minDomLBSearch(model.ONE());
        }
        return lastConflict(sequencer(Arrays.copyOf(strats, nb)));
    }
}