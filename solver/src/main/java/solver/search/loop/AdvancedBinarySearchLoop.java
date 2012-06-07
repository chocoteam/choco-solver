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

import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28 sept. 2010
 */
public class AdvancedBinarySearchLoop extends BinarySearchLoop {

    private IStateInt nbPrevisouDecisions;

    AdvancedBinarySearchLoop(Solver solver) {
        super(solver);
        nbPrevisouDecisions = env.makeInt();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Computes the next decision to apply, move to the next step in the run loop.
     * If there are no next decision, the current state is considered as a solution.
     * Once the solution has been stored -- and checked--, the next step in the tree search is computed: continue or stop.
     */
    @Override
    protected void openNode() {
        Decision tmp = decision;
        decision = strategy.getDecision();
        if (decision != null) {
            decision.setPrevious(tmp);
            moveTo(DOWN_LEFT_BRANCH);
        } else {
            decision = tmp;
            try {
                timeStamp++;
                int nbPrevDec = nbPrevisouDecisions.get();
                if (nbPrevDec > 0) {
                    objectivemanager.apply(decision);
                    objectivemanager.postDynamicCut();
                    Decision previous = decision.getPrevious();
                    for (int i = 0; i < nbPrevDec; i++) {
                        previous.apply();
                        previous = previous.getPrevious();
                    }
                    solver.getEngine().propagate();
                }
                recordSolution();
            } catch (ContradictionException e) {
                solver.getEngine().flush();
                moveTo(UP_BRANCH);
                jumpTo = 1;
                smList.onContradiction(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Backs up the current state.
     * Then gets the current computed decision,applies it to the problem and calls the main propagation.
     */
    @Override
    protected void downLeftBranch() {
        env.worldPush();
        try {
            decision.buildNext();
            objectivemanager.apply(decision);
            objectivemanager.postDynamicCut();
            Decision prev = decision.getPrevious();
            int nbPrevDec = nbPrevisouDecisions.get();
            for (int i = 0; i < nbPrevDec; i++) {
                prev.apply();
                prev = prev.getPrevious();
            }
            solver.getEngine().propagate();
            nbPrevisouDecisions.set(0);
            moveTo(OPEN_NODE);
        } catch (ContradictionException e) {
            solver.getEngine().flush();
            nbPrevisouDecisions.add(1);
            moveTo(UP_BRANCH);
            jumpTo = 1;
            smList.onContradiction(e);
        }

    }

    /**
     * {@inheritDoc}
     * <p/>
     * Backs up the current state.
     * Then gets the current computed decision,applies it to the problem and calls the main propagation.
     */
    @Override
    protected void downRightBranch() {
        env.worldPush();
        decision.buildNext();
        nbPrevisouDecisions.add(1);
        moveTo(OPEN_NODE);
    }
}
