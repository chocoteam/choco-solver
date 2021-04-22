/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Test suite for SetIntersectionView class
 * @author Dimitri Justeau-Allaire
 * @since 08/03/2021
 */
public class SetIntersectionViewTest {

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2, 3});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 1, 8, 3});
        SetVar setC = m.setVar(new int[] {}, new int[] {1, 5, 6, 2, 3});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        SetVar intersection = m.setIntersectionView(setA, setB, setC);
        while (m.getSolver().solve()) {
            int[] sA = setA.getValue().toArray();
            int[] sB = setB.getValue().toArray();
            int[] sC = setC.getValue().toArray();
            int[] all = ArrayUtils.concat(sA, ArrayUtils.concat(sB, sC));
            all = IntStream.of(all).distinct().toArray();
            int[] inter = IntStream.of(all)
                    .filter(i -> setA.getValue().contains(i) && setB.getValue().contains(i) && setC.getValue().contains(i))
                    .toArray();
            Arrays.sort(inter);
            int[] interValue = intersection.getValue().toArray();
            Assert.assertTrue(Arrays.equals(inter, interValue));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrainedSuccess() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2, 3});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 1, 8, 3});
        SetVar setC = m.setVar(new int[] {}, new int[] {1, 5, 6, 2, 3});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        SetVar intersection = m.setIntersectionView(setA, setB, setC);
        m.member(3, intersection).post();
        IntVar card = intersection.getCard();
        m.arithm(card, "=", 3).post();
        while (m.getSolver().solve()) {
            Assert.assertTrue(intersection.getValue().contains(3));
            Assert.assertEquals(card.getValue(), 3);
            int[] sA = setA.getValue().toArray();
            int[] sB = setB.getValue().toArray();
            int[] sC = setC.getValue().toArray();
            int[] all = ArrayUtils.concat(sA, ArrayUtils.concat(sB, sC));
            all = IntStream.of(all).distinct().toArray();
            int[] inter = IntStream.of(all)
                    .filter(i -> setA.getValue().contains(i) && setB.getValue().contains(i) && setC.getValue().contains(i))
                    .toArray();
            Arrays.sort(inter);
            int[] interValue = intersection.getValue().toArray();
            Arrays.sort(interValue);
            Assert.assertTrue(Arrays.equals(inter, interValue));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrainedFail() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2, 3});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 1, 8, 3});
        SetVar setC = m.setVar(new int[] {}, new int[] {1, 5, 6, 2, 3});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        SetVar intersection = m.setIntersectionView(setA, setB, setC);
        m.member(8, intersection).post();
        IntVar card = intersection.getCard();
        m.arithm(card, "=", 3).post();
        m.getSolver().solve();
        Assert.assertEquals(m.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void testCompareProp() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, IntStream.range(0, 10).toArray());
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 1, 2, 22, 5, 8, 9, 10});
        SetVar setC = m.setVar(new int[] {}, new int[] {2, 10, 1, 6, 22, 5, 14});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        SetVar intersection = m.setIntersectionView(setA, setB, setC);
        IntVar card = intersection.getCard();
        m.arithm(card, ">=", 3).post();
        while (m.getSolver().solve()) {}
        long nbSols = m.getSolver().getSolutionCount();
        // Test the same problem with PropIntersection
        m = new Model();
        setA = m.setVar(new int[] {}, IntStream.range(0, 10).toArray());
        setB = m.setVar(new int[] {}, new int[] {0, 1, 2, 22, 5, 8, 9, 10});
        setC = m.setVar(new int[] {}, new int[] {2, 10, 1, 6, 22, 5, 14});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        intersection = m.setVar(new int[] {}, IntStream.range(0, 23).toArray());
        m.intersection(new SetVar[] {setA, setB, setC}, intersection).post();
        card = intersection.getCard();
        m.arithm(card, ">=", 3).post();
        while (m.getSolver().solve()) {}
        Assert.assertEquals(nbSols, m.getSolver().getSolutionCount());
    }
}
