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
package org.chocosolver.solver.search.restart;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.Random;

import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Jean-Guillaume Fages
 * @since 17/12/14
 * Created by IntelliJ IDEA.
 */
public class NoGoodOnSolutionTest {

    final static int N = 15;
    final static int Z = 175;
    final static int NB_SOLS = 5;
    final static int MAX_NB_SOLS = 10;

    public static Model makeProblem() {
        int n = N;
        Random rd = new Random(0);
        int[][] costs = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                costs[i][j] = rd.nextInt(100);
            }
        }
        Model s = new Model();
        IntVar z = s.intVar("z", Z, Z + 10, true);
        IntVar[] vars = s.intVarArray("x", n, 0, n - 1, false);
        IntVar[] costOf = new IntVar[n];
        for (int i = 0; i < n; i++) {
            costOf[i] = s.intVar("costOf(" + i + ")", costs[i]);
        }
        for (int i = 0; i < n; i++) {
            s.element(costOf[i], costs[i], vars[i]).post();
        }
        s.sum(costOf, "=", z).post();
        s.circuit(vars).post();
        s.getSolver().setSearch(randomSearch(vars, 0));
        s.getSolver().limitSolution(MAX_NB_SOLS);
        return s;
    }

    @Test(groups="1s", timeOut=60000)
    public void testNormal() {
        // no restarts (ok)
        Model s = makeProblem();
        while (s.getSolver().solve()) ;
        out.println(s.getSolver().getMeasures());
        assertTrue(s.getSolver().getSolutionCount() == NB_SOLS);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRoS() {
        // restarts on solutions (infinite loop)
        Model s = makeProblem();
        s.getSolver().setRestartOnSolutions();
        while (s.getSolver().solve()) ;
        out.println(s.getSolver().getMeasures());
        assertTrue(s.getSolver().getSolutionCount() == MAX_NB_SOLS);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRoSNG() {
        // restarts on solutions with no goods on solutions (ok)
        Model s = makeProblem();
        s.getSolver().setNoGoodRecordingFromSolutions(s.retrieveIntVars(true));
        s.getSolver().setRestartOnSolutions();
        while (s.getSolver().solve()) ;
        out.println(s.getSolver().getMeasures());
        assertTrue(s.getSolver().getSolutionCount() == NB_SOLS);
    }

    @Test(groups="1s", timeOut=60000)
    public void testA() {
        // restarts on solutions and on fails (at activity presolve only) (loop infinitely)
        Model s = makeProblem();
        Solver r = s.getSolver();
        r.setSearch(activityBasedSearch(s.retrieveIntVars(true)));
        while (s.getSolver().solve()) ;
        out.println(r.getMeasures());
        assertTrue(r.getMeasures().getSolutionCount() == MAX_NB_SOLS);
    }

    @Test(groups="10s", timeOut=60000)
    public void testANG() {
        // restarts on solutions and on fails with restarts on solutions (ok)
        Model s = makeProblem();
        Solver r = s.getSolver();
        r.setNoGoodRecordingFromSolutions(s.retrieveIntVars(true));
        r.setSearch(activityBasedSearch(s.retrieveIntVars(true)));
        s.getSolver().showSolutions();
        while (s.getSolver().solve()) ;
        out.println(r.getMeasures());
        assertEquals(r.getMeasures().getSolutionCount(), NB_SOLS);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNQ() {
        // restarts on solutions and on fails with restarts on solutions (ok)
        Model model = new Model();
        int n = 8;
        IntVar[] vars = model.intVarArray("Q", n, 1, n, false);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = model.arithm(vars[i], "!=", vars[j]);
                neq.post();
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        model.getSolver().setNoGoodRecordingFromSolutions(model.retrieveIntVars(true));
        model.getSolver().setSearch(randomSearch(vars, 0));

        model.getSolver().setRestartOnSolutions();
        while (model.getSolver().solve()) ;
        out.println(model.getSolver().getMeasures());
        assertTrue(model.getSolver().getSolutionCount() == 92);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNQ2() {
        // restarts on solutions and on fails with restarts on solutions (ok)
        Model model = new Model();
        int n = 8;
        IntVar[] vars = model.intVarArray("Q", n, 1, n, false);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = model.arithm(vars[i], "!=", vars[j]);
                neq.post();
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        model.getSolver().setNoGoodRecordingFromSolutions(model.retrieveIntVars(false));
        model.getSolver().setNoGoodRecordingFromRestarts();
        model.getSolver().setSearch(randomSearch(vars, 0));
        model.getSolver().setRestartOnSolutions();
        while (model.getSolver().solve()) ;
        out.println(model.getSolver().getMeasures());
        assertEquals(model.getSolver().getSolutionCount(), 92);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNQ3() { //issue 327
        // restarts on solutions and on fails with restarts on solutions (ok)
        Model model = new Model();
        int n = 8;
        IntVar[] vars = model.intVarArray("Q", n, 1, n, false);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = model.arithm(vars[i], "!=", vars[j]);
                neq.post();
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        model.getSolver().setNoGoodRecordingFromSolutions(vars[0]);
        model.getSolver().showSolutions();
        model.getSolver().setSearch(inputOrderLBSearch(vars));
        while (model.getSolver().solve()) ;
        out.println(model.getSolver().getMeasures());
        assertEquals(model.getSolver().getSolutionCount(), 8);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNQ4() { //issue 327
        // restarts on solutions and on fails with restarts on solutions (ok)
        Model model = new Model();
        int n = 8;
        IntVar[] vars = model.intVarArray("Q", n, 1, n, false);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = model.arithm(vars[i], "!=", vars[j]);
                neq.post();
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        model.getSolver().setNoGoodRecordingFromSolutions(vars[0], vars[1]);
        model.getSolver().showSolutions();
        model.getSolver().setSearch(inputOrderLBSearch(vars));
//        IOutputFactory.showDecisions(solver);
        while (model.getSolver().solve()) ;
        out.println(model.getSolver().getMeasures());
        assertEquals(model.getSolver().getSolutionCount(), 36);
    }
}
