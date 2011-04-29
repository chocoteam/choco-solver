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

package solver.requests.list;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import solver.ICause;
import solver.constraints.propagators.Propagator;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.domain.delta.IDelta;
import solver.requests.IRequest;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/02/11
 */
public final class RequestTypedBitSetArrayList<R extends IRequest> implements IRequestList<R> {

    protected static int[] INDEX;

    static {
        INDEX = new int[(1 << 4) + 1];
        Arrays.fill(INDEX, -1);
        for (int j = 1; j < 5; j++) {
            INDEX[1 << j] = j - 1;
        }
    }

    protected R[] requests;
    protected IStateBitSet[] typedIdx;

    protected RequestTypedBitSetArrayList(IEnvironment env) {
        requests = (R[]) new IRequest[0];
        typedIdx = new IStateBitSet[4];
        for (int i = 0; i < 4; i++) {
            typedIdx[i] = env.makeBitSet(64);
        }
    }

    @Override
    public void setPassive(R request) {
        int idx = request.getIdxInVar();
        for (int j = 0; j < 4; j++) {
            typedIdx[j].set(idx, false);
        }
    }

    @Override
    public void addRequest(R request) {
        R[] tmp = requests;
        int size = tmp.length;
        requests = (R[]) new IRequest[size + 1];
        System.arraycopy(tmp, 0, requests, 0, size);
        requests[size] = request;
        request.setIdxInVar(size);

        int mask = request.getMask();
        for (int j = 0; j < 4; j++) {
            if ((mask & (1 << (j + 1))) != 0) {
                typedIdx[j].set(size, true);
            }
        }
    }

    @Override
    public void deleteRequest(IRequest request) {
        int i = 0;
        for (; i < requests.length && requests[i] != request; i++) {
        }
        if (i == requests.length) return;
        //remove requests
        R[] tmp = requests;
        requests = (R[]) new IRequest[tmp.length - 1];
        System.arraycopy(tmp, 0, requests, 0, i);
        System.arraycopy(tmp, i + 1, requests, i, tmp.length - i - 1);
        for (int j = i; j < requests.length; j++) {
            requests[j].setIdxInVar(j);
        }
        // remove indexes:
        for (int j = 0; j < 4; j++) {
            typedIdx[j].set(i, false);
        }
    }

    @Override
    public int size() {
        return requests.length;
    }

    @Override
    public int cardinality() {
        int cpt = 0;
        for (int i = 0; i < requests.length; i++) {
            if (requests[i].getPropagator().isActive()) {
                cpt++;
            }
        }
        return cpt;
    }

    @Override
    public void notifyButCause(ICause cause, EventType event, IDelta delta) {
        IRequest request;
        int mask = INDEX[event.mask];
        IStateBitSet _indices = typedIdx[mask];
        for (int i = _indices.nextSetBit(0); i >= 0; i = _indices.nextSetBit(i + 1)) {
            request = requests[i];
            Propagator<IntVar> o = request.getPropagator();
            if (o != cause) {
                request.update(event);
            }
        }

    }
}
