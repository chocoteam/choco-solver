/**
 *  Copyright (c) 2010, Ecole des Mines de Nantes
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
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.views.IView;

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
    public PropElement(IntVar v0, int[] values, IntVar v1, int offset, IEnvironment environment,
                       IntConstraint constraint) {
        super(ArrayUtils.toArray(v0, v1), environment, constraint, PropagatorPriority.BINARY, false);
        this.lval = values;
        this.cste = offset;
    }

    @Override
    public int getPropagationConditions() {
//        if (idx == 0)
        return EventType.INSTANTIATE.mask + EventType.REMOVE.mask;
//        else return IntVarEvent.REMVAL_MASK;
    }


    protected void updateValueFromIndex() throws ContradictionException {
        int minVal = Integer.MAX_VALUE;
        int maxVal = Integer.MIN_VALUE;
        DisposableIntIterator iter = this.vars[0].getIterator();
        for (; iter.hasNext();) {
            int index = iter.next();
            if (minVal > this.lval[index - cste]) minVal = this.lval[index - cste];
            if (maxVal < this.lval[index - cste]) maxVal = this.lval[index - cste];
        }
        iter.dispose();
        this.vars[1].updateLowerBound(minVal, this);
        this.vars[1].updateUpperBound(maxVal, this);

        // todo : <hcambaza> : why it does not perform AC on the value variable ?
    }

    protected void updateIndexFromValue() throws ContradictionException {
        boolean hasChange;
        do {

            int minFeasibleIndex = Math.max(cste, this.vars[0].getLB());
            int maxFeasibleIndex = Math.min(this.vars[0].getUB(), lval.length - 1 + cste);

            if (minFeasibleIndex > maxFeasibleIndex) {
                ContradictionException.throwIt(this, null, "feasible index is incoherent");
            }
            while ((this.vars[0].contains(minFeasibleIndex))
                    && !(this.vars[1].contains(lval[minFeasibleIndex - this.cste]))) {
                minFeasibleIndex++;
            }
            hasChange = this.vars[0].updateLowerBound(minFeasibleIndex, this);

            while ((this.vars[0].contains(maxFeasibleIndex))
                    && !(this.vars[1].contains(lval[maxFeasibleIndex - this.cste]))) {
                maxFeasibleIndex--;
            }
            hasChange |= this.vars[0].updateUpperBound(maxFeasibleIndex, this);

            if (this.vars[0].hasEnumeratedDomain()) {
                for (int i = minFeasibleIndex + 1; i <= maxFeasibleIndex - 1; i++) {
                    if (this.vars[0].contains(i) && !(this.vars[1].contains(this.lval[i - this.cste])))
                        hasChange |= this.vars[0].removeValue(i, this);
                }
            }
        } while (hasChange && !this.vars[1].getDomain().isEnumerated());
    }

    void awakeOnInst(int index) throws ContradictionException {
        if (index == 0) {
            this.vars[1].instantiateTo(this.lval[this.vars[0].getValue() - this.cste], this);
        }
    }

    void awakeOnRem(int index) throws ContradictionException {
        if (index == 0) {
            this.updateValueFromIndex();
        } else {
            this.updateIndexFromValue();
        }
    }

    @Override
    public void propagate() throws ContradictionException {
        this.updateIndexFromValue();
        this.updateValueFromIndex();
    }

    @Override
    public void propagateOnView(IView<IntVar> view, int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            awakeOnInst(varIdx);
        }
        if (EventType.isRemove(mask)) {
            awakeOnRem(varIdx);
        }
    }

    @Override
    public ESat isEntailed() {
        if (this.vars[1].instantiated()) {
            boolean allVal = true;
            boolean oneVal = false;
            DisposableIntIterator iter = this.vars[0].getIterator();
            for (; iter.hasNext();) {
                int val = iter.next();
                boolean b = (val - this.cste) >= 0
                        && (val - this.cste) < this.lval.length
                        && this.lval[val - this.cste] == this.vars[1].getValue();
                allVal &= b;
                oneVal |= b;
            }
            iter.dispose();
            if (allVal) {
                return ESat.TRUE;
            }
            if (oneVal) {
                return ESat.UNDEFINED;
            }
        } else {
            DisposableIntIterator iter = this.vars[0].getIterator();
            while (iter.hasNext()) {
                int val = iter.next();
                if ((val - this.cste) >= 0 &&
                        (val - this.cste) < this.lval.length) {
                    if (this.vars[1].contains(this.lval[val - this.cste])) {
                        iter.dispose();
                        return ESat.UNDEFINED;

                    }
                }
            }
            iter.dispose();
        }
        return ESat.FALSE;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append(this.vars[1].getName()).append(" = ");
        sb.append(" <");
        int i = 0;
        for (; i < Math.max(this.lval.length - 1, 5); i++) {
            sb.append(this.lval[i]).append(", ");
        }
        if (i == 5 && this.lval.length - 1 > 5) sb.append("..., ");
        sb.append(this.lval[lval.length - 1]);
        sb.append('[').append(this.vars[0].getName()).append(']');
        return sb.toString();
    }
}
