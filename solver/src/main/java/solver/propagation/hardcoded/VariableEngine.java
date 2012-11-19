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
package solver.propagation.hardcoded;

import choco.kernel.common.util.objects.BitsetFactory;
import choco.kernel.common.util.objects.IBitset;
import choco.kernel.memory.IEnvironment;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationUtils;
import solver.propagation.hardcoded.util.AId2AbId;
import solver.propagation.hardcoded.util.IId2AbId;
import solver.propagation.queues.CircularQueue;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This engine is variable-oriented one.
 * <br/>On a call to {@code onVariableUpdate}, it stores the event generated and schedules in a queue the variable for future revision.
 * <br/>A propagator can schedule itself on a call to {@code schedulePropagator}, in this case, the propagator is pushed into
 * second queue for delayed propagation.
 * <br/>On a call to {@code propagate} a variable is removed from the queue and a loop over its active propagator is achieved.
 * <br/>The queue of variables is always emptied before treating one element of the propagator one.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public class VariableEngine implements IPropagationEngine {

    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;
    protected final Propagator[] propagators;

    protected final CircularQueue<Variable> var_queue;
    protected Variable lastVar;
    protected Propagator lastProp;
    protected final IId2AbId v2i; // mapping between propagator ID and its absolute index
    protected final boolean[] schedule;

    protected final IBitset[] eventsets;
    protected final int[][] eventmasks;


    public VariableEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        variables = solver.getVars();
        int maxID = 0;
        for (int i = 0; i < variables.length; i++) {
            if (maxID == 0 || maxID < variables[i].getId()) {
                maxID = variables[i].getId();
            }
        }

        Constraint[] constraints = solver.getCstrs();
        List<Propagator> _propagators = new ArrayList<Propagator>();
        for (int c = 0; c < constraints.length; c++) {
            _propagators.addAll(Arrays.asList(constraints[c].propagators));
        }
        propagators = _propagators.toArray(new Propagator[_propagators.size()]);

        var_queue = new CircularQueue<Variable>(variables.length / 2);

        v2i = new AId2AbId(0, maxID, -1);
        for (int j = 0; j < variables.length; j++) {
            v2i.set(variables[j].getId(), j);
        }

        schedule = new boolean[variables.length];
        eventsets = new IBitset[maxID + 1];
        eventmasks = new int[maxID + 1][];
        for (int i = 0; i < variables.length; i++) {
            int nbp = variables[i].getNbProps();
            eventsets[i] = BitsetFactory.make(nbp);
            eventmasks[i] = new int[nbp];
        }
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
    public void init(Solver solver) {
    }

    @SuppressWarnings({"NullableProblems"})
    @Override
    public void propagate() throws ContradictionException {
        int id, mask;
        IBitset evtset;
        while (!var_queue.isEmpty()) {
            lastVar = var_queue.pollFirst();
            // revision of the variable
            id = v2i.get(lastVar.getId());
            schedule[id] = false;
            evtset = eventsets[id];
            for (int p = evtset.nextSetBit(0); p >= 0; p = evtset.nextSetBit(p + 1)) {
                if (Configuration.PRINT_PROPAGATION) {
                    PropagationUtils.printPropagation(lastVar, lastProp);
                }
                lastProp = lastVar.getPropagator(p);
                // clear event
                evtset.clear(p);
                mask = eventmasks[id][p];
                eventmasks[id][p] = 0;
                lastProp.decNbPendingEvt();
                // run propagation on the specific evt
                lastProp.fineERcalls++;
                lastProp.propagate(lastVar.getIndiceInPropagator(p), mask);
            }
        }
    }

    @Override
    public void flush() {
        int id;
        IBitset evtset;
        if (lastVar != null) {
            id = v2i.get(lastVar.getId());
            // explicit iteration is mandatory to dec nb pending evt
            evtset = eventsets[id];
            for (int p = evtset.nextSetBit(0); p >= 0; p = evtset.nextSetBit(p + 1)) {
                lastVar.getPropagator(p).decNbPendingEvt();
                eventmasks[id][p] = 0;
            }
            evtset.clear();
            schedule[id] = false;
        }
        while (!var_queue.isEmpty()) {
            lastVar = var_queue.pollFirst();
            // revision of the variable
            id = v2i.get(lastVar.getId());
            // explicit iteration is mandatory to dec nb pending evt
            evtset = eventsets[id];
            for (int p = evtset.nextSetBit(0); p >= 0; p = evtset.nextSetBit(p + 1)) {
                lastVar.getPropagator(p).decNbPendingEvt();
                eventmasks[id][p] = 0;
            }
            evtset.clear();
            schedule[id] = false;
        }
    }

    public void check() {
        for (int i = 0; i < eventmasks.length; i++) {
            if (eventmasks[i] != null)
                for (int j = 0; j < eventmasks[i].length; j++) {
                    assert eventmasks[i][j] == 0 : "MASK NOT CLEARED " + variables[0].getSolver().getMeasures().toOneShortLineString();
                }
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        if (Configuration.PRINT_VAR_EVENT) {
            PropagationUtils.printModification(variable, type, cause);
        }
        int vid = v2i.get(variable.getId());
        boolean _schedule = false;
        int nbp = variable.getNbProps();
        for (int p = 0; p < nbp; p++) {
            Propagator prop = variable.getPropagator(p);
            int pindice = variable.getIndiceInPropagator(p);
            if (cause != prop && prop.isActive() && prop.advise(pindice, type.mask)) {
                if (eventmasks[vid][p] == 0) {
                    assert !eventsets[vid].get(p);
                    if (Configuration.PRINT_SCHEDULE) {
                        PropagationUtils.printSchedule(prop);
                    }
                    prop.incNbPendingEvt();
                    eventsets[vid].set(p);
                } else if (Configuration.PRINT_SCHEDULE) {
                    PropagationUtils.printAlreadySchedule(prop);
                }
                eventmasks[vid][p] |= type.strengthened_mask;
                _schedule = true;
            }
        }
        if (!schedule[vid] && _schedule) {
            var_queue.addLast(variable);
            schedule[vid] = true;
        }

    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {
        desactivatePropagator(propagator);
    }

    @Override
    public void desactivatePropagator(Propagator propagator) {
        Variable[] variables = propagator.getVars();
        int[] vindices = propagator.getVIndices();
        for (int i = 0; i < variables.length; i++) {
            int vi = vindices[i];
            if (vi > -1) {// constants and reified propagators have a negative index
                assert variables[i].getPropagator(vi) == propagator : propagator.toString() + " >> " + variables[i];
                int vid = v2i.get(variables[i].getId());
                assert vi < eventmasks[vid].length;
                eventsets[vid].clear(vi);
                eventmasks[vid][vi] = 0;
            }
        }
        propagator.flushPendingEvt();
    }

    @Override
    public void clear() {
        // void
    }
}
