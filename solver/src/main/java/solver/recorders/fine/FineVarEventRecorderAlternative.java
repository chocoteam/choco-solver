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

import choco.kernel.memory.IStateBitSet;
import gnu.trove.map.hash.TIntIntHashMap;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.delta.IDelta;
import solver.variables.delta.IDeltaMonitor;

import java.util.Arrays;

/**
 * Another version of a variable event recorder for fine event.
 * Based on bitset to maintain active elements, it ensures propagators are alwarys iterated in the same way.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/02/12
 */
public class FineVarEventRecorderAlternative<V extends Variable> extends AbstractFineEventRecorder<V> {

    protected final V variable; // one variable
    protected final Propagator<V>[] propagators; // its propagators
    protected int idxV; // index of this within the variable structure -- mutable

    protected final TIntIntHashMap p2i; // hashmap to retrieve the position of a propagator in propagators thanks to its pid
    protected final IStateBitSet pactive; // active propagators
    protected final int[] idxVinPs; // index of the variable within the propagator -- immutable
    protected final IDeltaMonitor[] deltamon; // delta monitoring -- can be NONE
    protected final long[] timestamps; // a timestamp lazy clear the event structures
    protected final int evtmasks[]; // reference to events occuring -- inclusive OR over event mask


    public FineVarEventRecorderAlternative(V variable, Propagator<V>[] propagators, int[] idxVinP, Solver solver) {
        super(solver);
        this.variable = variable;
        variable.addMonitor(this);
        this.propagators = propagators.clone();

        int n = propagators.length;
        p2i = new TIntIntHashMap(n);

        this.deltamon = new IDeltaMonitor[n];
        this.timestamps = new long[n];
        Arrays.fill(timestamps, -1);
        this.evtmasks = new int[n];
        this.idxVinPs = idxVinP.clone();
        pactive = solver.getEnvironment().makeBitSet(n);

        IDelta delta = variable.getDelta();
        for (int i = 0; i < n; i++) {
            Propagator propagator = propagators[i];
            propagator.addRecorder(this);
            p2i.put(propagator.getId(), i);
            deltamon[i] = delta.createDeltaMonitor(propagator);
        }
        throw new SolverException("Do not handle indices correctly!");

    }

    @Override
    public boolean execute() throws ContradictionException {
        if (DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("* {}", this.toString());
        for (int i = pactive.nextSetBit(0); i > -1; i = pactive.nextSetBit(i + 1)) {
            Propagator propagator = propagators[i];
            int idx = p2i.get(propagator.getId());
            int evtmask_ = evtmasks[idx];
            if (evtmask_ > 0) {
//                  LoggerFactory.getLogger("solver").info(">> {}", this.toString());
                // for concurrent modification..
                deltamon[idx].freeze();
                evtmasks[idx] = 0; // and clean up mask
                assert (propagator.isActive()) : this + " is not active";
                propagator.fineERcalls++;
                propagator.propagate(this, idxVinPs[idx], evtmask_);
                deltamon[idx].unfreeze();
            }
        }
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
        if (DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("\t|- {}", this.toString());
        boolean oneoremore = false;
        for (int i = pactive.nextSetBit(0); i > -1; i = pactive.nextSetBit(i + 1)) {
            Propagator propagator = propagators[i];
            if (cause != propagator) { // due to idempotency of propagator, it should not schedule itself
                int idx = p2i.get(propagator.getId());
                if ((evt.mask & propagator.getPropagationConditions(idxVinPs[idx])) != 0) {
                    // 1. if instantiation, then decrement arity of the propagator
                    if (EventType.anInstantiationEvent(evt.mask)) {
                        propagator.decArity();
                    }
                    // 2. clear the structure if necessary
                    if (LAZY) {
                        if (timestamps[idx] - loop.timeStamp != 0) {
                            deltamon[idx].clear();
                            this.evtmasks[idx] = 0;
                            timestamps[idx] = loop.timeStamp;
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
    public void contradict(V var, EventType evt, ICause cause) {
        // nothing required here
    }

    @Override
    public int getIdxInV(V variable) {
        return idxV;
    }

    @Override
    public void setIdxInV(V variable, int idx) {
        idxV = idx;
    }

    @Override
    public void flush() {
        for (int i = pactive.nextSetBit(0); i > -1; i = pactive.nextSetBit(i + 1)) {
            int idx = p2i.get(propagators[i].getId());
            this.evtmasks[idx] = 0;
            this.deltamon[idx].clear();
        }
    }

    @Override
    public void enqueue() {
        enqueued = true;
        for (int i = pactive.nextSetBit(0); i > -1; i = pactive.nextSetBit(i + 1)) {
            propagators[i].incNbRecorderEnqued();
        }
    }


    @Override
    public void deque() {
        enqueued = false;
        for (int i = pactive.nextSetBit(0); i > -1; i = pactive.nextSetBit(i + 1)) {
            propagators[i].decNbRecrodersEnqued();
        }
    }

    @Override
    public void activate(Propagator<V> element) {
        if (pactive.cardinality() == 0) { // if this is the first propagator activated
            variable.activate(this); // activate this
        }
        int idx = p2i.get(element.getId());
        pactive.set(idx); // set the element as active
    }

    @Override
    public void desactivate(Propagator<V> element) {
        int idx = p2i.get(element.getId());
        this.evtmasks[idx] = 0;
        this.deltamon[idx].clear();
        pactive.clear(idx);

        int i = 0;
        for (; i < propagators.length && propagators[i].isPassive(); ) {
            i++;
        }
        if (i == propagators.length) {
            variable.desactivate(this);
            flush();
        }
    }

    @Override
    public IDeltaMonitor getDeltaMonitor(Propagator propagator, V variable) {
        return deltamon[p2i.get(propagator.getId())];
    }

    @Override
    public void virtuallyExecuted(Propagator propagator) {
        if (LAZY) {
            variable.getDelta().lazyClear();
        }
        int idx = p2i.get(propagator.getId());
        this.evtmasks[idx] = 0;
        this.deltamon[idx].unfreeze();
        this.timestamps[idx] = loop.timeStamp;

        //TODO: to remove when active propagators will be managed smartly
        boolean canDeque = true;
        for (int i = pactive.nextSetBit(0); i > -1 && canDeque; i = pactive.nextSetBit(i + 1)) {
            if (this.evtmasks[i] != 0) {
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
