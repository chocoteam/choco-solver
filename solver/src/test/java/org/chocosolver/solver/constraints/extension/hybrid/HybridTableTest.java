package org.chocosolver.solver.constraints.extension.hybrid;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.extension.hybrid.HybridTuples.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/02/2023
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
        tuples.add(any(), eq(col(0).add(1)), eq(col(0).sub(1)));

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
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), ne(col(0).add(1)));

        model.table(new IntVar[]{x, y}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y));
        while (solver.solve()) {
            System.out.printf("(%d, %d)\n",
                    x.getValue(), y.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 7);
    }

    @Test(groups = "1s")
    public void test4b() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), ne(col(0).sub(1)));

        model.table(new IntVar[]{x, y}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y));
        while (solver.solve()) {
            System.out.printf("(%d, %d)\n",
                    x.getValue(), y.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 7);
    }

    @Test(groups = "1s")
    public void test5a() {
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 3);
        IntVar y = model.intVar("y", 1, 3);

        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), ge(col(0).add(1)));

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
        tuples.add(any(), ge(col(0).sub(1)));

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
        tuples.add(any(), le(col(0).sub(1)));

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
        tuples.add(any(), le(col(0).add(1)));

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
        tuples.add(any(), le(col(0).add(1)));

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
    public void test8() {
        // |jp[0]| =  2, |jp[1]| =  2
        //|D_1*D_2...S_n| = 4
        //or(ne(jp[0],0),eq(jp[1],0))
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1);
        IntVar y = model.intVar("y", 0, 1);

        HybridTuples tuples = new HybridTuples();
        tuples.add(ne(0), any());
        tuples.add(any(), eq(0));

        model.table(new IntVar[]{x, y}, tuples).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(x, y));
        while (solver.solve()) {
            System.out.printf("(%d, %d)\n",
                    x.getValue(), y.getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 3);
    }
}