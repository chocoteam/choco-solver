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
import solver.propagation.queues.CircularQueue;
import solver.recorders.coarse.AbstractCoarseEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
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
public class VariableEngine implements IPropagationEngine {
    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;
    protected final Propagator[] propagators;

    protected final CircularQueue<Variable> var_queue;
    protected Variable lastVar;
    protected final CircularQueue<Propagator> pro_queue;
    protected Propagator lastProp;
    protected final boolean[] schedule;
    protected final int[][] masks_f;
    protected final int[] masks_c;


    public VariableEngine(Solver solver) {
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
        for (int c = 0; c < constraints.length; c++) {
            _propagators.addAll(Arrays.asList(constraints[c].propagators));
        }
        propagators = _propagators.toArray(new Propagator[_propagators.size()]);

        var_queue = new CircularQueue<Variable>(variables.length / 2);
        pro_queue = new CircularQueue<Propagator>(propagators.length);

        int size = solver.getNbIdElt();
        schedule = new boolean[size];
        masks_f = new int[maxID + 1][];
        for (int i = 0; i < variables.length; i++) {
            masks_f[variables[i].getId()] = new int[variables[i].getPropagators().length];
        }
        masks_c = new int[size];
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
        int id, mask;
        do {
            while (!var_queue.isEmpty()) {
                lastVar = var_queue.pollFirst();
                // revision of the variable
                id = lastVar.getId();
                schedule[id] = false;
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
            if (!pro_queue.isEmpty()) {
                lastProp = pro_queue.pollFirst();
                id = lastProp.getId();
                // revision of the propagator
                schedule[id] = false;
                mask = masks_c[id];
                masks_c[id] = 0;
                if (lastProp.isStateLess()) {
                    lastProp.setActive();
                }
                if (Configuration.PRINT_PROPAGATION) {
                    LoggerFactory.getLogger("solver").info("* {}", "<< ::" + lastProp.toString() + " >>");
                }
                lastProp.coarseERcalls++;
                lastProp.propagate(mask);
                onPropagatorExecution(lastProp);
            }
        } while (!var_queue.isEmpty() || !pro_queue.isEmpty());

    }

    @Override
    public void flush() {
        int id;
        if (lastVar != null) {
            id = lastVar.getId();
            Arrays.fill(masks_f[id], 0);
            schedule[id] = false;
        }
        while (!var_queue.isEmpty()) {
            lastVar = var_queue.pollFirst();
            // revision of the variable
            id = lastVar.getId();
            Arrays.fill(masks_f[id], 0);
            schedule[id] = false;
        }
        if (lastProp != null) {
            id = lastProp.getId();
            schedule[id] = false;
            masks_c[id] = 0;
        }
        while (!pro_queue.isEmpty()) {
            lastProp = pro_queue.pollFirst();
            id = lastProp.getId();
            schedule[id] = false;
            masks_c[id] = 0;
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
                if (prop.advise(pindices[p], type.mask)) {
                    masks_f[vid][p] |= type.strengthened_mask;
                    _schedule = true;
                }
            }
        }
        if (!schedule[vid] && _schedule) {
            var_queue.addLast(variable);
            schedule[vid] = true;
        }

    }

    @Override
    public void schedulePropagator(@NotNull Propagator propagator, EventType event) {
        int pid = propagator.getId();
        if (!schedule[pid]) {
            if (Configuration.PRINT_PROPAGATION) {
                LoggerFactory.getLogger("solver").info("\t|- {}", "<< ::" + propagator.toString() + " >>");
            }
            pro_queue.addLast(propagator);
            schedule[pid] = true;
        }
        masks_c[pid] |= event.getStrengthenedMask();
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
        assert pid > -1 : "try to desactivate an unknown constraint";
        if (schedule[pid]) {
            schedule[pid] = false;
            masks_c[pid] = 0;
            pro_queue.remove(propagator);
        }
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
