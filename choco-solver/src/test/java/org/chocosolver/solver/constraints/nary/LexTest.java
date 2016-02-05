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
package org.chocosolver.solver.constraints.nary;


import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 5 avr. 2006
 * Time: 08:42:43
 */
public class LexTest {


    @Test(groups="1s", timeOut=60000)
    public void testLessLexq() {
        for (int seed = 0; seed < 5; seed++) {
            Solver solver = new Solver();
            int n1 = 8;
            int k = 2;
            IntVar[] vs1 = new IntVar[n1 / 2];
            IntVar[] vs2 = new IntVar[n1 / 2];
            for (int i = 0; i < n1 / 2; i++) {
                vs1[i] = solver.intVar("" + i, 0, k, true);
                vs2[i] = solver.intVar("" + i, 0, k, true);
            }
            solver.post(solver.lexLessEq(vs1, vs2));
            solver.set(IntStrategyFactory.random_bound(ArrayUtils.append(vs1, vs2), seed));
            solver.findAllSolutions();
            int kpn = (int) Math.pow(k + 1, n1 / 2);
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), (kpn * (kpn + 1) / 2));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testLex() {
        for (int seed = 0; seed < 5; seed++) {
            Solver solver = new Solver();
            int n1 = 8;
            int k = 2;
            IntVar[] vs1 = new IntVar[n1 / 2];
            IntVar[] vs2 = new IntVar[n1 / 2];
            for (int i = 0; i < n1 / 2; i++) {
                vs1[i] = solver.intVar("" + i, 0, k, true);
                vs2[i] = solver.intVar("" + i, 0, k, true);
            }
            solver.post(solver.lexLess(vs1, vs2));
            solver.set(IntStrategyFactory.random_bound(ArrayUtils.append(vs1, vs2), seed));

            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 3240);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testLexiSatisfied() {
        Solver solver = new Solver();
        IntVar v1 = solver.intVar("v1", 1, 1, true);
        IntVar v2 = solver.intVar("v2", 2, 2, true);
        IntVar v3 = solver.intVar("v3", 3, 3, true);
        Constraint c1 = solver.lexLess(new IntVar[]{v1, v2}, new IntVar[]{v1, v3});
        Constraint c2 = solver.lexLess(new IntVar[]{v1, v2}, new IntVar[]{v1, v2});
        Constraint c3 = solver.lexLess(new IntVar[]{v1, v2}, new IntVar[]{v1, v1});
        Constraint c4 = solver.lexLessEq(new IntVar[]{v1, v2}, new IntVar[]{v1, v3});
        Constraint c5 = solver.lexLessEq(new IntVar[]{v1, v2}, new IntVar[]{v1, v2});
        Constraint c6 = solver.lexLessEq(new IntVar[]{v1, v2}, new IntVar[]{v1, v1});
        solver.post(c1, c2, c3, c4, c5, c6);
        Assert.assertEquals(ESat.TRUE, c1.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c2.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c3.isSatisfied());
        Assert.assertEquals(ESat.TRUE, c4.isSatisfied());
        Assert.assertEquals(ESat.TRUE, c5.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c6.isSatisfied());
    }


    @Test(groups="1s", timeOut=60000)
    public void testAshish() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = solver.intVar("a1", 5, 7, true);
        a[1] = solver.intVar("a2", 1, 1, true);

        b[0] = solver.intVar("b1", 5, 8, true);
        b[1] = solver.intVar("b2", 0, 0, true);


        solver.post(solver.lexLess(a, b));
        try {
            solver.propagate();

        } catch (ContradictionException e) {
            Assert.fail();
        }
//        SearchMonitorFactory.log(solver, true, true);
        solver.findAllSolutions();
        Assert.assertEquals(6, solver.getMeasures().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testBug1() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = solver.intVar("a2", new int[]{5, 8});
        a[1] = solver.intVar("a3", new int[]{-2, 0});

        b[0] = solver.intVar("b2", new int[]{5, 8});
        b[1] = solver.intVar("b3", new int[]{-3, -2});


        solver.post(solver.lexLess(a, b));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
        Assert.assertEquals(5, a[0].getUB());
//        SearchMonitorFactory.log(solver, true, true);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBug2() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = solver.intVar("a1", new int[]{-2, 5});
        a[1] = solver.intVar("a2", new int[]{-1, 1});

        b[0] = solver.intVar("b1", new int[]{3, 5});
        b[1] = solver.intVar("b2", new int[]{-6, -1});


        solver.post(solver.lexLess(a, b));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
        Assert.assertEquals(-2, a[0].getUB());
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 8);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBug3() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = solver.intVar("a1", new int[]{5});
        a[1] = solver.intVar("a2", new int[]{-1, 1});

        b[0] = solver.intVar("b1", new int[]{3, 5});
        b[1] = solver.intVar("b2", new int[]{-6, -1});


        solver.post(solver.lexLess(a, b));
        try {
            solver.propagate();
            Assert.fail();
        } catch (ContradictionException ignored) {
        }
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBug4() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[5];
        IntVar[] b = new IntVar[5];

        a[0] = solver.intVar("a1", new int[]{2});
        a[1] = solver.intVar("a2", new int[]{1, 3, 4});
        a[2] = solver.intVar("a3", new int[]{1, 2, 3, 4, 5});
        a[3] = solver.intVar("a4", new int[]{1, 2});
        a[4] = solver.intVar("a5", new int[]{3, 4, 5});

        b[0] = solver.intVar("b1", new int[]{0, 1, 2});
        b[1] = solver.intVar("b2", new int[]{1});
        b[2] = solver.intVar("b3", new int[]{0, 1, 2, 3, 4});
        b[3] = solver.intVar("b4", new int[]{0, 1});
        b[4] = solver.intVar("b5", new int[]{0, 1, 2});


        solver.post(solver.lexLess(a, b));
        try {
            solver.propagate();
        } catch (ContradictionException ignored) {
        }
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 216);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBug5() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[3];
        IntVar[] b = new IntVar[3];

        a[0] = solver.intVar("a1", new int[]{-10, -3, 2});
        a[1] = solver.intVar("a2", new int[]{-5, -4, 2});
        a[2] = solver.intVar("a3", new int[]{2});

        b[0] = solver.intVar("b1", new int[]{-10, -1, 3});
        b[1] = solver.intVar("b2", new int[]{-5});
        b[2] = solver.intVar("b3", new int[]{-4, 2});


        solver.post(solver.lexLess(a, b));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
        Assert.assertEquals(-1, b[0].getLB());
//        SearchMonitorFactory.log(solver, true, false);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 30);
    }
}
