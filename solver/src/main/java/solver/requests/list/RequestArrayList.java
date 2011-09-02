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
import choco.kernel.memory.IStateInt;
import com.sun.istack.internal.NotNull;
import solver.Cause;
import solver.ICause;
import solver.constraints.propagators.Propagator;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IDelta;

/**
 * [<--inactive-->|<--active--->|<---entailed-->]<br/>
 *
 * @author Charles Prud'homme
 * @since 23/02/11
 */
public final class RequestArrayList<R extends IRequest> implements IRequestList<R> {

    protected R[] requests;

    protected IStateInt firstActive;
    protected IStateInt firstPassive;

    protected RequestArrayList(IEnvironment environment) {
        requests = (R[]) new IRequest[0];
        firstActive = environment.makeInt();
        firstPassive = environment.makeInt();
    }

    @Override
    public void setActive(R request) {
        int first = this.firstActive.get();
        int i = request.getIdxInVar();
        if (first > i) {
            // swap element at pos "first" with element at pos "i"
            R tmp1 = requests[--first];
            requests[first] = requests[i];
            requests[first].setIdxInVar(first);
            requests[i] = tmp1;
            requests[i].setIdxInVar(i);
        }
        firstPassive.add(-1);
    }

    @Override
    public void setPassive(R request) {
        int last = this.firstPassive.get();
        int i = request.getIdxInVar();
        if (last > i) {
            // swap element at pos "last" with element at pos "i"
            R tmp1 = requests[--last];
            requests[last] = requests[i];
            requests[last].setIdxInVar(last);
            requests[i] = tmp1;
            requests[i].setIdxInVar(i);
        }
        firstPassive.add(-1);
    }


    @Override
    public void addRequest(R request) {
        if(firstActive.get()>0){
            throw new UnsupportedOperationException("Can not add a request: activation has already started");
        }
        R[] tmp = requests;
        requests = (R[]) new IRequest[tmp.length + 1];
        System.arraycopy(tmp, 0, requests, 0, tmp.length);
        requests[tmp.length] = request;
        request.setIdxInVar(tmp.length);
        this.firstPassive.add(1);
    }

    @Override
    public void deleteRequest(IRequest request) {
        int i = 0;
        for (; i < requests.length && requests[i] != request; i++) {
        }
        if (i == requests.length) return;
        R[] tmp = requests;
        requests = (R[]) new IRequest[tmp.length - 1];
        System.arraycopy(tmp, 0, requests, 0, i);
        System.arraycopy(tmp, i + 1, requests, i, tmp.length - i - 1);
        for (int j = i; j < requests.length; j++) {
            requests[j].setIdxInVar(j);
        }
        assert (this.firstPassive.getEnvironment().getWorldIndex() == 0);
        this.firstPassive.add(-1);
    }

    @Override
    public int size() {
        return requests.length;
    }

    @Override
    public int cardinality() {
        return firstPassive.get();
    }

    @Override
    public R get(int i) {
        return requests[i];
    }

    @Override
    public void notifyButCause(@NotNull ICause cause, EventType event, IDelta delta) {
        IRequest request;
        int first = firstActive.get();
        int last = firstPassive.get();
        assert cause != null : "should be Cause.Null instead";
        if (cause == Cause.Null) {
            for (int a = first; a < last; a++) {
                request = requests[a];
                request.update(event);
            }
        } else {
            //TODO: get the id of the cause, to avoid testing it at each iteration
            for (int a = first; a < last; a++) {
                request = requests[a];
                Propagator<IntVar> o = request.getPropagator();
                if (o != cause) {
                    request.update(event);
                }
            }
        }
    }


}
