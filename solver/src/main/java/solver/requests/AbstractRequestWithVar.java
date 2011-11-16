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

import solver.ICause;
import solver.constraints.propagators.Propagator;
import solver.propagation.engines.IPropagationEngine;
import solver.variables.EventType;
import solver.variables.Variable;

/**
 * An abstract request, for basic service implementation of a IRequest.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/03/11
 */
public abstract class AbstractRequestWithVar<V extends Variable> implements IRequestWithVariable<V> {

    protected final V variable; // Variable of the request
    protected final Propagator<V> propagator; // Propagator of the request
    protected IPropagationEngine engine;

    protected final int[] indices;

    protected boolean enqueued;

    AbstractRequestWithVar(Propagator<V> propagator, V variable, int idxInProp) {
        this.propagator = propagator;
        this.variable = variable;

        this.indices = new int[]{-1, -1, -1, -1};
        this.indices[VAR_IN_PROP] = idxInProp;
        enqueued = false;
    }

    @Override
    public void setIndex(int dim, int idx) {
        indices[dim] = idx;
    }

    @Override
    public int getIndex(int dim) {
        return indices[dim];
    }

    @Override
    public void setPropagationEngine(IPropagationEngine engine) {
        this.engine = engine;
    }

    @Override
    public final IPropagationEngine getPropagationEngine() {
        return engine;
    }

    @Override
    public void schedule() {
        engine.schedule(this);
    }

    @Override
    public final int getMask() {
        return propagator.getPropagationConditions(indices[VAR_IN_PROP]);
    }

    @Override
    public final Propagator<V> getPropagator() {
        return propagator;
    }

    @Override
    public final V getVariable() {
        return variable;
    }

    @Override
    public boolean enqueued() {
        return enqueued;
    }

    @Override
    public void enqueue() {
        enqueued = true;
        propagator.incNbRequestEnqued();
    }

    @Override
    public void deque() {
        enqueued = false;
        propagator.decNbRequestEnqued();
    }

    @Override
    public void activate() {
        variable.activate(this);
    }

    @Override
    public void desactivate() {
        variable.desactivate(this);
    }

    @Override
    public void beforeUpdate(V var, EventType evt, ICause cause) {
    }

    @Override
    public void afterUpdate(V var, EventType evt, ICause cause) {
        if (this.propagator != cause) {
            update(evt);
        }
    }

    @Override
    public void contradict(V var, EventType evt, ICause cause) {
    }
}
