/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
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

package solver.constraints.propagators.nary.sum;

import common.ESat;
import memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;

/**
 * A propagator for SUM(x_i) =/= b
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 *
 * @author Charles Prud'homme
 * @revision 04/03/12 use I in filterOn{G,L}eg
 * @since 18/03/11
 */
public class PropSumNeq extends Propagator<IntVar> {

    private final IStateInt nb_instantiated;
    final int[] c; // list of coefficients
    final int pos; // index of the last positive coefficient
    final int l; // number of variables
    final int b; // bound to respect

    protected static PropagatorPriority computePriority(int nbvars) {
        if (nbvars == 1) {
            return PropagatorPriority.UNARY;
        } else if (nbvars == 2) {
            return PropagatorPriority.BINARY;
        } else if (nbvars == 3) {
            return PropagatorPriority.TERNARY;
        } else {
            return PropagatorPriority.LINEAR;
        }
    }

    public PropSumNeq(IntVar[] vars, int[] coeffs, int pos, int b,
                      Solver solver, Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(vars, computePriority(vars.length), false);
        this.c = coeffs;
        this.pos = pos;
        l = vars.length;
        this.b = b;
        nb_instantiated = solver.getEnvironment().makeInt();
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int n = 0;
        for (Variable v : vars) {
            if (v.instantiated()) {
                n++;
            }
        }
        nb_instantiated.set(n);
        filter();
    }

    /**
     * if there is only one uninstantiated variable,
     * then filtering can be applied on this uninstantiated variable.
     *
     * @throws ContradictionException if the domain of variables are inconsistent regarding to the constraint
     */
    private void filter() throws ContradictionException {
        if (nb_instantiated.get() >= vars.length - 1) {
            int index = -1;
            int sum = -b;
            for (int i = 0; i < vars.length; i++) {
                if (vars[i].instantiated()) {
                    sum += vars[i].getValue() * c[i];
                } else {
                    index = i;
                }
            }
            // If every variables are instantiated (by side effects),
            if (index == -1) {
                // then check the sum is not equal to 0
                if (sum == 0) {
                    // Otherwise, FAIL, the constraint is not satisfied
                    this.contradiction(null, "sum is equal to 0");
                }
            } else {
                // Compute the value to remove (including position in the linear combination)
                if (vars[index].removeValue(sum / c[index], aCause)) {
                    this.setPassive();
                }
            }
        }
    }


    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            nb_instantiated.add(1);
            filter();
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask;
    }

    @Override
    public ESat isEntailed() {
        int sumUB = 0, sumLB = 0, i = 0;
        for (; i < pos; i++) { // first the positive coefficients
            sumLB += vars[i].getLB() * c[i];
            sumUB += vars[i].getUB() * c[i];
        }
        for (; i < l; i++) { // then the negative ones
            sumLB += vars[i].getUB() * c[i];
            sumUB += vars[i].getLB() * c[i];
        }
        if (sumLB < sumUB) { // not instantiated
            if (sumLB <= b && b <= sumUB) {
                return ESat.UNDEFINED;
            }
            return ESat.TRUE;
        } else { // instantiated
            assert sumLB == sumUB;
            return ESat.eval(sumLB != b);
        }
    }

    @Override
    public String toString() {
        StringBuilder linComb = new StringBuilder(20);
        linComb.append(vars[0].getName());
        int i = 1;
        for (; i < l; i++) {
            linComb.append(" + ").append(vars[i].getName());
        }
        linComb.append(" = ");
        linComb.append(b);
        return linComb.toString();
    }
}
