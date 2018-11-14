/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.parser;

import org.chocosolver.parser.flatzinc.Flatzinc4Parser;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_solve_goal extends GrammarTest {

    Model mSolver;
    Datas datas;

    @BeforeMethod
    public void before() {
        mSolver = new Model();
        datas = new Datas();
    }

    @Test(groups = "1s")
    public void testSatisfy() throws IOException {
        Flatzinc4Parser fp = parser("solve satisfy;", mSolver, datas);
        fp.solve_goal();
    }

    @Test(groups = "1s")
    public void testMaximize() throws IOException {
        datas.register("a", mSolver.intVar("a", 0, 10, true));
        Flatzinc4Parser fp = parser("solve maximize a;", mSolver, datas);
        fp.solve_goal();
    }

    @Test(groups = "1s")
    public void testMinimize() throws IOException {
        datas.register("a", mSolver.intVar("a", 0, 10, true));
        Flatzinc4Parser fp = parser("solve minimize a;", mSolver, datas);
        fp.solve_goal();
    }

    @Test(groups = "1s")
    public void testSatisfy2() throws IOException {
        datas.register("a", mSolver.intVar("a", 0, 10, true));
        Flatzinc4Parser fp = parser("solve ::int_search([a],input_order,indomain_min, complete) satisfy;", mSolver, datas);
        fp.solve_goal();
    }


    @Test(groups = "1s")
    public void testSatisfy3() throws IOException {
        datas.register("r", mSolver.intVarArray("r", 5, 0, 10, true));
        datas.register("s", mSolver.intVarArray("s", 5, 0, 10, true));
        datas.register("o", mSolver.intVar("o", 0, 10, true));
        Flatzinc4Parser fp = parser(
                "solve\n" +
                        "  ::seq_search(\n" +
                        "    [ int_search(r, input_order, indomain_min, complete),\n" +
                        "      int_search(s, input_order, indomain_min, complete) ])\n" +
                        "  minimize o;", mSolver, datas
        );
        fp.solve_goal();
    }

}
