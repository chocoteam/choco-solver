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
package org.chocosolver.solver;

import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.IntConstraintFactory.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 27/10/14
 */
public class PortfolioTests {

    private ParallelPortfolio langford(int k, int n, int t) {
        ParallelPortfolio prtfl = SolverFactory.makeParallelPortfolio("LF", t);
        IntVar[] p = VariableFactory.enumeratedArray("p", n * k, 0, k * n - 1, prtfl);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k - 1; j++) {
                prtfl.post(IntConstraintFactory.arithm(VariableFactory.offset(p[i + j * n], i + 2), "=", p[i + (j + 1) * n]));
            }
        }
        prtfl.post(IntConstraintFactory.arithm(p[0], "<", p[n * k - 1]));
        prtfl.post(IntConstraintFactory.alldifferent(p, "AC"));
        return prtfl;
    }

    private ParallelPortfolio golomb(int m, int t) {
        ParallelPortfolio prtfl = SolverFactory.makeParallelPortfolio("GR", t);
        IntVar[] ticks = VariableFactory.enumeratedArray("a", m, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), prtfl);

        prtfl.post(IntConstraintFactory.arithm(ticks[0], "=", 0));

        for (int i = 0; i < m - 1; i++) {
            prtfl.post(IntConstraintFactory.arithm(ticks[i + 1], ">", ticks[i]));
        }

        IntVar[] diffs = VariableFactory.enumeratedArray("d", (m * m - m) / 2, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), prtfl);
        IntVar[][] m_diffs = new IntVar[m][m];
        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                prtfl.post(IntConstraintFactory.scalar(new IntVar[]{ticks[j], ticks[i]}, new int[]{1, -1}, diffs[k]));
                prtfl.post(IntConstraintFactory.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2));
                prtfl.post(IntConstraintFactory.arithm(diffs[k], "-", ticks[m - 1], "<=", -((m - 1 - j + i) * (m - j + i)) / 2));
                prtfl.post(IntConstraintFactory.arithm(diffs[k], "<=", ticks[m - 1], "-", ((m - 1 - j + i) * (m - j + i)) / 2));
                m_diffs[i][j] = diffs[k];
            }
        }
        prtfl.post(IntConstraintFactory.alldifferent(diffs, "BC"));
        // break symetries
        if (m > 2) {
            prtfl.post(IntConstraintFactory.arithm(diffs[0], "<", diffs[diffs.length - 1]));
        }
        prtfl._fes_().set(IntStrategyFactory.lexico_LB(ticks));
        for (Solver s : prtfl.workers) {
            Chatterbox.showSolutions(s);
        }
        return prtfl;
    }

    @Test(groups = "1s", expectedExceptions = SolverException.class)
    public void testSatOneSolver() {
        ParallelPortfolio s0 = langford(3, 9, 1);
        Assert.assertTrue(s0.findSolution());
    }

    @Test(groups = "1s")
    public void testSatTwoSolvers() {
        ParallelPortfolio s0 = langford(3, 9, 2);
        Assert.assertTrue(s0.findSolution());
    }

    @Test(groups = "1s")
    public void testSatFourSolvers() {
        ParallelPortfolio s0 = langford(3, 9, 4);
        Assert.assertTrue(s0.findSolution());
    }

    @Test(groups = "1s")
    public void testSatTwoSolversNoSol() {
        ParallelPortfolio s0 = langford(3, 8, 2);
        Assert.assertFalse(s0.findSolution());
    }

    @Test(groups = "1s")
    public void testSatFourSolversNoSol() {
        ParallelPortfolio s0 = langford(3, 8, 4);
        Assert.assertFalse(s0.findSolution());
    }

    @Test(groups = "1s")
    public void testOptTwoSolvers() {
        ParallelPortfolio s0 = golomb(10, 2);
        IntVar obj = (IntVar) s0._fes_().getVars()[9];
        s0.findOptimalSolution(ResolutionPolicy.MINIMIZE, obj);
        Assert.assertEquals(ESat.TRUE, s0.isFeasible());
        Assert.assertEquals((int) s0.getSolutionRecorder().getLastSolution().getIntVal(obj), 55, "");
    }

    @Test(groups = "1s")
    public void testOptFourSolvers() {
        ParallelPortfolio s0 = golomb(10, 4);
        IntVar obj = (IntVar) s0._fes_().getVars()[9];
        s0.findOptimalSolution(ResolutionPolicy.MINIMIZE, obj);
        Assert.assertEquals(ESat.TRUE, s0.isFeasible());
        Assert.assertEquals((int) s0.getSolutionRecorder().getLastSolution().getIntVal(obj), 55, "");
    }

    @Test(groups = "1s")
    public void testOptTwoSolversNoSol() {
        ParallelPortfolio s0 = golomb(10, 2);
        s0.post(ICF.arithm((IntVar) s0._fes_().getVars()[9], "=", (IntVar) s0._fes_().getVars()[0]));
        s0.findOptimalSolution(ResolutionPolicy.MINIMIZE, (IntVar) s0._fes_().getVars()[9]);
        Assert.assertEquals(ESat.FALSE, s0.isFeasible());
    }

    @Test(groups = "1s")
    public void testOptFourSolversNoSol() {
        ParallelPortfolio s0 = golomb(10, 4);
        s0.post(ICF.arithm((IntVar) s0._fes_().getVars()[9], "=", (IntVar) s0._fes_().getVars()[0]));
        s0.findOptimalSolution(ResolutionPolicy.MINIMIZE, (IntVar) s0._fes_().getVars()[9]);
        Assert.assertEquals(ESat.FALSE, s0.isFeasible());
    }

    @Test(groups = "1s")
    public void test1() {
        ParallelPortfolio prtfl = SolverFactory.makeParallelPortfolio("test", 4);
        BoolVar bool = VF.bool("b", prtfl);
        IntVar bound = VF.bounded("bounded", 0, 10, prtfl);
        IntVar enuma = VF.enumerated("enum", 0, 10, prtfl);
        prtfl.carbonCopy();
        Solver[] solvers = prtfl.workers;
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(solvers[i].getNbVars(), 3);
        }
    }

    @Test(groups = "1s")
    public void test2() {
        ParallelPortfolio prtfl = SolverFactory.makeParallelPortfolio("test", 4);
        BoolVar a = VF.bool("a", prtfl);
        BoolVar b = VF.bool("b", prtfl);
        BoolVar c = VF.bool("c", prtfl);
        prtfl.carbonCopy();
        Solver[] solvers = prtfl.workers;
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(solvers[i].getNbVars(), 3);
        }
        SatFactory.addBoolAndEqVar(a, b, c);
        prtfl.carbonCopy();
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(solvers[i].getNbCstrs(), 1);
        }
        SatFactory.addBoolIsLeVar(a, b, c);
        prtfl.carbonCopy();
        long ns = solvers[0].findAllSolutions();
        for (int i = 1; i < 4; i++) {
            Assert.assertEquals(solvers[i].findAllSolutions(), ns);
        }
    }

    private ParallelPortfolio GR(int m) {
        int n = 4;
        ParallelPortfolio prtfl = SolverFactory.makeParallelPortfolio("test", n);
        IntVar[] ticks = VariableFactory.enumeratedArray("a", m, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), prtfl);

        prtfl.post(arithm(ticks[0], "=", 0));

        for (int i = 0; i < m - 1; i++) {
            prtfl.post(arithm(ticks[i + 1], ">", ticks[i]));
        }

        IntVar[] diffs = VariableFactory.enumeratedArray("d", (m * m - m) / 2, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), prtfl);
        IntVar[][] m_diffs = new IntVar[m][m];
        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                // d[k] is m[j]-m[i] and must be at least sum of first j-i integers
                prtfl.post(scalar(new IntVar[]{ticks[j], ticks[i]}, new int[]{1, -1}, diffs[k]));
                prtfl.post(arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2));
                prtfl.post(arithm(diffs[k], "-", ticks[m - 1], "<=", -((m - 1 - j + i) * (m - j + i)) / 2));
                prtfl.post(arithm(diffs[k], "<=", ticks[m - 1], "-", ((m - 1 - j + i) * (m - j + i)) / 2));
                m_diffs[i][j] = diffs[k];
            }
        }
        prtfl.post(alldifferent(diffs, "FC"));

        // break symetries
        if (m > 2) {
            prtfl.post(arithm(diffs[0], "<", diffs[diffs.length - 1]));
        }
        return prtfl;
    }

    @Test(groups = "1s")
    public void test3() {
        ParallelPortfolio prtfl = GR(3);
        prtfl.findSolution();
        int count = 1;
        while (count < 10 && prtfl.nextSolution()) {
            count++;
        }
    }

    @Test(groups = "1s", expectedExceptions = SolverException.class)
    public void test4() {
        ParallelPortfolio prtfl = GR(3);
        prtfl.findAllSolutions();
    }

    @Test(groups = "1s")
    public void test5() {
        int m = 9;
        ParallelPortfolio prtfl = GR(m);
        IntVar obj = prtfl._fes_().retrieveIntVars()[m - 1];
        prtfl.findOptimalSolution(ResolutionPolicy.MINIMIZE, obj);
        Assert.assertEquals((int)prtfl.getSolutionRecorder().getLastSolution().getIntVal(obj), 44, "");
    }

}
