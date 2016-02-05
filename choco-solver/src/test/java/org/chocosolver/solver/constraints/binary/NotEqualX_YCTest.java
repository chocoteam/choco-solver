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
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.lexico_LB;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * User: cprudhom
 * Mail: cprudhom(a)emn.fr
 * Date: 15 juin 2010
 * Since: Choco 2.1.1
 */
public class NotEqualX_YCTest {
    @Test(groups="1s", timeOut=60000)
    public void test1() {
        int n = 2;

        Solver s = new Solver();

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("v_" + i, 0, n, false);
        }
        s.arithm(vars[0], "!=", vars[1]).post();

        s.set(lexico_LB(vars));
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        assertEquals(sol, 6, "nb sol incorrect");

    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        int n = 2;

        Solver s = new Solver();

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("v_" + i, 0, n, true);
        }
        s.arithm(vars[0], "!=", vars[1]).post();
        s.set(lexico_LB(vars));
//        ChocoLogging.toSolution();
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        assertEquals(sol, 6, "nb sol incorrect");
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        int n = 2;

        Solver s = new Solver();

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("v_" + i, 0, n, true);
        }
        s.arithm(vars[0], "!=", vars[1]).post();
        s.set(lexico_LB(vars));

        try {
            s.propagate();
            vars[0].instantiateTo(1, Null);
            s.propagate();
            assertEquals(vars[1].getLB(), 0);
            assertEquals(vars[1].getUB(), 2);
            vars[1].removeValue(2, Null);
            s.propagate();
            assertEquals(vars[1].getLB(), 0);
            assertEquals(vars[1].getUB(), 0);
        } catch (ContradictionException e) {
            fail();
        }
    }

}
