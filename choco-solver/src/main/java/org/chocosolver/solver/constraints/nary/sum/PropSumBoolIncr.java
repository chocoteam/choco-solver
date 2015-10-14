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
package org.chocosolver.solver.constraints.nary.sum;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;

/**
 * A propagator for SUM(x_i) = y + b, where x_i are boolean variables
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 * <p>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public class PropSumBoolIncr extends PropSumBool {

    IStateInt bLB, bUB;
    boolean doFilter;

    public PropSumBoolIncr(BoolVar[] variables, int pos, Operator o, IntVar sum, int b) {
        super(variables, pos, o, sum, b, true);
        this.bLB = solver.getEnvironment().makeInt();
        this.bUB = solver.getEnvironment().makeInt();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            int i = 0, k;
            int lb = 0, ub = 0;
            for (; i < pos; i++) { // first the positive coefficients
                if (vars[i].isInstantiated()) {
                    k = vars[i].getLB();
                    lb += k;
                    ub += k;
                } else {
                    ub++;
                }
            }
            for (; i < l; i++) { // then the negative ones
                if (vars[i].isInstantiated()) {
                    k = vars[i].getLB();
                    lb -= k;
                    ub -= k;
                } else {
                    lb--;
                }
            }
            bLB.set(lb);
            bUB.set(ub);
        }
        doFilter = false;
        filter();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < pos) {
            int k = vars[idxVarInProp].getLB();
            if (k == 1) {
                bLB.add(1);
                doFilter |= o != Operator.GE;
            } else {
                bUB.add(-1);
                doFilter |= o != Operator.LE;
            }
        } else if (idxVarInProp < l) {
            int k = vars[idxVarInProp].getLB();
            if (k == 0) {
                bLB.add(1);
                doFilter |= o != Operator.GE;
            } else {
                bUB.add(-1);
                doFilter |= o != Operator.LE;
            }
        } else {
            doFilter |= true;
        }
        if (doFilter) {
            forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
        }
    }

    @Override
    protected void prepare() {
        sumLB = bLB.get() - sum.getUB();
        sumUB = bUB.get() - sum.getLB();
    }

}
