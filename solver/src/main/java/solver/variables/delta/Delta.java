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

package solver.variables.delta;

import solver.ICause;
import solver.recorders.IEventRecorder;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.delta.monitor.IntDeltaMonitor;

/**
 * A class to store the removed value of an integer variable.
 * <p/>
 * It defines methods to <code>add</code> a value, <code>clear</code> the structure
 * and execute a <code>Procedure</code> for each value stored.
 */
public final class Delta implements IntDelta {

	int[] rem;
	ICause[] causes;
	int last;
    int timestamp = -1;

    public Delta() {
        rem = new int[16];
        causes = new ICause[16];
    }

    @Override
    public IntDeltaMonitor getMonitor(ICause propagator) {
        return new IntDeltaMonitor(this,propagator);
    }

	private void ensureCapacity() {
        if (last >= rem.length) {
            int[] tmp = new int[last * 3 / 2 + 1];
			ICause[] tmpc = new ICause[last * 3 / 2 + 1];
			System.arraycopy(rem, 0, tmp, 0, last);
			System.arraycopy(causes, 0, tmpc, 0, last);
			rem = tmp;
			causes = tmpc;
        }
    }

    public void lazyClear() {
        if (timestamp - AbstractSearchLoop.timeStamp != 0) {
            clear();
        }
    }

    /**
     * Adds a new value to the delta
     *
     * @param value value to add
     * @param cause of the removal
     */
    public void add(int value, ICause cause) {
		if(IEventRecorder.LAZY){
       		lazyClear();
		}
        ensureCapacity();
        causes[last] = cause;
        rem[last++] = value;
    }



    @Override
    public int get(int idx) {
        return rem[idx];
    }

	@Override
    public ICause getCause(int idx) {
        return causes[idx];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return last;
    }

	@Override
    public void clear() {
        last = 0;
		timestamp = AbstractSearchLoop.timeStamp;
    }
}
