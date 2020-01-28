/**
 * Copyright (c) 1999-2020, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Ecole des Mines de Nantes nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;

/**
 * Let x be an integer variable with n values and v be a real variable. Given n constant values a1 to an,
 * this constraint ensures that:
 * <p/>
 * <code>x = i iff v = ai</code>
 * <p/>
 * a1... an sequence is supposed to be ordered (a1&lt;a2&lt;... an)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/01/2020
 */
public class PropMixedElement extends Propagator<Variable> {
    RealVar x;
    IntVar y;
    protected double[] values;

    public PropMixedElement(RealVar v0, IntVar v1, double[] values) {
        super(new Variable[]{v0, v1}, PropagatorPriority.BINARY, false);
        x = v0;
        y = v1;
        this.values = values;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            y.updateLowerBound(0, this);
            y.updateUpperBound(values.length - 1, this);
        }
        updateIInf();
        updateISup();
        updateReal();
    }

    public void updateIInf() throws ContradictionException {
        int inf = y.getLB();
        while (values[inf] < x.getLB()) {
            inf++;
        }
        y.updateLowerBound(inf, this);
    }

    public void updateISup() throws ContradictionException {
        int sup = y.getUB();
        while (values[sup] > x.getUB()) {
            sup--;
        }
        y.updateUpperBound(sup, this);
    }

    public void updateReal() throws ContradictionException {
        x.intersect(values[y.getLB()], values[y.getUB()], this);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int idx = y.getValue();
            return ESat.eval(
                    values[idx] <= x.getUB() && x.getLB() <= values[idx]
                            && (idx == 0 || values[idx - 1] < x.getLB())
                            && (idx == values.length - 1 || x.getUB() < values[idx + 1])
            );
        }
        return ESat.UNDEFINED;
    }
}
