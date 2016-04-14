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
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import static org.chocosolver.util.tools.MathUtils.safeMultiply;

/**
 * V0 * V1 = V2
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
public class PropTimesNaive extends Propagator<IntVar> {

    protected static final int MAX = Integer.MAX_VALUE - 1, MIN = Integer.MIN_VALUE + 1;

    private IntVar v0, v1, v2;

    public PropTimesNaive(IntVar v1, IntVar v2, IntVar result) {
        super(new IntVar[]{v1, v2, result}, PropagatorPriority.TERNARY, false);
        this.v0 = vars[0];
        this.v1 = vars[1];
        this.v2 = vars[2];
    }

    @Override
    public final int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public final void propagate(int evtmask) throws ContradictionException {
        boolean hasChanged = true;
        while (hasChanged) {
            hasChanged = div(v0, v2.getLB(), v2.getUB(), v1.getLB(), v1.getUB());
            hasChanged |= div(v1, v2.getLB(), v2.getUB(), v0.getLB(), v0.getUB());
            hasChanged |= mul(v2, v0.getLB(), v0.getUB(), v1.getLB(), v1.getUB());
        }
        if (v2.isInstantiatedTo(0) && (v0.isInstantiatedTo(0) || v1.isInstantiatedTo(1))) {
            setPassive();
        }
    }

    @Override
    public final ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            return ESat.eval(v0.getValue() * v1.getValue() == v2.getValue());
        }
        return ESat.UNDEFINED;
    }

    private boolean div(IntVar var, int a, int b, int c, int d) throws ContradictionException {
        int min, max;

        if (a <= 0 && b >= 0 && c <= 0 && d >= 0) { // case 1
            min = MIN;
            max = MAX;
            return var.updateLowerBound(min, this) | var.updateUpperBound(max, this);
        } else if (c == 0 && d == 0 && (a > 0 || b < 0)) // case 2
            this.contradiction(var, "");
        else if (c < 0 && d > 0 && (a > 0 || b < 0)) { // case 3
            max = Math.max(Math.abs(a), Math.abs(b));
            min = -max;
            return var.updateLowerBound(min, this) | var.updateUpperBound(max, this);
        } else if (c == 0 && d != 0 && (a > 0 || b < 0)) // case 4 a
            return div(var, a, b, 1, d);
        else if (c != 0 && d == 0 && (a > 0 || b < 0)) // case 4 b
            return div(var, a, b, c, -1);
        else { // if (c > 0 || d < 0) { // case 5
            float ac = (float) a / c, ad = (float) a / d,
                    bc = (float) b / c, bd = (float) b / d;
            float low = Math.min(Math.min(ac, ad), Math.min(bc, bd));
            float high = Math.max(Math.max(ac, ad), Math.max(bc, bd));
            min = (int) Math.round(Math.ceil(low));
            max = (int) Math.round(Math.floor(high));
            if (min > max) this.contradiction(var, "");
            return var.updateLowerBound(min, this) | var.updateUpperBound(max, this);
        }
        return false;
    }

    private boolean mul(IntVar var, int a, int b, int c, int d) throws ContradictionException {
        int min = Math.min(Math.min(safeMultiply(a, c), safeMultiply(a, d)), Math.min(safeMultiply(b, c), safeMultiply(b, d)));
        int max = Math.max(Math.max(safeMultiply(a, c), safeMultiply(a, d)), Math.max(safeMultiply(b, c), safeMultiply(b, d)));
        return var.updateLowerBound(min, this) | var.updateUpperBound(max, this);
    }


    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        for (int i = 0; i < 3; i++) {
            if (var != vars[i]) {
                newrules |= ruleStore.addBoundsRule(vars[i]);
            }
        }
        return newrules;
    }
}
