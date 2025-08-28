/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.set.expression;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SetVarExpressionTest {

    /**
     * Basic
     * ============= Relacional ============= values and sets
     * EQ (Equals) -> set = n value | setA = setB
     * NE (not equals) -> set != n values | setA != setB
     * Contains -> set contains n values | setA contains setB
     * NC (Not Contains) -> set notContains n values | setA notContains setB
     * SUBSET  -> set = is a subset of a SuperSet (values or set)
     * <p>
     * Basic relacionals => and, imp, or...
     * <p>
     * ============= Basic Arithmetic =============
     * UNION
     * INTERSECT
     * <p>
     * Simple and Compound -> a.eq((b.intersection(c.union(d)).union(e.intersection(f))).intersection(g)
     * <p>
     * Another functions
     * setCard - set Cardinality
     * notEmpty - a set can't empty
     * Empty
     */

    @Test
    public void notEmptyTest() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{0, 1, 2, 3});

//        model.notEmpty(setA).post(); Choco
        setA.notEmpty().post();

        model.getSolver().propagate();

        model.notMember(0, setA).post();
        model.notMember(1, setA).post();
        model.notMember(3, setA).post();

        model.getSolver().propagate();

        assertEquals(2, setA.getValue().toArray()[0]);
    }

    @Test()
    public void setCardTest() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

