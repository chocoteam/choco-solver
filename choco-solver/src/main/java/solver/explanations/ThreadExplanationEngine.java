/*
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
package solver.explanations;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.explanations.*;
import org.chocosolver.solver.explanations.antidom.AntiDomain;
import org.chocosolver.solver.search.loop.monitors.IMonitorDownBranch;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import solver.explanations.store.BufferedEventStore;

/**
 * UNSAFE
 * Created by cprudhom on 18/11/14.
 * Project: choco.
 */
public class ThreadExplanationEngine extends ExplanationEngine implements IMonitorDownBranch {


    private final BufferedEventStore eventStore;

    /**
     * Builds an ExplanationEngine
     *
     * @param slv associated solver's environment
     */
    public ThreadExplanationEngine(Solver slv, BufferedEventStore eventStore) {
        super(slv);
        this.eventStore = eventStore;
        slv.plugMonitor(this);
    }

    /**
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param val   a value
     * @param cause a cause
     */
    @Override
    public void removeValue(IntVar var, int val, ICause cause) {
        eventStore.push(var, cause, IntEventType.REMOVE, val, 0, 0);
    }

    /**
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param intVar an integer variable
     * @param value  a value
     * @param cause  a cause
     * @value old previous LB
     */
    @Override
    public void updateLowerBound(IntVar intVar, int old, int value, ICause cause) {
        eventStore.push(intVar, cause, IntEventType.INCLOW, old, value, 0);
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
    public void updateUpperBound(IntVar var, int old, int value, ICause cause) {
        eventStore.push(var, cause, IntEventType.DECUPP, old, value, 0);
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
        eventStore.push(var, cause, IntEventType.INSTANTIATE, val, oldLB, oldUB);
    }

    /**
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var        the reified variable
     * @param propagator the propagator to awake.
     */
    @Override
    public void activePropagator(BoolVar var, Propagator propagator) {
        eventStore.push(var, propagator, PropagatorEventType.FULL_PROPAGATION, 0, 0, 0);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void request() {
        eventStore.hurryUp();
    }

    @Override
    public AntiDomain getRemovedValues(IntVar v) {
        return eventStore.getRemovedValues(v);
    }

    @Override
    public ValueRemoval getValueRemoval(IntVar var, int val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Deduction explain(IntVar var, int val) {
        return eventStore.explain(var, val);
    }

    @Override
    public Deduction explain(Deduction deduction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Explanation flatten(IntVar var, int val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Explanation flatten(Explanation expl) {
        return eventStore.flatten(expl);
    }

    @Override
    public Explanation flatten(Deduction deduction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Explanation retrieve(IntVar var, int val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BranchingDecision getDecision(Decision decision, boolean isLeft) {
        return eventStore.getDecision(decision, isLeft);
    }

    @Override
    public PropagatorActivation getPropagatorActivation(Propagator propagator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void store(Deduction deduction, Explanation explanation) {
        eventStore.store(deduction, explanation);
    }

    @Override
    public void removeLeftDecisionFrom(Decision decision, Variable var) {
        eventStore.removeLeftDecisionFrom(decision, var);
    }


    @Override
    public void beforeDownLeftBranch() {
        eventStore.hurryUp();
    }

    @Override
    public void afterDownLeftBranch() {

    }

    @Override
    public void beforeDownRightBranch() {
        eventStore.hurryUp();
    }

    @Override
    public void afterDownRightBranch() {

    }
}
