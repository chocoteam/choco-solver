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
package org.chocosolver.solver.search.loop.plm;

import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.criteria.Criterion;

/**
 * This {@link org.chocosolver.solver.search.loop.plm.Move}'s implementation defines a Lager Neighborhood Search.
 * Created by cprudhom on 03/09/15.
 * Project: choco.
 */
public class MoveLNS implements Move {

    Move move; // the strategy required to complete the generated fragment

    INeighbor neighbor;

    long solutions; // nb solutions found so far
    boolean freshRestart;
    Criterion criterion;
    long limit;

    public MoveLNS(Move move, INeighbor neighbor, Criterion restartCriterion) {
        this.move = move;
        this.neighbor = neighbor;
        this.criterion = restartCriterion;
        this.solutions = 0;
        this.freshRestart = true;
    }

    @Override
    public boolean init() {
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
     * @param searchDriver SearchDriver
     * @return true if the decision path is extended
     */
    @Override
    public boolean extend(SearchDriver searchDriver) {
        boolean extend;
        // when a new fragment is needed (condition: at least one solution has been found)
        if (solutions > 0) {
            if (freshRestart) {
                // todo: find a better way to do that
                Decision tmp = searchDriver.decision;
                assert tmp == RootDecision.ROOT;
                searchDriver.decision = neighbor.fixSomeVariables();
                searchDriver.decision.setPrevious(tmp);
                searchDriver.mSolver.getEnvironment().worldPush();
                freshRestart = false;
                extend = true;
            } else {
                // if fast restart is on
                if (criterion.isMet()) {
                    // then is restart is triggered
                    doRestart(searchDriver);
                    extend = true;
                } else {
                    extend = move.extend(searchDriver);
                }
            }
        } else {
            extend = move.extend(searchDriver);
        }
        return extend;
    }

    /**
     * Return false when :
     * <ul>
     * <li>
     * move.repair(searchDriver) returns false and neighbor is complete.
     * </li>
     * <li>
     * posting the cut at root node fails
     * </li>
     * </ul>
     * Return true when:
     * <ul>
     * <li>
     * move.repair(searchDriver) returns true,
     * </li>
     * <li>
     * or move.repair(searchDriver) returns false and neighbor is not complete,
     * </li>
     * </ul>
     * <p>
     * Restart when:
     * <ul>
     * <li>
     * a new solution has been found
     * </li>
     * <li>
     * move.repair(searchDriver) returns false and neighbor is not complete,
     * </li>
     * <li>
     * or the fast restart criterion is met
     * </li>
     * </ul>
     *
     * @param searchDriver SearchDriver
     * @return true if the decision path is repaired
     */
    @Override
    public boolean repair(SearchDriver searchDriver) {
        boolean repair;
        // the detection of a new solution can only be met here
        if (solutions < searchDriver.mSolver.getMeasures().getSolutionCount()) {
            assert solutions == searchDriver.mSolver.getMeasures().getSolutionCount() - 1;
            solutions++;
            neighbor.recordSolution();
            doRestart(searchDriver);
            repair = true;
        }
        // when posting the cut directly at root node fails
        else if (freshRestart) {
            repair = false;
        }
        // the current sub-tree has been entirely explored
        else if (!(repair = move.repair(searchDriver))) {
            // but the neighbor cannot ensure completeness
            if (!neighbor.isSearchComplete()) {
                // then a restart is triggered
                doRestart(searchDriver);
                repair = true;
            }
        }
        // or a fast restart is on
        else if (criterion.isMet()) {
            // then is restart is triggered
            doRestart(searchDriver);
        }
        return repair;
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
     * @param searchDriver SearchDriver
     */
    private void doRestart(SearchDriver searchDriver) {
        if (freshRestart) {
            neighbor.restrictLess();
        }
        searchDriver.restart();
        freshRestart = true;
    }

}
