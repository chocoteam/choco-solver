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

import solver.search.strategy.selectors.graph.arcs.RandomArc;
import solver.search.strategy.selectors.graph.nodes.RandomNode;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.search.strategy.strategy.graph.NodeStrategy;
import solver.variables.graph.GraphVar;

/**
 * Basic strategies over graph variables
 * Just there to simplify strategies creation.
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 02/2013
 */
public final class GraphStrategyFactory {

    private GraphStrategyFactory() {
    }

    /**
     * Lexicographic graph branching strategy.
     * Branch on nodes then arcs/edges.
     * <p/>
     * <p/> node branching:
     * Let i be the first node such that
     * i in envelope(GRAPHVAR) and i not in kernel(GRAPHVAR).
     * The decision adds i to the kernel of GRAPHVAR.
     * It is fails, then i is removed from the envelope of GRAPHVAR.
     * <p/>
     * arc/edge branching:
     * <p/> node branching:
     * Let (i,j) be the first arc/edge such that
     * (i,j) in envelope(GRAPHVAR) and (i,j) not in kernel(GRAPHVAR).
     * The decision adds (i,j) to the kernel of GRAPHVAR.
     * It is fails, then (i,j) is removed from the envelope of GRAPHVAR
     *
     * @param GRAPHVAR a graph variable to branch on
     * @return a lexicographic strategy to instantiate g
     */
    public static <G extends GraphVar> AbstractStrategy graphLexico(G GRAPHVAR) {
        return new GraphStrategy(GRAPHVAR);
    }

    /**
     * Lexicographic graph branching strategy.
     * Alternate randomly node and arc/edge decisions.
     * <p/>
     * <p/> node branching:
     * Let i be a randomly selected node such that
     * i in envelope(GRAPHVAR) and i not in kernel(GRAPHVAR).
     * The decision adds i to the kernel of GRAPHVAR.
     * It is fails, then i is removed from the envelope of GRAPHVAR.
     * <p/>
     * arc/edge branching:
     * <p/> node branching:
     * Let (i,j) be a randomly selected arc/edge arc/edge such that
     * (i,j) in envelope(GRAPHVAR) and (i,j) not in kernel(GRAPHVAR).
     * The decision adds (i,j) to the kernel of GRAPHVAR.
     * It is fails, then (i,j) is removed from the envelope of GRAPHVAR
     *
     * @param GRAPHVAR a graph variable to branch on
     * @param SEED     randomness seed
     * @return a random strategy to instantiate g
     */
    public static <G extends GraphVar> AbstractStrategy graphRandom(G GRAPHVAR, long SEED) {
        return graphStrategy(GRAPHVAR, new RandomNode(GRAPHVAR, SEED), new RandomArc(GRAPHVAR, SEED), GraphStrategy.NodeArcPriority.ARCS);
    }

    /**
     * Dedicated graph branching strategy.
     *
     * @param GRAPHVAR   a graph variable to branch on
     * @param NODE_STRAT strategy over nodes
     * @param ARC_STRAT  strategy over arcs/edges
     * @param PRIORITY   enables to mention if it should first branch on nodes
     * @param <G>        either directed or undirected graph variable
     * @return a dedicated strategy to instantiate GRAPHVAR
     */
    public static <G extends GraphVar> AbstractStrategy graphStrategy(G GRAPHVAR, NodeStrategy NODE_STRAT, ArcStrategy ARC_STRAT, GraphStrategy.NodeArcPriority PRIORITY) {
        return new GraphStrategy(GRAPHVAR, NODE_STRAT, ARC_STRAT, PRIORITY);
    }
}
