/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.view.set.SetUnionView;
import org.chocosolver.util.objects.setDataStructures.SetUnion;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Test suite for SetUnionView class
 * @author Dimitri Justeau-Allaire
 * @since 08/03/2021
 */
public class SetUnionViewTest {

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 5, 8});
        SetVar setC = m.setVar(new int[] {}, new int[] {2, 5, 6});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        SetVar union = m.setUnionView(setA, setB, setC);
        while (m.getSolver().solve()) {
            int[] sA = setA.getValue().toArray();
            int[] sB = setB.getValue().toArray();
            int[] sC = setC.getValue().toArray();
            int[] all = ArrayUtils.concat(sA, ArrayUtils.concat(sB, sC));
            all = IntStream.of(all).distinct().toArray();
            Arrays.sort(all);
            int[] unionValue = union.getValue().toArray();
            Arrays.sort(unionValue);
            Assert.assertTrue(Arrays.equals(all, unionValue));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrainedSuccess() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 5, 8});
        SetVar setC = m.setVar(new int[] {}, new int[] {2, 5, 6});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        SetVar union = m.setUnionView(setA, setB, setC);
        m.member(8, union).post();
        IntVar card = union.getCard();
        m.arithm(card, "=", 3).post();
        while (m.getSolver().solve()) {
            Assert.assertTrue(union.getValue().contains(8));
            Assert.assertEquals(card.getValue(), 3);
            int[] sA = setA.getValue().toArray();
            int[] sB = setB.getValue().toArray();
            int[] sC = setC.getValue().toArray();
            int[] all = ArrayUtils.concat(sA, ArrayUtils.concat(sB, sC));
            all = IntStream.of(all).distinct().toArray();
            Arrays.sort(all);
            int[] unionValue = union.getValue().toArray();
            Arrays.sort(unionValue);
            Assert.assertTrue(Arrays.equals(all, unionValue));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrainedFail() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 5, 8});
        SetVar setC = m.setVar(new int[] {}, new int[] {2, 5, 6});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        SetVar union = m.setUnionView(setA, setB, setC);
        m.member(8, union).post();
        m.notMember(8, setB).post();
        IntVar card = union.getCard();
        m.arithm(card, "=", 3).post();
        m.getSolver().solve();
        Assert.assertEquals(m.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testCompareProp() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2, 3, 4, 5});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 5, 8, 9, 10});
        SetVar setC = m.setVar(new int[] {}, new int[] {2, 5, 6, 22, 5, 14});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        SetVar union = m.setUnionView(setA, setB, setC);
        m.member(8, union).post();
        IntVar card = union.getCard();
        m.arithm(card, ">=", 3).post();
        while (m.getSolver().solve()) {
            Assert.assertTrue(union.getValue().contains(8));
            Assert.assertTrue(card.getValue() >= 3);
            int[] sA = setA.getValue().toArray();
            int[] sB = setB.getValue().toArray();
            int[] sC = setC.getValue().toArray();
            int[] all = ArrayUtils.concat(sA, ArrayUtils.concat(sB, sC));
            all = IntStream.of(all).distinct().toArray();
            Arrays.sort(all);
            int[] unionValue = union.getValue().toArray();
            Arrays.sort(unionValue);
            Assert.assertTrue(Arrays.equals(all, unionValue));
        }
        long nbSols = m.getSolver().getSolutionCount();
        // Test the same problem with PropUnion
        m = new Model();
        setA = m.setVar(new int[] {}, new int[] {0, 1, 2, 3, 4, 5});
        setB = m.setVar(new int[] {}, new int[] {0, 5, 8, 9, 10});
        setC = m.setVar(new int[] {}, new int[] {2, 5, 6, 22, 5, 14});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        union = m.setVar(new int[] {}, IntStream.range(0, 23).toArray());
        m.union(new SetVar[] {setA, setB, setC}, union).post();
        m.member(8, union).post();
        card = union.getCard();
        m.arithm(card, ">=", 3).post();
        while (m.getSolver().solve()) {
            Assert.assertTrue(union.getValue().contains(8));
            Assert.assertTrue(card.getValue() >= 3);
            int[] sA = setA.getValue().toArray();
            int[] sB = setB.getValue().toArray();
            int[] sC = setC.getValue().toArray();
            int[] all = ArrayUtils.concat(sA, ArrayUtils.concat(sB, sC));
            all = IntStream.of(all).distinct().toArray();
            Arrays.sort(all);
            int[] unionValue = union.getValue().toArray();
            Arrays.sort(unionValue);
            Assert.assertTrue(Arrays.equals(all, unionValue));
        }
        Assert.assertEquals(nbSols, m.getSolver().getSolutionCount());
    }
}
