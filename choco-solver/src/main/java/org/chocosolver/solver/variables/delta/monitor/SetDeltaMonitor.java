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
package org.chocosolver.solver.variables.delta.monitor;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.TimeStampedObject;
import org.chocosolver.solver.variables.delta.ISetDelta;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public class SetDeltaMonitor extends TimeStampedObject implements ISetDeltaMonitor {

    protected final ISetDelta delta;

    protected int[] first, last; // references, in variable delta value to propagate, to un propagated values
    protected int[] frozenFirst, frozenLast; // same as previous while the recorder is frozen, to allow "concurrent modifications"
    protected ICause propagator;

    public SetDeltaMonitor(ISetDelta delta, ICause propagator) {
		super(delta.getSearchLoop());
        this.delta = delta;
        this.first = new int[2];
        this.last = new int[2];
        this.frozenFirst = new int[2];
        this.frozenLast = new int[2];
        this.propagator = propagator;
    }

    @Override
    public void freeze() {
		if (needReset()) {
			for (int i = 0; i < 2; i++) {
				this.first[i] = last[i] = 0;
			}
			resetStamp();
		}
        for (int i = 0; i < 2; i++) {
            this.frozenFirst[i] = first[i]; // freeze indices
            this.first[i] = this.frozenLast[i] = last[i] = delta.getSize(i);
        }
    }

    @Override
    public void unfreeze() {
        delta.lazyClear();    // fix 27/07/12
        resetStamp();
        for (int i = 0; i < 2; i++) {
            this.first[i] = last[i] = delta.getSize(i);
        }
    }

    @Override
    public void forEach(IntProcedure proc, SetEventType evt) throws ContradictionException {
        int x;
        if (evt == SetEventType.ADD_TO_KER) {
            x = ISetDelta.KERNEL;
        } else if (evt == SetEventType.REMOVE_FROM_ENVELOPE) {
            x = ISetDelta.ENVELOP;
        } else {
            throw new UnsupportedOperationException("The event in parameter should be ADD_TO_KER or REMOVE_FROM_ENVELOPE");
        }
        for (int i = frozenFirst[x]; i < frozenLast[x]; i++) {
            if (delta.getCause(i, x) != propagator) {
                proc.execute(delta.get(i, x));
            }
        }
    }
}
