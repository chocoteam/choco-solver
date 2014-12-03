/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import org.chocosolver.choco.checker.DomainBuilder;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public class ScaleViewTest {

    @Test(groups = "1s")
    public void test1() {
        Solver s = new Solver();

        IntVar X = VariableFactory.enumerated("X", 1, 3, s);
        IntVar Y = VariableFactory.scale(X, 2);

        IntVar[] vars = {X, Y};

        Constraint[] cstrs = {
                IntConstraintFactory.arithm(Y, "!=", 4)
        };

        AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);

        s.post(cstrs);
        s.set(strategy);
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);
    }


    @Test(groups = "1s")
    public void test2() {
        Solver s = new Solver();

        IntVar X = VariableFactory.enumerated("X", 1, 4, s);
        IntVar Y = VariableFactory.scale(X, 3);

        IntVar[] vars = {X, Y};

        Constraint[] cstrs = {
                IntConstraintFactory.arithm(Y, "!=", -2)
        };

        AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);

        s.post(cstrs);
        s.set(strategy);
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 4);
    }

    private Solver bijective(int low, int upp, int coeff) {
        Solver s = new Solver();

        IntVar X = VariableFactory.enumerated("X", low, upp, s);
        IntVar Y = VariableFactory.scale(X, coeff);

        IntVar[] vars = {X, Y};

        Constraint[] cstrs = {
                IntConstraintFactory.arithm(Y, ">=", low + coeff - 1),
                IntConstraintFactory.arithm(Y, "<=", upp - coeff - 1)
        };

        AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);

        s.post(cstrs);
        s.set(strategy);
        return s;
    }

    private Solver contraint(int low, int upp, int coeff) {
        Solver s = new Solver();

        IntVar X = VariableFactory.enumerated("X", low, upp, s);
        IntVar C = VariableFactory.fixed("C", coeff, s);
        IntVar Y = VariableFactory.enumerated("Y", low * coeff, upp * coeff, s);

        IntVar[] vars = {X, Y};

        Constraint[] cstrs = {
                IntConstraintFactory.arithm(Y, ">=", low + coeff - 1),
                IntConstraintFactory.arithm(Y, "<=", upp - coeff - 1),
                IntConstraintFactory.times(X, C, Y)
        };

        AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);

        s.post(cstrs);
        s.set(strategy);
        return s;
    }

    @Test(groups = "10s")
    public void testRandom1() {
        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            rand.setSeed(i);
            int low = rand.nextInt(10);
            int upp = low + rand.nextInt(1000);
            int coeff = rand.nextInt(5);

            Solver sb = bijective(low, upp, coeff);
            Solver sc = contraint(low, upp, coeff);
            sb.findAllSolutions();
            sc.findAllSolutions();
            Assert.assertEquals(sc.getMeasures().getSolutionCount(), sb.getMeasures().getSolutionCount());
            //Assert.assertEquals(sc.getMeasures().getNodeCount(), sb.getMeasures().getNodeCount());
        }
    }

    @Test(groups = "1s")
    public void testRandom2() {
        Solver sb = bijective(1, 9999, 3);
        Solver sc = contraint(1, 9999, 3);
        sb.findAllSolutions();
        sc.findAllSolutions();
        Assert.assertEquals(sc.getMeasures().getSolutionCount(), sb.getMeasures().getSolutionCount());
        //Assert.assertEquals(sc.getMeasures().getNodeCount(), sb.getMeasures().getNodeCount());
    }

    @Test(groups = "1s")
    public void testIt1() {
        Random random = new Random();
        for (int seed = 0; seed < 200; seed++) {
            random.setSeed(seed);
            Solver solver = new Solver();
            int[][] domains = DomainBuilder.buildFullDomains(1, -5, 5, random, random.nextDouble(), random.nextBoolean());
            IntVar o = VariableFactory.bounded("o", domains[0][0], domains[0][domains[0].length - 1], solver);
            IntVar v = VariableFactory.scale(o, 2);
            DisposableValueIterator vit = v.getValueIterator(true);
            while (vit.hasNext()) {
                Assert.assertTrue(o.contains(vit.next() / 2));
            }
            vit.dispose();
            vit = v.getValueIterator(false);
            while (vit.hasPrevious()) {
                Assert.assertTrue(o.contains(vit.previous() / 2));
            }
            vit.dispose();
            DisposableRangeIterator rit = v.getRangeIterator(true);
            while (rit.hasNext()) {
                Assert.assertTrue(o.contains(rit.min() / 2));
                Assert.assertTrue(o.contains(rit.max() / 2));
                rit.next();
            }
            rit = v.getRangeIterator(false);
            while (rit.hasPrevious()) {
                Assert.assertTrue(o.contains(rit.min() / 2));
                Assert.assertTrue(o.contains(rit.max() / 2));
                rit.previous();
            }
        }
    }

    @Test(groups = "1s")
    public void testIt2() {
        Random random = new Random();
        for (int seed = 0; seed < 200; seed++) {
            random.setSeed(seed);
            Solver solver = new Solver();
            int[][] domains = DomainBuilder.buildFullDomains(1, -5, 5, random, random.nextDouble(), random.nextBoolean());
            IntVar o = VariableFactory.enumerated("o", domains[0], solver);
            IntVar v = VariableFactory.scale(o, 2);
			if(!solver.getSettings().enableViews()){
				try {
					// currently, the propagation is not sufficient (bound)
					// could be fixed with an extension filtering
					solver.propagate();
				}catch (Exception e){
					e.printStackTrace();
					throw new UnsupportedOperationException();
				}
			}
            DisposableValueIterator vit = v.getValueIterator(true);
            while (vit.hasNext()) {
                Assert.assertTrue(o.contains(vit.next() / 2));
            }
            vit.dispose();
            vit = v.getValueIterator(false);
            while (vit.hasPrevious()) {
                Assert.assertTrue(o.contains(vit.previous() / 2));
            }
            vit.dispose();
            DisposableRangeIterator rit = v.getRangeIterator(true);
            while (rit.hasNext()) {
                Assert.assertTrue(o.contains(rit.min() / 2));
                Assert.assertTrue(o.contains(rit.max() / 2));
                rit.next();
            }
            rit = v.getRangeIterator(false);
            while (rit.hasPrevious()) {
                Assert.assertTrue(o.contains(rit.min() / 2));
                Assert.assertTrue(o.contains(rit.max() / 2));
                rit.previous();
            }
        }
    }
}
