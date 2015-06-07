/**
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package org.chocosolver.solver.explanations;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.store.ArrayEventStore;
import org.chocosolver.solver.explanations.store.IEventStore;
import org.chocosolver.solver.explanations.strategies.ConflictStrategy;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.FilteringMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.PoolManager;

/**
 * An Asynchronous, Reverse, Low-Intrusive and Lazy explanation engine
 * Based on "A Lazy explanation engine for Choco3", C.Prud'homme.
 * <p>
 * Created by cprudhom on 09/12/14.
 * Project: choco.
 */
public class ExplanationEngine implements FilteringMonitor {

    protected final IEventStore eventStore; // set of generated events
    private final RuleStore ruleStore; // set of active rules
    private final boolean saveCauses; // save the clauses in Explanation
    private final Solver mSolver;
    private ConflictStrategy cstrat;
    PoolManager<Explanation> explanationPool;


    /**
     * Create an explanation engine based on a rule store
     *
     * @param solver                   a solver
     * @param userFeedback             does user require feedback, ie, keep trace of the constraints in conflict ?
     * @param enablePartialExplanation do explanations need to be complete (for DBT or nogood extraction) ?
     */
    public ExplanationEngine(Solver solver, boolean userFeedback, boolean enablePartialExplanation) {
        this.mSolver = solver;
        this.saveCauses = userFeedback;
        eventStore = new ArrayEventStore(solver.getEnvironment());
        ruleStore = new RuleStore(solver, saveCauses, enablePartialExplanation);
        solver.set(this);
        this.explanationPool = new PoolManager<>();
    }

    /**
     * Return the conflict strategy declared
     */
    public ConflictStrategy getCstrat() {
        return cstrat;
    }

    /**
     * Set the conflict strategy to use
     */
    public void setCstrat(ConflictStrategy cstrat) {
        this.cstrat = cstrat;
    }

    /**
     * Indicate whether or not the clauses are saved in Explanation
     *
     * @return if clauses are saved
     */
    public boolean isSaveCauses() {
        return saveCauses;
    }

    /**
     * Compute the explanation of the last event from the event store (naturally, the one that leads to a conflict),
     * and return the explanation of the failure, that is, the (sub-)set of decisions and propagators explaining the conflict.
     *
     * @return an explanation (set of decisions and propagators).
     */
    public Explanation explain(ContradictionException cex) {
        Explanation explanation = makeExplanation(saveCauses);
        ruleStore.init();

        if (cex.v != null) {
            ruleStore.addFullDomainRule((IntVar) cex.v);
        } else {
            explanation.addCause(cex.c); // otherwise, we could miss it ;)
            cex.c.why(ruleStore, null, IntEventType.VOID, 0);
        }
        int i = eventStore.getSize() - 1;
        while (i > -1 && !ruleStore.isPreemptedStop()) {
            if (ruleStore.match(i, eventStore)) {
                ruleStore.update(i, eventStore, explanation);
            }
            i--;
        }
        return explanation;
    }

    public Explanation makeExplanation(boolean saveCauses) {
        Explanation explanation = explanationPool.getE();
        if (explanation == null) {
            explanation = new Explanation(explanationPool, saveCauses);
        }
        return explanation;
    }

    public RuleStore getRuleStore() {
        return ruleStore;
    }

    public IEventStore getEventStore() {
        return eventStore;
    }

    public Solver getSolver() {
        return mSolver;
    }

    /**
     * Get the explanation of a decision refutation
     *
     * @param decision a refuted decision
     * @return the explanation
     */
    public Explanation getDecisionRefutationExplanation(Decision decision) {
        return ruleStore.getDecisionRefutation(decision);
    }

    /**
     * Store a decision refutation, for future reasoning.
     *
     * @param decision    refuted decision
     * @param explanation the explanation of the refutation
     */
    public void storeDecisionExplanation(Decision decision, Explanation explanation) {
        ruleStore.storeDecisionRefutation(decision, explanation);
    }

    /**
     * Move a decision explanation from the old index to the new one.
     * Required for DBT only and should be called with care!
     *
     * @param decision a decision
     * @param to       the new index
     */
    public void moveDecisionRefutation(Decision decision, int to) {
        ruleStore.moveDecisionRefutation(decision, to);
    }


    /**
     * Free the explanation related to the decision (for efficiency purpose only)
     *
     * @param decision the decision which is going to be forgotten
     */
    public void freeDecisionExplanation(Decision decision) {
        ruleStore.freeDecisionExplanation(decision);
    }

    /**
     * This is the main explanation why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param val   a value
     * @param cause a cause
     */
    @Override
    public void removeValue(IntVar var, int val, ICause cause) {
        eventStore.pushEvent(var, cause, IntEventType.REMOVE, val, -1, -1);
    }

    /**
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param value a value
     * @param cause a cause
     * @value old previous LB
     */
    @Override
    public void updateLowerBound(IntVar var, int value, int old, ICause cause) {
        eventStore.pushEvent(var, cause, IntEventType.INCLOW, value, old, -1);
    }

    /**
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param value a value
     * @param cause a cause
     * @value old previous LB
     */
    @Override
    public void updateUpperBound(IntVar var, int value, int old, ICause cause) {
        eventStore.pushEvent(var, cause, IntEventType.DECUPP, value, old, -1);
    }

    /**
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param val   a value
     * @param cause a cause
     * @param oldLB previous lb
     * @param oldUB previous ub
     */
    @Override
    public void instantiateTo(IntVar var, int val, ICause cause, int oldLB, int oldUB) {
        eventStore.pushEvent(var, cause, IntEventType.INSTANTIATE, val, oldLB, oldUB);
    }

    @Override
    public void activePropagator(BoolVar var, Propagator propagator) {
        eventStore.pushEvent(var, propagator, PropagatorEventType.FULL_PROPAGATION, propagator.getId(), 0, 0);
    }
}
