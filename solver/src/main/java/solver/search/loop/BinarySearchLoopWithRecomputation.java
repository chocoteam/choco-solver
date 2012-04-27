/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.search.loop;

import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;

import java.util.Stack;

/**
 * A binary search loop with recomputation, to avoid backuping at before every propagation.
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @since 0.01
 */
public class BinarySearchLoopWithRecomputation extends BinarySearchLoop {

    protected final int gap;
    protected int counter;
    private final Stack<Decision> keyDecisions;

    private Decision tmp_dec;
    private Decision[] tmp_decs;

    @SuppressWarnings({"unchecked"})
    BinarySearchLoopWithRecomputation(Solver solver) {
        super(solver);
        gap = 10;
        counter = 0;
        keyDecisions = new Stack<Decision>();
        tmp_decs = new Decision[gap + 1];
    }

    @Override
    protected void initialPropagation() {
        super.initialPropagation();
        counter = 0;
    }

    protected void downBranch() {
        if (counter % gap == 0) {
            env.worldPush();
            keyDecisions.push(decision);
            counter = 0;
        }
        counter++;
        try {
            decision.buildNext();
            objectivemanager.apply(decision);
            objectivemanager.postDynamicCut();

            propEngine.propagate();
            moveTo(OPEN_NODE);
        } catch (ContradictionException e) {
            propEngine.flush();
            moveTo(UP_BRANCH);
            jumpTo = 1;
            smList.onContradiction(e);
        }
    }


    /**
     * {@inheritDoc}
     * Rolls back the previous state.
     * Then, if it goes back to the base world, stop the search.
     * Otherwise, gets the oppposite decision, applies it and calls the propagation.
     */
    @Override
    protected void upBranch() {
        env.worldPop();
        if (env.getWorldIndex() == rootWorldIndex) {
            // The entire tree search has been explored, the search cannot be followed
            interrupt();
        } else {
            recomputeState();
            jumpTo--;
            if (jumpTo <= 0 && decision.hasNext()) {
                moveTo(DOWN_RIGHT_BRANCH);
            } else {
                Decision tmp = decision;
                decision = decision.getPrevious();
                tmp.free();
            }
        }
    }

    private void recomputeState() {
        if (keyDecisions.peek() == decision) { // if the current decision was a backup one
            env.worldPop(); // restaure world before this decision
            keyDecisions.pop(); //remove the decision from the key decisions
        }
        env.worldPush(); // save the current state
        // Recompute the decisions to apply in order to restaure previous (ie, before failing) state
        int i = 0;
        tmp_dec = decision;
        while (tmp_dec != keyDecisions.peek()) { // while this is not the last key decision
            tmp_dec = tmp_dec.getPrevious(); // go backward
            tmp_decs[i++] = tmp_dec;
        }
        // Then re-apply decisions
        try {
//            objectivemanager.postDynamicCut();
            for (; i > 0; ) {
                tmp_decs[--i].apply();
                propEngine.propagate(); // required to ensure correctness of search decision
            }
            propEngine.propagate(); // required to ensure correctness of search decision
        } catch (ContradictionException e) {
            propEngine.flush();
            // this must not fail, it has been applied before
//            throw new SolverException("Unexpected contradiction during recomputation");
            while (decision != tmp_decs[i]) {
                tmp_dec = decision;
                decision = decision.getPrevious();
                tmp_dec.free();
            }
            env.worldPop();
            recomputeState();
        }
        counter = 0; // to force backup at next call to downBranch
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void restartSearch() {
        super.restart();
        counter = 0;
        keyDecisions.clear();
    }
}
