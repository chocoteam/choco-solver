/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sat;

import static org.chocosolver.solver.constraints.nary.sat.PropNogoods.iseq;
import static org.chocosolver.solver.constraints.nary.sat.PropNogoods.ivalue;
import static org.chocosolver.solver.constraints.nary.sat.PropNogoods.leq;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.util.Random;
import org.chocosolver.sat.SatSolver;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for PropNogoods
 * Created by cprudhom on 25/11/2015.
 * Project: choco.
 */
public class PropNogoodsTest {

    IntVar[] vars;
    PropNogoods PNG;
    int[] lits;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        Model model = new Model("nogoods");
        vars = model.intVarArray("X", 4, -1, 1, false);
        PNG = model.getNogoodStore().getPropNogoods();
        lits = new int[6];
        lits[0] = PNG.Literal(vars[0], 0, true);
        lits[1] = PNG.Literal(vars[0], 0, false);
        lits[2] = PNG.Literal(vars[1], 0, true);
        lits[3] = PNG.Literal(vars[1], 0, false);
        lits[4] = PNG.Literal(vars[2], 0, true);
        lits[5] = PNG.Literal(vars[2], 0, false);
        PNG.initialize();
        TIntList list = new TIntArrayList();
        list.add(SatSolver.negated(lits[0]));
        list.add(lits[1]);
        PNG.addNogood(list);
        list.clear();
        list.add(SatSolver.negated(lits[2]));
        list.add(lits[3]);
        PNG.addNogood(list);
        list.clear();
        list.add(SatSolver.negated(lits[4]));
        list.add(lits[5]);
        PNG.addNogood(list);
        PNG.propagate(2);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testPropagate() throws Exception {
        try {
            PNG.propagate(2);
        }catch (ContradictionException c){
            Assert.fail();
        }
        Assert.assertEquals(vars[0].getDomainSize(), 3);
        Assert.assertEquals(vars[1].getDomainSize(), 3);
        Assert.assertEquals(vars[2].getDomainSize(), 3);

        TIntList list = new TIntArrayList();
        list.add(SatSolver.negated(lits[0]));
        list.add(lits[2]);
        PNG.addNogood(list);
        list.clear();
        list.add(SatSolver.negated(lits[2]));
        list.add(lits[4]);
        PNG.addNogood(list);
        vars[0].instantiateTo(0, Cause.Null);
        try {
            PNG.propagate(2);
        }catch (ContradictionException c){
            Assert.fail();
        }
        Assert.assertTrue(vars[0].isInstantiatedTo(0));
        Assert.assertTrue(vars[1].isInstantiatedTo(0));
        Assert.assertTrue(vars[2].isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testPropagate1() throws Exception {
        PNG.propagate(2);
        TIntList list = new TIntArrayList();
        list.add(SatSolver.negated(lits[0]));
        list.add(lits[2]);
        PNG.addNogood(list);
        list.clear();
        list.add(SatSolver.negated(lits[2]));
        list.add(lits[4]);
        PNG.addNogood(list);
        vars[0].instantiateTo(0, Cause.Null);
        try {
            PNG.propagate(0, 15);
        }catch (ContradictionException c){
            Assert.fail();
        }
        Assert.assertTrue(vars[0].isInstantiatedTo(0));
        Assert.assertTrue(vars[1].isInstantiatedTo(0));
        Assert.assertTrue(vars[2].isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testIsEntailed1() throws Exception {
        vars[0].instantiateTo(0, Cause.Null);
        vars[1].instantiateTo(1, Cause.Null);
        vars[2].instantiateTo(-1, Cause.Null);
        Assert.assertEquals(PNG.isEntailed(), ESat.TRUE);
    }

    @Test(groups="1s", timeOut=60000)
    public void testIsEntailed2() throws Exception {
        Assert.assertEquals(PNG.isEntailed(), ESat.UNDEFINED);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLiteral1() throws Exception {
        Assert.assertTrue(lits[0] == 1);
        Assert.assertTrue(lits[1] == 3);
        Assert.assertTrue(lits[2] == 5);
        Assert.assertTrue(lits[3] == 7);
        Assert.assertTrue(lits[4] == 9);
        Assert.assertTrue(lits[5] == 11);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLiteral2() throws Exception {
        BoolVar[] b = vars[0].getModel().boolVarArray("B", 100);
        for(int i = 0 ; i < 100; i++){
            PNG.Literal(b[i], 0, true);
            PNG.Literal(b[i], 0, false);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testVariableBound1(){
        try {
            vars[0].instantiateTo(0, Cause.Null);
            PNG.VariableBound(0, true);
            PNG.VariableBound(1, false);


            vars[1].instantiateTo(1, Cause.Null);
            PNG.VariableBound(3, true);
            PNG.VariableBound(2, false);

            vars[2].instantiateTo(-1, Cause.Null);
            PNG.VariableBound(5, false);
            PNG.VariableBound(4, false);

        }catch (ContradictionException cew){
            Assert.fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testVariableBound2() throws Exception{
        vars[0].instantiateTo(0, Cause.Null);
        try {
            PNG.VariableBound(1, false);
            Assert.fail();
        }catch (ContradictionException ce){

        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testVariableBound3() throws Exception{
        vars[0].instantiateTo(1, Cause.Null);
        try {
            PNG.VariableBound(0, true);
            Assert.fail();
        }catch (ContradictionException cew){
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testVariableBound4(){
        try {
            vars[0].instantiateTo(1, Cause.Null);
            PNG.VariableBound(0, true);
            Assert.fail();
        }catch (ContradictionException cew){
        }
    }


    @Test(groups="1s", timeOut=60000)
    public void testApplyEarlyDeductions() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testDoReduce1() throws Exception {
        PNG.doReduce(1);
        Assert.assertTrue(vars[0].isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testDoReduce2() throws Exception {
        PNG.doReduce(0);
        Assert.assertFalse(vars[0].contains(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testDoReduce3() throws Exception {
        PNG.doReduce(3);
        Assert.assertEquals(vars[0].getUB(),0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testDoReduce4() throws Exception {
        PNG.doReduce(2);
        Assert.assertEquals(vars[0].getLB(),1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testIvalue(){
        int[] values = {0, 1, -1, 10, -10, 181, -181, 210, -210};
        for(int value: values) {
            long eqvalue = value;
            long ltvalue = leq(value);
            Assert.assertEquals(ivalue(eqvalue), value, "ivalue eq: " + value + ", " + eqvalue + "");
            Assert.assertEquals(ivalue(ltvalue), value, "ivalue leq: " + value + ", " + ltvalue + "");
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testDeclareDomainNogood(){
        IntVar var = vars[0].getModel().intVar("X4", -1, 1, false);
        PNG.declareDomainNogood(var);
        try{
            PNG.doReduce(13);
            Assert.assertTrue(var.isInstantiatedTo(-1));
        }catch (ContradictionException c){
            Assert.fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testIseq(){
        Random rnd = new Random();
        for(int i = 0 ; i < 1_000_000; i++){
            rnd.setSeed(i);
            int value = rnd.nextInt() * (1-rnd.nextInt(2));
            boolean eq = rnd.nextBoolean();
            long lvalue = eq ? value : leq(value);
            Assert.assertEquals(iseq(lvalue), eq);
            Assert.assertEquals(ivalue(lvalue), value);
        }
    }
}