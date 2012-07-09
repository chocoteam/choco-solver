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
import gnu.trove.map.hash.TIntIntHashMap;
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
    protected final boolean[] schedule_c;
    protected final IId2AbId v2i; // mapping between propagator ID and its absolute index
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index
    protected final TIntIntHashMap[] masks;
    protected final TIntIntHashMap[] idxVinP;

    public ArcEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        variables = solver.getVars();
        int nbVar = 0;
        int m = Integer.MAX_VALUE, M = Integer.MIN_VALUE;
        for (; nbVar < variables.length; nbVar++) {
            int id = variables[nbVar].getId();
            m = Math.min(m, id);
            M = Math.max(M, id);
        }
        v2i = new AId2AbId(m, M, -1);
        for (int i = 0; i < nbVar; i++) {
            int id = variables[i].getId();
            v2i.set(id, i);
        }
        List<Propagator> _propagators = new ArrayList();
        Constraint[] constraints = solver.getCstrs();
        int nbProp = 0;
        m = Integer.MAX_VALUE;
        M = Integer.MIN_VALUE;
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
//        p2i = new MId2AbId(M - m + 1, -1);
        for (int j = 0; j < propagators.length; j++) {
            p2i.set(propagators[j].getId(), j);
        }


        arc_queue_v = new CircularQueue<Variable>(8);
        arc_queue_p = new CircularQueue<Propagator>(8);
        pro_queue_c = new CircularQueue<Propagator>(propagators.length);

        schedule_c = new boolean[nbProp];
        masks = new TIntIntHashMap[nbVar];
        idxVinP = new TIntIntHashMap[nbVar];
        for (int i = 0; i < variables.length; i++) {
            masks[i] = new TIntIntHashMap(4, 0.5f, -1, -1);
            idxVinP[i] = new TIntIntHashMap(4, 0.5f, -1, -1);
            Propagator[] vprops = variables[i].getPropagators();
            int[] vindices = variables[i].getPIndices();
            for (int j = 0; j < vprops.length; j++) {
                int paid = p2i.get(vprops[j].getId());
                idxVinP[i].put(paid, vindices[j]);
                masks[i].put(paid, -1);
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
//                assert lastProp.isActive() : "propagator is not active"; <= CPRU: a propagator can be inactive, what matters is the mask

                    // revision of the variable
                    int vaid = v2i.get(lastVar.getId());
                    int paid = p2i.get(lastProp.getId());
                    int mask = masks[vaid].get(paid);
                    masks[vaid].adjustValue(paid, -(mask + 1)); // we add +1 to make sure new value is -1
                    if (mask > 0) {
                        if (IEventRecorder.DEBUG_PROPAG) {
                            LoggerFactory.getLogger("solver").info("* {}", "<< {F} " + lastVar.toString() + "::" + lastProp.toString() + " >>");
                        }
                        lastProp.fineERcalls++;
                        lastProp.propagate(null, idxVinP[vaid].get(paid), mask);
                    }
                }
            if (!pro_queue_c.isEmpty()) {
                lastProp = pro_queue_c.pollFirst();
                if (lastProp.isStateLess()) {
                    lastProp.setActive();
                }
                // revision of the propagator
                schedule_c[p2i.get(lastProp.getId())] = false;
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
            int vaid = v2i.get(lastVar.getId());
            int paid = p2i.get(lastProp.getId());
            masks[vaid].put(paid, -1);
        }
        while (!arc_queue_v.isEmpty()) {
            lastVar = arc_queue_v.pollFirst();
            lastProp = arc_queue_p.pollFirst();
            // revision of the variable
            int vaid = v2i.get(lastVar.getId());
            int paid = p2i.get(lastProp.getId());
            masks[vaid].put(paid, -1);
        }
        while (!pro_queue_c.isEmpty()) {
            lastProp = pro_queue_c.pollFirst();
            int pid = p2i.get(lastProp.getId());
            schedule_c[pid] = false;
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        int vaid = v2i.get(variable.getId());
        Propagator[] vProps = variable.getPropagators();
        for (int p = 0; p < vProps.length; p++) {
            Propagator prop = vProps[p];
            if (cause != prop && prop.isActive()) {
                int paid = p2i.get(prop.getId());
                if ((type.mask & prop.getPropagationConditions(idxVinP[vaid].get(paid))) != 0) {
                    if (IEventRecorder.DEBUG_PROPAG) {
                        LoggerFactory.getLogger("solver").info("\t|- {}", "<< {F} " + variable.toString() + "::" + prop.toString() + " >>");
                    }
                    int cm = masks[vaid].get(paid);
                    if (cm == -1) {  // add the arc into the queue
                        arc_queue_v.addLast(variable);
                        arc_queue_p.addLast(prop);
                        masks[vaid].adjustValue(paid, type.strengthened_mask + 1);
                    } else {
                        cm -= (cm |= type.strengthened_mask);
                        masks[vaid].adjustValue(paid, cm);
                    }

                }
            }
        }

    }

    @Override
    public void schedulePropagator(@NotNull Propagator propagator, EventType event) {
        int paid = p2i.get(propagator.getId());
        if (!schedule_c[paid]) {
            if (IEventRecorder.DEBUG_PROPAG) {
                LoggerFactory.getLogger("solver").info("\t|- {}", "<< ::" + propagator.toString() + " >>");
            }
            pro_queue_c.addLast(propagator);
            schedule_c[paid] = true;
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
        int paid = p2i.get(propagator.getId());
        Variable[] variables = propagator.getVars();
        for (int i = 0; i < variables.length; i++) {
            int vaid = v2i.get(variables[i].getId());
            int cm = masks[vaid].get(paid);
            if (cm > 0) {  // if it is present in the queue
                masks[vaid].adjustValue(paid, -cm); // simply clear the mask
            }
        }
        if (schedule_c[paid]) {
            schedule_c[paid] = false;
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
