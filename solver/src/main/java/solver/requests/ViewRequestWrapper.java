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
package solver.requests;

import choco.kernel.common.util.procedure.IntProcedure;
import solver.ICause;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.engines.IPropagationEngine;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/09/11
 */
public class ViewRequestWrapper implements IRequestWithVariable<IntVar> {

    final IRequestWithVariable<IntVar> original;
    final Modifier modifier;

    public ViewRequestWrapper(IRequestWithVariable original, Modifier modifier) {
        this.original = original;
        this.modifier = modifier;
    }

    @Override
    public Propagator<IntVar> getPropagator() {
        return original.getPropagator();
    }

    @Override
    public IntVar getVariable() {
        return original.getVariable();
    }

    @Override
    public int getIndex(int dim) {
        return original.getIndex(dim);
    }

    @Override
    public void setIndex(int dim, int idx) {
        original.setIndex(dim, idx);
    }

    @Override
    public int getMask() {
        return original.getMask();
    }

    @Override
    public void filter() throws ContradictionException {
        original.filter();
    }

    @Override
    public void schedule() {
        original.schedule();
    }

    @Override
    public void update(EventType eventType) {
        original.update(modifier.update(original.getVariable(), eventType));
    }

    @Override
    public void beforeUpdate(IntVar var, EventType evt, ICause cause) {
        original.beforeUpdate(var, evt, cause);
    }

    @Override
    public void afterUpdate(IntVar var, EventType evt, ICause cause) {
        original.afterUpdate(var, evt, cause);
    }

    @Override
    public void contradict(IntVar var, EventType evt, ICause cause) {
        original.contradict(var, evt, cause);
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
    public void forEach(IntProcedure proc) throws ContradictionException {
        original.forEach(proc);
    }

    @Override
    public void forEach(IntProcedure proc, int from, int to) throws ContradictionException {
        original.forEach(proc, from, to);
    }

    @Override
    public IPropagationEngine getPropagationEngine() {
        return original.getPropagationEngine();
    }

    @Override
    public void setPropagationEngine(IPropagationEngine engine) {
        original.setPropagationEngine(engine);
    }

    @Override
    public boolean enqueued() {
        return original.enqueued();
    }

    @Override
    public void enqueue() {
        original.enqueue();
    }

    @Override
    public void deque() {
        original.deque();
    }

    public static enum Modifier {
        MINUS {
            @Override
            EventType update(IntVar var, EventType eventType) {
                if (eventType == EventType.INCLOW || eventType == EventType.DECUPP) {
                    return (eventType == EventType.INCLOW ? EventType.DECUPP : EventType.INCLOW);
                }
                return eventType;
            }
        },
        ABS {
            @Override
            EventType update(IntVar var, EventType eventType) {
                if (var.instantiated()) {
                    return EventType.INSTANTIATE;
                } else {
                    if (var.getDomainSize() == 2 && Math.abs(var.getLB()) == var.getUB()) {
                        return EventType.INSTANTIATE;
                    }
                }
                return eventType;
            }
        };

        abstract EventType update(IntVar var, EventType e);
    }
}
