/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.move;

import org.chocosolver.cutoffseq.ICutoffStrategy;
import org.chocosolver.solver.Solver;
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
 * @author Charles Prud'homme
 * @since 03/09/2015
 */
public class MoveRestart implements Move {

    /**
     * the default {@link Move} to execute when no restart has to be done
     */
    private Move move;
    /**
     * How often the restart should occur
     */
    private ICutoffStrategy restartStrategy;
    /**
     * How to trigger a restart
     */
    private LongCriterion criterion;
    /**
     * Count the number of restarts
     */
    private int restartFromStrategyCount;
    /**
     * restrict the total number of restart
     */
    private int restartLimit;
    /**
     * When the next restart should be triggered
     */
    private long limit;

    /**
     * @param move            the default {@link Move} to execute when no restart has to be done
     * @param restartStrategy defines when restarts happen
     * @param criterion       defines how to trigger a restart
     * @param restartLimit    restrict the total number of restart
     */
    public MoveRestart(Move move, ICutoffStrategy restartStrategy, LongCriterion criterion, int restartLimit) {
        this.move = move;
        this.restartStrategy = restartStrategy;
        this.criterion = criterion;
        this.restartLimit = restartLimit;
    }

    @Override
    public boolean init() {
        restartFromStrategyCount = 0;
        limit = restartStrategy.getNextCutoff();
        return move.init();
    }

    @Override
    public boolean extend(Solver solver) {
        boolean extend;
        if (!criterion.isMet(limit)) {
            extend =  move.extend(solver);
        }else{
            restart(solver);
            extend = true;
        }
        return extend;
    }

    @Override
    public boolean repair(Solver solver) {
        boolean repair;
        if (!criterion.isMet(limit)) {
            repair =  move.repair(solver);
        }else{
            restart(solver);
            repair = true;
        }
        return repair;
    }

    @Override
    public void setTopDecisionPosition(int position) {
        this.move.setTopDecisionPosition(position);
    }

    @Override
    public <V extends Variable> AbstractStrategy<V> getStrategy() {
        return move.getStrategy();
    }

    @Override
    public <V extends Variable> void setStrategy(AbstractStrategy<V> aStrategy) {
        move.setStrategy(aStrategy);
    }

    @Override
    public void removeStrategy() {
        move.removeStrategy();
    }

    /**
     * Execute the restart and update measures
     * @param solver reference to the solver
     */
    protected void restart(Solver solver) {
        // update parameters for restarts
        restartFromStrategyCount++;
        if (restartFromStrategyCount >= restartLimit) {
            limit = Long.MAX_VALUE;
        } else if(criterion.isMet(limit)){
            limit += restartStrategy.getNextCutoff();
        }
        // then do the restart
        solver.restart();
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
