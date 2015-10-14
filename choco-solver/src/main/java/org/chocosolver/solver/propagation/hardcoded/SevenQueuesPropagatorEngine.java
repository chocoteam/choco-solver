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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.propagation.PropagationTrigger;
import org.chocosolver.solver.propagation.hardcoded.util.IId2AbId;
import org.chocosolver.solver.propagation.hardcoded.util.MId2AbId;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.IntCircularQueue;
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

    private static final int WORD_MASK = 0xffffffff;

    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    private final Solver solver;
    protected Propagator[] propagators;

    protected final CircularQueue<Propagator>[] pro_queue;
    protected Propagator lastProp;
    protected IId2AbId p2i; // mapping between propagator ID and its absolute index
    protected int notEmpty; // point out the no empty queues
    protected short[] scheduled; // also maintains the index of the queue!
    protected IntCircularQueue[] eventsets;
    private boolean init;
    protected int[][] eventmasks;// the i^th event mask stores modification events on the i^th variable, since the last propagation

    final PropagationTrigger trigger; // an object that starts the propagation

    final Settings.Idem idemStrat;


    public SevenQueuesPropagatorEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();
        this.trigger = new PropagationTrigger(this, solver);
        this.idemStrat = solver.getSettings().getIdempotencyStrategy();
        this.solver = solver;
        pro_queue = new CircularQueue[8];

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
            trigger.addAll(propagators);

            //p2i = new AId2AbId(m, M, -1);
            p2i = new MId2AbId(M - m + 1, -1);
            for (int j = 0; j < propagators.length; j++) {
                p2i.set(propagators[j].getId(), j);
            }
            for (int i = 0; i < 8; i++) {
                pro_queue[i] = new CircularQueue<>(16);
            }

            scheduled = new short[nbProp];
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
                        // clear event
                        mask = eventmasks[aid][v];
                        eventmasks[aid][v] = 0;
                        lastProp.decNbPendingEvt();
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
        int aid;
        IntCircularQueue evtset;
        if (lastProp != null) {
            aid = p2i.get(lastProp.getId());
            if (lastProp.reactToFineEvent()) {
                evtset = eventsets[aid];
                while (evtset.size() > 0) {
                    int v = evtset.pollFirst();
                    eventmasks[aid][v] = 0;
                }
                evtset.clear();
                lastProp.flushPendingEvt();
            }
            scheduled[aid] = 0;
        }
        for (int i = nextNotEmpty(0); i > -1; i = nextNotEmpty(i + 1)) {
            while (!pro_queue[i].isEmpty()) {
                lastProp = pro_queue[i].pollFirst();
                // revision of the variable
                aid = p2i.get(lastProp.getId());
                if (lastProp.reactToFineEvent()) {
                    evtset = eventsets[aid];
                    while (evtset.size() > 0) {
                        int v = evtset.pollFirst();
                        eventmasks[aid][v] = 0;
                    }
                    evtset.clear();
                    lastProp.flushPendingEvt();
                }
                scheduled[aid] = 0;
            }
            notEmpty = notEmpty & ~(1 << i);
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, IEventType type, ICause cause) {
        Propagator[] vpropagators = variable.getPropagators();
        int[] vindices = variable.getPIndices();
        Propagator prop;
        int pindice;
        EvtScheduler si = variable._schedIter();
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
                        eventmasks[aid][pindice] |= type.getStrengthenedMask();
                        if (needSched) {
                            prop.incNbPendingEvt();
                            eventsets[aid].addLast(pindice);
                        }
                    }
                    if (scheduled[aid] == 0) {
                        int prio = /*dynamic ? prop.dynPriority() :*/ prop.getPriority().priority;
                        pro_queue[prio].addLast(prop);
                        scheduled[aid] = (short) (prio + 1);
//                    notEmpty.set(prio);
                        notEmpty = notEmpty | (1 << prio);
                    }
                }
            }
        }
    }

    @Override
    public void delayedPropagation(Propagator propagator, PropagatorEventType type) throws ContradictionException {
        if (propagator.getNbPendingEvt() == 0) {
            propagator.propagate(type.getStrengthenedMask());
        }
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
                propagator.flushPendingEvt();
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

        short[] _scheduled = scheduled;
        scheduled = new short[nsize];
        System.arraycopy(_scheduled, 0, scheduled, 0, osize);


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


            // 3. remove eventsets
            IntCircularQueue estm = eventsets[idtm];
            assert !toDelete.reactToFineEvent() || eventsets[idtd].isEmpty() : "try to delete a propagator which has events to propagate (fine)";
            IntCircularQueue[] _eventsets = eventsets;
            eventsets = new IntCircularQueue[nsize];
            System.arraycopy(_eventsets, 0, eventsets, 0, nsize);

            // 4. remove eventmasks
            int[] emtm = eventmasks[idtm];
//            assert eventmasks[idtd]. : "try to delete a propagator which has events to propagate (fine)";
            int[][] _eventmasks = eventmasks;
            eventmasks = new int[nsize][];
            System.arraycopy(_eventmasks, 0, eventmasks, 0, nsize);

            // 4. copy data
            if (idtd < nsize) {
                propagators[idtd] = toMove;
                p2i.set(toMove.getId(), idtd);
                scheduled[idtd] = stm;
                eventsets[idtd] = estm;
                eventmasks[idtd] = emtm;
            }
            trigger.remove(toDelete);
        }
    }
}
