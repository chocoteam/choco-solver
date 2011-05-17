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

package solver.propagation.engines.group;

import solver.exception.ContradictionException;
import solver.propagation.engines.queues.aqueues.FixSizeCircularQueue;
import solver.requests.IRequest;

/**
 * An implementation of <code>IPropagationEngine</code>.
 * It deals with 2 main types of <code>IRequest</code>s.
 * Ones are intialized once (at the end of the list).
 * <p/>
 * Created by IntelliJ IDEA.
 * User: cprudhom
 * Date: 28 oct. 2010
 */
public final class Oldest extends AFixpointReacher {

    protected IRequest lastPoppedRequest;

    protected FixSizeCircularQueue<IRequest> toPropagate;

    public Oldest(int nbElement) {
        toPropagate = new FixSizeCircularQueue<IRequest>(nbElement);
    }

    @Override
    public boolean fixpoint() throws ContradictionException {
        while (!toPropagate.isEmpty()) {
            lastPoppedRequest = toPropagate.pop();
            lastPoppedRequest.deque();
            lastPoppedRequest.filter();
        }
        return true;
    }

    @Override
    public void update(IRequest request) {
        if (!request.enqueued()) {
            toPropagate.add(request);
            request.enqueue();
        }

    }

    @Override
    public boolean remove(IRequest request) {
        request.deque();
        toPropagate.remove(request);
        return toPropagate.isEmpty();
    }

    @Override
    public void flushAll() {
        while (!toPropagate.isEmpty()) {
            lastPoppedRequest = toPropagate.pop();
            lastPoppedRequest.deque();
        }
    }

    public String toString() {
        return "FIXPOINT:Queue";
    }
}
