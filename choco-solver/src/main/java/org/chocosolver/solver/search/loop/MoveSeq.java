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

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.Variable;

import java.util.Arrays;
import java.util.List;

/**
 * BETA: This a work-in-progress.
 * It is certainly not bug free, specially the repair method may lead to unexpected behavior.
 *
 * A move made of two or more moves.
 * The i^th move is called when the i-1 ^th returns false.
 *
 * Created by cprudhom on 29/10/2015.
 * Project: choco.
 */
public class MoveSeq implements Move {

    List<Move> moves;
    IStateInt index;
    AbstractStrategy seqStrat;
    TransitionDecision[] tds;

    public MoveSeq(Model model, Move... moves) {
        this.moves = Arrays.asList(moves);
        this.index = model.getEnvironment().makeInt(0);
        AbstractStrategy[] strats = new AbstractStrategy[moves.length];
        for (int i = 0; i < moves.length; i++) {
            strats[i] = moves[i].getStrategy();
        }
        tds = new TransitionDecision[moves.length - 1];
        for (int i = 0; i < tds.length; i++) {
            tds[i] = new TransitionDecision();
        }
        this.seqStrat = new StrategiesSequencer(strats);
    }

    @Override
    public boolean init() {
        boolean init = true;
        for (int i = 0; i < moves.size() && init; i++) {
            init = moves.get(i).init();
        }
        return init;
    }

    @Override
    public boolean extend(SearchLoop searchLoop) {
        boolean extend = false;
        int i = index.get();
        // if the current Move is able to extend the decision path
        if (i < moves.size()) {
            extend = moves.get(i).extend(searchLoop);
        }
        //otherwise, try the remaining ones.
        while (i < moves.size() - 1 && !extend) {
            // first, store the world index in which the first decision of this move is taken.
            i++;
            Decision tmp = searchLoop.decision;
            searchLoop.decision = tds[i - 1];
            searchLoop.decision.setPrevious(tmp);
            searchLoop.mModel.getEnvironment().worldPush();
            moves.get(i).setTopDecision(tds[i - 1]);
            extend = moves.get(i).extend(searchLoop);
        }
        index.set(i);
        return extend;
    }

    @Override
    public boolean repair(SearchLoop searchLoop) {
        boolean repair = false;
        int i = index.get() + 1;
        while (i > 0 && !repair) {
            repair = moves.get(--i).repair(searchLoop);
            if (i > 0) {
                Decision tmp = searchLoop.decision;
                searchLoop.decision = searchLoop.decision.getPrevious();
                tmp.free();
            }
        }
        index.set(i);
        return repair;
    }

    @Override
    public void setTopDecision(Decision topDecision) {
        for (int i = 0; i < moves.size(); i++) {
            moves.get(i).setTopDecision(topDecision);
        }
    }

    @Override
    public <V extends Variable> AbstractStrategy<V> getStrategy() {
        return seqStrat;
    }

    @Override
    public <V extends Variable> void setStrategy(AbstractStrategy<V> aStrategy) {
        throw new UnsupportedOperationException("A sequential Move does not support declaring search strategy in retrospect.\n" +
                "It has to be done on each of it child nodes.");
    }

    @Override
    public List<Move> getChildMoves() {
        return moves;
    }

    @Override
    public void setChildMoves(List<Move> someMoves) {
        this.moves = someMoves;
    }

    private static class TransitionDecision extends Decision {

        public TransitionDecision() {
            super(1);
        }

        @Override
        public void apply() throws ContradictionException {
            // do nothing
        }

        @Override
        public Object getDecisionValue() {
            return null;
        }

        @Override
        public void free() {

        }

        @Override
        public String toString() {
            return "Transition";
        }
    }
}
