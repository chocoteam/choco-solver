/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.variables;

import choco.checker.DomainBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.cnf.ConjunctiveNormalForm;
import solver.constraints.nary.cnf.Literal;
import solver.constraints.nary.cnf.Node;
import solver.constraints.reified.ReifiedConstraint;
import solver.constraints.ternary.Max;
import solver.search.strategy.StrategyFactory;

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
        solver.post(new ReifiedConstraint(
                bs[0],
                ConstraintFactory.eq(z, x, solver),
                ConstraintFactory.neq(z, x, solver),
                solver));
        solver.post(new ReifiedConstraint(
                bs[1],
                ConstraintFactory.eq(z, y, solver),
                ConstraintFactory.neq(z, y, solver),
                solver));
        solver.post(new ReifiedConstraint(
                bs[2],
                ConstraintFactory.geq(x, y, solver),
                ConstraintFactory.lt(x, y, solver),
                solver));
        solver.post(new ConjunctiveNormalForm(
                Node.or(Node.and(Literal.pos(bs[0]), Literal.pos(bs[2])),
                        Node.and(Literal.pos(bs[1]), Literal.neg(bs[2]))),
                solver
        ));
    }

    public void max(Solver solver, IntVar x, IntVar y, IntVar z) {
        solver.post(new Max(z, x, y, solver));
    }

    @Test
    public void testMax1(){
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
                ref.set(StrategyFactory.random(xs, ref.getEnvironment(), seed));
            }
            Solver solver = new Solver();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = VariableFactory.bounded("x", domains[0][0], domains[0][1], solver);
                xs[1] = VariableFactory.bounded("y", domains[1][0], domains[1][1], solver);
                xs[2] = VariableFactory.bounded("z", domains[1][0], domains[2][1], solver);
                max(solver, xs[0], xs[1], xs[2]);
//                SearchMonitorFactory.log(solver, true, true);
                solver.set(StrategyFactory.random(xs, solver.getEnvironment(), seed));
            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount(), "SOLUTIONS ("+seed+")");
            Assert.assertTrue(solver.getMeasures().getNodeCount() <= ref.getMeasures().getNodeCount(), "NODES ("+seed+")");
        }
    }

    @Test
    public void testMax2(){
        Random random = new Random();
        for (int seed = 0; seed < 9999; seed++) {
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
                ref.set(StrategyFactory.random(xs, ref.getEnvironment(), seed));
            }
            Solver solver = new Solver();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = VariableFactory.enumerated("x", domains[0], solver);
                xs[1] = VariableFactory.enumerated("y", domains[1], solver);
                xs[2] = VariableFactory.enumerated("z", domains[2], solver);
                max(solver, xs[0], xs[1], xs[2]);
//                SearchMonitorFactory.log(solver, true, true);
                solver.set(StrategyFactory.random(xs, solver.getEnvironment(), seed));
            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount(), "SOLUTIONS ("+seed+")");
            Assert.assertTrue(solver.getMeasures().getNodeCount() <= ref.getMeasures().getNodeCount(), "NODES ("+seed+")");
        }
    }
}
