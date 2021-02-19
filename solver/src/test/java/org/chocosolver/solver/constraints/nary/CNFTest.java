/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.nary.cnf.LogOp.implies;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/04/12
 */
public class CNFTest {

    @Test(groups="1s", timeOut=60000)
    public void testJGF() {
        for (int i = 0; i < 2; i++) {

            Model model = new Model();
            BoolVar a = model.boolVar("a");
            BoolVar b = model.boolVar("b");
            IntVar x = model.intVar("x", 0, 24, true);
            IntVar y = model.intVar("y", 0, 24, true);

            if (i == 0) {
                model.addClauses(implies(
                        a,
                        b
                ));
            } else {
                model.addClauses(implies(
                        b.not(),
                        a.not()
                ));
            }
            model.ifThenElse(b, model.arithm(x, ">=", y), model.arithm(x, "<", y));
//            SearchMonitorFactory.log(solver, true, true);
            while (model.getSolver().solve()) ;
//            System.out.printf("%d\n", solver.getSolutionCount());
        }
    }
}
