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
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.delta.IDeltaMonitor;

import java.util.Arrays;

/**
 * An event recorder associated with one propagator and its variables.
 * On a variable modification, the propagators is scheduled for FULL_PROPAGATION
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/01/12
 */
public class PropEventRecorder<V extends Variable> extends AbstractFineEventRecorder<V> {

    protected final Propagator<V> propagator; // one propagator
    protected final V[] variables; // its variables
    protected TIntIntHashMap idxVs; // index of this within the variables structure -- mutable

    public PropEventRecorder(V[] variables, Propagator<V> propagator, Solver solver) {
        super(solver);
        this.propagator = propagator;
        propagator.addRecorder(this);
        this.variables = variables.clone();
        this.idxVs = new TIntIntHashMap(variables.length, (float) 0.5, -2, -2);
        for (int i = 0; i < variables.length; i++) {
            variables[i].addMonitor(this); // BEWARE call setIdxInV(V variable, int idx) !!
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
        throw new SolverException("PropEventRecorder#execute() is empty and should not be called (nor scheduled)!");
    }

    @Override
    public void beforeUpdate(V var, EventType evt, ICause cause) {
    }

    @Override
    public void afterUpdate(V var, EventType evt, ICause cause) {
        // Only notify constraints that filter on the specific event received
        assert cause != null : "should be Cause.Null instead";
        if (cause != propagator) { // due to idempotency of propagator, it should not schedule itself
            // 1. if instantiation, then decrement arity of the propagator
            if (EventType.anInstantiationEvent(evt.mask)) {
                propagator.decArity();
            }
            // 2. schedule the coarse event recorder associated to thos
            propagator.forcePropagate(EventType.FULL_PROPAGATION);
        }
    }

    @Override
    public void contradict(V var, EventType evt, ICause cause) {
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
        // can be void
    }

    @Override
    public IDeltaMonitor getDeltaMonitor(Propagator propagator, V variable) {
        return IDeltaMonitor.Default.NONE;
    }

    @Override
    public void virtuallyExecuted() {
    }

    @Override
    public String toString() {
        return "<< " + Arrays.toString(variables) + "::" + propagator.toString() + " >>";
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
            variables[i].desactivate(this);
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
}
