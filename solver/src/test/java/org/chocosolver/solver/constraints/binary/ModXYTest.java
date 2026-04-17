/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.unary.PropMember;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;

/**
 * <br/>
 *
 * @author Arthur Godet
 * @since 29/03/2019
 */
public class ModXYTest extends AbstractBinaryTest {
    private static final int TEST_VALUE = 3; // TODO How to change this dependency to use AbstractBinaryTest

    @Override
    protected int validTuple(int vx, int vy) {
        return vx%TEST_VALUE == vy ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Model s) {
        return s.mod(vars[0], TEST_VALUE, vars[1]);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL() {
        Model s = new Model();
        IntVar dividend = s.intVar("dividend", 2, 3, false);
        int divisor = 1;
        IntVar remainder = s.intVar("remainder", 1, 2, false);
        s.mod(dividend, divisor, remainder).getOpposite().post();
        Solver r = s.getSolver();
        r.setSearch(inputOrderLBSearch(dividend, remainder));
        s.getSolver().solve();
    }

    @Test(groups="1s", timeOut=60000)
    public void testJT1(){
        Model model = new Model("model");
        IntVar a = model.intVar("a", 2,6);
        int b = 2;
        IntVar c = model.intVar("c", 0,1);
        model.mod(a, b, c).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2Var(){
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,9);
        IntVar z = model.intVar("z", 0, 9);
        model.mod(x, 2, z).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 10);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2Var2(){
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,9);
        IntVar z = model.intVar("z", 0, 9);
        model.mod(x, 2, z).post();
        model.mod(z, 2, 1).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarPropag() throws ContradictionException {
        Model model = new Model("model");
        IntVar x = model.intVar("x", new int[]{0, 2, 3, 5});
        IntVar z = model.intVar("z", 1,3);
        model.mod(x, 3, z).post();
        model.getSolver().propagate();
        Assert.assertTrue(z.isInstantiatedTo(2));
        Assert.assertEquals(x.getDomainSize(), 2);
        Assert.assertEquals(x.getLB(), 2);
        Assert.assertEquals(x.getUB(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarNegValues() throws ContradictionException {
        Model model = new Model("model");
        IntVar x = model.intVar("x", -5, 5);
        IntVar z = model.intVar("z", -5,5);
        model.mod(x, 3, z).post();
        model.getSolver().propagate();
        Assert.assertEquals(z.getDomainSize(), 5);
        Assert.assertEquals(x.getDomainSize(), 11);
        for(int i = -5; i<=5; i++) {
            Assert.assertTrue(x.contains(i));
        }
        for(int j = -2; j<=2; j++) {
            Assert.assertTrue(z.contains(j));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarNegValues2() throws ContradictionException {
        Model model = new Model("model");
        IntVar x = model.intVar("x", -5, 0);
        IntVar z = model.intVar("z", -5,5);
        model.mod(x, 3, z).post();
        model.getSolver().propagate();
        Assert.assertEquals(z.getDomainSize(), 3);
        Assert.assertEquals(x.getDomainSize(), 6);
        for(int i = -5; i<=0; i++) {
            Assert.assertTrue(x.contains(i));
        }
        for(int j = -2; j<=0; j++) {
            Assert.assertTrue(z.contains(j));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarNegValues3() throws ContradictionException {
        Model model = new Model("model");
        IntVar x = model.intVar("x", -5, 5);
        IntVar z = model.intVar("z", -5,0);
        model.mod(x, 3, z).post();
        model.getSolver().propagate();
        Assert.assertEquals(z.getDomainSize(), 3);
        Assert.assertEquals(x.getDomainSize(), 7);
        for(int i = -5; i<=0; i++) {
            Assert.assertTrue(x.contains(i));
        }
        Assert.assertTrue(x.contains(3));
        for(int j = -2; j<=0; j++) {
            Assert.assertTrue(z.contains(j));
        }
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = SolverException.class)
    public void testMod2VarsZeroDiv() {
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,9);
        IntVar y = model.intVar("y", 0, 9);
        model.mod(x, 0, y).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarsIntoMod1VarMod() {
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0, 9);
        IntVar y = model.intVar("y", 5);
        IntVar z = model.intVar("z", 3);
        Constraint cstr = model.mod(x, y, z);
        Assert.assertTrue(cstr.getPropagator(0) instanceof PropMember);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 10);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarsTable() {
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,9);
        IntVar y = model.intVar("y", 0, 9);
        model.mod(x, 5, y).post();
        Assert.assertEquals(model.getNbCstrs(), 1);
        Constraint constraint = model.getCstrs()[0];
        Assert.assertEquals(constraint.getName(), ConstraintsName.TABLE);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 10);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarsPropMod() {
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,10_000);
        IntVar y = model.intVar("y", 0, 10_000);
        model.mod(x, 5_000, y).post();
        Assert.assertEquals(model.getNbCstrs(), 1);
        Constraint constraint = model.getCstrs()[0];
        Assert.assertEquals(constraint.getPropagators().length, 1);
        Assert.assertSame(constraint.getPropagators()[0].getClass(), PropModXY.class);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 10_001);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarsTableReified() {
        Model model = new Model("model");
        int maxVal = 10_000;
        IntVar x = model.intVar("x", -maxVal,maxVal);
        IntVar y = model.intVar("y", -maxVal, maxVal);
        model.ifThen(model.arithm(x, ">", 10), model.mod(x, 5, y));
        long solLimit = 1000L;
        model.getSolver().limitSolution(solLimit);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), solLimit);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod3VarBigValues() throws ContradictionException {
        Model model = new Model("model");
        int maxVal = 1_000_000;
        IntVar x = model.intVar("x", -maxVal, maxVal);
        IntVar y = model.intVar("y", 5);
        IntVar z = model.intVar("z", -maxVal, maxVal);
        model.mod(x, y, z).post();
        model.getSolver().propagate();
        long solLimit = 1000L;
        model.getSolver().limitSolution(solLimit);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), solLimit);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2Perf() {
        Model model = new Model("model");
        int maxVal = 1_000_000;
        int size = 1000;
        int mod = 60;
        IntVar[] x = model.intVarArray("x", size, -maxVal, maxVal, true);
        IntVar[] y = model.intVarArray("y", size, 0, mod-1, true);
        for (int i = 0; i<x.length; i++) {
            model.mod(x[i], mod, y[i]).post();
        }
        model.allDifferent(x).post();
        Assert.assertEquals(model.getNbCstrs(), size+1);
        Constraint constraint = model.getCstrs()[0];
        Assert.assertEquals(constraint.getPropagators().length, 1);
        Assert.assertSame(constraint.getPropagators()[0].getClass(), PropModXY.class);
        model.getSolver().setSearch(Search.inputOrderLBSearch(x));
        model.getSolver().solve();
        for (int i = 0; i<x.length; i++) {
            Assert.assertEquals(x[i].getValue() % mod, y[i].getValue());
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testJT1Expr(){
        Model model = new Model("model");
        IntVar a = model.intVar("a", 2,6);
        int b = 2;
        IntVar c = model.mod("c", a, b);
        model.mod(a, b, c).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarExpr(){
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,9);
        IntVar z = model.mod("z", x, 2);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 10);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2Var2Expr(){
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,9);
        IntVar z = model.mod("z", x, 2);
        model.mod(z, 2, 1).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarPropagExpr() throws ContradictionException {
        Model model = new Model("model");
        IntVar x = model.intVar("x", new int[]{0, 2, 3, 5});
        IntVar z = model.mod("z", x, 3);
        model.arithm(z, ">=", 1).post();
        model.getSolver().propagate();
        Assert.assertTrue(z.isInstantiatedTo(2));
        Assert.assertEquals(x.getDomainSize(), 2);
        Assert.assertEquals(x.getLB(), 2);
        Assert.assertEquals(x.getUB(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarNegValuesExpr() throws ContradictionException {
        Model model = new Model("model");
        IntVar x = model.intVar("x", -5, 5);
        IntVar z = model.mod("z", x, 3);
        model.getSolver().propagate();
        Assert.assertEquals(z.getDomainSize(), 5);
        Assert.assertEquals(x.getDomainSize(), 11);
        for(int i = -5; i<=5; i++) {
            Assert.assertTrue(x.contains(i));
        }
        for(int j = -2; j<=2; j++) {
            Assert.assertTrue(z.contains(j));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarNegValues2Expr() throws ContradictionException {
        Model model = new Model("model");
        IntVar x = model.intVar("x", -5, 0);
        IntVar z = model.mod("z", x, 3);
        model.getSolver().propagate();
        Assert.assertEquals(z.getDomainSize(), 3);
        Assert.assertEquals(x.getDomainSize(), 6);
        for(int i = -5; i<=0; i++) {
            Assert.assertTrue(x.contains(i));
        }
        for(int j = -2; j<=0; j++) {
            Assert.assertTrue(z.contains(j));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarNegValues3Expr() throws ContradictionException {
        Model model = new Model("model");
        IntVar x = model.intVar("x", -5, 5);
        IntVar z = model.mod("z", x, 3);
        model.arithm(z, "<=", 0).post();
        model.getSolver().propagate();
        Assert.assertEquals(z.getDomainSize(), 3);
        Assert.assertEquals(x.getDomainSize(), 7);
        for(int i = -5; i<=0; i++) {
            Assert.assertTrue(x.contains(i));
        }
        Assert.assertTrue(x.contains(3));
        for(int j = -2; j<=0; j++) {
            Assert.assertTrue(z.contains(j));
        }
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = SolverException.class)
    public void testMod2VarsZeroDivExpr() {
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,9);
        IntVar z = model.mod("z", x, 0);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarsTableExpr() {
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,9);
        IntVar z = model.mod("z", x, 5);
        Assert.assertEquals(model.getNbCstrs(), 1);
        Constraint constraint = model.getCstrs()[0];
        Assert.assertEquals(constraint.getName(), ConstraintsName.TABLE);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 10);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNegExpr() {
        Model model = new Model("model");
        IntVar x = model.intVar("x", -7);
        IntVar y = model.intVar("x", 3);
        IntVar z = x.mod(y).intVar();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarsPropModExpr() {
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,10_000);
        IntVar z = model.mod("z", x, 5_000);
        Constraint constraint = model.getCstrs()[0];
        Assert.assertEquals(constraint.getPropagators().length, 1);
        Assert.assertSame(constraint.getPropagators()[0].getClass(), PropModXY.class);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 10_001);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2PerfExpr() {
        Model model = new Model("model");
        int maxVal = 1_000_000;
        int size = 1000;
        int mod = 60;
        IntVar[] x = model.intVarArray("x", size, -maxVal, maxVal, true);
        IntVar[] y = new IntVar[x.length];
        for (int i = 0; i<x.length; i++) {
            y[i] = x[i].mod(mod).intVar();
        }
        model.allDifferent(x).post();
        Assert.assertEquals(model.getNbCstrs(), size+1);
        Constraint constraint = model.getCstrs()[0];
        Assert.assertEquals(constraint.getPropagators().length, 1);
        Assert.assertSame(constraint.getPropagators()[0].getClass(), PropModXY.class);
        model.getSolver().setSearch(Search.inputOrderLBSearch(x));
        model.getSolver().solve();
        for (int i = 0; i<x.length; i++) {
            Assert.assertEquals(x[i].getValue() % mod, y[i].getValue());
        }
    }
}
