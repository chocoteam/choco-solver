/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainRandom;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/04/12
 */
public class DistanceTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        for (int i = 0; i < 100; i++) {
            long nbSol, nbNod;
            {
                final Model model = new Model();
                IntVar X = model.intVar("X", 1, 10, false);
                IntVar Y = model.intVar("Y", 1, 10, false);
                IntVar diff = model.intVar("X-Y", -9, 9, true);
                model.sum(new IntVar[]{Y, diff}, "=", X).post();
                IntVar Z = model.intAbsView(diff);
                model.arithm(Z, "=", 5).post();


                Solver r = model.getSolver();
                r.setSearch(randomSearch(new IntVar[]{X, Y}, i));
                while (model.getSolver().solve()) ;
                nbSol = r.getMeasures().getSolutionCount();
                nbNod = r.getMeasures().getNodeCount();
            }
            {
                final Model model = new Model();
                IntVar X = model.intVar("X", 1, 10, false);
                IntVar Y = model.intVar("Y", 1, 10, false);
                model.distance(X, Y, "=", 5).post();
                Solver r = model.getSolver();
                r.setSearch(randomSearch(new IntVar[]{X, Y}, i));
                while (model.getSolver().solve()) ;
                assertEquals(r.getMeasures().getSolutionCount(), nbSol);
                assertTrue(r.getMeasures().getNodeCount() <= nbNod);
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        for (int k = 4; k < 400; k *= 2) {
            Model s1 = new Model(), s2 = new Model();
            IntVar[] vs1, vs2;
            Propagator p2;
            {
                IntVar X = s1.intVar("X", 1, k, false);
                IntVar Y = s1.intVar("Y", 1, k, false);
                vs1 = new IntVar[]{X, Y};
                Constraint c = s1.distance(X, Y, "=", k / 2);
                c.post();
            }
            {
                IntVar X = s2.intVar("X", 1, k, false);
                IntVar Y = s2.intVar("Y", 1, k, false);
                vs2 = new IntVar[]{X, Y};
                Constraint c = s2.distance(X, Y, "=", k / 2);
                c.post();
                p2 = c.getPropagator(0);
            }

            try {
                s1.getSolver().propagate();
                s2.getSolver().propagate();
                Assert.assertEquals(vs1[0].getDomainSize(), vs2[0].getDomainSize());
                Assert.assertEquals(vs1[1].getDomainSize(), vs2[1].getDomainSize());

                for (int j = 0; j < 1000; j++) {
                    s1.getEnvironment().worldPush();
                    s2.getEnvironment().worldPush();

                    IntDomainRandom r = new IntDomainRandom(j);
                    int val = r.selectValue(vs1[0]);
                    vs1[0].removeValue(val, Cause.Null);
                    vs2[0].removeValue(val, Cause.Null);

                    s1.getSolver().propagate();
                    p2.propagate(0);

                    Assert.assertEquals(vs1[0].getDomainSize(), vs2[0].getDomainSize());
                    Assert.assertEquals(vs1[1].getDomainSize(), vs2[1].getDomainSize());

                    s1.getEnvironment().worldPop();
                    s2.getEnvironment().worldPop();
                }
            } catch (ContradictionException e) {
                Assert.fail();
            }
        }

    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Model model = new Model();
        IntVar X = model.intVar("X", -5, 5, true);
        IntVar Y = model.intVar("Y", -5, 5, true);
        IntVar Z = model.intVar("Z", 0, 10, true);
        model.distance(X, Y, "=", Z).post();
        Solver r = model.getSolver();
        r.setSearch(inputOrderLBSearch(new IntVar[]{Z, X, Y, Z}));
        while (model.getSolver().solve()) ;
    }

}
