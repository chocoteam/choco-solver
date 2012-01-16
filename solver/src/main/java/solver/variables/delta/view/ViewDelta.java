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
package solver.variables.delta.view;

import solver.variables.delta.IDeltaMonitor;
import solver.variables.delta.IntDelta;

/**
 * A view delta is designed to store nothing (like NoDelta),
 * but a delta monitor based on the variable observed.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/08/11
 */
public final class ViewDelta implements IntDelta {

    protected final IDeltaMonitor<IntDelta> deltaMonitor;

    public ViewDelta(IDeltaMonitor<IntDelta> deltaMonitor) {
        this.deltaMonitor = deltaMonitor;
    }

    @Override
    public IDeltaMonitor<IntDelta> getMonitor() {
        return deltaMonitor;
    }

    @Override
    public void add(int value) {
        throw new UnsupportedOperationException("Delta#add(int) unavailable for view");
    }

    @Override
    public int get(int idx) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException("Delta#get(int) unavailable for view");
    }

    @Override
    public int size() {
        return 0;
    }

	@Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
