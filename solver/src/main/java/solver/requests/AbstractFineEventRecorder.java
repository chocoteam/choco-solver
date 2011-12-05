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

import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.propagation.engines.IPropagationEngine;
import solver.search.measure.IMeasures;
import solver.variables.IVariableMonitor;
import solver.variables.Variable;

/**
 * An abstract class for fine event recorder.
 * A fine event is categorized by one or more event occurring on one or more variables.
 * It includes at least one variable and one propagator (in that very case, it is a ArcEventRecorder).
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/12/11
 */
public abstract class AbstractFineEventRecorder<V extends Variable> implements IEventRecorder, IVariableMonitor<V> {

    protected final V[] variables; // Variables of the recorder
    protected final Propagator<V>[] propagators; // Propagators of the recorder
    protected IPropagationEngine engine;
    protected int groupIdx = -1;
    protected final IMeasures measures; // for timestamp

    protected boolean enqueued; // to check wether this is enqueud or not.

    protected AbstractFineEventRecorder(V[] variables, Propagator<V>[] propagators, Solver solver) {
        this.variables = variables;
        this.propagators = propagators;
        this.engine = solver.getEngine();
        measures = solver.getMeasures();
        enqueued = false;
    }

    @Override
    public boolean enqueued() {
        return enqueued;
    }

    public void setPropagationEngine(IPropagationEngine engine) {
        this.engine = engine;
    }

    public final IPropagationEngine getPropagationEngine() {
        return engine;
    }

    @Override
    public void groupIdx(int idx) {
        this.groupIdx = idx;
    }

    @Override
    public int groupIdx() {
        return groupIdx;
    }
}
