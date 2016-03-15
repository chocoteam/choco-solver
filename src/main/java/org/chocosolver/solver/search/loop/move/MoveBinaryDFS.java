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
    AbstractStrategy strategy;
    /**
     * Index, in the decision path, of the decision taken just before selecting this move.
     */
    int topDecisionPosition;

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
        solver.getMeasures().incDepth();
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
        this.strategy = aStrategy;
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
        solver.getDecisionPath().removeLast();
        // goes up in the search tree and makes sure search monitors are correctly informed
        solver.getSearchMonitors().afterUpBranch();
        solver.getMeasures().incBackTrackCount();
        solver.getMeasures().decDepth();
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
