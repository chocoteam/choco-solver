/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.nary.sum;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

/**
 * A propagator for SUM(x_i) <= b
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 *
 * /!\ : thanks to views and pre-treatment, coefficients are merge into variable
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public final class PropSumLeq extends PropSumEq {


    public PropSumLeq(IntVar[] vars, int b, Solver solver,
                      Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(vars, b, solver, intVarPropagatorConstraint);
    }


    @Override
    boolean filterOnGeq() throws ContradictionException {
        return false;
    }

    @Override
    protected void checkEntailment() {
        if (sumUB - b <= 0) {
            this.setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        int sumUB = 0, sumLB = 0, i =0;
        for (; i < l; i++) {
            sumLB += x[i].getLB();
            sumUB += x[i].getUB();
        }
        if (sumUB <= b) {
            return ESat.TRUE;
        } else if (sumLB > b) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder linComb = new StringBuilder(20);
        linComb.append(vars[0].getName());
        int i = 1;
        for (; i < l; i++) {
            linComb.append(" + ").append(vars[i].getName());
        }
        linComb.append(" <= ");
        linComb.append(b);
        return linComb.toString();
    }
}
