/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.sat.PropNogoods;
import org.chocosolver.sat.SatSolver;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.BacktrackCounter;
import org.chocosolver.solver.search.restart.MonotonicRestartStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/06/13
 */
public class NogoodTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        final Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", 3, 0, 2, false);
        model.getSolver().setNoGoodRecordingFromRestarts();
        model.getSolver().setSearch(randomSearch(vars, 29091981L));
        model.getSolver().setRestarts(new BacktrackCounter(model, 0), new MonotonicRestartStrategy(30), 3);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 27);
        assertEquals(model.getSolver().getBackTrackCount(), 51);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        final Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", 3, 0, 3, false);
        model.getSolver().setNoGoodRecordingFromRestarts();
        model.getSolver().setSearch(randomSearch(vars, 29091981L));
        model.getSolver().setRestarts(new BacktrackCounter(model, 0), new MonotonicRestartStrategy(30), 1000);
        model.getSolver().limitTime(2000);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 64);
        assertEquals(model.getSolver().getBackTrackCount(), 121);
    }

    @Test(groups="1s", timeOut=6000000)
    public void test3() {
        Model model = new Model("nogoods");
        PropNogoods ngstore = model.getNogoodStore().getPropNogoods();
        ngstore.initialize();
        IntVar x = model.intVar("x", -1, 1, false);
        IntVar y = model.intVar("y", 0, 2, false);
        IntVar z = model.intVar("z", 1, 5, false);

        TIntList ng = new TIntArrayList();
        ng.add(SatSolver.negated(ngstore.Literal(x, 1, true)));
        ng.add(SatSolver.negated(ngstore.Literal(y, 1, true)));
        ng.add(ngstore.Literal(z, 3, false));
        ngstore.addNogood(ng);
        Solver solver = model.getSolver();
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 43);
    }

}
