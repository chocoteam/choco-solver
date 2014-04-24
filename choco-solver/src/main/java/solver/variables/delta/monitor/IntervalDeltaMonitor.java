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
package solver.variables.delta.monitor;

import solver.Cause;
import solver.ICause;
import solver.exception.ContradictionException;
import solver.search.loop.ISearchLoop;
import solver.variables.EventType;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.IIntervalDelta;
import util.procedure.IntProcedure;
import util.procedure.SafeIntProcedure;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/12/11
 */
public class IntervalDeltaMonitor implements IIntDeltaMonitor {

    protected final IIntervalDelta delta;
    protected int first, last, frozenFirst, frozenLast;
    protected ICause propagator;

    long timestamp = -1;
    final ISearchLoop loop;

    public IntervalDeltaMonitor(IIntervalDelta delta, ICause propagator) {
        this.delta = delta;
        loop = delta.getSearchLoop();
        this.first = 0;
        this.last = 0;
        this.frozenFirst = 0;
        this.frozenLast = 0;
        this.propagator = propagator;
    }

    @Override
    public void freeze() {
        assert delta.timeStamped() : "delta is not timestamped";
        lazyClear();
        this.frozenFirst = first; // freeze indices
        this.frozenLast = last = delta.size();
    }

    @Override
    public void unfreeze() {
        //propagator is idempotent
        delta.lazyClear();    // fix 27/07/12
        lazyClear();         // fix 27/07/12
        this.first = this.last = delta.size();
    }

    public void lazyClear() {
        if (timestamp - loop.getTimeStamp() != 0) {
            clear();
            timestamp = loop.getTimeStamp();
        }
    }

    @Override
    public void clear() {
        this.first = this.last = 0;
    }

    @Override
    public void forEach(SafeIntProcedure proc, EventType eventType) {
        if (EventType.isRemove(eventType.mask)) {
            for (int i = frozenFirst; i < frozenLast; i++) {
                if (propagator == Cause.Null || propagator != delta.getCause(i)) {
                    int lb = delta.getLB(i);
                    int ub = delta.getUB(i);
                    for (; lb <= ub; lb++) {
                        proc.execute(lb);
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void forEach(IntProcedure proc, EventType eventType) throws ContradictionException {
        if (EventType.isRemove(eventType.mask)) {
            for (int i = frozenFirst; i < frozenLast; i++) {
                if (propagator == Cause.Null || propagator != delta.getCause(i)) {
                    int lb = delta.getLB(i);
                    int ub = delta.getUB(i);
                    for (; lb <= ub; lb++) {
                        proc.execute(lb);
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return String.format("(%d,%d) => (%d,%d) :: %d", first, last, frozenFirst, frozenLast, delta.size());
    }
}
