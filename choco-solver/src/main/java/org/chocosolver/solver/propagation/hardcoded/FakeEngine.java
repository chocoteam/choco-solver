/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.propagation.hardcoded;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.slf4j.LoggerFactory;

/**
 * An fake propagation engine for debugging uses only.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 21/02/13
 */
class FakeEngine implements IPropagationEngine {

    private static FakeEngine singleton = new FakeEngine();

    @Override
    public void initialize() {

    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void propagate() throws ContradictionException {

    }

    @Override
    public void flush() {

    }

    @Override
    public void fails(ICause cause, Variable variable, String message) throws ContradictionException {
        throw new SolverException("The Propagator " + cause + " is not idempotent!\n" +
                "See stack trace for more details -- it can be due to a view!");
    }

    @Override
    public ContradictionException getContradictionException() {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public void onVariableUpdate(Variable variable, IEventType type, ICause cause) throws ContradictionException {
        throw new SolverException("The Propagator " + cause + " is not idempotent!");
    }

    @Override
    public void delayedPropagation(Propagator propagator, PropagatorEventType type) throws ContradictionException {

    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {

    }

    @Override
    public void desactivatePropagator(Propagator propagator) {

    }

    @Override
    public void dynamicAddition(boolean permanent, Propagator... ps) {

    }

    @Override
    public void updateInvolvedVariables(Propagator p) {

    }

    @Override
    public void propagateOnBacktrack(Propagator p) {

    }

    @Override
    public void dynamicDeletion(Propagator... ps) {

    }

    public static void checkIdempotency(Propagator lastProp) throws ContradictionException {
        if (LoggerFactory.getLogger(IPropagationEngine.class).isDebugEnabled()) {
            IPropagationEngine.Trace.printPropagation(null, lastProp);
        }
        switch (lastProp.getSolver().getSettings().getIdempotencyStrategy()) {
            case force:
                if (lastProp.isActive()) {
                    lastProp.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
                }
                break;
            case error:
                IPropagationEngine engine = lastProp.getSolver().getEngine();
                lastProp.getSolver().set(singleton);
                if (lastProp.isActive()) {
                    lastProp.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
                }
                lastProp.getSolver().set(engine);
                break;
        }
    }
}
