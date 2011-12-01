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
import solver.variables.Variable;

/**
 * A propagation request storing events occuring on a variable to inform a propagator.
 * It stores mask event (type of event) and pointers to removed values to propagate (if any).
 * <br/>
 * These paramaters are lazy cleared when necessary: usually before updating the request and before treating events.
 * </br>
 *
 * @author Charles Prud'homme
 * @since 23 sept. 2010
 */
public class EventRequest<V extends Variable> extends AbstractRequestWithVar<V> {

    int timestamp; // timestamp of the last clear call -- for lazy clear

    int first, last; // references, in variable delta value to propagate, to un propagated values
    int frozenFirst, frozenLast; // same as previous while the request is frozen, to allow "concurrent modifications"

    int evtmask; // reference to events occuring -- inclusive OR over event mask

    public EventRequest(Propagator<V> propagator, V variable, int idxInProp) {
        super(propagator, variable, idxInProp);

        this.evtmask = 0;
        this.first = 0;
        this.last = 0;
        this.frozenFirst = 0;
        this.frozenLast = 0;
        this.timestamp = -1;
    }

    @Override
    public void forEach(IntProcedure proc) throws ContradictionException {
        variable.getDelta().forEach(proc, frozenFirst, frozenLast);
    }

    @Override
    public void forEach(IntProcedure proc, int from, int to) throws ContradictionException {
        variable.getDelta().forEach(proc, from, to);
    }

    @Override
    public void filter() throws ContradictionException {
        //LoggerFactory.getLogger("solver").info("{} filter on {}", this.toString(), evtmask);
        if (evtmask > 0) {
            int evtmask_ = evtmask;
            // for concurrent modification..
            this.frozenFirst = first; // freeze indices
            this.first = this.frozenLast = last;
            this.evtmask = 0; // and clean up mask
            propagator.eventCalls++;
            assert (propagator.isActive()) : this + " is not active";
            propagator.propagateOnRequest(this, indices[VAR_IN_PROP], evtmask_);
        }
    }


    private void addAll(EventType e) {
        if ((e.mask & evtmask) == 0) { // if the event has not been recorded yet (through strengthened event also).
            evtmask |= e.strengthened_mask;
        }
        last = variable.getDelta().size();
    }

    protected void lazyClear() {
        if (timestamp - AbstractSearchLoop.timeStamp != 0) {
            this.evtmask = this.first = this.last = 0;
            timestamp = AbstractSearchLoop.timeStamp;
        }
    }

    @Override
    public void update(EventType e) {
//        LoggerFactory.getLogger("solver").info("\tfilter on {}", this.toString());
        // Only notify constraints that filter on the specific event received
        if ((e.mask & propagator.getPropagationConditions(indices[VAR_IN_PROP])) != 0) {
            lazyClear();
            if (EventType.anInstantiationEvent(e.mask)) {
                propagator.decArity();
            }
            addAll(e);
            schedule();
        }
    }

    @Override
    public String toString() {
        return "(" + variable.getName() + " :: " + propagator.getConstraint().toString() + ")";
    }

    @Override
    public void desactivate() {
        super.desactivate();
//        LoggerFactory.getLogger("solver").info("{} >> {} clean mask", Thread.currentThread().toString(),this.toString());
        evtmask = 0;
    }
}
