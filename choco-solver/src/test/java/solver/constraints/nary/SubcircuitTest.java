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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 12/06/12
 * Time: 21:29
 */

package solver.constraints.nary;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.tools.ArrayUtils;

public class SubcircuitTest {

    @Test(groups = "1s")
    public static void test1() {
        Solver solver = new Solver();
        IntVar[] x = VariableFactory.boundedArray("x", 10, 0, 20, solver);
        solver.post(IntConstraintFactory.subcircuit(x, 0, VariableFactory.bounded("length", 0, x.length - 1, solver)));
        solver.findSolution();
        Assert.assertEquals(1, solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public static void test2() {
        Solver solver = new Solver();
        IntVar[] x = VariableFactory.boundedArray("x", 5, 0, 4, solver);
        IntVar[] y = VariableFactory.boundedArray("y", 5, 5, 9, solver);
        IntVar[] vars = ArrayUtils.append(x, y);
        solver.post(IntConstraintFactory.subcircuit(vars, 0, VariableFactory.bounded("length", 0, vars.length - 1, solver)));
        solver.findSolution();
        Assert.assertTrue(solver.getMeasures().getSolutionCount() > 0);
    }

    @Test(groups = "1s")
    public static void test3() {
        Solver solver = new Solver();
        IntVar[] x = VariableFactory.enumeratedArray("x", 5, 0, 4, solver);
        IntVar[] y = VariableFactory.enumeratedArray("y", 5, 5, 9, solver);
        final IntVar[] vars = ArrayUtils.append(x, y);
        try {
            vars[1].removeValue(1, Cause.Null);
            vars[6].removeValue(6, Cause.Null);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        solver.post(IntConstraintFactory.subcircuit(vars, 0, VariableFactory.bounded("length", 0, vars.length - 1, solver)));
        solver.findSolution();
        Assert.assertTrue(solver.getMeasures().getSolutionCount() == 0);
    }

    @Test(groups = "1s")
    public static void test4() {
        Solver solver = new Solver();
        int n = 6;
        int min = 2;
        int max = 4;
        IntVar[] vars = VariableFactory.boundedArray("x", n, 0, n, solver);
        IntVar nb = VariableFactory.bounded("size", min, max, solver);
        solver.post(IntConstraintFactory.subcircuit(vars, 0, nb));
        solver.findAllSolutions();
        int nbSol = 0;
        for (int i = min; i <= max; i++) {
            nbSol += parmi(i, n) * factorial(i - 1);
        }
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), nbSol);
    }

    private static int factorial(int n) {
        if (n <= 1) {
            return 1;
        } else {
            return n * factorial(n - 1);
        }
    }

    private static int parmi(int k, int n) {
        return factorial(n) / (factorial(k) * factorial(n - k));
    }
}
