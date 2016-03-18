/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.search.loop.monitors.FailPerPropagator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.IntMap;

/**
 * Implementation of DowOverWDeg[1].
 *
 * [1]: F. Boussemart, F. Hemery, C. Lecoutre, and L. Sais, Boosting Systematic Search by Weighting Constraints, ECAI-04.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/12
 */
public class DomOverWDeg extends AbstractStrategy<IntVar>{

    /**
     * Failure per propagators counter
     */
    FailPerPropagator counter;

    /**
     * Kind of duplicate of pid2ari to limit calls of backtrackable objects
     */
    IntMap pid2arity;

    /**
     * Temporary. Stores index of variables with the same (best) score
     */
    TIntList bests;

    /**
     * Randomness to break ties
     */
    java.util.Random random;

    /**
     * The way value is selected for a given variable
     */
    IntValueSelector valueSelector;

    /**
     * Creates a DomOverWDeg variable selector
     *
     * @param variables     decision variables
     * @param seed          seed for breaking ties randomly
     * @param valueSelector a value selector
     */
    public DomOverWDeg(IntVar[] variables, long seed, IntValueSelector valueSelector) {
        super(variables);
        Model model = variables[0].getModel();
        counter = new FailPerPropagator(model.getCstrs(), model);
        pid2arity = new IntMap(model.getCstrs().length * 3 / 2 + 1, -1);
        bests = new TIntArrayList();
        this.valueSelector = valueSelector;
        random = new java.util.Random(seed);
    }


    @Override
    public Decision<IntVar> computeDecision(IntVar variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        int currentVal = valueSelector.selectValue(variable);
        return variable.getModel().getSolver().getDecisionPath().makeIntDecision(variable, DecisionOperator.int_eq, currentVal);
    }

    @Override
    public Decision<IntVar> getDecision() {
        IntVar best = null;
        bests.clear();
        pid2arity.clear();
        long _d1 = Integer.MAX_VALUE;
        long _d2 = 0;
        for (int idx = 0; idx < vars.length; idx++) {
            int dsize = vars[idx].getDomainSize();
            if (dsize > 1) {
                int weight = weight(vars[idx]);
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
        for (Propagator prop : v.getPropagators()) {
            int pid = prop.getId();
            // if the propagator has been already evaluated
            if (pid2arity.get(pid) > -1) {
                w += counter.getFails(prop);
            } else {
                // the arity of this propagator is not yet known
                int futVars = prop.arity();
                assert futVars > -1;
                pid2arity.put(pid, futVars);
                if (futVars > 1) {
                    w += counter.getFails(prop);
                }
            }
        }
        return w;
    }
}
