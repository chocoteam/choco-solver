/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.explanations.strategies;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.Deduction;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.search.loop.ISearchLoop;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.IntVar;

import java.io.Serializable;
import java.util.ArrayDeque;

/**
 * A specific type of decision which stores a set of decisions to apply.
 * This class is dedicated to explanation framework and is used for path reparation.
 * Decisions to apply "as is" and the one that should be refute, are stored, and the tree search is re-build
 * on a call to DecisionsSet.apply().
 */
public class DecisionsSet extends Decision<IntVar> implements Serializable {
    private final DynamicBacktracking dynamicBacktracking;
    private final ArrayDeque<Decision> decision_path; // list of decisions describing the decision path

    public DecisionsSet(DynamicBacktracking dynamicBacktracking) {
        this.dynamicBacktracking = dynamicBacktracking;
        this.decision_path = new ArrayDeque<>(8);
    }

    public void clearDecisionPath() {
        decision_path.clear();
    }


    public void push(Decision dec) {
        decision_path.addFirst(dec);
    }

    public void setDecisionToRefute(Decision dec) {
        decision_path.addLast(dec);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                          Decision<IntVar> services                                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public IntVar getDecisionVariable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getDecisionValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public void buildNext() {
        // nothing to do
    }

    @Override
    public void rewind() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply() throws ContradictionException {
        ISearchLoop mSearchLoop = dynamicBacktracking.getSolver().getSearchLoop();
        IEnvironment environment = dynamicBacktracking.getSolver().getEnvironment();
        Decision dec;
        // retrieve the decision applied BEFORE the decision to refute, which is the last one in the decision_path
        Decision dec2ref = decision_path.getLast();

        Decision previous = dec2ref.getPrevious();
        int swi = dec2ref.getWorldIndex();
        //assert swi ==environment.getWorldIndex();

        // simulate open_node and rebuild decisions history
        dec = decision_path.pollFirst();
        dec.setPrevious(previous);
        dec.setWorldIndex(swi++);
        mSearchLoop.setLastDecision(dec);
        dec.buildNext();

        // then simulate down_branch
        dec.apply();
        mSearchLoop.getSMList().afterDownLeftBranch();
        previous = dec;

        // iterate over decisions
        while (!decision_path.isEmpty()) {

            // simulate open_node and rebuild decisions history
            dec = decision_path.pollFirst();
            dec.setPrevious(previous);
            dec.setWorldIndex(swi++);
            mSearchLoop.setLastDecision(dec);
            dec.buildNext();

            // then simulate down_branch
            mSearchLoop.getSMList().beforeDownLeftBranch();
            environment.worldPush();
            dec.apply();
            mSearchLoop.getSMList().afterDownLeftBranch();

            previous = dec;
        }
    }

    @Override
    public void setPrevious(Decision decision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Decision getPrevious() {
        return this;
    }

    @Override
    public void free() {
//            throw new UnsupportedOperationException();
    }

    @Override
    public void reverse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Deduction getNegativeDeduction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Deduction getPositiveDeduction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return decision_path.getFirst().toString();
    }
}
