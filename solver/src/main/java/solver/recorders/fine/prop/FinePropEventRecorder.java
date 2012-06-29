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
package solver.recorders.fine.prop;

import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.recorders.IEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.Arrays;

/**
 * An event recorder associated with one propagator and its variables.
 * <br/>
 *
 * @author Charles Prud'homme
 * @revision 05/24/12 remove timestamp and deltamonitoring
 * @since 24/01/12
 */
public class FinePropEventRecorder<V extends Variable> extends PropEventRecorder<V> {

    protected final int[] evtmasks; // reference to events occuring -- inclusive OR over event mask
    // BEWARE a variable can NOT occur more than one time in a propagator !!
    protected final int[] idxVinP; //; // index of each variable within P -- immutable

    public FinePropEventRecorder(V[] variables, Propagator<V> propagator, int[] idxVinPs, Solver solver, IPropagationEngine engine) {
        super(variables, propagator, solver, engine);
        this.evtmasks = new int[nbVar];
        this.idxVinP = idxVinPs.clone();
    }

    @Override
    public boolean execute() throws ContradictionException {
        if (IEventRecorder.DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("* {}", this.toString());
        for (int i = 0; i < nbVar; i++) {
            _execute(i);
        }
        return true;
    }

    protected final void _execute(int i) throws ContradictionException {
        int evtmask_ = evtmasks[i];
        if (evtmask_ > 0) {
//                LoggerFactory.getLogger("solver").info(">> {}", this.toString());
            evtmasks[i] = 0; // and clean up mask
            execute(propagators[AbstractFineEventRecorder.PINDEX], idxVinP[i], evtmask_);
        }
    }

    @Override
    public void afterUpdate(int vIdx, EventType evt, ICause cause) {
        // Only notify constraints that filter on the specific event received
        assert cause != null : "should be Cause.Null instead";
        if (cause != propagators[AbstractFineEventRecorder.PINDEX]) { // due to idempotency of propagator, it should not schedule itself
            if (IEventRecorder.DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("\t|- {}", this.toString());
            int idx = v2i[vIdx - offset];
            if ((evt.mask & propagators[AbstractFineEventRecorder.PINDEX].getPropagationConditions(idxVinP[idx])) != 0) {
                // 1. record the event and values removed
                evtmasks[idx] |= evt.strengthened_mask;
                schedule();
            }
        }
    }

    @Override
    public void flush() {
        Arrays.fill(this.evtmasks, 0, nbVar, 0);
    }

    @Override
    public void virtuallyExecuted(Propagator propagator) {
        assert this.propagators[AbstractFineEventRecorder.PINDEX] == propagator : "wrong propagator";
        Arrays.fill(evtmasks, 0, nbVar, 0);
        if (enqueued) {
            scheduler.remove(this);
        }
    }

    @Override
    public void desactivate(Propagator<V> element) {
        super.desactivate(element);
        Arrays.fill(evtmasks, 0, nbVar, 0);
    }

    @Override
    public String toString() {
        return "<< {F} " + Arrays.toString(variables) + "::" + propagators[AbstractFineEventRecorder.PINDEX].toString() + " >>";
    }
}
