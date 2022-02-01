/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/07/12
 */
public abstract class AbstractTernaryTest {

    protected long brutForceTest(int[][] domains, boolean bounded) {
        long nbSol = 0;
        int vx, vy, vz;
        if (bounded) {
            for (vx = domains[0][0]; vx <= domains[0][1]; vx++) {
                for (vy = domains[1][0]; vy <= domains[1][1]; vy++) {
                    for (vz = domains[2][0]; vz <= domains[2][1]; vz++) {
                        nbSol += validTuple(vx, vy, vz);
                    }
                }
            }
        } else {
            for (int i = 0; i < domains[0].length && (vx = domains[0][i]) >= domains[0][0]; i++) {
                for (int j = 0; j < domains[1].length && (vy = domains[1][j]) >= domains[1][0]; j++) {
                    for (int k = 0; k < domains[2].length && (vz = domains[2][k]) >= domains[2][0]; k++) {
                        nbSol += validTuple(vx, vy, vz);
                    }
                }
            }
        }
        return nbSol;
    }

    protected abstract int validTuple(int vx, int vy, int vz);

    protected abstract Constraint make(IntVar[] vars, Model model);

    public Model modeler(int[][] domains, boolean bounded, long seed) {
        Model s = new Model();
        IntVar[] vars = new IntVar[3];
        for (int i = 0; i < 3; i++) {
            if (bounded) {
                vars[i] = s.intVar("x_" + i, domains[i][0], domains[i][1], true);
            } else {
                vars[i] = s.intVar("x_" + i, domains[i]);
            }
        }
        Constraint div = make(vars, s);
        div.post();
        s.getSolver().setSearch(randomSearch(vars,seed));
        return s;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test(groups="10s", timeOut=60000)
    public void test1() {
        boolean bounded; // true if domains are bounded, false if they are enumerated
        Random rand = new Random(0);
        for (int k = 0; k < 20000; k++) {
            long seed = System.currentTimeMillis();
            rand.setSeed(seed);
            bounded = rand.nextBoolean();
            int size = 5; // domain size
            int range = 15; // value range
            int[][] domains;
            if (bounded) {
                domains = DomainBuilder.buildFullDomains(3, size, range, rand);
            } else {
                domains = DomainBuilder.buildFullDomains2(3, size, range, rand, rand.nextDouble(), rand.nextBoolean());
            }
            // total number of solutions: brut force algorithm
            long base = brutForceTest(domains, bounded);
            Model s = modeler(domains, bounded, seed);
//            SearchMonitorFactory.log(s, false, false);
            try {
                while (s.getSolver().solve()) ;
            } catch (AssertionError ae) {
                System.err.printf("seed: %d\n", seed);
                throw ae;
            }
            long cp = s.getSolver().getSolutionCount();
            Assert.assertEquals(cp, base, "found: " + cp + " solutions, while " + base + " are expected (" + seed + ")");
        }
    }
}
