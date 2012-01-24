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

import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.delta.IDeltaMonitor;

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

    protected final V variable; // variable to observe
    protected final Propagator<V> propagator; // propagator to inform
    protected int idxVinP; // index of the variable within the propagator -- immutable
    protected int idxV; // index of this within the variable structure -- mutable

    protected final IDeltaMonitor deltamon; // delta monitoring -- can be NONE
    protected long timestamp = 0; // a timestamp lazy clear the event structures
    protected int evtmask; // reference to events occuring -- inclusive OR over event mask


    public ArcEventRecorder(V variable, Propagator<V> propagator, int idxVinP, Solver solver) {
        super(solver);
        this.variable = variable;
        this.propagator = propagator;
        this.idxVinP = idxVinP;
        this.deltamon = variable.getDelta().getMonitor();
        variable.addMonitor(this);
        propagator.addRecorder(this);
    }

    @Override
    public IDeltaMonitor getDeltaMonitor(V variable) {
        return deltamon;
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
        return new Propagator[]{propagator};
    }

    @Override
    public Variable[] getVariables() {
        return new Variable[]{variable};
    }

    @Override
    public boolean execute() throws ContradictionException {
        if (evtmask > 0) {
//            LoggerFactory.getLogger("solver").info(">> {}", this.toString());
            int evtmask_ = evtmask;
            // for concurrent modification..
            deltamon.freeze();
            this.evtmask = 0; // and clean up mask
            propagator.fineERcalls++;
            assert (propagator.isActive()) : this + " is not active";
            propagator.propagate(this, idxVinP, evtmask_);
            deltamon.unfreeze();
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
		if (cause != propagator) { // due to idempotency of propagator, it should not be schedule itself
			if ((evt.mask & propagator.getPropagationConditions(idxVinP)) != 0) {
//            LoggerFactory.getLogger("solver").info("\t << {}", this.toString());
                // 1. clear the structure if necessary
                if (LAZY) {
                    if (timestamp - AbstractSearchLoop.timeStamp != 0) {
                        this.evtmask = 0;
                        deltamon.clear();
                        timestamp = AbstractSearchLoop.timeStamp;
                    }
                }
                // 2. if instantiation, then decrement arity of the propagator
                if (EventType.anInstantiationEvent(evt.mask)) {
                    propagator.decArity();
                }
                // 3. record the event and values removed
                if ((evt.mask & evtmask) == 0) { // if the event has not been recorded yet (through strengthened event also).
                    evtmask |= evt.strengthened_mask;
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

    public void virtuallyExecuted() {
        this.evtmask = 0;
        if(LAZY){
            variable.getDelta().lazyClear();
            timestamp = AbstractSearchLoop.timeStamp;
        }
        deltamon.unfreeze();
        if (enqueued) {
            scheduler.remove(this);
        }
    }

    @Override
    public void flush() {
        this.evtmask = 0;
        deltamon.clear();
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
    public void activate() {
        variable.activate(this);
    }

    @Override
    public void desactivate() {
        variable.desactivate(this);
        flush();
    }

    @Override
    public String toString() {
        return variable + " -> " + propagator;
    }
}
