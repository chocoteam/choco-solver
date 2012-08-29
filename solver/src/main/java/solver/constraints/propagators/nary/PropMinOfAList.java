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
package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Arnaud Malapert
 * @author Charles Prud'homme
 * @since 18/03/12
 */
public class PropMinOfAList extends Propagator<IntVar> {

    /**
     * Index of the minimum variable.
     */
    public static final int MIN_INDEX = 0;
    /**
     * First index of the variables among which the minimum should be chosen.
     */
    public static final int VARS_OFFSET = 1;

    /**
     * Index of the minimum variable.
     */
    protected final IStateInt indexOfMinimumVariable;


    public PropMinOfAList(IntVar[] vars, Solver solver, Constraint<IntVar, Propagator<IntVar>> constraint) {
        super(vars, solver, constraint, PropagatorPriority.LINEAR, false);
        indexOfMinimumVariable = solver.getEnvironment().makeInt(-1);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int nbVars = vars.length;
        IntVar minVar = vars[MIN_INDEX];
        minVar.updateLowerBound(minInf(), this);
        minVar.updateUpperBound(minSup(), this);
        int minValue = minVar.getLB();
        for (int i = VARS_OFFSET; i < nbVars; i++) {
            vars[i].updateLowerBound(minValue, this);
        }
//        onlyOneMaxCandidatePropagation();
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idx, int mask) throws ContradictionException {
		if(true){
			forcePropagate(EventType.FULL_PROPAGATION);
			return;
		}
        if (EventType.isInstantiate(mask)) {
            if (idx >= VARS_OFFSET) { // Variable in the list
                IntVar minVar = vars[MIN_INDEX];
                minVar.updateLowerBound(minInf(), this);
                minVar.updateUpperBound(minSup(), this);
            } else { // Maximum variable
                int nbVars = vars.length;
                int minValue = vars[MIN_INDEX].getLB();
                for (int i = VARS_OFFSET; i < nbVars; i++) {
                    vars[i].updateLowerBound(minValue, this);
                }
                onlyOneMaxCandidatePropagation();
            }
        } else if (EventType.isBound(mask)) {
            if (EventType.isInclow(mask)) {
                if (idx >= VARS_OFFSET) { // Variable in the list
                    vars[MIN_INDEX].updateLowerBound(minInf(), this);
                    onlyOneMaxCandidatePropagation();
                } else { // Minimum variable
                    int nbVars = vars.length;
                    int minVal = vars[MIN_INDEX].getLB();
                    for (int i = VARS_OFFSET; i < nbVars; i++) {
                        vars[i].updateLowerBound(minVal, this);
                    }
                }
            }
            if (EventType.isDecupp(mask)) {
                if (idx >= VARS_OFFSET) { // Variable in the list
                    vars[MIN_INDEX].updateUpperBound(minSup(), this);
                } else { // Maximum variable
                    onlyOneMaxCandidatePropagation();
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int minInf = vars[MIN_INDEX].getLB();
        int minSup = vars[MIN_INDEX].getUB();

        int cptIn = 0;
        int cptBelow = 0;
        IntVar tmp;
        for (int i = VARS_OFFSET; i < vars.length; i++) {
            tmp = vars[i];
            int inf = tmp.getLB();
            int sup = tmp.getUB();
            if (inf == minInf
                    && minSup == sup
                    && sup == inf) {
                cptIn++;
            } else if (sup < minInf) {
                return ESat.FALSE;
            } else if (inf > minSup) {
                cptBelow++;
            }
        }
        if (cptBelow == vars.length - 1) {
            return ESat.FALSE;
        }
        if (cptIn > 0) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PropMin ");
        sb.append(vars[MIN_INDEX]).append(" = min({");
        for (int i = VARS_OFFSET; i < vars.length; i++) {
            if (i > VARS_OFFSET) {
                sb.append(", ");
            }
            sb.append(vars[i]);
        }
        sb.append("})");
        return sb.toString();

    }


    ///////////////////////////////

    protected final int minInf() {
        int nbVars = vars.length;
        int min = Integer.MAX_VALUE;
        for (int i = VARS_OFFSET; i < nbVars; i++) {
            int val = vars[i].getLB();
            if (val < min) {
                min = val;
            }
        }
        return min;
    }

    protected final int minSup() {
        int nbVars = vars.length;
        int min = Integer.MAX_VALUE;
        for (int i = VARS_OFFSET; i < nbVars; i++) {
            int val = vars[i].getUB();
            if (val < min) {
                min = val;
            }
        }
        return min;
    }

    /**
     * If only one candidate to be the min of the list, some additionnal
     * propagation can be performed (as in usual x == y constraint).
     */
    protected void onlyOneMaxCandidatePropagation() throws ContradictionException {
        int nbVars = vars.length;
        IntVar minVar = vars[MIN_INDEX];
        int idx = indexOfMinimumVariable.get();
        if (idx == -1) {
            int minMin = Integer.MAX_VALUE, minMinIdx = -1;
            int minMin2 = Integer.MAX_VALUE, minMin2Idx = -1;
            for (int i = VARS_OFFSET; i < nbVars; i++) {
                int val = vars[i].getLB();
                if (val <= minMin) {
                    minMin2 = minMin;
                    minMin2Idx = minMinIdx;
                    minMin = val;
                    minMinIdx = i;
                } else if (val < minMin2) {
                    minMin2 = val;
                    minMin2Idx = i;
                }
            }
            if (minMin2 > minVar.getUB()) {
                this.indexOfMinimumVariable.set(minMinIdx);
                idx = minMin2Idx;
            }
        }
        if (idx != -1) {
            minVar.updateUpperBound(vars[idx].getUB(), this);
            vars[idx].updateUpperBound(minVar.getUB(), this);
        }
    }
}
