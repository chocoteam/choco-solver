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
package solver.recorders.coarse;

import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/11
 */
public class CoarseEventRecorder extends AbstractCoarseEventRecorder {

	int timestamp; // timestamp of the last clear call -- for lazy clear

	protected final Propagator propagator;

	int evtmask; // reference to events occuring -- inclusive OR over event mask

	public CoarseEventRecorder(Propagator propagator, Solver solver) {
		super();
		this.propagator = propagator;
		this.evtmask = EventType.FULL_PROPAGATION.mask; // initialize with full propagation event
		propagator.addRecorder(this);
	}

	@Override
	public Propagator[] getPropagators() {
		return new Propagator[]{propagator};
	}

	public void update(EventType e) {
		if ((e.mask & propagator.getPropagationConditions()) != 0) {
//            LoggerFactory.getLogger("solver").info("\t << {}", this.toString());
			// 1. clear the structure if necessar
			if (LAZY) {
				if (timestamp - AbstractSearchLoop.timeStamp != 0) {
					this.evtmask = 0;
					timestamp = AbstractSearchLoop.timeStamp;
				}
			}
			// 2. store information concerning event
			if ((e.mask & evtmask) == 0) { // if the event has not been recorded yet (through strengthened event also).
				evtmask |= e.strengthened_mask;
			}
			// 3. schedule this
			if (!enqueued) {
				scheduler.schedule(this);
			}
		}
	}

	@Override
	public boolean execute() throws ContradictionException {
		if (!propagator.isStateLess()) {
			//promote event to top level event FULL_PROPAGATION
			evtmask |= EventType.FULL_PROPAGATION.strengthened_mask;
			propagator.setActive();
		}
		if(propagator.getNbPendingER() > 0) {
			evtmask |= EventType.FULL_PROPAGATION.strengthened_mask;
		}
		if (evtmask > 0) {
//            LoggerFactory.getLogger("solver").info(">> {}", this.toString());
			propagator.coarseERcalls++;
			int _evt = evtmask;
			evtmask = 0;
			propagator.propagate(_evt);
		}
		// unfreeze (and eventually unschedule) every fine event attached to this propagator
		propagator.forEachFineEvent(virtExec);
		return true;
	}

	@Override
	public void flush() {
		this.evtmask = 0;
	}


	@Override
	public String toString() {
		return propagator.toString();
	}
}
