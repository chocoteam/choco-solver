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

import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;

import java.util.Collections;
import java.util.List;

/**
 * This {@link Move}'s implementation defines a Lager Neighborhood Search.
 * Created by cprudhom on 03/09/15.
 * Project: choco.
 */
public class MoveLNS implements Move {

    protected Move move; // the strategy required to complete the generated fragment
    protected INeighbor neighbor;

    protected long solutions; // nb solutions found so far
    boolean freshRestart;
    ICounter counter;
    long frequency;

    public MoveLNS(Move move, INeighbor neighbor, ICounter restartCounter) {
        this.move = move;
        this.neighbor = neighbor;
        this.counter = restartCounter;
        this.frequency = counter.getLimitValue();
        this.solutions = 0;
        this.freshRestart = false;
    }

    @Override
    public boolean init() {
        neighbor.init();
        return move.init();
    }

    /**
     * Return false when:
     * <ul>
     * <li>
     * the underlying search has no more decision to provide,
     * </li>
     * </ul>
     * <p>
     * Return true when:
     * <ul>
     * <li>
     * a new neighbor is provided,
     * </li>
     * <li>
     * or a new decision is provided by the underlying decision
     * </li>
     * <li>
     * or the fast restart criterion is met.
     * </li>
     * </ul>
     * <p>
     * Restart when:
     * <ul>
     * <li>
     * a restart criterion is met
     * </li>
     * </ul>
     *
     * @param searchLoop SearchLoop
     * @return true if the decision path is extended
     */
    @Override
    public boolean extend(SearchLoop searchLoop) {
        boolean extend;
        // when a new fragment is needed (condition: at least one solution has been found)
        if (solutions > 0) {
            if (freshRestart) {
                Decision tmp = searchLoop.decision;
                assert tmp == RootDecision.ROOT;
                searchLoop.decision = neighbor.fixSomeVariables();
                searchLoop.decision.setPrevious(tmp);
                searchLoop.mSolver.getEnvironment().worldPush();
                freshRestart = false;
                extend = true;
            } else {
                // if fast restart is on
                if (counter.isMet()) {
                    // then is restart is triggered
                    doRestart(searchLoop);
                    extend = true;
                } else {
                    extend = move.extend(searchLoop);
                }
            }
        } else {
            extend = move.extend(searchLoop);
        }
        return extend;
    }

    /**
     * Return false when :
     * <ul>
     * <li>
     * move.repair(searchLoop) returns false and neighbor is complete.
     * </li>
     * <li>
     * posting the cut at root node fails
     * </li>
     * </ul>
     * Return true when:
     * <ul>
     * <li>
     * move.repair(searchLoop) returns true,
     * </li>
     * <li>
     * or move.repair(searchLoop) returns false and neighbor is not complete,
     * </li>
     * </ul>
     * <p>
     * Restart when:
     * <ul>
     * <li>
     * a new solution has been found
     * </li>
     * <li>
     * move.repair(searchLoop) returns false and neighbor is not complete,
     * </li>
     * <li>
     * or the fast restart criterion is met
     * </li>
     * </ul>
     *
     * @param searchLoop SearchLoop
     * @return true if the decision path is repaired
     */
    @Override
    public boolean repair(SearchLoop searchLoop) {
        boolean repair;
        if(solutions > 0
                // the second condition is only here for intiale calls, when solutions is not already up to date
                || searchLoop.mSolver.getMeasures().getSolutionCount() > 0) {
            // the detection of a new solution can only be met here
            if (solutions < searchLoop.mSolver.getMeasures().getSolutionCount()) {
                assert solutions == searchLoop.mSolver.getMeasures().getSolutionCount() - 1;
                solutions++;
                neighbor.recordSolution();
                doRestart(searchLoop);
                repair = true;
            }
            // when posting the cut directly at root node fails
            else if (freshRestart) {
                repair = false;
            }
            // the current sub-tree has been entirely explored
            else if (!(repair = move.repair(searchLoop))) {
                // but the neighbor cannot ensure completeness
                if (!neighbor.isSearchComplete()) {
                    // then a restart is triggered
                    doRestart(searchLoop);
                    repair = true;
                }
            }
            // or a fast restart is on
            else if (counter.isMet()) {
                // then is restart is triggered
                doRestart(searchLoop);
                repair = true;
            }
        }else{
            repair = move.repair(searchLoop);
        }
        return repair;
    }

    @Override
    public void setTopDecision(Decision topDecision) {
        this.move.setTopDecision(topDecision);
    }

    @Override
    public <V extends Variable> AbstractStrategy<V> getStrategy() {
        return move.getStrategy();
    }

    @Override
    public <V extends Variable> void setStrategy(AbstractStrategy<V> aStrategy) {
        move.setStrategy(aStrategy);
    }

    /**
     * Extend the neighbor when conditions are met and do the restart
     *
     * @param searchLoop SearchLoop
     */
    private void doRestart(SearchLoop searchLoop) {
        if (freshRestart) {
            neighbor.restrictLess();
        }
        freshRestart = true;
        counter.overrideLimit(counter.getLimitValue() + frequency);
        searchLoop.restart();
    }

    @Override
    public List<Move> getChildMoves() {
        return Collections.singletonList(move);
    }

    @Override
    public void setChildMoves(List<Move> someMoves) {
        if(someMoves.size() == 1) {
            this.move = someMoves.get(0);
        }else{
            throw new UnsupportedOperationException("Only one child move can be attached to it.");
        }
    }
}
