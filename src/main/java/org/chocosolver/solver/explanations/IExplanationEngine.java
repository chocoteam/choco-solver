/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.explanations;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

/**
 * Interface for Lazy explanation engine.
 * <p>
 * Created by cprudhom on 09/12/14.
 * Project: choco.
 */
public interface IExplanationEngine {

    /**
     * Indicate whether or not the clauses are saved in Explanation
     *
     * @return if clauses are saved
     */
    default boolean isSaveCauses() {
        return false;
    }

    /**
     * Compute the explanation of the last event from the event store (naturally, the one that leads to a conflict),
     * and return the explanation of the failure, that is, the (sub-)set of decisions and propagators explaining the conflict.
     *
     * @param cex    contradiction to explain
     * @return an explanation (set of decisions and propagators).
     */
    default Explanation explain(ContradictionException cex) {
        return null;
    }

    /**
     * @param saveCauses set to <tt>true</tt> if causes need to be stored
     * @return an empty explanation, ready to be filled up
     */
    default Explanation makeExplanation(boolean saveCauses) {
        return null;
    }

    /**
     * @return the current rule store
     */
    default RuleStore getRuleStore() {
        return null;
    }

    /**
     * @return the current store of events
     */
    default ArrayEventStore getEventStore() {
        return null;
    }

    /**
     * Get the explanation of a decision refutation
     *
     * @param decision a refuted decision
     * @return the explanation
     */
    default Explanation getDecisionRefutationExplanation(Decision decision) {
        return null;
    }

    /**
     * Store a decision refutation, for future reasoning.
     *
     * @param decision    refuted decision
     * @param explanation the explanation of the refutation
     */
    default void storeDecisionExplanation(Decision decision, Explanation explanation) {
    }

    /**
     * Move a decision explanation from the old index to the new one.
     * Required for DBT only and should be called with care!
     *
     * @param decision a decision
     * @param to       the new index
     */
    default void moveDecisionRefutation(Decision decision, int to) {
    }


    /**
     * Free the explanation related to the decision (for efficiency purpose only)
     *
     * @param decision the decision which is going to be forgotten
     */
    default void freeDecisionExplanation(Decision decision) {
    }

    /**
     * Explain the removal of the {@code val} from {@code var}, due to {@code cause}.
     * This is the main explanation why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param val   a value
     * @param cause a cause
     */
    default void removeValue(IntVar var, int val, ICause cause) {
    }

    /**
     * Explain the removal of [{@code old},{@code value}[ from {@code var}, due to {@code cause}.
     * <p/>
     * Prerequisite: {@code value} should belong to {@code var}
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param value a value
     * @param cause a cause
     * @value old previous LB
     */
    default void updateLowerBound(IntVar var, int value, int old, ICause cause) {
    }

    /**
     * Explain the removal of ]{@code value},{@code old}] from {@code var}, due to {@code cause}.
     * <p/>
     * Prerequisite: {@code value} should belong to {@code var}
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param value a value
     * @param cause a cause
     * @value old previous LB
     */
    default void updateUpperBound(IntVar var, int value, int old, ICause cause) {
    }

    /**
     * Explain the assignment to {@code val} of {@code var} due to {@code cause}.
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param val   a value
     * @param cause a cause
     * @param oldLB previous lb
     * @param oldUB previous ub
     */
    default void instantiateTo(IntVar var, int val, ICause cause, int oldLB, int oldUB) {
    }

    /**
     * Explain the activation of a propagator involved in a reified constraint
     *
     * @param var        the reified variable
     * @param propagator the propagator to awake.
     */
    default void activePropagator(BoolVar var, Propagator propagator) {
    }

    /**
     * Undo the last operation done
     */
    default void undo(){
    }
}
