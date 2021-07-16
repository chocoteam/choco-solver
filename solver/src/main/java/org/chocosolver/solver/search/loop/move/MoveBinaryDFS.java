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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;

import java.util.Collections;
import java.util.List;

/**
 * A move dedicated to run a Depth First Search with binary decisions.
 * <p>
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 3.3.1
 */
public class MoveBinaryDFS implements Move {

    /**
     * Search strategy to extend the search tree
     */
    protected AbstractStrategy strategy;
    /**
     * Index, in the decision path, of the decision taken just before selecting this move.
     */
    protected int topDecisionPosition;

    /**
     * Create this move without any search strategy
     */
    public MoveBinaryDFS(){
        this(null);
    }

    /**
     * Create this move with a search strategy
     * @param strategy a search strategy
     */
    public MoveBinaryDFS(AbstractStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public boolean init() {
        return strategy.init();
    }

    @Override
    public boolean extend(Solver solver) {
        boolean extended = false;
        Decision current = strategy.getDecision();
        if (current != null) { // null means there is no more decision
            solver.getDecisionPath().pushDecision(current);
            solver.getEnvironment().worldPush();
            extended = true;
        }
        return extended;
    }

    @Override
    public boolean repair(Solver solver) {
        solver.getMeasures().incBackTrackCount();
        solver.getEnvironment().worldPop();
        return rewind(solver);
    }

    @Override
    public void setTopDecisionPosition(int position) {
        this.topDecisionPosition = position;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Variable> AbstractStrategy<V> getStrategy() {
        return strategy;
    }

    @Override
    public <V extends Variable> void setStrategy(AbstractStrategy<V> aStrategy) {
        removeStrategy();
        this.strategy = aStrategy;
    }

    @Override
    public void removeStrategy() {
        if(this.strategy!=null){
            this.strategy.remove();
        }
    }

    /**
     * Go back in the search tree. Either refute a decision, or backtrack.
     * @param solver reference to the solver
     * @return {@code true} if a reparation has been found
     */
    protected boolean rewind(Solver solver) {
        boolean repaired = false;
        Decision head = solver.getDecisionPath().getLastDecision();
        while (!repaired && head.getPosition() != topDecisionPosition) {
            solver.setJumpTo(solver.getJumpTo()-1);
            if (solver.getJumpTo() <= 0 && head.hasNext()) {
                solver.getEnvironment().worldPush();
                repaired = true;
            } else {
                prevDecision(solver);
            }
            head = solver.getDecisionPath().getLastDecision();
        }
        return repaired;
    }

    /**
     * Backtrack in the search tree
     * @param solver reference to the solver
     */
    protected void prevDecision(Solver solver) {
        solver.getDecisionPath().synchronize();
        // goes up in the search tree and makes sure search monitors are correctly informed
        solver.getSearchMonitors().afterUpBranch();
        solver.getMeasures().incBackTrackCount();
        solver.getEnvironment().worldPop();
        solver.getSearchMonitors().beforeUpBranch();
    }

    @Override
    public List<Move> getChildMoves() {
        return Collections.emptyList();
    }

    @Override
    public void setChildMoves(List<Move> someMoves) {
        if(someMoves.size() > 0) {
            throw new UnsupportedOperationException("This is a terminal Move. No child move can be attached to it.");
        }
    }
}
