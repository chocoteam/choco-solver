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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.Math.pow;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;


/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 5 avr. 2006
 * Time: 08:42:43
 */
public class LexTest {


    @Test(groups="1s", timeOut=60000)
    public void testLessLexq() {
        for (int seed = 0; seed < 5; seed++) {
            Model model = new Model();
            int n1 = 8;
            int k = 2;
            IntVar[] vs1 = new IntVar[n1 / 2];
            IntVar[] vs2 = new IntVar[n1 / 2];
            for (int i = 0; i < n1 / 2; i++) {
                vs1[i] = model.intVar("" + i, 0, k, true);
                vs2[i] = model.intVar("" + i, 0, k, true);
            }
            model.lexLessEq(vs1, vs2).post();
            model.getSolver().setSearch(randomSearch(append(vs1, vs2), seed));
            while (model.getSolver().solve()) ;
            int kpn = (int) pow(k + 1, n1 / 2);
            assertEquals(model.getSolver().getSolutionCount(), (kpn * (kpn + 1) / 2));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testLex() {
        for (int seed = 0; seed < 5; seed++) {
            Model model = new Model();
            int n1 = 8;
            int k = 2;
            IntVar[] vs1 = new IntVar[n1 / 2];
            IntVar[] vs2 = new IntVar[n1 / 2];
            for (int i = 0; i < n1 / 2; i++) {
                vs1[i] = model.intVar("" + i, 0, k, true);
                vs2[i] = model.intVar("" + i, 0, k, true);
            }
            model.lexLess(vs1, vs2).post();
            model.getSolver().setSearch(randomSearch(append(vs1, vs2), seed));

            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 3240);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testLexiSatisfied() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", 1, 1, true);
        IntVar v2 = model.intVar("v2", 2, 2, true);
        IntVar v3 = model.intVar("v3", 3, 3, true);
        Constraint c1 = model.lexLess(new IntVar[]{v1, v2}, new IntVar[]{v1, v3});
        Constraint c2 = model.lexLess(new IntVar[]{v1, v2}, new IntVar[]{v1, v2});
        Constraint c3 = model.lexLess(new IntVar[]{v1, v2}, new IntVar[]{v1, v1});
        Constraint c4 = model.lexLessEq(new IntVar[]{v1, v2}, new IntVar[]{v1, v3});
        Constraint c5 = model.lexLessEq(new IntVar[]{v1, v2}, new IntVar[]{v1, v2});
        Constraint c6 = model.lexLessEq(new IntVar[]{v1, v2}, new IntVar[]{v1, v1});
        model.post(c1, c2, c3, c4, c5, c6);
        Assert.assertEquals(ESat.TRUE, c1.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c2.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c3.isSatisfied());
        Assert.assertEquals(ESat.TRUE, c4.isSatisfied());
        Assert.assertEquals(ESat.TRUE, c5.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c6.isSatisfied());
    }


    @Test(groups="1s", timeOut=60000)
    public void testAshish() {
        Model model = new Model();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = model.intVar("a1", 5, 7, true);
        a[1] = model.intVar("a2", 1, 1, true);

        b[0] = model.intVar("b1", 5, 8, true);
        b[1] = model.intVar("b2", 0, 0, true);


        model.lexLess(a, b).post();
        try {
            model.getSolver().propagate();

        } catch (ContradictionException e) {
            fail();
        }
//        SearchMonitorFactory.log(solver, true, true);
        while (model.getSolver().solve()) ;
        assertEquals(6, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testBug1() {
        Model model = new Model();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = model.intVar("a2", new int[]{5, 8});
        a[1] = model.intVar("a3", new int[]{-2, 0});

        b[0] = model.intVar("b2", new int[]{5, 8});
        b[1] = model.intVar("b3", new int[]{-3, -2});

        model.lexLess(a, b).post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            fail();
        }
        assertEquals(5, a[0].getUB());
//        SearchMonitorFactory.log(solver, true, true);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBug2() {
        Model model = new Model();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = model.intVar("a1", new int[]{-2, 5});
        a[1] = model.intVar("a2", new int[]{-1, 1});

        b[0] = model.intVar("b1", new int[]{3, 5});
        b[1] = model.intVar("b2", new int[]{-6, -1});


        model.lexLess(a, b).post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            fail();
        }
        assertEquals(-2, a[0].getUB());
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 8);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBug3() {
        Model model = new Model();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = model.intVar("a1", new int[]{5});
        a[1] = model.intVar("a2", new int[]{-1, 1});

        b[0] = model.intVar("b1", new int[]{3, 5});
        b[1] = model.intVar("b2", new int[]{-6, -1});


        model.lexLess(a, b).post();
        try {
            model.getSolver().propagate();
            fail();
        } catch (ContradictionException ignored) {
        }
        assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBug4() {
        Model model = new Model();
        IntVar[] a = new IntVar[5];
        IntVar[] b = new IntVar[5];

        a[0] = model.intVar("a1", new int[]{2});
        a[1] = model.intVar("a2", new int[]{1, 3, 4});
        a[2] = model.intVar("a3", new int[]{1, 2, 3, 4, 5});
        a[3] = model.intVar("a4", new int[]{1, 2});
        a[4] = model.intVar("a5", new int[]{3, 4, 5});

        b[0] = model.intVar("b1", new int[]{0, 1, 2});
        b[1] = model.intVar("b2", new int[]{1});
        b[2] = model.intVar("b3", new int[]{0, 1, 2, 3, 4});
        b[3] = model.intVar("b4", new int[]{0, 1});
        b[4] = model.intVar("b5", new int[]{0, 1, 2});


        model.lexLess(a, b).post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException ignored) {
        }
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 216);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBug5() {
        Model model = new Model();
        IntVar[] a = new IntVar[3];
        IntVar[] b = new IntVar[3];

        a[0] = model.intVar("a1", new int[]{-10, -3, 2});
        a[1] = model.intVar("a2", new int[]{-5, -4, 2});
        a[2] = model.intVar("a3", new int[]{2});

        b[0] = model.intVar("b1", new int[]{-10, -1, 3});
        b[1] = model.intVar("b2", new int[]{-5});
        b[2] = model.intVar("b3", new int[]{-4, 2});


        model.lexLess(a, b).post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            fail();
        }
        assertEquals(-1, b[0].getLB());
//        SearchMonitorFactory.log(solver, true, false);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 30);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = SolverException.class)
    public void outOfBoundsInLexLE() throws Exception{
        Model choco = new Model();

        IntVar[] x = new IntVar[]{
                choco.intVar(new int[]{2147483646}),
                choco.intVar(new int[]{-800571629,-800571628,-800571626,-800571624,-800571622}),
                choco.intVar(new int[]{-805251943,-805251939,-805251936,-805251935}),
                choco.intVar(new int[]{-937887857}),
        };

        IntVar[] y = new IntVar[]{
                choco.intVar(new int[]{2147483641,2147483643,2147483644,2147483646}),
                choco.intVar(new int[]{-3,0,2,3,4})
        };

        choco.post(choco.lexLessEq(x, y));
        choco.getSolver().propagate();
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = SolverException.class)
    public void outOfBoundsInLexLT() throws Exception{
        Model choco = new Model();

        IntVar[] x = new IntVar[]{
                choco.intVar(new int[]{2147483646}),
                choco.intVar(new int[]{-800571629,-800571628,-800571626,-800571624,-800571622}),
                choco.intVar(new int[]{-805251943,-805251939,-805251936,-805251935}),
                choco.intVar(new int[]{-937887857}),
        };

        IntVar[] y = new IntVar[]{
                choco.intVar(new int[]{2147483641,2147483643,2147483644,2147483646}),
                choco.intVar(new int[]{-3,0,2,3,4})
        };

        choco.post(choco.lexLess(x, y));
        choco.getSolver().propagate();
    }
}
