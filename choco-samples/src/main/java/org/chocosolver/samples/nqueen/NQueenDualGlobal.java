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
package org.chocosolver.samples.nqueen;

import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class NQueenDualGlobal extends AbstractNQueen {

    @Override
    public void buildModel() {
        vars = new IntVar[n];
        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];

        IntVar[] dualvars = new IntVar[n];
        IntVar[] dualdiag1 = new IntVar[n];
        IntVar[] dualdiag2 = new IntVar[n];

        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, 1, n, solver);
            diag1[i] = VariableFactory.enumerated("D1_" + i, 1, 2 * n, solver);
            diag2[i] = VariableFactory.enumerated("D2_" + i, -n, n, solver);

            dualvars[i] = VariableFactory.enumerated("DQ_" + i, 1, n, solver);
            dualdiag1[i] = VariableFactory.enumerated("DD1_" + i, 1, 2 * n, solver);
            dualdiag2[i] = VariableFactory.enumerated("DD2_" + i, -n, n, solver);
        }

        for (int i = 0; i < n; i++) {
            solver.post(IntConstraintFactory.arithm(diag1[i], "=", vars[i], "+", i));
            solver.post(IntConstraintFactory.arithm(diag2[i], "=", vars[i], "-", i));

            solver.post(IntConstraintFactory.arithm(dualdiag1[i], "=", dualvars[i], "+", i));
            solver.post(IntConstraintFactory.arithm(dualdiag2[i], "=", dualvars[i], "-", i));
        }
        solver.post(IntConstraintFactory.alldifferent(diag1, "BC"));
        solver.post(IntConstraintFactory.alldifferent(diag2, "BC"));
        solver.post(IntConstraintFactory.alldifferent(dualdiag1, "BC"));
        solver.post(IntConstraintFactory.alldifferent(dualdiag2, "BC"));

        solver.post(IntConstraintFactory.inverse_channeling(vars, dualvars, 1, 1));
    }


    public static void main(String[] args) {
        new NQueenDualGlobal().execute(args);
    }
}
