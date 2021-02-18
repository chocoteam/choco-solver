/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.chocosolver.util.ESat.TRUE;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/07/12
 */
public class DivTest extends AbstractTernaryTest {

    @Override
    protected int validTuple(int vx, int vy, int vz) {
        return vy != 0 && vz == vx / vy ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Model model) {
        return model.div(vars[0], vars[1], vars[2]);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL() {
        Model model = new Model();
        IntVar i = model.intVar("i", 0, 2, false);
        model.div(i, model.boolVar(true), model.boolVar(false)).getOpposite().post();
//        SMF.log(solver, true, false);
        while (model.getSolver().solve()) ;
    }

    @Test(groups="10s", timeOut=60000)
    public void testJL2() {
        for (int i = 0; i < 100000; i++) {
            final Model s = new Model();
            IntVar a = s.intVar("a", new int[]{0, 2, 3, 4});
            IntVar b = s.intVar("b", new int[]{-1, 1, 3, 4});
            IntVar c = s.intVar("c", new int[]{-3, 1, 4});
            s.div(a, b, c).post();
            Solver r = s.getSolver();
            r.setSearch(randomSearch(new IntVar[]{a, b, c}, i));
            //SMF.log(s, true, true);
            r.plugMonitor((IMonitorSolution) () -> {
                if (!TRUE.equals(r.isSatisfied())) {
                    throw new Error(s.toString());
                }
            });
            while (s.getSolver().solve()) ;
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testTP1(){
        final Model s = new Model();
        IntVar a = s.intVar("a", 0, 525000);
        IntVar b = s.intVar("b", 0, 5000);
        IntVar c = s.intVar("c", 0, 525000);
        s.div(a, b, c).post();
        Solver r = s.getSolver();
        r.solve();
        Assert.assertEquals(r.getSolutionCount(), 1);
    }
}
