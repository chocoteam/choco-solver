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
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationTrigger;
import solver.propagation.hardcoded.util.AId2AbId;
import solver.propagation.hardcoded.util.IId2AbId;
import solver.variables.EventType;
import solver.variables.Variable;
import util.objects.IntCircularQueue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * This engine handles two sets of queues.
 * The first sets of queues manages 'fine events' (that is, calls to `propagate(int,int)'
 * and considers three different subsets:
 * UNARY, BINARY, TERNARY propagators are stored in the top-priority queue,
 * LINEAR propagators are stored in the mid-priority queue,
 * QUADRATIC, CUBIC, VERY_SLOW propagators are stored in the low priority queue.
 *
 * Then, managing coarse events (that is, delayed calls to `propagate(int)') is made thanks to 4 additional queues:
 * UNARY, BINARY, TERNARY propagators cannot be delayed!
 * LINEAR propagators are in the top-priority queue,
 * QUADRATIC propagators are in the second-priority queue,
 * CUBIC propagators are in the second-priority queue,
 * VERY_SLOW propagators are in the second-priority queue.
 *
 * The engine empties the first queue, then propagates one event from the following one, and check the first queue again, etc.
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public class TwoBucketsPropagationEngine implements IPropagationEngine {

    private static final int WORD_MASK = 0xffffffff;

    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;
    protected Propagator[] propagators;
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index

    protected Propagator lastProp;
    protected int notEmpty; // point out the no empty queues

    protected final ArrayDeque<Propagator> pro_queue_f1;
    protected final ArrayDeque<Propagator> pro_queue_f2;
    protected final ArrayDeque<Propagator> pro_queue_f3;
    protected boolean[] schedule_f; // also maintains the index of the queue!
    protected IntCircularQueue[] event_f;

    protected final ArrayDeque<Propagator> pro_queue_c1;
    protected final ArrayDeque<Propagator> pro_queue_c2;
    protected final ArrayDeque<Propagator> pro_queue_c3;
    protected final ArrayDeque<Propagator> pro_queue_c4;
    protected boolean[] schedule_c;
    protected EventType[] event_c;

    private boolean init; // is ready to propagate?

    final PropagationTrigger trigger; // an object that starts the propagation

    public TwoBucketsPropagationEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();
        this.trigger = new PropagationTrigger(this, solver);

        variables = solver.getVars();
        List<Propagator> _propagators = new ArrayList<Propagator>();
        Constraint[] constraints = solver.getCstrs();
        int nbProp = 0;
        int m = Integer.MAX_VALUE, M = 0;
        for (int c = 0; c < constraints.length; c++) {
            Propagator[] cprops = constraints[c].getPropagators();
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

        pro_queue_f1 = new ArrayDeque<>(propagators.length / 2 + 1);
        pro_queue_f2 = new ArrayDeque<>(propagators.length / 2 + 1);
        pro_queue_f3 = new ArrayDeque<>(propagators.length / 2 + 1);
        schedule_f = new boolean[nbProp];


        pro_queue_c1 = new ArrayDeque<>(propagators.length / 2 + 1);
        pro_queue_c2 = new ArrayDeque<>(propagators.length / 2 + 1);
        pro_queue_c3 = new ArrayDeque<>(propagators.length / 2 + 1);
        pro_queue_c4 = new ArrayDeque<>(propagators.length / 2 + 1);
        schedule_c = new boolean[nbProp];

        notEmpty = 0;

        event_f = new IntCircularQueue[nbProp];
        for (int i = 0; i < nbProp; i++) {
            int nbv = propagators[i].getNbVars();
            event_f[i] = new IntCircularQueue(nbv);
        }
        event_c = new EventType[nbProp];
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
        if (trigger.needToRun()) {
            trigger.propagate();
        }
        for (int i = nextNotEmpty(0); i > -1; i = nextNotEmpty(0)) {
            switch (i) {
                case 0:
                    while (!pro_queue_f1.isEmpty()) {
                        propagateFine(pro_queue_f1);
                    }
                    notEmpty = notEmpty & ~1;
                    break;
                case 1:
                    propagateFine(pro_queue_f2);
                    if (pro_queue_f2.isEmpty()) {
                        notEmpty = notEmpty & ~(1 << 1);
                    }
                    break;
                case 2:
                    propagateFine(pro_queue_f3);
                    if (pro_queue_f3.isEmpty()) {
                        notEmpty = notEmpty & ~(1 << 2);
                    }
                    break;
                case 3:
                    propagateCoarse(pro_queue_c1);
                    if (pro_queue_c1.isEmpty()) {
                        notEmpty = notEmpty & ~(1 << 3);
                    }
                    break;
                case 4:
                    propagateCoarse(pro_queue_c2);
                    if (pro_queue_c2.isEmpty()) {
                        notEmpty = notEmpty & ~(1 << 4);
                    }
                    break;
                case 5:
                    propagateCoarse(pro_queue_c3);
                    if (pro_queue_c3.isEmpty()) {
                        notEmpty = notEmpty & ~(1 << 5);
                    }
                    break;
                case 6:
                    propagateCoarse(pro_queue_c4);
                    if (pro_queue_c4.isEmpty()) {
                        notEmpty = notEmpty & ~(1 << 6);
                    }
                    break;
            }
        }

    }

    private int nextNotEmpty(int fromIndex) {
        int word = notEmpty & (WORD_MASK << fromIndex);
        if (word != 0) {
            return Integer.numberOfTrailingZeros(word);
        } else {
            return -1;
        }
    }

    private void propagateFine(ArrayDeque<Propagator> pro_queue_f) throws ContradictionException {
        lastProp = pro_queue_f.pollFirst();
        // revision of the variable
        int aid = p2i.get(lastProp.getId());
        schedule_f[aid] = false;
        IntCircularQueue evtset = event_f[aid];
        //for (int v = evtset.nextSetBit(0); v >= 0; v = evtset.nextSetBit(v + 1)) {
        while(!evtset.isEmpty()){
            int v = evtset.pollFirst();
            assert lastProp.isActive() : "propagator is not active";
            if (Configuration.PRINT_PROPAGATION) {
                Trace.printPropagation(lastProp.getVar(v), lastProp);
            }
            // clear event
            //evtset.clear(v);
            int mask = lastProp.getMask(v);
            lastProp.clearMask(v);
            //lastProp.decNbPendingEvt();
            // run propagation on the specific event
            lastProp.fineERcalls++;
            lastProp.propagate(v, mask);
        }
        // This part is for debugging only!!
        if (Configuration.Idem.disabled != Configuration.IDEMPOTENCY) {
            FakeEngine.checkIdempotency(lastProp);
        }
    }

    private void propagateCoarse(ArrayDeque<Propagator> pro_queue_c) throws ContradictionException {
        lastProp = pro_queue_c.pollFirst();
        // revision of the variable
        int aid = p2i.get(lastProp.getId());
        schedule_c[aid] = false;
        EventType evt = event_c[aid];
        event_c[aid] = EventType.VOID;
        assert lastProp.isActive() : "propagator is not active";
        if (Configuration.PRINT_PROPAGATION) {
            Trace.printPropagation(null, lastProp);
        }
        lastProp.coarseERcalls++;
        lastProp.propagate(evt.getStrengthenedMask());
    }


    @Override
    public void flush() {
        if (lastProp != null) {
            flushFine();
            flushCoarse();
        }
        for (int i = nextNotEmpty(0); i > -1; i = nextNotEmpty(0)) {
            switch (i) {
                case 0:
                    while (!pro_queue_f1.isEmpty()) {
                        lastProp = pro_queue_f1.pollFirst();
                        flushFine();
                    }
                    break;
                case 1:
                    while (!pro_queue_f2.isEmpty()) {
                        lastProp = pro_queue_f2.pollFirst();
                        flushFine();
                    }
                    break;
                case 2:
                    while (!pro_queue_f3.isEmpty()) {
                        lastProp = pro_queue_f3.pollFirst();
                        flushFine();
                    }
                    break;
                case 3:
                    while (!pro_queue_c1.isEmpty()) {
                        lastProp = pro_queue_c1.pollFirst();
                        flushCoarse();
                    }
                    break;
                case 4:
                    while (!pro_queue_c2.isEmpty()) {
                        lastProp = pro_queue_c2.pollFirst();
                        flushCoarse();
                    }
                    break;
                case 5:
                    while (!pro_queue_c3.isEmpty()) {
                        lastProp = pro_queue_c3.pollFirst();
                        flushCoarse();
                    }
                    break;
                case 6:
                    while (!pro_queue_c4.isEmpty()) {
                        lastProp = pro_queue_c4.pollFirst();
                        flushCoarse();
                    }
                    break;
            }
            notEmpty = notEmpty & ~(1 << i);
        }
    }

    private void flushFine() {
        int aid = p2i.get(lastProp.getId());
        IntCircularQueue evtset = event_f[aid];
        //for (int p = evtset.nextSetBit(0); p >= 0; p = evtset.nextSetBit(p + 1)) {
        while(!evtset.isEmpty()){
            int p = evtset.pollLast();
            lastProp.clearMask(p);
        }
        evtset.clear();
        schedule_f[aid] = false;
    }

    private void flushCoarse() {
        int aid = p2i.get(lastProp.getId());
        schedule_c[aid] = false;
        event_c[aid] = EventType.VOID;
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        if (Configuration.PRINT_VAR_EVENT) {
            Trace.printModification(variable, type, cause);
        }
        int nbp = variable.getNbProps();
        for (int p = 0; p < nbp; p++) {
            Propagator prop = variable.getPropagator(p);
            int pindice = variable.getIndexInPropagator(p);
            if (cause != prop && prop.isActive() && prop.advise(pindice, type.mask)) {
                int aid = p2i.get(prop.getId());
                if (prop.updateMask(pindice, type)) { // not scheduled yet
                    //assert !event_f[aid].get(pindice);
                    if (Configuration.PRINT_SCHEDULE) {
                        Trace.printSchedule(prop);
                    }
                    //prop.incNbPendingEvt();
                    event_f[aid].addLast(pindice);//set(pindice);
                } else if (Configuration.PRINT_SCHEDULE) {
                    Trace.printAlreadySchedule(prop);
                }
                if (!schedule_f[aid]) {
                    PropagatorPriority prio = /*dynamic ? prop.dynPriority() :*/ prop.getPriority();
                    switch (prio) {
                        case UNARY:
                        case BINARY:
                        case TERNARY:
                            pro_queue_f1.addLast(prop);
                            schedule_f[aid] = true;
                            notEmpty = notEmpty | 1;
                            break;
                        case LINEAR:
                            pro_queue_f2.addLast(prop);
                            schedule_f[aid] = true;
                            notEmpty = notEmpty | (1 << 1);
                            break;
                        case QUADRATIC:
                        case CUBIC:
                        case VERY_SLOW:
                        default:
                            pro_queue_f3.addLast(prop);
                            schedule_f[aid] = true;
                            notEmpty = notEmpty | (1 << 2);
                            break;
                    }
                }
            }
        }

    }

    @Override
    public void delayedPropagation(Propagator propagator, EventType type) throws ContradictionException {
        int aid = p2i.get(propagator.getId());
        if (!schedule_c[aid]) {
            PropagatorPriority prio = /*dynamic ? prop.dynPriority() :*/ propagator.getPriority();
            switch (prio) {
                case UNARY:
                case BINARY:
                case TERNARY:
                    throw new SolverException("Cannot schedule coarse event for low priority propagator.");
                case LINEAR:
                    pro_queue_c1.addLast(propagator);
                    schedule_c[aid] = true;
                    event_c[aid] = type;
                    notEmpty = notEmpty | (1 << 3);
                    break;
                case QUADRATIC:
                    pro_queue_c2.addLast(propagator);
                    schedule_c[aid] = true;
                    event_c[aid] = type;
                    notEmpty = notEmpty | (1 << 4);
                    break;
                case CUBIC:
                    pro_queue_c3.addLast(propagator);
                    schedule_c[aid] = true;
                    event_c[aid] = type;
                    notEmpty = notEmpty | (1 << 5);
                    break;
                case VERY_SLOW:
                default:
                    pro_queue_c4.addLast(propagator);
                    schedule_c[aid] = true;
                    event_c[aid] = type;
                    notEmpty = notEmpty | (1 << 6);
                    break;
            }
        }
    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {
        lastProp = propagator;
        flushFine();
        flushCoarse();
    }

    @Override
    public void desactivatePropagator(Propagator propagator) {
        lastProp = propagator;
        flushFine();
        flushCoarse();
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
            p2i.set(propagators[j].getId(), j);
            trigger.add(propagators[j], cut);
        }

        boolean[] _schedule_f = schedule_f;
        schedule_f = new boolean[nsize];
        System.arraycopy(_schedule_f, 0, schedule_f, 0, osize);

        boolean[] _schedule_c = schedule_c;
        schedule_c = new boolean[nsize];
        System.arraycopy(_schedule_c, 0, schedule_c, 0, osize);


        IntCircularQueue[] _event_f = event_f;
        event_f = new IntCircularQueue[nsize];
        System.arraycopy(_event_f, 0, event_f, 0, osize);
        for (int i = osize; i < nsize; i++) {
            int nbv = propagators[i].getNbVars();
            event_f[i] = new IntCircularQueue(nbv);
        }
        EventType[] _event_c = event_c;
        event_c = new EventType[nsize];
        System.arraycopy(_event_c, 0, event_c, 0, osize);

    }
}
