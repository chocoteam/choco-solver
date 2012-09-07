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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 18/06/12
 * Time: 18:32
 */

package solver.constraints.propagators.nary.sum;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * Constraint that state that the sum of boolean variables vars is equal to the integer variable sum
 * Works incrementally in O(1) per instantiation event
 */
public class PropBoolSum extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    IntVar sum;
    int n;
    IStateInt min, max;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Constraint that state that the sum of boolean variables vars is equal to the integer variable sum
     * Works in O(1) per instantiation event
     *
     * @param vars
     * @param sum
     * @param solver
     * @param intVarPropagatorConstraint
     */
    protected PropBoolSum(BoolVar[] vars, IntVar sum, Solver solver, Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(ArrayUtils.append(vars, new IntVar[]{sum}), solver, intVarPropagatorConstraint, PropagatorPriority.UNARY, false);
        this.sum = sum;
        n = vars.length;
        min = environment.makeInt();
        max = environment.makeInt();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int lb = 0;
        int ub = 0;
        for (int i = 0; i < n; i++) {
            lb += vars[i].getLB();
            ub += vars[i].getUB();
        }
        min.set(lb);
        max.set(ub);
        filter();
    }

    private void filter() throws ContradictionException {
        int lb = min.get();
        int ub = max.get();
        sum.updateLowerBound(lb, aCause);
        sum.updateUpperBound(ub, aCause);
        if (lb != ub && sum.instantiated()) {
            if (sum.getValue() == lb) {
                for (int i = 0; i < n; i++) {
                    if (!vars[i].instantiated()) {
                        vars[i].instantiateTo(0, aCause);
                    }
                }
                setPassive();
            }
            if (sum.getValue() == ub) {
                for (int i = 0; i < n; i++) {
                    if (!vars[i].instantiated()) {
                        vars[i].instantiateTo(1, aCause);
                    }
                }
                setPassive();
            }
        }
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < n) {
            if (vars[idxVarInProp].getValue() == 1) {
                min.set(min.get() + 1);
            } else {
                max.set(max.get() - 1);
            }
        }
        filter();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask;
    }

    @Override
    public ESat isEntailed() {
        int lb = 0;
        int ub = 0;
        for (int i = 0; i < n; i++) {
            lb += vars[i].getLB();
            ub += vars[i].getUB();
        }
        if (lb > sum.getUB() || ub < sum.getLB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}