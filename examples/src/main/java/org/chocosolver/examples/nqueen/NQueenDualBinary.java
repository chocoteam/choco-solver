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
public class NQueenDualBinary extends AbstractNQueen {

    @Override
    public void buildModel() {
        model = new Model("NQueen");
        vars = new IntVar[n];
        IntVar[] dualvars = new IntVar[n];

        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("Q_" + i, 1, n, false);
            dualvars[i] = model.intVar("QD_" + i, 1, n, false);
        }

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                model.arithm(dualvars[i], "!=", dualvars[j], "+", -k).post();
                model.arithm(dualvars[i], "!=", dualvars[j], "+", k).post();
            }
        }
        model.inverseChanneling(vars, dualvars, 1, 1).post();
    }


    public static void main(String[] args) {
        new NQueenDualBinary().execute(args);
    }
}
