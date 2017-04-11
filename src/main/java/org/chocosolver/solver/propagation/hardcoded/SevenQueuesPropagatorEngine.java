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
import org.chocosolver.util.objects.queues.CircularQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This engine is priority-driven constraint-oriented seven queues engine.
 * <br/>On a call to {@code onVariableUpdate}, it stores the event generated and schedules the propagator in
 * one of the 7 queues wrt to its priority for future revision.
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public class SevenQueuesPropagatorEngine implements IPropagationEngine {

    /**
     * Mask to deal with emptiness (see {@link #notEmpty})
     */
    private static final int WORD_MASK = 0xffffffff;
    /**
     * For debugging purpose: set to <tt>true</tt> to output debugging information
     */
    private final boolean DEBUG;
    /**
     * The model declaring this engine
     */
    private final Model model;
    /**
     * current number of propagators declared
     */
    private int size;
    /**
     * The array of propagators to execute
     */
    private Propagator[] propagators;
    /**
     * The main structure of this engine: seven circular queues,
     * each of them is dedicated to store propagator to execute wrt their priority.
     */
    private final CircularQueue<Propagator>[] pro_queue;
    /**
     * The last propagator executed
     */
    private Propagator lastProp;
    /**
     * Mapping between propagator ID and its absolute index
     */
    private IntMap p2i;
    /**
     * One bit per queue: true if the queue is not empty.
     */
    private int notEmpty;
    /**
     * Per propagator: indicates whether it is scheduled (and in which queue) or not.
     */
    private short[] scheduled;
    /**
     * Per propagator: set of (variable) events to propagate
     */
    private IntCircularQueue[] eventsets;
    /**
     * PropagatorEventType's mask for delayed propagation
     */
    private int delayedPropagationType;
    /**
     * Set to <tt>true</tt> once {@link #initialize()} has been called.
     */
    private boolean init;
    /**
     * Per propagator (i) and per variable of the propagator (j): modification event mask of variable j from propagator i
     * since the last propagation of propagator j.
     */
    private int[][] eventmasks;

    /**
     * A specific object to deal with first propagation
     */
    private final PropagationTrigger trigger; // an object that starts the propagation


    /**
     * A seven-queue propagation engine.
     * Each of the seven queues deals with on priority.
     * When a propagator needs to be executed, it is scheduled in the queue corresponding to its priority.
     * The lowest priority queue is emptied before one element of the second lowest queue is popped, etc.
     * @param model the declaring model
     */
    public SevenQueuesPropagatorEngine(Model model) {
        this.trigger = new PropagationTrigger(this, model);
        this.model = model;
        //noinspection unchecked
        this.pro_queue = new CircularQueue[8];
        this.DEBUG = model.getSettings().debugPropagation();

    }

    @Override
    public void initialize() throws SolverException {
        if (!init) {
            List<Propagator> _propagators = new ArrayList<>();
            Constraint[] constraints = model.getCstrs();
            for (int c = 0; c < constraints.length; c++) {
                Propagator[] cprops = constraints[c].getPropagators();
                Collections.addAll(_propagators, cprops);
            }
            propagators = _propagators.toArray(new Propagator[_propagators.size()]);
            size = _propagators.size();
            p2i = new IntMap(size);
            for (int j = 0; j < size; j++) {
                if (p2i.containsKey(propagators[j].getId())) {
                    throw new SolverException("The following propagator " +
                            "is declared more than once into the propagation engine " +
                            "(this happens when a constraint is posted twice " +
                            "or when a posted constraint is also reified.)\n" +
                            propagators[j] + " of " + propagators[j].getConstraint());
                }
                p2i.put(propagators[j].getId(), j);
            }
            for (int i = 0; i < 8; i++) {
                pro_queue[i] = new CircularQueue<>(16);
            }

            scheduled = new short[size];
            eventsets = new IntCircularQueue[size];
            eventmasks = new int[size][];
            for (int i = 0; i < size; i++) {
                int nbv = propagators[i].getNbVars();
                if (propagators[i].reactToFineEvent()) {
                    eventsets[i] = new IntCircularQueue(nbv);
                    eventmasks[i] = new int[nbv];
                }
            }
            notEmpty = 0;
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
        int mask, aid;
        IntCircularQueue evtset;
        if (trigger.needToRun()) {
            trigger.propagate();
        }
        for (int i = nextNotEmpty(0); i > -1; i = nextNotEmpty(0)) {
            while (!pro_queue[i].isEmpty()) {
                lastProp = pro_queue[i].pollFirst();
                // revision of the variable
                aid = p2i.get(lastProp.getId());
                scheduled[aid] = 0;
                delayedPropagationType = 0;
                if (lastProp.reactToFineEvent()) {
                    evtset = eventsets[aid];
                    while (evtset.size() > 0) {
                        int v = evtset.pollFirst();
                        assert lastProp.isActive() : "propagator is not active:" + lastProp;
                        if (DEBUG) {
                            IPropagationEngine.Trace.printPropagation(lastProp.getVar(v), lastProp);
                        }
                        // clear event
                        mask = eventmasks[aid][v];
                        eventmasks[aid][v] = 0;
                        // run propagation on the specific event
                        lastProp.propagate(v, mask);
                    }
                    // now we can check whether a delayed propagation has been scheduled
                    if (delayedPropagationType > 0) {
                        if (DEBUG) {
                            IPropagationEngine.Trace.printPropagation(null, lastProp);
                        }
                        lastProp.propagate(delayedPropagationType);
                    }
                } else if (lastProp.isActive()) { // need to be checked due to views
                    //assert lastProp.isActive() : "propagator is not active:" + lastProp;
                    if (DEBUG) {
                        IPropagationEngine.Trace.printPropagation(null, lastProp);
                    }
                    lastProp.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
                }
            }
            notEmpty = notEmpty & ~(1 << i);
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

    @Override
    public void flush() {
        if (lastProp != null) {
            flush(lastProp);
        }
        for (int i = nextNotEmpty(0); i > -1; i = nextNotEmpty(i + 1)) {
            while (!pro_queue[i].isEmpty()) {
                // revision of the variable
                flush(pro_queue[i].pollLast());
            }
            notEmpty = notEmpty & ~(1 << i);
        }
        lastProp = null;
    }

    private void flush(Propagator prop) {
        IntCircularQueue evtset;
        int aid = p2i.get(prop.getId());
        assert aid > -1: "cannot flush unknown propagator";
        if (prop.reactToFineEvent()) {
            evtset = eventsets[aid];
            while (evtset.size() > 0) {
                int v = evtset.pollLast();
                eventmasks[aid][v] = 0;
            }
            evtset.clear();
        }
        scheduled[aid] = 0;
    }

    @Override
    public void onVariableUpdate(Variable variable, IEventType type, ICause cause) {
        if (DEBUG) {
            IPropagationEngine.Trace.printModification(variable, type, cause);
        }
        Propagator[] vpropagators = variable.getPropagators();
        int[] vindices = variable.getPIndices();
        Propagator prop;
        int mask = type.getMask();
        EvtScheduler si = variable._schedIter();
        //noinspection unchecked
        si.init(type);
        while (si.hasNext()) {
            int p = variable.getDindex(si.next());
            int t = variable.getDindex(si.next());
            for (; p < t; p++) {
                prop = vpropagators[p];
                if (prop.isActive() && cause != prop) {
                    schedule(prop, vindices[p], mask);
                }
            }
        }
    }

    private void schedule(Propagator prop, int pindice, int mask) {
        int aid = p2i.get(prop.getId());
        if (prop.reactToFineEvent()) {
            if (eventmasks[aid][pindice] == 0) {
                if (DEBUG) {
                    IPropagationEngine.Trace.printFineSchedule(prop);
                }
                eventsets[aid].addLast(pindice);
            }
            eventmasks[aid][pindice] |= mask;
        }
        if (scheduled[aid] == 0) {
            int prio = prop.getPriority().priority;
            pro_queue[prio].addLast(prop);
            scheduled[aid] = (short) (prio + 1);
            notEmpty = notEmpty | (1 << prio);
            if (DEBUG) {
                IPropagationEngine.Trace.printCoarseSchedule(prop);
            }
        }
    }


    @Override
    public void delayedPropagation(Propagator propagator, PropagatorEventType type) throws ContradictionException {
        assert propagator == lastProp;
        assert delayedPropagationType == 0 || delayedPropagationType == type.getMask();
        delayedPropagationType = type.getMask();
    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {
        desactivatePropagator(propagator);
    }

    @Override
    public void desactivatePropagator(Propagator propagator) {
        if (propagator.reactToFineEvent()) {
            int pid = propagator.getId();
            int aid = p2i.get(pid);
            if (aid > -1) {
                assert aid > -1 : "try to desactivate an unknown constraint";
                // we don't remove the element from its master to avoid costly operations
                IntCircularQueue evtset = eventsets[aid];
                while (evtset.size() > 0) {
                    int v = evtset.pollFirst();
                    eventmasks[aid][v] = 0;
                }
                evtset.clear();
            }
        }
    }

    @Override
    public void clear() {
        propagators = null;
        trigger.clear();
        p2i = null;
        for (int i = 0; i < 8; i++) {
            pro_queue[i] = null;
        }
        scheduled = null;
        eventsets = null;
        eventmasks = null;
        notEmpty = 0;
        init = false;
        lastProp = null;
    }

    @Override
    public void dynamicAddition(boolean permanent, Propagator... ps) throws SolverException {
        int osize = size;
        int nbp = ps.length;
        size += nbp;
        boolean resize = (size > propagators.length);
        if(resize) {
            int nsize = (size * 3 / 2) + 1;
            Propagator[] _propagators = propagators;
            propagators = new Propagator[nsize];
            System.arraycopy(_propagators, 0, propagators, 0, osize);

            short[] _scheduled = scheduled;
            scheduled = new short[nsize];
            System.arraycopy(_scheduled, 0, scheduled, 0, osize);

            IntCircularQueue[] _eventsets = eventsets;
            eventsets = new IntCircularQueue[nsize];
            System.arraycopy(_eventsets, 0, eventsets, 0, osize);

            int[][] _eventmasks = eventmasks;
            eventmasks = new int[nsize][];
            System.arraycopy(_eventmasks, 0, eventmasks, 0, osize);
        }
        System.arraycopy(ps, 0, propagators, osize, nbp);
        for (int i = osize; i < size; i++) {
            if (p2i.containsKey(propagators[i].getId())) {
                throw new SolverException("The following propagator " +
                        "is declared more than once into the propagation engine " +
                        "(this happens when a constraint is posted twice " +
                        "or when a posted constraint is also reified.)\n" +
                        propagators[i] + " of " + propagators[i].getConstraint());
            }
            p2i.put(propagators[i].getId(), i);
            trigger.dynAdd(propagators[i], permanent);
            if (propagators[i].reactToFineEvent()) {
                int nbv = propagators[i].getNbVars();
                eventsets[i] = new IntCircularQueue(nbv);
                eventmasks[i] = new int[nbv];
            }
        }
    }

    @Override
    public void updateInvolvedVariables(Propagator p) {
        if (p.reactToFineEvent()) {
            int i = p2i.get(p.getId());
            assert scheduled[i] == 0 : "Try to update variable scope during propagation";
            int nbv = p.getNbVars();
            eventsets[i] = new IntCircularQueue(nbv);
            eventmasks[i] = new int[nbv];
        }
        propagateOnBacktrack(p); // TODO: when p is not permanent AND a new var is added ... well, one looks for trouble!
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
            propagators[size] = null;
            int idtd = p2i.get(toDelete.getId());
            int idtm = p2i.get(toMove.getId());
            p2i.clear(toDelete.getId());

            assert idtd <= idtm : "wrong id for prop to delete";
            // 2. move toMove
            if (idtd < size) {
                propagators[idtd] = toMove;
                p2i.put(toMove.getId(), idtd);
                scheduled[idtd] = scheduled[idtm];
                assert !toDelete.reactToFineEvent() || eventsets[idtd].isEmpty() : "try to delete a propagator which has events to propagate (fine)";
                eventsets[idtd] = eventsets[idtm];
                eventmasks[idtd] = eventmasks[idtm];
            }
            trigger.remove(toDelete);
        }
    }
}
