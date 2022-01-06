/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.search.strategy.Search.greedySearch;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/2016
 */
public class ConflictOrderingSearchTest {

    Model model;
    IntVar[] mvars;
    ConflictOrderingSearch<IntVar> cos;

    @BeforeMethod(alwaysRun = true)
    public void before() {
        model = new Model();
        mvars = model.intVarArray(10, 0, 5);
        cos = new ConflictOrderingSearch(model, Search.inputOrderLBSearch(mvars));
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testStampIt1() throws Exception {
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 1);
        Assert.assertEquals(cos.vars.get(0), mvars[0]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
        cos.stampIt(mvars[1]);
        Assert.assertEquals(cos.vars.size(), 2);
        Assert.assertEquals(cos.vars.get(1), mvars[1]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), 1);
        Assert.assertEquals(cos.prev.get(1), 0);
        Assert.assertEquals(cos.next.get(1), -1);
        Assert.assertEquals(cos.pcft, 1);
        cos.stampIt(mvars[2]);
        Assert.assertEquals(cos.vars.size(), 3);
        Assert.assertEquals(cos.vars.get(2), mvars[2]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), 1);
        Assert.assertEquals(cos.prev.get(1), 0);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), -1);
        Assert.assertEquals(cos.pcft, 2);
        cos.stampIt(mvars[3]);
        Assert.assertEquals(cos.vars.size(), 4);
        Assert.assertEquals(cos.vars.get(3), mvars[3]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), 1);
        Assert.assertEquals(cos.prev.get(1), 0);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), 3);
        Assert.assertEquals(cos.prev.get(3), 2);
        Assert.assertEquals(cos.next.get(3), -1);
        Assert.assertEquals(cos.pcft, 3);
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 4);
        Assert.assertEquals(cos.prev.get(1), -1);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), 3);
        Assert.assertEquals(cos.prev.get(3), 2);
        Assert.assertEquals(cos.next.get(3), 0);
        Assert.assertEquals(cos.prev.get(0), 3);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
        cos.stampIt(mvars[3]);
        Assert.assertEquals(cos.vars.size(), 4);
        Assert.assertEquals(cos.prev.get(1), -1);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), 0);
        Assert.assertEquals(cos.prev.get(0), 2);
        Assert.assertEquals(cos.next.get(0), 3);
        Assert.assertEquals(cos.prev.get(3), 0);
        Assert.assertEquals(cos.next.get(3), -1);
        Assert.assertEquals(cos.pcft, 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testStampIt2() throws Exception {
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 1);
        Assert.assertEquals(cos.vars.get(0), mvars[0]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 1);
        Assert.assertEquals(cos.vars.get(0), mvars[0]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testStampIt3() throws Exception {
        for(int n = 3; n < 20; n++) {
            IntVar[] cvars = model.intVarArray(n, 1, 1);
            Random rnd = new Random(0);
            for (int i = 0; i < 200; i++) {
                for (int j = 0; j < (n * 3 / 2) + 1; j++) {
                    int x = rnd.nextInt(n);
                    cos.stampIt(cvars[x]);
                    Assert.assertTrue(cos.check());
                    Assert.assertNull(cos.firstNotInst());
                }
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testfirstNotInst() throws ContradictionException {
        cos.stampIt(mvars[0]);
        cos.stampIt(mvars[1]);
        cos.stampIt(mvars[2]);
        cos.stampIt(mvars[3]);
        cos.stampIt(mvars[4]);
        cos.stampIt(mvars[5]);
        Assert.assertEquals(cos.firstNotInst(), mvars[5]);
        mvars[5].instantiateTo(0, Cause.Null);
        Assert.assertEquals(cos.firstNotInst(), mvars[4]);
        mvars[4].instantiateTo(0, Cause.Null);
        mvars[3].instantiateTo(0, Cause.Null);
        Assert.assertEquals(cos.firstNotInst(), mvars[2]);
        mvars[2].instantiateTo(0, Cause.Null);
        Assert.assertEquals(cos.firstNotInst(), mvars[1]);
        mvars[1].instantiateTo(0, Cause.Null);
        mvars[0].instantiateTo(0, Cause.Null);
        Assert.assertNull(cos.firstNotInst());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testM1(){
        Model model = ProblemMaker.makeGolombRuler(6);
        model.getSolver().setSearch(Search.conflictOrderingSearch(
                Search.domOverWDegSearch(model.retrieveIntVars(
                        true
                ))
        ));
        model.getSolver().findSolution();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testM2(){
        Model model = new Model();
        IntVar x = model.intVar(0,1);
        x.eq(0).post();
        x.eq(1).post();
        model.getSolver().setSearch(Search.conflictOrderingSearch(
                Search.domOverWDegSearch(model.retrieveIntVars(
                        true
                ))
        ));
        model.getSolver().findSolution();
    }

    @Test(groups="1s", timeOut=60000)
    public void cosTest() {
        Model m = new Model();

        IntVar[] X = m.intVarArray("X",5,0,5);
        IntVar[] Y = m.intVarArray("Y",5,0,5);
        m.allDifferent(Y).post();
        m.arithm(Y[1],"=",Y[3]).post();
        Solver s = m.getSolver();
        s.setSearch(
                Search.conflictOrderingSearch(
                        Search.intVarSearch(new InputOrder<>(m), var -> {
                            Assert.assertFalse(var.getName().contains("Y"));
                            return var.getLB();
                        },X)
                ),
                greedySearch(inputOrderLBSearch(Y))
        );
        while(s.solve());
    }
}