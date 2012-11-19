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
import solver.variables.IntVar;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Variable-oriented activity based dynamic engine with aging and sampling.
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public class ActivityBasedVarEngine implements IPropagationEngine {

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

    protected final DoubleMinHeap var_heap;
    protected Variable lastVar;
    protected Propagator lastProp;

    protected final IId2AbId v2i; // mapping between variable ID and its absolute index
    protected final IId2AbId p2i; // mapping between propagator ID and its absolute index

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
    public static final short minOrmax = -1; // min: 1 ; max : -1

    public final Random random = new Random();

    protected final boolean[] schedule;
    protected final int[][] masks_f;

    protected Activity myActivity;


    public ActivityBasedVarEngine(Solver solver, Activity act, boolean sampling, boolean aging) {
        this.exception = new ContradictionException();
        this.environment = solver.getEnvironment();

        // 0. get the type of activity
        myActivity = act;
        this.sampling = sampling;
        this.aging = aging;

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
            masks_f[j] = new int[variables[j].getNbProps()];
        }
        // 4b. Mapping propagators
        p2i = new AId2AbId(mp, Mp, -1);
        for (int j = 0; j < propagators.length; j++) {
            p2i.set(propagators[j].getId(), j);
        }

        // 5. Build the structures
        schedule = new boolean[solver.getNbIdElt()];
        var_heap = new DoubleMinHeap(variables.length / 2 + 1);

        // 6. Build the array of weights
        A = new double[Mv - mv + 1];
        mA = new double[Mv - mv + 1];
        vA = new double[Mv - mv + 1];
        S = new double[Mv - mv + 1];
        I = new double[Mv - mv + 1];
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
        int id, aid, mask, nbp;
        nb_probes++;
        try {
            while (!var_heap.isEmpty()) {
                lastVar = variables[var_heap.removemin()];
                id = lastVar.getId();
                schedule[id] = false;
                aid = v2i.get(id);
                cid = aid;
                nbp = lastVar.getNbProps();
                for (int p = 0; p < nbp; p++) {
                    lastProp = lastVar.getPropagator(p);
                    mask = masks_f[aid][p];
                    if (mask > 0) {
                        if (Configuration.PRINT_PROPAGATION) {
                            PropagationUtils.printPropagation(lastVar, lastProp);
                        }
                        masks_f[aid][p] = 0;
                        lastProp.fineERcalls++;
                        lastProp.decNbPendingEvt();
                        lastProp.propagate(lastVar.getIndiceInPropagator(p), mask);
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
            while (idx < variables.length && checkInterval(idx)) {
                idx++;
            }
            if (idx == variables.length) {
                sampling = false;
                LoggerFactory.getLogger("fzn").info("% STOP {}!", nb_probes);
                System.arraycopy(mA, 0, A, 0, mA.length);
            } else {
//                LoggerFactory.getLogger("fzn").info("{}!={} => RANDOMIZE!", idx, variables.length);
                for (int i = 0; i < variables.length; i++) {
                    A[i] = random.nextDouble();
                }
            }
        } else {
            if (aging)
                for (int i = 0; i < variables.length; i++) {
                    if (affected.contains(i)) {
                        A[i] += myActivity.get(this, i);
                        I[i] = 0;
                        S[i] = 0;
                    } else {
                        A[i] *= sampling ? ONE : g;
                    }
                }
            else
                for (int i : affected.toArray()) {
                    A[i] += myActivity.get(this, i);
                    I[i] = 0;
                    S[i] = 0;
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
        if (!variables[idx].instantiated()) {
            double stdev = Math.sqrt(vA[idx] / (nb_probes - 1));
            double a = distribution(nb_probes) * stdev / Math.sqrt(nb_probes);
            boolean isOK = ((a / mA[idx]) < d);
            if (!isOK) {
//                LoggerFactory.getLogger("fzn").info("{}::  m: {}, v: {}, et: {} => {} < {}", new Object[]{variables[idx], mA[idx], vA[idx], stdev, (a / mA[idx]), d});
            }
            return isOK;
        }
        return true;
    }

    @Override
    public void flush() {
        int id;
        if (lastVar != null) {
            id = lastVar.getId();
            schedule[id] = false;
            Propagator[] vProps = lastVar.getPropagators();
            for (int p = 0; p < vProps.length; p++) {
                if (masks_f[v2i.get(id)][p] > 0) {
                    vProps[p].decNbPendingEvt();
                    masks_f[v2i.get(id)][p] = 0;
                }
            }
        }
        while (!var_heap.isEmpty()) {
            lastVar = variables[var_heap.removemin()];
            // revision of the variable
            id = lastVar.getId();
            schedule[id] = false;
            Propagator[] vProps = lastVar.getPropagators();
            for (int p = 0; p < vProps.length; p++) {
                if (masks_f[v2i.get(id)][p] > 0) {
                    vProps[p].decNbPendingEvt();
                    masks_f[v2i.get(id)][p] = 0;
                }
            }
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
            PropagationUtils.printModification(variable, type, cause);
        }
        int id = variable.getId();
        boolean _schedule = false;
        int aid = v2i.get(id);
        int nbp = variable.getNbProps();
        for (int p = 0; p < nbp; p++) {
            Propagator prop = variable.getPropagator(p);
            int pindice = variable.getIndiceInPropagator(p);
            if (cause != prop && prop.isActive() && prop.advise(pindice, type.mask)) {
                if (masks_f[aid][p] == 0) {
                    if (Configuration.PRINT_SCHEDULE) {
                        PropagationUtils.printSchedule(prop);
                    }
                    prop.incNbPendingEvt();
                } else if (Configuration.PRINT_SCHEDULE) {
                    PropagationUtils.printAlreadySchedule(prop);
                }
                masks_f[aid][p] |= type.strengthened_mask;
                _schedule = true;
            }
        }
        if (_schedule) {
            S[aid]++;
            if (cid != -1) {
                affected.add(cid);
                I[cid]++;
            }
            double _w = minOrmax * A[aid];
            if (!schedule[id]) {
                var_heap.insert(_w, v2i.get(id));
                schedule[id] = true;
            }
        }
    }

    @Override
    public void onPropagatorExecution(Propagator propagator) {
        desactivatePropagator(propagator);
    }

    @Override
    public void desactivatePropagator(Propagator propagator) {
        Variable[] variables = propagator.getVars();
        int[] vindices = propagator.getVIndices();
        for (int i = 0; i < variables.length; i++) {
            if (vindices[i] > -1) {// constant has a negative index
                assert variables[i].getPropagator(vindices[i]) == propagator : propagator.toString() + " >> " + variables[i];
                int vid = v2i.get(variables[i].getId());
                assert vindices[i] < masks_f[vid].length;
                masks_f[vid][vindices[i]] = 0;
            }
        }
        propagator.flushPendingEvt();
    }

    @Override
    public void clear() {
        // void
    }

    public enum Activity {
        ABS_IMP() {
            @Override
            public double get(ActivityBasedVarEngine engine, int i) {
                return engine.I[i];
            }
        },
        REL_IMP() {
            @Override
            public double get(ActivityBasedVarEngine engine, int i) {
                return (engine.I[i] > 0 ? 1.0 : 0.0);
            }
        },
        SCHEDoverIMP() {
            @Override
            public double get(ActivityBasedVarEngine engine, int i) {
                return engine.S[i] / (engine.I[i] + 1);
            }
        },
        IMPoverDOM() {
            @Override
            public double get(ActivityBasedVarEngine engine, int i) {
                return engine.I[i] / ((IntVar) engine.variables[i]).getDomainSize();
            }
        };

        public abstract double get(ActivityBasedVarEngine engine, int i);

    }
}
