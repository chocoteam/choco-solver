/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.constraints.checker.DomainBuilder.buildFullDomains;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class MemberTest {

    private int unionSize(int[] dom, int[] values) {
        int u = 0;
        la:
        for (int i = 0; i < dom.length; i++) {
            for (int j = 0; j < values.length; j++) {
                if (dom[i] == values[j]) {
                    u++;
                    continue la;
                }
            }
        }
        return u;
    }


    private int unionSize(int lb, int ub, int[] values) {
        int u = 0;
        la:
        for (int i = lb; i <= ub; i++) {
            for (int j = 0; j < values.length; j++) {
                if (i == values[j]) {
                    u++;
                    continue la;
                }
            }
        }
        return u;
    }

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        for (int i = 1; i < 99; i++) {
            Random r = new Random(i);
            for (double d = 0.0; d <= 1.0; d += 0.125) {

                Model s = new Model();

                IntVar[] vars = new IntVar[1];
                int[][] values = buildFullDomains(2, 0, i, r, d, false);
                vars[0] = s.intVar("v", values[0]);

                s.member(vars[0], values[1]).post();

                s.getSolver().setSearch(inputOrderLBSearch(vars));

                while (s.getSolver().solve()) ;
                long sol = s.getSolver().getSolutionCount();
                long nod = s.getSolver().getNodeCount();
                assertEquals(sol, unionSize(values[0], values[1]), "nb sol incorrect");
                assertEquals(nod, sol == 0 ? 0 : sol * 2 - 1, "nb sol incorrect");
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        for (int i = 1; i < 99; i++) {
            Random r = new Random(i);
            for (double d = 0.0; d <= 1.0; d += 0.125) {

                Model s = new Model();
                IntVar[] vars = new IntVar[1];
                int[][] values = buildFullDomains(2, 0, i, r, d, false);
                int lb = values[0][0];
                int ub = values[0][values[0].length - 1];

                vars[0] = s.intVar("v", lb, ub, true);

                s.member(vars[0], values[1]).post();

                s.getSolver().setSearch(inputOrderLBSearch(vars));

                while (s.getSolver().solve()) ;
                long sol = s.getSolver().getSolutionCount();
                long nod = s.getSolver().getNodeCount();
                assertEquals(sol, unionSize(lb, ub, values[1]), "nb sol incorrect");
                assertEquals(nod, sol == 0 ? 0 : sol * 2 - 1, "nb nod incorrect");
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test_alxpgr() {
        Model s = new Model();
        IntVar vars = s.intVar("v", 0, 10, false);
        int[] values = new int[]{0, 2, 4, 6, 8};

        s.member(vars, values).post();
        s.getSolver().setSearch(inputOrderLBSearch(vars));

        while (s.getSolver().solve()) ;

    }

}
