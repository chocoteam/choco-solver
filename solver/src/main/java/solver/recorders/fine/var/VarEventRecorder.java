/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.recorders.fine.var;

import choco.kernel.memory.IStateInt;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.propagation.PropagationEngine;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.Arrays;

/**
 * An event recorder associated with one variable and its propagator.
 * On a variable modification, its propagators are scheduled for FULL_PROPAGATION
 * <br/>
 *
 * @author Charles Prud'homme
 * @revision 05/24/12 remove timestamp and deltamonitoring
 * @since 24/01/12
 */
public class VarEventRecorder<V extends Variable> extends AbstractFineEventRecorder<V> {

    protected int idxV; // index of this within the variable structure -- mutable
    protected final int[] p2i; // a kind of hashmap to retrieve the position of a propagator in variable thanks to its pid
    protected final int[] propIdx; // an array of indices helping to get active propagators
    protected final IStateInt firstAP; // index of the first active propagator in propIdx
    protected final IStateInt firstPP; // index of the first passive propagator in propIdx
    protected final int offset;


    public VarEventRecorder(V variable, Propagator<V>[] props, Solver solver, PropagationEngine engine) {
        super((V[]) new Variable[]{variable}, props.clone(), solver, engine);
        int n = props.length;
        // first estimate amplitude of IDs
        int id = propagators[0].getId();
        int idM = id, idm = id;
        for (int i = 1; i < n; i++) {
            id = propagators[i].getId();
            if (id > idM) idM = id;
            if (id < idm) idm = id;
        }
        // then create max size arrays
        p2i = new int[idM - idm + 1];
        Arrays.fill(p2i, -1);
        offset = idm;
        propIdx = new int[n];
        for (int i = 0; i < n; i++) {
            Propagator propagator = props[i];
            int pid = propagator.getId();
            p2i[pid - offset] = i;
            propIdx[i] = i;
        }
        firstAP = solver.getEnvironment().makeInt(engine.forceActivation() ? 0 : n);
        firstPP = solver.getEnvironment().makeInt(n);
    }

    @Override
    public boolean execute() throws ContradictionException {
        throw new SolverException("VarEventRecorder#execute() is empty and should not be called (nor scheduled)!");
    }

    @Override
    public void afterUpdate(int vIdx, EventType evt, ICause cause) {
        // Only notify constraints that filter on the specific event received
        assert cause != null : "should be Cause.Null instead";
        int first = firstAP.get();
        int last = firstPP.get();
        for (int k = first; k < last; k++) {
            int i = propIdx[k];
            Propagator propagator = propagators[i];
            if (cause != propagator && propagator.isActive()) { // due to idempotency of propagator, it should not schedule itself
                // schedule the coarse event recorder associated to thos
                //propagator.forcePropagate(EventType.FULL_PROPAGATION);
                throw new UnsupportedOperationException("Unsafe");
            }
        }
    }

    @Override
    public int getIdx(V variable) {
        return idxV;
    }

    @Override
    public void setIdx(V variable, int idx) {
        idxV = idx;
    }

    @Override
    public void flush() {
        // can be void
    }

    @Override
    public void enqueue() {
        enqueued = true;
        // <cp> include not yet activate propagator in the loop, to avoid maintain this outside
//        int first = firstAP.get();
        int last = firstPP.get();
        for (int k = 0; k < last; k++) {
            int i = propIdx[k];
            propagators[i].incNbPendingEvt();
        }
    }


    @Override
    public void deque() {
        enqueued = false;
        // <cp> include not yet activate propagator in the loop, to avoid maintain this outside
//        int first = firstAP.get();
        int last = firstPP.get();
        for (int k = 0; k < last; k++) {
            int i = propIdx[k];
            propagators[i].decNbPendingEvt();
        }
    }

    @Override
    public void activate(Propagator<V> element) {
        int firstA = firstAP.get();
        if (firstA == propagators.length) { // if this is the first propagator activated
            engine.activateFineEventRecorder(this);
        }
        // then, swap the propagator to the active part (between firstAP and firstPP)
        int id = p2i[element.getId() - offset];
        int i = 0;
        // find the idx of the propagator within the array
        while (i < firstA && propIdx[i] != id) {
            i++;
        }
        if (i < firstA) { // if not already active -- otherwise, there is a problem
            swapP(i, firstA - 1); // swap it with the last not yet active
            firstAP.add(-1);
        } else {
            assert false : element + " is already active";
        }
    }

    @Override
    public void desactivate(Propagator<V> element) {
        int first = firstAP.get();
        int last = this.firstPP.get();
        int id = p2i[element.getId() - offset];
        _desactivateP(id);
        // maintain active propagator indices list
        // 1. find the position of id in propIdx
        int i = first;
        while (i < last && propIdx[i] != id) {
            i++;
        }
        assert i < last : element + " is already passive";
        // 2. swap it with the last active
        swapP(i, last - 1); //swap it with the last active
        firstPP.add(-1); // decrease pointer to last active
        if (last == 1) { // if it was the last active propagator, desactivate this
            engine.desactivateFineEventRecorder(this);
            flush();
        }
    }

    protected void swapP(int i, int j) {
        if (i != j) {
            int pi = propIdx[i];
            propIdx[i] = propIdx[j];
            propIdx[j] = pi;
        }
    }

    void _desactivateP(int i) {
        // void
    }

    @Override
    public void virtuallyExecuted(Propagator propagator) {
        // void
    }

    @Override
    public String toString() {
        return "<< " + variables[VINDEX] + "::" + Arrays.toString(propagators) + ">>";
    }
}
