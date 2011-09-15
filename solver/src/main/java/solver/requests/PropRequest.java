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
import solver.exception.ContradictionException;
import solver.propagation.engines.IPropagationEngine;
import solver.variables.EventType;
import solver.variables.Variable;

/**
 * A propagation request storing events occuring on a variable to inform a propagator.
 * It is related to:<br/>
 * - initialisation of the propagator,<br/>
 * - and the main filtering algorithm of the propagator<br/>
 * <br/>
 * When propagator required a call to the main filtering algorithm (for example, in PropAllDiffBC),
 * this request must be explicitly added to the list of request of each variables. It becomes a shared request,
 * not linked to a particular variable.
 * <br/>
 * The intialisation and the propagation must be distinguished.
 * <p/>
 * <p/>
 * <br/>
 * These paramaters are lazy cleared when necessary: usually before updating the request and before treating events.
 * </br>
 *
 * @author Charles Prud'homme
 * @since 23 sept. 2010
 */
public final class PropRequest<V extends Variable, P extends Propagator<V>> implements IRequest<V> {

    protected final P propagator; // Propagator of the request

    protected IPropagationEngine engine;

    protected int index = -1; // index of the request in the engine

    protected int gIndex = -1; // index of the group in the engine

    protected boolean enqueued;


    public PropRequest(P propagator) {
        this.propagator = propagator;
        enqueued = false;
    }

    @Override
    public int fromDelta() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int toDelta() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IPropagationEngine getPropagationEngine() {
        return engine;
    }

    @Override
    public void setPropagationEngine(IPropagationEngine engine) {
        this.engine = engine;
    }

    @Override
    public Propagator<V> getPropagator() {
        return propagator;
    }

    @Override
    public V getVariable() {
        return null;
    }

    @Override
    public void setIndex(int idx) {
        index = idx;
    }

    @Override
    public void setGroup(int gidx) {
        gIndex = gidx;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getGroup() {
        return gIndex;
    }

    @Override
    public int getIdxInVar() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIdxInVar(int idx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIdxVarInProp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMask() {
        return EventType.PROPAGATE.mask;
    }

    @Override
    public void filter() throws ContradictionException {
        propagator.propCalls++;
//        LoggerFactory.getLogger("solver").info("PROP: {}", this.toString());
        if (!propagator.isActive()) {
            propagator.initialize();
            propagator.setActive();
        }
        propagator.propagate();
    }

    @Override
    public void update(EventType e) {
        if (EventType.PROPAGATE == e) {
            engine.update(this);
        }
    }

    @Override
    public void activate() {
        // Can not be activated: this is active from its creation
    }

    @Override
    public void desactivate() {
        // Can not be desactivated: it depends on the event requests of the propagator
        // when they are all entailed, this can not be call anymore.
    }

    @Override
    public String toString() {
        return "(" + propagator.getConstraint().toString() + ")";
    }

    @Override
    public boolean enqueued() {
        return enqueued;
    }

    @Override
    public void enqueue() {
        enqueued = true;
    }

    @Override
    public void deque() {
        enqueued = false;
    }
}
