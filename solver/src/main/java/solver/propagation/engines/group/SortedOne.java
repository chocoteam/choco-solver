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

import solver.requests.IRequest;

import java.util.BitSet;
import java.util.Comparator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/04/11
 */
public abstract class SortedOne extends AFixpointReacher {

    protected final IRequest[] requests;

    protected final int size;

    protected final BitSet toPropagate;

    protected IRequest lastPoppedRequest;


    public SortedOne(IRequest[] requests, Comparator<IRequest> comparator) {
        super(comparator);
        this.requests = requests;
        size = requests.length;
        this.toPropagate = new BitSet(size);
    }


    @Override
    public void update(IRequest request) {
        update++;
        if (!request.enqueued()) {
            toPropagate.set(request.getIndex(), true);
            request.enqueue();
            pushed++;
        }
    }

    @Override
    public boolean remove(IRequest request) {
        request.deque();
        toPropagate.set(request.getIndex(), false);
        return toPropagate.isEmpty();
    }

    @Override
    public void flushAll() {
        for (int i = toPropagate.nextSetBit(0); i >= 0; i = toPropagate.nextSetBit(i + 1)) {
            requests[i].deque();
            toPropagate.set(i, false);
        }
    }

    public String toString() {
        return "Sorted";
    }

}
