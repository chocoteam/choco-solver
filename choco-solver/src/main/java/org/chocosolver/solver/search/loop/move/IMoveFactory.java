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
package org.chocosolver.solver.search.loop.move;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Resolver;
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.limits.SolutionCounter;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.restart.IRestartStrategy;
import org.chocosolver.solver.search.restart.MonotonicRestartStrategy;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.criteria.LongCriterion;

/**
 * Interface to define how to explore the search space from a macro perspective
 * (DFS, LDS, LNS, etc.)
 * @author Charles Prud'Homme, Jean-Guillaume Fages
 */
public interface IMoveFactory {

    Resolver _me();

    /**
     * Depth-First Search algorithm with binary decisions and no learning.
     * The current search loop (if any) will be replaced by this one after that call.
     * The current search strategy (if any) will also be replaced by the input one.
     *
     * @param aSearchStrategy the search strategy to apply
     */
    default void setDFS(AbstractStrategy aSearchStrategy) {
        _me().set(new MoveBinaryDFS(aSearchStrategy));
    }

    /**
     * Limited Discrepancy Search[1] algorithms with binary decisions and no learning.
     * <p>
     * [1]:W.D. Harvey and M.L.Ginsberg, Limited Discrepancy Search, IJCAI-95.
     *
     * The current search loop (if any) will be replaced by this one after that call.
     * The current search strategy (if any) will also be replaced by the input one.
     *
     * @param aSearchStrategy the search strategy to apply
     * @param discrepancy     the maximum discrepancy
     */
    default void setLDS(AbstractStrategy aSearchStrategy, int discrepancy) {
        IEnvironment env = _me().getModel().getEnvironment();
        _me().set(new MoveBinaryLDS(aSearchStrategy, discrepancy, env));
    }

    /**
     * Depth-bounded Discrepancy Search[1] algorithms with binary decisions and no learning.
     * <p>
     * [1]:T. Walsh, Depth-bounded Discrepancy Search, IJCAI-97.
     *
     * @param aSearchStrategy the search strategy to apply
     * @param discrepancy     the maximum discrepancy
     */
    default void setDDS(AbstractStrategy aSearchStrategy, int discrepancy) {
        IEnvironment env = _me().getModel().getEnvironment();
        _me().set(new MoveBinaryDDS(aSearchStrategy, discrepancy, env));
    }

    /**
     * Creates a move object based on:
     * Hybrid Best-First Search[1] algorithms with binary decisions.
     * <p>
     * [1]:D. Allouche, S. de Givry, G. Katsirelos, T. Schiex, M. Zytnicki,
     * Anytime Hybrid Best-First Search with Tree Decomposition for Weighted CSP, CP-2015.
     *
     * @param aSearchStrategy the search strategy to apply
     * @param a               lower bound to limit the rate of redundantly propagated decisions
     * @param b               upper bound to limit the rate of redundantly propagated decisions.
     * @param N               backtrack limit for each DFS try, should be large enough to limit redundancy
     */
    default void setHBFS(AbstractStrategy aSearchStrategy, double a, double b, long N) {
        _me().set(new MoveBinaryHBFS(_me().getModel(), aSearchStrategy, a, b, N));
    }

    /**
     * Creates a Move object that encapsulates the current move within a restart move.
     * Every time the <code>restartCriterion</code> is met, a restart is done, the new restart limit is updated
     * thanks to <code>restartStrategy</code>.
     * There will be at most <code>restartsLimit</code> restarts.
     *
     * @param restartCriterion the restart criterion, that is, the condition which triggers a restart
     * @param restartStrategy  the way restart limit (evaluated in <code>restartCriterion</code>) is updated, that is, computes the next limit
     * @param restartsLimit    number of allowed restarts
     */
    default void setRestarts(LongCriterion restartCriterion, IRestartStrategy restartStrategy, int restartsLimit) {
        _me().set(new MoveRestart(_me().getMove(), restartStrategy, restartCriterion, restartsLimit));
    }

    /**
     * Creates a Move object that encapsulates the current move within a restart move.
     * Every time a solution is found, a restart is done.
     */
    default Move restartOnSolutions() {
        return new MoveRestart(_me().getMove(),
                new MonotonicRestartStrategy(1),
                new SolutionCounter(_me().getModel(), 1),
                Integer.MAX_VALUE);
    }

    /**
     * Creates a Move object based on Large Neighborhood Search.
     * It encapsulates the current move within a LNS move.
     * Anytime a solution is encountered, it is recorded and serves as a basis for the <code>neighbor</code>.
     * The <code>neighbor</code> creates a <i>fragment</i>: selects variables to freeze/unfreeze wrt the last solution found.
     * If a fragment cannot be extended to a solution, a new one is selected by restarting the search.
     * If a fragment induces a search space which a too big to be entirely evaluated, restarting the search can be forced
     * using the <code>restartCriterion</code>. A fast restart strategy is often a good choice.
     *
     * @param neighbor         the neighbor for the LNS
     * @param restartCounter the (fast) restart counter. Initial limit gives the frequency.
     */
    default Move lns(INeighbor neighbor, ICounter restartCounter) {
        return new MoveLNS(_me().getMove(), neighbor, restartCounter);
    }


    /**
     * Creates a Move object based on Large Neighborhood Search.
     * It encapsulates the current move within a LNS move.
     * Anytime a solution is encountered, it is recorded and serves as a basis for the <code>neighbor</code>.
     * The <code>neighbor</code> creates a <i>fragment</i>: selects variables to freeze/unfreeze wrt the last solution found.
     * If a fragment cannot be extended to a solution, a new one is selected by restarting the search.
     *
     * @see #lns(INeighbor, ICounter)
     * @param neighbor         the neighbor for the LNS
     */
    default Move lns(INeighbor neighbor) {
        return new MoveLNS(_me().getMove(), neighbor, ICounter.Impl.None);
    }
}
