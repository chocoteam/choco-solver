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
package solver.constraints.propagators.nary.alldifferent;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * Propagator for AllDifferent that only reacts on instantiation
 *
 * @author Charles Prud'homme
 */
public class PropAllDiffInst extends Propagator<IntVar> {

    private final int n;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AllDifferent constraint for integer variables
     * enables to control the cardinality of the matching
     *
     * @param vars
     * @param constraint
     * @param sol
     */
    public PropAllDiffInst(IntVar[] vars, Constraint constraint, Solver sol) {
        super(vars, sol, constraint, PropagatorPriority.LINEAR, true);
        n = vars.length;
    }


    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask;
    }

    @Override
    public int getPropagationConditions() {
        return EventType.FULL_PROPAGATION.mask;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropAllDiffInst(");
        int i = 0;
        for (; i < Math.min(4, n); i++) {
            st.append(vars[i].getName()).append(", ");
        }
        if (i < n - 2) {
            st.append("...,");
        }
        st.append(vars[n - 1].getName()).append(")");
        return st.toString();
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int v = 0; v < n; v++) {
            if (vars[v].instantiated()) {
                int val = vars[v].getValue();
                for (int i = 0; i < n; i++) {
                    if (i != v) {
                        vars[i].removeValue(val, this);
                    }
                }
            }
        }
    }

    @Override
    public boolean advise(int idxVarInProp, int mask) {
        return super.advise(idxVarInProp, mask);
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        int val = vars[varIdx].getValue();
        for (int i = 0; i < n; i++) {
            if (i != varIdx) {
                vars[i].removeValue(val, this);
            }
        }
    }


    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (vars[i].getValue() == vars[j].getValue()) {
                        return ESat.FALSE;
                    }
                }
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
