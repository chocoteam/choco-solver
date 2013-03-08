/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
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

package solver.constraints.nary;


import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.ESat;
import util.tools.ArrayUtils;


/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 5 avr. 2006
 * Time: 08:42:43
 */
public class LexTest {


    @Test(groups = "1s")
    public void testLessLexq() {
        for (int seed = 0; seed < 5; seed++) {
            Solver solver = new Solver();
            int n1 = 8;
            int k = 2;
            IntVar[] vs1 = new IntVar[n1 / 2];
            IntVar[] vs2 = new IntVar[n1 / 2];
            for (int i = 0; i < n1 / 2; i++) {
                vs1[i] = VariableFactory.bounded("" + i, 0, k, solver);
                vs2[i] = VariableFactory.bounded("" + i, 0, k, solver);
            }
            solver.post(IntConstraintFactory.lex_less_eq(vs1, vs2));
            solver.set(IntStrategyFactory.random(ArrayUtils.append(vs1, vs2), seed));
            solver.findAllSolutions();
            int kpn = (int) Math.pow(k + 1, n1 / 2);
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), (kpn * (kpn + 1) / 2));
        }
    }

    @Test(groups = "1s")
    public void testLex() {
        for (int seed = 0; seed < 5; seed++) {
            Solver solver = new Solver();
            int n1 = 8;
            int k = 2;
            IntVar[] vs1 = new IntVar[n1 / 2];
            IntVar[] vs2 = new IntVar[n1 / 2];
            for (int i = 0; i < n1 / 2; i++) {
                vs1[i] = VariableFactory.bounded("" + i, 0, k, solver);
                vs2[i] = VariableFactory.bounded("" + i, 0, k, solver);
            }
            solver.post(IntConstraintFactory.lex_less(vs1, vs2));
            solver.set(IntStrategyFactory.random(ArrayUtils.append(vs1, vs2), seed));

            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 3240);
        }
    }

    @Test(groups = "1s")
    public void testLexiSatisfied() {
        Solver solver = new Solver();
        IntVar v1 = VariableFactory.bounded("v1", 1, 1, solver);
        IntVar v2 = VariableFactory.bounded("v2", 2, 2, solver);
        IntVar v3 = VariableFactory.bounded("v3", 3, 3, solver);
        Constraint c1 = IntConstraintFactory.lex_less(new IntVar[]{v1, v2}, new IntVar[]{v1, v3});
        Constraint c2 = IntConstraintFactory.lex_less(new IntVar[]{v1, v2}, new IntVar[]{v1, v2});
        Constraint c3 = IntConstraintFactory.lex_less(new IntVar[]{v1, v2}, new IntVar[]{v1, v1});
        Constraint c4 = IntConstraintFactory.lex_less_eq(new IntVar[]{v1, v2}, new IntVar[]{v1, v3});
        Constraint c5 = IntConstraintFactory.lex_less_eq(new IntVar[]{v1, v2}, new IntVar[]{v1, v2});
        Constraint c6 = IntConstraintFactory.lex_less_eq(new IntVar[]{v1, v2}, new IntVar[]{v1, v1});
        solver.post(c1, c2, c3, c4, c5, c6);
        Assert.assertEquals(ESat.TRUE, c1.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c2.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c3.isSatisfied());
        Assert.assertEquals(ESat.TRUE, c4.isSatisfied());
        Assert.assertEquals(ESat.TRUE, c5.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c6.isSatisfied());
    }


    @Test(groups = "1s")
    public void testAshish() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = VariableFactory.bounded("a1", 5, 7, solver);
        a[1] = VariableFactory.bounded("a2", 1, 1, solver);

        b[0] = VariableFactory.bounded("b1", 5, 8, solver);
        b[1] = VariableFactory.bounded("b2", 0, 0, solver);


        solver.post(IntConstraintFactory.lex_less(a, b));
        try {
            solver.propagate();

        } catch (ContradictionException e) {
            Assert.fail();
        }
        SearchMonitorFactory.log(solver, true, true);
        solver.findAllSolutions();
        Assert.assertEquals(6, solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void testBug1() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = VariableFactory.enumerated("a2", new int[]{5, 8}, solver);
        a[1] = VariableFactory.enumerated("a3", new int[]{-2, 0}, solver);

        b[0] = VariableFactory.enumerated("b2", new int[]{5, 8}, solver);
        b[1] = VariableFactory.enumerated("b3", new int[]{-3, -2}, solver);


        solver.post(IntConstraintFactory.lex_less(a, b));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
        Assert.assertEquals(5, a[0].getUB());
        SearchMonitorFactory.log(solver, true, true);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testBug2() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = VariableFactory.enumerated("a1", new int[]{-2, 5}, solver);
        a[1] = VariableFactory.enumerated("a2", new int[]{-1, 1}, solver);

        b[0] = VariableFactory.enumerated("b1", new int[]{3, 5}, solver);
        b[1] = VariableFactory.enumerated("b2", new int[]{-6, -1}, solver);


        solver.post(IntConstraintFactory.lex_less(a, b));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
        Assert.assertEquals(-2, a[0].getUB());
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 8);
    }

    @Test(groups = "1s")
    public void testBug3() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = VariableFactory.enumerated("a1", new int[]{5}, solver);
        a[1] = VariableFactory.enumerated("a2", new int[]{-1, 1}, solver);

        b[0] = VariableFactory.enumerated("b1", new int[]{3, 5}, solver);
        b[1] = VariableFactory.enumerated("b2", new int[]{-6, -1}, solver);


        solver.post(IntConstraintFactory.lex_less(a, b));
        try {
            solver.propagate();
            Assert.fail();
        } catch (ContradictionException e) {
        }
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 0);
    }

    @Test(groups = "1s")
    public void testBug4() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[5];
        IntVar[] b = new IntVar[5];

        a[0] = VariableFactory.enumerated("a1", new int[]{2}, solver);
        a[1] = VariableFactory.enumerated("a2", new int[]{1, 3, 4}, solver);
        a[2] = VariableFactory.enumerated("a3", new int[]{1, 2, 3, 4, 5}, solver);
        a[3] = VariableFactory.enumerated("a4", new int[]{1, 2}, solver);
        a[4] = VariableFactory.enumerated("a5", new int[]{3, 4, 5}, solver);

        b[0] = VariableFactory.enumerated("b1", new int[]{0, 1, 2}, solver);
        b[1] = VariableFactory.enumerated("b2", new int[]{1}, solver);
        b[2] = VariableFactory.enumerated("b3", new int[]{0, 1, 2, 3, 4}, solver);
        b[3] = VariableFactory.enumerated("b4", new int[]{0, 1}, solver);
        b[4] = VariableFactory.enumerated("b5", new int[]{0, 1, 2}, solver);


        solver.post(IntConstraintFactory.lex_less(a, b));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
        }
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 216);
    }

    @Test(groups = "1s")
    public void testBug5() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[3];
        IntVar[] b = new IntVar[3];

        a[0] = VariableFactory.enumerated("a1", new int[]{-10, -3, 2}, solver);
        a[1] = VariableFactory.enumerated("a2", new int[]{-5, -4, 2}, solver);
        a[2] = VariableFactory.enumerated("a3", new int[]{2}, solver);

        b[0] = VariableFactory.enumerated("b1", new int[]{-10, -1, 3}, solver);
        b[1] = VariableFactory.enumerated("b2", new int[]{-5}, solver);
        b[2] = VariableFactory.enumerated("b3", new int[]{-4, 2}, solver);


        solver.post(IntConstraintFactory.lex_less(a, b));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
        Assert.assertEquals(-1, b[0].getLB());
        SearchMonitorFactory.log(solver, true, false);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 30);
    }
}
