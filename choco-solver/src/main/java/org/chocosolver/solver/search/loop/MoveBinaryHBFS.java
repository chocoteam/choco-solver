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

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.limits.BacktrackCounter;
import org.chocosolver.solver.search.strategy.decision.Decision;
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
 */
public class MoveBinaryHBFS extends MoveBinaryDFS {

    /**
     * limited number of backtracks for each DFS try
     */
    BacktrackCounter dfslimit;

    /**
     * limit of bracktracks for the next DFS try.
     */
    long Z;

    /**
     * as limit is globally maintained, limit += Z at each try.
     */
    long limit;
    /**
     * maximum number of backtracks to not exceed when updating node recomputation parameters.
     */
    long N;

    /**
     * for node recomputation.
     */
    long nodesRecompute;

    /**
     * lower and upper bounds to limit the rate of redundantly propagated decisions.
     */
    double a, b;

    /**
     * The current objective manager, to deal with best bounds.
     */
    ObjectiveManager<IntVar, Integer> objectiveManager;

    /**
     * Indicates if the current resolution policy is minimization.
     */
    boolean isMinimization;

    /**
     * list of open right branches.
     */
    PriorityQueue<Open> opens;

    /**
     * Current open right branch.
     */
    Decision[] copen;

    /**
     * Used to find the first unknown open right branch
     */
    List<Decision> _unkopen;

    /**
     * Current decision in copen
     */
    int current;

    /**
     * The owner solver.
     */
    Solver mSolver;

    public MoveBinaryHBFS(Solver solver, AbstractStrategy strategy, double a, double b, long N) {
        super(strategy);
        this.mSolver = solver;
        this.dfslimit = new BacktrackCounter(solver, N);
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
        ObjectiveManager<IntVar, Integer> om = mSolver.getObjectiveManager();
        this.objectiveManager = om;
        if (objectiveManager.getPolicy() == ResolutionPolicy.SATISFACTION) {
            throw new UnsupportedOperationException("HBFS is not adapted to satisfaction problems.");
        }
        isMinimization = objectiveManager.getPolicy() == ResolutionPolicy.MINIMIZE;
        return init;
    }

    @Override
    public boolean extend(SearchLoop searchLoop) {
        boolean extend;
        // as we observe the number of backtracks, no limit can be reached on extend()
        if (current < copen.length) {
            Decision tmp = searchLoop.decision;
            searchLoop.decision = copen[current++];
            assert searchLoop.decision != null;
            searchLoop.decision.setPrevious(tmp);
            searchLoop.mSolver.getEnvironment().worldPush();
            extend = true;
        } else /*cut will checker with propagation */ {
            extend = super.extend(searchLoop);
        }
        return extend;
    }

    @Override
    public boolean repair(SearchLoop searchLoop) {
        boolean repair;
        if (!dfslimit.isMet(limit)) {
            current = copen.length;
            repair = super.repair(searchLoop);
        } else {
            extractOpenRightBranches(searchLoop);
            repair = true;
        }
        return repair;
    }

    /**
     * This methods extracts and stores all open right branches for future exploration
     */
    protected void extractOpenRightBranches(SearchLoop searchLoop) {
        // update parameters for restarts
        if (nodesRecompute > 0) {
            double ratio = nodesRecompute * 1.d / searchLoop.mMeasures.getNodeCount();
            if (ratio > b && Z <= N) {
                Z *= 2;
            } else if (ratio < a && Z >= 2) {
                Z /= 2;
            }
        }
        limit += Z;
        // then start the extraction of open right branches
        int i = compareSubpath(searchLoop);
        if(i < _unkopen.size()) {
            extractOB(searchLoop, i);
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
            nodesRecompute = searchLoop.mMeasures.getNodeCount() + copen.length;
        } else{
            // to be sure not to use the previous path
            current = copen.length;
        }
        // then do the restart
        searchLoop.restart();
    }

    /**
     * Copy the current decision path in _unkopen, for comparison with copen.
     * Then, it compares each decision, from the top to the bottom, to find the first difference.
     * This is required to avoid adding the same decision sub-path more than once
     * @param searchLoop the search loop
     * @return the index of the decision, in _unkopen, that stops the loop
     */
    private int compareSubpath(SearchLoop searchLoop) {
        _unkopen.clear();
        Decision decision = searchLoop.decision;
        while (decision != topDecision) {
            _unkopen.add(decision);
            decision = decision.getPrevious();
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
     * @param searchLoop the search loop
     * @param i the index of the decision, in _unkopen, that stops the loop
     */
    private void extractOB(SearchLoop searchLoop, int i) {
        Decision stopAt = _unkopen.get(i).getPrevious();
        // then, goes up in the search tree, and detect open nodes
        searchLoop.mSolver.getEnvironment().worldPop();
        Decision decision = searchLoop.decision;
        int bound;
        while (decision != stopAt) {
            bound = isMinimization ?
                    objectiveManager.getObjective().getLB() :
                    objectiveManager.getObjective().getUB();
            if (decision.hasNext() && isValid(bound)) {
                opens.add(new Open(decision, bound, isMinimization));
            }
            searchLoop.decision = searchLoop.decision.getPrevious();
            decision.free();
            decision = searchLoop.decision;
            searchLoop.mSolver.getEnvironment().worldPop();
        }
    }

    /**
     * If the bound of an O.R.B exceed the best known so far, it returns false.
     * @param bound the current bound of an O.R.B.
     * @return true if bound is valid wrt the best known so far.
     */
    private boolean isValid(int bound) {
        return isMinimization ?
                bound < objectiveManager.getBestUB() :
                bound > objectiveManager.getBestLB();
    }

    /**
     * A class to represent an open right branch, from which the search can be kept up.
     */
    private class Open implements Comparable<Open> {

        List<Decision> path;
        int currentBound; // store the current lower bound of the decision path for minimization
        byte minimization; // 1 for minimization, -1 for maximization

        /**
         * Create an open right branch for HBFS
         *
         * @param decision     an open decision
         * @param currentBound current lower (resp. upper) bound of the objective value for mimimization (resp. maximization)
         * @param minimization set to <tt>true</tt> for minimization
         */
        public Open(Decision<IntVar> decision, int currentBound, boolean minimization) {
            this.path = new ArrayList<>();
            while (decision != topDecision) {
                Decision d = decision.duplicate();
                while (decision.triesLeft() != d.triesLeft() - 1) {
                    d.buildNext();
                }
                path.add(d);
                decision = decision.getPrevious();
            }
            this.currentBound = currentBound;
            this.minimization = (byte) (minimization ? 1 : -1);
        }

        /**
         * Return the current decision path that can be extended
         * @return an array of decisions
         */
        public Decision[] toArray() {
            return path.toArray(new Decision[path.size()]);
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
