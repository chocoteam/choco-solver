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
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.limits.SolutionCounter;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.restart.IRestartStrategy;
import org.chocosolver.solver.search.restart.MonotonicRestartStrategy;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.criteria.LongCriterion;

/**
 * A search loop factory which provides common search loops, or services to modify the current one.
 * Created by cprudhom on 06/10/15.
 * Project: choco.
 */
public class SearchLoopFactory {

    SearchLoopFactory() {
    }

    /**
     * Depth-First Search algorithm with binary decisions and no learning.
     * The current search loop (if any) will be replaced by this one after that call.
     * The current search strategy (if any) will also be replaced by the input one.
     *
     * @param aModel         the target model
     * @param aSearchStrategy the search strategy to apply
     * @param <V>             the type of variables
     */
    public static <V extends Variable> void dfs(Model aModel, AbstractStrategy<V> aSearchStrategy) {
        aModel.set(new Resolver(aModel, new PropagateBasic(), new LearnNothing(), new MoveBinaryDFS(aSearchStrategy)));
    }

    /**
     * Limited Discrepancy Search[1] algorithms with binary decisions and no learning.
     * <p>
     * [1]:W.D. Harvey and M.L.Ginsberg, Limited Discrepancy Search, IJCAI-95.
     *
     * The current search loop (if any) will be replaced by this one after that call.
     * The current search strategy (if any) will also be replaced by the input one.
     *
     * @param aModel         the target model
     * @param aSearchStrategy the search strategy to apply
     * @param discrepancy     the maximum discrepancy
     * @param <V>             the type of variables
     */
    public static <V extends Variable> void lds(Model aModel, AbstractStrategy<V> aSearchStrategy, int discrepancy) {
        aModel.set(new Resolver(aModel, new PropagateBasic(), new LearnNothing(),
                new MoveBinaryLDS(aSearchStrategy, discrepancy, aModel.getEnvironment())));
    }

    /**
     * Depth-bounded Discrepancy Search[1] algorithms with binary decisions and no learning.
     * <p>
     * [1]:T. Walsh, Depth-bounded Discrepancy Search, IJCAI-97.
     *
     * The current search loop (if any) will be replaced by this one after that call.
     * The current search strategy (if any) will also be replaced by the input one.
     *
     * @param aModel         the target model
     * @param aSearchStrategy the search strategy to apply
     * @param discrepancy     the maximum discrepancy
     * @param <V>             the type of variables
     */
    public static <V extends Variable> void dds(Model aModel, AbstractStrategy<V> aSearchStrategy, int discrepancy) {
        aModel.set(new Resolver(aModel, new PropagateBasic(), new LearnNothing(),
                new MoveBinaryDDS(aSearchStrategy, discrepancy, aModel.getEnvironment())));
    }

    /**
     * Hybrid Best-First Search[1] algorithms with binary decisions and no learning.
     * <p>
     * [1]:D. Allouche, S. de Givry, G. Katsirelos, T. Schiex, M. Zytnicki,
     * Anytime Hybrid Best-First Search with Tree Decomposition for Weighted CSP, CP-2015.
     *
     *
     * The current search loop (if any) will be replaced by this one after that call.
     * The current search strategy (if any) will also be replaced by the input one.
     *
     * @param aModel         the target model
     * @param aSearchStrategy the search strategy to apply
     * @param a               lower bound to limit the rate of redundantly propagated decisions
     * @param b               upper bound to limit the rate of redundantly propagated decisions.
     * @param N               backtrack limit for each DFS try, should be large enough to limit redundancy
     * @param <V>             the type of variables
     */
    public static <V extends Variable> void hbfs(Model aModel, AbstractStrategy<V> aSearchStrategy,
                                                 double a, double b, long N) {
        aModel.set(new Resolver(aModel, new PropagateBasic(), new LearnNothing(),
                new MoveBinaryHBFS(aModel, aSearchStrategy, a, b, N)));
    }

    /**
     * Combines many Moves. They are considered sequentially.
     * This is a work-in-progress and it may lead to unexpected behavior when repair() is applied.
     * When the selected Move cannot be extended (resp. repaired), the following one (wrt to the input order) is selected.
     * @param moves a list of moves.
     * @param <V> type of variables
     */
    public static <V extends Variable> void seq(Model aModel, Move... moves) {
        aModel.getResolver().setMove(new MoveSeq(aModel, moves));
    }

    //****************************************************************************************************************//
    //***********************************  MOVE ***********************************************************************//
    //****************************************************************************************************************//

    /**
     * Fit the <code>aSearchLoop</code> with a restart strategy.
     * It encapsulates the current move within a restart move.
     * Every time the <code>restartCriterion</code> is met, a restart is done, the new restart limit is updated
     * thanks to <code>restartStrategy</code>.
     * There will be at most <code>restartsLimit</code> restarts.
     *
     * @param aModel         the target model
     * @param restartCriterion the restart criterion, that is, the condition which triggers a restart
     * @param restartStrategy  the way restart limit (evaluated in <code>restartCriterion</code>) is updated, that is, computes the next limit
     * @param restartsLimit    number of allowed restarts
     */
    public static void restart(Model aModel, LongCriterion restartCriterion, IRestartStrategy restartStrategy,
                               int restartsLimit) {
        Move currentMove = aModel.getResolver().getMove();
        aModel.getResolver().setMove(new MoveRestart(currentMove, restartStrategy, restartCriterion, restartsLimit));
    }

