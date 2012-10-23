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

import choco.kernel.memory.IEnvironment;
import com.sun.istack.internal.NotNull;
import org.slf4j.LoggerFactory;
import solver.Configuration;
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
public class EightQueuesVariableEngine implements IPropagationEngine {
    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;
    protected final Propagator[] propagators;

    protected final CircularQueue<Variable> var_queue;
    protected Variable lastVar;
    protected final boolean[] schedule_v;
    protected final int[][] masks_f;

    protected final CircularQueue<Propagator>[] pro_queue;
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index
    protected Propagator lastProp;
    protected final short[] schedule_in_c;
    protected final int[] masks_c;
    protected final BitSet notEmpty;


    public EightQueuesVariableEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        variables = solver.getVars();
        int maxID = 0;
        for (int i = 0; i < variables.length; i++) {
            if (maxID == 0 || maxID < variables[i].getId()) {
                maxID = variables[i].getId();
            }
        }

        Constraint[] constraints = solver.getCstrs();
        List<Propagator> _propagators = new ArrayList();
        int nbProp = 0;
        int m = Integer.MAX_VALUE, M = Integer.MIN_VALUE;
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

        var_queue = new CircularQueue<Variable>(variables.length / 2);
        pro_queue = new CircularQueue[8];
        for (int i = 0; i < 8; i++) {
            pro_queue[i] = new CircularQueue<Propagator>(16);
        }

        schedule_v = new boolean[maxID + 1];
        schedule_in_c = new short[nbProp];
        masks_f = new int[maxID + 1][];
        for (int i = 0; i < variables.length; i++) {
            masks_f[variables[i].getId()] = new int[variables[i].getPropagators().length];
        }
        masks_c = new int[nbProp];
        notEmpty = new BitSet(8);
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
        int id, mask, aid;
        do {
            while (!var_queue.isEmpty()) {
                lastVar = var_queue.pollFirst();
                // revision of the variable
                id = lastVar.getId();
                schedule_v[id] = false;
                Propagator[] vProps = lastVar.getPropagators();
                int[] idxVinP = lastVar.getPIndices();
                for (int p = 0; p < vProps.length; p++) {
                    lastProp = vProps[p];
                    mask = masks_f[id][p];
                    if (mask > 0) {
                        if (Configuration.PRINT_PROPAGATION) {
                            LoggerFactory.getLogger("solver").info("* {}", "<< {F} " + lastVar + "::" + lastProp.toString() + " >>");
                        }
                        masks_f[id][p] = 0;
                        lastProp.fineERcalls++;
                        lastProp.propagate(idxVinP[p], mask);
                    }
                }
            }
            int i = notEmpty.nextSetBit(0);
            if (i > -1) {
                lastProp = pro_queue[i].pollFirst();
                // revision of the propagator
                aid = p2i.get(lastProp.getId());
                mask = masks_c[aid];
                masks_c[aid] = 0;
                schedule_in_c[aid] = 0;
                if (lastProp.isStateLess()) {
                    lastProp.setActive();
                }
                if (Configuration.PRINT_PROPAGATION) {
                    LoggerFactory.getLogger("solver").info("* {}", "<< ::" + lastProp.toString() + " >>");
                }
                lastProp.coarseERcalls++;
                lastProp.propagate(mask);
                onPropagatorExecution(lastProp);
                if (pro_queue[i].isEmpty()) {
                    notEmpty.clear(i);
                }
            }
        } while (!var_queue.isEmpty() || !notEmpty.isEmpty());

    }

    @Override
    public void flush() {
        int id, aid;
        if (lastVar != null) {
            id = lastVar.getId();
            Arrays.fill(masks_f[id], 0);
            schedule_v[id] = false;
        }
        while (!var_queue.isEmpty()) {
            lastVar = var_queue.pollFirst();
            // revision of the variable
            id = lastVar.getId();
            Arrays.fill(masks_f[id], 0);
            schedule_v[id] = false;
        }
        if (lastProp != null) {
            aid = p2i.get(lastProp.getId());
            schedule_v[aid] = false;
            masks_c[aid] = 0;
        }
        for (int i = notEmpty.nextSetBit(0); i > -1; i = notEmpty.nextSetBit(i + 1)) {
            while (!pro_queue[i].isEmpty()) {
                lastProp = pro_queue[i].pollFirst();
                // revision of the variable
                aid = p2i.get(lastProp.getId());
                masks_c[aid] = 0;
                schedule_in_c[aid] = 0;
            }
            notEmpty.clear(i);
        }
    }

    public void check() {
        for (int i = 0; i < masks_f.length; i++) {
            if (masks_f[i] != null)
                for (int j = 0; j < masks_f[i].length; j++) {
                    assert masks_f[i][j] == 0 : "MASK NOT CLEARED " + variables[0].getSolver().getMeasures().toOneShortLineString();
                }
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        if (Configuration.PRINT_VAR_EVENT) {
            LoggerFactory.getLogger("solver").info("\t>> {} {} => {}", new Object[]{variable, type, cause});
        }
        int vid = variable.getId();
        boolean _schedule = false;
        Propagator[] vProps = variable.getPropagators();
        int[] pindices = variable.getPIndices();
        for (int p = 0; p < vProps.length; p++) {
            Propagator prop = vProps[p];
            if (cause != prop && prop.isActive()) {
                if (Configuration.PRINT_PROPAGATION)
                    LoggerFactory.getLogger("solver").info("\t|- {}", "<< {F} " + Arrays.toString(prop.getVars()) + "::" + prop.toString() + " >>");
                if ((type.mask & prop.getPropagationConditions(pindices[p])) != 0) {
                    masks_f[vid][p] |= type.strengthened_mask;
                    _schedule = true;
                }
            }
        }
        if (!schedule_v[vid] && _schedule) {
            var_queue.addLast(variable);
            schedule_v[vid] = true;
        }

    }

    @Override
    public void schedulePropagator(@NotNull Propagator propagator, EventType event) {
        int pid = propagator.getId();
        int aid = p2i.get(pid);
        if (schedule_in_c[aid] == 0) {
            int priority = propagator.dynPriority();
            pro_queue[priority].addLast(propagator);
            schedule_in_c[aid] = (short) (priority + 1);
            notEmpty.set(priority);
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
        Variable[] variables = propagator.getVars();
        int[] vindices = propagator.getVIndices();
        for (int i = 0; i < variables.length; i++) {
            if (vindices[i] > -1) {// constants and reified propagators have a negative index
                assert variables[i].getPropagators()[vindices[i]] == propagator : propagator.toString() + " >> " + variables[i];
                int vid = variables[i].getId();
                assert vindices[i] < masks_f[vid].length;
                masks_f[vid][vindices[i]] = 0;
            }
        }
        int pid = propagator.getId();
        int aid = p2i.get(pid);
//        if (aid > -1) {
        assert aid > -1 : "try to desactivate an unknown constraint";
        int prio = schedule_in_c[aid];
        if (prio > 0) {  // if in the queue...
            masks_c[aid] = 0;
            schedule_in_c[aid] = 0;
            pro_queue[prio - 1].remove(propagator); // removed from the queue
        }
//        }
    }

    @Override
    public void clear() {
        // void
    }

    ////////////// USELESS ///////////////

    @Override
    public boolean initialized() {
        return true;
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
