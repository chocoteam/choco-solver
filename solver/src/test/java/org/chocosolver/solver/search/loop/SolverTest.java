/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop;

import org.chocosolver.cutoffseq.LubyCutoffStrategy;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.propagation.PropagationProfiler;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.search.loop.move.MoveBinaryDFS;
import org.chocosolver.solver.search.loop.move.MoveBinaryLDS;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.FullyRandom;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import static org.chocosolver.solver.search.strategy.Search.*;
import static org.chocosolver.util.ProblemMaker.makeGolombRuler;
import static org.chocosolver.util.ProblemMaker.makeNQueenWithOneAlldifferent;
import static org.testng.Assert.assertEquals;

/**
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 */
public class SolverTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testReset() {
        Model m = new Model();
        IntVar x = m.intVar("X", 0, 9);
        IntVar y = m.intVar("Y", 0, 9);

        Constraint x_lesser_y = m.arithm(y, ">", x);
        x_lesser_y.post();
        m.member(x, new int[]{1, 2}).post();

        // computeOptimum
        m.setObjective(Model.MAXIMIZE, x);
        while (m.getSolver().solve()) ;
        assertEquals(m.getSolver().getSolutionCount(), 1);

        // enumerate optima does not work because of previous cut
        m.getSolver().reset();
        m.getSolver().getObjectiveManager().setCutComputer((Number number) -> number);
        while (m.getSolver().solve()) ;
        assertEquals(m.getSolver().getSolutionCount(), 7);

        // reset, remove constraint and enumerate solutions
        m.getSolver().reset();
        m.clearObjective();
        m.unpost(x_lesser_y);
        m.getSolver().setSearch(Search.defaultSearch(m));
        while (m.getSolver().solve()) ;
        assertEquals(m.getSolver().getSolutionCount(), 20);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test1DFS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.setSearch(inputOrderLBSearch(model.retrieveIntVars(true)));
        r.setDFS();

