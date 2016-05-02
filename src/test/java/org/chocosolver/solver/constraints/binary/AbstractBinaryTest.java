/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/07/12
 */
public abstract class AbstractBinaryTest {

    @DataProvider(name = "params")
    public Object[][] data1D(){
        // indicates whether to use explanations or not
        List<Object[]> elt = new ArrayList<>();
        elt.add(new Object[]{true});
        elt.add(new Object[]{false});
        return elt.toArray(new Object[elt.size()][1]);
    }

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
        r.set(randomSearch(vars,seed));
        return s;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test(groups="10s", timeOut=60000, dataProvider = "params")
    public void test1(boolean exp) {
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
//            SearchMonitorFactory.log(s, false, false);
            if(exp){
                s.getSolver().setCBJLearning(false, false);
            }
            try {
                while (s.solve()) ;
            } catch (AssertionError ae) {
                System.err.printf("seed: %d\n", seed);
                throw ae;
            }
            long cp = s.getSolver().getSolutionCount();
            Assert.assertEquals(cp, base, "found: " + cp + " solutions, while " + base + " are expected (" + seed + ")");
        }
    }
}
