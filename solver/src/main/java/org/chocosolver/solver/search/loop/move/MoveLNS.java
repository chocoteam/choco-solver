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

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils;

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
     * IntNeighbor to used
     */
    protected INeighbor neighbor;
    /**
     * Number of solutions found so far
     */
    protected long solutions;
    /**
     * Indicates if a solution has been loaded
     */
    protected boolean solutionLoaded;
    /**
     * Indicate a restart has been triggered
     */
    private boolean freshRestart;
    /**
     * Restart counter
     */
    protected ICounter counter;
    /**
     * For restart strategy
     */
    private long frequency;

    protected PropLNS prop;

    private boolean canApplyNeighborhood;

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
        this.solutionLoaded = false;
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
        if (solutions > 0 || solutionLoaded) {
            if (freshRestart) {
                assert solver.getDecisionPath().size() == 1;
                assert solver.getDecisionPath().getDecision(0) == RootDecision.ROOT;
                solver.getEnvironment().worldPush();
                if(prop == null){
                    prop = new PropLNS(solver.getModel().intVar(2));
                    new Constraint("LNS", prop).post();
                }
                solver.getEngine().propagateOnBacktrack(prop);
                canApplyNeighborhood = true;
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
        boolean repair = true;
        if(solutions > 0
                // the second condition is only here for intiale calls, when solutions is not already up to date
                || solver.getSolutionCount() > 0
                // the third condition is true when a solution was given as input
                || solutionLoaded) {
            // the detection of a new solution can only be met here
            if (solutions < solver.getSolutionCount()) {
                assert solutions == solver.getSolutionCount() - 1;
                solutions++;
                solutionLoaded = false;
                neighbor.recordSolution();
                doRestart(solver);
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
        solutionLoaded = true;
        if(solutions == 0){
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

    @Override
    public void removeStrategy() {
        move.removeStrategy();
    }

    /**
     * Extend the neighbor when conditions are met and do the restart
     *
     * @param solver SearchLoop
     */
    private void doRestart(Solver solver) {
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

    class PropLNS extends Propagator<IntVar>{

        PropLNS(IntVar var) {
            super(var);
            this.vars = new IntVar[0];
        }

        @Override
        public int getPropagationConditions(int vIdx) {
            return IntEventType.VOID.getMask();
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            if(canApplyNeighborhood) {
                canApplyNeighborhood = false;
                neighbor.fixSomeVariables();
            }
        }

        @Override
        public ESat isEntailed() {
            return ESat.TRUE;
        }

        @Override
        public void explain(int p, ExplanationForSignedClause explanation) {
            IntVar pivot = explanation.readVar(p);
            IntIterableRangeSet dom = explanation.complement(pivot);
            IntIterableSetUtils.unionOf(dom, explanation.readDom(p));
            pivot.intersectLit(dom, explanation);
        }
    }
}
