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
package solver.propagation.generator;

import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.IPropagationStrategy;
import solver.propagation.ISchedulable;
import solver.propagation.IScheduler;

/**
 * An abstract class for DSL to define a propagation strategy.
 * <br/>
 * It is an extension of group defined in: <br/>
 * "Propagator Groups", M.Z. Lagerkvist and C. Schulte -- 2009.
 * <br/>
 * A PropagationStrategy is a scheduler, because it stores elements to execute.
 * A PropagationStrategy is also schedulable in a master scheduler.
 *
 * @author Charles Prud'homme
 * @revision 04/03/12 add update feature
 * @since 15/12/11
 */
public abstract class PropagationStrategy<E extends ISchedulable> implements IPropagationStrategy<E> {

    static enum P {
        pickOne, sweepUp, clearOut, loopOut
    }

    protected E[] elements;
    protected P iteration = P.clearOut; // type of iteration
    protected IScheduler scheduler = IScheduler.Default.NONE;
    protected int schedulerIdx = -1; // index in the scheduler if required, -1 by default;
    protected boolean enqueued = false; // to check wether this is enqueud or not.
    protected IPropagationEngine engine;
    protected IEvaluator evaluator;

    protected PropagationStrategy(E... schedulables) {
        this.elements = schedulables;
    }

    public E[] array() {
        return elements;
    }

    //<-- DSL

    protected void set(P policy) {
        this.iteration = policy;
    }

    public final PropagationStrategy<E> pickOne() {
        set(P.pickOne);
        return this;
    }

    public final PropagationStrategy<E> sweepUp() {
        set(P.sweepUp);
        return this;
    }

    public final PropagationStrategy<E> clearOut() {
        set(P.clearOut);
        return this;
    }

    public final PropagationStrategy<E> loopOut() {
        set(P.loopOut);
        return this;
    }

    //-->
    //<-- PROPAGATION ENGINE

    @Override
    public IScheduler getScheduler() {
        return scheduler;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void setScheduler(IScheduler scheduler, int idxInS) {
        this.scheduler = scheduler;
        this.schedulerIdx = idxInS;

    }

    @Override
    public int getIndexInScheduler() {
        return schedulerIdx;
    }

    @Override
    public void setIndexInScheduler(int sIdx) {
        this.schedulerIdx = sIdx;
    }

    @Override
    public boolean enqueued() {
        return enqueued;
    }

    @Override
    public void enqueue() {
        enqueued = true;
    }

    @Override
    public void deque() {
        enqueued = false;
    }

    @Override
    public boolean needUpdate() {
        return false;
    }

    @Override
    public void update(E element) {
        // empty
    }

    public abstract int size();

    protected abstract boolean _pickOne() throws ContradictionException;

    protected abstract boolean _sweepUp() throws ContradictionException;

    protected abstract boolean _loopOut() throws ContradictionException;

    protected abstract boolean _clearOut() throws ContradictionException;

    @Override
    public boolean execute() throws ContradictionException {
        switch (iteration) {
            case pickOne:
                return _pickOne();
            case sweepUp:
                return _sweepUp();
            case loopOut:
                return _loopOut();
            case clearOut:
            default:
                return _clearOut();
        }
    }
    //-->

    public PropagationStrategy<E> duplicate() {
        throw new UnsupportedOperationException("unexpected call to Switcher, should be delegated");
    }

    @Override
    public void attachEvaluator(IEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public int evaluate() {
        return evaluator.eval(this);
    }
}
