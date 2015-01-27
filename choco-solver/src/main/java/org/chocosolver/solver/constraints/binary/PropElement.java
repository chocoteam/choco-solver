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
package org.chocosolver.solver.constraints.binary;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.structure.Operation;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.arlil.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * VALUE = TABLE[INDEX]
 * <br/>
 * Bound consistency.
 *
 * @author Hadrien Cambazard
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 02/02/12
 */
public class PropElement extends Propagator<IntVar> {

    int[] lval;
    int cste;

    /**
     * To indicate the ordering of the index value
     */
    public static enum Sort {
        // Values are unordered
        none,
        // Values are in the increasing order
        asc,
        // Values are in the decreasing order
        desc,
        // Let the constraint detect the ordering, if any
        detect
    }

    private Sort s;

    public PropElement(IntVar value, int[] values, IntVar index, int offset, Sort s) {
        super(ArrayUtils.toArray(value, index), PropagatorPriority.BINARY, true);
        this.lval = values;
        this.cste = offset;
        this.s = s;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        this.vars[1].updateLowerBound(cste, aCause);
        this.vars[1].updateUpperBound(lval.length - 1 + cste, aCause);
        filter(false, 2);
    }

    protected void filter(boolean startWithIndex, int nbRules) throws ContradictionException {
        boolean run;
        int nbR = 0;
        do {
            if (startWithIndex) {
                run = updateIndexFromValue();
            } else {
                run = updateValueFromIndex();
            }
            startWithIndex ^= true;
            nbR++;
        } while (run || nbR < nbRules);
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (IntEventType.isInstantiate(mask)) {
            if (varIdx == 1) {  // INDEX (should be only that)
                this.vars[0].instantiateTo(this.lval[this.vars[1].getValue() - this.cste], aCause);
                this.setPassive();
            }
        }
        filter(true, varIdx == 0 ? 1 : 2);
    }

    @Override
    public ESat isEntailed() {
        if (this.vars[0].isInstantiated()) {
            boolean allVal = true;
            boolean oneVal = false;
            int ub = this.vars[1].getUB();
            for (int val = this.vars[1].getLB(); val <= ub; val = this.vars[1].nextValue(val)) {
                boolean b = (val - this.cste) >= 0
                        && (val - this.cste) < this.lval.length
                        && this.lval[val - this.cste] == this.vars[0].getValue();
                allVal &= b;
                oneVal |= b;
            }
            if (allVal) {
                return ESat.TRUE;
            }
            if (oneVal) {
                return ESat.UNDEFINED;
            }
        } else {
            int ub = this.vars[1].getUB();
            for (int val = this.vars[1].getLB(); val <= ub; val = this.vars[1].nextValue(val)) {
                if ((val - this.cste) >= 0 &&
                        (val - this.cste) < this.lval.length) {
                    if (this.vars[0].contains(this.lval[val - this.cste])) {
                        return ESat.UNDEFINED;
                    }
                }
            }
        }
        return ESat.FALSE;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("nth(").append(this.vars[0]).append(" = ");
        sb.append(" <");
        int i = 0;
        for (; i < Math.min(this.lval.length - 1, 5); i++) {
            sb.append(this.lval[i]).append(", ");
        }
        if (i == 5 && this.lval.length - 1 > 5) sb.append("..., ");
        sb.append(this.lval[lval.length - 1]);
        sb.append("> [").append(this.vars[1]).append("])");
        return sb.toString();
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        return ruleStore.addPropagatorActivationRule(this)
                | ruleStore.addFullDomainRule((var == vars[0]) ? vars[1] : vars[0]);
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.vars[0].duplicate(solver, identitymap);
            IntVar X = (IntVar) identitymap.get(this.vars[0]);
            this.vars[1].duplicate(solver, identitymap);
            IntVar Y = (IntVar) identitymap.get(this.vars[1]);

            identitymap.put(this, new PropElement(X, this.lval, Y, this.cste, this.s));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean updateValueFromIndex() throws ContradictionException {
        boolean hasChanged;

        if (s == Sort.desc) {
            hasChanged = this.vars[0].updateLowerBound(this.lval[vars[1].getUB() - cste], aCause);
            hasChanged |= this.vars[0].updateUpperBound(this.lval[vars[1].getLB() - cste], aCause);
        } else if (s == Sort.asc) {
            hasChanged = this.vars[0].updateLowerBound(this.lval[vars[1].getLB() - cste], aCause);
            hasChanged |= this.vars[0].updateUpperBound(this.lval[vars[1].getUB() - cste], aCause);
        } else {
            int minVal = Integer.MAX_VALUE;
            int maxVal = Integer.MIN_VALUE;
            DisposableValueIterator iter = this.vars[1].getValueIterator(true);
            boolean isDsc = true;
            boolean isAsc = true;
            int prev = this.lval[vars[1].getLB() - cste];
            try {
                while (iter.hasNext()) {
                    int index = iter.next();
                    int val = this.lval[index - cste];
                    if (minVal > val) {
                        minVal = val;
                    }
                    if (maxVal < val) {
                        maxVal = val;
                    }
                    if (s == Sort.detect) {
                        if (val > prev) {
                            isDsc = false;
                        }
                        if (val < prev) {
                            isAsc = false;
                        }
                        prev = val;
                    }
                }
                if (s == Sort.detect) {
                    IEnvironment environment = solver.getEnvironment();
                    if (isDsc) {
                        s = Sort.desc;
                        environment.save(new Operation() {
                            @Override
                            public void undo() {
                                s = Sort.detect;
                            }
                        });
                    } else if (isAsc) {
                        s = Sort.asc;
                        environment.save(new Operation() {
                            @Override
                            public void undo() {
                                s = Sort.detect;
                            }
                        });
                    } else {
                        s = Sort.none;
                        environment.save(new Operation() {
                            @Override
                            public void undo() {
                                s = Sort.detect;
                            }
                        });
                    }
                }
                hasChanged = this.vars[0].updateLowerBound(minVal, aCause);
                hasChanged |= this.vars[0].updateUpperBound(maxVal, aCause);
            } finally {
                iter.dispose();
            }
        }
        return hasChanged;
    }

    protected boolean updateIndexFromValue() throws ContradictionException {
        boolean hasChanged;
        int minFeasibleIndex = Math.max(cste, this.vars[1].getLB());
        int maxFeasibleIndex = Math.min(this.vars[1].getUB(), lval.length - 1 + cste);

        if (minFeasibleIndex > maxFeasibleIndex) {
            contradiction(null, "impossible");
        }

        while ((this.vars[1].contains(minFeasibleIndex))
                && !(this.vars[0].contains(lval[minFeasibleIndex - this.cste])))
            minFeasibleIndex++;
        hasChanged = this.vars[1].updateLowerBound(minFeasibleIndex, aCause);

        while ((this.vars[1].contains(maxFeasibleIndex))
                && !(this.vars[0].contains(lval[maxFeasibleIndex - this.cste])))
            maxFeasibleIndex--;
        hasChanged |= this.vars[1].updateUpperBound(maxFeasibleIndex, aCause);

        if (this.vars[1].hasEnumeratedDomain()) {
            for (int i = minFeasibleIndex + 1; i <= maxFeasibleIndex - 1; i++) {
                if (this.vars[1].contains(i) && !(this.vars[0].contains(this.lval[i - this.cste])))
                    hasChanged |= this.vars[1].removeValue(i, aCause);
            }
        }
        return hasChanged;
    }

}
