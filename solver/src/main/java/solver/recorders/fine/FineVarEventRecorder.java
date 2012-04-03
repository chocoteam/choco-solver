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
package solver.recorders.fine;

import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.delta.IDelta;
import solver.variables.delta.IDeltaMonitor;

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
 * @since 06/12/11
 */
public class FineVarEventRecorder<V extends Variable> extends VarEventRecorder<V> {

    protected int[][] idxVinPs; // index of the variable within the propagator -- immutable
    protected IDeltaMonitor[] deltamon; // delta monitoring -- can be NONE
    protected long[] timestamps; // a timestamp lazy clear the event structures
    //BEWARE a variable can occur more than one time into a propagator
    protected int evtmasks[]; // reference to events occuring -- inclusive OR over event mask
    private boolean flag_swap_during_execution = false; // a flag to capture propagator swapping during its execution

    public FineVarEventRecorder(V variable, Propagator<V>[] props, int[] idxVinP, Solver solver) {
        super(variable, solver, props.length);
        int n = props.length;
        // CREATE EMPTY STRUCTURE
        this.deltamon = new IDeltaMonitor[n];
        this.idxVinPs = new int[n][];

        IDelta delta = variable.getDelta();
        int k = 0; // count the number of distinct propagator
        for (int i = 0; i < n; i++) {
            Propagator propagator = props[i];
            int pid = propagator.getId();
            int idx = p2i.get(pid);
            if (idx == -1) { // first occurrence of the variable
                this.propagators[k] = propagator;
                propagator.addRecorder(this);
                p2i.put(pid, k);
                propIdx[k] = k;
                deltamon[k] = delta.createDeltaMonitor(propagator);
                idxVinPs[k] = new int[]{idxVinP[i]};
                k++;
            } else {
                int[] itmp = idxVinPs[idx];
                idxVinPs[idx] = new int[itmp.length + 1];
                System.arraycopy(itmp, 0, idxVinPs[idx], 0, itmp.length);
                idxVinPs[idx][itmp.length] = idxVinP[i];
            }
        }
        if (k < n) {
            propagators = Arrays.copyOfRange(propagators, 0, k);
            propIdx = Arrays.copyOfRange(propIdx, 0, k);
            deltamon = Arrays.copyOfRange(deltamon, 0, k);
            idxVinPs = Arrays.copyOfRange(idxVinPs, 0, k);
        }
        this.evtmasks = new int[k];
        this.timestamps = new long[k];
        Arrays.fill(timestamps, -1);

        firstAP = solver.getEnvironment().makeInt(k);
        firstPP = solver.getEnvironment().makeInt(k);
    }

    @Override
    public boolean execute() throws ContradictionException {
        if (DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("* {}", this.toString());
        int first = firstAP.get();
        int last = firstPP.get();
        flag_swap_during_execution = false;
        for (int k = first; k < last; k++) {
            int i = propIdx[k];
            Propagator propagator = propagators[i];
            int idx = p2i.get(propagator.getId());
            int evtmask_ = evtmasks[idx];
            if (evtmask_ > 0) {
//                  LoggerFactory.getLogger("solver").info(">> {}", this.toString());
                // for concurrent modification..
                deltamon[idx].freeze();
                evtmasks[idx] = 0; // and clean up mask
                assert (propagator.isActive()) : this + " is not active";
                for (int j = 0; j < idxVinPs[idx].length; j++) {
                    propagator.fineERcalls++;
                    propagator.propagate(this, idxVinPs[idx][j], evtmask_);
                    if (flag_swap_during_execution) {
                        flag_swap_during_execution = false;
                        assert (propagator.isPassive()) : this + " is not passive";
                        last--;
                        k--;
                        break;
                    }
                    //<cp> if the propagator has been passivate, this has been updated
                    // and the deltamonitor is already clear() --> no need to unfreeze.
                }
                deltamon[idx].unfreeze();
            }

        }
        return true;
    }

    @Override
    public void afterUpdate(V var, EventType evt, ICause cause) {
// Only notify constraints that filter on the specific event received
        assert cause != null : "should be Cause.Null instead";
        if (DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("\t|- {}", this.toString());
        boolean oneoremore = false;
        int first = firstAP.get();
        int last = firstPP.get();
        for (int k = first; k < last; k++) {
            int i = propIdx[k];
            Propagator propagator = propagators[i];
            if (cause != propagator) { // due to idempotency of propagator, it should not schedule itself
                int idx = p2i.get(propagator.getId());
                for (int j = 0; j < idxVinPs[idx].length; j++) {
                    if ((evt.mask & propagator.getPropagationConditions(idxVinPs[idx][j])) != 0) {
                        // 1. if instantiation, then decrement arity of the propagator
                        if (EventType.anInstantiationEvent(evt.mask)) {
                            propagator.decArity();
                        }
                        // 2. clear the structure if necessary
                        if (LAZY) {
                            if (timestamps[idx] - AbstractSearchLoop.timeStamp != 0) {
                                deltamon[idx].clear();
                                this.evtmasks[idx] = 0;
                                timestamps[idx] = AbstractSearchLoop.timeStamp;
                            }
                        }
                        // 3. record the event and values removed
                        if ((evt.mask & evtmasks[idx]) == 0) { // if the event has not been recorded yet (through strengthened event also).
                            evtmasks[idx] |= evt.strengthened_mask;
                        }
                        oneoremore = true;
                    }
                }
            }
        }
        if (oneoremore) {
            if (!enqueued) {
                // 4. schedule this
                scheduler.schedule(this);
            } else if (scheduler.needUpdate()) {
                // 5. inform the scheduler of update if necessary
                scheduler.update(this);
            }
        }
    }

    @Override
    public void flush() {
        int first = firstAP.get();
        int last = firstPP.get();
        for (int k = first; k < last; k++) {
            int i = propIdx[k];
            int idx = p2i.get(propagators[i].getId());
            this.evtmasks[idx] = 0;
            this.deltamon[idx].clear();
        }
    }

    void _desactivateP(int i) {
        this.evtmasks[i] = 0;
        this.deltamon[i].clear();
        // inform this that the structure has changed
        // required to handle side effect in this#execute()
        flag_swap_during_execution = true;
    }

    @Override
    public IDeltaMonitor getDeltaMonitor(Propagator propagator, V variable) {
        return deltamon[p2i.get(propagator.getId())];
    }

    @Override
    public void virtuallyExecuted(Propagator propagator) {
        if (LAZY) {
            variables[VINDEX].getDelta().lazyClear();
        }
        int idx = p2i.get(propagator.getId());
        this.evtmasks[idx] = 0;
        this.deltamon[idx].unfreeze();
        this.timestamps[idx] = AbstractSearchLoop.timeStamp;

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
        return "<< {F} " + variables[VINDEX].toString() + "::" + Arrays.toString(propagators) + " >>";
    }
}
