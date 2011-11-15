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

package solver.requests;

import choco.kernel.common.util.procedure.IntProcedure;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.graph.GraphVar;

public class GraphRequest<V extends GraphVar, P extends Propagator<V>> extends AbstractRequest<V, P> {

    //NR NE AR AE : NodeRemoved NodeEnforced ArcRemoved ArcEnforced
    final static int NR = 0;
    final static int NE = 1;
    final static int AR = 2;
    final static int AE = 3;

    int timestamp; // timestamp of the last clear call -- for lazy clear
    int[] first, last; // references, in variable delta value to propagate, to un propagated values
    int[] frozenFirst, frozenLast; // same as previous while the request is frozen, to allow "concurrent modifications"

    int evtmask; // reference to events occuring

    public GraphRequest(P propagator, V variable, int idxInProp) {
        super(propagator, variable, idxInProp);

        this.evtmask = 0;

        this.first = new int[4];
        this.last = new int[4];
        this.frozenFirst = new int[4];
        this.frozenLast = new int[4];
        this.timestamp = -1;
    }

    @Override
    public void forEach(IntProcedure proc) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(IntProcedure proc, int from, int to) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public int fromNodeRemoval() {
        return frozenFirst[NR];
    }

    public int toNodeRemoval() {
        return frozenLast[NR];
    }

    public int fromNodeEnforcing() {
        return frozenFirst[NE];
    }

    public int toNodeEnforcing() {
        return frozenLast[NE];
    }

    public int fromArcRemoval() {
        return frozenFirst[AR];
    }

    public int toArcRemoval() {
        return frozenLast[AR];
    }

    public int fromArcEnforcing() {
        return frozenFirst[AE];
    }

    public int toArcEnforcing() {
        return frozenLast[AE];
    }


    public static long calls, filter;

    @Override
    public void filter() throws ContradictionException {
        calls++;
        int evtmask_ = evtmask;
        // for concurrent modification..
        for (int i = 0; i < 4; i++) {
            this.frozenFirst[i] = first[i]; // freeze indices
            this.first[i] = this.frozenLast[i] = last[i];
        }
        this.evtmask = 0; // and clean up mask
        filter++;
        assert (propagator.isActive());
        propagator.propagateOnRequest(this, indices[VAR_IN_PROP], evtmask_);
    }

    private void addAll(EventType e) {
        if ((e.fullmask & evtmask) == 0) {
            evtmask |= e.fullmask;
        }
        switch (e) {//Otherwise the request will do a snapshot of a delta that may have not been cleared yet
            case REMOVENODE:
                last[NR] = variable.getDelta().getNodeRemovalDelta().size();
                break;
            case ENFORCENODE:
                last[NE] = variable.getDelta().getNodeEnforcingDelta().size();
                break;
            case REMOVEARC:
                last[AR] = variable.getDelta().getArcRemovalDelta().size();
                break;
            case ENFORCEARC:
                last[AE] = variable.getDelta().getArcEnforcingDelta().size();
                break;
        }
    }

    protected void lazyClear() {
        if (timestamp - AbstractSearchLoop.timeStamp != 0) {
            timestamp = AbstractSearchLoop.timeStamp;
            for (int i = 0; i < 4; i++) {
                this.evtmask = this.first[i] = this.last[i] = 0;
            }
        }
    }

    @Override
    public void update(EventType e) {
        // Only notify constraints that filter on the specific event received
        if ((e.mask & propagator.getPropagationConditions(indices[VAR_IN_PROP])) != 0) {
            lazyClear();
            addAll(e);
            engine.update(this);
        }
    }
}
