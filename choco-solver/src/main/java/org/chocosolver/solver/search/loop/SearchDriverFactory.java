/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.loop.plm.*;
import org.chocosolver.solver.search.restart.AbstractRestartStrategy;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.criteria.Criterion;
import org.chocosolver.util.criteria.LongCriterion;

/**
 * A search driver factory which provides common search drivers, or services to modify the current one.
 * Created by cprudhom on 06/10/15.
 * Project: choco.
 */
public class SearchDriverFactory {

    SearchDriverFactory() {
    }

    /**
     * Create a Depth First Search algorithms with binary decisions and no learning.
     *
     * @param aSolver         the target solver
     * @param aSearchStrategy the search strategy to apply
     * @param <V>             the type of variables
     * @return a search driver to be set in Solver.
     */
    public static <V extends Variable> SearchDriver dfs(Solver aSolver, AbstractStrategy<V> aSearchStrategy) {
        return new SearchDriver(aSolver, new PropagateBasic(), new LearnNothing(), new MoveBinaryDFS(aSearchStrategy));
    }

    /**
     * Limited Discrepancy Search[1] algorithms with binary decisions and no learning.
     * <p>
     * [1]:W.D. Harvey and M.L.Ginsberg, Limited Discrepancy Search, IJCAI-95.
     *
     * @param aSolver         the target solver
     * @param aSearchStrategy the search strategy to apply
     * @param discrepancy     the maximum discrepancy
     * @param <V>             the type of variables
     * @return a search driver to be set in Solver.
     */
    public static <V extends Variable> SearchDriver lds(Solver aSolver, AbstractStrategy<V> aSearchStrategy, int discrepancy) {
        return new SearchDriver(aSolver, new PropagateBasic(), new LearnNothing(),
                new MoveBinaryLDS(aSearchStrategy, discrepancy, aSolver.getEnvironment()));
    }

    /**
     * Depth-bounded Discrepancy Search[1] algorithms with binary decisions and no learning.
     * <p>
     * [1]:T. Walsh, Depth-bounded Discrepancy Search, IJCAI-97.
     *
     * @param aSolver         the target solver
     * @param aSearchStrategy the search strategy to apply
     * @param discrepancy     the maximum discrepancy
     * @param <V>             the type of variables
     * @return a search driver to be set in Solver.
     */
    public static <V extends Variable> SearchDriver dds(Solver aSolver, AbstractStrategy<V> aSearchStrategy, int discrepancy) {
        return new SearchDriver(aSolver, new PropagateBasic(), new LearnNothing(),
                new MoveBinaryDDS(aSearchStrategy, discrepancy, aSolver.getEnvironment()));
    }

    //****************************************************************************************************************//
    //***********************************  MOVE ***********************************************************************//
    //****************************************************************************************************************//

    /**
     * Fit the <code>aSearchDriver</code> with a restart strategy.
     * It encapsulates the current move within a restart move.
     * Every time the <code>restartCriterion</code> is met, a restart is done, the new restart limit is updated
     * thanks to <code>restartStrategy</code>.
     * There will be at most <code>restartsLimit</code> restarts.
     *
     * @param aSearchDriver    the search driver to modify
     * @param restartCriterion the restart criterion, that is, the condition which triggers a restart
     * @param restartStrategy  the way restart limit (evaluated in <code>restartCriterion</code>) is updated, that is, computes the next limit
     * @param restartsLimit    number of allowed restarts
     * @return the modified search driver
     */
    public static SearchDriver restart(SearchDriver aSearchDriver, LongCriterion restartCriterion, AbstractRestartStrategy restartStrategy,
                                       int restartsLimit) {
        Move currentMove = aSearchDriver.getMove();
        aSearchDriver.setMove(new MoveRestart(currentMove, restartStrategy, restartCriterion, restartsLimit));
        return aSearchDriver;
    }


    /**
     * Fit the <code>aSearchDriver</code> with a Large Neighborhood Search.
     * It encapsulates the current move within a LNS move.
     * Anytime a solution is encountered, it is recorded and serves as a basis for the <code>neighbor</code>.
     * The <code>neighbor</code> creates a <i>fragment</i>: selects variables to freeze/unfreeze wrt the last solution found.
     * If a fragment cannot be extended to a solution, a new one is selected by restarting the search.
     * If a fragment induces a search space which a too big to be entirely evaluated, restarting the search can be forced
     * using the <code>restartCriterion</code>. A fast restart strategy is often a good choice.
     *
     * @param aSearchDriver    the search driver to modify
     * @param neighbor         the neighbor for the LNS
     * @param restartCriterion the (fast) restart criterion
     * @return the modified search driver
     */
    public static SearchDriver lns(SearchDriver aSearchDriver, INeighbor neighbor, Criterion restartCriterion) {
        Move currentMove = aSearchDriver.getMove();
        aSearchDriver.setMove(new MoveLNS(currentMove, neighbor, restartCriterion));
        return aSearchDriver;
    }

    /**
     * Limit the exploration of the search space with the help of a <code>aStopCriterion</code>.
     * When the condition depicted in the criterion is met,
     * the search stops.
     *
     * @param aSearchDriver  the search driver to modify
     * @param aStopCriterion the stop criterion which, when met, stops the search.
     * @return the modified search driver.
     */
    public static SearchDriver limitSearch(SearchDriver aSearchDriver, Criterion aStopCriterion) {
        aSearchDriver.setStopCriterion(aStopCriterion);
        return aSearchDriver;
    }

    //****************************************************************************************************************//
    //***********************************  LEARN *********************************************************************//
    //****************************************************************************************************************//

    /**
     * Record nogoods from restarts.
     * Any time the search restart to the root node, nogoods are extracted from the last visited branch.
     * <p>
     * It does not check whether or not a restart strategy has been declared.
     *
     * @param aSearchDriver the search driver to modify
     * @return a modified search driver
     */
    public static SearchDriver learnNogoodFromRestarts(SearchDriver aSearchDriver) {
        aSearchDriver.setLearn(new LearnNogoodFromRestarts(aSearchDriver.getSolver()));
        return aSearchDriver;
    }

    /**
     * Record nogoods from solutions.
     * Any time a solution is found, a nogood is extracted from the set of variables in parameter.
     *
     * @param aSearchDriver the search driver to modify
     * @return a modified search driver
     */
    public static SearchDriver learnNogoodFromSolutions(SearchDriver aSearchDriver, IntVar[] variables) {
        aSearchDriver.setLearn(new LearnNogoodFromSolutions(variables));
        return aSearchDriver;
    }

    /**
     * Record nogoods from failures.
     * This is only relevant with associated with {@link SearchDriverFactory#lds(Solver, AbstractStrategy, int)}.
     * Any time the search fails, a nogood is extracted from the current visited branch.
     * Only IntDecision with assignment or refutation operator are handled.
     * <p>
     * It does not check whether or not a restart strategy has been declared.
     *
     * @param aSearchDriver the search driver to modify
     * @return a modified search driver
     */
    public static SearchDriver learnNogoodFromFailures(SearchDriver aSearchDriver) {
        aSearchDriver.setLearn(new LearnNogoodFromFailures(aSearchDriver.getSolver()));
        return aSearchDriver;
    }
}
