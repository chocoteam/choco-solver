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
package solver.recorders.coarse;

import choco.kernel.common.util.procedure.UnaryProcedure;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IScheduler;
import solver.recorders.IEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.Variable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/12/11
 */
public abstract class AbstractCoarseEventRecorder implements IEventRecorder<Variable> {

    protected IScheduler scheduler = IScheduler.Default.NONE;
    protected int schedulerIdx = -1; // index in the scheduler if required, -1 by default;
    protected boolean enqueued; // to check wether this is enqueud or not.
	protected final AbstractSearchLoop loop;

    protected static final UnaryProcedure<AbstractFineEventRecorder, Propagator> virtExec = new UnaryProcedure<AbstractFineEventRecorder, Propagator>() {

        Propagator coarseProp;

        @Override
        public UnaryProcedure set(Propagator propagator) {
            coarseProp = propagator;
            return this;
        }

        @Override
        public void execute(AbstractFineEventRecorder abstractFineEventRecorder) throws ContradictionException {
            abstractFineEventRecorder.virtuallyExecuted(coarseProp);
        }
    };

    protected AbstractCoarseEventRecorder(AbstractSearchLoop loop) {
        this.enqueued = false;
		this.loop = loop;
    }

    public abstract void update(EventType e);

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

    @Override
    public Variable[] getVariables() {
        return new Variable[0];
    }

    @Override
    public boolean enqueued() {
        return enqueued;
    }

    @Override
    public void deque() {
        enqueued = false;
    }

    @Override
    public void enqueue() {
        enqueued = true;
    }
}
