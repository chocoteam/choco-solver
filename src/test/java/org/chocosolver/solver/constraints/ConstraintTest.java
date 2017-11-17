/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/03/2014
 */
public class ConstraintTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testBooleanChannelingJL() {
        //#issue 190
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("bs", 3);
        SetVar s1 = model.setVar("s1", new int[]{}, new int[]{-3, -2, -1, 0, 1, 2, 3});
        SetVar s2 = model.setVar("s2", new int[]{}, new int[]{-3, -2, -1, 0, 1, 2, 3});
        model.or(model.allEqual(new SetVar[]{s1, s2}), model.setBoolsChanneling(bs, s1, 0)).post();
        while (model.getSolver().solve()) ;
        assertEquals(2040, model.getSolver().getSolutionCount());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDependencyConditions() {
        Model model = new Model();
        IntVar[] ivs = model.intVarArray("X", 4, 0, 10, false);
        model.allDifferent(ivs, "BC").post(); // boundAndInst()
        model.arithm(ivs[0], "+", ivs[1], "=", 4).post(); // all()
        model.arithm(ivs[0], ">=", ivs[2]).post(); // INST + UB or INST + LB
        model.arithm(ivs[0], "!=", ivs[3]).post(); // instantiation()

        Solver r = model.getSolver();
        r.setSearch(randomSearch(ivs, 0));
        while (model.getSolver().solve()) ;
        assertEquals(r.getMeasures().getSolutionCount(), 48);
        assertEquals(r.getMeasures().getNodeCount(), 100);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDependencyConditions2() {
        Model model = new Model();
        IntVar[] ivs = model.intVarArray("X", 4, 0, 10, false);
        model.allDifferent(ivs, "BC").post(); // boundAndInst()
        model.arithm(ivs[0], "+", ivs[1], "=", 4).post(); // all()
        Constraint cr = model.arithm(ivs[0], ">=", ivs[2]);
        cr.post(); // INST + UB or INST + LB
        model.arithm(ivs[0], "!=", ivs[3]).post(); // instantiation()
        model.unpost(cr);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testOpposite1() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        Constraint co = c.getOpposite();
        Assert.assertEquals(c.getOpposite(), co);
        Assert.assertEquals(co.getOpposite(), c);
        Assert.assertFalse(c.isReified());
        Assert.assertFalse(co.isReified());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testOpposite2() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        BoolVar b = c.reify();
        Constraint co = c.getOpposite();
        Assert.assertEquals(c.getOpposite(), co);
        Assert.assertEquals(co.getOpposite(), c);
        Assert.assertTrue(c.isReified());
        Assert.assertTrue(co.isReified());
        Assert.assertEquals(b.not(), co.reify());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testOpposite3() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        Constraint co = c.getOpposite();
        BoolVar b = co.reify();
        Assert.assertEquals(c.getOpposite(), co);
        Assert.assertEquals(co.getOpposite(), c);
        Assert.assertTrue(c.isReified());
        Assert.assertTrue(co.isReified());
        Assert.assertEquals(b.not(), c.reify());
    }


    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testPostAndReif1() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        c.post();
        try {
            c.reify();
            fail();
        } catch (SolverException se) {
            se.printStackTrace(m.getSolver().getOut());
            throw se;
        }
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testPostAndReif2() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        c.reify();
        try {
            c.post();
            fail();
        } catch (SolverException se) {
            se.printStackTrace(m.getSolver().getOut());
            throw se;
        }
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testPostTwice1() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        c.post();
        try {
            c.post();
            fail();
        } catch (SolverException se) {
            se.printStackTrace(m.getSolver().getOut());
            throw se;
        }
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testPostTwice2() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        c.post();
        try {
            new Constraint("copycat", c.getPropagators());
            fail();
        } catch (SolverException se) {
            se.printStackTrace(m.getSolver().getOut());
            throw se;
        }
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testPostRemove1() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        try {
            m.unpost(c);
            fail();
        } catch (SolverException se) {
            se.printStackTrace(m.getSolver().getOut());
            throw se;
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPostRemove2() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        c.post();
        m.unpost(c);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPostRemove3() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c1 = m.arithm(v, ">", 1);
        Constraint c2 = m.arithm(v, ">", 0);
        c1.post();
        c2.post();
        m.unpost(c1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPostRemove4() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c1 = m.arithm(v, ">", 1);
        Constraint c2 = m.arithm(v, ">", 0);
        c1.post();
        c2.post();
        m.unpost(c2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPostUnpostPost1() {
        Model m = new Model();
        IntVar v = m.intVar(1, 3);
        IntVar w = m.intVar(0, 2);
        Constraint c1 = m.arithm(v, ">", w);
        c1.post();
        while (m.getSolver().solve()) ;
        Assert.assertEquals(m.getSolver().getMeasures().getSolutionCount(), 6);
        m.getSolver().reset();
        m.unpost(c1);
        while (m.getSolver().solve()) ;
        Assert.assertEquals(m.getSolver().getMeasures().getSolutionCount(), 9);
        m.getSolver().reset();
        c1.post();
        while (m.getSolver().solve()) ;
        Assert.assertEquals(m.getSolver().getMeasures().getSolutionCount(), 6);
    }

    @DataProvider(name = "unpost")
    public Object[][] providUP() {
        return new Object[][]{
                {"="}, {"!="}, {"<"}, {">"}};
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "unpost")
    public void testUnpost(String op) {
        Model m = new Model();
        {
            IntVar[] M = m.intVarArray("M", 3, 0, 2, false);
            BoolVar[][] choice = m.boolVarMatrix("choice", 3, 3);
            for (int i = 0; i < 3; i++) {
                m.boolsIntChanneling(choice[i], M[i], 0).post();
            }
            m.arithm(M[0], "=", 0).post();
            m.arithm(M[1], "=", 0).post();
            m.getSolver().setSearch(Search.inputOrderLBSearch(M));
            m.getSolver().findAllSolutions();
        }
        Model m2 = new Model();
        {
            IntVar[] M = m2.intVarArray("M", 3, 0, 2, false);
            BoolVar[][] choice = m2.boolVarMatrix("choice", 3, 3);
            for (int i = 0; i < 3; i++) {
                m2.boolsIntChanneling(choice[i], M[i], 0).post();
            }
            m2.arithm(M[0], "=", 0).post();
            m2.arithm(M[1], "=", 0).post();
            // begin diff
            Constraint c2 = m2.arithm(M[2], op, M[1]);
            c2.post();
            m2.unpost(c2);
            // end diff
            m2.getSolver().setSearch(Search.inputOrderLBSearch(M));
            m2.getSolver().findAllSolutions();
        }
        assertEquals(m.getSolver().getSolutionCount(), m2.getSolver().getSolutionCount());
    }

    @Test(groups = "1s", timeOut = 6000000)
    public void testUnlink1() {
        Model model = new Model("unlink");
        IntVar[] vars = model.intVarArray("X", 3, 0, 4);
        vars[0].eq(1).post();
        vars[0].ne(0).post();
        model.post(
                model.sum(new IntVar[]{vars[0], vars[1]}, ">", 1),
                model.sum(new IntVar[]{vars[0], vars[1], vars[2]}, ">", 2)
        );
        Propagator[] propagators = vars[0].getPropagators();

        Assert.assertEquals(vars[0].getPIndices(), new int[]{0,1,0,0,0,0,0,0});
        Assert.assertEquals(vars[0].getPropagators(), propagators);

        Assert.assertEquals(vars[1].getPIndices(), new int[]{1,0,0,0,0,0,0,0});
        Assert.assertEquals(vars[1].getPropagators(), new Propagator[]{propagators[0], propagators[1], null, null, null, null, null, null});

        Assert.assertEquals(vars[2].getPIndices(), new int[]{2,0,0,0,0,0,0,0});
        Assert.assertEquals(vars[2].getPropagators(), new Propagator[]{propagators[0], null, null, null, null, null, null, null});

        Assert.assertEquals(propagators[0].getVIndices(), new int[]{0,0,0});
        Assert.assertEquals(propagators[1].getVIndices(), new int[]{1,1});
        Assert.assertEquals(propagators[2].getVIndices(), new int[]{2});
        Assert.assertEquals(propagators[3].getVIndices(), new int[]{3});

    }

    @Test(groups="1s", timeOut=6000000)
    public void testJiTee1(){
        Random rr = new Random(2); //2 gives a suitable first requirement 500 for 'load'
        Model model = new Model("model");
        IntVar load = model.intVar("load", new int[] {0, 100, 200, 300, 400, 500, 600, 700});
        IntVar dim_A = model.intVar("dim_A", new int[] {150, 195, 270, 370, 470});
        model.arithm(dim_A, "<=", 271).post();
        model.arithm(load, ">", 400).post();
        model.getEnvironment().worldPush();
        Constraint c = null;
        //Repeatedly post / unpost. This is unstable on Windows, Ibex crashes quite often. But main concern is to make this work!
        //I cannot understand why solutions are lost after the first contradiction has been found, even when propagation is not on!
        for (int round = 0; round < 350; round++) {
            //a constraint at each round: post and unpost
            int reqInt = (round % 100);
            if(c!=null)model.unpost(c);
            c = model.arithm(dim_A, ">", 5 * reqInt);
            c.post();
            while (model.getSolver().solve()) {}
            Assert.assertEquals(model.getSolver().getSolutionCount()>0, 5 * reqInt < 270);
            model.getEnvironment().worldPopUntil(0);
            model.getSolver().hardReset();
            model.getEnvironment().worldPush();
        }

    }
    @Test(groups="1s", timeOut=6000000)
    public void testJiTee2(){
        Constraint stickyCstr = null;
        Random rr = new Random(2); //2 gives a suitable first requirement 500 for 'load'
        Model model = new Model("model");
        IntVar load = model.intVar("load", new int[] {0, 100, 200, 300, 400, 500, 600, 700});
        IntVar dim_A = model.intVar("dim_A", new int[] {150, 195, 270, 370, 470});
        model.arithm(dim_A, "<=", 271).post();
        model.arithm(load, ">", 400).post();
        model.getEnvironment().worldPush();
        int reqLoad = 0;
        //Repeatedly post / unpost. This is unstable on Windows, Ibex crashes quite often. But main concern is to make this work!
        //I cannot understand why solutions are lost after the first contradiction has been found, even when propagation is not on!
        for (int round = 0; round < 350; round++) {
            //Randomly unpost a sticky constraint that remains between iterations. Probability of unpost() annd permanent removal is higher than creation and post()
            if (stickyCstr != null) {
                int r = rr.nextInt(100);
                if (r <=12 ) {
                    model.unpost(stickyCstr);
                    stickyCstr = null;
                }
            }
            //a constraint at each round: post and unpost
            Constraint c;
            int reqInt = (round % 100) * 5;
            c = model.arithm(dim_A, ">", reqInt);
            c.post();

            //Randomly post a sticky constraint that remains between iterations. Probability to post() is lower than unpost()
            int r = 0;
            if (stickyCstr == null) {
                r = rr.nextInt(100);
                if (r <=7) {
                    stickyCstr = model.arithm(load ,"=", r*100 );
                    reqLoad = r * 100;
                    model.post(stickyCstr);
                }
            }
            while (model.getSolver().solve()) {}
            Assert.assertEquals(model.getSolver().getSolutionCount() == 0, reqInt >= 270 || (stickyCstr != null && reqLoad < 500) , ""+round);
            model.unpost(c);
            model.getEnvironment().worldPopUntil(0);
            model.getSolver().hardReset();
            model.getEnvironment().worldPush();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testAmIMeYet(){
        Model model = new Model();
        model.set(new Settings() {
            @Override
            public boolean warnUser() {
                return true;
            }
            @Override
            public boolean checkDeclaredConstraints() {
                return true;
            }
        });

        IntVar varA = model.intVar("A", 0, 1);
        IntVar varB = model.intVar("B", 0, 1);
        IntVar varC = model.intVar("C", 0, 1);
        IntVar varD = model.intVar("D", 0, 1);

        Constraint eq = model.arithm(varA, "=", varB);
        model.ifThen(eq, model.arithm(varC, "=", 1));
        model.ifThen(eq, model.arithm(varD, "=", 0));

        Solver solver = model.getSolver();
        solver.showSolutions();
        solver.findAllSolutions();
    }

}

