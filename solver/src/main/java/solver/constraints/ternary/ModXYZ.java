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

package solver.constraints.ternary;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.StringUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.nary.sum.PropSumEq;
import solver.constraints.propagators.ternary.PropDivXYZ;
import solver.constraints.propagators.ternary.PropTimes;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

/**
 * X mod Y = Z
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class ModXYZ extends IntConstraint<IntVar> {

    public ModXYZ(IntVar X, IntVar Y, IntVar Z, Solver solver) {
        super(new IntVar[]{X, Y, Z}, solver);
        int xl = Math.abs(X.getLB());
        int xu = Math.abs(X.getUB());
        int b = Math.max(xl, xu);
        IntVar t1 = VariableFactory.bounded(StringUtils.randomName(), -b, b, solver);
        IntVar t2 = VariableFactory.bounded(StringUtils.randomName(), -b, b, solver);
        setPropagators(
                new PropDivXYZ(X, Y, t1, solver, this),
                new PropTimes(t1, Y, t2, solver, this),
                new PropSumEq(new IntVar[]{Z, Views.minus(X), t2}, 0, solver, this)
        );
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        if (tuple[1] == 0) {
            return ESat.eval(tuple[0] == tuple[2]);
        }
        return ESat.eval(tuple[0] % tuple[1] == tuple[2]);
    }

    @Override
    public String toString() {
        return String.format("%s MOD %s = %s", vars[0].getName(), vars[1].getName(), vars[2].getName());
    }
}
