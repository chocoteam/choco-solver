/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package org.chocosolver.solver.para;

import org.chocosolver.solver.MasterSolver;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 27/10/14
 */
public class MasterSolverTests {

    private Solver langford(int k, int n) {
        Solver solver = new Solver();
        IntVar[] p = VariableFactory.enumeratedArray("p", n * k, 0, k * n - 1, solver);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k - 1; j++) {
                solver.post(IntConstraintFactory.arithm(VariableFactory.offset(p[i + j * n], i + 2), "=", p[i + (j + 1) * n]));
            }
        }
        solver.post(IntConstraintFactory.arithm(p[0], "<", p[n * k - 1]));
        solver.post(IntConstraintFactory.alldifferent(p, "AC"));
        return solver;
    }

    private Solver golomb(int m) {
        Solver solver = new Solver();
        IntVar[] ticks = VariableFactory.enumeratedArray("a", m, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);

        solver.post(IntConstraintFactory.arithm(ticks[0], "=", 0));

        for (int i = 0; i < m - 1; i++) {
            solver.post(IntConstraintFactory.arithm(ticks[i + 1], ">", ticks[i]));
        }

        IntVar[] diffs = VariableFactory.enumeratedArray("d", (m * m - m) / 2, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);
        IntVar[][] m_diffs = new IntVar[m][m];
        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                solver.post(IntConstraintFactory.scalar(new IntVar[]{ticks[j], ticks[i]}, new int[]{1, -1}, diffs[k]));
                solver.post(IntConstraintFactory.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2));
                solver.post(IntConstraintFactory.arithm(diffs[k], "-", ticks[m - 1], "<=", -((m - 1 - j + i) * (m - j + i)) / 2));
                solver.post(IntConstraintFactory.arithm(diffs[k], "<=", ticks[m - 1], "-", ((m - 1 - j + i) * (m - j + i)) / 2));
                m_diffs[i][j] = diffs[k];
            }
        }
        solver.post(IntConstraintFactory.alldifferent(diffs, "BC"));
        // break symetries
        if (m > 2) {
            solver.post(IntConstraintFactory.arithm(diffs[0], "<", diffs[diffs.length - 1]));
        }
        return solver;
    }

    @Test(groups = "1s")
    public void testSatOneSolver() {
        Solver s0 = langford(3, 9);
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 0);
        ms.configureSearches();
        Assert.assertTrue(ms.findSolution());
    }

    @Test(groups = "1s")
    public void testSatTwoSolvers() {
        Solver s0 = langford(3, 9);
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 1);
        ms.configureSearches();
        Assert.assertTrue(ms.findSolution());
    }

    @Test(groups = "1s")
    public void testSatFourSolvers() {
        Solver s0 = langford(3, 9);
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 3);
        ms.configureSearches();
        Assert.assertTrue(ms.findSolution());
    }

    @Test(groups = "1s")
    public void testSatOneSolverNoSol() {
        Solver s0 = langford(3, 8);
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 0);
        ms.configureSearches();
        Assert.assertFalse(ms.findSolution());
    }

    @Test(groups = "1s")
    public void testSatTwoSolversNoSol() {
        Solver s0 = langford(3, 8);
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 1);
        ms.configureSearches();
        Assert.assertFalse(ms.findSolution());
    }

    @Test(groups = "1s")
    public void testSatFourSolversNoSol() {
        Solver s0 = langford(3, 8);
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 3);
        ms.configureSearches();
        Assert.assertFalse(ms.findSolution());
    }

    //
    @Test(groups = "1s")
    public void testOptOneSolver() {
        Solver s0 = golomb(10);
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 0);
        ms.configureSearches();
        ms.findOptimalSolution(ResolutionPolicy.MINIMIZE, (IntVar) s0.getVars()[9]);
        Assert.assertEquals(ESat.TRUE, ms.isFeasible());
        Assert.assertEquals(34, s0.getObjectiveManager().getBestSolutionValue());
    }

    @Test(groups = "1s")
    public void testOptTwoSolvers() {
        Solver s0 = golomb(10);
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 1);
        ms.configureSearches();
        ms.findOptimalSolution(ResolutionPolicy.MINIMIZE, (IntVar) s0.getVars()[9]);
        Assert.assertEquals(ESat.TRUE, ms.isFeasible());
        Assert.assertEquals(34, s0.getObjectiveManager().getBestSolutionValue());
    }

    @Test(groups = "1s")
    public void testOptFourSolvers() {
        Solver s0 = golomb(10);
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 3);
        ms.configureSearches();
        ms.findOptimalSolution(ResolutionPolicy.MINIMIZE, (IntVar) s0.getVars()[9]);
        Assert.assertEquals(ESat.TRUE, ms.isFeasible());
        Assert.assertEquals(34, s0.getObjectiveManager().getBestSolutionValue());
    }

    @Test(groups = "1s")
    public void testOptOneSolverNoSol() {
        Solver s0 = golomb(10);
        s0.post(ICF.arithm((IntVar) s0.getVars()[9], "=", (IntVar) s0.getVars()[0]));
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 0);
        ms.configureSearches();
        ms.findOptimalSolution(ResolutionPolicy.MINIMIZE, (IntVar) s0.getVars()[9]);
        Assert.assertEquals(ESat.FALSE, ms.isFeasible());
    }

    @Test(groups = "1s")
    public void testOptTwoSolversNoSol() {
        Solver s0 = golomb(10);
        s0.post(ICF.arithm((IntVar) s0.getVars()[9], "=", (IntVar) s0.getVars()[0]));
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 1);
        ms.configureSearches();
        ms.findOptimalSolution(ResolutionPolicy.MINIMIZE, (IntVar) s0.getVars()[9]);
        Assert.assertEquals(ESat.FALSE, ms.isFeasible());
    }

    @Test(groups = "1s")
    public void testOptFourSolversNoSol() {
        Solver s0 = golomb(10);
        s0.post(ICF.arithm((IntVar) s0.getVars()[9], "=", (IntVar) s0.getVars()[0]));
        MasterSolver ms = new MasterSolver();
        ms.populate(s0, 3);
        ms.configureSearches();
        ms.findOptimalSolution(ResolutionPolicy.MINIMIZE, (IntVar) s0.getVars()[9]);
        Assert.assertEquals(ESat.FALSE, ms.isFeasible());
    }

}