        model.getSolver().solve();
        assertEquals(r.getMeasures().getSolutionCount(), 1);
        assertEquals(r.getMeasures().getNodeCount(), 24);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test1LDS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.setSearch(inputOrderLBSearch(model.retrieveIntVars(false)));
        r.setLDS(4);
        model.getSolver().solve();
        assertEquals(r.getMeasures().getSolutionCount(), 1);
        assertEquals(r.getMeasures().getNodeCount(), 144);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test1DDS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.setSearch(inputOrderLBSearch(model.retrieveIntVars(true)));
        r.setDDS(4);
        model.getSolver().solve();
        assertEquals(r.getMeasures().getSolutionCount(), 1);
        assertEquals(r.getMeasures().getNodeCount(), 70);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void test1HBFS() {
        Model model = makeGolombRuler(8);
        Solver r = model.getSolver();
        r.setSearch(inputOrderLBSearch(model.retrieveIntVars(false)));
        r.setHBFS(.05, .1, 32);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 7);
        assertEquals(model.getSolver().getNodeCount(), 4542);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2DFS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.setDFS();
        r.setSearch(inputOrderLBSearch(model.retrieveIntVars(true)));
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 92);
        assertEquals(model.getSolver().getNodeCount(), 480);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2LDS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.setSearch(inputOrderLBSearch(model.retrieveIntVars(false)));
        r.setLDS(4);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 7);
        assertEquals(model.getSolver().getNodeCount(), 205);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2DDS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.setSearch(inputOrderLBSearch(model.retrieveIntVars(false)));
        r.setDDS(5);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 2);
        assertEquals(model.getSolver().getNodeCount(), 130);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2DDS2() {
        Model model = new Model();
        Solver r = model.getSolver();
        IntVar[] bs = model.boolVarArray("b", 4);
        r.setSearch(inputOrderLBSearch(bs));
        r.setDDS(3);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 8);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3() {
        Model model = makeGolombRuler(6);
        Solver r = model.getSolver();
        r.setDFS();
        r.setSearch(inputOrderLBSearch(model.retrieveIntVars(false)));
        while (model.getSolver().solve()) ;

        assertEquals(model.getSolver().getSolutionCount(), 3);
        assertEquals(model.getSolver().getNodeCount(), 16);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test4() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.setDFS();
        r.setSearch(inputOrderLBSearch(model.retrieveIntVars(false)));
        model.getSolver().setRestarts(limit -> model.getSolver().getNodeCount() >= limit, new LubyCutoffStrategy(2), 2);
        while (model.getSolver().solve()) ;

        assertEquals(model.getSolver().getRestartCount(), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test5() {
        Model model = makeGolombRuler(5);
        Solver r = model.getSolver();
        r.setDFS();
        r.setSearch(inputOrderLBSearch(model.retrieveIntVars(false)));
        r.setLNS(new RandomNeighborhood(model.retrieveIntVars(false), 15, 0), new NodeCounter(model, 10));
        r.limitSearch(() -> r.getMeasures().getNodeCount() >= 1000);
        while (model.getSolver().solve()) ;

        assertEquals(model.getSolver().getRestartCount(), 314);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test6() {
        Model model = makeGolombRuler(6);
        Solver r = model.getSolver();
        r.setDFS();
        r.setSearch(inputOrderLBSearch(model.retrieveIntVars(false)));
        model.getSolver().setLNS(new RandomNeighborhood(model.retrieveIntVars(false), 15, 0), new NodeCounter(model, 10));
        r.addStopCriterion(() -> r.getMeasures().getNodeCount() >= 1000);
        while (model.getSolver().solve()) ;

        assertEquals(r.getMeasures().getRestartCount(), 972);
    }


    @Test(groups = "1s", timeOut = 60000)
    public void test7() {
        Model model = new Model();
        int n = 3;
        BoolVar[] B = model.boolVarArray("b", n - 1);
        Solver r = model.getSolver();
        r.setSearch(inputOrderUBSearch(B));
        r.setLDS(1);


        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4);

    }

    @Test(groups = "1s", timeOut = 60000)
    public void test8() {
        Model model = new Model();
        int n = 3;
        IntVar[] X = model.intVarArray("X", n, 0, n, false);
        BoolVar[] B = model.boolVarArray("b", n - 1);
        for (int i = 0; i < n - 1; i++) {
            model.arithm(X[i], "<", X[i + 1]).reifyWith(B[i]);
        }
        Solver r = model.getSolver();
        r.setSearch(inputOrderUBSearch(B), greedySearch(inputOrderLBSearch(X)));
        r.setLDS(1);
        r.limitSolution(10);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test9() {
        Model model = new Model();
        int n = 3;
        IntVar[] X = model.intVarArray("X", n, 0, n, false);
        BoolVar[] B = model.boolVarArray("b", n - 1);
        for (int i = 0; i < n - 1; i++) {
            model.arithm(X[i], "<", X[i + 1]).reifyWith(B[i]);
        }

        Solver r = model.getSolver();
        r.setMove(new MoveBinaryLDS(inputOrderUBSearch(B), 1, model.getEnvironment()),
                new MoveBinaryDFS(greedySearch(inputOrderLBSearch(X)))
        );


        r.limitSolution(10);
        while (model.getSolver().solve()) ;
        assertEquals(r.getMeasures().getSolutionCount(), 4);
    }


    private int countSolutions(Solver solver, AbstractStrategy<IntVar> strategy) {
        Decision<IntVar> dec = null;
        int sol = 0;
        boolean search = true;
        while (search) {
            if (solver.moveForward(dec)) {
                dec = strategy.getDecision();
                if (dec == null) {
                    sol++;
                } else {
                    continue;
                }
            }
            search = solver.moveBackward();
            dec = strategy.getDecision();
        }
        return sol;
    }

    @Test(groups = "1s")
    public void testMoves1() {
        Model model = new Model();
        IntVar x1 = model.intVar("x1", 0, 2);
        IntVar x2 = model.intVar("x2", 0, 2);
        x1.eq(x2).post();
        Solver solver = model.getSolver();
        IntStrategy strategy = inputOrderLBSearch(x1, x2);
        Assert.assertEquals(3, countSolutions(solver, strategy));
    }

    @Test(groups = "1s")
    public void testMoves2() {
        Model model = new Model();
        IntVar x1 = model.intVar("x1", 0, 2);
        IntVar x2 = model.intVar("x2", 0, 2);
        x1.ne(x2).post();
        x1.eq(x2).post();
        Solver solver = model.getSolver();
        IntStrategy strategy = inputOrderLBSearch(x1, x2);
        Assert.assertEquals(0, countSolutions(solver, strategy));
    }


    @Test(groups = "1s")
    public void testMoves3() {
        Model model = new Model();
        IntVar x1 = model.intVar("x1", 0, 2);
        IntVar x2 = model.intVar("x2", 4, 5);
        x1.eq(x2).post();
        Solver solver = model.getSolver();
        IntStrategy strategy = inputOrderLBSearch(x1, x2);
        Assert.assertEquals(0, countSolutions(solver, strategy));
    }


    @Test(groups = "1s")
    public void testMessage() {
        Model choco = new Model(Settings.init().setWarnUser(true));
        Constraint expr = choco.arithm(choco.intVar(1, 2), "<", 2);
        expr.getOpposite().post();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        Solver solver = choco.getSolver();
        solver.logWithANSI(true);
        solver.log().add(new PrintStream(errContent));
        solver.solve();
        Assert.assertEquals("\u001B[37mNo search strategies defined.\n" +
                "\u001B[0m\u001B[37mSet to default ones.\n" +
                "\u001B[0m", errContent.toString());
        solver.hardReset();
        solver.logWithANSI(false);
        errContent.reset();
        solver.solve();
        Assert.assertEquals("No search strategies defined.\n" +
                "Set to default ones.\n", errContent.toString());

    }

    @Test(groups = "1s", timeOut = 60000)
    public void testProfiler() {
        Model model = new Model();
        int[] itemSize = new int[]{4096, 4096, 4096, 4096, 4096, 4096};
        IntVar[] itemBin = new IntVar[6];
        itemBin[0] = model.intVar("VGU0", 0, 1);
        itemBin[1] = model.intVar("VGU1", new int[]{0, 2});
        itemBin[2] = model.intVar("VGU2", 1, 2);
        itemBin[3] = model.intVar("VGU3", 1, 2);
        itemBin[4] = model.intVar("VGU4", 0);
        itemBin[5] = model.intVar("VGU5", 0);
        IntVar[] binLoad = model.intVarArray("binLoad", 3, 0, 8192);
        model.binPacking(itemBin, itemSize, binLoad, 0).post();
        Solver solver = model.getSolver();
        PropagationProfiler p = solver.profilePropagation();
        solver.setSearch(new FullyRandom(model.retrieveIntVars(true), 0));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 2);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        p.writeTo(pw, true);
        pw.flush();
        Assert.assertEquals(baos.toString(), "Propagators\n" +
                " \n" +
                "* id      : row id\n" +
                "* coarse  : for a given propagator, number of coarse propagations, i.e., calls to `propagate(int)`\n" +
                "* fine    : for a given propagator, number of fine propagations, i.e., calls to `propagate(int,int)`\n" +
                "* filter  : for a given propagator, number of times a call to propagation removes a value from a variable's domain\n" +
                "* fails   : for a given propagator, number of times it throws a failure\n" +
                "* name    : name of the given propagator \n" +
                " \n" +
                " id        coarse      fine    filter     fails  name\n" +
                " 0              3         2         2         0  \"PropBinPacking(VGU0, VGU1, VGU2, ..., binLoad[2])\"\n" +
                " 1              1         0         0         0  \"binLoad[0] + binLoad[1] + binLoad[2] = 24576\"\n" +
                " 2              1         0         0         0  \"true\"\n" +
                "Total           5         2         2         0\n" +
                "\n" +
                "Integer variables\n" +
                " \n" +
                "* id      : row id\n" +
                "* inst    : for a given integer variable, number of instantiation events\n" +
                "* lower   : for a given integer variable, number of lower bound increasing events\n" +
                "* upper   : for a given integer variable, number of upper bound decreasing events\n" +
                "* bounds  : for a given integer variable, number of bounds modification events\n" +
                "* remove  : for a given integer variable, number of value removal events\n" +
                "* name    : name of the given variable \n" +
                " \n" +
                " id          inst     lower     upper    bounds    remove  name\n" +
                " 0              1         0         0         0         0  \"VGU0\"\n" +
                " 1              1         0         0         0         0  \"VGU1\"\n" +
                " 2              2         0         0         0         0  \"VGU2\"\n" +
                " 3              2         0         0         0         0  \"VGU3\"\n" +
                " 4              0         0         0         0         0  \"VGU4\"\n" +
                " 5              0         0         0         0         0  \"VGU5\"\n" +
                " 6              1         0         0         0         0  \"binLoad[0]\"\n" +
                " 7              1         0         0         0         0  \"binLoad[1]\"\n" +
                " 8              1         0         0         0         0  \"binLoad[2]\"\n" +
                " 9              0         0         0         0         0  \"cste -- 24576\"\n" +
                " 10             0         0         0         0         0  \"cste -- 1\"\n\n");
    }
}
