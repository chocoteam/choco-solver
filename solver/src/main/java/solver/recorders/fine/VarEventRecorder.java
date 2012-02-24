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

import choco.kernel.memory.IStateInt;
import gnu.trove.map.hash.TIntIntHashMap;
import org.slf4j.LoggerFactory;
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
    protected final TIntIntHashMap p2i; // hashmap to retrieve the position of a propagator in propagators thanks to its pid
    protected final int[] propIdx; // an array of indices helping to get active propagators
    protected IStateInt firstAP; // index of the first active propagator in propIdx
    protected IStateInt firstPP; // index of the first passive propagator in propIdx

    public VarEventRecorder(V variable, Propagator<V>[] propagators, Solver solver) {
        super(solver);
        this.variable = variable;
        variable.addMonitor(this);
        this.propagators = propagators.clone();

        int n = propagators.length;
        p2i = new TIntIntHashMap(n);
        this.propIdx = new int[n];
        firstAP = solver.getEnvironment().makeInt(n);
        firstPP = solver.getEnvironment().makeInt(n);

        for (int i = 0; i < n; i++) {
            Propagator propagator = propagators[i];
            propagator.addRecorder(this);
            p2i.put(propagator.getId(), i);
            propIdx[i] = i;
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
        if (DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("\t|- {}", this.toString());
        boolean oneoremore = false;
        int first = firstAP.get();
        int last = firstPP.get();
        for (int k = first; k < last; k++) {
            int i = propIdx[k];
            Propagator propagator = propagators[i];
            if (cause != propagator) { // due to idempotency of propagator, it should not schedule itself
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
        int first = firstAP.get();
        int last = firstPP.get();
        for (int k = first; k < last; k++) {
            int i = propIdx[k];
            propagators[i].incNbRecorderEnqued();
        }
    }


    @Override
    public void deque() {
        enqueued = false;
        enqueued = false;
        int first = firstAP.get();
        int last = firstPP.get();
        for (int k = first; k < last; k++) {
            int i = propIdx[k];
            propagators[i].decNbRecrodersEnqued();
        }
    }

    @Override
    public void activate(Propagator<V> element) {
        int firstA = firstAP.get();
        if (firstA == propagators.length) { // if this is the first propagator activated
            variable.activate(this); // activate this
        }
        // then, swap the propagator to the active part (between firstAP and firstPP)
        int id = p2i.get(element.getId());
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
        int id = p2i.get(element.getId());
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
            variable.desactivate(this);
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
