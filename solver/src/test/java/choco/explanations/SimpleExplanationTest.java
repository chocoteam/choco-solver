/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package choco.explanations;

import choco.kernel.memory.IEnvironment;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

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
     * @param enumerated
     */
    public static void test(boolean enumerated) {
        // initialize
        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();
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
        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
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

//	static Solver s;
//    static IEnvironment env;
//    static IntVar[] vars;
//    static Constraint[] lcstrs;
//
//
//    public static void init() {
//        s = new Solver();
//        env = s.getEnvironment();
//
//    }
//
//    public static void setvars(boolean enumerated) {
//        vars = new IntVar[3];
//        for (int i = 0; i < vars.length; i++) {
//            vars[i] = enumerated ? VariableFactory.enumerated("x" + i, 1, vars.length, s)
//                    : VariableFactory.bounded("x" + i, 1, vars.length + 1, s);
//        }
//
//    }
//
//    public static void constraints() {
//
//        lcstrs = new Constraint[3];
//
//        lcstrs[0] = ConstraintFactory.lt(vars[0], vars[1], s);
//        lcstrs[1] = ConstraintFactory.lt(vars[1], vars[2], s);
//        lcstrs[2] = ConstraintFactory.neq(vars[0], vars[1], s);
//    }
//
//    private static void solve() {
//
//        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
//
//        s.post(lcstrs);
//        s.set(strategy);
//
//        s.findSolution();
//
//
//        long sol = s.getMeasures().getSolutionCount();
//        Assert.assertEquals(sol, 1, "nb sol incorrect");
//
//    }
//
//
//    @Test(groups = "1s")
//    public void test1() {
//        init();
//        setvars(true);
//        constraints();
//        solve();
//    }
//
//
//    @Test(groups = "1s")
//    public void test2() {
//        init();
//        setvars(false);
//        constraints();
//        solve();
//    }


}
