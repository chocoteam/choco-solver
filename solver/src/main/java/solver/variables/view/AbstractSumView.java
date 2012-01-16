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
package solver.variables.view;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * declare an IntVar based on X and Y, such X + Y
 * <br/>
 * Based on
 * "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 * <br/>And <br/>
 * "Bounds Consistency Techniques for Long Linear Constraint" <br/>
 * W. Harvey and J. Schimpf
 *
 * @author Charles Prud'homme
 * @since 01/09/11
 */
public abstract class AbstractSumView extends AbstractViewWithDomain {

    public AbstractSumView(IntVar a, IntVar b, Solver solver) {
        super(a, b, solver);
        int lbA = A.getLB();
        int ubA = A.getUB();
        int lbB = B.getLB();
        int ubB = B.getUB();
        int min = lbA + lbB;
        int max = ubA + ubB;
        this.LB.set(min);
        this.UB.set(max);
        this.SIZE.set(max - min + 1);
    }

    public String getName() {
        return String.format("(%s + %s)", A, B);
    }

    /////////////// SERVICES REQUIRED FROM SUM //////////////////////////

    void filterOnLeq(ICause cause, int ub) throws ContradictionException {
        int lbA = A.getLB(), lbB = B.getLB();
        int sumLB = lbA + lbB - ub;
        if (-sumLB < 0) contradiction(cause, EventType.FULL_PROPAGATION, MSG_EMPTY);
        int ubA = A.getUB(), ubB = B.getUB();
        if (ubA - lbA + sumLB > 0) {
            A.updateUpperBound(-sumLB + lbA, this, true);//CPRU not idempotent
        }
        if (ubB - lbB + sumLB > 0) {
            B.updateUpperBound(-sumLB + lbB, this, true);//CPRU not idempotent
        }
    }

    void filterOnGeq(ICause cause, int lb) throws ContradictionException {
        int ubA = A.getUB(), ubB = B.getUB();
        int sumUB = ubA + ubB - lb;
        if (-sumUB > 0) contradiction(cause, EventType.FULL_PROPAGATION, MSG_EMPTY);
        int lbA = A.getLB(), lbB = B.getLB();
        if (ubA - lbA - sumUB > 0) {
            A.updateLowerBound(-sumUB + ubA, this);//CPRU not idempotent
        }
        if (ubB - lbB - sumUB > 0) {
            B.updateLowerBound(-sumUB + ubB, this);//CPRU not idempotent
        }
    }
}
