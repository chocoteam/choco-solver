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
package solver.search.strategy.selectors.variables;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import memory.IStateInt;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.SolverException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.search.loop.monitors.FailPerPropagator;
import solver.search.strategy.selectors.VariableSelector;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.IntVar;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/12
 */
public class DomOverWDeg implements VariableSelector<IntVar>, IVariableMonitor<IntVar> {

    /* list of variables */
    IntVar[] variables;

    FailPerPropagator counter;

    TIntObjectHashMap<IStateInt> pid2ari;
    TIntIntHashMap pid2arity;
    Random random;

    /* index of the smallest domain variable */
    int small_idx;

    public DomOverWDeg(IntVar[] variables, long seed) {
        this.variables = variables.clone();
        small_idx = 0;
        Solver solver = variables[0].getSolver();
        counter = new FailPerPropagator(solver.getCstrs(), solver);
        pid2ari = new TIntObjectHashMap<IStateInt>();
        pid2arity = new TIntIntHashMap(10, 0.5F, -1, -1);
        for (int i = 0; i < variables.length; i++) {
            variables[i].addMonitor(this);
            Propagator[] props = variables[i].getPropagators();
            for (int j = 0; j < props.length; j++) {
                pid2ari.putIfAbsent(props[j].getId(), solver.getEnvironment().makeInt(props[j].arity()));
            }
        }
        random = new Random(seed);
    }

    @Override
    public IntVar[] getScope() {
        return variables;
    }

    @Override
    public boolean hasNext() {
        int idx = 0;
        for (; idx < variables.length && variables[idx].getDomainSize() == 1; idx++) {
        }
        return idx < variables.length;
    }

    @Override
    public void advance() {
        pid2arity.clear();
        small_idx = -1;
        long _d1 = Integer.MAX_VALUE;
        long _d2 = 0;
        for (int idx = 0; idx < variables.length; idx++) {
            int dsize = variables[idx].getDomainSize();
            if (dsize > 1) {
                int degree = variables[idx].getNbProps();
                int weight = weight(variables[idx]);
                long c1 = dsize * _d2;
                long c2 = _d1 * degree * weight;
                if (c1 < c2 || (c1 == c2 && random.nextBoolean())) {
                    _d1 = dsize;
                    _d2 = degree * weight;
                    small_idx = idx;
                }
            }
        }
        // swap with the last index
    }

    private int weight(IntVar v) {
        int w = 1;
        Propagator[] propagators = v.getPropagators();
        for (int p = 0; p < propagators.length; p++) {
            Propagator prop = propagators[p];
            int pid = prop.getId();
            if (pid2arity.get(pid) > 1) {
                w += counter.getFails(prop);
            } else {
                int a = pid2ari.get(pid).get();
                pid2arity.put(pid, a);
                if (a > 1) {
                    w += counter.getFails(prop);
                }
            }
        }
        return w;
    }

    @Override
    public IntVar getVariable() {
        return variables[small_idx];
    }

    @Override
    public void onUpdate(IntVar var, EventType evt) {
        if (evt == EventType.INSTANTIATE) {
            Propagator[] props = var.getPropagators();
            for (int i = 0; i < props.length; i++) {
                int pid = props[i].getId();
                pid2ari.get(pid).add(-1);
            }
        }
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        throw new SolverException("DomOverWDeg does not modify variables on IVariableMonitor.onUpdate.\n" +
                "So it cannot explain value removals.");
    }

}
