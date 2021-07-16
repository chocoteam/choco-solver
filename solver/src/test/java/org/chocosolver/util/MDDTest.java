/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Created by cprudhom on 04/11/14.
 * Project: Choco3
 */
public class MDDTest {

    @Test(groups="1s", timeOut=60000)
    public void test0() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("X", 4, 0, 2, false);
        Tuples tuples = new Tuples();
        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples);
        Assert.assertEquals(mdd.getDiagram(), new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    }

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("X", 4, 0, 2, false);
        Tuples tuples = new Tuples();
        tuples.add(0, 0, 0, 0);
        tuples.add(0, 0, 0, 1);
        tuples.add(0, 0, 1, 0);
        tuples.add(0, 0, 1, 1);
        tuples.add(0, 1, 0, 0);
        tuples.add(0, 1, 0, 1);
        tuples.add(0, 1, 1, 0);
        tuples.add(0, 1, 1, 1);
        tuples.add(2, 2, 2, 2);
        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples);
        Assert.assertEquals(mdd.getDiagram(), new int[]{3, 0, 12, 6, 6, 0, 9, 9, 0, -1, -1, 0, 0, 0, 15, 0, 0, 18, 0, 0, -1});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("X", 3, 0, 1, false);
        Tuples tuples = new Tuples();
        tuples.add(0, 0, 0);
        tuples.add(0, 0, 1);
        tuples.add(0, 1, 0);
        tuples.add(0, 1, 1);
        tuples.add(1, 0, 0);
        tuples.add(1, 0, 1);
        tuples.add(1, 1, 0);
        tuples.add(1, 1, 1);
        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples);
        Assert.assertEquals(mdd.getDiagram(), new int[]{2, 2, 4, 4, -1, -1});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Model model = new Model();
        IntVar[] vars = new IntVar[2];
        vars[0] = model.intVar("X", -1, 0, false);
        vars[1] = model.intVar("Y", new int[]{-1, 2});
        Tuples tuples = new Tuples();
        tuples.add(0, -1);
        tuples.add(-1, 2);
        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples);
        Assert.assertEquals(mdd.getDiagram(), new int[]{6, 2, -1, 0, 0, 0, 0, 0, 0, -1});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Model model = new Model();
        IntVar[] vars = new IntVar[2];
        vars[0] = model.intVar("X", 0, 1, false);
        vars[1] = model.intVar("Y", new int[]{-1, 1});
        Tuples tuples = new Tuples();
        tuples.add(0, -1);
        tuples.add(1, -1);
        tuples.add(0, 1);
        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples);
        Assert.assertEquals(mdd.getDiagram(), new int[]{2, 5, -1, 0, -1, -1, 0, 0});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test5() {
        Model model = new Model();
        IntVar[] vars = new IntVar[3];
        vars[0] = model.intVar("V0", -1, 1, false);
        vars[1] = model.intVar("V1", -1, 1, false);
        vars[2] = model.intVar("V2", -1, 1, false);
        Tuples tuples = new Tuples();
        tuples.add(0, -1, -1);
        tuples.add(-1, 0, -1);
        tuples.add(1, -1, 0);
        tuples.add(0, 0, 0);
        tuples.add(-1, 1, 0);
        tuples.add(1, 0, 1);
        tuples.add(0, 1, 1);

        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples, true, false);
        Assert.assertEquals(mdd.getDiagram(), new int[]{6, 3, 12, 9, 15, 18, 0, 9, 15, -1, 0, 0, 15, 18, 0, 0, -1, 0, 0, 0, -1});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }

        mdd = new MultivaluedDecisionDiagram(vars, tuples, false, false);
        Assert.assertEquals(mdd.getDiagram(), new int[]{6, 3, 12, 9, 15, 18, 0, 9, 15, -1, 0, 0, 15, 18, 0, 0, -1, 0, 0, 0, -1});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }

        mdd = new MultivaluedDecisionDiagram(vars, tuples, true, true);
        Assert.assertEquals(mdd.getDiagram(), new int[]{3, 12, 18, 0, 6, 9, -1, 0, 0, 0, -1, 0, 6, 9, 15, 0, 0, -1, 9, 15, 0});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }

        mdd = new MultivaluedDecisionDiagram(vars, tuples, false, true);
        Assert.assertEquals(mdd.getDiagram(), new int[]{3, 12, 18, 0, 6, 9, -1, 0, 0, 0, -1, 0, 6, 9, 15, 0, 0, -1, 9, 15, 0});
        for (int t = 0; t < tuples.nbTuples(); t++) {
            Assert.assertTrue(mdd.exists(tuples.get(t)));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test6() {
        Model model = new Model();
        IntVar[] vars = new IntVar[3];
        vars[0] = model.intVar("X", 0, 2, false);
        vars[1] = model.intVar("Y", new int[]{0, 2});
        vars[2] = model.intVar("Z", new int[]{-2, 0, 2});
        int[][] transitions = new int[8][3];
        transitions[0] = new int[]{0,0,1};
        transitions[1] = new int[]{0,1,2};
        transitions[2] = new int[]{0,2,3};
        transitions[3] = new int[]{1,2,4};
        transitions[4] = new int[]{2,2,4};
        transitions[5] = new int[]{3,0,5};
        transitions[6] = new int[]{4,0,-1};
        transitions[7] = new int[]{5,0,-1};

        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, transitions);
        model.mddc(vars, mdd).post();

        Solver solver  = model.getSolver();
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 3);

        Assert.assertEquals(mdd.getDiagram(), new int[]{3,3,6,0,0,9,9,0,0,0,0,-1,0,0});

    }

    @Test(groups="1s", timeOut=60000)
    public void test7() {
        Model model = new Model();
        IntVar[] vars = new IntVar[3];
        vars[0] = model.intVar("X", 0, 2, false);
        vars[1] = model.intVar("Y", new int[]{0, 2});
        vars[2] = model.intVar("Z", new int[]{-2, 0, 2});
        Tuples tuples = new Tuples();
        tuples.add(0, 2, 0);
        tuples.add(1, 2, 0);
        tuples.add(2, 0, 0);

        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(vars, tuples, false, true);
        model.mddc(vars, mdd).post();

        Solver solver  = model.getSolver();
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 3);
        Assert.assertEquals(mdd.getDiagram(), new int[]{3, 3, 11, 0, 0, 6, 0, 0, -1, 0, 0, 6, 0, 0});
    }

}
