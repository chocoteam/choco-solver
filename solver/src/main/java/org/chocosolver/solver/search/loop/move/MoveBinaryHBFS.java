/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.move;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.objective.IObjectiveManager;
import org.chocosolver.solver.search.limits.BacktrackCounter;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * A move dedicated to run an Hybrid Best-First Search[1] (HBFS) with binary decisions.
 * <p>
 * [1]:D. Allouche, S. de Givry, G. Katsirelos, T. Schiex, M. Zytnicki,
 * Anytime Hybrid Best-First Search with Tree Decomposition for Weighted CSP, CP-2015.
 * <p>
 * It restarts anytime a backtrack limit is reached and a new open right branch needs to be selected.
 * <p>
 * Created by cprudhom on 02/11/2015.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 02/11/2015.
 */
public class MoveBinaryHBFS extends MoveBinaryDFS {

    /**
     * limited number of backtracks for each DFS try
     */
    private final BacktrackCounter dfslimit;

    /**
     * limit of bracktracks for the next DFS try.
     */
    private long Z;

    /**
     * as limit is globally maintained, limit += Z at each try.
     */
    private long limit;
    /**
     * maximum number of backtracks to not exceed when updating node recomputation parameters.
     */
    private final long N;

    /**
     * for node recomputation.
     */
    private long nodesRecompute;

    /**
     * lower bound to limit the rate of redundantly propagated decisions.
     */
    private final double a;
    /**
     * upper bound to limit the rate of redundantly propagated decisions.
     */
    private final double b;

    /**
     * The current objective manager, to deal with best bounds.
     */
    private IObjectiveManager<IntVar> objectiveManager;

    /**
     * Indicates if the current resolution policy is minimization.
     */
    private boolean isMinimization;

    /**
     * list of open right branches.
     */
    private final PriorityQueue<Open> opens;

    /**
     * Current open right branch.
     */
    private Decision[] copen;

    /**
     * Used to find the first unknown open right branch
     */
    private final List<Decision> _unkopen;

    /**
     * Current decision in copen
     */
    private int current;

    /**
     * The owner model.
     */
    private final Model mModel;

    /**
     * Create a move dedicated to run an Hybrid Best-First Search[1] (HBFS) with binary decisions.
     * @param model a model
     * @param strategy the search strategy to use
     * @param a lower bound to limit the rate of redundantly propagated decisions.
     * @param b upper bound to limit the rate of redundantly propagated decisions.
     * @param N maximum number of backtracks to not exceed when updating node recomputation parameters.
     */
    public MoveBinaryHBFS(Model model, AbstractStrategy strategy, double a, double b, long N) {
        super(strategy);
        this.mModel = model;
        this.dfslimit = new BacktrackCounter(model, N);
        this.opens = new PriorityQueue<>();
        this.copen = new Decision[0];
        this.current = 0;
        this.Z = 1;
        this.limit = Z;
        this.N = N;
        this.a = a;
        this.b = b;
        this._unkopen = new ArrayList<>();
    }

    @Override
    public boolean init() {
        boolean init = super.init();
        this.objectiveManager = mModel.getSolver().getObjectiveManager();
        if (objectiveManager.getPolicy() == ResolutionPolicy.SATISFACTION) {
            throw new UnsupportedOperationException("HBFS is not adapted to satisfaction problems.");
        }
        isMinimization = objectiveManager.getPolicy() == ResolutionPolicy.MINIMIZE;
        return init;
    }

    @Override
    public boolean extend(Solver solver) {
        boolean extend;
        // as we observe the number of backtracks, no limit can be reached on extend()
        if (current < copen.length) {
            solver.getDecisionPath().pushDecision(copen[current++]);
            solver.getEnvironment().worldPush();
            extend = true;
        } else /*cut will checker with propagation */ {
            extend = super.extend(solver);
        }
        return extend;
    }

    @Override
    public boolean repair(Solver solver) {
        boolean repair;
        if (!dfslimit.isMet(limit)) {
            current = copen.length;
            repair = super.repair(solver);
        } else {
            extractOpenRightBranches(solver);
            repair = true;
        }
        return repair;
    }

    /**
     * This methods extracts and stores all open right branches for future exploration
     * @param solver reference to the solver
     */
    protected void extractOpenRightBranches(Solver solver) {
        // update parameters for restarts
        if (nodesRecompute > 0) {
            double ratio = nodesRecompute * 1.d / solver.getNodeCount();
            if (ratio > b && Z <= N) {
                Z *= 2;
            } else if (ratio < a && Z >= 2) {
                Z /= 2;
            }
        }
        limit += Z;
        // then start the extraction of open right branches
        int i = compareSubpath(solver);
        if(i < _unkopen.size()) {
            extractOB(solver, i);
        }
        // finally, get the best ORB to keep up the search
        Open next = opens.poll();
        while (next != null && !isValid(next.currentBound())) {
            next = opens.poll();
        }
        if (next != null) {
            copen = next.toArray();
            // the decision in 0 is the last taken, then the array us reversed
            ArrayUtils.reverse(copen);
            current = 0;
            nodesRecompute = solver.getNodeCount() + copen.length;
        } else{
            // to be sure not to use the previous path
            current = copen.length;
        }
        // then do the restart
        solver.restart();
    }

