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
import solver.exception.SolverException;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.delta.IDeltaMonitor;

import java.util.Arrays;

/**
 * An event recorder associated with one variable and its propagator.
 * On a variable modification, its propagators are scheduled for FULL_PROPAGATION
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/01/12
 */
public class VarEventRecorder<V extends Variable> extends AbstractFineEventRecorder<V> {

    protected final V variable; // one variable
    protected final Propagator<V>[] propagators; // its propagators
    protected int idxV; // index of this within the variable structure -- mutable

    public VarEventRecorder(V variable, Propagator<V>[] propagators, Solver solver) {
        super(solver);
        this.variable = variable;
        variable.addMonitor(this);
        this.propagators = propagators.clone();

        for (int i = 0; i < propagators.length; i++) {
            propagators[i].addRecorder(this);
        }
    }

    @Override
    public Variable[] getVariables() {
        return new Variable[]{variable};
    }

    @Override
    public Propagator[] getPropagators() {
        return propagators;
    }

    @Override
    public boolean execute() throws ContradictionException {
        throw new SolverException("VarEventRecorder#execute() is empty and should not be called (nor scheduled)!");
    }

    @Override
    public void beforeUpdate(V var, EventType evt, ICause cause) {
        // nothing required here
    }

    @Override
    public void afterUpdate(V var, EventType evt, ICause cause) {
        // Only notify constraints that filter on the specific event received
        assert cause != null : "should be Cause.Null instead";
        for (int i = 0; i < propagators.length; i++) {
            Propagator propagator = propagators[i];
            if (cause != propagator // due to idempotency of propagator, it should not schedule itself
                    && propagator.isActive()) { // CPRU: could be maintained incrementally
                // 1. if instantiation, then decrement arity of the propagator
                if (EventType.anInstantiationEvent(evt.mask)) {
                    propagator.decArity();
                }
                // 2. schedule the coarse event recorder associated to thos
                propagator.forcePropagate(EventType.FULL_PROPAGATION);
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
        // can be void
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
        variable.activate(this);
    }

    @Override
    public void desactivate(Propagator<V> element) {
        // must be desactivate when no propagator are active
        int count = propagators.length;
        for (int i = 0; i < propagators.length; i++) {
            if (propagators[i].isPassive()) {
                count--;
                _desactivateP(i);
            }
        }
        if (count == 0) {
            variable.desactivate(this);
            flush();
        }
    }

    void _desactivateP(int i) {
        // void
    }

    @Override
    public IDeltaMonitor getDeltaMonitor(Propagator propagator, V variable) {
        return IDeltaMonitor.Default.NONE;
    }

    @Override
    public void virtuallyExecuted(Propagator propagator) {
        // void
    }

    @Override
    public String toString() {
        return "<< " + variable + "::" + Arrays.toString(propagators) + ">>";
    }
}
