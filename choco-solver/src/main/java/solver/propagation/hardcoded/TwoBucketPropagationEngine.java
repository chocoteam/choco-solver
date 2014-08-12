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
import solver.propagation.hardcoded.util.IId2AbId;
import solver.propagation.hardcoded.util.MId2AbId;
import solver.variables.EventType;
import solver.variables.Variable;
import util.objects.IntCircularQueue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This engine handles two sets of queues.
 * The first sets of queues manages 'fine events' (that is, calls to `propagate(int,int)'
 * and considers three different subsets:
 * UNARY, BINARY, TERNARY propagators are stored in the top-priority queue,
 * LINEAR propagators are stored in the mid-priority queue,
 * QUADRATIC, CUBIC, VERY_SLOW propagators are stored in the low priority queue.
 * <p/>
 * Then, managing coarse events (that is, delayed calls to `propagate(int)') is made thanks to 4 additional queues:
 * UNARY, BINARY, TERNARY propagators cannot be delayed!
 * LINEAR propagators are in the top-priority queue,
 * QUADRATIC propagators are in the second-priority queue,
 * CUBIC propagators are in the second-priority queue,
 * VERY_SLOW propagators are in the second-priority queue.
 * <p/>
 * The engine empties the first queue, then propagates one event from the following one, and check the first queue again, etc.
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public class TwoBucketPropagationEngine implements IPropagationEngine {

    private static final int WORD_MASK = 0xffffffff;
    private static final short[] match_f = Configuration.FINE_EVENT_QUEUES;
    private static final short[] match_c = Configuration.COARSE_EVENT_QUEUES;

    private static final short max_f;
    private static final short max_c;

    static {
        short _max_ = -1;
        for (int i = 0; i < match_f.length; i++) {
            if (_max_ < match_f[i]) _max_ = match_f[i];
        }
        _max_++;
        max_f = _max_;
        _max_ = -1;
        for (int i = 0; i < match_c.length; i++) {
            if (_max_ < match_c[i]) _max_ = match_c[i];
        }
        _max_++;
        max_c = _max_;
    }

    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;
    protected Propagator[] propagators;
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index

    protected Propagator lastProp;
    protected int notEmpty; // point out the no empty queues

    protected final ArrayDeque<Propagator>[] pro_queue_f;
    protected boolean[] schedule_f; // also maintains the index of the queue!
    protected IntCircularQueue[] event_f;
    protected int[][] eventmasks;// the i^th event mask stores modification events on the i^th variable, since the last propagation

    protected final ArrayDeque<Propagator>[] pro_queue_c;
    protected boolean[] schedule_c;
    protected EventType[] event_c;

    private boolean init; // is ready to propagate?

    final PropagationTrigger trigger; // an object that starts the propagation

    public TwoBucketPropagationEngine(Solver solver) {
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
        //p2i = new AId2AbId(m, M, -1);
        p2i = new MId2AbId(M - m + 1, -1);
        for (int j = 0; j < propagators.length; j++) {
            p2i.set(propagators[j].getId(), j);
        }
        trigger.addAll(propagators);

        pro_queue_f = new ArrayDeque[max_f];
        for (int i = 0; i < max_f; i++) {
            pro_queue_f[i] = new ArrayDeque<>(propagators.length / 2 + 1);
        }
        schedule_f = new boolean[nbProp];


        pro_queue_c = new ArrayDeque[max_c];
        for (int i = 0; i < max_c; i++) {
            pro_queue_c[i] = new ArrayDeque<>(propagators.length / 2 + 1);
        }
        schedule_c = new boolean[nbProp];

        notEmpty = 0;

        event_f = new IntCircularQueue[nbProp];
        eventmasks = new int[nbProp][];
        for (int i = 0; i < nbProp; i++) {
            if (propagators[i].reactToFineEvent()) {
                int nbv = propagators[i].getNbVars();
                event_f[i] = new IntCircularQueue(nbv);
                eventmasks[i] = new int[nbv];
            }
        }
        event_c = new EventType[nbProp];
        Arrays.fill(event_c, EventType.VOID);
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
            if (i == 0) { // specific case, for finest events
                while (!pro_queue_f[i].isEmpty()) {
                    propagateFine(pro_queue_f[i]);
                }
                notEmpty = notEmpty & ~1;
            } else if (i < max_f) { // other finest events, lower priority
                propagateFine(pro_queue_f[i]);
                if (pro_queue_f[i].isEmpty()) {
                    notEmpty = notEmpty & ~(1 << i);
                }
            } else { // coarse events
                int j = i - max_f;
                propagateCoarse(pro_queue_c[j]);
                if (pro_queue_c[j].isEmpty()) {
                    notEmpty = notEmpty & ~(1 << i);
                }
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
        assert schedule_f[aid] : "try to propagate an unscheduled propagator";
        schedule_f[aid] = false;
        if (lastProp.reactToFineEvent()) {
            IntCircularQueue evtset = event_f[aid];
            while (!evtset.isEmpty()) {
                int v = evtset.pollFirst();
                assert lastProp.isActive() : "propagator is not active:" + lastProp;
                if (Configuration.PRINT_PROPAGATION) {
                    Trace.printPropagation(lastProp.getVar(v), lastProp);
                }
                // clear event
                int mask = eventmasks[aid][v];
                eventmasks[aid][v] = 0;
                // run propagation on the specific event
                lastProp.fineERcalls++;
                lastProp.propagate(v, mask);
            }
        } else if(lastProp.isActive()){ // need to be checked due to views
            //assert lastProp.isActive() : "propagator is not active:" + lastProp;
            if (Configuration.PRINT_PROPAGATION) {
                Trace.printPropagation(null, lastProp);
            }
            lastProp.propagate(EventType.FULL_PROPAGATION.getMask());
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
        assert schedule_c[aid] : "try to propagate an unscheduled propagator";
        schedule_c[aid] = false;
        EventType evt = event_c[aid];
        event_c[aid] = EventType.VOID;
        assert lastProp.isActive() : "propagator is not active:" + lastProp;
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
            if (i < max_f) { // other finest events, lower priority
                while (!pro_queue_f[i].isEmpty()) {
                    lastProp = pro_queue_f[i].pollLast();
                    flushFine();
                }
            } else { // coarse events
                while (!pro_queue_c[i - max_f].isEmpty()) {
                    lastProp = pro_queue_c[i - max_f].pollLast();
                    flushCoarse();
                }
            }
            notEmpty = notEmpty & ~(1 << i);
        }
    }

    private void flushFine() {
        int aid = p2i.get(lastProp.getId());
        if (lastProp.reactToFineEvent()) {
            IntCircularQueue evtset = event_f[aid];
            while (!evtset.isEmpty()) {
                eventmasks[aid][evtset.pollLast()] = 0;
            }
            evtset.clear();
        }
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
                if (prop.reactToFineEvent()) {
                    boolean needSched = (eventmasks[aid][pindice] == 0);
                    eventmasks[aid][pindice] |= type.strengthened_mask;
                    if (needSched) {
                        //assert !event_f[aid].get(pindice);
                        if (Configuration.PRINT_SCHEDULE) {
                            Trace.printSchedule(prop);
                        }
                        event_f[aid].addLast(pindice);
                    } else if (Configuration.PRINT_SCHEDULE) {
                        Trace.printAlreadySchedule(prop);
                    }
                }
                if (!schedule_f[aid]) {
                    PropagatorPriority prio = prop.getPriority();
                    int q = match_f[prio.priority - 1];
                    pro_queue_f[q].addLast(prop);
                    schedule_f[aid] = true;
                    notEmpty = notEmpty | (1 << q);
                }
            }
        }

    }

    @Override
    public void delayedPropagation(Propagator propagator, EventType type) throws ContradictionException {
        int aid = p2i.get(propagator.getId());
        if (!schedule_c[aid]) {
            PropagatorPriority prio = /*dynamic ? prop.dynPriority() :*/ propagator.getPriority();
            int q = match_c[prio.priority - 1];
            if (q == -1) throw new SolverException("Cannot schedule coarse event for low priority propagator.");
            pro_queue_c[q].addLast(propagator);
            schedule_c[aid] = true;
            event_c[aid] = type;
            notEmpty = notEmpty | (1 << (q + max_f));
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
    public void dynamicAddition(Constraint c, boolean permanent) {
        int osize = propagators.length;
        int nbp = c.getPropagators().length;
        int nsize = osize + nbp;
        Propagator[] _propagators = propagators;
        propagators = new Propagator[nsize];
        System.arraycopy(_propagators, 0, propagators, 0, osize);
        System.arraycopy(c.getPropagators(), 0, propagators, osize, nbp);
        for (int j = osize; j < nsize; j++) {
            p2i.set(propagators[j].getId(), j);
            trigger.dynAdd(propagators[j], permanent);
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
            if (propagators[i].reactToFineEvent()) {
                event_f[i] = new IntCircularQueue(propagators[i].getNbVars());
            }
        }
        EventType[] _event_c = event_c;
        event_c = new EventType[nsize];
        System.arraycopy(_event_c, 0, event_c, 0, osize);
        Arrays.fill(event_c, osize, nsize, EventType.VOID);

        int[][] _eventmasks = eventmasks;
        eventmasks = new int[nsize][];
        System.arraycopy(_eventmasks, 0, eventmasks, 0, osize);
        for (int i = osize; i < nsize; i++) {
            if (propagators[i].reactToFineEvent()) {
                eventmasks[i] = new int[propagators[i].getNbVars()];
            }
        }
    }

    @Override
    public void dynamicDeletion(Constraint c) {
        for (Propagator toDelete : c.getPropagators()) {
            int nsize = propagators.length - 1;
            Propagator toMove = propagators[nsize];
            int idtd = p2i.get(toDelete.getId());
            int idtm = p2i.get(toMove.getId());


            assert idtd <= idtm : "wrong id for prop to delete";

            // 1. remove from propagators[] and p2i
            Propagator[] _propagators = propagators;
            propagators = new Propagator[nsize];
            System.arraycopy(_propagators, 0, propagators, 0, nsize);

            // 2. resize schedule_f[]
            boolean sftm = schedule_f[idtm];
            assert !schedule_f[idtd] : "try to delete a propagator which is scheduled (fine)";
            boolean[] _schedule_f = schedule_f;
            schedule_f = new boolean[nsize];
            System.arraycopy(_schedule_f, 0, schedule_f, 0, nsize);

            // 3. resize schedule_c[]
            boolean sctm = schedule_c[idtm];
            assert !schedule_c[idtd] : "try to delete a propagator which is scheduled (coarse)";
            boolean[] _schedule_c = schedule_c;
            schedule_c = new boolean[nsize];
            System.arraycopy(_schedule_c, 0, schedule_c, 0, nsize);

            // 4. remove event_f
            IntCircularQueue icqtm = event_f[idtm];
            assert event_f[idtd].isEmpty() : "try to delete a propagator which has events to propagate (fine)";
            IntCircularQueue[] _event_f = event_f;
            event_f = new IntCircularQueue[nsize];
            System.arraycopy(_event_f, 0, event_f, 0, nsize);


            // 5. remove event_f
            EventType ettm = event_c[idtm];
            assert event_c[idtd] == EventType.VOID : "try to delete a propagator which has events to propagate (coarse)";
            EventType[] _event_c = event_c;
            event_c = new EventType[nsize];
            System.arraycopy(_event_c, 0, event_c, 0, nsize);

            // 6. remove eventmasks
            int[] emtm = eventmasks[idtm];
//            assert eventmasks[idtd]. : "try to delete a propagator which has events to propagate (fine)";
            int[][] _eventmasks = eventmasks;
            eventmasks = new int[nsize][];
            System.arraycopy(_eventmasks, 0, eventmasks, 0, nsize);

            // 6. copy data
            if (idtd < nsize) {
                propagators[idtd] = toMove;
                p2i.set(toMove.getId(), idtd);
                schedule_f[idtd] = sftm;
                schedule_c[idtd] = sctm;
                event_f[idtd] = icqtm;
                event_c[idtd] = ettm;
                eventmasks[idtd] = emtm;
            }
            trigger.remove(toDelete);
        }
    }
}
