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

import solver.constraints.propagators.Propagator;
import solver.propagation.engines.IPropagationEngine;
import solver.variables.Variable;

/**
 * An abstract request, for basic service implementation of a IRequest.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/03/11
 */
public abstract class AbstractRequest<V extends Variable, P extends Propagator<V>> implements IRequest<V> {

    protected final V variable; // Variable of the request
    protected final P propagator; // Propagator of the request
    protected IPropagationEngine engine;

    protected int index = -1; // index of the request in the engine

    protected int gIndex = -1; // index of the group in the engine

    protected final int idxVarInProp; // index of the variable within the propagatro

    protected int idxInVar; // current index of the request within variable request list

    protected boolean enqueued;

    AbstractRequest(P propagator, V variable, int idxInProp) {
        this.propagator = propagator;
        this.variable = variable;
        this.idxVarInProp = idxInProp;
        enqueued = false;
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
    public void setGroup(int gidx) {
        gIndex = gidx;
    }

    @Override
    public void setIndex(int idx) {
        index = idx;
    }

    @Override
    public final int getIndex() {
        return index;
    }

    @Override
    public final int getGroup() {
        return gIndex;
    }

    @Override
    public final int getIdxVarInProp() {
        return idxVarInProp;
    }

    @Override
    public final int getMask() {
        return propagator.getPropagationConditions(idxVarInProp);
    }

    @Override
    public final Propagator<V> getPropagator() {
        return propagator;
    }

    @Override
    public final V getVariable() {
        return variable;
    }

    public final int getIdxInVar() {
        return idxInVar;
    }

    @Override
    public final void setIdxInVar(int idx) {
        idxInVar = idx;
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
}
