/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.cnf.ILogical;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.constraints.nary.cnf.LogicTreeToolBox;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.nary.cnf.LogOp.and;
import static org.chocosolver.solver.constraints.nary.cnf.LogOp.ifOnlyIf;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 nov. 2010
 */
public class LogicTreeTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Model model = new Model();

        BoolVar a = model.boolVar("a");
        BoolVar b = model.boolVar("b");
        BoolVar c = model.boolVar("c");
        BoolVar d = model.boolVar("d");


        LogOp root = LogOp.nand(LogOp.nor(a, b), LogOp.or(c, d));

        ILogical l = LogicTreeToolBox.toCNF(root, model);

        Assert.assertEquals(l.toString(), "((a or b or not(c)) and (a or b or not(d)))");
    }

    @Test(groups="1s", timeOut=60000)
    public void test12() {
        Model model = new Model();

        BoolVar a = model.boolVar("a");
        BoolVar b = model.boolVar("b");
        BoolVar c = model.boolVar("c");
        BoolVar d = model.boolVar("d");
        BoolVar e = model.boolVar("e");


        LogOp root = LogOp.and(LogOp.nand(LogOp.nor(a, b), LogOp.or(c, d)), e);

        ILogical l = LogicTreeToolBox.toCNF(root, model);

        Assert.assertEquals(l.toString(), "(e and (a or b or not(c)) and (a or b or not(d)))");
    }


    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Model model = new Model();

        BoolVar a = model.boolVar("a").not();
        BoolVar b = model.boolVar("b");
        BoolVar c = model.boolVar("c");
        BoolVar d = model.boolVar("d");

        LogOp root = LogOp.or(LogOp.or(LogOp.or(a, b), c), d);

        LogicTreeToolBox.merge(LogOp.Operator.OR, root);

        Assert.assertEquals(root.toString(), "(d or c or not(a) or b)");
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Model model = new Model();

        BoolVar a = model.boolVar("a");
        BoolVar b = model.boolVar("b");
        BoolVar c = model.boolVar("c");

        LogOp root = LogOp.or(LogOp.and(a, b), c);
        root = LogicTreeToolBox.developOr(root);
        Assert.assertEquals(root.toString(), "((a or c) and (b or c))");
    }


    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Model model = new Model();

        BoolVar a = model.boolVar("a").not();
        BoolVar b = model.boolVar("b");
        BoolVar c = model.boolVar("c");
        BoolVar d = model.boolVar("d");

        LogOp root = LogOp.nor(LogOp.or(LogOp.nand(a, b), c), d);

        LogicTreeToolBox.expandNot(root);

        Assert.assertEquals(root.toString(), "(((not(a) and b) and not(c)) and not(d))");
    }


    @Test(groups="1s", timeOut=60000)
    public void test5() {
        Model model = new Model();

        BoolVar a = model.boolVar("a").not();
        BoolVar b = model.boolVar("b");
        BoolVar c = model.boolVar("c");
        BoolVar d = model.boolVar("d");

        LogOp root = LogOp.and(LogOp.and(LogOp.and(a, b), c), d);

        LogicTreeToolBox.merge(LogOp.Operator.AND, root);

        Assert.assertEquals(root.toString(), "(d and c and not(a) and b)");
    }


    @Test(groups="1s", timeOut=60000)
    public void test6() {
        Model model = new Model();

        BoolVar a = model.boolVar("a");
        BoolVar b = model.boolVar("b");

        LogOp root = LogOp.implies(a, b);

        ILogical l = LogicTreeToolBox.toCNF(root, model);

        Assert.assertEquals(l.toString(), "(b or not(a))");
    }

    @Test(groups="1s", timeOut=60000)
    public void test7() {
        Model model = new Model();

        BoolVar a = model.boolVar("a");
        BoolVar b = model.boolVar("b");
        BoolVar c = model.boolVar("c");


        LogOp root = LogOp.ifThenElse(a, b, c);

        ILogical l = LogicTreeToolBox.toCNF(root, model);

        Assert.assertEquals(l.toString(), "((a or c) and (b or c) and (b or not(a)))");
    }

    @Test(groups="1s", timeOut=60000)
    public void test8() {
        Model model = new Model();

        BoolVar a = model.boolVar("a");
        BoolVar na = a.not();
        BoolVar b = model.boolVar("b");
        BoolVar nb = b.not();
        BoolVar c = model.boolVar("c");
        BoolVar d = model.boolVar("d");

        LogOp root = LogOp.and(LogOp.or(a, b, na), LogOp.or(c, d), LogOp.or(b, nb));

        ILogical l = LogicTreeToolBox.toCNF(root, model);

        Assert.assertEquals(l.toString(), "(c or d)");
    }

    @Test(groups="1s", timeOut=60000)
    public void test9() {
        Model model = new Model();

        BoolVar a = model.boolVar("a");
        BoolVar na = a.not();
        BoolVar b = model.boolVar("b");
        BoolVar c = model.boolVar("c");
        BoolVar d = model.boolVar("d");

        LogOp root = LogOp.and(a, b, na, c, d);

        ILogical l = LogicTreeToolBox.toCNF(root, model);

        Assert.assertEquals(l.toString(), "cste -- 0 = 0");
    }

    @Test(groups="1s", timeOut=60000)
    public void test10() {

        Model model = new Model();
        BoolVar[] rows = model.boolVarArray("b", 3);

        model.ifThen(
                rows[0],
                model.arithm(rows[1], "+", rows[2], "=", 2));
        model.ifThen(
                rows[0].not(),
                model.arithm(rows[1], "+", rows[2], "<=", 1));
        //SearchMonitorFactory.log(solver, true, true);
        while (model.getSolver().solve()) ;
        long nbSol = model.getSolver().getSolutionCount();

        for (int seed = 0; seed < 2000; seed++) {
            Model sCNF = new Model();
            BoolVar[] rCNF = sCNF.boolVarArray("b", 3);
            LogOp tree = ifOnlyIf(
                    rCNF[0],
                    and(rCNF[1], rCNF[2])
            );
            sCNF.addClauses(tree);
            sCNF.getSolver().setSearch(randomSearch(rCNF, seed));

//            SearchMonitorFactory.log(sCNF, true, true);
            while (sCNF.getSolver().solve()) ;
            assertEquals(sCNF.getSolver().getSolutionCount(), nbSol);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test11(){
        Model model = new Model();
        BoolVar a = model.boolVar("a");
        BoolVar b = model.boolVar("b");
        LogOp l = LogOp.or(
                LogOp.and(a, b.not()),
                LogOp.and(a.not(), b),
                LogOp.and(a.not(), b.not())
        );
        ILogical ll = LogicTreeToolBox.toCNF(l, model);
        Assert.assertEquals(ll.toString(), "(not(b) or not(a))");
    }

    @Test(groups="1s", timeOut=60000)
    public void test13(){
        Model model = new Model();
        BoolVar a = model.boolVar("a");
        BoolVar b = model.boolVar("b");
        LogOp l = LogOp.or(a, b, a.not());
        ILogical ll = LogicTreeToolBox.toCNF(l, model);
        Assert.assertEquals(ll.toString(), "cste -- 1 = 1");
    }


    @Test(groups="1s", timeOut=60000)
    public void test14(){
        Model model = new Model();
        BoolVar a = model.boolVar("a");
        BoolVar b = model.boolVar("b");
        LogOp l = LogOp.or(a, b, a.not(), a.not());
        ILogical ll = LogicTreeToolBox.toCNF(l, model);
        Assert.assertEquals(ll.toString(), "cste -- 1 = 1");
    }

    @Test(groups="1s", timeOut=60000)
    public void test15(){
        Model model = new Model();
        IntVar a = model.intVar("a", -1, 1, false);
        BoolVar b1 = model.boolVar("b1");
        BoolVar b2 = model.boolVar("b2");
        model.arithm(a,"=",0).reifyWith(b1);
        model.arithm(a,">",0).reifyWith(b2);

        LogOp l = LogOp.or(
                LogOp.and(b1, b2.not()),
                LogOp.and(b1.not(), b2),
                LogOp.and(b1.not(), b2.not())
        );
        model.addClauses(l);
        model.getMinisat().getPropSat().initialize();
        try {
            model.getSolver().propagate();
            b1.instantiateTo(1, Cause.Null);
            model.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        Assert.assertTrue(b1.isInstantiatedTo(1));
        Assert.assertTrue(b2.isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void test16(){
        Model model = new Model();
        IntVar a = model.intVar("a", -1, 1, false);
        BoolVar b1 = model.boolVar("b1");
        BoolVar b2 = model.boolVar("b2");
        model.arithm(a,"=",0).reifyWith(b1);
        model.arithm(a,">",0).reifyWith(b2);

        LogOp l = LogOp.or(b1.not(), b2.not());
        model.addClauses(l);
        model.getMinisat().getPropSat().initialize();
        try {
            model.getSolver().propagate();
            b1.instantiateTo(1, Cause.Null);
            model.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        Assert.assertTrue(b1.isInstantiatedTo(1));
        Assert.assertTrue(b2.isInstantiatedTo(0));
        Assert.assertTrue(a.isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void test17(){
        Model model = new Model();
        IntVar a = model.intVar("a", -1, 1, false);
        BoolVar b1 = model.boolVar("b1");
        BoolVar b2 = model.boolVar("b2");
        model.arithm(a,"=",0).reifyWith(b1);
        model.arithm(a,">",0).reifyWith(b2);

        model.addClauses(new BoolVar[0], new BoolVar[]{b1, b2});
        model.getMinisat().getPropSat().initialize();
        try {
            model.getSolver().propagate();
            b1.instantiateTo(1, Cause.Null);
            model.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        Assert.assertTrue(b1.isInstantiatedTo(1));
        Assert.assertTrue(b2.isInstantiatedTo(0));
        Assert.assertTrue(a.isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void test18() {
        Model model = new Model();

        BoolVar a = model.boolVar("a");
        BoolVar b = model.boolVar("b");
        BoolVar c = model.boolVar("c");
        BoolVar d = model.boolVar("d");
        BoolVar e = model.boolVar("e");
        BoolVar f = model.boolVar("f");

        LogOp root = LogOp.or(
                LogOp.and(d, b, c),
                LogOp.and(a, e, c),
                LogOp.and(a, b, f)
        );

        ILogical l = LogicTreeToolBox.toCNF(root, model);

        Assert.assertEquals(l.toString(), "((a or b) and (a or c) and (a or d) and (b or c) and (b or e) and (c or f) and (a or b or c) and (a or b or d) and (a or b or e) and (a or b or f) and (a or c or d) and (a or c or e) and (a or c or f) and (a or d or e) and (a or d or f) and (b or c or d) and (b or c or e) and (b or c or f) and (b or d or e) and (b or e or f) and (c or d or f) and (c or e or f) and (d or e or f))");
    }
}
