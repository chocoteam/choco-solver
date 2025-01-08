package org.chocosolver.examples.set.expression;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.impl.SetVarExpression;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetVarExpressionTest {

    /** Basic
     * ============= Relacional ============= values and sets
     * EQ (Equals) -> set = n value | setA = setB
     * NE (not equals) -> set != n values | setA != setB
     * Contains -> set contains n values | setA contains setB
     * NC (Not Contains) -> set notContains n values | setA notContains setB
     * SUBSET  -> set = is a subset of a SuperSet (values or set)
     *
     * Basic relacionals => and, imp, or...
     *
     * ============= Basic Arithmetic =============
     * UNION
     * INTERSECT
     *
     * Simple and Compound -> a.eq((b.intersection(c.union(d)).union(e.intersection(f))).intersection(g)
     *
     * Another functions
     * setCard - set Cardinality
     * notEmpty - a set can't empty
     * Empty
     */

    @Test
    public void notEmptyTest() throws ContradictionException{
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{0, 1, 2, 3});

//        model.notEmpty(setA).post(); Choco
        setA.notEmpty().post();

        model.getSolver().propagate();

        model.notMember(0, setA).post();
        model.notMember(1, setA).post();
        model.notMember(3, setA).post();

        model.getSolver().propagate();

        assertTrue(setA.getValue().toArray()[0] == 2);
    }

    @Test()
    public void setCardTest() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA",new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

