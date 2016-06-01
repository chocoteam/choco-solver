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
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.search.strategy.strategy.*;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

public class Search {

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
        return setVarSearch(new GeneralizedMinDomVarSelector(), new SetDomainMin(), true, sets);
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
        return realVarSearch(new Cyclic<>(), new RealDomainMiddle(), reals);
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
        // sets booleans to 1 and intvar to upper bound for maximisation
        // branch on lower bound otherwise
        boolean satOrMin = vars[0].getModel().getResolutionPolicy()!= ResolutionPolicy.MAXIMIZE;
        IntValueSelector valSel = new IntValueSelector() {
            @Override
            public int selectValue(IntVar var) {
                if(var.isBool() || !satOrMin){
                    return var.getUB();
                }else {
                    return var.getLB();
                }
            }
        };
        return new DomOverWDeg(vars, 0, valSel);
    }

    /**
     * Assignment strategy which selects a variable according to <code>DomOverWDeg</code> and assign it to its lower bound
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

    // ************************************************************************************
    // SOME EXAMPLES OF STRATEGIES YOU CAN BUILD
    // ************************************************************************************

    /**
     * Assigns the first non-instantiated variable to its lower bound.
     * @param vars list of variables
     * @return int strategy based on value assignments
     */
    public static IntStrategy inputOrderLBSearch(IntVar... vars) {
        return intVarSearch(new InputOrder<>(vars[0].getModel()), new IntDomainMin(), vars);
    }

    /**
     * Assigns the first non-instantiated variable to its upper bound.
     * @param vars list of variables
     * @return assignment strategy
     */
    public static IntStrategy inputOrderUBSearch(IntVar... vars) {
        return intVarSearch(new InputOrder<>(vars[0].getModel()), new IntDomainMax(), vars);
    }

    /**
     * Assigns the non-instantiated variable of smallest domain size to its lower bound.
     * @param vars list of variables
     * @return assignment strategy
     */
    public static IntStrategy minDomLBSearch(IntVar... vars) {
        return intVarSearch(new FirstFail(vars[0].getModel()), new IntDomainMin(), vars);
    }

    /**
     * Assigns the non-instantiated variable of smallest domain size to its upper bound.
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
        ArrayList<AbstractStrategy> strats = new ArrayList<>();
        if (livars.size() > 0) {
            strats.add(intVarSearch(livars.toArray(new IntVar[livars.size()])));
        }
        if (lsvars.size() > 0) {
            strats.add(setVarSearch(lsvars.toArray(new SetVar[lsvars.size()])));
        }
        if (lrvars.size() > 0) {
            strats.add(realVarSearch(lrvars.toArray(new RealVar[lrvars.size()])));
        }

        // 4. lexico LB/UB branching for the objective variable
        if (objective != null) {
            boolean max = r.getObjectiveManager().getPolicy() == ResolutionPolicy.MAXIMIZE;
            if((objective.getTypeAndKind() & Variable.REAL) != 0){
                strats.add(realVarSearch(new Cyclic<>(), max?new RealDomainMax():new RealDomainMin(), (RealVar) objective));
            }else{
                strats.add(max ? minDomUBSearch((IntVar) objective) : minDomLBSearch((IntVar) objective));
            }
        }

        // 5. avoid null pointers in case all variables are instantiated
        if (strats.isEmpty()) {
            strats.add(minDomLBSearch(model.boolVar(true)));
        }

        // 6. add last conflict
        return lastConflict(sequencer(strats.toArray(new AbstractStrategy[strats.size()])));
    }
}