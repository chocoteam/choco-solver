/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.search.strategy;

import choco.kernel.memory.IEnvironment;
import solver.Solver;
import solver.search.strategy.enumerations.MyCollection;
import solver.search.strategy.enumerations.sorters.*;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.heuristics.HeuristicValFactory;
import solver.search.strategy.enumerations.values.heuristics.zeroary.Random;
import solver.search.strategy.selectors.AdapterValueIIterator;
import solver.search.strategy.selectors.VariableSelector;
import solver.search.strategy.selectors.values.InDomainMax;
import solver.search.strategy.selectors.values.InDomainMin;
import solver.search.strategy.selectors.variables.*;
import solver.search.strategy.selectors.variables.AntiFirstFail;
import solver.search.strategy.selectors.variables.FirstFail;
import solver.search.strategy.selectors.variables.InputOrder;
import solver.search.strategy.selectors.variables.Largest;
import solver.search.strategy.selectors.variables.MaxRegret;
import solver.search.strategy.selectors.variables.MostConstrained;
import solver.search.strategy.selectors.variables.Smallest;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.DigraphStrategy;
import solver.search.strategy.strategy.Assignment;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.variables.graph.directedGraph.DirectedGraphVar;

import java.util.LinkedList;

/**
 * Strategies, Variable selectors and Value selectors factory.
 * Just there to simplify strategies creation.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 5 juil. 2010
 */
public final class StrategyFactory {

    private StrategyFactory() {
    }

    //****************************************************************************************************************//
    //******************************************* VARIABLE SELECTORS *************************************************//
    //****************************************************************************************************************//

    /**
     * Chooses variables in the order they appears in <code>vars</code>
     *
     * @param env  environment
     * @param vars list of variables
     * @return input_order variable selector
     */
    public static VariableSelector<IntVar> inputOrder(IEnvironment env, IntVar... vars) {
        return new InputOrder(vars, env);
    }

    /**
     * Chooses the variable with the smallest domain
     *
     * @param env  environment
     * @param vars list of variables
     * @return first_fail variable selector
     */
    public static VariableSelector<IntVar> firstFail(IEnvironment env, IntVar... vars) {
        return new FirstFail(vars, env);
    }

    /**
     * Chooses the variable with the largest domain
     *
     * @param env  environment
     * @param vars list of variables
     * @return anti_first_fail variable selector
     */
    public static VariableSelector<IntVar> antiFirstFail(IEnvironment env, IntVar... vars) {
        return new AntiFirstFail(vars, env);
    }

    /**
     * Chooses the variable with the smallest value in its domain
     *
     * @param env  environment
     * @param vars list of variables
     * @return smallest variable selector
     */
    public static VariableSelector<IntVar> smallest(IEnvironment env, IntVar... vars) {
        return new Smallest(vars, env);
    }

    /**
     * Chooses the variable with the largest value in its domain
     *
     * @param env  environment
     * @param vars list of variables
     * @return largest variable selector
     */
    public static VariableSelector<IntVar> largest(IEnvironment env, IntVar... vars) {
        return new Largest(vars, env);
    }

    /**
     * Chooses the variable with the largest difference between the two smallest value in its domain
     *
     * @param env  environment
     * @param vars list of variables
     * @return max_regret variable selector
     */
    public static VariableSelector<IntVar> maxRegret(IEnvironment env, IntVar... vars) {
        return new MaxRegret(vars, env);
    }

    /**
     * Chooses the variable with the smallest domain, breaking ties using the number of constraints
     *
     * @param env  environment
     * @param vars list of variables
     * @return most_constrained variable selector
     */
    public static VariableSelector<IntVar> mostConstrained(IEnvironment env, IntVar... vars) {
        return new MostConstrained(vars, env);
    }

    /**
     * Chooses the variable with the largest constraints attached
     *
     * @param env  environment
     * @param vars list of variables
     * @return occurrence variable selector
     */
    public static VariableSelector<IntVar> occurrence(IEnvironment env, IntVar... vars) {
        return new Occurrence(vars, env);
    }

    //****************************************************************************************************************//
    //********************************************** VALUE ITERATORS *************************************************//
    //****************************************************************************************************************//

    /**
     * Sets the <b>inDomainMin</b> value iterator to the list of variable in parameter.
     * This iterator chooses the smallest value in the variable's domain
     *
     * @param env environment
     * @param var list of variables declaring this value iterator
     */
    public static void indomainMin(IEnvironment env, IntVar... var) {
        for (IntVar v : var) {
            v.setHeuristicVal(new AdapterValueIIterator(new InDomainMin(v, env)));
        }
    }

