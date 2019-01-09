/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.learn.Implications;
import org.chocosolver.solver.learn.LazyImplications;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.ValueSortedMap;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 31/10/2018.
 */
public class PropGreaterOrEqualXY_CTest {

    @Test(groups="1s", timeOut=60000)
    public void testXisPivot() {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999,999);
        IntVar y = mo.intVar("y", -999,999);
        PropGreaterOrEqualXY_C prop = new PropGreaterOrEqualXY_C(new IntVar[]{x, y},5);
        // root domains
        IntIterableRangeSet oR = new IntIterableRangeSet(-999,999);
        IntIterableRangeSet pR = new IntIterableRangeSet(-999,999);
        // current domain of non-pivot variable
        IntIterableRangeSet oD = new IntIterableRangeSet(10,30);
        // expected domain
        IntIterableRangeSet oE = new IntIterableRangeSet(31,999);
        IntIterableRangeSet pE = new IntIterableRangeSet(-25,999);

        check(prop, y, oR, oD, oE, x, pR, pE);
        check(prop, x, oR, oD, oE, y, pR, pE);
        pR.remove(12);
        pE.remove(12);
        check(prop, y, oR, oD, oE, x, pR, pE);
        check(prop, x, oR, oD, oE, y, pR, pE);
    }

    private void check(PropGreaterOrEqualXY_C prop,
                       IntVar o, IntIterableRangeSet oR, IntIterableRangeSet oD, IntIterableRangeSet oE,
                       IntVar p, IntIterableRangeSet pR, IntIterableRangeSet pE) {
        ExplanationForSignedClause expl = Mockito.mock(ExplanationForSignedClause.class);
        ValueSortedMap front = Mockito.mock(ValueSortedMap.class);
        Implications ig = Mockito.mock(LazyImplications.class);

        Mockito.when(ig.getIntVarAt(0)).thenReturn(p);
        Mockito.when(expl.getSet(o)).thenReturn(oD);
        Mockito.when(expl.getRootSet(p)).thenReturn(pR);
        IntIterableRangeSet comp = oR.duplicate();
        comp.removeAll(oD);
        Mockito.when(expl.getComplementSet(o)).thenReturn(comp);

        ArgumentCaptor<IntIterableRangeSet> dom = ArgumentCaptor.forClass(IntIterableRangeSet.class);
        Mockito.doAnswer(
                ans -> {
                    Assert.assertEquals(dom.getValue(), oE);
                    return null;
                }).when(expl).addLiteral(eq(o), dom.capture(), eq(false));
        Mockito.doAnswer(
                ans -> {
                    Assert.assertEquals(dom.getValue(), pE);
                    return null;
                }).when(expl).addLiteral(eq(p), dom.capture(), eq(true));

        prop.explain(expl, front, ig, 0);
    }
}