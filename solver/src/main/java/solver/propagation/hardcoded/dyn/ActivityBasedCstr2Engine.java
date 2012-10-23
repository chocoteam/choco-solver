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
import gnu.trove.set.TIntSet;
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
import java.util.Random;

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
public class ActivityBasedCstr2Engine implements IPropagationEngine {

    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;
    protected final Propagator[] propagators;

    protected static final int F = 1, C = 2;

    protected final DoubleMinHeap propSet1;
    //    protected final CircularQueue<Propagator> propSet1;
    protected final CircularQueue<Propagator> propSet2;

    protected Propagator lastProp;
    protected final CircularQueue<Propagator> pro_queue_c;
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index
    protected final short[] schedule;
    protected final int[][] masks_f;
    protected final int[] masks_c;

    protected double[] A;  // activities
    protected double[] I;  //  count the impact of variables
    protected TIntSet affected;
    protected int cid = -1;

    protected int nb_probes;
    protected double LIMIT = 0.0d;

    public final Random random = new Random();

    protected Activity myActivity;


    public ActivityBasedCstr2Engine(Solver solver, Activity act) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        // 0. get the type of activity
        myActivity = act;

        // 2. Copy the variables
        variables = solver.getVars();

        // 2. Copy the propagators
        List<Propagator> _propagators = new ArrayList();
        Constraint[] constraints = solver.getCstrs();
        int nbProp = 0;
        int mp = Integer.MAX_VALUE, Mp = Integer.MIN_VALUE;
        for (int c = 0; c < constraints.length; c++) {
            Propagator[] cprops = constraints[c].propagators;
            for (int j = 0; j < cprops.length; j++, nbProp++) {
                _propagators.add(cprops[j]);
                int id = cprops[j].getId();
                mp = Math.min(mp, id);
                Mp = Math.max(Mp, id);
            }
        }
        propagators = _propagators.toArray(new Propagator[_propagators.size()]);

        // 4. Map ID and index and prepare to store masks
        p2i = new AId2AbId(mp, Mp, -1);
        for (int j = 0; j < propagators.length; j++) {
            p2i.set(propagators[j].getId(), j);
        }
        // 5. Build the structures
        propSet1 = new DoubleMinHeap(propagators.length / 10);
//        propSet1 = new CircularQueue<Propagator>(propagators.length / 10);
        propSet2 = new CircularQueue<Propagator>(propagators.length / 10);
        pro_queue_c = new CircularQueue<Propagator>(propagators.length);
        schedule = new short[nbProp];
        masks_f = new int[nbProp][];
        for (int i = 0; i < nbProp; i++) {
            masks_f[i] = new int[propagators[i].getNbVars()];
        }
        masks_c = new int[nbProp];

        // 6. Build the array of weights
        A = new double[Mp - mp + 1];
        I = new double[Mp - mp + 1];
        affected = new TIntHashSet();
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
        nb_probes++;
        try {
            do {
                do {
                    while (!propSet1.isEmpty()) {
                        lastProp = propagators[propSet1.removemin()];
//                        lastProp = propSet1.pollFirst();
                        _propagate();
                    }
                    if (!propSet2.isEmpty()) {
                        lastProp = propSet2.pollFirst();
                        _propagate();
                    }
                    /*while(!propSet2.isEmpty()) {
                        lastProp = propSet2.pollFirst();
                        _propagate();
                    }
                    if (!propSet1.isEmpty()) {
                        lastProp = propagators[propSet1.removemin()];
                        _propagate();
                    }*/
                } while (!propSet1.isEmpty() || !propSet2.isEmpty());

                if (!pro_queue_c.isEmpty()) {
                    lastProp = pro_queue_c.pollFirst();
                    // revision of the propagator
                    aid = p2i.get(lastProp.getId());
                    mask = masks_c[aid];
                    masks_c[aid] = 0;
                    schedule[aid] ^= C;
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
            } while (!propSet1.isEmpty() || !propSet2.isEmpty() || !pro_queue_c.isEmpty());
        } finally {
            cid = -1;
            updateActivities();
        }
    }

    private void _propagate() throws ContradictionException {
        assert lastProp.isActive() : "propagator is not active";
        // revision of the variable
        int aid = p2i.get(lastProp.getId());
        cid = aid;
        schedule[aid] ^= F;
        int nbVars = lastProp.getNbVars();
        for (int v = 0; v < nbVars; v++) {
            int mask = masks_f[aid][v];
            if (mask > 0) {
                if (Configuration.PRINT_PROPAGATION) {
                    LoggerFactory.getLogger("solver").info("* {}", "<< {F} " + lastProp.getVar(v) + "::" + lastProp.toString() + " >>");
                }
                masks_f[aid][v] = 0;
                lastProp.fineERcalls++;
                lastProp.propagate(v, mask);
            }
        }
    }

    private void updateActivities() {
        double coeff = 1.0 / propagators.length;
        for (int i : affected.toArray()) {
            A[i] += myActivity.get(this, i) * coeff;
            I[i] = 0;
            LIMIT += A[i];

        }
        LIMIT /= affected.size();
//        System.out.printf("LIMIT %.5f\n", LIMIT);

        affected.clear();
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
        while (!propSet1.isEmpty()) {
            lastProp = propagators[propSet1.removemin()];
//            lastProp = propSet1.pollFirst();
            // revision of the variable
            aid = p2i.get(lastProp.getId());
            Arrays.fill(masks_f[aid], 0);
            schedule[aid] = 0;
        }
        while (!propSet2.isEmpty()) {
            lastProp = propSet2.pollFirst();
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
                if ((type.mask & prop.getPropagationConditions(pindices[p])) != 0) {
                    int aid = p2i.get(prop.getId());
                    masks_f[aid][pindices[p]] |= type.strengthened_mask;
                    if ((schedule[aid] & F) == 0) {
                        double _w = A[aid];
                        if (_w > LIMIT) {
                            propSet2.addLast(prop);
                        } else {
                            propSet1.insert(_w, aid);
//                            propSet1.addLast(prop);
                        }
                        schedule[aid] |= F;
                        if (cid != -1) {
                            affected.add(cid);
                            I[cid]++;
                        }
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
            if (Configuration.PRINT_PROPAGATION) {
                LoggerFactory.getLogger("solver").info("\t|- {}", "<< ::" + propagator.toString() + " >>");
            }
            pro_queue_c.addLast(propagator);
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
        if (aid > -1) {
            Arrays.fill(masks_f[aid], 0); // fill with NO_MASK, outside the loop, to handle propagator currently executed
            if ((schedule[aid] & F) != 0) { // if in the queue...
                schedule[aid] ^= F;
                double _w = A[aid];

                if (_w > LIMIT) {
                    propSet2.remove(propagator);
                } else {
                    propSet1.remove(aid);
//                    propSet1.remove(propagator);
                }
            }
            if ((schedule[aid] & C) != 0) { // if in the queue...
                schedule[aid] ^= C;
                masks_c[aid] = 0;
                pro_queue_c.remove(propagator); // removed from the queue
            }
        }
    }

    @Override
    public void clear() {
        // void
    }

    public enum Activity {
        ABS_IMP() {
            @Override
            public double get(ActivityBasedCstr2Engine engine, int i) {
                return engine.I[i];
            }
        },
        REL_IMP() {
            @Override
            public double get(ActivityBasedCstr2Engine engine, int i) {
                return (engine.I[i] > 0 ? 1.0 : 0.0);
            }
        };

        public abstract double get(ActivityBasedCstr2Engine engine, int i);

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
