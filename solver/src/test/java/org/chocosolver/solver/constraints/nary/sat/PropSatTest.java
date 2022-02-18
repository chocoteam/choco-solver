/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sat;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.SatDecorator;
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

import static org.chocosolver.sat.MiniSat.neg;

/**
 * Test class for PropSat
 * Created by cprudhom on 25/11/2015.
 * Project: choco.
 */
public class PropSatTest {

    IntVar[] vars;
    PropSat PNG;
    int[] lits;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        Model model = new Model("sat");
        vars = model.intVarArray("X", 4, -1, 1, false);
        PNG = model.getMinisat().getPropSat();
        lits = new int[6];
        lits[0] = MiniSat.makeLiteral(PNG.makeIntEq(vars[0], 0), true);
        lits[1] = MiniSat.makeLiteral(PNG.makeIntEq(vars[0], 0), false);
        lits[2] = MiniSat.makeLiteral(PNG.makeIntEq(vars[1], 0), true);
        lits[3] = MiniSat.makeLiteral(PNG.makeIntEq(vars[1], 0), false);
        lits[4] = MiniSat.makeLiteral(PNG.makeIntEq(vars[2], 0), true);
        lits[5] = MiniSat.makeLiteral(PNG.makeIntEq(vars[2], 0), false);
        PNG.initialize();
        TIntList list = new TIntArrayList();
        list.add(lits[0]);
        list.add(lits[1]);
        PNG.addClause(list);
        list.clear();
        list.add(lits[2]);
        list.add(lits[3]);
        PNG.addClause(list);
        list.clear();
        list.add(lits[4]);
        list.add(lits[5]);
        PNG.addClause(list);
        PNG.propagate(2);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPropagate() throws Exception {
        try {
            PNG.propagate(2);
        } catch (ContradictionException c) {
            Assert.fail();
        }
        Assert.assertEquals(vars[0].getDomainSize(), 3);
        Assert.assertEquals(vars[1].getDomainSize(), 3);
        Assert.assertEquals(vars[2].getDomainSize(), 3);

        TIntList list = new TIntArrayList();
        list.add(MiniSat.neg(lits[0]));
        list.add(lits[2]);
        PNG.addClause(list);
        list.clear();
        list.add(MiniSat.neg(lits[2]));
        list.add(lits[4]);
        PNG.addClause(list);
        vars[0].instantiateTo(0, Cause.Null);
        try {
            PNG.propagate(2);
        } catch (ContradictionException c) {
            Assert.fail();
        }
        Assert.assertTrue(vars[0].isInstantiatedTo(0));
        Assert.assertTrue(vars[1].isInstantiatedTo(0));
        Assert.assertTrue(vars[2].isInstantiatedTo(0));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPropagate1() throws Exception {
        PNG.propagate(2);
        TIntList list = new TIntArrayList();
        list.add(MiniSat.neg(lits[0]));
        list.add(lits[2]);
        PNG.addClause(list);
        list.clear();
        list.add(MiniSat.neg(lits[2]));
        list.add(lits[4]);
        PNG.addClause(list);
        vars[0].instantiateTo(0, Cause.Null);
        try {
            PNG.propagate(0, 15);
        } catch (ContradictionException c) {
            Assert.fail();
        }
        Assert.assertTrue(vars[0].isInstantiatedTo(0));
        Assert.assertTrue(vars[1].isInstantiatedTo(0));
        Assert.assertTrue(vars[2].isInstantiatedTo(0));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testIsEntailed1() throws Exception {
        vars[0].instantiateTo(0, Cause.Null);
        vars[1].instantiateTo(1, Cause.Null);
        vars[2].instantiateTo(-1, Cause.Null);
        Assert.assertEquals(PNG.isEntailed(), ESat.TRUE);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testIsEntailed2() throws Exception {
        Assert.assertEquals(PNG.isEntailed(), ESat.UNDEFINED);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testLiteral1() throws Exception {
        Assert.assertEquals(lits[0], 1);
        Assert.assertEquals(lits[1], 0);
        Assert.assertEquals(lits[2], 3);
        Assert.assertEquals(lits[3], 2);
        Assert.assertEquals(lits[4], 5);
        Assert.assertEquals(lits[5], 4);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testLiteral2() throws Exception {
        BoolVar[] b = vars[0].getModel().boolVarArray("B", 100);
        for (int i = 0; i < 100; i++) {
            MiniSat.makeLiteral(PNG.makeIntEq(b[i], 0), true);
            MiniSat.makeLiteral(PNG.makeIntEq(b[i], 0), false);
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testVariableBound1() {
        try {
            vars[0].instantiateTo(0, Cause.Null);
            PNG.doBound(0);
            Assert.assertEquals(PNG.value(0), ESat.TRUE);

            vars[1].instantiateTo(1, Cause.Null);
            PNG.doBound(1);
            Assert.assertEquals(PNG.value(1), ESat.FALSE);

            vars[2].removeValue(-1, Cause.Null);
            PNG.doBound(2);
            Assert.assertEquals(PNG.value(2), ESat.UNDEFINED);

        } catch (ContradictionException cew) {
            Assert.fail();
        }
    }


    /**
     * Add clauses to ensure domain consistency, that is:
     * <ol>
     *     <li>
     *          [ x &le; d ] &rArr; [ x &le; d +1 ]
     *     </li>
     *     <li>
     *          [ x = d ] &hArr; ([ x &le; d ] &and; &not;[ x &le; d + 1])
     *     </li>
     * </ol>
     *
     * @param var an integer variable
     * @return if clauses have been successfully added to the store.
     */
    boolean declareDomainNogood(IntVar var) {
        int size = var.getDomainSize();
        int[] lits = new int[size * 2];
        // 1. generate lits
        int a = var.getLB();
        int ub = var.getUB();
        int i = 0;
        while (a <= ub) {
            lits[i] = MiniSat.makeLiteral(PNG.makeIntEq(var, a), true);
            lits[i + size] = MiniSat.makeLiteral(PNG.makeIntLe(var, a), true);
            i++;
            a = var.nextValue(a);
        }
        TIntList clauses = new TIntArrayList();
        boolean add = false;
        // 2. add clauses
        // 2a.  [ x <= d ] => [ x <= d +1 ]
        for (int j = size; j < 2 * size - 1; j++) {
            clauses.add(neg(lits[j]));
            clauses.add(lits[j + 1]);
            add |= PNG.addClause(clauses);
            clauses.clear();
        }
        // 2b.  [ x = d ] <=> [ x <= d ] and not[ x <= d +1 ]
        for (int k = 0; k < size - 1; k++) {
            // [ x = d ] or not[ x <= d ] or [ x <= d +1 ]
            clauses.add(lits[k]);
            clauses.add(neg(lits[size + k]));
            clauses.add(lits[size + k + 1]);
            add |= PNG.addClause(clauses);
            clauses.clear();
            // not [ x = d ] or [ x <= d ]
            clauses.add(neg(lits[k]));
            clauses.add(lits[size + k]);
            add |= PNG.addClause(clauses);
            clauses.clear();
            // not [ x = d ] or not[ x <= d +1 ]
            clauses.add(neg(lits[k]));
            clauses.add(neg(lits[size + k + 1]));
            add |= PNG.addClause(clauses);
            clauses.clear();
        }
        ((SatDecorator) PNG.getMiniSat()).storeEarlyDeductions();
        return add;
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDeclareDomainNogood() {
        IntVar var = vars[0].getModel().intVar("X4", -1, 1, false);
        declareDomainNogood(var);
        try {
            PNG.getMiniSat().touched_variables_.add(7);
            ((SatDecorator)PNG.getMiniSat()).storeEarlyDeductions();
            ((SatDecorator)PNG.getMiniSat()).applyEarlyDeductions(Cause.Null);
            PNG.doBound(0);
            Assert.assertTrue(var.isInstantiatedTo(-1));
        } catch (ContradictionException c) {
            Assert.fail();
        }
    }

}