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
import org.slf4j.LoggerFactory;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.hardcoded.util.AId2AbId;
import solver.propagation.hardcoded.util.IId2AbId;
import solver.propagation.queues.CircularQueue;
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
    protected final Propagator[] propagators;

    protected final CircularQueue<Propagator>[] pro_queue;
    protected Propagator lastProp;
    protected final short[] scheduled; // also maintains the index of the queue!
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index
    protected final BitSet notEmpty; // point out the no empty queues
    protected final int[][] evtmasks;

    public SevenQueuesConstraintEngine(Solver solver) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        variables = solver.getVars();
        List<Propagator> _propagators = new ArrayList();
        Constraint[] constraints = solver.getCstrs();
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
        pro_queue = new CircularQueue[9];
        for (int i = 0; i < 9; i++) {
            pro_queue[i] = new CircularQueue<Propagator>(16);
        }

        scheduled = new short[nbProp];
        evtmasks = new int[nbProp][];
        for (int i = 0; i < nbProp; i++) {
            evtmasks[i] = new int[propagators[i].getNbVars()];
        }
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
    }

    @SuppressWarnings({"NullableProblems"})
    @Override
    public void propagate() throws ContradictionException {
        int mask, aid;
        for (int i = notEmpty.nextSetBit(0); i > -1; i = notEmpty.nextSetBit(0)) {
            while (!pro_queue[i].isEmpty()) {
                lastProp = pro_queue[i].pollFirst();
                assert lastProp.isActive() : "propagator is not active:" + lastProp;
                // revision of the variable
                aid = p2i.get(lastProp.getId());
                scheduled[aid] = 0;
                int nbVars = lastProp.getNbVars();
                for (int v = 0; v < nbVars; v++) {
                    mask = evtmasks[aid][v];
                    if (mask > 0) {
                        if (Configuration.PRINT_PROPAGATION) {
                            LoggerFactory.getLogger("solver").info("* {}", "<< {F} " + lastProp.getVar(v) + "::" + lastProp.toString() + " >>");
                        }
                        evtmasks[aid][v] = 0;
                        lastProp.fineERcalls++;
                        lastProp.decNbPendingEvt();
                        lastProp.propagate(v, mask);
                    }
                }
            }
            notEmpty.clear(i);
        }
    }

    @Override
    public void flush() {
        int aid;
        if (lastProp != null) {
            aid = p2i.get(lastProp.getId());
            Arrays.fill(evtmasks[aid], 0);
            scheduled[aid] = 0;
            lastProp.flushPendingEvt();
        }
        for (int i = notEmpty.nextSetBit(0); i > -1; i = notEmpty.nextSetBit(i + 1)) {
            while (!pro_queue[i].isEmpty()) {
                lastProp = pro_queue[i].pollFirst();
                // revision of the variable
                aid = p2i.get(lastProp.getId());
                Arrays.fill(evtmasks[aid], 0);
                scheduled[aid] = 0;
                lastProp.flushPendingEvt();

            }
            notEmpty.clear(i);
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
                    if (evtmasks[aid][pindices[p]] == 0) {
                        prop.incNbPendingEvt();
                    }
                    evtmasks[aid][pindices[p]] |= type.strengthened_mask;
                    if (scheduled[aid] == 0) {
                        int prio = 0;
                        pro_queue[prio].addLast(prop);
                        scheduled[aid] = (short) (prio + 1);
                        notEmpty.set(prio);
                    }
                }
            }
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
//        if (aid > -1) {
        assert aid > -1 : "try to desactivate an unknown constraint";
        Arrays.fill(evtmasks[aid], 0); // fill with NO_MASK, outside the loop, to handle propagator currently executed
        int prio = scheduled[aid];
        if (prio > 0) { // if in the queue...
            scheduled[aid] = 0;
            pro_queue[prio - 1].remove(propagator); // removed from the queue
            propagator.flushPendingEvt();
        }
//        }
    }

    @Override
    public void clear() {
        // void
    }
}
