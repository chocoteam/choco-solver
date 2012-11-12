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
import gnu.trove.map.hash.TIntIntHashMap;
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
    protected final IId2AbId v2i; // mapping between propagator ID and its absolute index
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index
    protected final TIntIntHashMap[] masks_f;
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

        masks_f = new TIntIntHashMap[nbVar];
        idxVinP = new TIntIntHashMap[nbVar];
        for (int i = 0; i < variables.length; i++) {
            masks_f[i] = new TIntIntHashMap(4, 0.5f, -1, -1);
            idxVinP[i] = new TIntIntHashMap(4, 0.5f, -1, -1);
            Propagator[] varprops = variables[i].getPropagators();
            int[] idVinP = variables[i].getPIndices();
            for (int p = 0; p < varprops.length; p++) {
                int paid = p2i.get(varprops[p].getId());
                idxVinP[i].put(paid, idVinP[p]);
                masks_f[i].put(paid, -1);
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
    }

    @SuppressWarnings({"NullableProblems"})
    @Override
    public void propagate() throws ContradictionException {
        int vaid, paid, mask;
        while (!arc_queue_v.isEmpty()) {
            lastVar = arc_queue_v.pollFirst();
            lastProp = arc_queue_p.pollFirst();
//                assert lastProp.isActive() : "propagator is not active"; <= CPRU: a propagator can be inactive, what matters is the mask

            // revision of the variable
            vaid = v2i.get(lastVar.getId());
            paid = p2i.get(lastProp.getId());
            mask = masks_f[vaid].get(paid);
            masks_f[vaid].adjustValue(paid, -(mask + 1)); // we add +1 to make sure new value is -1
            if (mask > 0) {
                if (Configuration.PRINT_PROPAGATION) {
                    LoggerFactory.getLogger("solver").info("* {}", "<< {F} " + lastVar.toString() + "::" + lastProp.toString() + " >>");
                }
                lastProp.fineERcalls++;
                lastProp.decNbPendingEvt();
                lastProp.propagate(idxVinP[vaid].get(paid), mask);
            }
        }
    }

    @Override
    public void flush() {
        int vaid, paid;
        if (lastProp != null && lastVar != null) {
            vaid = v2i.get(lastVar.getId());
            paid = p2i.get(lastProp.getId());
            masks_f[vaid].put(paid, -1);
            lastProp.decNbPendingEvt();
        }
        while (!arc_queue_v.isEmpty()) {
            lastVar = arc_queue_v.pollFirst();
            lastProp = arc_queue_p.pollFirst();
            // revision of the variable
            vaid = v2i.get(lastVar.getId());
            paid = p2i.get(lastProp.getId());
            masks_f[vaid].put(paid, -1);
            lastProp.decNbPendingEvt();
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        Propagator prop;
        int paid, cm, vaid = v2i.get(variable.getId());
        Propagator[] vProps = variable.getPropagators();
        for (int p = 0; p < vProps.length; p++) {
            prop = vProps[p];
            if (cause != prop && prop.isActive()) {
                paid = p2i.get(prop.getId());
                if (prop.advise(idxVinP[vaid].get(paid), type.mask)) {
                    if (Configuration.PRINT_PROPAGATION) {
                        LoggerFactory.getLogger("solver").info("\t|- {}", "<< {F} " + variable.toString() + "::" + prop.toString() + " >>");
                    }
                    cm = masks_f[vaid].get(paid);
                    if (cm == -1) {  // add the arc into the queue
                        arc_queue_v.addLast(variable);
                        arc_queue_p.addLast(prop);
                        masks_f[vaid].adjustValue(paid, type.strengthened_mask + 1);
                        prop.incNbPendingEvt();
                    } else {
                        cm -= (cm |= type.strengthened_mask);
                        masks_f[vaid].adjustValue(paid, cm);
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
        int vaid, cm, paid = p2i.get(propagator.getId());
//        if (paid > -1) {
        assert paid > -1 : "try to desactivate an unknown constraint";
        Variable[] variables = propagator.getVars();
        for (int i = 0; i < variables.length; i++) {
            vaid = v2i.get(variables[i].getId());
            if (vaid > -1) {
                cm = masks_f[vaid].get(paid);
                if (cm > 0) {  // if it is present in the queue
                    masks_f[vaid].adjustValue(paid, -cm); // simply clear the mask
                }
            }
        }
        propagator.flushPendingEvt();
//        }
    }

    @Override
    public void clear() {
        // void
    }
}
