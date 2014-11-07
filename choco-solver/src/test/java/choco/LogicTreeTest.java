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

package choco;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.constraints.SatFactory;
import solver.constraints.nary.cnf.ILogical;
import solver.constraints.nary.cnf.LogOp;
import solver.constraints.nary.cnf.LogicTreeToolBox;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.BoolVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 nov. 2010
 */
public class LogicTreeTest {

    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver();

        BoolVar a = VariableFactory.bool("a", solver);
        BoolVar b = VariableFactory.bool("b", solver);
        BoolVar c = VariableFactory.bool("c", solver);
        BoolVar d = VariableFactory.bool("d", solver);


        LogOp root = LogOp.nand(LogOp.nor(a, b), LogOp.or(c, d));

        ILogical l = LogicTreeToolBox.toCNF(root, solver);

        Assert.assertEquals(l.toString(), "((b or a or not(c)) and (b or a or not(d)))");
    }

    @Test(groups = "1s")
    public void test12() {
        Solver solver = new Solver();

        BoolVar a = VariableFactory.bool("a", solver);
        BoolVar b = VariableFactory.bool("b", solver);
        BoolVar c = VariableFactory.bool("c", solver);
        BoolVar d = VariableFactory.bool("d", solver);
        BoolVar e = VariableFactory.bool("e", solver);


        LogOp root = LogOp.and(LogOp.nand(LogOp.nor(a, b), LogOp.or(c, d)), e);

        ILogical l = LogicTreeToolBox.toCNF(root, solver);

        Assert.assertEquals(l.toString(), "(e and (b or a or not(c)) and (b or a or not(d)))");
    }


    @Test(groups = "1s")
    public void test2() {
        Solver solver = new Solver();

        BoolVar a = VariableFactory.bool("a", solver).not();
        BoolVar b = VariableFactory.bool("b", solver);
        BoolVar c = VariableFactory.bool("c", solver);
        BoolVar d = VariableFactory.bool("d", solver);

        LogOp root = LogOp.or(LogOp.or(LogOp.or(a, b), c), d);

        LogicTreeToolBox.merge(LogOp.Operator.OR, root);

        Assert.assertEquals(root.toString(), "(d or c or not(a) or b)");
    }

    @Test(groups = "1s")
    public void test3() {
        Solver solver = new Solver();

        BoolVar a = VariableFactory.bool("a", solver);
        BoolVar b = VariableFactory.bool("b", solver);
        BoolVar c = VariableFactory.bool("c", solver);

        LogOp root = LogOp.or(LogOp.and(a, b), c);
        root = LogicTreeToolBox.developOr(root);
        Assert.assertEquals(root.toString(), "((a or c) and (b or c))");
    }


    @Test(groups = "1s")
    public void test4() {
        Solver solver = new Solver();

        BoolVar a = VariableFactory.bool("a", solver).not();
        BoolVar b = VariableFactory.bool("b", solver);
        BoolVar c = VariableFactory.bool("c", solver);
        BoolVar d = VariableFactory.bool("d", solver);

        LogOp root = LogOp.nor(LogOp.or(LogOp.nand(a, b), c), d);

        LogicTreeToolBox.expandNot(root);

        Assert.assertEquals(root.toString(), "(((not(a) and b) and not(c)) and not(d))");
    }


    @Test(groups = "1s")
    public void test5() {
        Solver solver = new Solver();

        BoolVar a = VariableFactory.bool("a", solver).not();
        BoolVar b = VariableFactory.bool("b", solver);
        BoolVar c = VariableFactory.bool("c", solver);
        BoolVar d = VariableFactory.bool("d", solver);

        LogOp root = LogOp.and(LogOp.and(LogOp.and(a, b), c), d);

        LogicTreeToolBox.merge(LogOp.Operator.AND, root);

        Assert.assertEquals(root.toString(), "(d and c and not(a) and b)");
    }


    @Test(groups = "1s")
    public void test6() {
        Solver solver = new Solver();

        BoolVar a = VariableFactory.bool("a", solver);
        BoolVar b = VariableFactory.bool("b", solver);

        LogOp root = LogOp.implies(a, b);

        ILogical l = LogicTreeToolBox.toCNF(root, solver);

        Assert.assertEquals(l.toString(), "(b or not(a))");
    }

    @Test(groups = "1s")
    public void test7() {
        Solver solver = new Solver();

        BoolVar a = VariableFactory.bool("a", solver);
        BoolVar b = VariableFactory.bool("b", solver);
        BoolVar c = VariableFactory.bool("c", solver);


        LogOp root = LogOp.ifThenElse(a, b, c);

        ILogical l = LogicTreeToolBox.toCNF(root, solver);

        Assert.assertEquals(l.toString(), "((a or c) and (b or not(a)) and (b or c))");
    }

    @Test(groups = "1s")
    public void test8() {
        Solver solver = new Solver();

        BoolVar a = VariableFactory.bool("a", solver);
        BoolVar na = a.not();
        BoolVar b = VariableFactory.bool("b", solver);
        BoolVar nb = b.not();
        BoolVar c = VariableFactory.bool("c", solver);
        BoolVar d = VariableFactory.bool("d", solver);

        LogOp root = LogOp.and(LogOp.or(a, b, na), LogOp.or(c, d), LogOp.or(b, nb));

        ILogical l = LogicTreeToolBox.toCNF(root, solver);

        Assert.assertEquals(l.toString(), "(c or d)");
    }

    @Test(groups = "1s")
    public void test9() {
        Solver solver = new Solver();

        BoolVar a = VariableFactory.bool("a", solver);
        BoolVar na = a.not();
        BoolVar b = VariableFactory.bool("b", solver);
        BoolVar c = VariableFactory.bool("c", solver);
        BoolVar d = VariableFactory.bool("d", solver);

        LogOp root = LogOp.and(a, b, na, c, d);

        ILogical l = LogicTreeToolBox.toCNF(root, solver);

        Assert.assertEquals(l.toString(), "cste -- 0");
    }

    @Test(groups = "1s")
    public void test10() {

        Solver solver = new Solver();
        BoolVar[] rows = VariableFactory.boolArray("b", 3, solver);

        LogicalConstraintFactory.ifThen(
						rows[0],
						IntConstraintFactory.arithm(rows[1], "+", rows[2], "=", 2));
        LogicalConstraintFactory.ifThen(
						VariableFactory.not(rows[0]),
						IntConstraintFactory.arithm(rows[1], "+", rows[2], "<=", 1));
        //SearchMonitorFactory.log(solver, true, true);
        solver.findAllSolutions();
        long nbSol = solver.getMeasures().getSolutionCount();

        for (int seed = 0; seed < 2000; seed++) {
            Solver sCNF = new Solver();
            BoolVar[] rCNF = VariableFactory.boolArray("b", 3, sCNF);
            LogOp tree = LogOp.ifOnlyIf(
                    rCNF[0],
                    LogOp.and(rCNF[1], rCNF[2])
            );
            SatFactory.addClauses(tree, sCNF);
            sCNF.set(IntStrategyFactory.random_bound(rCNF, seed));

//            SearchMonitorFactory.log(sCNF, true, true);
            sCNF.findAllSolutions();
            Assert.assertEquals(sCNF.getMeasures().getSolutionCount(), nbSol);
        }
    }

}
