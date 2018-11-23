/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
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
public class PropNotEqualXY_CTest {

    @Test(groups="1s", timeOut=60000)
    public void testEx1() {
        Model mo = new Model();
        IntVar x = mo.intVar("x", -999,999);
        IntVar y = mo.intVar("y", -999,999);
        PropNotEqualXY_C prop = new PropNotEqualXY_C(new IntVar[]{x, y}, 5);
        // root domains
        IntIterableRangeSet oR = new IntIterableRangeSet(-999,999);
        IntIterableRangeSet pR = new IntIterableRangeSet(-999,999);
        // domain of non-pivot variable
        IntIterableRangeSet oD = new IntIterableRangeSet(10);
        // expected domains
        IntIterableRangeSet oE = oR.duplicate();
        oE.remove(10);
        IntIterableRangeSet pE = pR.duplicate();
        pE.remove(-5);

        check(prop, x, oR, oD, oE, y, pR, pE);
        check(prop, y, oR, oD, oE, x, pR, pE);
    }

    private void check(PropNotEqualXY_C prop,
                       IntVar o, IntIterableRangeSet oR, IntIterableRangeSet oD, IntIterableRangeSet oE,
                       IntVar p, IntIterableRangeSet pR, IntIterableRangeSet pE) {
        ExplanationForSignedClause expl = Mockito.mock(ExplanationForSignedClause.class);
        ValueSortedMap front = Mockito.mock(ValueSortedMap.class);
        Implications ig = Mockito.mock(LazyImplications.class);

        Mockito.when(ig.getIntVarAt(0)).thenReturn(p);
        Mockito.when(expl.getSet(o)).thenReturn(oD);
        Mockito.when(expl.getRootSet(o)).thenReturn(oR.duplicate());
        Mockito.when(expl.getRootSet(p)).thenReturn(pR.duplicate());


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