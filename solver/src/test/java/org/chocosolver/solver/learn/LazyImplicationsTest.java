/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.learn;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ProblemMaker;
import org.chocosolver.util.objects.ValueSortedMap;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 25/01/2017.
 */
public class LazyImplicationsTest {

    @Test(groups = "1s", timeOut = 60000)
    public void test1() {
        Model model = ProblemMaker.makeNQueenWithBinaryConstraints(8);
        IntVar[] qs = (IntVar[]) model.getHook("vars");
        Solver solver = model.getSolver();
        EventRecorder ee = new EventRecorder(solver);
        LazyImplications aGI = (LazyImplications) ee.getGI().get();
        solver.setEventObserver(ee);
        while (solver.solve()) {
            for (IntVar q : qs) {
                int i = aGI.rootEntries.get(q).p;
                IntIterableRangeSet set = aGI.entries[i].d;
                Assert.assertTrue(set.contains(q.getValue()));
                Assert.assertEquals(set.size(), 1);
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = AssertionError.class)
    public void test2() throws ContradictionException {
        Model model = new Model();
        BoolVar[] bvars = model.boolVarArray("b", 3);
        LazyImplications mIG = new LazyImplications(model);
//        bvars[0].instantiateTo(0, Cause.Null);
        mIG.pushEvent(bvars[0], Cause.Null, IntEventType.INSTANTIATE, 0, 0, 1); // 3
        mIG.pushEvent(bvars[1], Cause.Null, IntEventType.INSTANTIATE, 0, 0, 1); // 4
        mIG.pushEvent(bvars[0], Cause.Null, IntEventType.INSTANTIATE, 0, 0, 1); // 5
        ValueSortedMap<IntVar> front = new ValueSortedMap<>();
        // failed bvar
        Assert.assertEquals(mIG.rightmostNode(6, bvars[0]), 5);
        Assert.assertEquals(mIG.rightmostNode(5, bvars[0]), 3);
        Assert.assertEquals(mIG.rightmostNode(4, bvars[0]), 3);
        Assert.assertEquals(mIG.rightmostNode(3, bvars[0]), 0);
        Assert.assertEquals(mIG.rightmostNode(2, bvars[0]), 0);
        Assert.assertEquals(mIG.rightmostNode(1, bvars[0]), 0);

        // normal bvar
        Assert.assertEquals(mIG.rightmostNode(6, bvars[1]), 4);
        Assert.assertEquals(mIG.rightmostNode(5, bvars[1]), 4);
        Assert.assertEquals(mIG.rightmostNode(4, bvars[1]), 1);
        Assert.assertEquals(mIG.rightmostNode(3, bvars[1]), 1);
        Assert.assertEquals(mIG.rightmostNode(2, bvars[1]), 1);

        // unchanged bvar
        Assert.assertEquals(mIG.rightmostNode(6, bvars[2]), 2);
        Assert.assertEquals(mIG.rightmostNode(5, bvars[2]), 2);
        Assert.assertEquals(mIG.rightmostNode(4, bvars[2]), 2);
        Assert.assertEquals(mIG.rightmostNode(3, bvars[2]), 2);
        // + expected error when limit cannot be reached
        Assert.assertEquals(mIG.rightmostNode(2, bvars[2]), 2);

        IntVar[][] x = model.intVarMatrix("x", 10, 20, 0, 10);
        IntVar obj = model.intVar("obj", 0, 9999);
        model.sum(ArrayUtils.append(x), "=", obj).post();
    }


}