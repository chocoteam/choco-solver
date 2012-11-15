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
import solver.propagation.PropagationUtils;
import solver.propagation.hardcoded.util.AId2AbId;
import solver.propagation.hardcoded.util.IId2AbId;
import solver.propagation.queues.DoubleMinHeap;
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
public class ActivityBasedCstrEngine implements IPropagationEngine {

    static final double ONE = 1.0f;

    static final double[] distribution = new double[]{// two-sided 95%
            999.99d,
            12.706f, 4.303f, 3.182f, 2.776f, 2.571f, // 1...5
            2.447f, 2.365f, 2.306f, 2.262f, 2.228f,  // 6...10
            2.201f, 2.179f, 2.160f, 2.145f, 2.131f,  // 10...15
            2.120f, 2.110f, 2.101f, 2.093f, 2.086f,  // 16...20
            2.080f, 2.074f, 2.069f, 2.064f, 2.060f,  // 21...25
            2.056f, 2.052f, 2.048f, 2.045f, 2.042f,  // 26...30
            2.040f, 2.037f, 2.035f, 2.032f, 2.030f,  // 31...35
            2.028f, 2.026f, 2.024f, 2.023f, 2.021f,  // 36...40
            2.000f, 1.990f, 1.984f, 1.980f, 1.977f,  // 60, 80, 100, 120, 140
            1.975f, 1.973f, 1.972f, 1.969f, 1.960f   // 160, 180, 200, 250, inf
    };

    private static double distribution(int n) {
        if (n <= 0) {
            throw new UnsupportedOperationException();
        } else if (n > 0 && n < 41) {
            return distribution[n - 1];
        } else if (n < 61) {
            return distribution[40];
        } else if (n < 81) {
            return distribution[41];
        } else if (n < 101) {
            return distribution[42];
        } else if (n < 121) {
            return distribution[43];
        } else if (n < 141) {
            return distribution[44];
        } else if (n < 161) {
            return distribution[45];
        } else if (n < 181) {
            return distribution[46];
        } else if (n < 201) {
            return distribution[47];
        } else if (n < 251) {
            return distribution[48];
        } else {
            return distribution[49];
        }
    }


    protected final ContradictionException exception; // the exception in case of contradiction
    protected final IEnvironment environment; // environment of backtrackable objects
    protected final Variable[] variables;
    protected final Propagator[] propagators;

    protected final DoubleMinHeap prop_heap;
    protected Propagator lastProp;
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index
    protected final boolean[] schedule;
    protected final int[][] masks_f;

    protected double[] A;  // activities
    protected double[] mA;  // mean of activities
    protected double[] vA;  // variation of activities
    protected double[] I;  //  count the impact of variables
    protected double[] S; // count the number of time a variable has been scheduled
    protected TIntSet affected;
    protected int nb_probes;
    protected int cid = -1;
    final double g = .999F, d = 0.2F; // g for aging, d for interval size estimation

    public boolean sampling; // is this still in a sampling phase
    public boolean aging;
    public static final short minOrmax = -1; // 1: min , -1: max

    public final Random random = new Random();

    protected Activity myActivity;


    public ActivityBasedCstrEngine(Solver solver, Activity act, boolean sampling, boolean aging) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        // 0. get the type of activity
        myActivity = act;
        this.sampling = sampling;
        this.aging = aging;

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
        prop_heap = new DoubleMinHeap(propagators.length / 10 + 1);
        schedule = new boolean[nbProp];
        masks_f = new int[nbProp][];
        for (int i = 0; i < nbProp; i++) {
            masks_f[i] = new int[propagators[i].getNbVars()];
        }

