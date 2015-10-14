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
package org.chocosolver.solver.constraints.binary.element;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.ranges.IntIterableBitSet;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * VALUE = TABLE[INDEX-OFFSET], naive version, ensuring bound consistency on result and range consistency on index.
 * <br/>
 *
 * @author Hadrien Cambazard
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 02/02/12
 */
//2015OCT - cprudhom : simplify the code, without changing the consistency, no Sort anymore
public class PropElement extends Propagator<IntVar> {

    final int[] values;
    final int offset;
    final IntVar index, result;
    final IntIterableSet fidx; // forbidden indices

    public PropElement(IntVar value, int[] values, IntVar index, int offset) {
        super(ArrayUtils.toArray(value, index), PropagatorPriority.BINARY, false);
        this.values = values;
        this.offset = offset;
        this.index = index;
        this.result = value;
        fidx = new IntIterableBitSet();
        fidx.setOffset(index.getLB());
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        index.updateBounds(offset, values.length - 1 + offset, this);
        int min = result.getLB();
        int max = result.getUB();
        int nmin = max;
        int nmax = min;
        do {
            fidx.clear();
            int iub = index.getUB();
            for (int i = index.getLB(); i <= iub; i = index.nextValue(i)) {
                int value = values[i - offset];
                if (!result.contains(value)/*value < min || value > max*/) {
                    fidx.add(i);
                } else {
                    if (value < nmin) {
                        nmin = value;
                    }
                    if (value > nmax) {
                        nmax = value;
                    }
                }
            }
            result.updateBounds(nmin, nmax, this);
            if (fidx.size() > 0) {
                index.removeValues(fidx, this);
            }
            min = result.getLB();
            max = result.getUB();
        } while (result.hasEnumeratedDomain() && (nmin > min || max < nmax));
        if (result.isInstantiated() && !index.isInstantiated()) {
            setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        if (index.isInstantiated()) { // if INDEX is known
            return ESat.eval(result.isInstantiatedTo(values[index.getValue() - offset]));
        } else if (result.isInstantiated()) { // if RESULT
            int res = result.getLB();
            int ub = index.getUB();
            int i = index.getLB();
            int val = values[i - offset];
            while (i <= ub && val == res) {
                i = index.nextValue(i);
                val = values[i - offset];
            }
            return ESat.eval(i == ub);
        }
        return ESat.UNDEFINED;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("element(").append(this.result).append(" = ");
        sb.append(" <");
        int i = 0;
        for (; i < Math.min(this.values.length - 1, 5); i++) {
            sb.append(this.values[i]).append(", ");
        }
        if (i == 5 && this.values.length - 1 > 5) sb.append("..., ");
        sb.append(this.values[values.length - 1]);
        sb.append("> [").append(this.index).append("])");
        return sb.toString();
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        return ruleStore.addPropagatorActivationRule(this)
                | ruleStore.addFullDomainRule((var == result) ? index : result);
    }

}
