/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class PropMemberEnum extends Propagator<IntVar> {

    private final TIntHashSet values;
    private final IntIterableBitSet vrms;

    public PropMemberEnum(IntVar var, int[] values) {
        super(new IntVar[]{var}, PropagatorPriority.UNARY, false);
        this.values = new TIntHashSet(values);
        vrms = new IntIterableBitSet();
        vrms.setOffset(vars[0].getLB());
        int ub = this.vars[0].getUB();
        for (int val = this.vars[0].getLB(); val <= ub; val = this.vars[0].nextValue(val)) {
            if (!this.values.contains(val)) {
                vrms.add(val);
            }
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        vars[0].removeValues(vrms, this);
        if (vars[0].hasEnumeratedDomain()) {
            setPassive();
        }else{
            int lb = this.vars[0].getLB();
            int ub = this.vars[0].getUB();
            while(lb <= ub && values.contains(lb)){
                lb++;
            }
            if(lb == ub){
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int nb = 0;
        int ub = this.vars[0].getUB();
        for (int val = this.vars[0].getLB(); val <= ub; val = this.vars[0].nextValue(val)) {
            if (values.contains(val)) {
                nb++;
            }
        }
        if (nb == 0) return ESat.FALSE;
        else if (nb == vars[0].getDomainSize()) return ESat.TRUE;
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return vars[0].getName() + " in " + Arrays.toString(values.toArray());
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        return ruleStore.addPropagatorActivationRule(this);
    }
}