        // 6. Build the array of weights
        A = new double[Mp - mp + 1];
        mA = new double[Mp - mp + 1];
        vA = new double[Mp - mp + 1];
        S = new double[Mp - mp + 1];
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
    }

    @SuppressWarnings({"NullableProblems"})
    @Override
    public void propagate() throws ContradictionException {
        int mask, aid;
        nb_probes++;
        try {
            while (!prop_heap.isEmpty()) {
                lastProp = propagators[prop_heap.removemin()];
                assert lastProp.isActive() : "propagator is not active";
                // revision of the variable
                aid = p2i.get(lastProp.getId());
                cid = aid;
                schedule[aid] = false;
                int nbVars = lastProp.getNbVars();
                for (int v = 0; v < nbVars; v++) {
                    mask = masks_f[aid][v];
                    if (mask > 0) {
                        if (Configuration.PRINT_PROPAGATION) {
                            PropagationUtils.printPropagation(lastProp.getVar(v), lastProp);
                        }
                        masks_f[aid][v] = 0;
                        lastProp.fineERcalls++;
                        lastProp.decNbPendingEvt();
                        lastProp.propagate(v, mask);
                    }
                }
            }
        } finally {
            cid = -1;
            updateActivities();
        }
    }

    private void updateActivities() {
        if (sampling) {
            for (int i : affected.toArray()) {
                double activity = myActivity.get(this, i);
                double oldmA = mA[i];

                double U = activity - oldmA;
                mA[i] += (U / nb_probes);
                vA[i] += (U * (activity - mA[i]));
                I[i] = 0;
                S[i] = 0;
            }
            affected.clear();
            int idx = 0;
            //            LoggerFactory.getLogger("fzn").info("CHECK...");
            while (idx < propagators.length && checkInterval(idx)) {
                idx++;
            }
            if (idx == propagators.length /*|| nb_probes > 999*/) {
                sampling = false;
                LoggerFactory.getLogger("fzn").info("% STOP {}!", nb_probes);
                System.arraycopy(mA, 0, A, 0, mA.length);
            } else {
                //                LoggerFactory.getLogger("fzn").info("{}!={} => RANDOMIZE!", idx, variables.length);
                for (int i = 0; i < propagators.length; i++) {
                    A[i] = random.nextDouble();
                }
            }
        } else {
            if (aging)
                for (int i = 0; i < propagators.length; i++) {
                    if (affected.contains(i)) {
                        A[i] += myActivity.get(this, i);
                        I[i] = 0;
                        S[i] = 0;
                    } else {
                        A[i] *= sampling ? ONE : g;
                    }
                }
            else {
                for (int i : affected.toArray()) {
                    A[i] += myActivity.get(this, i);
                    I[i] = 0;
                    S[i] = 0;
                }
            }
        }
    }

    /**
     * Return true if the interval is small enough
     *
     * @param idx idx of the variable to check
     * @return true if the confidence interval is small enough, false otherwise
     */
    private boolean checkInterval(int idx) {
        if (propagators[idx].isActive() && !propagators[idx].isCompletelyInstantiated()) {
            double stdev = Math.sqrt(vA[idx] / (nb_probes - 1));
            double a = distribution(nb_probes) * stdev / Math.sqrt(nb_probes);
            boolean isOK = ((a / mA[idx]) < d);
            if (!isOK) {
//                LoggerFactory.getLogger("fzn").info("{}::  m: {}, v: {}, et: {} => {} < {}", new Object[]{propagators[idx], mA[idx], vA[idx], stdev, (a / mA[idx]), d});
            }
            return isOK;
        }
        return true;
    }

    @Override
    public void flush() {
        int aid;
        if (lastProp != null) {
            aid = p2i.get(lastProp.getId());
            Arrays.fill(masks_f[aid], 0);
            schedule[aid] = false;
            lastProp.flushPendingEvt();
        }
        while (!prop_heap.isEmpty()) {
            lastProp = propagators[prop_heap.removemin()];
            // revision of the variable
            aid = p2i.get(lastProp.getId());
            Arrays.fill(masks_f[aid], 0);
            schedule[aid] = false;
            lastProp.flushPendingEvt();
        }
    }

    @Override
    public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        if (Configuration.PRINT_VAR_EVENT) {
            PropagationUtils.printModification(variable, type, cause);
        }
        Propagator[] vProps = variable.getPropagators();
        int[] pindices = variable.getPIndices();
        for (int p = 0; p < vProps.length; p++) {
            Propagator prop = vProps[p];
            if (cause != prop && prop.isActive() && prop.advise(pindices[p], type.mask)) {
                if (Configuration.PRINT_SCHEDULE){
                    PropagationUtils.printSchedule(prop);
                }
                int aid = p2i.get(prop.getId());
                if (masks_f[aid][pindices[p]] == 0) {
                    prop.incNbPendingEvt();
                }
                masks_f[aid][pindices[p]] |= type.strengthened_mask;
                if (!schedule[aid]) {
                    double _w = minOrmax * A[aid];
                    prop_heap.insert(_w, aid);
                    schedule[aid] = true;
                    S[aid]++;
                    if (cid != -1) {
                        affected.add(cid);
                        I[cid]++;
                    }
                    prop.incNbPendingEvt();
                }
            }
        }

    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {
        desactivatePropagator(propagator);
    }

    @Override
    public void desactivatePropagator(Propagator propagator) {
        int pid = propagator.getId();
        int aid = p2i.get(pid);
        if (aid > -1) {
            Arrays.fill(masks_f[aid], 0); // fill with NO_MASK, outside the loop, to handle propagator currently executed
            if (schedule[aid]) { // if in the queue...
                schedule[aid] = false;
                prop_heap.remove(aid); // removed from the queue
                lastProp.flushPendingEvt();
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
            public double get(ActivityBasedCstrEngine engine, int i) {
                return engine.I[i];
            }
        },
        REL_IMP() {
            @Override
            public double get(ActivityBasedCstrEngine engine, int i) {
                return (engine.I[i] > 0 ? 1.0 : 0.0);
            }
        };

        public abstract double get(ActivityBasedCstrEngine engine, int i);

    }
}
