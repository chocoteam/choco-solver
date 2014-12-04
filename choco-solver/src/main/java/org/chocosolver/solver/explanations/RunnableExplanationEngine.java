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
package org.chocosolver.solver.explanations;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.explanations.antidom.AntiDomain;
import org.chocosolver.solver.explanations.store.IEventStore;
import org.chocosolver.solver.explanations.store.QueueEventStore;
import org.chocosolver.solver.search.loop.monitors.IMonitorClose;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitPropagation;
import org.chocosolver.solver.search.loop.monitors.IMonitorUpBranch;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UNSAFE
 *
 * Created by cprudhom on 13/11/14.
 * Project: choco.
 */
public class RunnableExplanationEngine extends ExplanationEngine implements Runnable, IMonitorClose, IMonitorInitPropagation, IMonitorUpBranch {

    static Logger LOGGER = LoggerFactory.getLogger(RunnableExplanationEngine.class);

    private final IEventStore estore;
    final ExplanationEngine mainEngine;

    Thread me;
    AtomicBoolean alive;

    public RunnableExplanationEngine(Solver solver, ExplanationEngine eng) {
        super(solver);
        estore = new QueueEventStore();
        mainEngine = eng;
        solver.plugMonitor(this);
    }

    @Override
    public void run() {
        int one, two, three;
        IntVar var;
        IEventType etype;
        ICause cause;
        boolean bug;
        while (alive.get()) {
            while (estore.getSize() > 0) {
                synchronized (estore) {
                    estore.popEvent();
                    var = estore.getVariable(0);
                    etype = estore.getEventType(0);
                    cause = estore.getCause(0);
                    one = estore.getFirstValue(0);
                    two = estore.getSecondValue(0);
                    three = estore.getThirdValue(0);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("GOT {} {} {} {} {}", var, cause, etype, one, two, three);
                    }
                    bug = true;
                    if (etype == IntEventType.REMOVE) {
                        mainEngine.removeValue(var, one, cause);
                        bug = false;
                    }
                    if (etype == IntEventType.INSTANTIATE) {
                        mainEngine.instantiateTo(var, one, cause, two, three);
                        bug = false;
                    }
                    if (etype == IntEventType.INCLOW) {
                        mainEngine.updateLowerBound(var, one, two, cause);
                        bug = false;
                    }
                    if (etype == IntEventType.DECUPP) {
                        mainEngine.updateUpperBound(var, one, two, cause);
                        bug = false;
                    }
                    if (etype == PropagatorEventType.FULL_PROPAGATION) {
                        Propagator prop = (Propagator) cause;
                        BoolVar bVar = (BoolVar) var;
                        mainEngine.activePropagator(bVar, prop);
                        bug = false;
                    }
                }
                if (bug) {
                    throw new UnsupportedOperationException("Unknown type " + etype);
                }
            }
        }

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
        synchronized (estore) {
            estore.pushEvent(var, cause, IntEventType.REMOVE, val, 0, 0);
        }
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
        synchronized (estore) {
            estore.pushEvent(intVar, cause, IntEventType.INCLOW, old, value, 0);
        }
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
        synchronized (estore) {
            estore.pushEvent(var, cause, IntEventType.DECUPP, old, value, 0);
        }
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
        synchronized (estore) {
            estore.pushEvent(var, cause, IntEventType.INSTANTIATE, val, oldLB, oldUB);
        }
    }

    @Override
    public void activePropagator(BoolVar var, Propagator propagator) {
        synchronized (estore) {
            estore.pushEvent(var, propagator, PropagatorEventType.FULL_PROPAGATION, 0, 0, 0);
        }
    }

    @Override
    public Deduction explain(IntVar var, int val) {
        goOrWait();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("explain({}, {}) -- {} ({})", var, val, Thread.currentThread().equals(me), estore.getSize());
        }
        return mainEngine.explain(var, val);
    }

    @Override
    public Deduction explain(Deduction deduction) {
        goOrWait();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("explain({}) -- {} ({})", deduction, Thread.currentThread().equals(me), estore.getSize());
        }
        return mainEngine.explain(deduction);
    }

    @Override
    public Explanation flatten(IntVar var, int val) {
        goOrWait();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("flatten({}, {}) -- {} ({})", var, val, Thread.currentThread().equals(me), estore.getSize());
        }
        return mainEngine.flatten(var, val);
    }

    @Override
    public Explanation flatten(Explanation expl) {
        goOrWait();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("flatten({}) -- {} ({})", expl, Thread.currentThread().equals(me), estore.getSize());
        }
        return mainEngine.flatten(expl);
    }

    @Override
    public Explanation flatten(Deduction deduction) {
        goOrWait();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("flatten({}) -- {} ({})", deduction, Thread.currentThread().equals(me), estore.getSize());
        }
        return mainEngine.flatten(deduction);
    }

    @Override
    public Explanation retrieve(IntVar var, int val) {
        goOrWait();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("retrieve({}, {}) -- {} ({})", var, val, Thread.currentThread().equals(me), estore.getSize());
        }
        return mainEngine.retrieve(var, val);
    }

    @Override
    public void store(Deduction deduction, Explanation explanation) {
        goOrWait();
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("store({}, {}) -- {} ({})", deduction, explanation, Thread.currentThread().equals(me), estore.getSize());
        mainEngine.store(deduction, explanation);
    }

    @Override
    public void removeLeftDecisionFrom(Decision decision, Variable var) {
        goOrWait();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("removeLeftDecisionFrom({}, {}) -- {} ({})", decision, var, Thread.currentThread().equals(me), estore.getSize());
        }
        mainEngine.removeLeftDecisionFrom(decision, var);
    }


    @Override
    public BranchingDecision getDecision(Decision decision, boolean isLeft) {
        goOrWait();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getDecision({}, {}) -- {} ({})", decision, isLeft, Thread.currentThread().equals(me), estore.getSize());
        }
        return mainEngine.getDecision(decision, isLeft);
    }

    @Override
    public AntiDomain getRemovedValues(IntVar v) {
        goOrWait();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getRemovedValues({}) -- {} ({})", v, Thread.currentThread().equals(me), estore.getSize());
        }
        AntiDomain ad = mainEngine.getRemovedValues(v);
        LoggerFactory.getLogger("test").debug("AD {} : {} >> {}", v, ad, estore.getSize());
        return ad;
    }

    public ValueRemoval getValueRemoval(IntVar var, int val) {
        goOrWait();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getValueRemoval({},{}) -- {} ({})", var, val, Thread.currentThread().equals(me), estore.getSize());
        }
        return mainEngine.getValueRemoval(var, val);
    }

    @Override
    public PropagatorActivation getPropagatorActivation(Propagator propagator) {
        goOrWait();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getPropagatorActivation({}) -- {} ({})", propagator, Thread.currentThread().equals(me), estore.getSize());
        }
        return mainEngine.getPropagatorActivation(propagator);
    }

    private void goOrWait() {
        if (!Thread.currentThread().equals(me)) {
            while (estore.getSize() > 0) ;
        }
    }

    @Override
    public void beforeClose() {
        alive.set(false);
        try {
            me.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterClose() {

    }

    @Override
    public void beforeInitialPropagation() {

    }

    @Override
    public void afterInitialPropagation() {
        alive = new AtomicBoolean(true);
        me = new Thread(this);
        me.setDaemon(true);
        me.start();
    }

    @Override
    public void beforeUpBranch() {
        synchronized (estore) {
            estore.clear();
        }
    }

    @Override
    public void afterUpBranch() {

    }

}
