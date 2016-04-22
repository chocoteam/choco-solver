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

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
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
     * For debugging purpose: set to <tt>true</tt> to use color on console when debugging
     */
    private final boolean COLOR;
    /**
     * The strategy to use for idempotency (for debugging purpose)
     */
    private final Settings.Idem idemStrat;

    /**
     * Internal unique contradiction exception, used on propagation failures
     */
    private final ContradictionException exception;
    /**
     * The model declaring this engine
     */
    private final Model model;
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
    private IntMap p2i; //
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
     * Set to <tt>true</tt> once {@link #initialize()} has been called.
     */
    private boolean init;
    /**
     * Per propagator (i) and per variable of the propagator (j): modification event mask of variable j from propagator i
     * since the last propagation of propagator j.
     */
    private int[][] eventmasks;
    /**
     * Per propagator: counter of events to be propagated
     */
    private int[] pendingEvt;

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
        this.exception = new ContradictionException();
        this.trigger = new PropagationTrigger(this, model);
        this.idemStrat = model.getSettings().getIdempotencyStrategy();
        this.model = model;
        //noinspection unchecked
        this.pro_queue = new CircularQueue[8];
        this.DEBUG = model.getSettings().debugPropagation();
        this.COLOR = model.getSettings().outputWithANSIColors();

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
    public void initialize() throws SolverException{
        if (!init) {
            List<Propagator> _propagators = new ArrayList<>();
            Constraint[] constraints = model.getCstrs();
            int nbProp = 0;
            for (int c = 0; c < constraints.length; c++) {
                Propagator[] cprops = constraints[c].getPropagators();
                for (int j = 0; j < cprops.length; j++, nbProp++) {
                    _propagators.add(cprops[j]);
                }
            }
            propagators = _propagators.toArray(new Propagator[_propagators.size()]);

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
            for (int i = 0; i < 8; i++) {
                pro_queue[i] = new CircularQueue<>(16);
            }

            scheduled = new short[nbProp];
            pendingEvt = new int[nbProp];
            eventsets = new IntCircularQueue[nbProp];
            eventmasks = new int[nbProp][];
            for (int i = 0; i < nbProp; i++) {
                int nbv = propagators[i].getNbVars();
                if (propagators[i].reactToFineEvent()) {
                    eventsets[i] = new IntCircularQueue(nbv);
                    eventmasks[i] = new int[nbv];
                }
            }
            notEmpty = 0;
            init = true;
        }
        trigger.addAll(propagators);
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
                if (lastProp.reactToFineEvent()) {
                    evtset = eventsets[aid];
                    while (evtset.size() > 0) {
                        int v = evtset.pollFirst();
                        assert lastProp.isActive() : "propagator is not active:" + lastProp;
                        if (DEBUG) {
                            IPropagationEngine.Trace.printPropagation(lastProp.getVar(v), lastProp, COLOR);
                        }
                        // clear event
                        mask = eventmasks[aid][v];
                        eventmasks[aid][v] = 0;
                        assert (pendingEvt[aid] > 0) : "number of enqueued records is <= 0 " + this;
                        pendingEvt[aid]--;
                        // run propagation on the specific event
                        lastProp.propagate(v, mask);
                    }
                    // now we can check whether a delayed propagation has been scheduled
                    if(scheduled[aid]>0){
                        if (DEBUG) {
                            IPropagationEngine.Trace.printPropagation(null, lastProp, COLOR);
                        }
                        mask = scheduled[aid];
                        scheduled[aid] = 0;
                        lastProp.propagate(mask);
                    }
                } else if (lastProp.isActive()) { // need to be checked due to views
                    //assert lastProp.isActive() : "propagator is not active:" + lastProp;
                    if (DEBUG) {
                        IPropagationEngine.Trace.printPropagation(null, lastProp, COLOR);
                    }
                    lastProp.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
                }
                // This part is for debugging only!!
                if (Settings.Idem.disabled != idemStrat) {
                    FakeEngine.checkIdempotency(lastProp);
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
            flush(p2i.get(lastProp.getId()));
        }
        for (int i = nextNotEmpty(0); i > -1; i = nextNotEmpty(i + 1)) {
            while (!pro_queue[i].isEmpty()) {
                lastProp = pro_queue[i].pollFirst();
                // revision of the variable
                flush(p2i.get(lastProp.getId()));
            }
            notEmpty = notEmpty & ~(1 << i);
        }
    }

    private void flush(int aid) {
        IntCircularQueue evtset;
        if (lastProp.reactToFineEvent()) {
            evtset = eventsets[aid];
            while (evtset.size() > 0) {
                int v = evtset.pollFirst();
                eventmasks[aid][v] = 0;
            }
            evtset.clear();
            pendingEvt[aid] = 0;
        }
        scheduled[aid] = 0;
    }

    @Override
    public void onVariableUpdate(Variable variable, IEventType type, ICause cause) {
        if (DEBUG) {
            IPropagationEngine.Trace.printModification(variable, type, cause, COLOR);
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
                if (prop.isActive() && cause != prop) {
                    int aid = p2i.get(prop.getId());
                    if (prop.reactToFineEvent()) {
                        boolean needSched = (eventmasks[aid][pindice] == 0);
                        eventmasks[aid][pindice] |= type.getMask();
                        if (needSched) {
                            if (DEBUG) {
                                IPropagationEngine.Trace.printFineSchedule(prop, COLOR);
                            }
                            assert (pendingEvt[aid] >= 0) : "number of enqueued records is < 0 " + this;
                            pendingEvt[aid]++;
                            eventsets[aid].addLast(pindice);
                        }
                    }
                    if (scheduled[aid] == 0) {
                        int prio = /*dynamic ? prop.dynPriority() :*/ prop.getPriority().priority;
                        pro_queue[prio].addLast(prop);
                        scheduled[aid] = (short) (prio + 1);
//                    notEmpty.set(prio);
                        notEmpty = notEmpty | (1 << prio);
                        if (DEBUG) {
                            IPropagationEngine.Trace.printCoarseSchedule(prop, COLOR);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void delayedPropagation(Propagator propagator, PropagatorEventType type) throws ContradictionException {
        assert scheduled[p2i.get(propagator.getId())] == 0 || scheduled[p2i.get(propagator.getId())] == type.getMask();
        scheduled[p2i.get(propagator.getId())] = (short) type.getMask();
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
                pendingEvt[aid] = 0;
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
    }

    @Override
    public void dynamicAddition(boolean permanent, Propagator... ps) throws SolverException{
        int osize = propagators.length;
        int nbp = ps.length;
        int nsize = osize + nbp;
        Propagator[] _propagators = propagators;
        propagators = new Propagator[nsize];
        System.arraycopy(_propagators, 0, propagators, 0, osize);
        System.arraycopy(ps, 0, propagators, osize, nbp);
        for (int j = osize; j < nsize; j++) {
            if(p2i.containsKey(propagators[j].getId())){
                throw new SolverException("The following propagator " +
                        "is declared more than once into the propagation engine " +
                        "(this happens when a constraint is posted twice " +
                        "or when a posted constraint is also reified.)\n" +
                        propagators[j]+" of "+propagators[j].getConstraint());
            }
            p2i.put(propagators[j].getId(), j);
            trigger.dynAdd(propagators[j], permanent);
        }

        short[] _scheduled = scheduled;
        scheduled = new short[nsize];
        System.arraycopy(_scheduled, 0, scheduled, 0, osize);


        int[] _pendingEvt = pendingEvt;
        pendingEvt = new int[nsize];
        System.arraycopy(_pendingEvt, 0, pendingEvt, 0, osize);

        IntCircularQueue[] _eventsets = eventsets;
        eventsets = new IntCircularQueue[nsize];
        System.arraycopy(_eventsets, 0, eventsets, 0, osize);

        int[][] _eventmasks = eventmasks;
        eventmasks = new int[nsize][];
        System.arraycopy(_eventmasks, 0, eventmasks, 0, osize);
        for (int i = osize; i < nsize; i++) {
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
        trigger.dynAdd(p, true);
    }

    @Override
    public void dynamicDeletion(Propagator... ps) {
        for (Propagator toDelete : ps) {
            int nsize = propagators.length - 1;
            Propagator toMove = propagators[nsize];
            int idtd = p2i.get(toDelete.getId());
            int idtm = p2i.get(toMove.getId());
            p2i.clear(toDelete.getId());

            assert idtd <= idtm : "wrong id for prop to delete";

            // 1. remove from propagators[] and p2i
            Propagator[] _propagators = propagators;
            propagators = new Propagator[nsize];
            System.arraycopy(_propagators, 0, propagators, 0, nsize);

            // 2. resize scheduled
            short stm = scheduled[idtm];
            assert scheduled[idtd] == 0 : "try to delete a propagator which is scheduled (fine)";
            short[] _scheduled = scheduled;
            scheduled = new short[nsize];
            System.arraycopy(_scheduled, 0, scheduled, 0, nsize);


            // 3. resize scheduled
            int ptm = pendingEvt[idtm];
            assert pendingEvt[idtd] == 0 : "try to delete a propagator which is scheduled (fine)";
            int[] _pendingEvt = pendingEvt;
            pendingEvt = new int[nsize];
            System.arraycopy(_pendingEvt, 0, pendingEvt, 0, nsize);

            // 4. remove eventsets
            IntCircularQueue estm = eventsets[idtm];
            assert !toDelete.reactToFineEvent() || eventsets[idtd].isEmpty() : "try to delete a propagator which has events to propagate (fine)";
            IntCircularQueue[] _eventsets = eventsets;
            eventsets = new IntCircularQueue[nsize];
            System.arraycopy(_eventsets, 0, eventsets, 0, nsize);

            // 5. remove eventmasks
            int[] emtm = eventmasks[idtm];
//            assert eventmasks[idtd]. : "try to delete a propagator which has events to propagate (fine)";
            int[][] _eventmasks = eventmasks;
            eventmasks = new int[nsize][];
            System.arraycopy(_eventmasks, 0, eventmasks, 0, nsize);

            // 6. copy data
            if (idtd < nsize) {
                propagators[idtd] = toMove;
                p2i.put(toMove.getId(), idtd);
                scheduled[idtd] = stm;
                pendingEvt[idtd] = ptm;
                eventsets[idtd] = estm;
                eventmasks[idtd] = emtm;
            }
            trigger.remove(toDelete);
        }
    }
}
