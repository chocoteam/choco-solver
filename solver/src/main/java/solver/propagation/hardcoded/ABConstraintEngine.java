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
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
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
import solver.propagation.queues.DoubleMinHeap;
import solver.recorders.coarse.AbstractCoarseEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This engine is constraint-oriented one, based on activity on constraint.
 * <br/>On a call to {@code onVariableUpdate}, it stores the event generated and schedules in a queue the propagators touched for future revision.
 * <br/>A propagator can schedule itself on a call to {@code schedulePropagator}, in this case, the propagator is pushed into
 * second queue for delayed propagation.
 * <br/>On a call to {@code propagate} a propagator is removed from the queue and propagated.
 * <br/>The queue of propagators for fine-grained events is always emptied before treating one element of the coarse-grained one.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public class ABConstraintEngine implements IPropagationEngine {
    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;
    protected final Propagator[] propagators;

    protected static final int F = 1, C = 2;

    protected double[] w;
    protected final double g = .999F;


    protected final DoubleMinHeap pro_queue_f;
    protected Propagator lastProp;
    protected final DoubleMinHeap pro_queue_c;
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index
    protected final short[] schedule;
    protected final int[][] masks_f;
    protected final int[] masks_c;


    public ABConstraintEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        variables = solver.getVars();
        List<Propagator> _propagators = new ArrayList();
        Constraint[] constraints = solver.getCstrs();
        int nbProp = 0;
        TIntList nbVars = new TIntArrayList();
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
        pro_queue_f = new DoubleMinHeap(nbProp / 10 + 1);
        pro_queue_c = new DoubleMinHeap(nbProp + 1);

        schedule = new short[nbProp];
        masks_f = new int[nbProp][];
        for (int i = 0; i < nbProp; i++) {
            masks_f[i] = new int[nbVars.get(i)];
        }
        masks_c = new int[nbProp];
        w = new double[nbProp];
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
        for (int i = 0; i < w.length; i++) {
            w[i] *= g;
        }
        int aid, mask;
        do {
            while (!pro_queue_f.isEmpty()) {
                lastProp = propagators[pro_queue_f.removemin()];
                assert lastProp.isActive() : "propagator is not active";
                // revision of the variable
                aid = p2i.get(lastProp.getId());
                schedule[aid] ^= F;
                int nbVars = lastProp.getNbVars();
                for (int v = 0; v < nbVars; v++) {
                    mask = masks_f[aid][v];
                    if (mask > 0) {
                        if (Configuration.PRINT_PROPAGATION) {
                            LoggerFactory.getLogger("solver").info("* {}", "<< {F} " + Arrays.toString(lastProp.getVars()) + "::" + lastProp.toString() + " >>");
                        }
                        masks_f[aid][v] = 0;
                        lastProp.fineERcalls++;
                        lastProp.propagate(v, mask);
                    }
                }
                w[aid] += 1;
            }
            if (!pro_queue_c.isEmpty()) {
                lastProp = propagators[pro_queue_c.removemin()];
                // revision of the propagator
                aid = p2i.get(lastProp.getId());
                schedule[aid] ^= C;
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
                w[aid] += 1;
            }
        } while (!pro_queue_f.isEmpty() || !pro_queue_c.isEmpty());

    }

    @Override
    public void flush() {
        int aid;
        if (lastProp != null) {
            aid = p2i.get(lastProp.getId());
            Arrays.fill(masks_f[aid], 0);
            schedule[aid] = 0;
            masks_c[aid] = 0;
        }
        while (!pro_queue_f.isEmpty()) {
            lastProp = propagators[pro_queue_f.removemin()];
            // revision of the variable
            aid = p2i.get(lastProp.getId());
            Arrays.fill(masks_f[aid], 0);
            schedule[aid] = 0;
        }
        while (!pro_queue_c.isEmpty()) {
            lastProp = propagators[pro_queue_c.removemin()];
            // revision of the variable
            aid = p2i.get(lastProp.getId());
            schedule[aid] = 0;
            masks_c[aid] = 0;
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        if (Configuration.PRINT_VAR_EVENT) {
            LoggerFactory.getLogger("solver").info("\t>> {} {} => {}", new Object[]{variable, type, cause});
        }
        Propagator[] vProps = variable.getPropagators();
        int[] pindices = variable.getPIndices();
        for (int p = 0; p < vProps.length; p++) {
            Propagator prop = vProps[p];
            if (cause != prop && prop.isActive()) {
                if (Configuration.PRINT_PROPAGATION)
                    LoggerFactory.getLogger("solver").info("\t|- {}", "<< {F} " + Arrays.toString(prop.getVars()) + "::" + prop.toString() + " >>");
                if (prop.advise(pindices[p], type.mask)) {
                    int aid = p2i.get(prop.getId());
                    masks_f[aid][pindices[p]] |= type.strengthened_mask;
                    if ((schedule[aid] & F) == 0) {
                        double _w = w[aid];
                        pro_queue_f.insert(_w, aid);
                        schedule[aid] |= F;
                    }
                }
            }
        }

    }

    @Override
    public void schedulePropagator(@NotNull Propagator propagator, EventType event) {
        int aid = p2i.get(propagator.getId());
        if ((schedule[aid] & C) == 0) {
            if (Configuration.PRINT_PROPAGATION) {
                LoggerFactory.getLogger("solver").info("\t|- {}", "<< ::" + propagator.toString() + " >>");
            }
            double _w = w[aid];
            pro_queue_c.insert(_w, aid);
            schedule[aid] |= C;
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
//        if (aid != -1) {
        assert aid > -1 : "try to desactivate an unknown constraint";
        Arrays.fill(masks_f[aid], 0);
        if ((schedule[aid] & F) != 0) {
            schedule[aid] ^= F;
            pro_queue_f.remove(aid);
        }
        if ((schedule[aid] & C) != 0) {
            masks_c[aid] = 0;
            schedule[aid] ^= C;
            pro_queue_c.remove(aid);
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
