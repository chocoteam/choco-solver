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

package solver.constraints.propagators.binary;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;

/**
 * VALUE = TABLE[INDEX]
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20 sept. 2010
 */
public class PropElement extends Propagator<IntVar> {

    final int[] lval;
    final int cste;

    @SuppressWarnings({"unchecked"})
    public PropElement(IntVar value, int[] values, IntVar index, int offset, Solver solver,
                       IntConstraint constraint) {
        super(ArrayUtils.toArray(value, index), solver, constraint, PropagatorPriority.BINARY, false);
        this.lval = values;
        this.cste = offset;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {   // value : need to react on removals
            return EventType.REMOVE.mask;
        } else {  // index : need to react on removals AND on instantiations
            return EventType.INSTANTIATE.mask + EventType.REMOVE.mask;
        }
    }


    protected void updateValueFromIndex(boolean repropag) throws ContradictionException {
        int minVal = Integer.MAX_VALUE;
        int maxVal = Integer.MIN_VALUE;
        int ub = vars[1].getUB();
        for (int index = vars[1].getLB(); index <= ub; index = vars[1].nextValue(index)) {
            if (minVal > this.lval[index - cste]) minVal = this.lval[index - cste];
            if (maxVal < this.lval[index - cste]) maxVal = this.lval[index - cste];
        }
        this.vars[0].updateLowerBound(minVal, this, repropag);
        this.vars[0].updateUpperBound(maxVal, this, repropag);
        // todo : <hcambaza> : why it does not perform AC on the value variable ?
        // <nj> perhaps because it is possible to have several times the same value in VALUES
    }

    protected void updateIndexFromValue(boolean repropag) throws ContradictionException {
        boolean hasChange;
        do {
            int minFeasibleIndex = Math.max(cste, this.vars[1].getLB());
            int maxFeasibleIndex = Math.min(this.vars[1].getUB(), lval.length - 1 + cste);

            if (minFeasibleIndex > maxFeasibleIndex) {
                this.contradiction(null, "feasible index is incoherent");
            }
            while ((this.vars[1].contains(minFeasibleIndex))
                    && !(this.vars[0].contains(lval[minFeasibleIndex - this.cste]))) {
                minFeasibleIndex++;
            }
            hasChange = this.vars[1].updateLowerBound(minFeasibleIndex, this, repropag);

            while ((this.vars[1].contains(maxFeasibleIndex))
                    && !(this.vars[0].contains(lval[maxFeasibleIndex - this.cste]))) {
                maxFeasibleIndex--;
            }
            hasChange |= this.vars[1].updateUpperBound(maxFeasibleIndex, this, repropag);

            if (this.vars[1].hasEnumeratedDomain()) {
                for (int i = minFeasibleIndex + 1; i <= maxFeasibleIndex - 1; i++) {
                    if (this.vars[1].contains(i) && !(this.vars[0].contains(this.lval[i - this.cste])))
                        hasChange |= this.vars[1].removeValue(i, this, repropag);
                }
            }
        } while (hasChange && !this.vars[0].hasEnumeratedDomain());
    }



    void awakeOnInst(int index) throws ContradictionException {
        if (index == 1) {  // index (should be only that)
            this.vars[0].instantiateTo(this.lval[this.vars[1].getValue() - this.cste], this, false);
            this.setPassive();
        }
    }

    void awakeOnRem(int index) throws ContradictionException {
        if (index == 0) {  // value
            this.updateIndexFromValue(true);
        } else {  // index
            this.updateValueFromIndex(true);
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        this.updateIndexFromValue(false);
        this.updateValueFromIndex(false);
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            awakeOnInst(varIdx);
        }
        if (EventType.isRemove(mask)) {
            awakeOnRem(varIdx);
        }
    }

    @Override
    public ESat isEntailed() {
        if (this.vars[0].instantiated()) {
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
        sb.append(this.vars[0].getName()).append(" = ");
        sb.append(" <");
        int i = 0;
        for (; i < Math.max(this.lval.length - 1, 5); i++) {
            sb.append(this.lval[i]).append(", ");
        }
        if (i == 5 && this.lval.length - 1 > 5) sb.append("..., ");
        sb.append(this.lval[lval.length - 1]);
        sb.append('[').append(this.vars[1].getName()).append(']');
        return sb.toString();
    }

    @Override
    public Explanation explain(Deduction d) {
        Variable reason = (d.getVar() == vars[0]) ? vars[1] : vars[0];
        Explanation explanation = new Explanation(this);
        explanation.add(reason.explain(VariableState.DOM));
        return explanation;
    }
}
