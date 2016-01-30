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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 12/06/12
 * Time: 21:29
 */

package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CircuitTest {

    @Test(groups = "1s", timeOut=1000)
    public static void test1() {
        Solver solver = new Solver();
        IntVar[] x = VariableFactory.boundedArray("x", 10, 0, 20, solver);
        solver.post(IntConstraintFactory.circuit(x, 0));
        solver.findSolution();
        Assert.assertEquals(1, solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s", timeOut=1000)
    public static void test2() {
        Solver solver = new Solver();
        IntVar[] x = VariableFactory.enumeratedArray("x", 10, 0, 10, solver);
        solver.post(IntConstraintFactory.circuit(x, 0));
        solver.findSolution();
        Assert.assertEquals(1, solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s", timeOut=1000)
    public static void test3() {
        Solver solver = new Solver();
        IntVar[] x = VariableFactory.boundedArray("x", 5, 0, 4, solver);
        IntVar[] y = VariableFactory.boundedArray("y", 5, 5, 9, solver);
        IntVar[] vars = ArrayUtils.append(x, y);
        solver.post(IntConstraintFactory.circuit(vars, 0));
        solver.findSolution();
        Assert.assertEquals(0, solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s", timeOut=1000)
    public static void test4() {
        for (int n = 2; n < 8; n++) {
            Solver solver = new Solver();
            IntVar[] x = VariableFactory.boundedArray("x", n, 0, n - 1, solver);
            solver.post(IntConstraintFactory.circuit(x, 0));
            solver.findAllSolutions();
            Assert.assertEquals(factorial(n - 1), solver.getMeasures().getSolutionCount());
        }
    }

    private static int factorial(int n) {
        if (n == 1) {
            return 1;
        } else {
            return n * factorial(n - 1);
        }
    }
}
