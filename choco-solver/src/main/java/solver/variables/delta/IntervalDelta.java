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

import solver.Configuration;
import solver.ICause;
import solver.search.loop.ISearchLoop;

/**
 * A class to store the removed intervals of an integer variable.
 * <p/>
 * It defines methods to <code>add</code> a value, <code>clear</code> the structure
 * and execute a <code>Procedure</code> for each value stored.
 */
public final class IntervalDelta extends AbstractDelta implements IIntervalDelta {
    private static final int SIZE = 32;

    int[] from;
    int[] to;
    ICause[] causes;
    int last;

    public IntervalDelta(ISearchLoop loop) {
		super(loop);
        from = new int[SIZE];
        to = new int[SIZE];
        causes = new ICause[SIZE];
    }

    private void ensureCapacity() {
        if (last >= from.length) {
            int[] tmp = new int[last * 3 / 2 + 1];
            System.arraycopy(from, 0, tmp, 0, last);
            from = tmp;
            tmp = new int[last * 3 / 2 + 1];
            System.arraycopy(to, 0, tmp, 0, last);
            to = tmp;
            ICause[] tmpc = new ICause[last * 3 / 2 + 1];
            System.arraycopy(causes, 0, tmpc, 0, last);
            causes = tmpc;
        }
    }

	@Override
    public void lazyClear() {
        if (timestamp - loop.getTimeStamp() != 0) {
            clear();
        }
    }

    @Override
    public void add(int lb, int ub, ICause cause) {
        if (Configuration.LAZY_UPDATE) {
            lazyClear();
        }
        ensureCapacity();
        causes[last] = cause;
        from[last] = lb;
        to[last++] = ub;
    }

    @Override
    public int getLB(int idx) {
        return from[idx];
    }

    @Override
    public int getUB(int idx) {
        return to[idx];
    }

    @Override
    public ICause getCause(int idx) {
        return causes[idx];
    }

    @Override
    public int size() {
        return last;
    }

    @Override
    public void clear() {
        last = 0;
        timestamp = loop.getTimeStamp();
    }
}
