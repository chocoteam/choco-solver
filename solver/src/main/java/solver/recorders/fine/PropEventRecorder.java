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
 * An event recorder associated with one propagator and its variables.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/01/12
 */
public class PropEventRecorder<V extends Variable> extends AbstractFineEventRecorder<V> {

    protected final Propagator<V> propagator; // one propagator
    protected final V[] variables; // its variables
    protected TIntIntHashMap idxVs; // index of this within the variables structure -- mutable

    protected final TIntObjectHashMap<IDeltaMonitor> deltamon; // delta monitoring -- can be NONE
    protected final TIntLongHashMap timestamps; // a timestamp lazy clear the event structures
    protected final TIntIntHashMap evtmasks; // reference to events occuring -- inclusive OR over event mask
    protected TIntIntHashMap idxVinP; //; // index of each variable within P -- immutable

    public PropEventRecorder(V[] variables, Propagator<V> propagator, int[] idxVinPs, Solver solver) {
        super(solver);
        this.propagator = propagator;
        propagator.addRecorder(this);

        this.variables = variables.clone();
        this.deltamon = new TIntObjectHashMap<IDeltaMonitor>(variables.length);
        this.timestamps = new TIntLongHashMap(variables.length, (float) 0.5, -2, -2);
        this.evtmasks = new TIntIntHashMap(variables.length, (float) 0.5, -1, -1);
        this.idxVs = new TIntIntHashMap(variables.length, (float) 0.5, -2, -2);
        this.idxVinP = new TIntIntHashMap(variables.length, (float) 0.5, -1, -1);

        for (int i = 0; i < variables.length; i++) {
            V variable = variables[i];
            variable.addMonitor(this); // BEWARE call setIdxInV(V variable, int idx) !!
            int vid = variable.getId();
            deltamon.put(vid, variable.getDelta().getMonitor());
            timestamps.put(vid, -1);
            evtmasks.put(vid, 0);
            idxVinP.put(vid, idxVinPs[i]);
        }
    }

    @Override
    public Variable[] getVariables() {
        return variables;
    }

    @Override
    public Propagator[] getPropagators() {
        return new Propagator[]{propagator};
    }

    @Override
    public boolean execute() throws ContradictionException {
//        LoggerFactory.getLogger("solver").info("* {}", this.toString());
        boolean oneLeft;//TODO: to remove
        do {
            oneLeft = false;
            for (int i = 0; i < variables.length; i++) {
                Variable variable = variables[i];
                int vid = variable.getId();
                int evtmask_ = evtmasks.get(vid);
                if (evtmask_ > 0) {
                    oneLeft = true;
//            LoggerFactory.getLogger("solver").info(">> {}", this.toString());
                    // for concurrent modification..
                    deltamon.get(vid).freeze();
                    evtmasks.put(vid, 0); // and clean up mask

                    assert (propagator.isActive()) : this + " is not active ("+propagator.isStateLess()+" & "+ propagator.isPassive()+")";
                    propagator.fineERcalls++;
                    propagator.propagate(this, idxVinP.get(vid), evtmask_);
                    deltamon.get(vid).freeze();
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
        if (cause != propagator) { // due to idempotency of propagator, it should not schedule itself
            int vid = var.getId();
            if ((evt.mask & propagator.getPropagationConditions(idxVinP.get(vid))) != 0) {
//                LoggerFactory.getLogger("solver").info("\t|- {} - {}", this.toString(), var);
                // 1. if instantiation, then decrement arity of the propagator
                if (EventType.anInstantiationEvent(evt.mask)) {
                    propagator.decArity();
                }
                // 2. clear the structure if necessary
                if (LAZY) {
                    if (timestamps.get(vid) - AbstractSearchLoop.timeStamp != 0) {
                        deltamon.get(vid).clear();
                        this.evtmasks.put(vid, 0);
                        timestamps.put(vid, AbstractSearchLoop.timeStamp);
                    }
                }
                // 3. record the event and values removed
                int em = evtmasks.get(vid);
                if ((evt.mask & em) == 0) { // if the event has not been recorded yet (through strengthened event also).
                    evtmasks.put(vid, em | evt.strengthened_mask);
                }
                // 4. schedule this
                if (!enqueued()) {
                    scheduler.schedule(this);
                }
            }
        }
    }

    @Override
    public void contradict(V var, EventType evt, ICause cause) {
        // nothing required here
    }

    @Override
    public int getIdxInV(V variable) {
        return idxVs.get(variable.getId());
    }

    @Override
    public void setIdxInV(V variable, int idx) {
        idxVs.put(variable.getId(), idx);
    }

    @Override
    public void flush() {
        for (int i = 0; i < variables.length; i++) {
            int vid = variables[i].getId();
            this.evtmasks.put(vid, 0);
            this.deltamon.get(vid).clear();
        }
    }

    @Override
    public void enqueue() {
        enqueued = true;
        propagator.incNbRecorderEnqued();
    }


    @Override
    public void deque() {
        enqueued = false;
        propagator.decNbRecrodersEnqued();
    }

    @Override
    public void activate(Propagator<V> element) {
        for (int i = 0; i < variables.length; i++) {
            variables[i].activate(this);
        }
    }

    @Override
    public void desactivate(Propagator<V> element) {
        for (int i = 0; i < variables.length; i++) {
            int vid = variables[i].getId();
            variables[i].desactivate(this);
            this.evtmasks.put(vid, 0);
            this.deltamon.get(vid).clear();
        }
    }

    @Override
    public IDeltaMonitor getDeltaMonitor(Propagator propagator, V variable) {
        return deltamon.get(variable.getId());
    }

    @Override
    public void virtuallyExecuted() {
        if (LAZY) {
            for (int i = 0; i < variables.length; i++) {
                variables[i].getDelta().lazyClear();
                int vid = variables[i].getId();
                this.evtmasks.put(vid, 0);
                this.deltamon.get(vid).unfreeze();
                this.timestamps.put(vid, AbstractSearchLoop.timeStamp);
            }
        }
        if (enqueued) {
            scheduler.remove(this);
        }
    }

    @Override
    public String toString() {
        return "<< " + Arrays.toString(variables) + "::" + propagator.toString() + " >>";
    }
}