//        setA.setCard(model.intVar(4)); Choco
        setA.setCard(4).post();

        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            assertEquals(4, setA.getValue().size());
        }

        model.getSolver().propagate();

        assertEquals(126, nbSol); // binomial coefficient, 4 in 9
    }

    @Test()
    public void simpleEQTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{1, 2});
        setA.eq(1).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{1}, setA.getValue().toArray());
    }

    @Test()
    public void simpleEQTest02() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{1, 2});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{1, 2});
        setA.eq(1).imp(setB.eq(2)).post();
        setA.eq(1).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{1}, setA.getValue().toArray());
        assertArrayEquals(new int[]{2}, setB.getValue().toArray());
    }

    @Test()
    public void simpleEQTest03() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        setA.eq(1, 2, 3).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{1, 2, 3}, setA.getValue().toArray());
    }

    @Test()
    public void simpleNETest01() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{1, 2});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{1, 2});
        setA.eq(2).post();
        setA.ne(1).imp(setB.eq(2)).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{2}, setA.getValue().toArray());
        assertArrayEquals(new int[]{2}, setB.getValue().toArray());
    }

    @Test()
    public void simpleContainsTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        setA.contains(1, 2, 3).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{1, 2, 3}, setA.getLB().toArray());
    }

    @Test()
    public void simpleNotContainsTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        setA.notContains(1, 2, 3).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{0, 4, 5, 6, 7, 8, 9}, setA.getUB().toArray());
    }


    @Test()
    public void simpleSubSetTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        setA.subSet(1, 2, 3, 4, 5).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, setA.getUB().toArray());

        setA.notEmpty().post();
        setA.notContains(4, 5).post();
        model.getSolver().propagate();

        assertArrayEquals(new int[]{1, 2, 3}, setA.getUB().toArray());
    }

    @Test()
    public void biEqTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{1, 2, 3, 4, 5});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{1, 2, 3, 4, 5});
        setA.eq(setB).post();

        setA.eq(1, 2).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{1, 2}, setA.getValue().toArray());
        assertArrayEquals(new int[]{1, 2}, setB.getValue().toArray());
    }

    @Test()
    public void biEQTest02() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{1, 2, 3, 4, 5});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{1, 2, 3, 4, 5});
        SetVar setC = model.setVar("setC", new int[]{}, new int[]{1, 2, 3, 4, 5});
        SetVar setD = model.setVar("setD", new int[]{}, new int[]{1, 2, 3, 4, 5});
        setA.eq(setB).imp(setC.eq(setD)).post();
        setA.eq(1, 2, 3).post();
        setB.eq(1, 2, 3).post();
        setC.eq(4, 5).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{1, 2, 3}, setA.getValue().toArray());
        assertArrayEquals(new int[]{1, 2, 3}, setB.getValue().toArray());
        assertArrayEquals(new int[]{4, 5}, setC.getValue().toArray());
        assertArrayEquals(new int[]{4, 5}, setD.getValue().toArray());
    }

    @Test()
    public void biEQTest03() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        SetVar setB = model.setVar("setB", 1, 2, 3);
        setA.eq(setB).post();

        model.getSolver().propagate();

        assertArrayEquals(setA.getValue().toArray(), setB.getValue().toArray());
    }

    @Test()
    public void biNETest01() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{1, 2, 3, 4, 5});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{1, 2, 3, 4, 5});
        SetVar setC = model.setVar("setC", 3, 4);
        SetVar setD = model.setVar("setD", 1, 2);

        setA.eq(setC).post();
        setA.ne(setD).imp(setB.eq(setD)).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{3, 4}, setA.getValue().toArray());
        assertArrayEquals(new int[]{1, 2}, setB.getValue().toArray());
    }

    @Test()
    public void biContainsTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        setA.contains(setB).post();
        setB.eq(1, 2, 3).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{1, 2, 3}, setB.getValue().toArray());
        assertArrayEquals(new int[]{1, 2, 3}, setA.getLB().toArray());
        assertArrayEquals(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, setA.getUB().toArray());
    }


    @Test()
    public void biNotContainsTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        setA.notContains(setB).post();
        setB.eq(1, 2, 3).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{0, 4, 5, 6, 7, 8, 9}, setA.getUB().toArray());
    }

    @Test()
    public void biSubSetTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{1, 2, 3, 4, 5});
        setA.subSet(setB).post();

        model.getSolver().propagate();

        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, setA.getUB().toArray());
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, setB.getUB().toArray());

        setA.notEmpty().post();
        setA.notContains(4, 5).post();
        model.getSolver().propagate();

        assertArrayEquals(new int[]{1, 2, 3}, setA.getUB().toArray());
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, setB.getUB().toArray());
    }

    @Test()
    public void unionTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setC = model.setVar("setC", new int[]{}, new int[]{1, 3, 5, 7, 9, 11, 13});
        SetVar setD = model.setVar("setD", new int[]{}, new int[]{4, 5, 6, 7, 10, 12, 14});
        SetVar setCD = model.setVar("setCD", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});

        setCD.eq(setC.union(setD)).post();
        model.getSolver().propagate();

        assertArrayEquals(new int[]{1, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14}, setCD.getUB().toArray());
    }

    @Test()
    public void intersectTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setCD = model.setVar("CD", new int[]{}, new int[]{1, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{2, 4, 6, 8, 10, 12, 14});
        SetVar setBCD = model.setVar("setBCD", new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});

        setBCD.eq(setB.intersection(setCD)).post();
        model.getSolver().propagate();

        assertArrayEquals(new int[]{4, 6, 10, 12, 14}, setBCD.getUB().toArray());
    }

    @Test()
    public void intersectAndUnionTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setC = model.setVar("setC", new int[]{}, new int[]{1, 3, 5, 7, 9, 11, 13});
        SetVar setD = model.setVar("setD", new int[]{}, new int[]{4, 5, 6, 7, 10, 12, 14});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{2, 4, 6, 8, 10, 12, 14});
        SetVar setBCD = model.setVar("setBCD", new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});

        setBCD.eq(setB.intersection(setC.union(setD))).post();
        model.getSolver().propagate();

        assertArrayEquals(new int[]{4, 6, 10, 12, 14}, setBCD.getUB().toArray());
    }

    @Test()
    public void intersectTest02() throws ContradictionException {
        Model model = new Model();
        SetVar setE = model.setVar("setE", new int[]{}, new int[]{3, 5, 9, 11, 15, 17, 19});
        SetVar setF = model.setVar("setF", new int[]{}, new int[]{4, 8, 12, 16, 20});
        SetVar setEF = model.setVar("setEF", new int[]{}, new int[]{3, 4, 5, 8, 9, 11, 12, 15, 16, 17, 19, 20});

        setEF.eq(setE.intersection(setF)).post();
        model.getSolver().propagate();

        assertArrayEquals(new int[]{}, setEF.getUB().toArray());
    }


    @Test()
    public void intersectAndUnionTest02() throws ContradictionException {
        Model model = new Model();
        SetVar setE = model.setVar("setE", new int[]{}, new int[]{3, 5, 9, 11, 15, 17, 19});
        SetVar setF = model.setVar("setF", new int[]{}, new int[]{4, 8, 12, 16, 20});
        SetVar setC = model.setVar("setC", new int[]{}, new int[]{1, 3, 5, 7, 9, 11, 13});
        SetVar setD = model.setVar("setD", new int[]{}, new int[]{4, 5, 6, 7, 10, 12, 14});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{2, 4, 6, 8, 10, 12, 14});

        SetVar setBCDEF = model.setVar("setBCDEF", new int[]{}, new int[]{
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20});

        setBCDEF.eq((setB.intersection(setC.union(setD)).union(setE.intersection(setF)))).post();
        model.getSolver().propagate();

        assertArrayEquals(new int[]{4, 6, 10, 12, 14}, setBCDEF.getUB().toArray());
    }

    @Test()
    public void setExpressionTest01() throws ContradictionException {
        Model model = new Model();
        SetVar setA = model.setVar("setA", new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7});
        SetVar setB = model.setVar("setB", new int[]{}, new int[]{2, 4, 6, 8, 10, 12, 14});
        SetVar setC = model.setVar("setC", new int[]{}, new int[]{1, 3, 5, 7, 9, 11, 13});
        SetVar setD = model.setVar("setD", new int[]{}, new int[]{4, 5, 6, 7, 10, 12, 14});
        SetVar setE = model.setVar("setE", new int[]{}, new int[]{3, 5, 9, 11, 15, 17, 19});
        SetVar setF = model.setVar("setF", new int[]{}, new int[]{4, 8, 12, 16, 20});
        SetVar setG = model.setVar("setG", new int[]{}, new int[]{2, 6, 10, 14, 18});

        setA.eq((setB.intersection(setC.union(setD)).union(setE.intersection(setF))).intersection(setG)).post();
        model.getSolver().propagate();

        assertArrayEquals(new int[]{6}, setA.getUB().toArray());
        assertArrayEquals(new int[]{2, 4, 6, 8, 10, 12, 14}, setB.getUB().toArray());
        assertArrayEquals(new int[]{1, 3, 5, 7, 9, 11, 13}, setC.getUB().toArray());
        assertArrayEquals(new int[]{4, 5, 6, 7, 10, 12, 14}, setD.getUB().toArray());
        assertArrayEquals(new int[]{3, 5, 9, 11, 15, 17, 19}, setE.getUB().toArray());
        assertArrayEquals(new int[]{4, 8, 12, 16, 20}, setF.getUB().toArray());
        assertArrayEquals(new int[]{2, 6, 10, 14, 18}, setG.getUB().toArray());
    }
}
