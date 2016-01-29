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
package org.chocosolver.solver.variables;

import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public class MaxViewTest {

    public void maxref(Solver solver, IntVar x, IntVar y, IntVar z) {
        BoolVar[] bs = VariableFactory.boolArray("b", 3, solver);
        LogicalConstraintFactory.ifThenElse(bs[0], IntConstraintFactory.arithm(z, "=", x), IntConstraintFactory.arithm(z, "!=", x));
        LogicalConstraintFactory.ifThenElse(bs[1], IntConstraintFactory.arithm(z, "=", y), IntConstraintFactory.arithm(z, "!=", y));
        LogicalConstraintFactory.ifThenElse(bs[2], IntConstraintFactory.arithm(x, ">=", y), IntConstraintFactory.arithm(x, "<", y));
        SatFactory.addClauses(LogOp.or(LogOp.and(bs[0], bs[2]),
                LogOp.and(bs[1], bs[2].not())), solver);
    }

    public void max(Solver solver, IntVar x, IntVar y, IntVar z) {
        solver.post(IntConstraintFactory.maximum(z, x, y));
    }

    @Test(groups = "10s")
    public void testMax1() {
        Random random = new Random();
        for (int seed = 1; seed < 9999; seed++) {
            random.setSeed(seed);
            int[][] domains = DomainBuilder.buildFullDomains(3, 1, 15);
            Solver ref = new Solver();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = VariableFactory.bounded("x", domains[0][0], domains[0][1], ref);
                xs[1] = VariableFactory.bounded("y", domains[1][0], domains[1][1], ref);
                xs[2] = VariableFactory.bounded("z", domains[2][0], domains[2][1], ref);
                maxref(ref, xs[0], xs[1], xs[2]);
//                SearchMonitorFactory.log(ref, true, true);
                ref.set(IntStrategyFactory.random_bound(xs, seed));
            }
            Solver solver = new Solver();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = VariableFactory.bounded("x", domains[0][0], domains[0][1], solver);
                xs[1] = VariableFactory.bounded("y", domains[1][0], domains[1][1], solver);
                xs[2] = VariableFactory.bounded("z", domains[1][0], domains[2][1], solver);
                max(solver, xs[0], xs[1], xs[2]);
//                SearchMonitorFactory.log(solver, true, true);
                solver.set(IntStrategyFactory.random_bound(xs, seed));
            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount(), "SOLUTIONS (" + seed + ")");
            Assert.assertTrue(solver.getMeasures().getNodeCount() <= ref.getMeasures().getNodeCount(), "NODES (" + seed + ")");
        }
    }

    @Test(groups = "10s")
    public void testMax2() {
        Random random = new Random();
        for (int seed = 169; seed < 9999; seed++) {
            random.setSeed(seed);
            int[][] domains = DomainBuilder.buildFullDomains(3, 1, 15, random, random.nextDouble(), random.nextBoolean());
            Solver ref = new Solver();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = VariableFactory.enumerated("x", domains[0], ref);
                xs[1] = VariableFactory.enumerated("y", domains[1], ref);
                xs[2] = VariableFactory.enumerated("z", domains[2], ref);
                maxref(ref, xs[0], xs[1], xs[2]);
//                SearchMonitorFactory.log(ref, true, true);
                ref.set(IntStrategyFactory.random_value(xs, seed));
            }
            Solver solver = new Solver();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = VariableFactory.enumerated("x", domains[0], solver);
                xs[1] = VariableFactory.enumerated("y", domains[1], solver);
                xs[2] = VariableFactory.enumerated("z", domains[2], solver);
                max(solver, xs[0], xs[1], xs[2]);
//                SearchMonitorFactory.log(solver, true, true);
                solver.set(IntStrategyFactory.random_value(xs, seed));
            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount(), "SOLUTIONS (" + seed + ")");
            // BEWARE: MAX does not ensure AC, unlike reformulation; so nb of nodes can be different...
//            Assert.assertTrue(solver.getMeasures().getNodeCount() <= ref.getMeasures().getNodeCount(), "NODES (" + seed + "): "
//                    + solver.getMeasures().getNodeCount() + " vs. " + ref.getMeasures().getNodeCount());
        }
    }
}
