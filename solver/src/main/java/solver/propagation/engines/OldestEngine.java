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

package solver.propagation.engines;

import solver.exception.ContradictionException;
import solver.propagation.engines.group.Group;
import solver.propagation.engines.queues.aqueues.FixSizeCircularQueue;
import solver.requests.IRequest;

import java.util.BitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/04/11
 */
public class OldestEngine implements IEngine {

    protected final Group[] groups;

    protected final int nbGroup;

    protected BitSet active;

    //fixme: debug fixsized circular queue!
    protected final FixSizeCircularQueue<Group> queue;

    protected Group lastGroup;

    public OldestEngine(Group[] groups) {
        this.groups = groups;
        nbGroup = groups.length;
        this.queue = new FixSizeCircularQueue<Group>(nbGroup);
        active = new BitSet(nbGroup);
    }

    @Override
    public void fixPoint() throws ContradictionException {
        while (!queue.isEmpty()) {
            lastGroup = queue.remove();
            if (!lastGroup.fixpoint()) {
                queue.add(lastGroup);
            } else {
                active.set(lastGroup.getIndex(), false);
            }
        }
    }

    @Override
    public void update(IRequest request) {
        int gidx = request.getGroup();
        Group g = groups[gidx];
        g.update(request);
        if (!active.get(gidx)) {
            queue.add(g);
            active.set(gidx, true);
        }
    }

    @Override
    public void remove(IRequest request) {
        int gidx = request.getGroup();
        Group g = groups[gidx];
        if (g.remove(request)) {
            queue.remove(g);
            active.set(gidx, false);
        }
    }

    @Override
    public void flushAll() {
        for (int i = nbGroup - 1; i >= 0; i--) {
            groups[i].flushAll();
        }
        queue.clear();
        active.clear();
    }
}
