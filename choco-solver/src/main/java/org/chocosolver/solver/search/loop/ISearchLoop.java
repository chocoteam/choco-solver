/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.loop.monitors.ISearchMonitor;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorList;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;

import java.io.Serializable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 6 oct. 2010
 */
public interface ISearchLoop extends Serializable {

    static final int INIT = 0;
    static final int INITIAL_PROPAGATION = 1;
    static final int OPEN_NODE = 1 << 1;
    static final int DOWN_LEFT_BRANCH = 1 << 2;
    static final int DOWN_RIGHT_BRANCH = 1 << 3;
    static final int UP_BRANCH = 1 << 4;
    static final int RESTART = 1 << 5;

    static final String MSG_LIMIT = "a limit has been reached";
    static final String MSG_ROOT = "the entire search space has been explored";
    static final String MSG_CUT = "applying the cut leads to a failure";
    static final String MSG_FIRST_SOL = "stop at first solution";
    static final String MSG_INIT = "failure encountered during initial propagation";
    static final String MSG_SEARCH_INIT = "search strategy detects inconsistency";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// RESOLUTION /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Launch the resolution of the problem described in a Solver.
     *
     * @param stopAtFirst should stop at first solution
     */
    void launch(boolean stopAtFirst);

    /**
     * Resume the resolution of the problem described in a Solver.
     */
    void resume();

    /**
     * This method enables to solve a problem another time:
     * <ul>
     * <li>It backtracks up to the root node of the search tree,</li>
     * <li>it sets the objective manager to null,</li>
     * <li>it resets the measures to 0,</li>
     * <li>and sets the propagation engine to NO_NoPropagationEngine.</li>
     * </ul>
     */
    void reset();

    /**
     * Sets the following action in the search to be a restart instruction.
     * Note that the restart may not be immediate
     */
    void restart();

    /**
     * Retrieves the state of the root node (after the initial propagation)
     * Has an immediate effect
     */
    void restoreRootNode();

    /**
     * Force the search to stop
     *
     * @param msgNgood a message to motivate the interruption -- for logging only
     * @param voidable is the interruption weak, or not
     */
    void interrupt(String msgNgood, boolean voidable);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// SETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Branch a search monitor
     *
     * @param sm a search monitor
     */
    void plugSearchMonitor(ISearchMonitor sm);

    void set(AbstractStrategy strategy);

    void setObjectiveManager(ObjectiveManager om);

    void reachLimit(boolean voidable);

    /**
     * Complete (or not) the declared search strategy with one over all variables
     *
     * @param isComplete set to true to complete the current search strategy
     */
    void makeCompleteStrategy(boolean isComplete);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// GETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ObjectiveManager getObjectiveManager();

    AbstractStrategy getStrategy();

    boolean hasReachedLimit();

    boolean hasEndedUnexpectedly();

    boolean isComplete();

    boolean canBeResumed();

    @Deprecated
    int getTimeStamp();

    Decision getLastDecision();

    int getCurrentDepth();

    /**
     * Indicate if the default searcg strategy is used
     *
     * @return false if a search strategy is used
     */
    boolean isDefaultSearchUsed();

    /**
     * Indicate if the search strategy is completed with one over all variables
     *
     * @return false if no strategy over all variables complete the declared one
     */
    boolean isSearchCompleted();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// TEMPORARY //////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    SearchMonitorList getSMList();

    void forceAlive(boolean b);

    void setLastDecision(Decision cobdec);

    void overridePreviousWorld(int i);

    void moveTo(int nextState);

    int getSearchWorldIndex();
}
