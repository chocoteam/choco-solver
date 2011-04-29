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

package solver.constraints.unary;

import choco.kernel.ESat;
import gnu.trove.TIntHashSet;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.unary.PropMemberBound;
import solver.constraints.propagators.unary.PropMemberEnum;
import solver.variables.IntVar;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class Member extends IntConstraint<IntVar> {

    final TIntHashSet values;
    final int lb, ub;

    public Member(IntVar var, int[] values, Solver solver) {
        this(var, values, solver, PropagatorPriority.UNARY);
    }

    public Member(IntVar var, int[] values, Solver solver, PropagatorPriority storeThreshold) {
        super(new IntVar[]{var}, solver, storeThreshold);
        this.values = new TIntHashSet(values);
        lb = 0;
        ub = 0;
        setPropagators(new PropMemberEnum(var, this.values, solver.getEnvironment(), this, storeThreshold, false));
    }

    public Member(IntVar var, int lowerbound, int upperbound, Solver solver) {
        this(var, lowerbound, upperbound, solver, PropagatorPriority.UNARY);
    }

    public Member(IntVar var, int lowerbound, int upperbound, Solver solver, PropagatorPriority storeThreshold) {
        super(new IntVar[]{var}, solver, storeThreshold);
        this.values = null;
        this.lb = lowerbound;
        this.ub = upperbound;
        setPropagators(new PropMemberBound(var, lowerbound, upperbound, solver.getEnvironment(), this, storeThreshold, false));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        if (values != null) {
            return ESat.eval(values.contains(tuple[0]));
        } else {
            return ESat.eval(lb <= tuple[0] && tuple[0] <= ub);
        }
    }

    @Override
    public String toString() {
        return vars[0].toString() + " in " + (values == null ? "[" + lb + "," + ub + "]" : Arrays.toString(values.toArray()));
    }
}
