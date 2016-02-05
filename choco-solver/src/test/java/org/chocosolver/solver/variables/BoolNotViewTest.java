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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.search.strategy.IntStrategyFactory.random_bound;
import static org.testng.Assert.assertTrue;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11/02/13
 */
public class BoolNotViewTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Random random = new Random();
        for (int seed = 0; seed < 2000; seed++) {
            random.setSeed(seed);
            Solver ref = new Solver();
            {
                BoolVar[] xs = new BoolVar[2];
                xs[0] = ref.boolVar("x");
                xs[1] = ref.boolVar("y");
                ref.sum(xs, "=", 1).post();
                ref.set(random_bound(xs, seed));
            }
            Solver solver = new Solver();
            {
                BoolVar[] xs = new BoolVar[2];
                xs[0] = solver.boolVar("x");
                xs[1] = solver.boolNotView(xs[0]);
                solver.sum(xs, "=", 1).post();
                solver.set(random_bound(xs, seed));
            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount());

        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testIt() {
        Solver ref = new Solver();
        BoolVar o = ref.boolVar("b");
        BoolVar v = ref.boolNotView(o);
        DisposableValueIterator vit = v.getValueIterator(true);
        while (vit.hasNext()) {
            Assert.assertTrue(o.contains(vit.next()));
        }
        vit.dispose();
        vit = v.getValueIterator(false);
        while (vit.hasNext()) {
            Assert.assertTrue(o.contains(vit.next()));
        }
        vit.dispose();
        DisposableRangeIterator rit = v.getRangeIterator(true);
        while (rit.hasNext()) {
            rit.next();
            Assert.assertTrue(o.contains(rit.min()));
            Assert.assertTrue(o.contains(rit.max()));
        }
        rit = v.getRangeIterator(false);
        while (rit.hasNext()) {
            rit.next();
            Assert.assertTrue(o.contains(rit.min()));
            Assert.assertTrue(o.contains(rit.max()));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testPrevNext() {
        Solver solver = new Solver();
        BoolVar a = solver.boolVar("a");
        BoolVar b = solver.boolVar("b");
        solver.arithm(a, "+", solver.boolNotView(b), "=", 2).post();
        assertTrue(solver.findSolution());
    }
}
