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
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.IPropagationStrategy;
import solver.propagation.queues.CircularQueue;
import solver.recorders.IEventRecorder;
import solver.recorders.coarse.AbstractCoarseEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;

import java.util.*;

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
public class ArcEngine implements IPropagationEngine {

    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;
    protected final Propagator[] propagators;

    protected final CircularQueue<Variable> arc_queue_v;
    protected Variable lastVar;
    protected final CircularQueue<Propagator> arc_queue_p;
    protected Propagator lastProp;
    protected final CircularQueue<Propagator> pro_queue_c;
    protected final BitSet schedule_c;
    protected final int[][] masks;  // -1 : not inserted, 0: inserted but no event, >0: inserted and events
    protected final int[][] idxVinP;

    public ArcEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        variables = solver.getVars();
        List<Propagator> _propagators = new ArrayList();
        Constraint[] constraints = solver.getCstrs();
        for (int c = 0; c < constraints.length; c++) {
            Propagator[] cprops = constraints[c].propagators;
            Collections.addAll(_propagators, cprops);
        }
        propagators = _propagators.toArray(new Propagator[_propagators.size()]);

        arc_queue_v = new CircularQueue<Variable>(8);
        arc_queue_p = new CircularQueue<Propagator>(8);
        pro_queue_c = new CircularQueue<Propagator>(propagators.length);

        int size = solver.getNbIdElt();
        schedule_c = new BitSet(size);
        masks = new int[size][size];
        idxVinP = new int[size][size];
        for (int i = 0; i < variables.length; i++) {
            int vid = variables[i].getId();
            Arrays.fill(masks[vid], -1);
            Arrays.fill(idxVinP[vid], -1);
            Propagator[] vprops = variables[i].getPropagators();
            int[] vindices = variables[i].getPIndices();
            for (int j = 0; j < vprops.length; j++) {
                int pid = vprops[j].getId();
                idxVinP[vid][pid] = vindices[j];
            }
        }
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
        do {
            while (!arc_queue_v.isEmpty()) {
                lastVar = arc_queue_v.pollFirst();
                lastProp = arc_queue_p.pollFirst();
//                assert lastProp.isActive() : "propagator is not active";
                // revision of the variable
                int vid = lastVar.getId();
                int pid = lastProp.getId();
                int mask = masks[vid][pid];
                masks[vid][pid] = -1;
                if (mask > 0) {
                    if (IEventRecorder.DEBUG_PROPAG) {
                        LoggerFactory.getLogger("solver").info("* {}", "<< {F} " + lastVar.toString() + "::" + lastProp.toString() + " >>");
                    }
                    lastProp.fineERcalls++;
                    lastProp.propagate(null, idxVinP[vid][pid], mask);
                }
            }
            if (!pro_queue_c.isEmpty()) {
                lastProp = pro_queue_c.pollFirst();
                int pid = lastProp.getId();
                if (lastProp.isStateLess()) {
                    lastProp.setActive();
                }
                // revision of the propagator
                schedule_c.clear(pid);
                lastProp.coarseERcalls++;
                if (IEventRecorder.DEBUG_PROPAG) {
                    LoggerFactory.getLogger("solver").info("* {}", "<< ::" + lastProp.toString() + " >>");
                }
                lastProp.propagate(EventType.FULL_PROPAGATION.mask);
                onPropagatorExecution(lastProp);
            }
        } while (!arc_queue_v.isEmpty() || !pro_queue_c.isEmpty());

    }

    @Override
    public void flush() {
        if (lastProp != null && lastVar != null) {
            int vid = lastVar.getId();
            int pid = lastProp.getId();
            masks[vid][pid] = -1;
        }
        while (!arc_queue_v.isEmpty()) {
            lastVar = arc_queue_v.pollFirst();
            lastProp = arc_queue_p.pollFirst();
            // revision of the variable
            int vid = lastVar.getId();
            int pid = lastProp.getId();
            masks[vid][pid] = -1;
        }
        pro_queue_c.clear();
        schedule_c.clear();
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        int vid = variable.getId();
        Propagator[] vProps = variable.getPropagators();
        int[] vindices = idxVinP[vid];
        for (int p = 0; p < vProps.length; p++) {
            Propagator prop = vProps[p];
            if (cause != prop) {
                int pid = prop.getId();
                if ((type.mask & prop.getPropagationConditions(vindices[pid])) != 0) {
                    if (IEventRecorder.DEBUG_PROPAG) {
                        LoggerFactory.getLogger("solver").info("\t|- {}", "<< {F} " + variable.toString() + "::" + prop.toString() + " >>");
                    }
                    if (masks[vid][pid] == -1) {  // add the arc into the queue
                        arc_queue_v.addLast(variable);
                        arc_queue_p.addLast(prop);
                        masks[vid][pid] = type.strengthened_mask;
                    } else {
                        masks[vid][pid] |= type.strengthened_mask;
                    }

                }
            }
        }

    }

    @Override
    public void schedulePropagator(@NotNull Propagator propagator, EventType event) {
        int pid = propagator.getId();
        if (!schedule_c.get(pid)) {
            if (IEventRecorder.DEBUG_PROPAG) {
                LoggerFactory.getLogger("solver").info("\t|- {}", "<< ::" + propagator.toString() + " >>");
            }
            pro_queue_c.addLast(propagator);
            schedule_c.set(pid);
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
        Variable[] variables = propagator.getVars();
        for (int i = 0; i < variables.length; i++) {
            int vid = variables[i].getId();
            if (masks[vid][pid] > 0) {  // if it is present in the queue
                masks[vid][pid] = 0; // simply clear the mask
            }
        }
        if (schedule_c.get(pid)) {
            schedule_c.clear(pid);
            pro_queue_c.remove(propagator);
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
    public void clearWatermark(int id1, int id2, int id3) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMarked(int id1, int id2, int id3) {
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
