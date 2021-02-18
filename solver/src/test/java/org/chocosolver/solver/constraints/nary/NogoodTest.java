/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertEquals;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.sat.SatSolver;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.sat.PropNogoods;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.limits.BacktrackCounter;
import org.chocosolver.solver.search.restart.MonotonicRestartStrategy;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        assertEquals(model.getSolver().getBackTrackCount(), 54);
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
        assertEquals(model.getSolver().getBackTrackCount(), 133);
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Model model = new Model("nogoods");
        IntVar x = model.intVar("x", -1, 1, false);
        IntVar y = model.intVar("y", 0, 2, false);
        IntVar z = model.intVar("z", 1, 5, false);
        PropNogoods ngstore = model.getNogoodStore().getPropNogoods();
        ngstore.initialize();


        TIntList ng = new TIntArrayList();
        ng.add(SatSolver.negated(ngstore.Literal(x, 1, true)));
        ng.add(SatSolver.negated(ngstore.Literal(y, 1, true)));
        ng.add(ngstore.Literal(z, 3, false));
        ngstore.addNogood(ng);
        Solver solver = model.getSolver();
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 43);
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() throws ContradictionException {
        Model chocoModel = new Model("ASSIST");

        IntVar v1 = chocoModel.intVar("v1", 0, 3, false);
        IntVar v2 = chocoModel.intVar("v2", 0, 3, false);
        IntVar v3 = chocoModel.intVar("v3", 0, 1, false);

        chocoModel.getSolver().propagate();

		/* Default case - make sure only solutions differing in the v1, v2 values are returned */
        chocoModel.getSolver().setNoGoodRecordingFromSolutions(v1, v2);

		/* Adding a constraint */
        Tuples tuples = new Tuples();
        tuples.add(0, 0, 0);
        tuples.add(0, 1, 0);
        tuples.add(1, 1, 1);
        tuples.add(1, 2, 1);
        tuples.add(2, 2, 0);
        tuples.add(3, 3, 0);

        IntVar[] varArray = {v1, v2, v3};
        chocoModel.table(varArray, tuples, "GAC3rm+").post();

		/* Setting the optional case - show me only solutions that differ in v1 value */
        chocoModel.getSolver().setNoGoodRecordingFromSolutions(v2);

        chocoModel.getSolver().findAllSolutions();
        Assert.assertEquals(chocoModel.getSolver().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
        public void test5() {
        final Model model = new Model();
        SetVar[] vars = model.setVarArray("vars", 3, new int[]{}, new int[]{1,2});
        model.getSolver().setNoGoodRecordingFromRestarts();
        model.getSolver().setSearch(Search.setVarSearch(new Random<SetVar>(29091981L), new SetDomainMin(), true, vars));
        model.getSolver().setRestarts(new BacktrackCounter(model, 0), new MonotonicRestartStrategy(30), 3);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 64);
        assertEquals(model.getSolver().getBackTrackCount(), 133);
    }


}
