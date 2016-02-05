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
package org.chocosolver.samples.nqueen;


import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class NQueenDualGlobal extends AbstractNQueen {

    @Override
    public void buildModel() {
        model = new Model("NQueen");
        vars = new IntVar[n];
        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];

        IntVar[] dualvars = new IntVar[n];
        IntVar[] dualdiag1 = new IntVar[n];
        IntVar[] dualdiag2 = new IntVar[n];

        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("Q_" + i, 1, n, false);
            diag1[i] = model.intVar("D1_" + i, 1, 2 * n, false);
            diag2[i] = model.intVar("D2_" + i, -n, n, false);

            dualvars[i] = model.intVar("DQ_" + i, 1, n, false);
            dualdiag1[i] = model.intVar("DD1_" + i, 1, 2 * n, false);
            dualdiag2[i] = model.intVar("DD2_" + i, -n, n, false);
        }

        for (int i = 0; i < n; i++) {
            model.arithm(diag1[i], "=", vars[i], "+", i).post();
            model.arithm(diag2[i], "=", vars[i], "-", i).post();

            model.arithm(dualdiag1[i], "=", dualvars[i], "+", i).post();
            model.arithm(dualdiag2[i], "=", dualvars[i], "-", i).post();
        }
        model.allDifferent(diag1, "BC").post();
        model.allDifferent(diag2, "BC").post();
        model.allDifferent(dualdiag1, "BC").post();
        model.allDifferent(dualdiag2, "BC").post();

        model.inverseChanneling(vars, dualvars, 1, 1).post();
    }


    public static void main(String[] args) {
        new NQueenDualGlobal().execute(args);
    }
}
