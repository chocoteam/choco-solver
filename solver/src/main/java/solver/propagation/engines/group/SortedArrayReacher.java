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
import solver.requests.IRequest;

import java.util.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/04/11
 */
public final class SortedArrayReacher implements IReacher {

    SortedSet<IRequest> requests;

    protected IRequest lastPoppedRequest;


    public SortedArrayReacher(IRequest[] requests, Comparator<IRequest> comparator) {
        this.requests = new TreeSet<IRequest>(comparator);
    }

    @Override
    public boolean one() throws ContradictionException {
        lastPoppedRequest = requests.first();
        requests.remove(lastPoppedRequest);
        lastPoppedRequest.deque();
        lastPoppedRequest.filter();
        return requests.isEmpty();
    }

    @Override
    public boolean iterate() throws ContradictionException {
        return all();
    }

    @Override
    public boolean all() throws ContradictionException {
        while (!requests.isEmpty()) {
            lastPoppedRequest = requests.first();
            requests.remove(lastPoppedRequest);
            lastPoppedRequest.deque();
            lastPoppedRequest.filter();
        }
        return true;
    }

    @Override
    public void update(IRequest request) {
        requests.add(request);
        request.enqueue();
    }

    @Override
    public boolean remove(IRequest request) {
        request.deque();
        requests.remove(request);
        return requests.isEmpty();
    }

    @Override
    public void flushAll() {
        for (int i = 0; i < requests.size(); i++) {
            requests.first().deque();
        }
    }

    public String toString() {
        return "Sorted";
    }

}
