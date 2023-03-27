/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.hybrid;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.chocosolver.solver.constraints.extension.hybrid.HybridTuples.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/03/2023
 */
public class HybridTableTest {

    @Test(groups = "1s")
    public void test1() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);
        IntVar z = model.intVar("z", 1, 3);
        int a = 1, b = 2, c = 3;

        HybridTuples tuples = new HybridTuples();
        tuples.add(ne(a), any(), eq(c));
        tuples.add(eq(c), le(b), ne(a));
        tuples.add(lt(c), eq(b), ne(b));
        tuples.add(gt(b), ge(b), any());

        model.table(new IntVar[]{x, y, z}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y, z));
        while (solver.solve()) {
            System.out.printf("(%d, %d, %d)\n",
                    x.getValue(), y.getValue(), z.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 14);
    }

    @Test(groups = "1s")
    public void test2() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);
        IntVar z = model.intVar("z", 1, 3);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), col(0), col(0));

        model.table(new IntVar[]{x, y, z}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y, z));
        while (solver.solve()) {
            System.out.printf("(%d, %d, %d)\n",
                    x.getValue(), y.getValue(), z.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 3);
    }


    @Test(groups = "1s")
    public void test3b() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 5);
        IntVar y = model.intVar("y", 1, 5);
        IntVar z = model.intVar("z", 1, 5);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), eq(col(0), 1), eq(col(0), -1));

        model.table(new IntVar[]{x, y, z}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y, z));
        while (solver.solve()) {
            System.out.printf("(%d, %d, %d)\n",
                    x.getValue(), y.getValue(), z.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 3);
    }


    @Test(groups = "1s")
    public void test4a() {
        Model model = new Model();
        IntVar w = model.intVar("w", 1, 2);
        IntVar x = model.intVar("x", 1, 2);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), ne(col(0), 1));

        model.table(new IntVar[]{w, x}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(w, x));
        while (solver.solve()) {
            System.out.printf("(%d, %d)\n",
                    w.getValue(), x.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 3);
    }

    @Test(groups = "1s")
    public void test4b() {
        Model model = new Model();
        IntVar w = model.intVar("w", 1, 3);
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);
        IntVar z = model.intVar("z", 1, 3);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), ne(col(0)), ne(col(0), +1), ne(col(0), -1));

        model.table(new IntVar[]{w, x, y, z}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(w, x, y, z));
        while (solver.solve()) {
            System.out.printf("(%d, %d, %d, %d)\n",
                    w.getValue(), x.getValue(), y.getValue(), z.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 32);
    }

    @Test(groups = "1s")
    public void test5a() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), ge(col(0), 1));

        model.table(new IntVar[]{x, y}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y));
        while (solver.solve()) {
            System.out.printf("(%d, %d)\n",
                    x.getValue(), y.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 3);
    }

    @Test(groups = "1s")
    public void test5b() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), ge(col(0), -1));

        model.table(new IntVar[]{x, y}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y));
        while (solver.solve()) {
            System.out.printf("(%d, %d)\n",
                    x.getValue(), y.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 8);
    }

    @Test(groups = "1s")
    public void test6a() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), le(col(0), -1));

        model.table(new IntVar[]{x, y}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y));
        while (solver.solve()) {
            System.out.printf("(%d, %d)\n",
                    x.getValue(), y.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 3);
    }

    @Test(groups = "1s")
    public void test6b() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), le(col(0), 1));

        model.table(new IntVar[]{x, y}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y));
        while (solver.solve()) {
            System.out.printf("(%d, %d)\n",
                    x.getValue(), y.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 8);
    }

    @Test(groups = "1s")
    public void test7() {
        // 
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), le(col(0), 1));

        model.table(new IntVar[]{x, y}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y));
        while (solver.solve()) {
            System.out.printf("(%d, %d)\n",
                    x.getValue(), y.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 8);
    }

    private void run(Model model, IntVar[] vars, HybridTuples tuples, int nbSolutions) {
        model.table(vars, tuples).post();
        Solver solver = model.getSolver();
        while (solver.solve()) {
            Arrays.stream(vars).forEach(v -> System.out.printf("%d, ", v.getValue()));
            System.out.print("\n");
        }
        Assert.assertEquals(solver.getSolutionCount(), nbSolutions);
    }

    @Test(groups = "1s")
    public void test8() {
        //or(ne(jp[0],0),eq(jp[1],0))
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1);
        IntVar y = model.intVar("y", 0, 1);
        HybridTuples tuples = new HybridTuples();
        tuples.add(ne(0), any());
        tuples.add(any(), eq(0));
        run(model, new IntVar[]{x, y}, tuples, 3);
    }

    @Test(groups = "1s")
    public void test9() {
        // |done[0]| =  2
        //|state[1][0]| =  17
        //|state[1][1]| =  17
        //or(eq(done[0],0),eq(state[1][0],state[1][1]))
        Model model = new Model();
        IntVar b = model.intVar("b", 0, 1);
        IntVar x = model.intVar("x", 0, 16);
        IntVar y = model.intVar("y", 0, 16);
        HybridTuples tuples = new HybridTuples();
        tuples.add(eq(0), any(), any());
        tuples.add(any(), any(), col(1));
        run(model, new IntVar[]{b, x, y}, tuples, 306);
    }

    @Test(groups = "1s")
    public void test10() {
        // |done[0]| =  2
        // |state[0][0]| =  2
        // |state[0][1]| =  2
        // |move[1][0]| =  17
        // or(iff(ne(state[0][0],state[0][1]),eq(move[1][0],0)),done[0])
        Model model = new Model();
        IntVar done = model.intVar("done", 0, 1);
        IntVar state0 = model.intVar("state0", 0, 1);
        IntVar state1 = model.intVar("state1", 0, 1);
        IntVar move = model.intVar("move", 0, 16);
        HybridTuples tuples = new HybridTuples();
        tuples.add(eq(1), any(), any(), any());
        tuples.add(any(), any(), ne(col(1)), eq(0));
        run(model, new IntVar[]{done, state0, state1, move}, tuples, 70);
    }

    @Test(groups = "1s")
    public void test11() {
        // |locked[1][0]| =  2
        //|state[1][0]| =  17
        //|locked[13][0]| =  2
        //|D_1*D_2...S_n| = 68
        //eq(and(eq(state[1][0],13),locked[13][0]),locked[1][0])
        Model model = new Model();
        IntVar locked1 = model.intVar("locked1", 0, 1);
        IntVar state1 = model.intVar("state1", 0, 16);
        IntVar locked13 = model.intVar("locked13", 0, 1);
        HybridTuples tuples = new HybridTuples();
        tuples.add(eq(13), eq(1), eq(1));
        tuples.add(eq(13), ne(1), eq(0));
        tuples.add(ne(13), eq(1), eq(0));
        run(model, new IntVar[]{state1, locked1, locked13}, tuples, 18);
    }

    @Test(groups = "1s")
    public void testCol() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);
        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), col(0));
        model.table(new IntVar[]{x, y}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(y, x));
        while (solver.solve()) {
            System.out.printf("(%d, %d)\n",
                    x.getValue(), y.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 3);
        Assert.assertEquals(solver.getFailCount(), 0);
    }

    @Test(groups = "1s")
    public void test12() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);

        HybridTuples tuples = new HybridTuples();
        tuples.add(nin(0, 2, 4, 6), in(-1,1,3, 5));

        model.table(new IntVar[]{x, y}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y));
        while (solver.solve()) {
            System.out.printf("(%d, %d)\n",
                    x.getValue(), y.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 4);
    }
}
