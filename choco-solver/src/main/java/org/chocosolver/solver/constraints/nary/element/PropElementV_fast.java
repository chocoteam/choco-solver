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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 10/05/13
 * Time: 01:32
 */

package org.chocosolver.solver.constraints.nary.element;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Fast Element constraint
 *
 * @author Jean-Guillaume Fages
 * @since 05/2013
 */
public class PropElementV_fast extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar var, index;
    private int offset;
    private final boolean fast;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropElementV_fast(IntVar value, IntVar[] values, IntVar index, int offset, boolean fast) {
        super(ArrayUtils.append(new IntVar[]{value, index}, values), PropagatorPriority.LINEAR, false);
        this.var = vars[0];
        this.index = vars[1];
        this.offset = offset;
        this.fast = fast;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        index.updateLowerBound(offset, this);
        index.updateUpperBound(vars.length + offset - 3, this);
        int lb = index.getLB();
        int ub = index.getUB();
        int min = Integer.MAX_VALUE / 2;
        int max = Integer.MIN_VALUE / 2;
        // 1. bottom up loop
        for (int i = lb; i <= ub; i = index.nextValue(i)) {
            if (disjoint(var, vars[2 + i - offset])) {
                index.removeValue(i, this);
            }
            min = Math.min(min, vars[2 + i - offset].getLB());
            max = Math.max(max, vars[2 + i - offset].getUB());
        }
        // 2. top-down loop for bounded domains
        if (!index.hasEnumeratedDomain()) {
            if (index.getUB() < ub) {
                for (int i = ub - 1; i >= lb; i = index.previousValue(i)) {
                    if (disjoint(var, vars[2 + i - offset])) {
                        index.removeValue(i, this);
                    } else break;
                }
            }
        }
        var.updateLowerBound(min, this);
        var.updateUpperBound(max, this);
        if (index.isInstantiated()) {
            equals(var, vars[2 + index.getValue() - offset]);
        }
        if (var.isInstantiated() && index.isInstantiated()) {
            IntVar v = vars[2 + index.getValue() - offset];
            if (v.isInstantiated() && v.getValue() == var.getValue()) {
                setPassive();
            }
        }
    }

    private void equals(IntVar a, IntVar b) throws ContradictionException {
        int s = a.getDomainSize() + b.getDomainSize();
        a.updateLowerBound(b.getLB(), this);
        a.updateUpperBound(b.getUB(), this);
        b.updateLowerBound(a.getLB(), this);
        b.updateUpperBound(a.getUB(), this);
        if (!fast) {
            if (a.getDomainSize() != b.getDomainSize()) {
                int lb = a.getLB();
                int ub = a.getUB();
                for (int i = lb; i <= ub; i = a.nextValue(i)) {
                    if (!b.contains(i)) {
                        a.removeValue(i, this);
                    }
                }
            }
            if (a.getDomainSize() != b.getDomainSize()) {
                int lb = b.getLB();
                int ub = b.getUB();
                for (int i = lb; i <= ub; i = b.nextValue(i)) {
                    if (!a.contains(i)) {
                        b.removeValue(i, this);
                    }
                }
            }
        }
        if (a.getDomainSize() + b.getDomainSize() != s) {
            equals(a, b);
        }
    }

    private boolean disjoint(IntVar a, IntVar b) {
        if (a.getLB() > b.getUB() || b.getLB() > a.getUB()) {
            return true;
        }
        if (fast) {
            return false;
        }
        int lb = a.getLB();
        int ub = a.getUB();
        for (int i = lb; i <= ub; i = a.nextValue(i)) {
            if (b.contains(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ESat isEntailed() {
        int lb = index.getLB();
        int ub = index.getUB();
        int min = Integer.MAX_VALUE / 2;
        int max = Integer.MIN_VALUE / 2;
        int val = var.getLB();
        boolean exists = false;
        for (int i = lb; i <= ub; i = index.nextValue(i)) {
            int j = 2 + i - offset;
            if (j >= 2 && j < vars.length) {
                min = Math.min(min, vars[j].getLB());
                max = Math.max(max, vars[j].getUB());
                exists |= vars[j].contains(val);
            }
        }
        if (min > var.getUB() || max < var.getLB()) {
            return ESat.FALSE;
        }
        if (var.isInstantiated() && !exists) {
            return ESat.FALSE;
        }
        if (var.isInstantiated() && min == max) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean nrule = ruleStore.addPropagatorActivationRule(this);
        for(int i = 0; i < vars.length; i++){
            if(var != vars[i]) nrule |= ruleStore.addFullDomainRule(vars[i]);
        }
        return nrule;
    }
}
