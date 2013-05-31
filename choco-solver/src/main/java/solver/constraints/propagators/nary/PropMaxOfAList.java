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

import memory.IStateInt;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;

/**
 * <br/>
 *
 * @author Arnaud Malapert
 * @author Charles Prud'homme
 * @since 18/03/12
 */
public class PropMaxOfAList extends Propagator<IntVar> {

    /**
     * Index of the maximum variable.
     */
    public static final int MAX_INDEX = 0;
    /**
     * First index of the variables among which the maximum should be chosen.
     */
    public static final int VARS_OFFSET = 1;

    /**
     * Index of the maximum variable.
     */
    protected final IStateInt indexOfMaximumVariable;


    public PropMaxOfAList(IntVar[] variables) {
        super(variables, PropagatorPriority.LINEAR, false, true);
        indexOfMaximumVariable = solver.getEnvironment().makeInt(-1);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int nbVars = vars.length;
        IntVar maxVar = vars[MAX_INDEX];
        maxVar.updateLowerBound(maxInf(), aCause);
        maxVar.updateUpperBound(maxSup(), aCause);
        int maxValue = maxVar.getUB();
        for (int i = VARS_OFFSET; i < nbVars; i++) {
            vars[i].updateUpperBound(maxValue, aCause);
        }
//        onlyOneMaxCandidatePropagation();
    }

    @Override
    public void propagate(int idx, int mask) throws ContradictionException {
        if (true) {
            forcePropagate(EventType.FULL_PROPAGATION);
            return;
        }
        if (EventType.isInstantiate(mask)) {
            if (idx >= VARS_OFFSET) { // Variable in the list
                IntVar maxVar = vars[MAX_INDEX];
                maxVar.updateLowerBound(maxInf(), aCause);
                maxVar.updateUpperBound(maxSup(), aCause);
            } else { // Maximum variable
                int nbVars = vars.length;
                int maxValue = vars[MAX_INDEX].getUB();
                for (int i = VARS_OFFSET; i < nbVars; i++) {
                    vars[i].updateUpperBound(maxValue, aCause);
                }
                onlyOneMaxCandidatePropagation();
            }
        } else if (EventType.isBound(mask)) {
            if (EventType.isInclow(mask)) {
                if (idx >= VARS_OFFSET) { // Variable in the list
                    vars[MAX_INDEX].updateLowerBound(maxInf(), aCause);
                } else { // Maximum variable
                    onlyOneMaxCandidatePropagation();
                }
            }
            if (EventType.isDecupp(mask)) {
                if (idx >= VARS_OFFSET) { // Variable in the list
                    vars[MAX_INDEX].updateUpperBound(maxSup(), aCause);
                    onlyOneMaxCandidatePropagation();
                } else { // Maximum variable
                    int nbVars = vars.length;
                    int maxVal = vars[MAX_INDEX].getUB();
                    for (int i = VARS_OFFSET; i < nbVars; i++) {
                        vars[i].updateUpperBound(maxVal, aCause);
                    }
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int maxInf = vars[MAX_INDEX].getLB();
        int maxSup = vars[MAX_INDEX].getUB();

        int cptIn = 0;
        int cptAbove = 0;
        IntVar tmp;
        for (int i = VARS_OFFSET; i < vars.length; i++) {
            tmp = vars[i];
            int inf = tmp.getLB();
            int sup = tmp.getUB();
            if (inf == maxInf
                    && maxSup == sup
                    && inf == sup) {
                cptIn++;
            } else if (inf > maxSup) {
                return ESat.FALSE;
            } else if (sup < maxInf) {
                cptAbove++;
            }
        }
        if (cptAbove == vars.length - 1) {
            return ESat.FALSE;
        }
        if (cptIn > 0) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PropMax ");
        sb.append(vars[MAX_INDEX]).append(" = max({");
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

    protected final int maxInf() {
        int nbVars = vars.length;
        int max = Integer.MIN_VALUE;
        for (int i = VARS_OFFSET; i < nbVars; i++) {
            int val = vars[i].getLB();
            if (val > max) {
                max = val;
            }
        }
        return max;
    }

    protected final int maxSup() {
        int nbVars = vars.length;
        int max = Integer.MIN_VALUE;
        for (int i = VARS_OFFSET; i < nbVars; i++) {
            int val = vars[i].getUB();
            if (val > max) {
                max = val;
            }
        }
        return max;
    }

    /**
     * If only one candidate to be the max of the list, some additionnal
     * propagation can be performed (as in usual x == y constraint).
     *
     * @throws solver.exception.ContradictionException
     *          exception
     */
    protected void onlyOneMaxCandidatePropagation() throws ContradictionException {
        int nbVars = vars.length;
        IntVar maxVar = vars[MAX_INDEX];
        int idx = indexOfMaximumVariable.get();
        if (idx == -1) {
            int maxMax = Integer.MIN_VALUE, maxMaxIdx = -1;
            int maxMax2 = Integer.MIN_VALUE;
            for (int i = VARS_OFFSET; i < nbVars; i++) {
                int val = vars[i].getUB();
                if (val >= maxMax) {
                    maxMax2 = maxMax;
                    maxMax = val;
                    maxMaxIdx = i;
                } else if (val > maxMax2) {
                    maxMax2 = val;
                }
            }
            if (maxMax2 < maxVar.getLB()) {
                this.indexOfMaximumVariable.set(maxMaxIdx);
                idx = maxMaxIdx;
            }
        }
        if (idx != -1) {
            maxVar.updateLowerBound(vars[idx].getLB(), aCause);
            vars[idx].updateLowerBound(maxVar.getLB(), aCause);
        }
    }
}
