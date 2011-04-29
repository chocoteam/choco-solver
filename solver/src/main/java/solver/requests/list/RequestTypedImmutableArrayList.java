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
public final class RequestTypedImmutableArrayList<V extends IRequest> implements IRequestList<V> {

    protected static int[] INDEX;

    static {
        INDEX = new int[(1 << 4) + 1];
        Arrays.fill(INDEX, -1);
        for (int j = 1; j < 5; j++) {
            INDEX[1 << j] = j - 1;
        }
    }

    protected V[] requests;
    protected int[][] typedIdx;

    protected RequestTypedImmutableArrayList() {
        requests = (V[]) new IRequest[0];
        typedIdx = new int[4][0];
    }

    @Override
    public void setPassive(V request) {
    }

    @Override
    public void addRequest(V request) {
        V[] tmp = requests;
        int size = tmp.length;
        requests = (V[]) new IRequest[size + 1];
        System.arraycopy(tmp, 0, requests, 0, size);
        requests[size] = request;
        request.setIdxInVar(size);

        int mask = request.getMask();
        for (int j = 0; j < 4; j++) {
            if ((mask & (1 << (j + 1))) != 0) {
                int[] itmp = typedIdx[j];
                typedIdx[j] = new int[itmp.length + 1];
                System.arraycopy(itmp, 0, typedIdx[j], 0, itmp.length);
                typedIdx[j][itmp.length] = size;
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
        V[] tmp = requests;
        requests = (V[]) new IRequest[tmp.length - 1];
        System.arraycopy(tmp, 0, requests, 0, i);
        System.arraycopy(tmp, i + 1, requests, i, tmp.length - i - 1);
        for (int j = i; j < requests.length; j++) {
            requests[j].setIdxInVar(j);
        }
        // remove indexes:
        int oldidx = i;
        int mask = request.getMask();
        for (int j = 0; j < 4; j++) {
            if ((mask & (1 << (j + 1))) != 0) {
                for (; i < typedIdx[j].length && typedIdx[j][i] != oldidx; i++) {
                }
                int[] itmp = typedIdx[j];
                typedIdx[j] = new int[itmp.length - 1];
                System.arraycopy(itmp, 0, typedIdx[j], 0, i);
                System.arraycopy(itmp, i + 1, typedIdx[j], i, itmp.length - i - 1);
            }
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
        int[] _indices = typedIdx[mask];
        for (int i = 0; i < _indices.length; i++) {
            request = requests[_indices[i]];
            Propagator<IntVar> o = request.getPropagator();
            if (request.getPropagator().isActive()) {
                if (o != cause) {
                    request.update(event);
                }
            }
        }

    }
}
