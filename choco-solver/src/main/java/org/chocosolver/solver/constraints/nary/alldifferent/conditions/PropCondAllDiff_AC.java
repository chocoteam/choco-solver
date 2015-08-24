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
package org.chocosolver.solver.constraints.nary.alldifferent.conditions;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffAC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;

/**
 * Propagator for AllDifferent AC constraint for integer variables
 * subject to conditions (e.g. allDifferent_except_0)
 * AllDiff only applies on the subset of variables satisfying the given condition
 *
 * @author Jean-Guillaume Fages
 */
public class PropCondAllDiff_AC extends Propagator<IntVar> {

    private Condition condition;
    private AlgoAllDiffAC filter;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AllDifferent constraint for integer variables
     * Holds only on the subset of variables satisfying the given condition
     *
     * @param variables array of integer variables
     * @param condition defines the subset of variables which is considered by the
     *                  alldifferent constraint
     */
    public PropCondAllDiff_AC(IntVar[] variables, Condition condition) {
        super(variables, PropagatorPriority.QUADRATIC, false);
        this.condition = condition;
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int nb = 0;
        for (IntVar v : vars) {
            if (condition.holdOnVar(v)) {
                nb++;
            }
        }
        IntVar[] vs = new IntVar[nb];
        for (IntVar v : vars) {
            if (condition.holdOnVar(v)) {
                nb--;
                vs[nb] = v;
            }
        }
        filter = new AlgoAllDiffAC(vs, this);
        filter.propagate();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************
    @Override
    public ESat isEntailed() {
        int nbInst = 0;
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].isInstantiated()) {
                nbInst++;
                if (condition.holdOnVar(vars[i])) {
                    for (int j = i + 1; j < vars.length; j++) {
                        if (condition.holdOnVar(vars[j]))
                            if (vars[j].isInstantiated() && vars[i].getValue() == vars[j].getValue()) {
                                return ESat.FALSE;
                            }
                    }
                }
            }
        }
        if (nbInst == vars.length) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
