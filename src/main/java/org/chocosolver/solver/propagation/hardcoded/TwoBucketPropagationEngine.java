/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.propagation.hardcoded;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.propagation.PropagationTrigger;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.IntCircularQueue;
import org.chocosolver.util.objects.IntMap;

import java.util.*;

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

    /**
     * Word mask used to check next queue to pop.
     */
    private static final int WORD_MASK = 0xffffffff;

    /**
     * Reference to the model declaring this propagation engine.
     */
    private final Model model;

    /**
     * current number of propagators declared
     */
    private int size;
    /**
     * List of propagators.
     */
    private Propagator[] propagators;

    /**
     * When debugging is required, set this parameter to <tt>true</tt>.
     */
    private final boolean DEBUG;

    /**
     * Fine events priority binding.
     */
    private final short[] match_f;

    /**
     * Coarse events priority binding.
     */
    private final short[] match_c;

    /**
     * Number of active priorities for fine events.
     */
    private short max_f;

    /**
     * Number of active priorities for coarse events.
     */
    private short max_c;

    /**
     * Mapping between propagators' ID and their index in the list of propagators.
     */
    private IntMap p2i;

    /**
     * Reference to the last propagator executed, for flushing purpose.
     */
    private Propagator lastProp;

    /**
     * Indicates which queues are not empty.
     */
    private int notEmpty; // point out the no empty queues

    /**
     * Queue of propagators to execute on fine events.
     */
    private ArrayDeque<Propagator>[] pro_queue_f;

    /**
     * Indicates which propagators are currently scheduled for fine event propagation.
     * More efficient than calling {@code pro_queue_f.contains(p)}.
     */
    private boolean[] schedule_f;

    /**
     * Stores, for each propagator, the index of modified variables until the last propagation.
     */
    private IntCircularQueue[] event_f;

    /**
     * Stores, for each couple (propagator - variable), the fine event to propagate.
     */
    private int[][] eventmasks;

    /**
     * Queue of propagators to execute on coarse events.
     */
    private ArrayDeque<Propagator>[] pro_queue_c;

    /**
     * Indicates which propagators are currently scheduled for coarse event propagation.
     * More efficient than calling {@code pro_queue_c.contains(p)}.
     */
    private boolean[] schedule_c;

    /**
     * Stores, for each propagator, the coarse event to propagate.
     */
    private PropagatorEventType[] event_c;

    /**
     * Set to <tt>true</tt> when this propagation engine is initialized, thus after {@link #initialize()}.
     */
    private boolean init; // is ready to propagate?

    /**
     * A specfic propagation engine which only deals with first propagation of propagators.
     */
    private final PropagationTrigger trigger; // an object that starts the propagation
    /**
     * Creates a two-bucket propagation engine.
     * It propagates all fine events first, wrt their increasing priority, before propagating the smallest priority coarse and propagates all fine events again.
     * Iterates like this until failure or fix-point.
     *
     * @param model the declaring model.
     */
    public TwoBucketPropagationEngine(Model model) {
        this.trigger = new PropagationTrigger(this, model);
        this.model = model;

        match_f = model.getSettings().getFineEventPriority();
        match_c = model.getSettings().getCoarseEventPriority();

        this.DEBUG = model.getSettings().debugPropagation();
    }

    @Override
    public void initialize() throws SolverException{
        if (!init) {
            List<Propagator> _propagators = new ArrayList<>();
            Constraint[] constraints = model.getCstrs();
            for (int c = 0; c < constraints.length; c++) {
                Propagator[] cprops = constraints[c].getPropagators();
                Collections.addAll(_propagators, cprops);
            }
            propagators = _propagators.toArray(new Propagator[_propagators.size()]);
            size = _propagators.size();
            p2i = new IntMap(propagators.length);
            for (int j = 0; j < propagators.length; j++) {
                if(p2i.containsKey(propagators[j].getId())){
                    throw new SolverException("The following propagator " +
                            "is declared more than once into the propagation engine " +
                            "(this happens when a constraint is posted twice " +
                            "or when a posted constraint is also reified.)\n" +
                            propagators[j]+" of "+propagators[j].getConstraint());
                }
                p2i.put(propagators[j].getId(), j);
            }


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

            //noinspection unchecked
            pro_queue_f = new ArrayDeque[max_f];
            for (int i = 0; i < max_f; i++) {
                pro_queue_f[i] = new ArrayDeque<>(propagators.length / 2 + 1);
            }
            schedule_f = new boolean[size];

            //noinspection unchecked
            pro_queue_c = new ArrayDeque[max_c];
            for (int i = 0; i < max_c; i++) {
                pro_queue_c[i] = new ArrayDeque<>(propagators.length / 2 + 1);
            }
            schedule_c = new boolean[size];

            notEmpty = 0;

            event_f = new IntCircularQueue[size];
            eventmasks = new int[size][];
            for (int i = 0; i < size; i++) {
                if (propagators[i].reactToFineEvent()) {
                    int nbv = propagators[i].getNbVars();
                    event_f[i] = new IntCircularQueue(nbv);
                    eventmasks[i] = new int[nbv];
                }
            }
            event_c = new PropagatorEventType[size];
            Arrays.fill(event_c, PropagatorEventType.VOID);
            init = true;
        }
        trigger.addAll(Arrays.copyOfRange(propagators, 0, size));
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
                if (DEBUG) {
                    IPropagationEngine.Trace.printPropagation(lastProp.getVar(v), lastProp);
                }
                // clear event
                int mask = eventmasks[aid][v];
                eventmasks[aid][v] = 0;
                // run propagation on the specific event
                lastProp.propagate(v, mask);
            }
        } else if (lastProp.isActive()) { // need to be checked due to views
            //assert lastProp.isActive() : "propagator is not active:" + lastProp;
            if (DEBUG) {
                IPropagationEngine.Trace.printPropagation(null, lastProp);
            }
            lastProp.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
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
        if (DEBUG) {
            IPropagationEngine.Trace.printPropagation(null, lastProp);
        }
        lastProp.propagate(evt.getMask());
    }


    @Override
    public void flush() {
        if (lastProp != null) {
            flushFine(lastProp);
            flushCoarse(lastProp);
        }
        for (int i = nextNotEmpty(0); i > -1; i = nextNotEmpty(0)) {
            if (i < max_f) { // other finest events, lower priority
                while (!pro_queue_f[i].isEmpty()) {
                    flushFine(pro_queue_f[i].pollLast());
                }
            } else { // coarse events
                while (!pro_queue_c[i - max_f].isEmpty()) {
                    flushCoarse(pro_queue_c[i - max_f].pollLast());
                }
            }
            notEmpty = notEmpty & ~(1 << i);
        }
        lastProp = null;
    }

    private void flushFine(Propagator prop) {
        int aid = p2i.get(prop.getId());
        if (prop.reactToFineEvent()) {
            IntCircularQueue evtset = event_f[aid];
            while (!evtset.isEmpty()) {
                eventmasks[aid][evtset.pollLast()] = 0;
            }
            evtset.clear();
        }
        schedule_f[aid] = false;
    }

    private void flushCoarse(Propagator prop) {
        int aid = p2i.get(prop.getId());
        schedule_c[aid] = false;
        event_c[aid] = PropagatorEventType.VOID;
    }

    @Override
    public void onVariableUpdate(Variable variable, IEventType type, ICause cause) {
        if (DEBUG) {
            IPropagationEngine.Trace.printModification(variable, type, cause);
        }
        Propagator[] vpropagators = variable.getPropagators();
        int[] vindices = variable.getPIndices();
        Propagator prop;
        int pindice;
        EvtScheduler si = variable._schedIter();
        //noinspection unchecked
        si.init(type);
        while (si.hasNext()) {
            int p = variable.getDindex(si.next());
            int t = variable.getDindex(si.next());
            for (; p < t; p++) {
                prop = vpropagators[p];
                pindice = vindices[p];
                if (cause != prop && prop.isActive()) {
                    int aid = p2i.get(prop.getId());
                    if (prop.reactToFineEvent()) {
                        boolean needSched = (eventmasks[aid][pindice] == 0);
                        eventmasks[aid][pindice] |= type.getMask();
                        if (needSched) {
                            //assert !event_f[aid].get(pindice);
                            if (DEBUG) {
                                IPropagationEngine.Trace.printFineSchedule(prop);
                            }
                            event_f[aid].addLast(pindice);
                        }
                    }
                    if (!schedule_f[aid]) {
                        PropagatorPriority prio = prop.getPriority();
                        int q = match_f[prio.priority - 1];
                        pro_queue_f[q].addLast(prop);
                        schedule_f[aid] = true;
                        notEmpty = notEmpty | (1 << q);
                        if (DEBUG) {
                            IPropagationEngine.Trace.printCoarseSchedule(prop);
                        }
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
            pro_queue_c[q].addLast(propagator);
            schedule_c[aid] = true;
            event_c[aid] = type;
            notEmpty = notEmpty | (1 << (q + max_f));
        }
    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {
        flushFine(propagator);
        flushCoarse(propagator);
    }

    @Override
    public void desactivatePropagator(Propagator propagator) {
        flushFine(propagator);
        flushCoarse(propagator);
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
        lastProp = null;
    }

    @Override
    public void dynamicAddition(boolean permanent, Propagator... ps) throws SolverException{
        int osize = size;
        int nbp = ps.length;
        size += nbp;
        boolean resize = (size > propagators.length);
        if(resize){
            int nsize = (size * 3 / 2) + 1;
            Propagator[] _propagators = propagators;
            propagators = new Propagator[nsize];
            System.arraycopy(_propagators, 0, propagators, 0, osize);

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

        }
        System.arraycopy(ps, 0, propagators, osize, nbp);
        for (int i = osize; i < size; i++) {
            if(p2i.containsKey(propagators[i].getId())){
                throw new SolverException("The following propagator " +
                        "is declared more than once into the propagation engine " +
                        "(this happens when a constraint is posted twice " +
                        "or when a posted constraint is also reified.)\n" +
                        propagators[i]+" of "+propagators[i].getConstraint());
            }
            p2i.put(propagators[i].getId(), i);
            trigger.dynAdd(propagators[i], permanent);
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
            assert !schedule_f[i] && !schedule_c[i] : "Try to update variable scope during propagation";
            int nbv = p.getNbVars();
            eventmasks[i] = new int[nbv];
            event_f[i] = new IntCircularQueue(nbv);
        }
        propagateOnBacktrack(p);// TODO: when p is not permanent AND a new var is added ... well, one looks for trouble!
    }

    @Override
    public void propagateOnBacktrack(Propagator p) {
        trigger.propagateOnBacktrack(p);
    }

    @Override
    public void dynamicDeletion(Propagator... ps) {
        for (Propagator toDelete : ps) {
            if(lastProp == toDelete){
                lastProp = null;
            }
            size--;
            // 1. delete toDelete
            Propagator toMove = propagators[size];
            int idtd = p2i.get(toDelete.getId());
            int idtm = p2i.get(toMove.getId());
            p2i.clear(toDelete.getId());

            assert idtd <= idtm : "wrong id for prop to delete";
            // 6. move toMove
            if (idtd < size) {
                propagators[idtd] = toMove;
                p2i.put(toMove.getId(), idtd);
                schedule_f[idtd] = schedule_f[idtm];
                schedule_c[idtd] = schedule_c[idtm];
                event_f[idtd] = event_f[idtm];
                event_c[idtd] = event_c[idtm];
                eventmasks[idtd] = eventmasks[idtm];
            }
            trigger.remove(toDelete);
        }
    }
}
