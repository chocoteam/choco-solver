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
package solver.propagation.hardcoded.dyn;

import choco.kernel.memory.IEnvironment;
import com.sun.istack.internal.NotNull;
import gnu.trove.set.hash.TIntHashSet;
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
import solver.propagation.queues.DoubleMinHeap;
import solver.recorders.coarse.AbstractCoarseEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Variable-oriented activity based dynamic engine with aging and sampling.
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public class GenerationBasedVarEngine implements IPropagationEngine {


    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects

    protected final Variable[] variables;
    protected final Propagator[] propagators;

    protected final DoubleMinHeap var_heap;
    protected Variable lastVar;
    protected final CircularQueue<Propagator> pro_queue;
    protected Propagator lastProp;

    protected final IId2AbId v2i; // mapping between variable ID and its absolute index
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index

    public static final short minOrmax = 1; // min: 1 ; max : -1

    protected final boolean[] schedule;
    protected final int[][] masks_f;
    protected final int[] masks_c;

    protected final TIntHashSet[] T; // for each variable, the set of "touched" variables
    protected int[] I; // for each variable, the number of touched variables scheduled

    public GenerationBasedVarEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        // 1. Copy the variables
        variables = solver.getVars();

        // 2. Copy the propagators
        Constraint[] constraints = solver.getCstrs();
        List<Propagator> _propagators = new ArrayList();
        int mp = Integer.MAX_VALUE, Mp = Integer.MIN_VALUE;
        for (int c = 0; c < constraints.length; c++) {
            Propagator[] cprops = constraints[c].propagators;
            for (int j = 0; j < cprops.length; j++) {
                _propagators.add(cprops[j]);
                int id = cprops[j].getId();
                mp = Math.min(mp, id);
                Mp = Math.max(Mp, id);
            }
        }
        propagators = _propagators.toArray(new Propagator[_propagators.size()]);

        // 3. Retrieve the range of variable IDs
        int mv = Integer.MAX_VALUE, Mv = Integer.MIN_VALUE;
        for (int i = 0; i < variables.length; i++) {
            int id = variables[i].getId();
            mv = Math.min(mv, id);
            Mv = Math.max(Mv, id);
        }
        // 4a. Map ID and index and prepare to store masks
        v2i = new AId2AbId(mv, Mv, -1);
        masks_f = new int[Mv - mv + 1][];
        for (int j = 0; j < variables.length; j++) {
            v2i.set(variables[j].getId(), j);
            masks_f[j] = new int[variables[j].getPropagators().length];
        }
        T = new TIntHashSet[Mv - mv + 1];
        I = new int[Mv - mv + 1];
        for (int j = 0; j < variables.length; j++) {
            T[j] = new TIntHashSet();
            for (int p = 0; p < variables[j].getPropagators().length; p++) {
                Propagator pp = variables[j].getPropagators()[p];
                for (int i = 0; i < pp.getNbVars(); i++) {
                    if ((pp.getVar(i).getTypeAndKind() & Variable.VAR) != 0) {
                        int vid = pp.getVar(i).getId();
                        T[j].add(vid);
                        assert v2i.get(vid) > -1 : "unknown key ::>" + pp.getVar(i).toString();
                    }
                }
            }
        }


        // 4b. Mapping propagators
        p2i = new AId2AbId(mp, Mp, -1);
        for (int j = 0; j < propagators.length; j++) {
            p2i.set(propagators[j].getId(), j);
        }
        masks_c = new int[Mp - mp + 1];

        // 5. Build the structures
        schedule = new boolean[solver.getNbIdElt()];
        var_heap = new DoubleMinHeap(variables.length / 2 + 1);
        pro_queue = new CircularQueue<Propagator>(propagators.length);
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
        int id, aid, mask;
        try {
            do {
                while (!var_heap.isEmpty()) {
                    lastVar = variables[var_heap.removemin()];
                    id = lastVar.getId();
                    schedule[id] = false;
                    aid = v2i.get(id);
                    Propagator[] vProps = lastVar.getPropagators();
                    int[] idxVinP = lastVar.getPIndices();

                    for (int p = 0; p < vProps.length; p++) {
                        lastProp = vProps[p];
                        mask = masks_f[aid][p];
                        if (mask > 0) {
                            if (Configuration.PRINT_PROPAGATION) {
                                LoggerFactory.getLogger("solver").info("* {}", "<< {F} " + lastVar + "::" + lastProp.toString() + " >>");
                            }
                            masks_f[aid][p] = 0;
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
                    aid = p2i.get(id);
                    mask = masks_c[aid];
                    masks_c[aid] = 0;
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
            } while (!var_heap.isEmpty() || !pro_queue.isEmpty());
        } finally {
        }
    }

    @Override
    public void flush() {
        int id;
        if (lastVar != null) {
            id = lastVar.getId();
            schedule[id] = false;
            int aid = v2i.get(id);
            Arrays.fill(masks_f[aid], 0);
        }
        while (!var_heap.isEmpty()) {
            lastVar = variables[var_heap.removemin()];
            // revision of the variable
            id = lastVar.getId();
            schedule[id] = false;
            int aid = v2i.get(id);
            Arrays.fill(masks_f[aid], 0);
        }
        if (lastProp != null) {
            id = lastProp.getId();
            schedule[id] = false;
            masks_c[p2i.get(id)] = 0;
        }
        while (!pro_queue.isEmpty()) {
            lastProp = pro_queue.pollFirst();
            id = lastProp.getId();
            schedule[id] = false;
            masks_c[p2i.get(id)] = 0;
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        if (Configuration.PRINT_VAR_EVENT) {
            LoggerFactory.getLogger("solver").info("\t>> {} {} => {}", new Object[]{variable, type, cause});
        }
        int id = variable.getId();
        boolean _schedule = false;
        Propagator[] vProps = variable.getPropagators();
        int[] pindices = variable.getPIndices();
        int aid = v2i.get(id);
        for (int p = 0; p < vProps.length; p++) {
            Propagator prop = vProps[p];
            if (cause != prop && prop.isActive()) {
                if (Configuration.PRINT_PROPAGATION)
                    LoggerFactory.getLogger("solver").info("\t|- {}", "<< {F} " + Arrays.toString(prop.getVars()) + "::" + prop.toString() + " >>");
                if (prop.advise(pindices[p], type.mask)) {
                    masks_f[aid][p] |= type.strengthened_mask;
                    _schedule = true;
                }
            }
        }
        if (_schedule) {
            if (!schedule[id]) {
                int c = T[aid].size();
                for (int k : T[aid].toArray()) {
                    int aaid = v2i.get(k);
                    if (schedule[k]) {
                        c--;
                        assert I[aaid] > -1;
                        I[aaid]--;
                        //System.out.println(I[aaid]);
                        var_heap.update(minOrmax * I[aaid], aaid);

                    }
                }
//                assert c > -1 : "negative value";
                I[aid] = c;
                var_heap.insert(minOrmax * I[aid], aid);
                schedule[id] = true;
            }
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
        masks_c[p2i.get(pid)] |= event.getStrengthenedMask();
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
            if (vindices[i] > -1) {// constant has a negative index
                assert variables[i].getPropagators()[vindices[i]] == propagator : propagator.toString() + " >> " + variables[i];
                int vid = v2i.get(variables[i].getId());
                assert vindices[i] < masks_f[vid].length;
                masks_f[vid][vindices[i]] = 0;
            }
        }
        int pid = propagator.getId();
        if (schedule[pid]) {
            schedule[pid] = false;
            masks_c[v2i.get(pid)] = 0;
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
