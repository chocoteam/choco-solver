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
package org.chocosolver.solver.explanations.store;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.explanations.BranchingDecision;
import org.chocosolver.solver.explanations.Deduction;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.antidom.AntiDomain;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;

/**
 * Created by cprudhom on 18/11/14.
 * Project: choco.
 */
public class BufferedEventStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(BufferedEventStore.class);

    private final ExplanationEngine delegate;

    private final ArrayDeque<IntVar> varChunks;
    private final ArrayDeque<ICause> cauChunks;
    private final ArrayDeque<IEventType> masChunks;
    private final ArrayDeque<Integer> val1Chunks;
    private final ArrayDeque<Integer> val2Chunks;
    private final ArrayDeque<Integer> val3Chunks;
    volatile int size;


    public BufferedEventStore(ExplanationEngine receiver) {
        delegate = receiver;
        varChunks = new ArrayDeque<>();
        cauChunks = new ArrayDeque<>();
        masChunks = new ArrayDeque<>();
        val1Chunks = new ArrayDeque<>();
        val2Chunks = new ArrayDeque<>();
        val3Chunks = new ArrayDeque<>();
        size = 0;
    }


    public synchronized void push(IntVar var, ICause cause, IEventType etype, int one, int two, int three) {
        varChunks.addLast(var);
        cauChunks.addLast(cause);
        masChunks.addLast(etype);
        val1Chunks.addLast(one);
        val2Chunks.addLast(two);
        val3Chunks.addLast(three);
        size++;
        LOGGER.debug("PUSH: <{},{},{},{},{},{}>", var, cause, etype, one, two, three);

    }

    public synchronized void pop() {
        if (size > 0) {
            IntVar var;
            IEventType etype;
            ICause cause;
            int one, two, three;
            var = varChunks.removeFirst();
            etype = masChunks.removeFirst();
            cause = cauChunks.removeFirst();
            one = val1Chunks.removeFirst();
            two = val2Chunks.removeFirst();
            three = val3Chunks.removeFirst();

            LOGGER.debug("POP: <{},{},{},{},{},{}> ({})", var, cause, etype, one, two, three, size - 1);

            if (etype == IntEventType.REMOVE) {
                delegate.removeValue(var, one, cause);
            } else if (etype == IntEventType.INSTANTIATE) {
                delegate.instantiateTo(var, one, cause, two, three);
            } else if (etype == IntEventType.INCLOW) {
                delegate.updateLowerBound(var, one, two, cause);
            } else if (etype == IntEventType.DECUPP) {
                delegate.updateUpperBound(var, one, two, cause);
            } else if (etype == PropagatorEventType.FULL_PROPAGATION) {
                Propagator prop = (Propagator) cause;
                BoolVar bVar = (BoolVar) var;
                delegate.activePropagator(bVar, prop);
            }
            size--;
        } else {
            notify();
        }
    }

    public Deduction explain(IntVar var, int val) {
        return delegate.explain(var, val);
    }

    public Explanation flatten(Explanation expl) {
        return delegate.flatten(expl);
    }


    public void store(Deduction deduction, Explanation explanation) {
        delegate.store(deduction, explanation);
    }


    public void removeLeftDecisionFrom(Decision decision, Variable var) {
        delegate.removeLeftDecisionFrom(decision, var);
    }


    public BranchingDecision getDecision(Decision decision, boolean isLeft) {
        return delegate.getDecision(decision, isLeft);
    }


    public AntiDomain getRemovedValues(IntVar v) {
        return delegate.getRemovedValues(v);
    }

    public synchronized void hurryUp() {
        LOGGER.debug("hurryUp! {}", size);
        if (size > 0) {
            LOGGER.debug("LOCKED hurryUp -> wait");
            try {
                wait();
            } catch (InterruptedException ex) {
                LOGGER.error("{}", ex);
            }
        }
        LOGGER.debug("RUN hurryUp! ");
    }
}