    /**
     * Sets the <b>inDomainMax</b> value iterator to the list of variable in parameter.
     * This iterator chooses the largest value in the variable's domain
     *
     * @param env environment
     * @param var list of variables declaring this value iterator
     */
    public static void indomainMax(IEnvironment env, IntVar... var) {
        for (IntVar v : var) {
            v.setHeuristicVal(new AdapterValueIIterator(new InDomainMax(v, env)));
        }
    }

    /**
     * Sets the <b>inDomainMiddle</b> value iterator to the list of variable in parameter.
     * This iterator chooses the closest value to the mean between the variable's domain current bounds
     *
     * @param var list of variables declaring this value iterator
     */
    public static void indomainMiddle(IntVar... var) {
        throw new UnsupportedOperationException("not yet implemented");
//        for(Variable v : var){
//            v.setHeuristicVal(new InDomainMiddle(v));
//        }
    }

    /**
     * Sets the <b>inDomainRandom</b> value iterator to the list of variable in parameter.
     * This iterator chooses a random value in the variable's domain
     *
     * @param var list of variables declaring this value iterator
     */
    public static void indomainRandom(IntVar... var) {
        throw new UnsupportedOperationException("not yet implemented");
//        for(Variable v : var){
//            v.setHeuristicVal(new InDomainMiddle(v));
//        }
    }

    //****************************************************************************************************************//
    //************************************************* ASSIGNMENT ***************************************************//
    //****************************************************************************************************************//

    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMin</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> preset(IntVar[] variables, IEnvironment environment) {
        return inputOrderIncDomain(variables, environment);
    }


    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMin</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static Assignment inputOrderIncDomain(IntVar[] variables, choco.kernel.memory.IEnvironment environment) {
        indomainMin(environment, variables);
        return new Assignment(variables,
                inputOrder(environment, variables)
        );
    }

    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMin</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static Assignment inputOrderInDomainMin(IntVar[] variables, IEnvironment environment) {
        indomainMin(environment, variables);
        return inputOrderIncDomain(variables, environment);
//        return new Assignment(variables,
//                inputOrder(variables, environment),
//                environment);
    }

    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMin</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static Assignment forceInputOrderInDomainMin(Variable[] variables, IEnvironment environment) {
        IntVar[] ivars = new IntVar[variables.length];
        for (int i = 0; i < variables.length; i++) {
            ivars[i] = (IntVar) variables[i];
        }
        indomainMin(environment, ivars);
        return inputOrderIncDomain(ivars, environment);
    }

    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMax</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static Assignment inputOrderInDomainMax(IntVar[] variables, IEnvironment environment) {
        indomainMax(environment, variables);
        return new Assignment(variables,
                inputOrder(environment, variables)
        );
    }

    /**
     * Assignment strategy combining <code>FirstFail</code> and <code>InDomainMin</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static Assignment firstFailInDomainMin(IntVar[] variables, IEnvironment environment) {
        indomainMin(environment, variables);
        return new Assignment(variables,
                firstFail(environment, variables)
        );
    }

    /**
     * Assignment strategy combining <code>FirstFail</code> and <code>InDomainMax</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static Assignment firstFailInDomainMax(IntVar[] variables, IEnvironment environment) {
        indomainMax(environment, variables);
        return new Assignment(variables,
                firstFail(environment, variables)
        );
    }

    public static StrategyVarValAssign random(IntVar[] vars, IEnvironment environment) {
        for (IntVar v : vars) {
            v.setHeuristicVal(new Random(v.getDomain()));
        }
        LinkedList<AbstractSorter<IntVar>> sorters = new LinkedList<AbstractSorter<IntVar>>();
        sorters.add(new solver.search.strategy.enumerations.sorters.Random<IntVar>());
        return new StrategyVarValAssign(vars,
                sorters,
                ValidatorFactory.instanciated,
                environment,
                MyCollection.Type.DYN);
    }

    public static StrategyVarValAssign domwdegMindom(IntVar[] vars, Solver solver) {
        for (IntVar var : vars) {
            var.setHeuristicVal(HeuristicValFactory.enumVal(var.getDomain(), var.getUB(), -1, var.getLB()));
        }

        solver.search.strategy.enumerations.sorters.DomOverWDeg dd =
                new solver.search.strategy.enumerations.sorters.DomOverWDeg();
        solver.getSearchLoop().branchSearchMonitor(dd);
        LinkedList<AbstractSorter<IntVar>> sorters = new LinkedList<AbstractSorter<IntVar>>();
        sorters.add(dd);
        return new StrategyVarValAssign(vars,
                sorters,
                ValidatorFactory.instanciated,
                solver.getEnvironment(),
                MyCollection.Type.DYN);
    }


    public static AbstractStrategy randomArcs(GraphVar vars, IEnvironment env) {
        return new DigraphStrategy(vars);
    }
}
