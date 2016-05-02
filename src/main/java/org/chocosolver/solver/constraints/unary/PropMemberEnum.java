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
package org.chocosolver.solver.constraints.unary;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.ranges.IntIterableBitSet;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.ESat;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class PropMemberEnum extends Propagator<IntVar> {

    private final TIntHashSet values;
    private final IntIterableSet vrms;

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
