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
import solver.search.strategy.enumerations.sorters.SorterFactory;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.selectors.graph.arcs.RandomArc;
import solver.search.strategy.selectors.graph.nodes.RandomNode;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.search.strategy.strategy.graph.NodeStrategy;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;

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

    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMin</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> presetI(IntVar[] variables, IEnvironment environment) {
        HeuristicValFactory.presetI(variables);
        return StrategyVarValAssign.sta(variables,
                SorterFactory.presetI(variables),
                ValidatorFactory.instanciated,
                environment);
    }


    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>MinVal</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> inputOrderMinVal(IntVar[] variables, IEnvironment environment) {
        HeuristicValFactory.indomainMin(variables);
        return StrategyVarValAssign.sta(variables,
                SorterFactory.inputOrder(variables),
                ValidatorFactory.instanciated,
                environment);
    }

    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMin</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> forceInputOrderMinVal(Variable[] variables, IEnvironment environment) {
        IntVar[] ivars = new IntVar[variables.length];
        for (int i = 0; i < variables.length; i++) {
            ivars[i] = (IntVar) variables[i];
        }
        return inputOrderMinVal(ivars, environment);
    }

    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMax</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> inputOrderMaxVal(IntVar[] variables, IEnvironment environment) {
        HeuristicValFactory.indomainMax(variables);
        return StrategyVarValAssign.sta(variables,
                SorterFactory.inputOrder(variables),
                ValidatorFactory.instanciated,
                environment);
    }

    /**
     * Assignment strategy combining <code>MinDom</code> and <code>MinVal</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> minDomMinVal(IntVar[] variables, IEnvironment environment) {
        HeuristicValFactory.indomainMin(variables);
        return StrategyVarValAssign.dyn(variables,
                SorterFactory.minDomain(),
                ValidatorFactory.instanciated,
                environment);
    }


    public static AbstractStrategy<IntVar> random(IntVar[] vars, IEnvironment environment) {
        HeuristicValFactory.random(vars);
        return StrategyVarValAssign.dyn(vars,
                SorterFactory.random(),
                ValidatorFactory.instanciated,
                environment);
    }

    public static AbstractStrategy<IntVar> random(IntVar[] vars, IEnvironment environment, long seed) {
        HeuristicValFactory.random(seed, vars);
        return StrategyVarValAssign.dyn(vars,
                SorterFactory.random(seed),
                ValidatorFactory.instanciated,
                environment);
    }

    public static AbstractStrategy<IntVar> domwdegMindom(IntVar[] vars, Solver solver) {
        for (IntVar var : vars) {
            var.setHeuristicVal(HeuristicValFactory.enumVal(var, var.getLB(), 1, var.getUB()));
        }
        return StrategyVarValAssign.dyn(vars,
                SorterFactory.domOverWDeg(solver),
                ValidatorFactory.instanciated,
                solver.getEnvironment());
    }


    public static <G extends GraphVar> AbstractStrategy graphStrategy(G g, NodeStrategy nodeStrat, ArcStrategy arcStrat) {
        return new GraphStrategy(g,nodeStrat,arcStrat);
    }
    
    public static <G extends GraphVar> AbstractStrategy graphLexico(G g) {
        return new GraphStrategy(g);
    }
    
    public static <G extends GraphVar> AbstractStrategy graphRandom(G g, long seed) {
        return graphStrategy(g, new RandomNode(g, seed), new RandomArc(g, seed));
    }
}
