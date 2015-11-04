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

import java.util.List;

/**
 * A combination of Move and Learn which results in tabu Decision-repair[1] (TDR) with binary decisions.
 * <p>
 * [1]: N. Jussien, O. Lhomme, Local search with constraint propagation and conflict-based heuristics, AI-139 (2002).
 *
 * Created by cprudhom on 04/11/2015.
 * Project: choco.
 */
public class MoveLearnBinaryTDR implements Move, Learn {

    @Override
    public boolean init() {
        return false;
    }

    @Override
    public boolean extend(SearchLoop searchLoop) {
        return false;
    }

    @Override
    public boolean repair(SearchLoop searchLoop) {
        return false;
    }

    @Override
    public <V extends Variable> AbstractStrategy<V> getStrategy() {
        return null;
    }

    @Override
    public <V extends Variable> void setStrategy(AbstractStrategy<V> aStrategy) {

    }

    @Override
    public List<Move> getChildMoves() {
        return null;
    }

    @Override
    public void setChildMoves(List<Move> someMoves) {

    }

    @Override
    public void setTopDecision(Decision topDecision) {

    }

    @Override
    public void record(SearchLoop searchLoop) {

    }

    @Override
    public void forget(SearchLoop searchLoop) {

    }
}
