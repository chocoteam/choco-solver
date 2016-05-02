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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public class OffsetViewTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Model s = new Model();

        IntVar X = s.intVar("X", 1, 3, false);
        IntVar Y = s.intOffsetView(X, 2);

        IntVar[] vars = {X, Y};

        s.arithm(Y, "!=", 4).post();

        s.getSolver().set(inputOrderLBSearch(vars));
        while (s.solve()) ;
        assertEquals(s.getSolver().getSolutionCount(), 2);
    }


    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Model s = new Model();

        IntVar X = s.intVar("X", 1, 4, false);
        IntVar Y = s.intOffsetView(X, 3);

        IntVar[] vars = {X, Y};

        s.arithm(Y, "!=", -2).post();

        s.getSolver().set(inputOrderLBSearch(vars));
        while (s.solve()) ;
        assertEquals(s.getSolver().getSolutionCount(), 4);
    }

    private Model bijective(int low, int upp, int coeff) {
        Model s = new Model();

        IntVar X = s.intVar("X", low, upp, false);
        IntVar Y = s.intOffsetView(X, coeff);

        IntVar[] vars = {X, Y};

        s.arithm(Y, ">=", low + coeff - 1).post();
        s.arithm(Y, "<=", upp - coeff - 1).post();

        s.getSolver().set(inputOrderLBSearch(vars));
        return s;
    }

    private Model contraint(int low, int upp, int coeff) {
        Model s = new Model();

        IntVar X = s.intVar("X", low, upp, false);
        IntVar Y = s.intVar("Y", low + coeff, upp + coeff, false);

        IntVar[] vars = {X, Y};

        s.arithm(Y, ">=", low + coeff - 1).post();
        s.arithm(Y, "<=", upp - coeff - 1).post();
        s.arithm(X, "=", Y, "+", coeff).post();

        s.getSolver().set(inputOrderLBSearch(vars));
        return s;
    }

    @Test(groups="5m", timeOut=300000)
    public void testRandom1() {
        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            rand.setSeed(i);
            int low = rand.nextInt(10);
            int upp = low + rand.nextInt(1000);
            int coeff = rand.nextInt(50);

            Model sb = bijective(low, upp, coeff);
            Model sc = contraint(low, upp, coeff);
            while (sb.solve()) ;
            while (sc.solve()) ;
            assertEquals(sc.getSolver().getSolutionCount(), sb.getSolver().getSolutionCount());
            assertEquals(sc.getSolver().getNodeCount(), sb.getSolver().getNodeCount());

        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testRandom2() {
        Model sb = bijective(1, 1999, 3);
        Model sc = contraint(1, 1999, 3);
        while (sb.solve()) ;
        while (sc.solve()) ;
        assertEquals(sc.getSolver().getSolutionCount(), sb.getSolver().getSolutionCount());
        assertEquals(sc.getSolver().getNodeCount(), sb.getSolver().getNodeCount());

    }

    @Test(groups="5m", timeOut=300000)
    public void testRandom3() {
        int N = 9999;
        for (int i = 1; i < 10; i++) {
            Model sb = bijective(1, N, 3);
            Model sc = contraint(1, N, 3);
            while (sb.solve()) ;
            while (sc.solve()) ;
            assertEquals(sc.getSolver().getSolutionCount(), sb.getSolver().getSolutionCount());
            assertEquals(sc.getSolver().getNodeCount(), sb.getSolver().getNodeCount());
        }

    }

    @Test(groups="1s", timeOut=60000)
    public void testIt1() {
        Random random = new Random();
        for (int seed = 0; seed < 200; seed++) {
            random.setSeed(seed);
            Model model = new Model();
            int[][] domains = DomainBuilder.buildFullDomains(1, -5, 5, random, random.nextDouble(), random.nextBoolean());
            IntVar o = model.intVar("o", domains[0][0], domains[0][domains[0].length - 1], true);
            IntVar v = model.intOffsetView(o, 2);
            DisposableValueIterator vit = v.getValueIterator(true);
            while (vit.hasNext()) {
                Assert.assertTrue(o.contains(vit.next() - 2));
            }
            vit.dispose();
            vit = v.getValueIterator(false);
            while (vit.hasPrevious()) {
                Assert.assertTrue(o.contains(vit.previous() - 2));
            }
            vit.dispose();
            DisposableRangeIterator rit = v.getRangeIterator(true);
            while (rit.hasNext()) {
                Assert.assertTrue(o.contains(rit.min() - 2));
                Assert.assertTrue(o.contains(rit.max() - 2));
                rit.next();
            }
            rit = v.getRangeIterator(false);
            while (rit.hasPrevious()) {
                Assert.assertTrue(o.contains(rit.min() - 2));
                Assert.assertTrue(o.contains(rit.max() - 2));
                rit.previous();
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testIt2() {
        Random random = new Random();
        for (int seed = 0; seed < 200; seed++) {
            random.setSeed(seed);
            Model model = new Model();
            int[][] domains = DomainBuilder.buildFullDomains(1, -5, 5, random, random.nextDouble(), random.nextBoolean());
            IntVar o = model.intVar("o", domains[0]);
            IntVar v = model.intOffsetView(o, 2);
			if(!model.getSettings().enableViews()){
				try {
					model.getSolver().propagate();
				}catch (Exception e){
					e.printStackTrace();
					throw new UnsupportedOperationException();
				}
			}
            DisposableValueIterator vit = v.getValueIterator(true);
            while (vit.hasNext()) {
                Assert.assertTrue(o.contains(vit.next() - 2));
            }
            vit.dispose();
            vit = v.getValueIterator(false);
            while (vit.hasPrevious()) {
                Assert.assertTrue(o.contains(vit.previous() - 2));
            }
            vit.dispose();
            DisposableRangeIterator rit = v.getRangeIterator(true);
            while (rit.hasNext()) {
                Assert.assertTrue(o.contains(rit.min() - 2));
                Assert.assertTrue(o.contains(rit.max() - 2));
                rit.next();
            }
            rit = v.getRangeIterator(false);
            while (rit.hasPrevious()) {
                Assert.assertTrue(o.contains(rit.min() - 2));
                Assert.assertTrue(o.contains(rit.max() - 2));
                rit.previous();
            }
        }
    }
}