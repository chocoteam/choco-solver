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
import solver.propagation.IScheduler;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.delta.IDeltaMonitor;

/**
 * A event recorder wrapper for views.
 * They delegate every operations to the wrapped event recorder, based on the original variable,
 * but an event modifier can work to transform the original event occurring on the original variable,
 * to adapt it to the view.
 * For instance, MINUS view requires to transpose bound events.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/12/11
 */
public class ViewEventRecorderWrapper<V extends Variable> extends AbstractFineEventRecorder<V> {

    AbstractFineEventRecorder<V> original;
    IModifier<V> modifier;

    public ViewEventRecorderWrapper(AbstractFineEventRecorder<V> original, IModifier<V> modifier, Solver solver) {
        super(solver);
        this.original = original;
        this.modifier = modifier;
    }

    @Override
    public IDeltaMonitor getDeltaMonitor(V variable) {
        return original.getDeltaMonitor(variable);
    }

    @Override
    public void setScheduler(IScheduler scheduler, int idxInS) {
        original.setScheduler(scheduler, 0);
    }

    @Override
    public IScheduler getScheduler() {
        return original.getScheduler();
    }

    @Override
    public void activate() {
        original.activate();
    }

    @Override
    public void desactivate() {
        original.desactivate();
    }

    @Override
    public Variable[] getVariables() {
        return original.getVariables();
    }

    @Override
    public Propagator[] getPropagators() {
        return original.getPropagators();
    }

    @Override
    public void flush() {
        original.flush();
    }

    @Override
    public boolean execute() throws ContradictionException {
        return original.execute();
    }

    @Override
    public void enqueue() {
        original.enqueue();
    }

    @Override
    public void deque() {
        original.deque();
    }

    @Override
    public void beforeUpdate(V var, EventType evt, ICause cause) {
        original.beforeUpdate(var, evt, cause);
    }

    /**
     * This is the main reason we create this class
     *
     * @param var   variable concerned
     * @param evt   modification event
     * @param cause origin of the modification
     */
    @Override
    public void afterUpdate(V var, EventType evt, ICause cause) {
        original.afterUpdate(var, modifier.update(var, evt), cause);
    }

    @Override
    public void contradict(V var, EventType evt, ICause cause) {
        original.contradict(var, evt, cause);
    }

    @Override
    public int getIdxInV(V variable) {
        return original.getIdxInV(variable);
    }

    @Override
    public void setIdxInV(V variable, int idx) {
        original.setIdxInV(variable, idx);
    }

}
