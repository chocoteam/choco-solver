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

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainRandom;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.IntStrategyFactory.lexico_LB;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.random_value;
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
                model.set(random_value(new IntVar[]{X, Y}, i));
//				solver.getResolver().plugSearchMonitor(new IMonitorSolution() {
//					@Override
//					public void onSolution() {
//						System.out.println("REF");
//						for(Variable v:solver.getVars()){
//							System.out.println(v+" inst? "+v.instantiated());
//						}
//					}
//				});
                model.solveAll();
                nbSol = model.getMeasures().getSolutionCount();
                nbNod = model.getMeasures().getNodeCount();
            }
            {
                final Model model = new Model();
                IntVar X = model.intVar("X", 1, 10, false);
                IntVar Y = model.intVar("Y", 1, 10, false);
                model.distance(X, Y, "=", 5).post();
                model.set(random_value(new IntVar[]{X, Y}, i));
//				solver.getResolver().plugSearchMonitor(new IMonitorSolution() {
//					@Override
//					public void onSolution() {
//						System.out.println("NO REF");
//						for(Variable v:solver.getVars()){
//							System.out.println(v);
//						}
//					}
//				});
                model.solveAll();
                assertEquals(model.getMeasures().getSolutionCount(), nbSol);
                assertTrue(model.getMeasures().getNodeCount() <= nbNod);
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

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Model model = new Model();
        IntVar X = model.intVar("X", -5, 5, true);
        IntVar Y = model.intVar("Y", -5, 5, true);
        IntVar Z = model.intVar("Z", 0, 10, true);
        model.distance(X, Y, "=", Z).post();
        model.set(lexico_LB(new IntVar[]{Z, X, Y, Z}));
//        SearchMonitorFactory.log(solver, true, true);
        model.solveAll();
//        System.out.printf("end\n");
    }

}
