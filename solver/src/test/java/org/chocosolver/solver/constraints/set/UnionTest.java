/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class UnionTest {


    @Test(groups = "1s", timeOut = 60000)
    public void testUnionFixed() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        SetVar union = model.setVar(1, 2, 3, 4, 5);
        model.union(setVars, union).post();

        assertEquals(checkSolutions(model, setVars, model.setVar(0, 1, 2), union), 16807);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSetVarsFixed() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[3];
        setVars[0] = model.setVar(1, 2);
        setVars[1] = model.setVar(new int[]{}, new int[]{3});
        setVars[2] = model.setVar(4, 5);
        SetVar union = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7});
        model.union(setVars, union).post();

        assertEquals(checkSolutions(model, setVars, model.setVar(0, 1, 2), union), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testImpossible() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{1}, new int[]{1, 2, 3, 4});
        SetVar union = model.setVar(2, 3, 4); // different domains
        model.union(setVars, union).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }


    private int checkSolutions(Model model, SetVar[] setVars, SetVar indice, SetVar union) {
        int nbSol = 0;
        int iOffset = indice.getUB().min();
        while (model.getSolver().solve()) {
            //System.out.printf("%s = U(%s) %s%n", union.getLB(), indice.getLB(), Arrays.toString(setVars));
            nbSol++;
            ISet computed = SetFactory.makeLinkedList();
            for (int i : indice.getLB()) {
                i -= iOffset;
                for (Integer value : setVars[i].getValue()) {
                    assertTrue(union.getValue().contains(value));
                    computed.add(value);
                }
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }

    @DataProvider
    public Object[][] decOrNot() {
        return new Object[][]{{true}, {false}};
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "decOrNot")
    public void testUnionVarFixed0(boolean dec) {
        Model model = new Model();
        SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{0, 1, 3});
        SetVar indices = model.setVar("I", new int[]{}, new int[]{0, 1, 2});
        SetVar union = model.setVar("U", new int[]{}, new int[]{0, 1, 2, 3});
        if (dec) {
            decUnionVar(sets, indices, union, 4);
        } else {
            model.union(union, indices, 0, sets).post();
        }
        indices.getCard().gt(0).post();
        model.getSolver().setSearch(Search.setVarSearch(ArrayUtils.append(sets, new SetVar[]{indices, union})));
        //model.getSolver().showShortStatistics();
        assertEquals(checkSolutions(model, sets, indices, union), 3584);
        Assert.assertEquals(model.getSolver().getNodeCount(), 7236);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "decOrNot")
    public void testUnionVarFixed00(boolean dec) {
        Model model = new Model();
        SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{0, 1});
        SetVar indices = model.setVar("I", new int[]{}, new int[]{0, 1, 2});
        SetVar union = model.setVar("U", new int[]{}, new int[]{0, 1});
        if (dec) {
            decUnionVar(sets, indices, union, 4);
        } else {
            model.union(union, indices, 0, sets).post();
        }
        indices.getCard().gt(0).post();
        union.getCard().gt(0).post();
        sets[0].getCard().gt(0).post();
        model.getSolver().setSearch(Search.setVarSearch(ArrayUtils.append(sets, new SetVar[]{indices, union})));
        //model.getSolver().showDecisions();
        model.getSolver().limitSolution(10);
        assertEquals(checkSolutions(model, sets, indices, union), 10);
        Assert.assertEquals(model.getSolver().getNodeCount(), 30);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "decOrNot")
    public void testUnionVarFixed000(boolean dec) {
        Model model = new Model();
        SetVar[] sets = model.setVarArray("S", 3, new int[]{0, 1}, new int[]{0, 1});
        SetVar indices = model.setVar("I", new int[]{}, new int[]{0, 1, 2});
        SetVar union = model.setVar("U", new int[]{0}, new int[]{0, 1});
        if (dec) {
            decUnionVar(sets, indices, union, 4);
        } else {
            model.union(union, indices, 0, sets).post();
        }
        indices.getCard().gt(0).post();
        union.getCard().gt(0).post();
        sets[0].getCard().gt(0).post();
        model.getSolver().setSearch(Search.setVarSearch(ArrayUtils.append(sets, new SetVar[]{indices, union})));
        //model.getSolver().showDecisions();
        assertEquals(checkSolutions(model, sets, indices, union), 7);
        Assert.assertEquals(model.getSolver().getNodeCount(), 14);
    }


    @Test(groups = "1s", timeOut = 60000, dataProvider = "decOrNot")
    public void testUnionVarFixed1(boolean dec) {
        Model model = new Model();
        SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{0, 1, 3});
        SetVar indices = model.setVar("I", new int[]{}, new int[]{0, 1, 2});
        SetVar union = model.setVar("U", new int[]{}, new int[]{0, 1, 2, 3});
        if (dec) {
            decUnionVar(sets, indices, union, 4);
        } else {
            model.union(union, indices, 0, sets).post();
        }
        model.getSolver().setSearch(Search.setVarSearch(ArrayUtils.append(sets, new SetVar[]{indices, union})));
        assertEquals(checkSolutions(model, sets, indices, union), 4096);
        Assert.assertEquals(model.getSolver().getNodeCount(), 8212);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnionVarFixed2() {
        Model model = new Model();
        SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{5, 6, 8});
        SetVar indices = model.setVar("I", new int[]{}, new int[]{2, 3, 4});
        SetVar union = model.setVar("U", new int[]{}, new int[]{5, 6, 7, 8});
        model.union(union, indices, 2, sets).post();
        indices.getCard().gt(0).post();
        model.getSolver().setSearch(Search.setVarSearch(ArrayUtils.append(sets, new SetVar[]{indices, union})));
        assertEquals(checkSolutions(model, sets, indices, union), 3584);
        Assert.assertEquals(model.getSolver().getNodeCount(), 7236);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnionVarFixed3() {
        Model model = new Model();
        SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{5, 6, 8});
        SetVar indices = model.setVar("I", new int[]{}, new int[]{2, 3, 4});
        SetVar union = model.setVar("U", new int[]{}, new int[]{5, 6, 7, 8});
        model.union(union, indices, 2, sets).post();
        model.getSolver().setSearch(Search.setVarSearch(ArrayUtils.append(sets, new SetVar[]{indices, union})));
        assertEquals(checkSolutions(model, sets, indices, union), 4096);
        Assert.assertEquals(model.getSolver().getNodeCount(), 8212);
    }


    private void decUnionVar(SetVar[] sets, SetVar indices, SetVar union, int l) {
        Model m = indices.getModel();
        int n = sets.length;
        //indices.getCard().gt(0).post();

        BoolVar[][] b_si = new BoolVar[n][];
        for (int k = 0; k < n; k++) {
            b_si[k] = m.setBoolsView(sets[k], l, 0);
        }
        BoolVar[] b_u = m.setBoolsView(union, l, 0);
        BoolVar[] b_i = m.setBoolsView(indices, n, 0);

        for (int k = 0; k < l; k++) {
            BoolVar[] r = new BoolVar[n];
            for (int j = 0; j < n; j++) {
                r[j] = b_si[j][k].and(b_i[j]).boolVar();
            }
            m.addClausesBoolOrArrayEqVar(r, b_u[k]);
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnionBounds() {
        Model model = new Model();
        SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{2, 3, 5, 6});
        SetVar indices = model.setVar("I", new int[]{}, new int[]{-1, 0, 2, 6});
        SetVar union = model.setVar("U", new int[]{}, new int[]{1, 2, 3, 6, 7});
        //model.union(union, indices, sets).post();
        model.union(union, indices, 0, sets).post();
        while (model.getSolver().solve());
        Assert.assertEquals(model.getSolver().getSolutionCount(), 9216);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnionWrongBoundsResult1() {
        Model model = new Model();
        SetVar[] sets = model.setVarArray("S", 2, new int[]{}, new int[]{0, 1});
        SetVar indices = model.setVar("I", new int[]{}, new int[]{0, 1, 2});
        SetVar union = model.setVar("U", new int[]{}, new int[]{0});
        model.union(union, indices, sets).post();
        while (model.getSolver().solve()) ;
        Assert.assertEquals(model.getSolver().getSolutionCount(), 25);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnionWrongBoundsResult2() {
        Model model = new Model();
        SetVar[] sets = model.setVarArray("S", 2, new int[]{}, new int[]{0, 1, 2});
        SetVar indices = model.setVar("I", new int[]{0, 1, 2, 3, 4, 5}, new int[]{0, 1, 2, 3, 4, 5});
        SetVar union = model.setVar("U", new int[]{}, new int[]{0, 1});
        model.union(union, indices, sets).post();
        Assert.assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnionWrongBoundsResult3() {
        Model model = new Model();
        SetVar[] sets = model.setVarArray("S", 2, new int[]{}, new int[]{0, 1, 2});
        SetVar indices = model.setVar("I", new int[]{}, new int[]{0, 1, 2, 3, 4});
        SetVar union = model.setVar("U", new int[]{}, new int[]{0, 1});
        model.union(union, indices, sets).post();
        while (model.getSolver().solve()) ;
        Assert.assertEquals(model.getSolver().getSolutionCount(), 144);
    }

}
