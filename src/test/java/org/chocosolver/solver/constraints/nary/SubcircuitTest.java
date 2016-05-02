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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SubcircuitTest {

    @Test(groups="1s", timeOut=60000)
    public static void test1() {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 10, 0, 20, true);
        model.subCircuit(x, 0, model.intVar("length", 0, x.length - 1, true)).post();
        model.solve();
        assertEquals(1, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public static void test2() {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 5, 0, 4, true);
        IntVar[] y = model.intVarArray("y", 5, 5, 9, true);
        IntVar[] vars = append(x, y);
        model.subCircuit(vars, 0, model.intVar("length", 0, vars.length - 1, true)).post();
        model.solve();
        assertTrue(model.getSolver().getSolutionCount() > 0);
    }

    @Test(groups="1s", timeOut=60000)
    public static void test3() {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 5, 0, 4, false);
        IntVar[] y = model.intVarArray("y", 5, 5, 9, false);
        final IntVar[] vars = append(x, y);
        try {
            vars[1].removeValue(1, Null);
            vars[6].removeValue(6, Null);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
        model.subCircuit(vars, 0, model.intVar("length", 0, vars.length - 1, true)).post();
        model.solve();
        assertTrue(model.getSolver().getSolutionCount() == 0);
    }

    @Test(groups="1s", timeOut=60000)
    public static void test4() {
        Model model = new Model();
        int n = 6;
        int min = 2;
        int max = 4;
        IntVar[] vars = model.intVarArray("x", n, 0, n, true);
        IntVar nb = model.intVar("size", min, max, true);
        model.subCircuit(vars, 0, nb).post();
        while (model.solve()) ;
        int nbSol = 0;
        for (int i = min; i <= max; i++) {
            nbSol += parmi(i, n) * factorial(i - 1);
        }
        assertEquals(model.getSolver().getSolutionCount(), nbSol);
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
