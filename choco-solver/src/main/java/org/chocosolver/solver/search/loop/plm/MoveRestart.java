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

import org.chocosolver.solver.search.restart.IRestartStrategy;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.criteria.LongCriterion;

/**
 * This {@link org.chocosolver.solver.search.loop.plm.Move} implementation enables restarting a search on certain conditions
 * (most of the time based on a counter). It is not self-content
 * and needs a underlying {@link org.chocosolver.solver.search.loop.plm.Move} to delegate common movements when no restart is needed.
 * <p>
 * Created by cprudhom on 03/09/15.
 * Project: choco.
 */
public class MoveRestart implements Move {

    Move move;
    IRestartStrategy restartStrategy;
    LongCriterion criterion;
    int restartFromStrategyCount, restartCutoff, restartLimit;
    long limit;

    /**
     * @param move            the default {@link org.chocosolver.solver.search.loop.plm.Move} to execute when no restart has to be done
     * @param restartStrategy defines when restarts happen
     * @param criterion       defines how to trigger a restart
     * @param restartLimit    restrict the total number of restart
     */
    public MoveRestart(Move move, IRestartStrategy restartStrategy, LongCriterion criterion, int restartLimit) {
        this.move = move;
        this.restartStrategy = restartStrategy;
        this.criterion = criterion;
        this.restartLimit = restartLimit;
    }

    @Override
    public boolean init() {
        restartFromStrategyCount = 0;
        limit = restartCutoff = restartStrategy.getScaleFactor();
        return move.init();
    }

    @Override
    public boolean extend(SearchDriver searchDriver) {
        if (!criterion.isMet(limit)) {
            return move.extend(searchDriver);
        }
        restart(searchDriver);
        return true;
    }

    @Override
    public boolean repair(SearchDriver searchDriver) {
        if (!criterion.isMet(limit)) {
            return move.repair(searchDriver);
        }
        restart(searchDriver);
        return true;
    }

    @Override
    public <V extends Variable> AbstractStrategy<V> getStrategy() {
        return move.getStrategy();
    }

    @Override
    public <V extends Variable> void setStrategy(AbstractStrategy<V> aStrategy) {
        move.setStrategy(aStrategy);
    }

    protected void restart(SearchDriver searchDriver) {
        // update parameters for restarts
        restartFromStrategyCount++;
        restartCutoff = restartStrategy.getNextCutoff(restartFromStrategyCount);
        if (restartFromStrategyCount >= restartLimit) {
            limit = Long.MAX_VALUE;
        } else {
            limit += restartCutoff;
        }
        // then do the restart
        searchDriver.restart();
    }
}
