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

import choco.kernel.common.util.procedure.IntProcedure;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.delta.IDeltaMonitor;
import solver.variables.delta.IntDelta;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/12/11
 */
public class IntDeltaMonitor implements IDeltaMonitor<IntDelta> {

    protected final IntDelta delta;
    protected int first, last, frozenFirst, frozenLast;

    public IntDeltaMonitor(IntDelta delta) {
        this.delta = delta;
        this.first = 0;
        this.last = 0;
        this.frozenFirst = 0;
        this.frozenLast = 0;
    }


    @Override
    public void update(EventType evt) {
        last = delta.size();
    }

    @Override
    public void freeze() {
        this.frozenFirst = first; // freeze indices
        this.frozenLast = last = delta.size();
    }

    @Override
    public void unfreeze() {
        //propagator is idempotent
        this.first = this.last = delta.size();
    }

    @Override
    public void clear() {
        this.first = this.last = 0;
    }

    @Override
    public void forEach(IntProcedure proc, EventType eventType) throws ContradictionException {
        if (EventType.isRemove(eventType.mask)) {
            for (int i = frozenFirst; i < frozenLast; i++) {
                proc.execute(delta.get(i));
            }
        }else{
			throw new UnsupportedOperationException();
		}
    }
}