    /**
     * Copy the current decision path in _unkopen, for comparison with copen.
     * Then, it compares each decision, from the top to the bottom, to find the first difference.
     * This is required to avoid adding the same decision sub-path more than once
     * @param solver the search loop
     * @return the index of the decision, in _unkopen, that stops the loop
     */
    private int compareSubpath(Solver solver) {
        _unkopen.clear();
        DecisionPath decisionPath = solver.getDecisionPath();
        int pos = decisionPath.size() - 1;
        Decision decision = decisionPath.getDecision(pos);
        while (decision.getPosition() != topDecisionPosition) {
            _unkopen.add(decision);
            decision = decisionPath.getDecision(--pos);
        }
        Collections.reverse(_unkopen);
        //
        int i = 0;
        int I = Math.min(_unkopen.size(), copen.length);
        while(i < I && copen[i].isEquivalentTo(_unkopen.get(i))){
            i++;
        }
        return i;
    }

    /**
     * Extract the open right branches from the current path until it reaches the i^th decision of _unkopen
     * @param solver the search loop
     * @param i the index of the decision, in _unkopen, that stops the loop
     */
    private void extractOB(Solver solver, int i) {
//        Decision stopAt = solver.getDecisionPath().getDecision(_unkopen.get(i).getPosition()-1);
        int stopAt = _unkopen.get(i).getPosition()-1;
        // then, goes up in the search tree, and detect open nodes
        solver.getEnvironment().worldPop();
        DecisionPath dp = solver.getDecisionPath();
        int bound;
        Decision decision = dp.getLastDecision();
        while (decision.getPosition() != stopAt) {
            bound = isMinimization ?
                    objectiveManager.getObjective().getLB() :
                    objectiveManager.getObjective().getUB();
            if (decision.hasNext() && isValid(bound)) {
                opens.add(new Open(decision, dp, bound, isMinimization));
            }
            dp.synchronize();
            decision = dp.getLastDecision();
            solver.getEnvironment().worldPop();
        }
    }

    /**
     * If the bound of an O.R.B exceed the best known so far, it returns false.
     * @param bound the current bound of an O.R.B.
     * @return true if bound is valid wrt the best known so far.
     */
    private boolean isValid(int bound) {
        return isMinimization ?
                bound < objectiveManager.getBestUB().intValue() :
                bound > objectiveManager.getBestLB().intValue();
    }

    /**
     * A class to represent an open right branch, from which the search can be kept up.
     */
    private class Open implements Comparable<Open> {

        /**
         * List of open decisions
         */
        private final List<Decision> path;
        /**
         * store the current lower bound of the decision path for minimization
         */
        private final int currentBound;
        /**
         * 1 for minimization, -1 for maximization
         */
        private final byte minimization;

        /**
         * Create an open right branch for HBFS
         *
         * @param decision      an open decision in <i>decisionPath</i>
         * @param decisionPath  the current decision path
         * @param currentBound current lower (resp. upper) bound of the objective value for mimimization (resp. maximization)
         * @param minimization set to <tt>true</tt> for minimization
         */
        public Open(Decision decision, DecisionPath decisionPath, int currentBound, boolean minimization) {
            this.path = new ArrayList<>();
            while (decision.getPosition() != topDecisionPosition) {
                Decision d = decision.duplicate();
                while (decision.triesLeft() != d.triesLeft() - 1) {
                    d.buildNext();
                }
                path.add(d);
                decision = decisionPath.getDecision(decision.getPosition() -1);
            }
            this.currentBound = currentBound;
            this.minimization = (byte) (minimization ? 1 : -1);
        }

        /**
         * Return the current decision path that can be extended
         * @return an array of decisions
         */
        public Decision[] toArray() {
            return path.toArray(new Decision[0]);
        }

        /**
         * Return the current bound of this open right branch
         * @return the current bound of this
         */
        public int currentBound() {
            return currentBound;
        }

        @Override
        public int compareTo(Open o) {
            // the minimum lower bound
            int clb = minimization * (currentBound - o.currentBound);
            if (clb == 0) {
                // the maximum depth
                return (o.path.size() - path.size());
            } else {
                return clb;
            }
        }

        @Override
        public String toString() {
            StringBuilder st = new StringBuilder();
            st.append('[').append(currentBound).append(']');
            for(int i = path.size() - 1;i > -1 ; i--){
                st.append(path.get(i)).append(',');
            }
            return st.toString();
        }
    }
}
