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
package solver.propagation.engines.concurrent;

import solver.requests.IRequest;

import java.util.BitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class Sequencer {

    private final IRequest[] requests;
    private BitSet toPropagate; // indices of requests to propagate
    private int[] forbidden; // indices of forbidden variables for active requests

    public Sequencer(IRequest[] requests, int nbRequests, int nbVars) {
        this.requests = requests;
        this.toPropagate = new BitSet(nbRequests);
        forbidden = new int[nbVars];
    }

    public synchronized int getFreeRequestId() {
        int idx = toPropagate.nextSetBit(0);
        while (idx > -1
                && conditions(requests[idx].getVariable().getUniqueID())) {
            idx = toPropagate.nextSetBit(idx + 1);
        }
        if (idx > -1) {
            IRequest request = requests[idx];
            int to = request.getPropagator().getNbVars();
            for (int i = 0; i < to; i++) {
                forbidd(request.getPropagator().getVar(i).getUniqueID());
            }
            toPropagate.set(idx, false);
        }
        return idx;
    }

    private boolean conditions(long uid) {
        return (forbidden[(int) uid] > 0);
    }

    public synchronized void allow(int vUid) {
        forbidden[vUid]--;
    }

    public synchronized void forbidd(int vUid) {
        forbidden[vUid]++;
    }

    public synchronized int cardinality() {
        return toPropagate.cardinality();
    }

    public synchronized void set(int index, boolean value) {
        toPropagate.set(index, value);
    }

    public synchronized void flushAll() {
        for (int i = toPropagate.nextSetBit(0); i >= 0; i = toPropagate.nextSetBit(i + 1)) {
            requests[i].deque();
        }
        toPropagate.clear();
    }

}
