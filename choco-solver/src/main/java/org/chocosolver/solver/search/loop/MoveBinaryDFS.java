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

import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;

import static org.chocosolver.solver.search.strategy.decision.RootDecision.ROOT;

/**
 * A move dedicated to run a Depth First Search with binary decisions.
 * <p>
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 */
public class MoveBinaryDFS implements Move {

    AbstractStrategy strategy;

    public MoveBinaryDFS(AbstractStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public boolean init() {
        return strategy.init();
    }

    @Override
    public boolean extend(SearchLoop searchLoop) {
        boolean extended = false;
        Decision tmp = searchLoop.decision;
        searchLoop.decision = strategy.getDecision();
        if (searchLoop.decision != null) { // null means there is no more decision
            searchLoop.decision.setPrevious(tmp);
            searchLoop.mSolver.getEnvironment().worldPush();
            extended = true;
        } else {
            searchLoop.decision = tmp;
        }
        return extended;
    }

    @Override
    public boolean repair(SearchLoop searchLoop) {
        searchLoop.mMeasures.incBackTrackCount();
        searchLoop.mMeasures.incDepth();
        searchLoop.mSolver.getEnvironment().worldPop();
        return rewind(searchLoop);
    }

    @Override
    public <V extends Variable> AbstractStrategy<V> getStrategy() {
        return strategy;
    }

    @Override
    public <V extends Variable> void setStrategy(AbstractStrategy<V> aStrategy) {
        this.strategy = aStrategy;
    }

    protected boolean rewind(SearchLoop searchLoop) {
        boolean repaired = false;
        while (!repaired && searchLoop.decision != ROOT) {
            searchLoop.jumpTo--;
            if (searchLoop.jumpTo <= 0 && searchLoop.decision.hasNext()) {
                searchLoop.mSolver.getEnvironment().worldPush();
                repaired = true;
            } else {
                prevDecision(searchLoop);
            }
        }
        return repaired;
    }

    protected void prevDecision(SearchLoop searchLoop) {
        Decision tmp = searchLoop.decision;
        searchLoop.decision = searchLoop.decision.getPrevious();
        tmp.free();
        searchLoop.searchMonitors.afterUpBranch(); // to make sure search monitors are correctly informed
        searchLoop.mMeasures.incBackTrackCount();
        searchLoop.mMeasures.incDepth();
        searchLoop.mSolver.getEnvironment().worldPop();
        searchLoop.searchMonitors.beforeUpBranch(); // to make sure search monitors are correctly informed
    }

    @Override
    public Move getChildMove() {
        return null;
    }

    @Override
    public void setChildMove(Move aMove) {
        throw new UnsupportedOperationException("This is a terminal Move.");
    }
}