//        setA.setCard(model.intVar(4)); Choco
        setA.setCard(4).post();

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertEquals(setA.getValue().size(), 4);
        }

        model.getSolver().propagate();

        assertEquals(nbSol, 126); // binomial coefficient, 4 in 9
    }

    @Test()
    public void simpleEQTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{1,2});
        setA.eq(1).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getValue().toArray(), new int[] {1}));
    }

    @Test()
    public void simpleEQTest02() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{1,2});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{1,2});
        setA.eq(1).imp(setB.eq(2)).post();
        setA.eq(1).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getValue().toArray(), new int[] {1}));
        assertTrue(Arrays.equals(setB.getValue().toArray(), new int[] {2}));
    }

    @Test()
    public void simpleEQTest03() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9});
        setA.eq(1,2,3).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getValue().toArray(), new int[] {1,2,3}));
    }

    @Test()
    public void simpleNETest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{1,2});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{1,2});
        setA.eq(2).post();
        setA.ne(1).imp(setB.eq(2)).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getValue().toArray(), new int[] {2}));
        assertTrue(Arrays.equals(setB.getValue().toArray(), new int[] {2}));
    }

    @Test()
    public void simpleContainsTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9});
        setA.contains(1,2,3).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getLB().toArray(), new int[] {1,2,3}));
    }

    @Test()
    public void simpleNotContainsTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9});
        setA.notContains(1,2,3).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getUB().toArray(), new int[] {0,4,5,6,7,8,9}));
    }


    @Test()
    public void simpleSubSetTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9});
        setA.subSet(1,2,3,4,5).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getUB().toArray(), new int[] {1,2,3,4,5}));

        setA.notEmpty().post();
        setA.notContains(4,5).post();
        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getUB().toArray(), new int[] {1,2,3}));
    }

    @Test()
    public void biEqTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{1,2,3,4,5});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{1,2,3,4,5});
        setA.eq(setB).post();

        setA.eq(1,2).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getValue().toArray(), new int[] {1,2}));
        assertTrue(Arrays.equals(setB.getValue().toArray(), new int[] {1,2}));
    }

    @Test()
    public void biEQTest02() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{1,2,3,4,5});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{1,2,3,4,5});
        SetVarExpression setC = new SetVarExpression(model,"setC", new int[]{}, new int[]{1,2,3,4,5});
        SetVarExpression setD = new SetVarExpression(model,"setD", new int[]{}, new int[]{1,2,3,4,5});
        setA.eq(setB).imp(setC.eq(setD)).post();
        setA.eq(1,2,3).post();
        setB.eq(1,2,3).post();
        setC.eq(4,5).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getValue().toArray(), new int[] {1,2,3}));
        assertTrue(Arrays.equals(setB.getValue().toArray(), new int[] {1,2,3}));
        assertTrue(Arrays.equals(setC.getValue().toArray(), new int[] {4,5}));
        assertTrue(Arrays.equals(setD.getValue().toArray(), new int[] {4,5}));
    }

    @Test()
    public void biEQTest03() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9});
        SetVarExpression setB = new SetVarExpression(model,"setB", 1,2,3);
        setA.eq(setB).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getValue().toArray(), setB.getValue().toArray()));
    }

    @Test()
    public void biNETest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{1,2,3,4,5});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{1,2,3,4,5});
        SetVarExpression setC = new SetVarExpression(model,"setC", 3,4);
        SetVarExpression setD = new SetVarExpression(model,"setD", 1,2);

        setA.eq(setC).post();
        setA.ne(setD).imp(setB.eq(setD)).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getValue().toArray(), new int[] {3,4}));
        assertTrue(Arrays.equals(setB.getValue().toArray(), new int[] {1,2}));
    }

    @Test()
    public void biContainsTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9});
        setA.contains(setB).post();
        setB.eq(1,2,3).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setB.getValue().toArray(), new int[] {1,2,3}));
        assertTrue(Arrays.equals(setA.getLB().toArray(), new int[] {1,2,3}));
        assertTrue(Arrays.equals(setA.getUB().toArray(), new int[] {0,1,2,3,4,5,6,7,8,9}));
    }


    @Test()
    public void biNotContainsTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9});
        setA.notContains(setB).post();
        setB.eq(1,2,3).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getUB().toArray(), new int[] {0,4,5,6,7,8,9}));
    }

    @Test()
    public void biSubSetTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{1,2,3,4,5});
        setA.subSet(setB).post();

        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getUB().toArray(), new int[] {1,2,3,4,5}));
        assertTrue(Arrays.equals(setB.getUB().toArray(), new int[] {1,2,3,4,5}));

        setA.notEmpty().post();
        setA.notContains(4,5).post();
        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getUB().toArray(), new int[] {1,2,3}));
        assertTrue(Arrays.equals(setB.getUB().toArray(), new int[] {1,2,3,4,5}));
    }

    @Test()
    public void unionTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setC = new SetVarExpression(model,"setC", new int[]{}, new int[]{1,3,5,7,9,11,13});
        SetVarExpression setD = new SetVarExpression(model,"setD", new int[]{}, new int[]{4,5,6,7,10,12,14});
        SetVarExpression setCD = new SetVarExpression(model,"setCD", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20});

        setCD.eq(setC.union(setD)).post();
        model.getSolver().propagate();

        assertTrue(Arrays.equals(setCD.getUB().toArray(), new int[] {1, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14}));
    }

    @Test()
    public void intersectTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setCD = new SetVarExpression(model,"CD", new int[]{}, new int[]{1, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{2,4,6,8,10,12,14});
        SetVarExpression setBCD = new SetVarExpression(model,"setBCD", new int[]{}, new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14});

        setBCD.eq(setB.intersection(setCD)).post();
        model.getSolver().propagate();

        assertTrue(Arrays.equals(setBCD.getUB().toArray(), new int[] {4,6,10,12,14}));
    }

    @Test()
    public void intersectAndUnionTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setC = new SetVarExpression(model,"setC", new int[]{}, new int[]{1,3,5,7,9,11,13});
        SetVarExpression setD = new SetVarExpression(model,"setD", new int[]{}, new int[]{4,5,6,7,10,12,14});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{2,4,6,8,10,12,14});
        SetVarExpression setBCD = new SetVarExpression(model,"setBCD", new int[]{}, new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14});

        setBCD.eq(setB.intersection(setC.union(setD))).post();
        model.getSolver().propagate();

        assertTrue(Arrays.equals(setBCD.getUB().toArray(), new int[] {4,6,10,12,14}));
    }

    @Test()
    public void intersectTest02() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setE = new SetVarExpression(model,"setE", new int[]{}, new int[]{3,5,9,11,15,17,19});
        SetVarExpression setF = new SetVarExpression(model,"setF", new int[]{}, new int[]{4,8,12,16,20});
        SetVarExpression setEF = new SetVarExpression(model,"setEF", new int[]{}, new int[]{3,4,5,8,9,11,12,15,16,17,19,20});

        setEF.eq(setE.intersection(setF)).post();
        model.getSolver().propagate();

        assertTrue(Arrays.equals(setEF.getUB().toArray(), new int[] {}));
    }


    @Test()
    public void intersectAndUnionTest02() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setE = new SetVarExpression(model,"setE", new int[]{}, new int[]{3,5,9,11,15,17,19});
        SetVarExpression setF = new SetVarExpression(model,"setF", new int[]{}, new int[]{4,8,12,16,20});
        SetVarExpression setC = new SetVarExpression(model,"setC", new int[]{}, new int[]{1,3,5,7,9,11,13});
        SetVarExpression setD = new SetVarExpression(model,"setD", new int[]{}, new int[]{4,5,6,7,10,12,14});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{2,4,6,8,10,12,14});

        SetVarExpression setBCDEF = new SetVarExpression(model,"setBCDEF", new int[]{}, new int[]{
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20});

        setBCDEF.eq((setB.intersection(setC.union(setD)).union(setE.intersection(setF)))).post();
        model.getSolver().propagate();

        assertTrue(Arrays.equals(setBCDEF.getUB().toArray(), new int[] {4,6,10,12,14}));
    }

    @Test()
    public void setExpressionTest01() throws ContradictionException {
        Model model = new Model();
        SetVarExpression setA = new SetVarExpression(model,"setA", new int[]{}, new int[]{1,2,3,4,5,6,7});
        SetVarExpression setB = new SetVarExpression(model,"setB", new int[]{}, new int[]{2,4,6,8,10,12,14});
        SetVarExpression setC = new SetVarExpression(model,"setC", new int[]{}, new int[]{1,3,5,7,9,11,13});
        SetVarExpression setD = new SetVarExpression(model,"setD", new int[]{}, new int[]{4,5,6,7,10,12,14});
        SetVarExpression setE = new SetVarExpression(model,"setE", new int[]{}, new int[]{3,5,9,11,15,17,19});
        SetVarExpression setF = new SetVarExpression(model,"setF", new int[]{}, new int[]{4,8,12,16,20});
        SetVarExpression setG = new SetVarExpression(model,"setG", new int[]{}, new int[]{2,6,10,14,18});

        setA.eq((setB.intersection(setC.union(setD)).union(setE.intersection(setF))).intersection(setG)).post();
        model.getSolver().propagate();

        assertTrue(Arrays.equals(setA.getUB().toArray(), new int[] {6}));
        assertTrue(Arrays.equals(setB.getUB().toArray(), new int[] {2, 4, 6, 8, 10, 12, 14}));
        assertTrue(Arrays.equals(setC.getUB().toArray(), new int[] {1, 3, 5, 7, 9, 11, 13}));
        assertTrue(Arrays.equals(setD.getUB().toArray(), new int[] {4, 5, 6, 7, 10, 12, 14}));
        assertTrue(Arrays.equals(setE.getUB().toArray(), new int[] {3, 5, 9, 11, 15, 17, 19}));
        assertTrue(Arrays.equals(setF.getUB().toArray(), new int[] {4, 8, 12, 16, 20}));
        assertTrue(Arrays.equals(setG.getUB().toArray(), new int[] {2, 6, 10, 14, 18}));
    }
}
