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

import gnu.trove.list.array.TIntArrayList;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * A specialized fine event recorder associated with one variable and two or more propagators.
 * It observes a variable, records events occurring on the variable,
 * schedules it self when calling the filtering algortithm of the propagators
 * is required.
 * It also stores, if required, pointers to value removals.
 * <br/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/12/11
 */
public abstract class VarEventRecorder<V extends Variable> extends AbstractFineEventRecorder<V> {

    protected final V variable; // variable to observe
    protected List<Propagator<V>> propagators; // propagators to inform
    protected TIntArrayList idxVinPs; // index of the variable within the propagator -- immutable
    protected int idxV; // index of this within the variable structure -- mutable

    protected long timestamp = 0; // a timestamp lazy clear the event structures
    protected int evtmask; // reference to events occuring -- inclusive OR over event mask


    public VarEventRecorder(V variable, Solver solver) {
        super(solver);
        this.variable = variable;
        this.propagators = new ArrayList<Propagator<V>>();
        this.idxVinPs = new TIntArrayList();
    }

    public void addPropagator(Propagator<V> propagator, int idxVinP) {
        propagators.add(propagator);
        idxVinPs.add(idxVinP);
    }


    @Override
    public int getIdxInV(V variable) {
        return idxV;
    }

    @Override
    public void setIdxInV(V variable, int idx) {
        this.idxV = idx;
    }

    @Override
    public Propagator[] getPropagators() {
        return propagators.toArray(new Propagator[propagators.size()]);
    }

    @Override
    public Variable[] getVariables() {
        return new Variable[]{variable};
    }

    @Override
    public void beforeUpdate(V var, EventType evt, ICause cause) {
        // nothing required here
    }

    @Override
    public void contradict(V var, EventType evt, ICause cause) {
        // nothing required here
    }

    @Override
    public void flush() {
        this.evtmask = 0;
    }

    @Override
    public void enqueue() {
        enqueued = true;
        for (int i = 0; i < propagators.size(); i++) {
            propagators.get(i).incNbRecorderEnqued();
        }
    }

    @Override
    public void deque() {
        enqueued = false;
        for (int i = 0; i < propagators.size(); i++) {
            propagators.get(i).decNbRecrodersEnqued();
        }
    }

    @Override
    public void activate() {
        variable.activate(this);
    }

    @Override
    public void desactivate() {
        variable.desactivate(this);
        flush();
    }

    @Override
    public String toString() {
        return variable + " -> <" + propagators + ">";
    }
}
