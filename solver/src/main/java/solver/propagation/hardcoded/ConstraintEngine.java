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
import org.slf4j.LoggerFactory;
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
import solver.recorders.IEventRecorder;
import solver.recorders.coarse.AbstractCoarseEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This engine is constraint-oriented one.
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
public class ConstraintEngine implements IPropagationEngine {
    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;
    protected final Propagator[] propagators;

    protected static final int F = 1, C = 2;

    protected final CircularQueue<Propagator> pro_queue_f;
    protected Propagator lastProp;
    protected final CircularQueue<Propagator> pro_queue_c;
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index
    protected final short[] schedule;
    protected final int[][] masks_f;
    protected final int[] masks_c;


    public ConstraintEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        variables = solver.getVars();
        List<Propagator> _propagators = new ArrayList();
        TIntArrayList nbVars = new TIntArrayList();
        Constraint[] constraints = solver.getCstrs();
        int nbProp = 0;
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
        for (int j = 0; j < propagators.length; j++) {
            p2i.set(propagators[j].getId(), j);
        }
        pro_queue_f = new CircularQueue<Propagator>(propagators.length / 10 + 1);
        pro_queue_c = new CircularQueue<Propagator>(propagators.length);

        schedule = new short[nbProp];
        masks_f = new int[nbProp][];
        for (int i = 0; i < nbProp; i++) {
            masks_f[i] = new int[nbVars.get(i)];
        }
        masks_c = new int[nbProp];
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
        int mask, aid;
        int[] vindices;
        do {
            while (!pro_queue_f.isEmpty()) {
                lastProp = pro_queue_f.pollFirst();
                assert lastProp.isActive() : "propagator is not active";
                // revision of the variable
                aid = p2i.get(lastProp.getId());
                schedule[aid] ^= F;
                vindices = lastProp.getVIndices();
                for (int v = 0; v < vindices.length; v++) {
                    mask = masks_f[aid][v];
                    if (mask > 0) {
                        if (IEventRecorder.DEBUG_PROPAG) {
                            LoggerFactory.getLogger("solver").info("* {}", "<< {F} " + Arrays.toString(lastProp.getVars()) + "::" + lastProp.toString() + " >>");
                        }
                        masks_f[aid][v] = 0;
                        lastProp.fineERcalls++;
                        lastProp.propagate(null, vindices[v], mask);
                    }
                }
            }
            if (!pro_queue_c.isEmpty()) {
                lastProp = pro_queue_c.pollFirst();
                // revision of the propagator
                aid = p2i.get(lastProp.getId());
                mask = masks_c[aid];
                masks_c[aid] = 0;
                schedule[aid] ^= C;
                lastProp.coarseERcalls++;
                if (IEventRecorder.DEBUG_PROPAG) {
                    LoggerFactory.getLogger("solver").info("* {}", "<< ::" + lastProp.toString() + " >>");
                }
                lastProp.propagate(mask);
                if (lastProp.isStateLess()) {
                    lastProp.setActive();
                } else {
                    onPropagatorExecution(lastProp);
                }
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
        }
        while (!pro_queue_f.isEmpty()) {
            lastProp = pro_queue_f.pollFirst();
            // revision of the variable
            aid = p2i.get(lastProp.getId());
            Arrays.fill(masks_f[aid], 0);
            schedule[aid] = 0;
        }
        while (!pro_queue_c.isEmpty()) {
            lastProp = pro_queue_c.pollFirst();
            // revision of the variable
            aid = p2i.get(lastProp.getId());
            schedule[aid] = 0;
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        Propagator[] vProps = variable.getPropagators();
        int[] pindices = variable.getPIndices();
        for (int p = 0; p < vProps.length; p++) {
            Propagator prop = vProps[p];
            if (cause != prop && prop.isActive()) {
                if (IEventRecorder.DEBUG_PROPAG)
                    LoggerFactory.getLogger("solver").info("\t|- {}", "<< {F} " + Arrays.toString(prop.getVars()) + "::" + prop.toString() + " >>");
                if ((type.mask & prop.getPropagationConditions(pindices[p])) != 0) {
                    int aid = p2i.get(prop.getId());
                    masks_f[aid][pindices[p]] |= type.strengthened_mask;
                    if ((schedule[aid] & F) == 0) {
                        pro_queue_f.addLast(prop);
                        schedule[aid] |= F;
                    }
                }
            }
        }

    }

    @Override
    public void schedulePropagator(@NotNull Propagator propagator, EventType event) {
        int pid = propagator.getId();
        int aid = p2i.get(pid);
        if ((schedule[aid] & C) == 0) {
            if (IEventRecorder.DEBUG_PROPAG) {
                LoggerFactory.getLogger("solver").info("\t|- {}", "<< ::" + propagator.toString() + " >>");
            }
            pro_queue_c.addLast(propagator);
            schedule[aid] |= C;
            masks_c[aid] |= event.getStrengthenedMask();
        }
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
        if (lastProp == propagator) {
            for (int i = 0; i < propagator.getNbVars(); i++) {
                masks_f[aid][i] = 0;
            }
        } else if ((schedule[aid] & F) != 0) {
            for (int i = 0; i < propagator.getNbVars(); i++) {
                masks_f[aid][i] = 0;
            }
            schedule[aid] ^= F;
            pro_queue_f.remove(propagator);
        }
        if ((schedule[aid] & C) != 0) {
            schedule[aid] ^= C;
            pro_queue_c.remove(propagator);
            masks_c[aid] = 0;
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
