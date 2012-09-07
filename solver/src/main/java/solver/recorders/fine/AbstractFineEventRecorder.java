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
package solver.recorders.fine;

import choco.kernel.common.Indexable;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.IScheduler;
import solver.recorders.IActivable;
import solver.recorders.IEventRecorder;
import solver.search.loop.AbstractSearchLoop;
import solver.search.measure.IMeasures;
import solver.variables.EventType;
import solver.variables.Variable;

/**
 * An abstract class for fine event recorder.
 * A fine event is categorized by one or more event occurring on one or more variables.
 * It includes at least one variable and one propagator
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/12/11
 */
public abstract class AbstractFineEventRecorder<V extends Variable> implements IEventRecorder<V>,
        IActivable<Propagator<V>>, Indexable<V> {

    protected final AbstractSearchLoop loop;
    protected final IPropagationEngine engine;

    protected IScheduler scheduler = IScheduler.Default.NONE;
    protected int schedulerIdx = -1; // index in the scheduler if required, -1 by default;
    protected final IMeasures measures; // for timestamp
    protected boolean enqueued; // to check wether this is enqueud or not.

    protected final V[] variables;
    protected final Propagator<V>[] propagators;

    protected AbstractFineEventRecorder(V[] variables, Propagator<V>[] propagators, Solver solver, IPropagationEngine engine) {
        this.variables = variables;
        this.propagators = propagators;
        measures = solver.getMeasures();
        enqueued = false;
        schedulerIdx = -1;
        loop = solver.getSearchLoop();
        this.engine = engine;
    }

    @Override
    public final V[] getVariables() {
        return variables;
    }

    @Override
    public final Propagator<V>[] getPropagators() {
        return propagators;
    }

    @Override
    public boolean enqueued() {
        return enqueued;
    }

    @Override
    public void setScheduler(IScheduler scheduler, int idxInS) {
        this.scheduler = scheduler;
        this.schedulerIdx = idxInS;
    }

    @Override
    public IScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public int getIndexInScheduler() {
        return schedulerIdx;
    }

    @Override
    public void setIndexInScheduler(int sIdx) {
        this.schedulerIdx = sIdx;
    }


    public abstract void afterUpdate(int vIdx, EventType evt, ICause cause);

    /**
     * Set the event recorder in the same state as the one after its execution.
     * It is dequed, mask is void and delta monitor is unfreeze.
     *
     * @param propagator the propagator executed in the coarse recorder
     */
    public abstract void virtuallyExecuted(Propagator propagator);

    @Override
    public void activate(Propagator<V> element) {
        engine.activateFineEventRecorder(this);
    }

    @Override
    public void desactivate(Propagator<V> element) {
        engine.desactivateFineEventRecorder(this);
    }

    protected final void schedule() {
        if (!enqueued) {
            scheduler.schedule(this);
        } else if (scheduler.needUpdate()) {
            scheduler.update(this);
        }
    }

    protected final void execute(Propagator propagator, int idx, int mask) throws ContradictionException {
        assert (propagator.isActive()) : this + " is not active (" + propagator.isStateLess() + " & " + propagator.isPassive() + ")";
        propagator.fineERcalls++;
        propagator.propagate(idx, mask);
    }
}
