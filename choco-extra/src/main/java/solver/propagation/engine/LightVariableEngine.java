/**
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
package solver.propagation.engine;

import memory.IEnvironment;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationTrigger;
import solver.propagation.hardcoded.util.AId2AbId;
import solver.propagation.hardcoded.util.IId2AbId;
import solver.propagation.queues.AQueue;
import solver.propagation.queues.CircularQueue;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Not that fast yet
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 04/10/13
 */
public class LightVariableEngine implements IPropagationEngine {

    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final AQueue<Variable> var_queue;
    protected int lastVarIdx;
    protected final boolean[] schedule;
    protected final PropagationTrigger trigger; // an object that starts the propagation
    protected final IId2AbId v2i; // mapping between propagator ID and its absolute index
    private boolean init;
    protected final ICause[][] varevtcause;

    protected final ICause[] prop_causes = new ICause[4];
    private final static ICause NONE = new ICause() {
        public void explain(Deduction d, Explanation e) {
            throw new UnsupportedOperationException();
        }
    };

    public static long nbPOP = 0;

    public LightVariableEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();
        this.trigger = new PropagationTrigger(this, solver);
        Variable[] variables = solver.getVars();
        int n = variables.length;
        int maxID = 0;
        for (int i = 0; i < n; i++) {
            if (maxID == 0 || maxID < variables[i].getId()) {
                maxID = variables[i].getId();
            }
        }
        Constraint[] constraints = solver.getCstrs();
        List<Propagator> _propagators = new ArrayList<Propagator>();
        for (int c = 0; c < constraints.length; c++) {
            _propagators.addAll(Arrays.asList(constraints[c].getPropagators()));
        }
        trigger.addAll(_propagators.toArray(new Propagator[_propagators.size()]));
        var_queue = new CircularQueue<Variable>(n / 2);
        v2i = new AId2AbId(0, maxID, -1);
        for (int j = 0; j < n; j++) {
            v2i.set(variables[j].getId(), j);
        }
        schedule = new boolean[n];
        varevtcause = new ICause[n][4];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 4; j++) {
                varevtcause[i][j] = NONE;
            }
        }
        lastVarIdx = -1;
        init = true;
    }

    @Override
    public boolean isInitialized() {
        return init;
    }

    @SuppressWarnings({"NullableProblems"})
    @Override
    public void propagate() throws ContradictionException {
        if (trigger.needToRun()) {
            trigger.propagate();
        }
        Variable v;
        Propagator[] props;
        Propagator pr;
        int id, idx, mask;
        while (!var_queue.isEmpty()) {
            nbPOP++;
            v = var_queue.pollFirst();
            id = v2i.get(v.getId());
            lastVarIdx = id;
            assert schedule[id];
            schedule[id] = false;
            for (int i = 0; i < 4; i++) {
                prop_causes[i] = varevtcause[id][i];
                varevtcause[id][i] = NONE;
            }
            props = v.getPropagators();
            int n = props.length;
			for(int i=0;i<n;i++){
                pr = props[i];
                if (pr.isActive()) {
                    mask = 0;
                    if (prop_causes[0] != NONE && prop_causes[0] != pr) {
                        mask = EventType.INSTANTIATE.strengthened_mask;
                    } else {
                        if (prop_causes[1] != NONE && prop_causes[1] != pr) {
                            mask = EventType.INCLOW.strengthened_mask;
                        }
                        if (prop_causes[2] != NONE && prop_causes[2] != pr) {
                            mask |= EventType.DECUPP.strengthened_mask;
                        }
                        if (prop_causes[3] != NONE && prop_causes[3] != pr) {
                            mask |= EventType.REMOVE.strengthened_mask;
                        }
                    }
                    idx = v.getIndexInPropagator(i);
                    if (mask > 0 && pr.advise(idx, mask)) {
                        pr.fineERcalls++;
                        pr.propagate(idx, mask);
                    }
                }
            }
        }
        lastVarIdx = -1;
    }

    @Override
    public void flush() {
        if (lastVarIdx >= 0) {
            schedule[lastVarIdx] = false;
            Arrays.fill(varevtcause[lastVarIdx], NONE);
        }
        while (!var_queue.isEmpty()) {
            lastVarIdx = v2i.get(var_queue.pollFirst().getId());
            schedule[lastVarIdx] = false;
            Arrays.fill(varevtcause[lastVarIdx], NONE);
        }
        lastVarIdx = -1;
    }

    @Override
    public void fails(ICause cause, Variable variable, String message) throws ContradictionException {
        throw exception.set(cause, variable, message);
    }

    @Override
    public ContradictionException getContradictionException() {
        return exception;
    }

    @Override
    public void clear() {
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        int vid = v2i.get(variable.getId());
        int mask = type.mask;
        ICause[] causes = varevtcause[vid];
        if ((mask & EventType.INSTANTIATE.mask) != 0) {
            causes[0] = cause;
        }
        if ((mask & EventType.INCLOW.mask) != 0) {
            causes[1] = cause;
        }
        if ((mask & EventType.DECUPP.mask) != 0) {
            causes[2] = cause;
        }
        if ((mask & EventType.REMOVE.mask) != 0) {
            causes[3] = cause;
        }
        if (!schedule[vid]) {
            var_queue.addLast(variable);
            schedule[vid] = true;
        }
    }

    @Override
    public void delayedPropagation(Propagator propagator, EventType type) throws ContradictionException {

    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {
    }

    @Override
    public void desactivatePropagator(Propagator propagator) {
    }

    @Override
    public void dynamicAddition(Constraint c, boolean cut) {
        throw new UnsupportedOperationException("Dynamic constraint addition is not available within FastVariableEngine");
    }
}
