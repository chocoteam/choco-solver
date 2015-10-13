/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.search.loop.monitors.FailPerPropagator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.fast.FastDecision;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.PoolManager;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/12
 */
public class DomOverWDeg extends AbstractStrategy<IntVar> implements IVariableMonitor<IntVar> {

    /* list of variables */
    IntVar[] variables;

    FailPerPropagator counter;

    TIntObjectHashMap<IStateInt> pid2ari;
    TIntIntHashMap pid2arity;
    TIntList bests;

    java.util.Random random;

    PoolManager<FastDecision> decisionPool;

    IntValueSelector valueSelector;

    public DomOverWDeg(IntVar[] variables, long seed, IntValueSelector valueSelector) {
        super(variables);
        this.variables = variables.clone();
        Solver solver = variables[0].getSolver();
        counter = new FailPerPropagator(solver.getCstrs(), solver);
        pid2ari = new TIntObjectHashMap<>();
        pid2arity = new TIntIntHashMap(10, 0.5F, -1, -1);
        bests = new TIntArrayList();
        this.valueSelector = valueSelector;
        decisionPool = new PoolManager<>();
        random = new java.util.Random(seed);
    }

    @Override
    public boolean init(){
        IEnvironment env = vars[0].getSolver().getEnvironment();
        for (int i = 0; i < variables.length; i++) {
            variables[i].addMonitor(this);
            Propagator[] props = variables[i].getPropagators();
            for (int j = 0; j < props.length; j++) {
                pid2ari.putIfAbsent(props[j].getId(), env.makeInt(props[j].arity()));
            }
        }
        return true;
    }


    @Override
    public Decision<IntVar> computeDecision(IntVar variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        int currentVal = valueSelector.selectValue(variable);
        FastDecision currrent = decisionPool.getE();
        if (currrent == null) {
            currrent = new FastDecision(decisionPool);
        }
        currrent.set(variable, currentVal, DecisionOperator.int_eq);
        return currrent;
    }

    @Override
    public Decision<IntVar> getDecision() {
        IntVar best = null;
        bests.clear();
        pid2arity.clear();
        long _d1 = Integer.MAX_VALUE;
        long _d2 = 0;
        for (int idx = 0; idx < vars.length; idx++) {
            int dsize = variables[idx].getDomainSize();
            if (dsize > 1) {
                int weight = weight(variables[idx]);
                long c1 = dsize * _d2;
                long c2 = _d1 * weight;
                if (c1 < c2) {
                    bests.clear();
                    bests.add(idx);
                    _d1 = dsize;
                    _d2 = weight;
                } else if (c1 == c2) {
                    bests.add(idx);
                }
            }
        }
        if (bests.size() > 0) {
            int currentVar = bests.get(random.nextInt(bests.size()));
            best = vars[currentVar];
        }
        return computeDecision(best);
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
                if (pid2ari.get(pid) == null) {
                    pid2ari.putIfAbsent(prop.getId(), v.getSolver().getEnvironment().makeInt(prop.arity()));
                }
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
    public void onUpdate(IntVar var, IEventType evt) {
        if (evt == IntEventType.INSTANTIATE) {
            Propagator[] props = var.getPropagators();
            for (int i = 0; i < props.length; i++) {
                int pid = props[i].getId();
				if (pid2ari.get(pid) == null) {
					pid2ari.putIfAbsent(pid, var.getSolver().getEnvironment().makeInt(props[i].arity()));
				}
                pid2ari.get(pid).add(-1);
            }
        }
    }

}
