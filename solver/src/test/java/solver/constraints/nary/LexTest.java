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


import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.lex.Lex;
import solver.exception.ContradictionException;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;


/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 5 avr. 2006
 * Time: 08:42:43
 */
public class LexTest {


    @Test
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
            solver.post(new Lex(vs1, vs2, false, solver));
            solver.set(StrategyFactory.random(ArrayUtils.append(vs1, vs2), solver.getEnvironment(), seed));
            solver.findAllSolutions();
            int kpn = (int) Math.pow(k + 1, n1 / 2);
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), (kpn * (kpn + 1) / 2));
        }
    }

    @Test
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
            solver.post(new Lex(vs1, vs2, true, solver));
            solver.set(StrategyFactory.random(ArrayUtils.append(vs1, vs2), solver.getEnvironment(), seed));

            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 3240);
        }
    }

    @Test
    public void testLexiSatisfied() {
        Solver solver = new Solver();
        IntVar v1 = VariableFactory.bounded("v1", 1, 1, solver);
        IntVar v2 = VariableFactory.bounded("v2", 2, 2, solver);
        IntVar v3 = VariableFactory.bounded("v3", 3, 3, solver);
        Constraint c1 = new Lex(new IntVar[]{v1, v2}, new IntVar[]{v1, v3}, true, solver);
        Constraint c2 = new Lex(new IntVar[]{v1, v2}, new IntVar[]{v1, v2}, true, solver);
        Constraint c3 = new Lex(new IntVar[]{v1, v2}, new IntVar[]{v1, v1}, true, solver);
        Constraint c4 = new Lex(new IntVar[]{v1, v2}, new IntVar[]{v1, v3}, false, solver);
        Constraint c5 = new Lex(new IntVar[]{v1, v2}, new IntVar[]{v1, v2}, false, solver);
        Constraint c6 = new Lex(new IntVar[]{v1, v2}, new IntVar[]{v1, v1}, false, solver);
        solver.post(c1, c2, c3, c4, c5, c6);
        Assert.assertEquals(ESat.TRUE, c1.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c2.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c3.isSatisfied());
        Assert.assertEquals(ESat.TRUE, c4.isSatisfied());
        Assert.assertEquals(ESat.TRUE, c5.isSatisfied());
        Assert.assertEquals(ESat.FALSE, c6.isSatisfied());
    }


    @Test
    public void testAshish() {
        Solver solver = new Solver();
        IntVar[] a = new IntVar[2];
        IntVar[] b = new IntVar[2];

        a[0] = VariableFactory.bounded("a1", 5, 7, solver);
        a[1] = VariableFactory.bounded("a2", 1, 1, solver);

        b[0] = VariableFactory.bounded("b1", 5, 8, solver);
        b[1] = VariableFactory.bounded("b2", 0, 0, solver);


        solver.post(new Lex(a, b, true, solver));
        try {
            solver.propagate();

        } catch (ContradictionException e) {
            Assert.fail();
        }
        solver.findAllSolutions();
        Assert.assertEquals(6, solver.getMeasures().getSolutionCount());
    }
}
