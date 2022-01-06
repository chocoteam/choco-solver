/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.nqueen;


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
