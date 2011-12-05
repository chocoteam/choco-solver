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

import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.Variable;

/**
 * A specialized fine event recorder associated with one variable and one propagator.
 * It observes a variable, records events occurring on the variable,
 * schedules it self when calling the filtering algortithm of the propagator
 * is required.
 * It also stores, if required, pointers to value removals.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/12/11
 */
public class ArcEventRecorder<V extends Variable> extends AbstractFineEventRecorder<V> {

    long timestamp; // timestamp of the last clear call -- for lazy clear

    protected final int[] indices;


    int first, last; // references, in variable delta value to propagate, to un propagated values
    int frozenFirst, frozenLast; // same as previous while the request is frozen, to allow "concurrent modifications"

    int evtmask; // reference to events occuring -- inclusive OR over event mask


    public ArcEventRecorder(V variable, Propagator<V> propagator, Solver solver) {
        //todo revoir la creation
        super((V[]) new Variable[]{variable}, new Propagator[]{propagator}, solver);
        this.indices = new int[]{-1, -1, -1, -1};
//        this.indices[VAR_IN_PROP] = idxInProp;
    }

    @Override
    public void schedule() {
        if (!enqueued()) {
            //engine.schedule(this);
        }
    }

    @Override
    public void execute() throws ContradictionException {
        if (evtmask > 0) {
            int evtmask_ = evtmask;
            // for concurrent modification..
            this.frozenFirst = first; // freeze indices
            this.first = this.frozenLast = last;
            this.evtmask = 0; // and clean up mask
            propagators[0].eventCalls++;
            assert (propagators[0].isActive()) : this + " is not active";
//            propagators[0].propagateOnRequest(this, indices[VAR_IN_PROP], evtmask_);
        }
    }

    @Override
    public void setIndex(int dim, int idx) {
    }

    @Override
    public int getIndex(int dim) {
        return 0;
    }

    @Override
    public void beforeUpdate(V var, EventType evt, ICause cause) {
        // nothing required here
    }

    @Override
    public void afterUpdate(V var, EventType evt, ICause cause) {
    }

    @Override
    public void contradict(V var, EventType evt, ICause cause) {
        // nothing required here
    }

    @Override
    public void enqueue() {
    }

    @Override
    public void deque() {
    }
}
