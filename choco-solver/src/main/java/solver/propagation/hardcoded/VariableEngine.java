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

import memory.IEnvironment;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationTrigger;
import solver.propagation.hardcoded.util.AId2AbId;
import solver.propagation.hardcoded.util.IId2AbId;
import solver.propagation.queues.CircularQueue;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
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
    protected Propagator[] propagators;

    protected final CircularQueue<Variable> var_queue;
    protected Variable lastVar;
    protected Propagator lastProp;
    protected final IId2AbId v2i; // mapping between propagator ID and its absolute index
    protected final boolean[] schedule;

    protected final BitSet[] eventsets;

    private boolean init;

    final PropagationTrigger trigger; // an object that starts the propagation


    public VariableEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();
        this.trigger = new PropagationTrigger(this, solver);

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
            _propagators.addAll(Arrays.asList(constraints[c].getPropagators()));
        }
        propagators = _propagators.toArray(new Propagator[_propagators.size()]);
        trigger.addAll(propagators);

        var_queue = new CircularQueue<Variable>(variables.length / 2);

        v2i = new AId2AbId(0, maxID, -1);
        for (int j = 0; j < variables.length; j++) {
            v2i.set(variables[j].getId(), j);
        }

        schedule = new boolean[variables.length];
        eventsets = new BitSet[maxID + 1];
        for (int i = 0; i < variables.length; i++) {
            int nbp = variables[i].getNbProps();
            eventsets[i] = new BitSet(nbp);
        }
        init = true;
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
    public boolean isInitialized() {
        return init;
    }

    @SuppressWarnings({"NullableProblems"})
    @Override
    public void propagate() throws ContradictionException {
        int id, mask, vIp;
        BitSet evtset;
        if (trigger.needToRun()) {
            trigger.propagate();
        }
        while (!var_queue.isEmpty()) {
            lastVar = var_queue.pollFirst();
            // revision of the variable
            id = v2i.get(lastVar.getId());
            schedule[id] = false;
            evtset = eventsets[id];
            for (int p = evtset.nextSetBit(0); p >= 0; p = evtset.nextSetBit(p + 1)) {
                lastProp = lastVar.getPropagator(p);
                vIp = lastVar.getIndiceInPropagator(p);
                assert lastProp.isActive() : "propagator is not active:" + lastProp;
                if (Configuration.PRINT_PROPAGATION) {
                    IPropagationEngine.Trace.printPropagation(lastVar, lastProp);
                }
                // clear event
                evtset.clear(p);
                mask = lastProp.getMask(vIp);
                lastProp.clearMask(vIp);
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
        BitSet evtset;
        if (lastVar != null) {
            id = v2i.get(lastVar.getId());
            // explicit iteration is mandatory to dec nb pending evt
            evtset = eventsets[id];
            for (int p = evtset.nextSetBit(0); p >= 0; p = evtset.nextSetBit(p + 1)) {
                lastProp = lastVar.getPropagator(p);
                lastProp.clearMask(lastVar.getIndiceInPropagator(p));
                lastProp.decNbPendingEvt();
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
                lastProp = lastVar.getPropagator(p);
                lastProp.clearMask(lastVar.getIndiceInPropagator(p));
                lastProp.decNbPendingEvt();
            }
            evtset.clear();
            schedule[id] = false;
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        if (Configuration.PRINT_VAR_EVENT) {
            IPropagationEngine.Trace.printModification(variable, type, cause);
        }
        int vid = v2i.get(variable.getId());
        boolean _schedule = false;
        int nbp = variable.getNbProps();
        for (int p = 0; p < nbp; p++) {
            Propagator prop = variable.getPropagator(p);
            int pindice = variable.getIndiceInPropagator(p);
            if (cause != prop && prop.isActive() && prop.advise(pindice, type.mask)) {
                if (prop.updateMask(pindice, type)) {
                    assert !eventsets[vid].get(p);
                    if (Configuration.PRINT_SCHEDULE) {
                        IPropagationEngine.Trace.printSchedule(prop);
                    }
                    prop.incNbPendingEvt();
                    eventsets[vid].set(p);
                } else if (Configuration.PRINT_SCHEDULE) {
                    IPropagationEngine.Trace.printAlreadySchedule(prop);
                }
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
                propagator.clearMask(i);
                eventsets[vid].clear(vi);
            }
        }
        propagator.flushPendingEvt();
    }

    @Override
    public void clear() {
        // void
    }

    @Override
    public void dynamicAddition(Constraint c, boolean cut) {
        int osize = propagators.length;
        int nbp = c.getPropagators().length;
        int nsize = osize + nbp;
        Propagator[] _propagators = propagators;
        propagators = new Propagator[nsize];
        System.arraycopy(_propagators, 0, propagators, 0, osize);
        System.arraycopy(c.getPropagators(), 0, propagators, osize, nbp);
        for (int j = osize; j < nsize; j++) {
            trigger.add(propagators[j], cut);
            Variable[] vars = propagators[j].getVars();
            for (int k = 0; k < vars.length; k++) {
                nbp = vars[k].getNbProps();
                int i = v2i.get(vars[k].getId());
                eventsets[i] = new BitSet(nbp);

            }
        }

    }
}
