/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.propagation.hardcoded;

import choco.kernel.memory.IEnvironment;
import com.sun.istack.internal.NotNull;
import gnu.trove.list.array.TIntArrayList;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.IPropagationStrategy;
import solver.propagation.hardcoded.util.AId2AbId;
import solver.propagation.hardcoded.util.IId2AbId;
import solver.propagation.queues.CircularQueue;
import solver.recorders.coarse.AbstractCoarseEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * This engine is priority-driven constraint-oriented seven queues engine.
 * <br/>On a call to {@code onVariableUpdate}, it stores the event generated and schedules the propagator in
 * one of the 7 queues wrt to its priority for future revision.
 * <br/>A propagator can schedule itself on a call to {@code schedulePropagator}.
 * In this case, the propagator is pushed into one of the 7 other queues for delayed propagation.
 * <br/>On a call to {@code propagate} a variable is removed from the queue and a loop over its active propagator is achieved.
 * <br/>The queues of fine-grained events is always emptied before treating one element of the coarse-grained one.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public class SevenQueuesConstraintEngine implements IPropagationEngine {
    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;

    protected static final int O = 8; // coarse offset

    protected final Propagator[] propagators;
    protected final CircularQueue<Propagator>[] pro_queue;
    protected Propagator lastProp;
    protected final short[] schedule_in_f, schedule_in_c;
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index
    protected final BitSet notEmpty;
    protected final int[][] masks_f;
    protected final int[] masks_c;

    public SevenQueuesConstraintEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        variables = solver.getVars();
        List<Propagator> _propagators = new ArrayList();
        Constraint[] constraints = solver.getCstrs();
        int nbProp = 0;
        TIntArrayList nbVars = new TIntArrayList();
        int m = Integer.MAX_VALUE, M = Integer.MIN_VALUE;
        for (int c = 0; c < constraints.length; c++) {
            Propagator[] cprops = constraints[c].propagators;
            for (int j = 0; j < cprops.length; j++, nbProp++) {
                _propagators.add(cprops[j]);
                nbVars.add(cprops[j].getNbVars());
                int id = cprops[j].getId();
                m = Math.min(m, id);
                M = Math.max(M, id);
            }
        }
        propagators = _propagators.toArray(new Propagator[_propagators.size()]);
        p2i = new AId2AbId(m, M, -1);
//        p2i = new MId2AbId(M - m + 1, -1);
        for (int j = 0; j < propagators.length; j++) {
            p2i.set(propagators[j].getId(), j);
        }
        pro_queue = new CircularQueue[16];
        for (int i = 0; i < 16; i++) {
            pro_queue[i] = new CircularQueue<Propagator>(16);
        }

        schedule_in_f = new short[nbProp];
        schedule_in_c = new short[nbProp];
        masks_f = new int[nbProp][];
        for (int i = 0; i < nbProp; i++) {
            masks_f[i] = new int[nbVars.get(i)];
        }
        masks_c = new int[nbProp];
        notEmpty = new BitSet(15);
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
        for (int p = 0; p < propagators.length; p++) {
            schedulePropagator(propagators[p], EventType.FULL_PROPAGATION);
        }
    }

    @SuppressWarnings({"NullableProblems"})
    @Override
    public void propagate() throws ContradictionException {
        int mask, pid, aid;
        for (int i = notEmpty.nextSetBit(0); i > -1; i = notEmpty.nextSetBit(0)) {
            if (i < O) { // fine grained
                while (!pro_queue[i].isEmpty()) {
                    lastProp = pro_queue[i].pollFirst();
                    assert lastProp.isActive() : "propagator is not active:" + lastProp;
                    // revision of the variable
                    pid = lastProp.getId();
                    aid = p2i.get(pid);
                    schedule_in_f[aid] = 0;
                    int nbVars = lastProp.getNbVars();
                    for (int v = 0; v < nbVars; v++) {
                        mask = masks_f[aid][v];
                        if (mask > 0) {
                            masks_f[aid][v] = 0;
                            lastProp.fineERcalls++;
                            lastProp.propagate(null, v, mask);
                        }
                    }
                }
                notEmpty.clear(i);
            } else { // coarse grained
                lastProp = pro_queue[i].pollFirst();
                pid = lastProp.getId();
                // revision of the propagator
                aid = p2i.get(pid);
                mask = masks_c[aid];
                schedule_in_c[aid] = 0;
                masks_c[aid] = 0;
                if (lastProp.isStateLess()) {
                    lastProp.setActive();
                }
                lastProp.coarseERcalls++;
                lastProp.propagate(mask);
                onPropagatorExecution(lastProp);
                if (pro_queue[i].isEmpty()) {
                    notEmpty.clear(i);
                }
            }
        }
    }

    @Override
    public void flush() {
        int aid;
        if (lastProp != null) {
            aid = p2i.get(lastProp.getId());
            Arrays.fill(masks_f[aid], 0);
            schedule_in_f[aid] = 0;
        }
        for (int i = notEmpty.nextSetBit(0); i > -1; i = notEmpty.nextSetBit(i + 1)) {
            while (!pro_queue[i].isEmpty()) {
                lastProp = pro_queue[i].pollFirst();
                // revision of the variable
                aid = p2i.get(lastProp.getId());
                if (i < O) {
                    Arrays.fill(masks_f[aid], 0);
                    schedule_in_f[aid] = 0;
                } else {
                    masks_c[aid] = 0;
                    schedule_in_c[aid] = 0;
                }
            }
            notEmpty.clear();
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        Propagator[] vProps = variable.getPropagators();
        int[] pindices = variable.getPIndices();
        for (int p = 0; p < vProps.length; p++) {
            Propagator prop = vProps[p];
            if (cause != prop && prop.isActive()) {
                int pid = prop.getId();
                if ((type.mask & prop.getPropagationConditions(pindices[p])) != 0) {
                    int aid = p2i.get(pid);
                    masks_f[aid][pindices[p]] |= type.strengthened_mask;
                    if (schedule_in_f[aid] == 0) {
                        int prio = prop.dynPriority();
                        pro_queue[prio].addLast(prop);
                        schedule_in_f[aid] = (short) (prio + 1);
                        notEmpty.set(prio);
                    }
                }
            }
        }

    }

    @Override
    public void schedulePropagator(@NotNull Propagator propagator, EventType event) {
        int pid = propagator.getId();
        int aid = p2i.get(pid);
        if (schedule_in_c[aid] == 0) {
            int priority = propagator.dynPriority();
            pro_queue[O + priority].addLast(propagator);
            schedule_in_c[aid] = (short) (priority + 1);
            notEmpty.set(O + priority);
        }
        masks_c[aid] |= event.getStrengthenedMask();
    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {
        desactivatePropagator(propagator);
    }

    @Override
    public void activatePropagator(Propagator propagator) {
        // void
    }

    @Override
    public void desactivatePropagator(Propagator propagator) {
        int pid = propagator.getId();
        int aid = p2i.get(pid);
        if (aid > -1) {
            Arrays.fill(masks_f[aid], 0); // fill with NO_MASK, outside the loop, to handle propagator currently executed
            int prio = schedule_in_f[aid];
            if (prio > 0) { // if in the queue...
                schedule_in_f[aid] = 0;
                pro_queue[prio - 1].remove(propagator); // removed from the queue
            }
            prio = schedule_in_c[aid];
            if (prio > 0) {  // if in the queue...
                masks_c[aid] = 0;
                schedule_in_c[aid] = 0;
                pro_queue[O + prio - 1].remove(propagator); // removed from the queue
            }
        }
    }

    @Override
    public void clear() {
        // void
    }

    ////////////// USELESS ///////////////

    @Override
    public boolean initialized() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean forceActivation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IPropagationEngine set(IPropagationStrategy propagationStrategy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepareWM(Solver solver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearWatermark(int id1, int id2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMarked(int id1, int id2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addEventRecorder(AbstractFineEventRecorder fer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addEventRecorder(AbstractCoarseEventRecorder er) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void activateFineEventRecorder(AbstractFineEventRecorder fer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void desactivateFineEventRecorder(AbstractFineEventRecorder fer) {
        throw new UnsupportedOperationException();
    }
}
