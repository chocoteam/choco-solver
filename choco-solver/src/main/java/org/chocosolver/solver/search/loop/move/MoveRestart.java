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

import org.chocosolver.solver.Resolver;
import org.chocosolver.solver.search.restart.IRestartStrategy;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.criteria.LongCriterion;

import java.util.Collections;
import java.util.List;

/**
 * This {@link Move} implementation enables restarting a search on certain conditions
 * (most of the time based on a counter). It is not self-content
 * and needs a underlying {@link Move} to delegate common movements when no restart is needed.
 * <p>
 * Created by cprudhom on 03/09/15.
 * Project: choco.
 */
public class MoveRestart implements Move {

    Move move;
    IRestartStrategy restartStrategy;
    LongCriterion criterion;
    int restartFromStrategyCount, restartLimit;
    long limit;

    /**
     * @param move            the default {@link Move} to execute when no restart has to be done
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
        limit = restartStrategy.getFirstCutOff();
        return move.init();
    }

    @Override
    public boolean extend(Resolver resolver) {
        boolean extend;
        if (!criterion.isMet(limit)) {
            extend =  move.extend(resolver);
        }else{
            restart(resolver);
            extend = true;
        }
        return extend;
    }

    @Override
    public boolean repair(Resolver resolver) {
        boolean repair;
        if (!criterion.isMet(limit)) {
            repair =  move.repair(resolver);
        }else{
            restart(resolver);
            repair = true;
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

    protected void restart(Resolver resolver) {
        // update parameters for restarts
        restartFromStrategyCount++;
        if (restartFromStrategyCount >= restartLimit) {
            limit = Long.MAX_VALUE;
        } else if(criterion.isMet(limit)){
            limit += restartStrategy.getNextCutoff(restartFromStrategyCount);
        }
        // then do the restart
        resolver.restart();
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
