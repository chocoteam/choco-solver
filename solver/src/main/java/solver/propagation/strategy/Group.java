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
package solver.propagation.strategy;

import solver.exception.ContradictionException;
import solver.propagation.ISchedulable;
import solver.propagation.IScheduler;

/**
 * An interface to describe strategies for propagation.
 * It is an extension of group defined in: <br/>
 * "Propagator Groups", M.Z. Lagerkvist and C. Schulte -- 2009.
 * <br/>
 * A Group is a scheduler, because it stores elements to execute.
 * A Group is also schedulable in a master scheduler.
 * A Group embeds two or more IEventRecorder or groups (or both).
 *
 * @author Charles Prud'homme
 * @since 08/12/11
 */
public abstract class Group implements IScheduler, ISchedulable {

    public static enum Iteration {
        PICK_ONE, SWEEP_UP, CLEAR_OUT
    }

    protected IScheduler scheduler = IScheduler.Default.NONE;
    protected int schedulerIdx = -1; // index in the scheduler if required, -1 by default;
    protected boolean enqueued; // to check wether this is enqueud or not.

    protected final Iteration iteration;

    public Group(Iteration iteration) {
        this.enqueued = false;
        this.iteration = iteration;
    }

    @Override
    public IScheduler getScheduler() {
        return scheduler;
    }

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

    protected abstract boolean pickOne() throws ContradictionException;

    protected abstract boolean sweepUp() throws ContradictionException;

    protected abstract boolean clearOut() throws ContradictionException;

    @Override
    public final boolean execute() throws ContradictionException {
        switch (iteration) {
            case PICK_ONE:
                return pickOne();
            case SWEEP_UP:
                return sweepUp();
            case CLEAR_OUT:
            default:
                return clearOut();
        }
    }
}
