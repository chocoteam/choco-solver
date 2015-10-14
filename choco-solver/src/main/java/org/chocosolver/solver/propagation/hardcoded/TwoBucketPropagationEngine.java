/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.propagation.hardcoded;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.propagation.PropagationTrigger;
import org.chocosolver.solver.propagation.hardcoded.util.IId2AbId;
import org.chocosolver.solver.propagation.hardcoded.util.MId2AbId;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.IntCircularQueue;

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
 * <p>
 * Then, managing coarse events (that is, delayed calls to `propagate(int)') is made thanks to 4 additional queues:
 * UNARY, BINARY, TERNARY propagators cannot be delayed!
 * LINEAR propagators are in the top-priority queue,
 * QUADRATIC propagators are in the second-priority queue,
 * CUBIC propagators are in the second-priority queue,
 * VERY_SLOW propagators are in the second-priority queue.
 * <p>
 * The engine empties the first queue, then propagates one event from the following one, and check the first queue again, etc.
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public class TwoBucketPropagationEngine implements IPropagationEngine {

    private static final int WORD_MASK = 0xffffffff;

    protected final Solver solver;
    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected Propagator[] propagators;


    private final short[] match_f;
    private final short[] match_c;

    private short max_f;
    private short max_c;

    protected IId2AbId p2i; // mapping between propagator ID and its absolute index

    protected Propagator lastProp;
    protected int notEmpty; // point out the no empty queues

    protected ArrayDeque<Propagator>[] pro_queue_f;
    protected boolean[] schedule_f; // also maintains the index of the queue!
    protected IntCircularQueue[] event_f;
    protected int[][] eventmasks;// the i^th event mask stores modification events on the i^th variable, since the last propagation

    protected ArrayDeque<Propagator>[] pro_queue_c;
    protected boolean[] schedule_c;
    protected PropagatorEventType[] event_c;

    private boolean init; // is ready to propagate?

    final PropagationTrigger trigger; // an object that starts the propagation
    final Settings.Idem idemStrat;

    public TwoBucketPropagationEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();
        this.trigger = new PropagationTrigger(this, solver);
        this.idemStrat = solver.getSettings().getIdempotencyStrategy();
        this.solver = solver;

        match_f = solver.getSettings().getFineEventPriority();
        match_c = solver.getSettings().getCoarseEventPriority();

    }

    @Override
    public void initialize() {
        if (!init) {
            List<Propagator> _propagators = new ArrayList<>();
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
            event_c = new PropagatorEventType[nbProp];
            Arrays.fill(event_c, PropagatorEventType.VOID);
            init = true;
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
        //assert schedule_f[aid] : "try to propagate an unscheduled propagator";
        schedule_f[aid] = false;
        if (lastProp.reactToFineEvent()) {
            IntCircularQueue evtset = event_f[aid];
            while (!evtset.isEmpty()) {
                int v = evtset.pollFirst();
                assert lastProp.isActive() : "propagator is not active:" + lastProp;
                // clear event
                int mask = eventmasks[aid][v];
                eventmasks[aid][v] = 0;
                // run propagation on the specific event
                lastProp.propagate(v, mask);
            }
        } else if (lastProp.isActive()) { // need to be checked due to views
            //assert lastProp.isActive() : "propagator is not active:" + lastProp;
            lastProp.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
        }
        // This part is for debugging only!!
        if (Settings.Idem.disabled != idemStrat) {
            FakeEngine.checkIdempotency(lastProp);
        }
    }

    private void propagateCoarse(ArrayDeque<Propagator> pro_queue_c) throws ContradictionException {
        lastProp = pro_queue_c.pollFirst();
        // revision of the variable
        int aid = p2i.get(lastProp.getId());
        assert schedule_c[aid] : "try to propagate an unscheduled propagator";
        schedule_c[aid] = false;
        PropagatorEventType evt = event_c[aid];
        event_c[aid] = PropagatorEventType.VOID;
        assert lastProp.isActive() : "propagator is not active:" + lastProp;
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
        event_c[aid] = PropagatorEventType.VOID;
    }

    @Override
    public void onVariableUpdate(Variable variable, IEventType type, ICause cause) {
        EvtScheduler si = variable._schedIter();
        si.init(type);
        while (si.hasNext()) {
            int p = variable.getDindex(si.next());
            int t = variable.getDindex(si.next());
            for (; p < t; p++) {
                Propagator prop = variable.getPropagator(p);
                int pindice = variable.getIndexInPropagator(p);
                if (cause != prop && prop.isActive()) {
                    int aid = p2i.get(prop.getId());
                    if (prop.reactToFineEvent()) {
                        boolean needSched = (eventmasks[aid][pindice] == 0);
                        eventmasks[aid][pindice] |= type.getStrengthenedMask();
                        if (needSched) {
                            //assert !event_f[aid].get(pindice);
                            event_f[aid].addLast(pindice);
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
    }

    @Override
    public void delayedPropagation(Propagator propagator, PropagatorEventType type) throws ContradictionException {
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
        propagators = null;
        p2i = null;
        trigger.clear();
        max_f = 0;
        max_c = 0;
        pro_queue_f = null;
        schedule_f = null;
        pro_queue_c = null;
        schedule_c = null;
        notEmpty = 0;
        event_f = null;
        eventmasks = null;
        event_c = null;
        init = false;
    }

    @Override
    public void dynamicAddition(boolean permanent, Propagator... ps) {
        int osize = propagators.length;
        int nbp = ps.length;
        int nsize = osize + nbp;
        Propagator[] _propagators = propagators;
        propagators = new Propagator[nsize];
        System.arraycopy(_propagators, 0, propagators, 0, osize);
        System.arraycopy(ps, 0, propagators, osize, nbp);
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


        PropagatorEventType[] _event_c = event_c;
        event_c = new PropagatorEventType[nsize];
        System.arraycopy(_event_c, 0, event_c, 0, osize);
        Arrays.fill(event_c, osize, nsize, PropagatorEventType.VOID);

        IntCircularQueue[] _event_f = event_f;
        event_f = new IntCircularQueue[nsize];
        System.arraycopy(_event_f, 0, event_f, 0, osize);

        int[][] _eventmasks = eventmasks;
        eventmasks = new int[nsize][];
        System.arraycopy(_eventmasks, 0, eventmasks, 0, osize);
        for (int i = osize; i < nsize; i++) {
            if (propagators[i].reactToFineEvent()) {
                eventmasks[i] = new int[propagators[i].getNbVars()];
                event_f[i] = new IntCircularQueue(propagators[i].getNbVars());
            }
        }
    }

    @Override
    public void updateInvolvedVariables(Propagator p) {
        if (p.reactToFineEvent()) {
            int i = p2i.get(p.getId());
            assert !schedule_f[i] && schedule_c[i] : "Try to update variable scope during propagation";
            int nbv = p.getNbVars();
            eventmasks[i] = new int[nbv];
            event_f[i] = new IntCircularQueue(nbv);
        }
        propagateOnBacktrack(p);// TODO: when p is not permanent AND a new var is added ... well, one looks for trouble!
    }

    @Override
    public void propagateOnBacktrack(Propagator p) {
        trigger.dynAdd(p, true);
    }

    @Override
    public void dynamicDeletion(Propagator... ps) {
        for (Propagator toDelete : ps) {
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
            assert !toDelete.reactToFineEvent() || event_f[idtd].isEmpty() : "try to delete a propagator which has events to propagate (fine)";
            IntCircularQueue[] _event_f = event_f;
            event_f = new IntCircularQueue[nsize];
            System.arraycopy(_event_f, 0, event_f, 0, nsize);


            // 5. remove event_f
            PropagatorEventType ettm = event_c[idtm];
            assert event_c[idtd] == PropagatorEventType.VOID : "try to delete a propagator which has events to propagate (coarse)";
            PropagatorEventType[] _event_c = event_c;
            event_c = new PropagatorEventType[nsize];
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
