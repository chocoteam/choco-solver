/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.modeling;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.hybrid.HybridTuples;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.extension.hybrid.HybridTuples.any;
import static org.chocosolver.solver.constraints.extension.hybrid.HybridTuples.col;

/**
 * Examples shown in the user documentation, page "Modeling / Constraints over integer variables"
 * (docs/content/docs/Modeling/IntConstraints.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the page.
 *
 * @author Charles Prud'homme
 */
public class IntConstraintsExamples {

    @Test(groups = "1s")
    public void arithm() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 5);
        IntVar y = model.intVar("y", 0, 5);
        IntVar z = model.intVar("z", 0, 10);
        // tag::arithm[]
        model.arithm(x, ">=", 3).post();
        // end::arithm[]
        // tag::arithm2[]
        String op = ">="; // among ">=", ">", "<=", "<", "=" and "!="
        model.arithm(x, op, y).post();
        // with x an IntVar and y either an IntVar or an int

        String op2 = "+"; // either "+" or "-"
        model.arithm(x, op2, y, "=", z).post();
        // with x and y IntVar, and z either an int or an IntVar
        // end::arithm2[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(x.getValue() + y.getValue(), z.getValue());
    }

    @Test(groups = "1s")
    public void mathematical() {
        Model model = new Model();
        IntVar x = model.intVar("x", -5, 5);
        IntVar y = model.intVar("y", -5, 5);
        IntVar z = model.intVar("z", -25, 25);
        int a = 2;
        int b = 1;
        // tag::times[]
        model.times(x, a, z).post(); // x * a = z
        model.times(x, y, 6).post(); // x * y = 6
        model.times(x, y, z).post(); // x * y = z
        // end::times[]
        Assert.assertTrue(model.getSolver().solve());

        model = new Model();
        x = model.intVar("x", -5, 5);
        y = model.intVar("y", -5, 5);
        z = model.intVar("z", -25, 25);
        // tag::div[]
        model.div(x, y, z).post(); // x / y = z
        // it ensures that y != 0, and z is rounded towards 0
        // end::div[]
        // tag::mod[]
        model.mod(x, a, b).post(); // x % a = b
        model.mod(x, a, z).post(); // x % a = z
        model.mod(x, y, z).post(); // x % y = z
        // end::mod[]
        Assert.assertTrue(model.getSolver().solve());

        model = new Model();
        x = model.intVar("x", -5, 5);
        y = model.intVar("y", -5, 5);
        // tag::absolute[]
        model.absolute(x, y).post(); // x = |y|
        // end::absolute[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(x.getValue(), Math.abs(y.getValue()));

        model = new Model();
        IntVar base = model.intVar("base", 1, 3);
        IntVar exponent = model.intVar("exponent", 0, 3);
        x = model.intVar("x", 0, 30);
        IntVar x2 = model.intVar("x2", 0, 30);
        // tag::pow[]
        model.pow(base, exponent, x).post(); // x = base ^ exponent
        model.pow(base, 2, x2).post();       // x2 = base ^ 2
        // end::pow[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void minMaxSum() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("v", 4, 0, 5);
        IntVar min = model.intVar("min", 0, 5);
        IntVar max = model.intVar("max", 0, 5);
        IntVar x = model.intVar("x", 0, 20);
        int[] coefs = {1, 2, 3, 4};
        // tag::minMax[]
        model.min(min, vars).post();
        model.max(max, vars).post();
        // end::minMax[]
        // tag::sum[]
        String op = ">="; // among ">=", ">", "<=", "<", "=" and "!="
        model.sum(vars, op, x).post(); // sum(vars) >= x
        // end::sum[]
        // tag::scalar[]
        model.scalar(vars, coefs, "=", x).post(); // sum(vars[i] * coefs[i]) = x
        // end::scalar[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void allDifferent() {
        Model model = new Model();
        // tag::allDifferent[]
        IntVar[] vars = model.intVarArray("X", 4, 1, 5);
        model.allDifferent(vars).post();
        // end::allDifferent[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void count() {
        Model model = new Model();
        // tag::count[]
        IntVar occ0 = model.intVar("occ0", 0, 5);
        IntVar[] vars = model.intVarArray("X", 7, 0, 5);
        model.count(0, vars, occ0).post(); // occ0 is the number of variables of vars equal to 0
        // end::count[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void globalCardinality() {
        Model model = new Model();
        // tag::globalCardinality[]
        IntVar[] vars = model.intVarArray("X", 7, 0, 5);
        int[] values = new int[]{1, 3, 5};
        IntVar[] occs = model.intVarArray("O", 3, 0, 3);
        model.globalCardinality(vars, values, occs, false).post();
        // end::globalCardinality[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void element() {
        Model model = new Model();
        // tag::element[]
        IntVar idx = model.intVar("I", 0, 5, false);
        IntVar rst = model.intVar("R", 0, 10, false);
        model.element(rst, new int[]{0, 2, 4, 6, 7}, idx).post(); // rst = table[idx]
        // end::element[]
        model.arithm(idx, "=", 3).post();
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(rst.getValue(), 6);
    }

    @Test(groups = "1s")
    public void tasks() {
        Model model = new Model();
        IntVar start = model.intVar("start", 0, 10);
        int duration = 3;
        // tag::task[]
        Task task = model.taskVar(start, duration);
        // end::task[]
        IntVar end = model.intVar("end", 0, 20);
        // tag::taskEnd[]
        Task task2 = model.taskVar(start, duration, end);
        task2.post(); // start + duration = end
        // end::taskEnd[]
        IntVar varDuration = model.intVar("varDuration", 1, 5);
        IntVar end2 = model.intVar("end2", 0, 20);
        // tag::taskVarDuration[]
        Task task3 = model.taskVar(start, varDuration, end2);
        task3.post(); // start + varDuration = end2
        // end::taskVarDuration[]
        // tag::taskBounds[]
        // earliest start, latest start, duration, earliest end, latest end
        Task task4 = model.taskVar(0, 5, 3, 3, 8);
        // end::taskBounds[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(end.getValue(), start.getValue() + duration);
        Assert.assertEquals(end2.getValue(), start.getValue() + varDuration.getValue());
    }

    @Test(groups = "1s")
    public void cumulative() {
        Model model = new Model();
        IntVar[] starts = model.intVarArray("start", 4, 0, 20);
        int[] durations = {2, 3, 1, 4};
        // tag::cumulative[]
        Task[] tasks = new Task[4];
        IntVar[] heights = new IntVar[4];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = model.taskVar(starts[i], durations[i]);
            heights[i] = model.intVar(1);
        }
        IntVar capacity = model.intVar(1);
        model.cumulative(tasks, heights, capacity).post();
        // end::cumulative[]
        Assert.assertTrue(model.getSolver().solve());
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                Assert.assertTrue(tasks[i].getEnd().getValue() <= tasks[j].getStart().getValue()
                        || tasks[j].getEnd().getValue() <= tasks[i].getStart().getValue());
            }
        }
    }

    @Test(groups = "1s")
    public void tables() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 4, 0, 3);
        // tag::tableAllowed[]
        Tuples allEqual = new Tuples(true); // true stands for 'allowed' combinations
        allEqual.add(0, 0, 0, 0);
        allEqual.add(1, 1, 1, 1);
        allEqual.add(2, 2, 2, 2);
        allEqual.add(3, 3, 3, 3);
        model.table(X, allEqual, "CT+").post();
        // end::tableAllowed[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(X[0].getValue(), X[3].getValue());

        model = new Model();
        X = model.intVarArray("X", 4, 0, 3);
        // tag::tableForbidden[]
        Tuples notAllEqual = new Tuples(false); // false stands for 'forbidden' combinations
        notAllEqual.add(0, 0, 0, 0);
        notAllEqual.add(1, 1, 1, 1);
        notAllEqual.add(2, 2, 2, 2);
        notAllEqual.add(3, 3, 3, 3);
        model.table(X, notAllEqual, "CT").post(); // CT+ only handles allowed tuples
        // end::tableForbidden[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void universalValue() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 3, 0, 99);
        // tag::universal[]
        int STAR = -1;
        Tuples rel = new Tuples(STAR); // allowed combinations, with STAR as universal value
        rel.add(0, 0, STAR);
        rel.add(3, 2, 1);
        model.table(X, rel).post();
        // end::universal[]
        model.arithm(X[2], "=", 42).post();
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(X[0].getValue(), 0);
    }

    @Test(groups = "1s")
    public void hybridTuples() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 3);
        IntVar y = model.intVar("y", 0, 3);
        IntVar z = model.intVar("z", 0, 3);
        // tag::hybrid[]
        // x, y and z are equal: any value for x, then y and z equal to the first column
        HybridTuples tuples = new HybridTuples();
        tuples.add(any(), col(0), col(0));
        model.table(new IntVar[]{x, y, z}, tuples).post();
        // end::hybrid[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(x.getValue(), z.getValue());
    }

    @Test(groups = "1s")
    public void clauses() {
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 5);
        BoolVar a = bs[0], b = bs[1], c = bs[2], d = bs[3], e = bs[4];
        // tag::clauses[]
        model.addClauses(LogOp.and(LogOp.nand(LogOp.nor(a, b), LogOp.or(c, d)), e));
        // end::clauses[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(e.getValue(), 1);
    }
}
