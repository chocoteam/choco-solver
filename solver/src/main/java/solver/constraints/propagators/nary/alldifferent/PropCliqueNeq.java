/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.nary.alldifferent;

import choco.annotations.PropAnn;
import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.util.ArrayDeque;
import java.util.Deque;

import static choco.annotations.PropAnn.Status.*;

/**
 * Based on: </br>
 * "A Fast and Simple Algorithm for Bounds Consistency of the AllDifferent Constraint"</br>
 * A. Lopez-Ortiz, CG. Quimper, J. Tromp, P.van Beek
 * <br/>
 *
 * @author Xavier Lorca
 * @since 07/02/11
 */
@PropAnn(tested = {BENCHMARK, CORRECTION, CONSISTENCY})
public class PropCliqueNeq extends Propagator<IntVar> {


    public PropCliqueNeq(IntVar[] vars, Solver solver, Constraint<IntVar, Propagator<IntVar>> constraint) {
        super(vars, solver, constraint, PropagatorPriority.LINEAR, true);
        int n = vars.length;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        //Principle : if v0 is instantiated and v1 is enumerated, then awakeOnInst(0) performs all needed pruning
//        Otherwise, we must check if we can remove the value from v1 when the bounds has changed.
        if (vars[vIdx].hasEnumeratedDomain()) {
            return EventType.INSTANTIATE.mask;
        }
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public void propagate(int eventmask) throws ContradictionException {
        int left, right;
        for (int j = 0; j < vars.length; j++) {
            left = right = Integer.MIN_VALUE;
            for (int i = 0; i < j; i++) {
                if (vars[i].instantiated()) {
                    int val = vars[i].getValue();
                    if (val == right + 1) {
                        right = val;
                    } else {
                        vars[j].removeInterval(left, right, this);
                        left = right = val;
                    }
                }
            }
            for (int i = j + 1; i < vars.length; i++) {
                if (vars[i].instantiated()) {
                    int val = vars[i].getValue();
                    if (val == right + 1) {
                        right = val;
                    } else {
                        vars[j].removeInterval(left, right, this);
                        left = right = val;
                    }
                }
            }
            vars[j].removeInterval(left, right, this);
        }
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            checkInst(idxVarInProp);
        } else if (EventType.isInclow(mask) && EventType.isDecupp(mask)) {
            checkBounds(idxVarInProp);
        } else if (EventType.isInclow(mask)) {
            checkLB(idxVarInProp);
        } else if (EventType.isDecupp(mask)) {
            checkUB(idxVarInProp);
        }
    }

    protected void checkInst(int i) throws ContradictionException {
        Deque<IntVar> modified = new ArrayDeque<IntVar>();
        modified.push(vars[i]);
        while (!modified.isEmpty()) {
            IntVar cur = modified.pop();
            int valCur = cur.getValue();
            for (IntVar toCheck : vars) {
                if (toCheck != cur && toCheck.contains(valCur)) {
                    toCheck.removeValue(valCur, this);
                    if (toCheck.instantiated()) {
                        modified.push(toCheck);
                    }
                }
            }
        }
    }

    protected void checkBounds(int i) throws ContradictionException {
        int lb = vars[i].getLB();
        int ub = vars[i].getUB();
        for (int j = 0; j < vars.length; j++) {
            if (j != i && vars[j].instantiated()) {
                int val = vars[j].getValue();
                if (val == lb) {
                    vars[i].updateLowerBound(val + 1, this);
                }
                if (val == ub) {
                    vars[i].updateUpperBound(val - 1, this);
                }
            }
        }
    }

    protected void checkLB(int i) throws ContradictionException {
        int lb = vars[i].getLB();
        for (int j = 0; j < vars.length; j++) {
            if (j != i && vars[j].instantiated()) {
                int val = vars[j].getValue();
                if (val == lb) {
                    vars[i].updateLowerBound(val + 1, this);
                }
            }
        }
    }

    protected void checkUB(int i) throws ContradictionException {
        int ub = vars[i].getUB();
        for (int j = 0; j < vars.length; j++) {
            if (j != i && vars[j].instantiated()) {
                int val = vars[j].getValue();
                if (val == ub) {
                    vars[i].updateUpperBound(val - 1, this);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (this.isCompletelyInstantiated()) {
            for (int i = 0; i < vars.length; i++) {
                for (int j = i + 1; j < vars.length; j++) {
                    if (vars[i].getValue() == vars[j].getValue()) {
                        return ESat.FALSE;
                    }
                }
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropCliqueNeq(");
        int i = 0;
        for (; i < Math.min(4, vars.length); i++) {
            st.append(vars[i].getName()).append(", ");
        }
        if (i < vars.length - 2) {
            st.append("...,");
        }
        st.append(vars[vars.length - 1].getName()).append(")");
        return st.toString();
    }
}
