/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class PropNotMemberEnum extends Propagator<IntVar> {

    /**
     * Forbidden values
     */
    private final TIntHashSet values;
    /**
     * Set of values to remove (needed for domain operations)
     */
    private final IntIterableBitSet vrms;

    /**
     * A propagator which forbids <i>values</i> from <i>var</i> domain
     * @param var a variable
     * @param values some values
     */
    public PropNotMemberEnum(IntVar var, int[] values) {
        super(new IntVar[]{var}, PropagatorPriority.UNARY, false, true);
        this.values = new TIntHashSet(values);
        vrms = new IntIterableBitSet();
        int of = Integer.MAX_VALUE;
        for(int i = 0 ; i < values.length; i++){
            if(values[i]<of){
                of = values[i];
            }
        }
        vrms.setOffset(of);
        vrms.addAll(values);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        vars[0].removeValues(vrms, this);
        if (vars[0].hasEnumeratedDomain()) {
            setPassive();
        }else{
            int lb = this.vars[0].getLB();
            int ub = this.vars[0].getUB();
            while(lb <= ub && !values.contains(lb)){
                lb++;
            }
            if(lb == ub){
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int ub = this.vars[0].getUB();
        int nb = 0;
        for (int val = this.vars[0].getLB(); val <= ub; val = this.vars[0].nextValue(val)) {
            if (!values.contains(val)) {
                nb++;
            }
        }
        if (nb == 0) return ESat.FALSE;
        else if (nb == vars[0].getDomainSize()) return ESat.TRUE;
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return vars[0].getName() + " outside " + Arrays.toString(values.toArray());
    }

}
