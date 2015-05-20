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
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * X + Y <= C
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/06/12
 */
public final class PropLessOrEqualXY_C extends Propagator<IntVar> {

    final IntVar x;
    final IntVar y;
    final int cste;

    @SuppressWarnings({"unchecked"})
    public PropLessOrEqualXY_C(IntVar[] vars, int c) {
        super(vars, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
        this.cste = c;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.INSTANTIATE.getMask() + IntEventType.INCLOW.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x.updateUpperBound(this.cste - y.getLB(), aCause);
        y.updateUpperBound(this.cste - x.getLB(), aCause);
        if (x.getUB() + y.getUB() <= this.cste) {
            this.setPassive();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            y.updateUpperBound(this.cste - x.getLB(), aCause);
        } else {
            x.updateUpperBound(this.cste - y.getLB(), aCause);
        }
        if (x.getUB() + y.getUB() <= this.cste) {
            this.setPassive();
        }
    }


    @Override
    public ESat isEntailed() {
        if (x.getLB() + y.getLB() > cste)
            return ESat.FALSE;
        else if (x.getUB() + y.getUB() <= this.cste)
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return x.getName() + " + " + y.getName() + " <= " + cste;
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        if (var.equals(x)) {
            newrules |=ruleStore.addLowerBoundRule(y);
        } else if (var.equals(y)) {
            newrules |=ruleStore.addLowerBoundRule(x);
        } else {
            newrules |=super.why(ruleStore, var, evt, value);
        }
        return newrules;
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.vars[0].duplicate(solver, identitymap);
            IntVar X = (IntVar) identitymap.get(this.vars[0]);
            this.vars[1].duplicate(solver, identitymap);
            IntVar Y = (IntVar) identitymap.get(this.vars[1]);

            identitymap.put(this, new PropLessOrEqualXY_C(new IntVar[]{X, Y}, this.cste));
        }
    }
}