    /**
     * Fit the <code>aSearchLoop</code> with a restart strategy triggered on solutions.
     * It encapsulates the current move within a restart move.
     * Every time a solution is found, a restart is done.
     *
     * @param aModel         the target model
     */
    public static void restartOnSolutions(Model aModel) {
        Move currentMove = aModel.getResolver().getMove();
        aModel.getResolver().setMove(
                new MoveRestart(currentMove,
                        new MonotonicRestartStrategy(1),
                        new SolutionCounter(aModel, 1),
                        Integer.MAX_VALUE));
    }

    /**
     * Fit the <code>aSearchLoop</code> with a Large Neighborhood Search.
     * It encapsulates the current move within a LNS move.
     * Anytime a solution is encountered, it is recorded and serves as a basis for the <code>neighbor</code>.
     * The <code>neighbor</code> creates a <i>fragment</i>: selects variables to freeze/unfreeze wrt the last solution found.
     * If a fragment cannot be extended to a solution, a new one is selected by restarting the search.
     * If a fragment induces a search space which a too big to be entirely evaluated, restarting the search can be forced
     * using the <code>restartCriterion</code>. A fast restart strategy is often a good choice.
     *
     * @param aModel         the target model
     * @param neighbor         the neighbor for the LNS
     * @param restartCounter the (fast) restart counter. Initial limit gives the frequency.
     */
    public static void lns(Model aModel, INeighbor neighbor, ICounter restartCounter) {
        Move currentMove = aModel.getResolver().getMove();
        aModel.getResolver().setMove(new MoveLNS(currentMove, neighbor, restartCounter));
    }


    /**
     * Fit the <code>aSearchLoop</code> with a Large Neighborhood Search.
     * It encapsulates the current move within a LNS move.
     * Anytime a solution is encountered, it is recorded and serves as a basis for the <code>neighbor</code>.
     * The <code>neighbor</code> creates a <i>fragment</i>: selects variables to freeze/unfreeze wrt the last solution found.
     * If a fragment cannot be extended to a solution, a new one is selected by restarting the search.
     *
     * @see #lns(Model, INeighbor, ICounter)
     * @param aModel         the target model
     * @param neighbor         the neighbor for the LNS
     */
    public static void lns(Model aModel, INeighbor neighbor) {
        Move currentMove = aModel.getResolver().getMove();
        aModel.getResolver().setMove(new MoveLNS(currentMove, neighbor, ICounter.Impl.None));
    }

    /**
     * Equips the <code>aSearchLoop</code> with a tabu decision-repair (TDR) algorithm.
     * It encapsulates the current move into a TDR algorithm and add a learn component to explain failure.
     * Anytime a conflict occurred, its explanation is computed and the decision sub-path in conflict is recorded
     * into a tabu list of size <code>tabuListSize</code>.
     * Then, a neighbor of the decision path is computed in order to be compatible with the conflicts stored in the tabu list.
     *
     * If no neighbor can be found, the search ends; the search may not be complete though.
     *
     * @param aModel the target model
     * @param tabuListSize the size of the tabu list which records conflicts.
     */
    private static void tabuDecisionRepair(Model aModel, int tabuListSize) {
        // TODO: incomplete, have to deal with gamma when extending
//        Move currentMove = aModel.getResolver().getMove();
//        MoveLearnBinaryTDR tdr = new MoveLearnBinaryTDR(aModel, currentMove, tabuListSize);
//        aModel.getResolver().setMove(tdr);
//        aModel.getResolver().setLearn(tdr);
    }

    //****************************************************************************************************************//
    //***********************************  LEARN *********************************************************************//
    //****************************************************************************************************************//

    /**
     * Conflict-based Backjumping (CBJ) explanation strategy.
     * It backtracks up to the most recent decision involved in the explanation, and forget younger decisions.
     * @param aModel         the target model
     * @param nogoodsOn set to true to extract nogoods from failures
     * @param userFeedbackOn set to true to record the propagation in conflict
     *                       (only relevant when one wants to interpret the explanation of a failure).
     * @see org.chocosolver.solver.explanations.ExplanationFactory#CBJ
     */
    public static void learnCBJ(Model aModel, boolean nogoodsOn, boolean userFeedbackOn) {
        if (!(aModel.getResolver().getLearn() instanceof LearnCBJ)) {
            aModel.getResolver().setLearn(new LearnCBJ(aModel, nogoodsOn, userFeedbackOn));
        }
    }

    /**
     * Dynamic ExplanConflict-based Backjumping (CBJ) explanation strategy.
     * It backtracks up to most recent decision involved in the explanation, keep unrelated ones.
     * @param aModel         the target model
     * @param nogoodsOn set to true to extract nogoods from failures
     * @param userFeedbackOn set to true to record the propagation in conflict
     *                       (only relevant when one wants to interpret the explanation of a failure).
     * @see org.chocosolver.solver.explanations.ExplanationFactory#CBJ
     */
    public static void learnDBT(Model aModel, boolean nogoodsOn, boolean userFeedbackOn) {
        if (!(aModel.getResolver().getLearn() instanceof LearnDBT)) {
            aModel.getResolver().setLearn(new LearnDBT(aModel, nogoodsOn, userFeedbackOn));
        }
    }
}
