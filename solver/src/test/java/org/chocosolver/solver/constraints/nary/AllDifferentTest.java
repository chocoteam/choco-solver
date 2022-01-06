/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import static org.chocosolver.solver.constraints.checker.DomainBuilder.buildFullDomains;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Random;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20 sept. 2010
 */
public class AllDifferentTest {

    public static void model(boolean simple, int n, int nbSol) {
        Model s = new Model();

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = s.intVar("v_" + i, 1, n, false);
        }
        s.allDifferent(vars, "BC").post();
        if (simple) {

            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    int k = j - i;
                    s.arithm(vars[i], "!=", vars[j]).post();
                    s.arithm(vars[i], "!=", vars[j], "+", k).post();
                    s.arithm(vars[i], "!=", vars[j], "+", -k).post();
                }
            }
        } else {
            IntVar[] diag1 = new IntVar[n];
            IntVar[] diag2 = new IntVar[n];

            for (int i = 0; i < n; i++) {
                diag1[i] = s.intVar("v_" + (i + 2 * n), -n, n, false);
                diag2[i] = s.intVar("v_" + (i + n), 1, 2 * n, false);
            }
            for (int i = 0; i < n; i++) {
                s.arithm(diag1[i], "=", vars[i], "-", i).post();
                s.arithm(diag2[i], "=", vars[i], "+", i).post();
            }
            s.allDifferent(diag1, "BC").post();
            s.allDifferent(diag2, "BC").post();
        }
        Solver r = s.getSolver();
        r.setSearch(inputOrderLBSearch(vars));
        while (s.getSolver().solve()) ;
        long sol = r.getMeasures().getSolutionCount();
        assertEquals(sol, nbSol, "nb sol incorrect");
    }


    @Test(groups="1s", timeOut=60000)
    public void test1() {
        model(true, 8, 92);
        model(false, 8, 92);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        model(true, 8, 92);
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {

        Model s = new Model();

        int n = 4;
        IntVar[] vars = new IntVar[n];
        vars[0] = s.intVar("v_0", new int[]{1, 6});
        vars[1] = s.intVar("v_1", new int[]{1, 3});
        vars[2] = s.intVar("v_2", new int[]{3, 5});
        vars[3] = s.intVar("v_3", new int[]{1, 3, 5, 6});


        s.allDifferent(vars, "BC").post();
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                s.arithm(vars[i], "!=", vars[j], "+", -k).post();
                s.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        s.getSolver().setSearch(inputOrderLBSearch(vars));
        //        ChocoLogging.toSolution();
        while (s.getSolver().solve()) ;
        long sol = s.getSolver().getSolutionCount();
        assertEquals(sol, 1, "nb sol incorrect");

    }


    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Model s = new Model();
        int n = 5;
        IntVar[] vars = new IntVar[n];
        vars[0] = s.intVar("v_0", 5);
        vars[1] = s.intVar("v_1", 3);
        vars[2] = s.intVar("v_2", 3, 4, true);
        vars[3] = s.intVar("v_3", 2, 6, true);
        vars[4] = s.intVar("v_4", 2, 6, true);
        s.allDifferent(vars, "BC").post();
        s.getSolver().setSearch(inputOrderLBSearch(vars));
        while (s.getSolver().solve()) ;
        long sol = s.getSolver().getSolutionCount();
        assertEquals(sol, 2, "nb sol incorrect");
    }


    @Test(groups="10s", timeOut=60000)
    public void test6() {
        Random rand;
        for (int seed = 0; seed < 10; seed++) {
            rand = new Random(seed);
            for (double d = 0.25; d <= 1.0; d += 0.25) {
                for (int h = 0; h <= 1; h++) {
                    for (int b = 0; b <= 1; b++) {
                        int n = 1 + rand.nextInt(5);
                        int[][] domains = buildFullDomains(n, 1, 2 * n, rand, d, h == 0);

                        Model neqs = alldiffs(domains, 0, b == 0);
                        while (neqs.getSolver().solve()) ;

                        Model clique = alldiffs(domains, 1, b == 0);
                        while (clique.getSolver().solve()) ;
                        assertEquals(clique.getSolver().getSolutionCount(), neqs.getSolver().getSolutionCount(), "nb sol incorrect " + seed);
                        assertEquals(clique.getSolver().getNodeCount(), neqs.getSolver().getNodeCount(), "nb nod incorrect" + seed);

                        Model bc = alldiffs(domains, 2, b == 0);
                        while (bc.getSolver().solve()) ;
                        assertEquals(bc.getSolver().getSolutionCount(), neqs.getSolver().getSolutionCount(), "nb sol incorrect " + seed);
                        assertTrue(bc.getSolver().getNodeCount() <= neqs.getSolver().getNodeCount(), "nb nod incorrect" + seed);

                        Model ac = alldiffs(domains, 3, b == 0);
                        while (ac.getSolver().solve()) ;
                        assertEquals(ac.getSolver().getSolutionCount(), neqs.getSolver().getSolutionCount(), "nb sol incorrect " + seed);
                        assertTrue(ac.getSolver().getNodeCount() <= neqs.getSolver().getNodeCount(), "nb nod incorrect" + seed);
                    }
                }
            }
        }
    }


    protected Model alldiffs(int[][] domains, int c, boolean bounded) {
        Model s = new Model();

        IntVar[] vars = new IntVar[domains.length];
        if (bounded) {
            for (int i = 0; i < domains.length; i++) {
                vars[i] = s.intVar("v_" + i, domains[i][0], domains[i][domains[i].length - 1], true);
            }
        } else {
            for (int i = 0; i < domains.length; i++) {
                vars[i] = s.intVar("v_" + i, domains[i]);
            }
        }

        switch (c) {
            case 0:
                for (int i = 0; i < vars.length - 1; i++) {
                    for (int j = i + 1; j < vars.length; j++) {
                        s.arithm(vars[i], "!=", vars[j]).post();
                    }
                }
                break;
            case 1:
                s.allDifferent(vars, "NEQS").post();
                break;
            case 2:
                s.allDifferent(vars, "BC").post();
                break;
            case 3:
                s.allDifferent(vars, "AC").post();
                break;
        }
        s.getSolver().setSearch(inputOrderLBSearch(vars));
        return s;
    }

    @Test(groups="1s", timeOut=60000)
    public void testXX() {
        Model model = new Model();
        IntVar[] ts = new IntVar[4];
        ts[0] = model.intVar("t0", new int[]{2, 3, 4});
        ts[1] = model.intVar("t1", new int[]{-3, -2, -1, 1, 2});
        ts[2] = model.intVar("t2", new int[]{-3, -2, -1, 1, 2, 3});
        ts[3] = model.intVar("t3", new int[]{-3, -2, -1, 1, 2, 3});

        try {
            model.getSolver().propagate();
            ts[0].removeValue(2, Cause.Null);
            ts[1].removeValue(2, Cause.Null);
            ts[0].removeValue(3, Cause.Null);
            ts[1].removeValue(1, Cause.Null);
            ts[2].removeValue(-3, Cause.Null);
            ts[2].removeValue(3, Cause.Null);
            ts[3].removeValue(-3, Cause.Null);
            ts[3].removeValue(3, Cause.Null);
            model.getSolver().propagate();
        } catch (ContradictionException ignored) {

        }
//        System.out.printf("%s\n", solver.toString());


    }

    @Test(groups="1s", timeOut=60000)
    public void testXXX() throws ContradictionException {
        Model model = new Model();
        IntVar[] ts = new IntVar[3];
        ts[0] = model.intVar("t0", 2, 2, false);
        ts[1] = model.intVar("t1", 1, 3, false);
        ts[2] = model.intVar("t2", 1, 3, false);

        model.allDifferent(ts, "BC").post();

        model.getSolver().propagate();
        assertEquals(ts[1].getDomainSize(), 2);
        assertEquals(ts[2].getDomainSize(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testB() {
        Model model = new Model();
        IntVar[] ts = new IntVar[3];
        ts[0] = model.intVar("t0", 2, 4, true);
        ts[1] = model.intVar("t1", 1, 3, true);
        ts[2] = model.intVar("t2", 1, 3, true);

        model.allDifferent(ts, "FC").post();

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 10);
        assertEquals(model.getSolver().getNodeCount(), 23);
    }

    @Test(groups="1s", timeOut=60000)
    public void testE() {
        Model model = new Model();
        IntVar[] ts = new IntVar[3];
        ts[0] = model.intVar("t0", 2, 4, false);
        ts[1] = model.intVar("t1", 1, 3, false);
        ts[2] = model.intVar("t2", 1, 3, false);

        model.allDifferent(ts, "FC").post();

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 10);
        assertEquals(model.getSolver().getNodeCount(), 19);
    }

    @Test(groups="1s", timeOut=60000)
    public void testB1() throws ContradictionException {
        Model model = new Model();
        IntVar[] X = new IntVar[32];
        X[0] = model.intVar("V0", new int[]{-1, 19, 24, 25});
        X[1] = model.intVar("V1", new int[]{-13, 1, 12});
        X[2] = model.intVar("V2", new int[]{-9, -8, 9, 19});
        X[3] = model.intVar("V3", new int[]{6});
        X[4] = model.intVar("V4", new int[]{-3, 4, 11, 12});
        X[5] = model.intVar("V5", new int[]{-6, 25});
        X[6] = model.intVar("V6", new int[]{7, 12, 21});
        X[7] = model.intVar("V7", new int[]{4, 7, 11, 12});
        X[8] = model.intVar("V8", new int[]{-8, -4, 0, 21});
        X[9] = model.intVar("V9", new int[]{-3, 12});
        X[10] = model.intVar("X10", new int[]{0});
        X[11] = model.intVar("X11", new int[]{-15, -3, 3});
        X[12] = model.intVar("X12", new int[]{-5, 3, 21, 24});
        X[13] = model.intVar("X13", new int[]{3});
        X[14] = model.intVar("X14", new int[]{-16, 13, 16});
        X[15] = model.intVar("X15", new int[]{-14, -12, 0, 20});
        X[16] = model.intVar("X16", new int[]{-9, 11});
        X[17] = model.intVar("X17", new int[]{-15, 13});
        X[18] = model.intVar("X18", new int[]{-12, -4, 21});
        X[19] = model.intVar("X19", new int[]{-1});
        X[20] = model.intVar("X20", new int[]{2, 11, 14});
        X[21] = model.intVar("X21", new int[]{-9, 7, 21});
        X[22] = model.intVar("X22", new int[]{-16, 10, 15});
        X[23] = model.intVar("X23", new int[]{20, 24});
        X[24] = model.intVar("X24", new int[]{23});
        X[25] = model.intVar("X25", new int[]{-7, 5});
        X[26] = model.intVar("X26", new int[]{-2, 1, 10, 12});
        X[27] = model.intVar("X27", new int[]{-16, -6, 12, 15});
        X[28] = model.intVar("X28", new int[]{-9});
        X[29] = model.intVar("X29", new int[]{-6, -4});
        X[30] = model.intVar("X30", new int[]{-15, -2, -1, 3});
        X[31] = model.intVar("X31", new int[]{1, 10, 14});

        model.allDifferent(X, "AC").post();
        model.getSolver().propagate();
        assertEquals(X[14].getUB(), 16);
        assertEquals(X[14].getLB(), -16);
        assertEquals(X[14].getDomainSize(), 2);
    }

    @Test
    public void testUF() throws ContradictionException {
        Model choco = new Model();

        IntVar x0 = choco.intVar(new int[]{0});
        IntVar x1 = choco.intVar(new int[]{602499212});
        IntVar x2 = choco.intVar(new int[]{-1578598400,-1578598399,-1578598398,-1578598395,-1578598394});

        choco.post(choco.allDifferent(new IntVar[]{x0, x1, x2}, "BC"));
        choco.getSolver().propagate();  // Throws contradiction (which is obviously incorrect)

        System.out.println("x0 = " + x0); // should be left untouched
        System.out.println("x1 = " + x1);  // should be left untouched
        System.out.println("x2 = " + x2); // should be left untouched
    }
}