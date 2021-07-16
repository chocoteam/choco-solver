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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
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
public abstract class AbstractBinaryTest {

    protected long brutForceTest(int[][] domains, boolean bounded) {
        long nbSol = 0;
        int vx, vy;
        if (bounded) {
            for (vx = domains[0][0]; vx <= domains[0][1]; vx++) {
                for (vy = domains[1][0]; vy <= domains[1][1]; vy++) {
                    nbSol += validTuple(vx, vy);
                }
            }
        } else {
            for (int i = 0; i < domains[0].length && (vx = domains[0][i]) >= domains[0][0]; i++) {
                for (int j = 0; j < domains[1].length && (vy = domains[1][j]) >= domains[1][0]; j++) {
                    nbSol += validTuple(vx, vy);
                }
            }
        }
        return nbSol;
    }

    protected abstract int validTuple(int vx, int vy);

    protected abstract Constraint make(IntVar[] vars, Model model);

    public Model modeler(int[][] domains, boolean bounded, long seed) {
        Model s = new Model();
        IntVar[] vars = new IntVar[2];
        for (int i = 0; i < 2; i++) {
            if (bounded) {
                vars[i] = s.intVar("x_" + i, domains[i][0], domains[i][1], true);
            } else {
                vars[i] = s.intVar("x_" + i, domains[i]);
            }
        }
        make(vars, s).post();
        Solver r = s.getSolver();
        r.setSearch(randomSearch(vars,seed));
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
			if(k==0){
				seed = 1410851231099l;
			}
            rand.setSeed(seed);
            bounded = rand.nextBoolean();
            int size = 5; // domain size
            int range = 15; // value range
            int[][] domains;
            if (bounded) {
                domains = DomainBuilder.buildFullDomains(2, size, range, rand);
            } else {
                domains = DomainBuilder.buildFullDomains2(2, size, range, rand, rand.nextDouble(), rand.nextBoolean());
            }
            // total number of solutions: brut force algorithm
            long base = brutForceTest(domains, bounded);
            Model s = modeler(domains, bounded, seed);
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
