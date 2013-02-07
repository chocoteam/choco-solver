/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.explanations;


import solver.ICause;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.RootDecision;
import solver.variables.IntVar;

import java.util.ArrayDeque;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/10/12
 */
public class DynamicBacktracking extends ConflictBasedBackjumping {

    ArrayDeque<Decision> decision_path; // list of decisions describing the decision path
    CombinedDecision cobdec;

    public DynamicBacktracking(RecorderExplanationEngine mExplanationEngine) {
        super(mExplanationEngine);
        decision_path = new ArrayDeque<Decision>(8);
        cobdec = new CombinedDecision();
    }

    protected Decision updateVRExplainUponbacktracking(int nworld, Explanation expl, ICause cause) {
        if (cause == mSolver.getSearchLoop().getObjectivemanager()) {
            super.updateVRExplainUponbacktracking(nworld, expl, cause);
        }

        decision_path.clear();
        Decision dec = mSolver.getSearchLoop().decision; // the current decision to undo
        while (dec != RootDecision.ROOT && nworld > 1) {
            // 1. make a reverse copy of the decision, ready to be a LEFT branch
            dec.reverse();
            dec.rewind();
            // 3. add it to the pool of decision to force
            decision_path.add(dec);
            // get the previous
            dec = dec.getPrevious();
            nworld--;
        }
        if (dec != RootDecision.ROOT) {
            if (!dec.hasNext())
                throw new UnsupportedOperationException("DynamicBacktracking.updatVRExplain should get to a POSITIVE decision");
            decision_path.add(dec);
            Deduction left = dec.getPositiveDeduction();
            expl.remove(left);
            assert left.mType == Deduction.Type.DecLeft;
            BranchingDecision va = (BranchingDecision) left;
            mExplanationEngine.leftbranchdecisions.get(va.getVar().getId()).remove(va.getDecision().getId());

            Deduction right = dec.getNegativeDeduction();
            mExplanationEngine.database.put(right.id, mExplanationEngine.flatten(expl));

            mSolver.getSearchLoop().decision = cobdec;
        }
        return dec;
    }


    private class CombinedDecision extends Decision<IntVar> {

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
            // apply the list of decisions
            Decision tmp;
            Decision dec = decision_path.pollLast();
            // erase the current fake decision
            dec.hasNext();
            downbranch(dec);
            while (!decision_path.isEmpty()) {
                mSolver.getEnvironment().worldPush();
                // get the following decision
                tmp = decision_path.pollLast();
                // rebuild history
                tmp.setPrevious(dec);
                dec = tmp;
                // then
                downbranch(dec);
            }
        }


        private void downbranch(Decision dec) throws ContradictionException {
            mSolver.getSearchLoop().decision = dec;
            mSolver.getSearchLoop().smList.beforeDownLeftBranch();
            dec.buildNext();
            mSolver.getSearchLoop().getObjectivemanager().apply(dec);
            mSolver.getSearchLoop().smList.afterDownLeftBranch();
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
        public void explain( Deduction d, Explanation e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "[DBT]";
        }
    }
}
