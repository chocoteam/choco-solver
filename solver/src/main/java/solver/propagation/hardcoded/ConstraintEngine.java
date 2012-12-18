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
import solver.propagation.PropagationTrigger;
import solver.propagation.hardcoded.util.AId2AbId;
import solver.propagation.hardcoded.util.IId2AbId;
import solver.propagation.queues.CircularQueue;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * This engine is constraint-oriented one.
 * <br/>On a call to {@code onVariableUpdate}, it stores the event generated and schedules in a queue the propagators touched for future revision.
 * <br/>A propagator can schedule itself on a call to {@code schedulePropagator}, in this case, the propagator is pushed into
 * second queue for delayed propagation.
 * <br/>On a call to {@code propagate} a propagator is removed from the queue and propagated.
 * <br/>The queue of propagators for fine-grained events is always emptied before treating one element of the coarse-grained one.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public class ConstraintEngine implements IPropagationEngine {

    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;
    protected Propagator[] propagators;

    protected final CircularQueue<Propagator> pro_queue_f;
    protected Propagator lastProp;
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index
    protected boolean[] schedule;
    protected IBitset[] eventsets;
    protected int[][] eventmasks;

    private boolean init; // is ready to propagate?

    final PropagationTrigger trigger; // an object that starts the propagation

    public ConstraintEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();
        this.trigger = new PropagationTrigger(this, solver);

        variables = solver.getVars();
        List<Propagator> _propagators = new ArrayList<Propagator>();
        Constraint[] constraints = solver.getCstrs();
        int nbProp = 0;
        int m = Integer.MAX_VALUE, M = 0;
        for (int c = 0; c < constraints.length; c++) {
            Propagator[] cprops = constraints[c].propagators;
            for (int j = 0; j < cprops.length; j++, nbProp++) {
                _propagators.add(cprops[j]);
                int id = cprops[j].getId();
                m = Math.min(m, id);
                M = Math.max(M, id);
            }
        }
        propagators = _propagators.toArray(new Propagator[_propagators.size()]);
        p2i = new AId2AbId(m, M, -1);
        for (int j = 0; j < propagators.length; j++) {
            p2i.set(propagators[j].getId(), j);
        }
        trigger.addAll(propagators);

        pro_queue_f = new CircularQueue<Propagator>(propagators.length / 2 + 1);

        schedule = new boolean[nbProp];
        eventmasks = new int[nbProp][];
        eventsets = new IBitset[nbProp];
        for (int i = 0; i < nbProp; i++) {
            int nbv = propagators[i].getNbVars();
            eventmasks[i] = new int[nbv];
            eventsets[i] = BitsetFactory.make(nbv);
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
        int mask, aid;
        IBitset evtset;
        if (trigger.needToRun()) {
            trigger.propagate();
        }
        while (!pro_queue_f.isEmpty()) {
            lastProp = pro_queue_f.pollFirst();
            // revision of the variable
            aid = p2i.get(lastProp.getId());
            schedule[aid] = false;
            evtset = eventsets[aid];
            for (int v = evtset.nextSetBit(0); v >= 0; v = evtset.nextSetBit(v + 1)) {
                assert lastProp.isActive() : "propagator is not active";
                if (Configuration.PRINT_PROPAGATION) {
                    IPropagationEngine.Trace.printPropagation(lastProp.getVar(v), lastProp);
                }
                // clear event
                evtset.clear(v);
                mask = eventmasks[aid][v];
                eventmasks[aid][v] = 0;
                lastProp.decNbPendingEvt();
                // run propagation on the specific event
                lastProp.fineERcalls++;
                lastProp.propagate(v, mask);
            }
        }
    }

    @Override
    public void flush() {
        int aid;
        IBitset evtset;
        if (lastProp != null) {
            aid = p2i.get(lastProp.getId());
            evtset = eventsets[aid];
            for (int p = evtset.nextSetBit(0); p >= 0; p = evtset.nextSetBit(p + 1)) {
                eventmasks[aid][p] = 0;
            }
            evtset.clear();
            schedule[aid] = false;
            lastProp.flushPendingEvt();
        }
        while (!pro_queue_f.isEmpty()) {
            lastProp = pro_queue_f.pollFirst();
            // revision of the variable
            aid = p2i.get(lastProp.getId());
            evtset = eventsets[aid];
            for (int p = evtset.nextSetBit(0); p >= 0; p = evtset.nextSetBit(p + 1)) {
                eventmasks[aid][p] = 0;
            }
            evtset.clear();
            schedule[aid] = false;
            lastProp.flushPendingEvt();
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        if (Configuration.PRINT_VAR_EVENT) {
            IPropagationEngine.Trace.printModification(variable, type, cause);
        }
        int nbp = variable.getNbProps();
        for (int p = 0; p < nbp; p++) {
            Propagator prop = variable.getPropagator(p);
            int pindice = variable.getIndiceInPropagator(p);
            if (cause != prop && prop.isActive() && prop.advise(pindice, type.mask)) {
                int aid = p2i.get(prop.getId());
                if (eventmasks[aid][pindice] == 0) { // not scheduled yet
                    assert !eventsets[aid].get(pindice);
                    if (Configuration.PRINT_SCHEDULE) {
                        IPropagationEngine.Trace.printSchedule(prop);
                    }
                    prop.incNbPendingEvt();
                    eventsets[aid].set(pindice);
                } else if (Configuration.PRINT_SCHEDULE) {
                    IPropagationEngine.Trace.printAlreadySchedule(prop);
                }
                eventmasks[aid][pindice] |= type.strengthened_mask;
                if (!schedule[aid]) {
                    pro_queue_f.addLast(prop);
                    schedule[aid] = true;
                }
            }
        }

    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {
        desactivatePropagator(propagator);
    }

    @Override
    public void desactivatePropagator(Propagator propagator) {
        int pid = propagator.getId();
        int aid = p2i.get(pid);
        //if (aid > -1) {
        assert aid > -1 : "try to desactivate an unknown constraint";
        // we don't remove the element from its master to avoid costly operations
        IBitset evtset = eventsets[aid];
        for (int p = evtset.nextSetBit(0); p >= 0; p = evtset.nextSetBit(p + 1)) {
            eventmasks[aid][p] = 0;
        }
        evtset.clear();
        propagator.flushPendingEvt();
    }

    @Override
    public void clear() {
        // void
    }

    @Override
    public void dynamicAddition(Constraint c, boolean cut) {
        int osize = propagators.length;
        int nbp = c.propagators.length;
        int nsize = osize + nbp;
        Propagator[] _propagators = propagators;
        propagators = new Propagator[nsize];
        System.arraycopy(_propagators, 0, propagators, 0, osize);
        System.arraycopy(c.propagators, 0, propagators, osize, nbp);
        for (int j = osize; j < nsize; j++) {
            p2i.set(propagators[j].getId(), j);
            trigger.add(propagators[j], cut);
        }

        boolean[] _schedule = schedule;
        schedule = new boolean[nsize];
        System.arraycopy(_schedule, 0, schedule, 0, osize);

        int[][] _eventmasks = eventmasks;
        eventmasks = new int[nsize][];
        System.arraycopy(_eventmasks, 0, eventmasks, 0, osize);

        IBitset[] _eventsets = eventsets;
        eventsets = new IBitset[nsize];
        System.arraycopy(_eventsets, 0, eventsets, 0, osize);
        for (int i = osize; i < nsize; i++) {
            int nbv = propagators[i].getNbVars();
            eventmasks[i] = new int[nbv];
            eventsets[i] = BitsetFactory.make(nbv);
        }
    }
}
