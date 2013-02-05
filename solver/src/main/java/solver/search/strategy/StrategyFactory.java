/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.search.strategy;

import choco.kernel.memory.IEnvironment;
import solver.Solver;
import solver.search.strategy.enumerations.sorters.ActivityBased;
import solver.search.strategy.enumerations.sorters.Incr;
import solver.search.strategy.enumerations.sorters.Seq;
import solver.search.strategy.enumerations.sorters.SorterFactory;
import solver.search.strategy.enumerations.sorters.metrics.LowerBound;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.selectors.graph.arcs.RandomArc;
import solver.search.strategy.selectors.graph.nodes.RandomNode;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy.NodeArcPriority;
import solver.search.strategy.strategy.graph.NodeStrategy;
import solver.search.strategy.strategy.set.SetSearchStrategy;
import solver.variables.IntVar;
import solver.variables.SetVar;
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
                new Seq<IntVar>(SorterFactory.minDomain(), SorterFactory.inputOrder(variables)),
                ValidatorFactory.instanciated,
                environment);
    }

    /**
     * Assignment strategy combining <code>MinDom</code> and <code>LowerBound</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> minDomLowBound(IntVar[] variables, IEnvironment environment) {
        HeuristicValFactory.indomainMin(variables);
        return StrategyVarValAssign.dyn(variables,
                new Seq<IntVar>(new Incr<IntVar>(LowerBound.build()), SorterFactory.inputOrder(variables)),
                ValidatorFactory.instanciated,
                environment);
    }

    /**
     * Assignment strategy combining <code>MinDom</code> and <code>MidVal</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> minDomMidVal(IntVar[] variables, IEnvironment environment) {
        HeuristicValFactory.indomainSplitMin(variables);
        return StrategyVarValAssign.dyn(variables,
                new Seq<IntVar>(SorterFactory.minDomain(), SorterFactory.inputOrder(variables)),
                ValidatorFactory.instanciated,
                environment);
    }

    /**
     * Assignment strategy combining <code>MinDom</code> and <code>MaxVal</code>
     *
     * @param variables   list of variables
     * @param environment environment
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> minDomMaxVal(IntVar[] variables, IEnvironment environment) {
        HeuristicValFactory.indomainMax(variables);
        return StrategyVarValAssign.dyn(variables,
                new Seq<IntVar>(SorterFactory.minDomain(), SorterFactory.inputOrder(variables)),
                ValidatorFactory.instanciated,
                environment);
    }

    public static AbstractStrategy<IntVar> maxRegMinVal(IntVar[] variables, IEnvironment environment) {
        HeuristicValFactory.indomainMin(variables);
        return StrategyVarValAssign.dyn(variables,
                new Seq<IntVar>(SorterFactory.maxRegret(), SorterFactory.inputOrder(variables)),
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

    public static AbstractStrategy<IntVar> domddegMinDom(IntVar[] vars) {
        Solver solver = vars[0].getSolver();
        for (IntVar var : vars) {
            var.setHeuristicVal(HeuristicValFactory.enumVal(var, var.getLB(), 1, var.getUB()));
        }
        return StrategyVarValAssign.dyn(vars,
                new Seq<IntVar>(SorterFactory.domddeg(), SorterFactory.random()),
                ValidatorFactory.instanciated,
                solver.getEnvironment());
    }


    public static AbstractStrategy<IntVar> domwdegMindom(IntVar[] vars, long seed) {
        for (IntVar var : vars) {
            var.setHeuristicVal(HeuristicValFactory.enumVal(var, var.getLB(), 1, var.getUB()));
        }
        Solver solver = vars[0].getSolver();
        return StrategyVarValAssign.dyn(vars,
                SorterFactory.domOverWDeg(solver, seed),
                ValidatorFactory.instanciated,
                solver.getEnvironment());
    }

    public static AbstractStrategy<IntVar> domwdeginputorderMindom(IntVar[] vars, Solver solver) {
        for (IntVar var : vars) {
            var.setHeuristicVal(HeuristicValFactory.enumVal(var, var.getLB(), 1, var.getUB()));
        }
        return StrategyVarValAssign.dyn(vars,
                new Seq<IntVar>(SorterFactory.domOverWDeg(solver, 0), SorterFactory.inputOrder(vars)),
                ValidatorFactory.instanciated,
                solver.getEnvironment());
    }

    public static AbstractStrategy<IntVar> domwdegMindom(IntVar[] vars, Solver solver, long seed) {
        for (IntVar var : vars) {
            var.setHeuristicVal(HeuristicValFactory.enumVal(var, var.getLB(), 1, var.getUB()));
        }
        return StrategyVarValAssign.dyn(vars,
                SorterFactory.domOverWDeg(solver, seed),
                ValidatorFactory.instanciated,
                solver.getEnvironment());
    }

    public static AbstractStrategy<IntVar> domwdegMiddom(IntVar[] vars, Solver solver, long seed) {
        HeuristicValFactory.indomainMiddle(vars);
        return StrategyVarValAssign.dyn(vars,
                SorterFactory.domOverWDeg(solver, seed),
                ValidatorFactory.instanciated,
                solver.getEnvironment());
    }


    public static AbstractStrategy<IntVar> domwdegMaxdom(IntVar[] vars, Solver solver, long seed) {
        HeuristicValFactory.indomainMax(vars);
        return StrategyVarValAssign.dyn(vars,
                SorterFactory.domOverWDeg(solver, seed),
                ValidatorFactory.instanciated,
                solver.getEnvironment());
    }

    public static AbstractStrategy<IntVar> ABSrandom(IntVar[] vars, Solver solver, double g, double d, int a, double r, int samplingIterationForced, long seed) {
        return new ActivityBased(solver, vars, g, d, a, r, samplingIterationForced, seed);
    }

    public static <G extends GraphVar> AbstractStrategy graphStrategy(G g, NodeStrategy nodeStrat, ArcStrategy arcStrat, NodeArcPriority priority) {
        return new GraphStrategy(g, nodeStrat, arcStrat, priority);
    }

    public static <G extends GraphVar> AbstractStrategy graphLexico(G g) {
        return new GraphStrategy(g);
    }

    public static <G extends GraphVar> AbstractStrategy graphRandom(G g, long seed) {
        return graphStrategy(g, new RandomNode(g, seed), new RandomArc(g, seed), NodeArcPriority.ARCS);
    }

    public static SetSearchStrategy setLex(SetVar[] sets) {
        return new SetSearchStrategy(sets);
    }
}
