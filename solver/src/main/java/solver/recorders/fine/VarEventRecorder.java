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
public class VarEventRecorder<V extends Variable> extends AbstractFineEventRecorder<V> {

    protected final V variable; // one variable
    protected Propagator<V>[] propagators; // its propagators
    protected int idxV; // index of this within the variable structure -- mutable

    protected TIntIntHashMap idxVinPs; // index of the variable within the propagator -- immutable
    protected final TIntObjectHashMap<IDeltaMonitor> deltamon; // delta monitoring -- can be NONE
    protected final TIntLongHashMap timestamps; // a timestamp lazy clear the event structures
    protected final TIntIntHashMap evtmasks; // reference to events occuring -- inclusive OR over event mask


    public VarEventRecorder(V variable, Propagator<V>[] propagators, int[] idxVinP, Solver solver) {
        super(solver);
        this.variable = variable;
        variable.addMonitor(this);

        this.propagators = propagators.clone();
        this.deltamon = new TIntObjectHashMap<IDeltaMonitor>(propagators.length);
        this.timestamps = new TIntLongHashMap(propagators.length, (float) 0.5, -2, -2);
        this.evtmasks = new TIntIntHashMap(propagators.length, (float) 0.5, -1, -1);
        this.idxVinPs = new TIntIntHashMap(propagators.length, (float) 0.5, -2, -2);
        for (int i = 0; i < propagators.length; i++) {
            Propagator propagator = propagators[i];
            propagator.addRecorder(this);

            int pid = propagator.getId();
            idxVinPs.put(pid, idxVinP[i]);
            deltamon.put(pid, variable.getDelta().getMonitor());
            timestamps.put(pid, -1);
            evtmasks.put(pid, 0);
        }
    }

    @Override
    public int getIdxInV(V variable) {
        return idxV;
    }

    @Override
    public void setIdxInV(V variable, int idx) {
        this.idxV = idx;
    }

    @Override
    public Propagator[] getPropagators() {
        return propagators;
    }

    @Override
    public Variable[] getVariables() {
        return new Variable[]{variable};
    }

    @Override
    public boolean execute() throws ContradictionException {
//        LoggerFactory.getLogger("solver").info("* {}", this.toString());
        boolean oneLeft;//TODO: to remove
        do {
            oneLeft = false;
            for (int i = 0; i < propagators.length; i++) {
                Propagator propagator = propagators[i];
                int pid = propagator.getId();
                int evtmask_ = evtmasks.get(pid);
                if (evtmask_ > 0) {
                    oneLeft = true;
//            LoggerFactory.getLogger("solver").info(">> {}", this.toString());
                    // for concurrent modification..
                    deltamon.get(pid).freeze();
                    evtmasks.put(pid, 0); // and clean up mask

                    assert (propagator.isActive()) : this + " is not active";
                    if (propagator.isActive()) {
                        propagator.fineERcalls++;
                        propagator.propagate(this, idxVinPs.get(pid), evtmask_);
                    }
                    deltamon.get(pid).freeze();
                }
            }
        } while (oneLeft);
        return true;
    }

    @Override
    public void beforeUpdate(V var, EventType evt, ICause cause) {
        // nothing required here
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
//                LoggerFactory.getLogger("solver").info("\t|- {} - {}", this.toString(), propagator);
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
    public void contradict(V var, EventType evt, ICause cause) {
        // nothing required here
    }

    @Override
    public void flush() {
        for (int i = 0; i < propagators.length; i++) {
            int pid = propagators[i].getId();
            this.evtmasks.put(pid, 0);
            this.deltamon.get(pid).clear();
        }
    }

    @Override
    public void enqueue() {
        enqueued = true;
        for (int i = 0; i < propagators.length; i++) {
            propagators[i].incNbRecorderEnqued();
        }
    }


    @Override
    public void deque() {
        enqueued = false;
        for (int i = 0; i < propagators.length; i++) {
            propagators[i].decNbRecrodersEnqued();
        }
    }

    @Override
    public void activate(Propagator<V> element) {
        // if already activated, .activate has no side effect
        variable.activate(this);
    }

    @Override
    public void desactivate(Propagator<V> element) {
        // must be desactivate when no propagator are active
        int count = propagators.length;
        for (int i = 0; i < propagators.length; i++) {
            if (propagators[i].isPassive()) {
                count--;
                int pid = propagators[i].getId();
                this.evtmasks.put(pid, 0);
                this.deltamon.get(pid).clear();
            }
        }
        if (count == 0) {
            variable.desactivate(this);
            flush();
        }
    }

    @Override
    public IDeltaMonitor getDeltaMonitor(Propagator propagator, V variable) {
        return deltamon.get(propagator.getId());
    }

    @Override
    public void virtuallyExecuted() {
        if (LAZY) {
            variable.getDelta().lazyClear();
        }
        for (int i = 0; i < propagators.length; i++) {
            int pid = propagators[i].getId();
            this.evtmasks.put(pid, 0);
            this.deltamon.get(pid).unfreeze();
            this.timestamps.put(pid, AbstractSearchLoop.timeStamp);
        }
        if (enqueued) {
            scheduler.remove(this);
        }
    }

    @Override
    public String toString() {
        return "<< " + variable.toString() + "::" + Arrays.toString(propagators) + " >>";
    }
}
