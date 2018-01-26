/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.move;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;

import java.util.Collections;
import java.util.List;

/**
 * This {@link Move}'s implementation defines a Large Neighborhood Search.
 * Created by cprudhom on 03/09/15.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 03/09/2015
 */
public class MoveLNS implements Move {

    /**
     * the strategy required to complete the generated fragment
     */
    protected Move move;
    /**
     * Neighbor to used
     */
    protected INeighbor neighbor;
    /**
     * Number of solutions found so far
     */
    protected long solutions;
    /**
     * Indicate a restart has been triggered
     */
    protected boolean freshRestart;
    /**
     * Restart counter
     */
    protected ICounter counter;
    /**
     * For restart strategy
     */
    protected long frequency;

    /**
     * Create a move which defines a Large Neighborhood Search.
     * @param move how the subtree is explored
     * @param neighbor how the fragment are computed
     * @param restartCounter when a restart should occur
     */
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
     * @param solver SearchLoop
     * @return true if the decision path is extended
     */
    @Override
    public boolean extend(Solver solver) {
        boolean extend;
        // when a new fragment is needed (condition: at least one solution has been found)
        if (solutions > 0) {
            if (freshRestart) {
                assert solver.getDecisionPath().size() == 1;
                assert solver.getDecisionPath().getDecision(0) == RootDecision.ROOT;
                neighbor.fixSomeVariables(solver.getDecisionPath());
                solver.getEnvironment().worldPush();
                freshRestart = false;
                extend = true;
            } else {
                // if fast restart is on
                if (counter.isMet()) {
                    // then is restart is triggered
                    doRestart(solver);
                    extend = true;
                } else {
                    extend = move.extend(solver);
                }
            }
        } else {
            extend = move.extend(solver);
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
     * @param solver SearchLoop
     * @return true if the decision path is repaired
     */
    @Override
    public boolean repair(Solver solver) {
        boolean repair;
        if(solutions > 0
                // the second condition is only here for intiale calls, when solutions is not already up to date
                || solver.getSolutionCount() > 0) {
            // the detection of a new solution can only be met here
            if (solutions < solver.getSolutionCount()) {
                assert solutions == solver.getSolutionCount() - 1;
                solutions++;
                neighbor.recordSolution();
                doRestart(solver);
                repair = true;
            }
            // when posting the cut directly at root node fails
            else if (freshRestart) {
                repair = false;
            }
            // the current sub-tree has been entirely explored
            else if (!(repair = move.repair(solver))) {
                // but the neighbor cannot ensure completeness
                if (!neighbor.isSearchComplete()) {
                    // then a restart is triggered
                    doRestart(solver);
                    repair = true;
                }
            }
            // or a fast restart is on
            else if (counter.isMet()) {
                // then is restart is triggered
                doRestart(solver);
                repair = true;
            }
        }else{
            repair = move.repair(solver);
        }
        return repair;
    }

    /**
     * Give an initial solution to begin with if called before executing the solving process
     * or erase the last recorded one otherwise.
     * @param solution a solution to record
     * @param solver that manages the LNS
     */
    public void loadFromSolution(Solution solution, Solver solver){
        neighbor.loadFromSolution(solution);
        if(solutions == 0){
            solutions++;
            freshRestart = true;
        }else{
            doRestart(solver);
        }
    }

    @Override
    public void setTopDecisionPosition(int position) {
        move.setTopDecisionPosition(position);
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
     * @param solver SearchLoop
     */
    protected void doRestart(Solver solver) {
        if (!freshRestart) {
            neighbor.restrictLess();
        }
        freshRestart = true;
        counter.overrideLimit(counter.currentValue() + frequency);
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
