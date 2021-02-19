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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;

/**
 * Created by cprudhom on 14/01/15.
 * Project: choco.
 */
public class ClauseChannelingTest {

    @Test(groups="10s", timeOut=300000)
    public void test1E() {
        for (int i = 1; i < 100; i++) {
            for (int seed = 1; seed < 100; seed++) {
                Model model = new Model();
                IntVar iv = model.intVar("iv", 1, i, false);
                BoolVar[] eqs = model.boolVarArray("eq", i);
                BoolVar[] lqs = model.boolVarArray("lq", i);

                model.clausesIntChanneling(iv, eqs, lqs).post();

                Solver r = model.getSolver();
                r.setSearch(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                while (model.getSolver().solve()) ;
                assertEquals(r.getMeasures().getSolutionCount(), i);
            }
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void test1B() {
        for (int i = 1; i < 100; i++) {
            for (int seed = 1; seed < 200; seed++) {
                Model model = new Model();
                IntVar iv = model.intVar("iv", 1, i, true);
                BoolVar[] eqs = model.boolVarArray("eq", i);
                BoolVar[] lqs = model.boolVarArray("lq", i);

                model.clausesIntChanneling(iv, eqs, lqs).post();

                Solver r = model.getSolver();
                r.setSearch(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                while (model.getSolver().solve()) ;
                assertEquals(r.getMeasures().getSolutionCount(), i);
            }
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void test2E() {
        for (int i = 1; i < 100; i++) {
            for (int seed = 0; seed < 20; seed++) {
                Model sr = new Model();
                Model sc = new Model();
                {
                    IntVar iv = sr.intVar("iv", 1, i, false);
                    BoolVar[] eqs = sr.boolVarArray("eq", i);
                    BoolVar[] lqs = sr.boolVarArray("lq", i);

                    for (int j = 1; j <= i; j++) {
                        sr.arithm(iv, "=", j).reifyWith(eqs[j - 1]);
                    }
                    for (int j = 1; j <= i; j++) {
                        sr.arithm(iv, "<=", j).reifyWith(lqs[j - 1]);
                    }

                    Solver r = sr.getSolver();
                    r.setSearch(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                    while (sr.getSolver().solve()) ;
                }
                {
                    IntVar iv = sc.intVar("iv", 1, i, false);
                    BoolVar[] eqs = sc.boolVarArray("eq", i);
                    BoolVar[] lqs = sc.boolVarArray("lq", i);

                    sc.clausesIntChanneling(iv, eqs, lqs).post();

                    Solver r = sc.getSolver();
                    r.setSearch(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                    while (sc.getSolver().solve()) ;
                }
                Assert.assertEquals(sr.getSolver().getSolutionCount(), i);
                Assert.assertEquals(sc.getSolver().getSolutionCount(), i);
                Assert.assertEquals(sc.getSolver().getNodeCount(), sr.getSolver().getNodeCount());

            }
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void test2B() {
        for (int i = 1; i < 100; i++) {
            for (int seed = 3; seed < 20; seed++) {
                Model sr = new Model();
                Model sc = new Model();
                {
                    IntVar iv = sr.intVar("iv", 1, i, true);
                    BoolVar[] eqs = sr.boolVarArray("eq", i);
                    BoolVar[] lqs = sr.boolVarArray("lq", i);

                    for (int j = 1; j <= i; j++) {
                        sr.arithm(iv, "=", j).reifyWith(eqs[j - 1]);
                    }
                    for (int j = 1; j <= i; j++) {
                        sr.arithm(iv, "<=", j).reifyWith(lqs[j - 1]);
                    }

                    Solver r = sr.getSolver();
                    r.setSearch(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                    while (sr.getSolver().solve()) ;
                }
                {
                    IntVar iv = sc.intVar("iv", 1, i, true);
                    BoolVar[] eqs = sc.boolVarArray("eq", i);
                    BoolVar[] lqs = sc.boolVarArray("lq", i);

                    sc.clausesIntChanneling(iv, eqs, lqs).post();

                    Solver r = sc.getSolver();
                    r.setSearch(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                    while (sc.getSolver().solve()) ;
                }
                Assert.assertEquals(sr.getSolver().getSolutionCount(), i);
                Assert.assertEquals(sc.getSolver().getSolutionCount(), i);
                Assert.assertEquals(sc.getSolver().getNodeCount(), sr.getSolver().getNodeCount());

            }
        }
    }

}
