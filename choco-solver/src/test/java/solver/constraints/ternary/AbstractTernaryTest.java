/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.choco.checker.DomainBuilder;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

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

    protected abstract Constraint make(IntVar[] vars, Solver solver);

    public Solver modeler(int[][] domains, boolean bounded, long seed) {
        Solver s = new Solver();
        IntVar[] vars = new IntVar[3];
        for (int i = 0; i < 3; i++) {
            if (bounded) {
                vars[i] = VariableFactory.bounded("x_" + i, domains[i][0], domains[i][1], s);
            } else {
                vars[i] = VariableFactory.enumerated("x_" + i, domains[i], s);
            }
        }
        Constraint div = make(vars, s);
        s.post(div);
		if(bounded){
			s.set(IntStrategyFactory.random_bound(vars, seed));
		}else{
			s.set(IntStrategyFactory.random_value(vars, seed));
		}
        return s;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test(groups = "10s")
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
            Solver s = modeler(domains, bounded, seed);
//            SearchMonitorFactory.log(s, false, false);
            try {
                s.findAllSolutions();
            } catch (AssertionError ae) {
                System.err.printf("seed: %d\n", seed);
                throw ae;
            }
            long cp = s.getMeasures().getSolutionCount();
            Assert.assertEquals(cp, base, "found: " + cp + " solutions, while " + base + " are expected (" + seed + ")");
        }
    }
}
