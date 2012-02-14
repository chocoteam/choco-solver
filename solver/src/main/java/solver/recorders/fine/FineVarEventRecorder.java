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

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.Variable;
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

    protected TIntIntHashMap idxVinPs; // index of the variable within the propagator -- immutable
    protected final TIntObjectHashMap<IDeltaMonitor> deltamon; // delta monitoring -- can be NONE
    protected final TIntLongHashMap timestamps; // a timestamp lazy clear the event structures
    protected final TIntIntHashMap evtmasks; // reference to events occuring -- inclusive OR over event mask


    public FineVarEventRecorder(V variable, Propagator<V>[] propagators, int[] idxVinP, Solver solver) {
        super(variable, propagators, solver);

        this.deltamon = new TIntObjectHashMap<IDeltaMonitor>(propagators.length);
        this.timestamps = new TIntLongHashMap(propagators.length, (float) 0.5, -2, -2);
        this.evtmasks = new TIntIntHashMap(propagators.length, (float) 0.5, -1, -1);
        this.idxVinPs = new TIntIntHashMap(propagators.length, (float) 0.5, -2, -2);
        for (int i = 0; i < propagators.length; i++) {
            Propagator propagator = propagators[i];
            int pid = propagator.getId();
            idxVinPs.put(pid, idxVinP[i]);
            deltamon.put(pid, variable.getDelta().getMonitor(propagator));
            timestamps.put(pid, -1);
            evtmasks.put(pid, 0);
        }
    }

    @Override
    public boolean execute() throws ContradictionException {
        if (DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("* {}", this.toString());
        for (int i = 0; i < propagators.length; i++) {
            Propagator propagator = propagators[i];
            int pid = propagator.getId();
            int evtmask_ = evtmasks.get(pid);
            if (evtmask_ > 0) {
//            LoggerFactory.getLogger("solver").info(">> {}", this.toString());
                // for concurrent modification..
                deltamon.get(pid).freeze();
                evtmasks.put(pid, 0); // and clean up mask

                assert (propagator.isActive()) : this + " is not active";
                if (propagator.isActive()) {
                    propagator.fineERcalls++;
                    propagator.propagate(this, idxVinPs.get(pid), evtmask_);
                }
                deltamon.get(pid).unfreeze();
            }
        }
        return true;
    }

    @Override
    public void afterUpdate(V var, EventType evt, ICause cause) {
        // Only notify constraints that filter on the specific event received
        assert cause != null : "should be Cause.Null instead";
        boolean oneoremore = false;
        for (int i = 0; i < propagators.length; i++) {
            Propagator propagator = propagators[i];
            if (cause != propagator // due to idempotency of propagator, it should not schedule itself
                    && propagator.isActive()) { // CPRU: could be maintained incrementally
                if (DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("\t|- {} - {}", this.toString(), propagator);
                int pid = propagator.getId();
                if ((evt.mask & propagator.getPropagationConditions(idxVinPs.get(pid))) != 0) {
                    // 1. if instantiation, then decrement arity of the propagator
                    if (EventType.anInstantiationEvent(evt.mask)) {
                        propagator.decArity();
                    }
                    // 2. clear the structure if necessary
                    if (LAZY) {
                        if (timestamps.get(pid) - AbstractSearchLoop.timeStamp != 0) {
                            deltamon.get(pid).clear();
                            this.evtmasks.put(pid, 0);
                            timestamps.put(pid, AbstractSearchLoop.timeStamp);
                        }
                    }
                    // 3. record the event and values removed
                    int em = evtmasks.get(pid);
                    if ((evt.mask & em) == 0) { // if the event has not been recorded yet (through strengthened event also).
                        evtmasks.put(pid, em | evt.strengthened_mask);
                    }
                    oneoremore = true;
                }
            }
        }
        if (oneoremore) {
            // 4. schedule this
            if (!enqueued()) {
                scheduler.schedule(this);
            }
        }

    }

    @Override
    public void flush() {
        for (int i = 0; i < propagators.length; i++) {
            int pid = propagators[i].getId();
            this.evtmasks.put(pid, 0);
            this.deltamon.get(pid).clear();
        }
    }

    void _desactivateP(int i) {
        int pid = propagators[i].getId();
        this.evtmasks.put(pid, 0);
        this.deltamon.get(pid).clear();
    }

    @Override
    public IDeltaMonitor getDeltaMonitor(Propagator propagator, V variable) {
        return deltamon.get(propagator.getId());
    }

    @Override
    public void virtuallyExecuted(Propagator propagator) {
        if (LAZY) {
            variable.getDelta().lazyClear();
        }
        int pid = propagator.getId();
        this.evtmasks.put(pid, 0);
        this.deltamon.get(pid).unfreeze();
        this.timestamps.put(pid, AbstractSearchLoop.timeStamp);

        //TODO: to remove when active propagators will be managed smartly
        boolean canDeque = true;
        for (int i = 0; i < propagators.length && canDeque; i++) {
            if (this.evtmasks.get(propagators[i].getId()) != 0) {
                canDeque = false;
            }
        }
        if (enqueued && canDeque) {
            scheduler.remove(this);
        }
    }

    @Override
    public String toString() {
        return "<< {F} " + variable.toString() + "::" + Arrays.toString(propagators) + " >>";
    }
}
