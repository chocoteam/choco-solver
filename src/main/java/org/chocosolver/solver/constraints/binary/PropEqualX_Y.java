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
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * X = Y
 * <p>
 * Ensures Arc-Consistency
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 1 oct. 2010
 */
public final class PropEqualX_Y extends Propagator<IntVar> {

    private IntVar x, y;
    // enumerated domains
    private boolean bothEnumerated;
    private IIntDeltaMonitor[] idms;
    private IntProcedure rem_proc;
    private int indexToFilter;

    public PropEqualX_Y(IntVar x, IntVar y) {
        super(ArrayUtils.toArray(x, y), PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
        if (x.hasEnumeratedDomain() && y.hasEnumeratedDomain()) {
            bothEnumerated = true;
            idms = new IIntDeltaMonitor[2];
            idms[0] = vars[0].monitorDelta(this);
            idms[1] = vars[1].monitorDelta(this);
            rem_proc = i -> vars[indexToFilter].removeValue(i, this);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vars[0].hasEnumeratedDomain() && vars[1].hasEnumeratedDomain())
            return IntEventType.all();
        else
            return IntEventType.boundAndInst();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void updateBounds() throws ContradictionException {
        while (x.updateLowerBound(y.getLB(), this) | y.updateLowerBound(x.getLB(), this)) ;
        while (x.updateUpperBound(y.getUB(), this) | y.updateUpperBound(x.getUB(), this)) ;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        updateBounds();
        // ensure that, in case of enumerated domains,  holes are also propagated
        if (bothEnumerated) {
            int ub = x.getUB();
            for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                if (!(y.contains(val))) {
                    x.removeValue(val, this);
                }
            }
            ub = y.getUB();
            for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                if (!(x.contains(val))) {
                    y.removeValue(val, this);
                }
            }
            idms[0].unfreeze();
            idms[1].unfreeze();
        }
        if (x.isInstantiated()) {
            assert (y.isInstantiated());
            // no more test should be done on the value,
            // filtering algo ensures that both are assigned to the same value
            setPassive();
        }
    }


    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        updateBounds();
        if (x.isInstantiated()) {
            assert (y.isInstantiated());
            setPassive();
        } else if (bothEnumerated) {
            indexToFilter = 1 - varIdx;
            idms[varIdx].freeze();
            idms[varIdx].forEachRemVal(rem_proc);
            idms[varIdx].unfreeze();
        }
    }

    @Override
    public ESat isEntailed() {
        if ((x.getUB() < y.getLB()) ||
                (x.getLB() > y.getUB()) ||
                x.hasEnumeratedDomain() && y.hasEnumeratedDomain() && !match()
                )
            return ESat.FALSE;
        else if (x.isInstantiated() &&
                y.isInstantiated() &&
                (x.getValue() == y.getValue()))
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    private boolean match() {
        int lb = x.getLB();
        int ub = x.getUB();
        for (; lb <= ub; lb = x.nextValue(lb)) {
            if (y.contains(lb)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "prop(" + vars[0].getName() + ".EQ." + vars[1].getName() + ")";
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        if (var.equals(x)) {
            IntEventType ievt = (IntEventType) evt;
            switch (ievt) {
                case REMOVE:
                    newrules |= ruleStore.addRemovalRule(y, value);
                    break;
                case DECUPP:
                    newrules |= ruleStore.addUpperBoundRule(y);
                    break;
                case INCLOW:
                    newrules |= ruleStore.addLowerBoundRule(y);
                    break;
                case INSTANTIATE:
                    newrules |= ruleStore.addFullDomainRule(y);
                    break;
                default: break;// nothing to do
            }
        } else if (var.equals(y)) {
            IntEventType ievt = (IntEventType) evt;
            switch (ievt) {
                case REMOVE:
                    newrules |= ruleStore.addRemovalRule(x, value);
                    break;
                case DECUPP:
                    newrules |= ruleStore.addUpperBoundRule(x);
                    break;
                case INCLOW:
                    newrules |= ruleStore.addLowerBoundRule(x);
                    break;
                case INSTANTIATE:
                    newrules |= ruleStore.addFullDomainRule(x);
                    break;
                default: break;// nothing to do
            }
        } else {
            newrules |= super.why(ruleStore, var, evt, value);
        }
        return newrules;
    }

}
