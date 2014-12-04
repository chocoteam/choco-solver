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
import org.chocosolver.solver.explanations.store.ArrayEventStore;
import org.chocosolver.solver.explanations.store.IEventStore;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;

/**
 * A explanation engine that works in a lazy way.
 * It stores events on the fly, but do not compute anything until an explicit call is made.
 * However, it needs to be used AFTER RESTART only. Otherwise, it can be not synchronized anymore.
 *
 * @author Charles Prud'homme
 * @since 25/03/13
 */
public class LazyExplanationEngineFromRestart extends RecorderExplanationEngine {

    final IEventStore estore;

    /**
     * Builds an ExplanationEngine
     *
     * @param slv associated solver's environment
     */
    public LazyExplanationEngineFromRestart(Solver slv) {
        super(slv);
        estore = new ArrayEventStore(slv.getEnvironment());
    }


    @Override
    public void beforeInitialPropagation() {
        for (Variable v : solver.getVars()) {
            super.getRemovedValues((IntVar) v);
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
        estore.pushEvent(var, cause, IntEventType.REMOVE, val, 0, 0);
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
        estore.pushEvent(intVar, cause, IntEventType.INCLOW, old, value, 0);
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
        estore.pushEvent(var, cause, IntEventType.DECUPP, old, value, 0);
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
        estore.pushEvent(var, cause, IntEventType.INSTANTIATE, val, oldLB, oldUB);
    }

    @Override
    public void activePropagator(BoolVar var, Propagator propagator) {
        estore.pushEvent(var, propagator, PropagatorEventType.FULL_PROPAGATION, 0, 0, 0);
    }

    @Override
    public BranchingDecision getDecision(Decision decision, boolean isLeft) {
        BranchingDecision br = super.getDecision(decision, isLeft);
        if (!isLeft) {
            // a refutation is explained thanks to the previous ones which are refutable
            if (decision != RootDecision.ROOT) {
                Explanation explanation = database.get(br.getId());
                if (explanation == null) {
                    explanation = new Explanation();
                } else {
                    explanation.reset();
                }

                Decision d = decision.getPrevious();
                while ((d != RootDecision.ROOT)) {
                    if (d.hasNext()) {
                        explanation.add(d.getPositiveDeduction(this));
                    }
                    d = d.getPrevious();
                }
                this.store(br, explanation);
            }
        }
        return br;
    }

    // *********************** //

    private void playEvents() {
        estore.setUptodate(true);
        int to = estore.getSize();
        int one, two, three;
        IntVar var;
        IEventType etype;
        ICause cause;
        boolean bug;
        for (int c = 0; c < to; c++) {
            var = estore.getVariable(c);
            etype = estore.getEventType(c);
            cause = estore.getCause(c);
            one = estore.getFirstValue(c);

            bug = true;
            if (etype == IntEventType.REMOVE) {
                super.removeValue(var, one, cause);
                bug = false;
            }
            if (etype == IntEventType.INSTANTIATE) {
                two = estore.getSecondValue(c);
                three = estore.getThirdValue(c);
                super.instantiateTo(var, one, cause, two, three);
                bug = false;
            }
            if (etype == IntEventType.INCLOW) {
                two = estore.getSecondValue(c);
                super.updateLowerBound(var, one, two, cause);
                bug = false;
            }
            if (etype == IntEventType.DECUPP) {
                two = estore.getSecondValue(c);
                super.updateUpperBound(var, one, two, cause);
                bug = false;
            }
            if (etype == PropagatorEventType.FULL_PROPAGATION) {
                Propagator prop = (Propagator) cause;
                BoolVar bVar = (BoolVar) var;
                super.activePropagator(bVar, prop);
                bug = false;
            }
            if (bug) {
                throw new UnsupportedOperationException("Unknown type " + etype);
            }
        }

    }

    // *********************** //


    @Override
    public AntiDomain getRemovedValues(IntVar v) {
        if (!estore.isUptodate()) {
            playEvents();
        }
        return super.getRemovedValues(v);
    }

    public ValueRemoval getValueRemoval(IntVar var, int val) {
        if (!estore.isUptodate()) {
            playEvents();
        }
        return super.getValueRemoval(var, val);
    }

    @Override
    public PropagatorActivation getPropagatorActivation(Propagator propagator) {
        if (!estore.isUptodate()) {
            playEvents();
        }
        return super.getPropagatorActivation(propagator);
    }
}
