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
package solver.constraints.binary;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.selectors.values.IntDomainRandom;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/04/12
 */
public class DistanceTest {

    @Test(groups = "1s")
    public void test1() {
        for (int i = 0; i < 100; i++) {
            long nbSol, nbNod;
            {
				final Solver solver = new Solver();
                IntVar X = VariableFactory.enumerated("X", 1, 10, solver);
                IntVar Y = VariableFactory.enumerated("Y", 1, 10, solver);
				IntVar diff = VariableFactory.bounded("X-Y",-9,9,solver);
				solver.post(IntConstraintFactory.sum(new IntVar[]{Y,diff},X));
                IntVar Z = VariableFactory.abs(diff);
                solver.post(IntConstraintFactory.arithm(Z, "=", 5));
                solver.set(IntStrategyFactory.random_value(new IntVar[]{X, Y}, i));
//				solver.getSearchLoop().plugSearchMonitor(new IMonitorSolution() {
//					@Override
//					public void onSolution() {
//						System.out.println("REF");
//						for(Variable v:solver.getVars()){
//							System.out.println(v+" inst? "+v.instantiated());
//						}
//					}
//				});
                solver.findAllSolutions();
                nbSol = solver.getMeasures().getSolutionCount();
                nbNod = solver.getMeasures().getNodeCount();
            }
            {
                final Solver solver = new Solver();
                IntVar X = VariableFactory.enumerated("X", 1, 10, solver);
                IntVar Y = VariableFactory.enumerated("Y", 1, 10, solver);
                solver.post(IntConstraintFactory.distance(X, Y, "=", 5));
                solver.set(IntStrategyFactory.random_value(new IntVar[]{X, Y}, i));
//				solver.getSearchLoop().plugSearchMonitor(new IMonitorSolution() {
//					@Override
//					public void onSolution() {
//						System.out.println("NO REF");
//						for(Variable v:solver.getVars()){
//							System.out.println(v);
//						}
//					}
//				});
                solver.findAllSolutions();
                Assert.assertEquals(solver.getMeasures().getSolutionCount(), nbSol);
                Assert.assertTrue(solver.getMeasures().getNodeCount() <= nbNod);
            }
        }
    }

    @Test(groups = "1s")
    public void test2() {
        for (int k = 4; k < 400; k *= 2) {
            Solver s1 = new Solver(), s2 = new Solver();
            IntVar[] vs1, vs2;
            Propagator p1, p2;
            {
                IntVar X = VariableFactory.enumerated("X", 1, k, s1);
                IntVar Y = VariableFactory.enumerated("Y", 1, k, s1);
                vs1 = new IntVar[]{X, Y};
                Constraint c = IntConstraintFactory.distance(X, Y, "=", k / 2);
                s1.post(c);
                p1 = c.getPropagator(0);
            }
            {
                IntVar X = VariableFactory.enumerated("X", 1, k, s2);
                IntVar Y = VariableFactory.enumerated("Y", 1, k, s2);
                vs2 = new IntVar[]{X, Y};
                Constraint c = IntConstraintFactory.distance(X, Y, "=", k / 2);
                s2.post(c);
                p2 = c.getPropagator(0);
            }

            try {
                s1.propagate();
                s2.propagate();
                Assert.assertEquals(vs1[0].getDomainSize(), vs2[0].getDomainSize());
                Assert.assertEquals(vs1[1].getDomainSize(), vs2[1].getDomainSize());

                for (int j = 0; j < 1000; j++) {
                    s1.getEnvironment().worldPush();
                    s2.getEnvironment().worldPush();

                    IntDomainRandom r = new IntDomainRandom(j);
                    int val = r.selectValue(vs1[0]);
                    vs1[0].removeValue(val, Cause.Null);
                    vs2[0].removeValue(val, Cause.Null);

                    s1.propagate();
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

    @Test(groups = "1s")
    public void test3() {
        Solver solver = new Solver();
        IntVar X = VariableFactory.bounded("X", -5, 5, solver);
        IntVar Y = VariableFactory.bounded("Y", -5, 5, solver);
        IntVar Z = VariableFactory.bounded("Z", 0, 10, solver);
        solver.post(IntConstraintFactory.distance(X, Y, "=", Z));
        solver.set(IntStrategyFactory.lexico_LB(new IntVar[]{Z, X, Y, Z}));
//        SearchMonitorFactory.log(solver, true, true);
        solver.findAllSolutions();
//        System.out.printf("end\n");
    }

}
