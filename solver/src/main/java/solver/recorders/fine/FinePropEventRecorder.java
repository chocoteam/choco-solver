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
import solver.propagation.IPropagationEngine;
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
    // BEWARE a variable can occur more than one time in a propagator !!
    protected int[][] idxVinP; //; // index of each variable within P -- immutable

    public FinePropEventRecorder(V[] variables, Propagator<V> propagator, int[] idxVinPs, Solver solver, IPropagationEngine engine) {
        super(variables, propagator, solver, engine, variables.length);
        int n = variables.length;
        this.idxVinP = new int[n][];
        int k = 0; // count the number of unique variable
        for (int i = 0; i < n; i++) {
            V variable = variables[i];
            int vid = variable.getId();
            int idx = v2i.get(vid);
            if (idx == -1) { // first occurrence of the variable
                this.variables[k] = variable;
                v2i.put(vid, k);
                varIdx[k] = k;
                idxVinP[k] = new int[]{idxVinPs[i]};
                k++;
            } else { // snd or more occurrence of the variable
                int[] tmp = idxVinP[idx];
                idxVinP[idx] = new int[tmp.length + 1];
                System.arraycopy(tmp, 0, idxVinP[idx], 0, tmp.length);
                idxVinP[idx][tmp.length] = idxVinPs[i];
            }
        }
        nbUVar = k;
        if (k < n) {
            this.variables = Arrays.copyOfRange(variables, 0, k);
            this.varIdx = Arrays.copyOfRange(varIdx, 0, k);
            this.idxVinP = Arrays.copyOfRange(idxVinP, 0, k);
        }
        this.evtmasks = new int[k];
    }

    @Override
    public boolean execute() throws ContradictionException {
        if (DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("* {}", this.toString());
        for (int i = 0; i < nbUVar; i++) {
            int evtmask_ = evtmasks[i];
            if (evtmask_ > 0) {
//                LoggerFactory.getLogger("solver").info(">> {}", this.toString());
                evtmasks[i] = 0; // and clean up mask
                assert (propagators[PINDEX].isActive()) : this + " is not active (" + propagators[PINDEX].isStateLess() + " & " + propagators[PINDEX].isPassive() + ")";
                propagators[PINDEX].fineERcalls++;
                for (int j = 0; j < idxVinP[i].length; j++) { // a loop for variable appearing more than once in a propagator
                    propagators[PINDEX].propagate(this, idxVinP[i][j], evtmask_);
                }
            }
        }
        return true;
    }

    @Override
    public void afterUpdate(V var, EventType evt, ICause cause) {
        // Only notify constraints that filter on the specific event received
        assert cause != null : "should be Cause.Null instead";
        if (cause != propagators[PINDEX]) { // due to idempotency of propagator, it should not schedule itself
            if (DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("\t|- {}", this.toString());
            int vid = var.getId();
            int idx = v2i.get(vid);
            for (int j = 0; j < idxVinP[idx].length; j++) { // a loop for variable appearing more than once in a propagator
                if ((evt.mask & propagators[PINDEX].getPropagationConditions(idxVinP[idx][j])) != 0) {
                    // 1. if instantiation, then decrement arity of the propagator
                    if (EventType.anInstantiationEvent(evt.mask)) {
                        propagators[PINDEX].decArity();
                    }
                    // 2. record the event and values removed
                    if ((evt.mask & evtmasks[idx]) == 0) { // if the event has not been recorded yet (through strengthened event also).
                        evtmasks[idx] |= evt.strengthened_mask;
                    }
                    if (!enqueued) {
                        // 3. schedule this
                        scheduler.schedule(this);
                    } else if (scheduler.needUpdate()) {
                        // 4. inform the scheduler of update if necessary
                        scheduler.update(this);
                    }
                }
            }
        }
    }

    @Override
    public void flush() {
        Arrays.fill(this.evtmasks, 0, nbUVar, 0);
    }

    @Override
    public void virtuallyExecuted(Propagator propagator) {
        assert this.propagators[PINDEX] == propagator : "wrong propagator";
        Arrays.fill(evtmasks, 0, nbUVar, 0);
        if (enqueued) {
            scheduler.remove(this);
        }
    }

    @Override
    public void desactivate(Propagator<V> element) {
        super.desactivate(element);
        for (int i = 0; i < nbUVar; i++) {
            this.evtmasks[i] = 0;
        }
    }

    @Override
    public String toString() {
        return "<< {F} " + Arrays.toString(variables) + "::" + propagators[PINDEX].toString() + " >>";
    }
}
