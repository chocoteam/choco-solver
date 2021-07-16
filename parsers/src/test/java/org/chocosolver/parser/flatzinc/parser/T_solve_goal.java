/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.parser;

import org.chocosolver.parser.flatzinc.Flatzinc4Parser;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.solver.Model;
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

    @BeforeMethod(alwaysRun = true)
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


    /**
     * var 1..10: x;
     * var 1..10: y;
     * <p>
     * var bool: p;
     * <p>
     * constraint p <-> y < 5 /\ x > 5;
     * <p>
     * solve :: seq_search([
     * seq_search([
     * int_search([x], input_order, indomain_min, complete),
     * bool_search([p], input_order, indomain_max, complete)
     * ]),
     * int_search([y], input_order, indomain_max, complete)
     * ]) satisfy;
     */
    @Test(groups = "1s")
    public void testNestedSeq() throws IOException {
        datas.register("x", mSolver.intVar("x", 0, 10, true));
        datas.register("y", mSolver.intVar("y", 0, 10, true));
        Flatzinc4Parser fp = parser(
                "solve :: seq_search([\n" +
                        "    seq_search([\n" +
                        "      int_search([x], input_order, indomain_min, complete),\n" +
                        "      bool_search([p], input_order, indomain_max, complete)\n" +
                        "    ]),\n" +
                        "    int_search([y], input_order, indomain_max, complete)\n" +
                        "]) satisfy;", mSolver, datas
        );
        fp.solve_goal();
    }
}
