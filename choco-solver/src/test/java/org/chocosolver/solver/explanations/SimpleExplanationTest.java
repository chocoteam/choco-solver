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
package org.chocosolver.solver.explanations;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 30 oct. 2010
 * Time: 17:09:26
 */
public class SimpleExplanationTest {

    /**
     * Refactored by JG to have no static fields (for parallel execution)
     *
     * @param enumerated true -> enumerated domains
     */
    public static void test(boolean enumerated) {
        // initialize
        Solver s = new Solver();
        // set varriables
        IntVar[] vars = new IntVar[3];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = enumerated ? VariableFactory.enumerated("x" + i, 1, vars.length, s)
                    : VariableFactory.bounded("x" + i, 1, vars.length + 1, s);
        }
        // post constraints
        Constraint[] lcstrs = new Constraint[3];
        lcstrs[0] = IntConstraintFactory.arithm(vars[0], "<", vars[1]);
        lcstrs[1] = IntConstraintFactory.arithm(vars[1], "<", vars[2]);
        lcstrs[2] = IntConstraintFactory.arithm(vars[0], "!=", vars[1]);
        // configure Solver
        AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
        s.post(lcstrs);
        s.set(strategy);
        // solve
        s.findSolution();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 1, "nb sol incorrect");
    }


    @Test(groups = "1s")
    public void test1() {
        test(true);
    }

    @Test(groups = "1s")
    public void test2() {
        test(false);
    }
}
