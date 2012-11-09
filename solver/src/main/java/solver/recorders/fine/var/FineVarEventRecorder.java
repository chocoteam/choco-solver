/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.recorders.fine.var;

import org.slf4j.LoggerFactory;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.Arrays;

/**
 * A specialized fine event recorder associated with one variable and two or more propagators.
 * It observes a variable, records events occurring on the variable,
 * schedules it self when calling the filtering algortithm of the propagators
 * is required.
 * It also stores, if required, pointers to value removals.
 * <br/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @revision 05/24/12 remove timestamp and deltamonitoring
 * @since 06/12/11
 */
public class FineVarEventRecorder<V extends Variable> extends VarEventRecorder<V> {

    //BEWARE a variable can NOT occur more than one time into a propagator
    protected final int[] idxVinPs; // index of the variable within the propagator -- immutable
    protected int evtmasks[]; // reference to events occuring -- inclusive OR over event mask
    private boolean flag_swap_during_execution = false; // a flag to capture propagator swapping during its execution

    public FineVarEventRecorder(V variable, Propagator<V>[] props, int[] idxVinP, Solver solver, IPropagationEngine engine) {
        super(variable, props, solver, engine);
        this.idxVinPs = idxVinP.clone();
        this.evtmasks = new int[props.length];
    }

    @Override
    public boolean execute() throws ContradictionException {
        if (Configuration.PRINT_PROPAGATION) LoggerFactory.getLogger("solver").info("* {}", this.toString());
        int first = firstAP.get();
        int last = firstPP.get();
        flag_swap_during_execution = false;
        for (int k = first; k < last; k++) {
            _execute(propIdx[k]);
            if (flag_swap_during_execution) {
                flag_swap_during_execution = false;
                last--;
                k--;
                //<cp> if the propagator has been passivate, this has been updated
                // and the deltamonitor is already clear() --> no need to unfreeze.
            }
        }
        return true;
    }

    private void _execute(int i) throws ContradictionException {
        assert p2i[propagators[i].getId() - offset] == i;
        int evtmask_ = evtmasks[i];
        if (evtmask_ > 0) {
//          LoggerFactory.getLogger("solver").info(">> {}", this.toString());
            evtmasks[i] = 0; // and clean up mask
            execute(propagators[i], idxVinPs[i], evtmask_);
        }
    }

    @Override
    public void afterUpdate(int vIdx, EventType evt, ICause cause) {
// Only notify constraints that filter on the specific event received
        assert cause != null : "should be Cause.Null instead";
        if (Configuration.PRINT_PROPAGATION) LoggerFactory.getLogger("solver").info("\t|- {}", this.toString());
        boolean atleastone = false;
        int first = firstAP.get();
        int last = firstPP.get();
        for (int k = first; k < last; k++) {
            int i = propIdx[k];
            Propagator propagator = propagators[i];
            if (cause != propagator) { // due to idempotency of propagator, it should not schedule itself
//                int idx = p2i[propagator.getId() - offset];
                assert p2i[propagator.getId() - offset] == i;
                if (propagator.advise(idxVinPs[i], evt.mask)) {
                    // record the event and values removed
                    if ((evt.mask & evtmasks[i]) == 0) { // if the event has not been recorded yet (through strengthened event also).
                        evtmasks[i] |= evt.strengthened_mask;
                    }
                    atleastone = true;
                }
            }
        }
        if (atleastone) {
            schedule();
        }
    }

    @Override
    public void flush() {
        int first = firstAP.get();
        int last = firstPP.get();
        for (int k = first; k < last; k++) {
            int i = propIdx[k];
            assert p2i[propagators[i].getId() - offset] == i;
            this.evtmasks[i] = 0;
        }
    }

    void _desactivateP(int i) {
        this.evtmasks[i] = 0;
        // inform this that the structure has changed
        // required to handle side effect in this#execute()
        flag_swap_during_execution = true;
    }

    @Override
    public void virtuallyExecuted(Propagator propagator) {
        int idx = p2i[propagator.getId() - offset];
        this.evtmasks[idx] = 0;

        //TODO: to remove when active propagators will be managed smartly
        boolean canDeque = true;
        int first = firstAP.get();
        int last = firstPP.get();
        for (int k = first; k < last; k++) {
            if (this.evtmasks[propIdx[k]] != 0) {
                canDeque = false;
            }
        }
        if (enqueued && canDeque) {
            scheduler.remove(this);
        }
    }

    @Override
    public String toString() {
        return "<< {F} " + variables[AbstractFineEventRecorder.VINDEX].toString() + "::" + Arrays.toString(propagators) + " >>";
    }
}
