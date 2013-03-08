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

package choco;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.constraints.nary.cnf.ALogicTree;
import solver.constraints.nary.cnf.Literal;
import solver.constraints.nary.cnf.LogicTreeToolBox;
import solver.constraints.nary.cnf.Node;
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

        Literal a = Literal.pos(VariableFactory.bool("a", solver));
        Literal b = Literal.pos(VariableFactory.bool("b", solver));
        Literal c = Literal.pos(VariableFactory.bool("c", solver));
        Literal d = Literal.pos(VariableFactory.bool("d", solver));


        ALogicTree root = Node.nand(Node.nor(a, b), Node.or(c, d));

        root = LogicTreeToolBox.toCNF(root);

        Assert.assertEquals(root.toString(), "((b or a or not c) and (b or a or not d))");
    }

    @Test(groups = "1s")
    public void test12() {
        Solver solver = new Solver();

        Literal a = Literal.pos(VariableFactory.bool("a", solver));
        Literal b = Literal.pos(VariableFactory.bool("b", solver));
        Literal c = Literal.pos(VariableFactory.bool("c", solver));
        Literal d = Literal.pos(VariableFactory.bool("d", solver));
        Literal e = Literal.pos(VariableFactory.bool("e", solver));


        ALogicTree root = Node.and(Node.nand(Node.nor(a, b), Node.or(c, d)), e);

        root = LogicTreeToolBox.toCNF(root);

        Assert.assertEquals(root.toString(), "(e and (b or a or not c) and (b or a or not d))");
    }


    @Test(groups = "1s")
    public void test2() {
        Solver solver = new Solver();

        Literal a = Literal.neg(VariableFactory.bool("a", solver));
        Literal b = Literal.pos(VariableFactory.bool("b", solver));
        Literal c = Literal.pos(VariableFactory.bool("c", solver));
        Literal d = Literal.pos(VariableFactory.bool("d", solver));

        ALogicTree root = Node.or(Node.or(Node.or(a, b), c), d);

        LogicTreeToolBox.merge(ALogicTree.Operator.OR, root);

        Assert.assertEquals(root.toString(), "(d or c or not a or b)");
    }

    @Test(groups = "1s")
    public void test3() {
        Solver solver = new Solver();

        Literal a = Literal.pos(VariableFactory.bool("a", solver));
        Literal b = Literal.pos(VariableFactory.bool("b", solver));
        Literal c = Literal.pos(VariableFactory.bool("c", solver));

        ALogicTree root = Node.or(Node.and(a, b), c);
        root = LogicTreeToolBox.developOr(root);
        Assert.assertEquals(root.toString(), "((a or c) and (b or c))");
    }


    @Test(groups = "1s")
    public void test4() {
        Solver solver = new Solver();

        Literal a = Literal.neg(VariableFactory.bool("a", solver));
        Literal b = Literal.pos(VariableFactory.bool("b", solver));
        Literal c = Literal.pos(VariableFactory.bool("c", solver));
        Literal d = Literal.pos(VariableFactory.bool("d", solver));

        ALogicTree root = Node.nor(Node.or(Node.nand(a, b), c), d);

        LogicTreeToolBox.expandNot(root);

        Assert.assertEquals(root.toString(), "(((not a and b) and not c) and not d)");
    }


    @Test(groups = "1s")
    public void test5() {
        Solver solver = new Solver();

        Literal a = Literal.neg(VariableFactory.bool("a", solver));
        Literal b = Literal.pos(VariableFactory.bool("b", solver));
        Literal c = Literal.pos(VariableFactory.bool("c", solver));
        Literal d = Literal.pos(VariableFactory.bool("d", solver));

        ALogicTree root = Node.and(Node.and(Node.and(a, b), c), d);

        LogicTreeToolBox.merge(ALogicTree.Operator.AND, root);

        Assert.assertEquals(root.toString(), "(d and c and not a and b)");
    }


    @Test(groups = "1s")
    public void test6() {
        Solver solver = new Solver();

        Literal a = Literal.pos(VariableFactory.bool("a", solver));
        Literal b = Literal.pos(VariableFactory.bool("b", solver));

        ALogicTree root = Node.implies(a, b);

        root = LogicTreeToolBox.toCNF(root);

        Assert.assertEquals(root.toString(), "(b or not a)");
    }

    @Test(groups = "1s")
    public void test7() {
        Solver solver = new Solver();

        Literal a = Literal.pos(VariableFactory.bool("a", solver));
        Literal b = Literal.pos(VariableFactory.bool("b", solver));
        Literal c = Literal.pos(VariableFactory.bool("c", solver));


        ALogicTree root = Node.ifThenElse(a, b, c);

        root = LogicTreeToolBox.toCNF(root);

        Assert.assertEquals(root.toString(), "((a or c) and (b or not a) and (b or c))");
    }

    @Test(groups = "1s")
    public void test8() {
        Solver solver = new Solver();

        Literal a = Literal.pos(VariableFactory.bool("a", solver));
        Literal na = Literal.neg(a.flattenBoolVar()[0]);
        Literal b = Literal.pos(VariableFactory.bool("b", solver));
        Literal nb = Literal.neg(b.flattenBoolVar()[0]);
        Literal c = Literal.pos(VariableFactory.bool("c", solver));
        Literal d = Literal.pos(VariableFactory.bool("d", solver));

        ALogicTree root = Node.and(Node.or(a, b, na), Node.or(c, d), Node.or(b, nb));

        root = LogicTreeToolBox.toCNF(root);

        Assert.assertEquals(root.toString(), "(c or d)");
    }

    @Test(groups = "1s")
    public void test9() {
        Solver solver = new Solver();

        Literal a = Literal.pos(VariableFactory.bool("a", solver));
        Literal na = Literal.neg(a.flattenBoolVar()[0]);
        Literal b = Literal.pos(VariableFactory.bool("b", solver));
        Literal c = Literal.pos(VariableFactory.bool("c", solver));
        Literal d = Literal.pos(VariableFactory.bool("d", solver));

        ALogicTree root = Node.and(a, b, na, c, d);

        root = LogicTreeToolBox.toCNF(root);

        Assert.assertEquals(root.toString(), "false");
    }

    @Test(groups = "1s")
    public void test10() {

        Solver solver = new Solver();
        BoolVar[] rows = VariableFactory.boolArray("b", 3, solver);

        solver.post(
                IntConstraintFactory.implies(
                        rows[0],
                        IntConstraintFactory.arithm(rows[1], "+", rows[2], "=", 2)));
        solver.post(
                IntConstraintFactory.implies(
                        VariableFactory.not(rows[0]),
                        IntConstraintFactory.arithm(rows[1], "+", rows[2], "<=", 1))
        );
        //SearchMonitorFactory.log(solver, true, true);
        solver.findAllSolutions();
        long nbSol = solver.getMeasures().getSolutionCount();

        for (int seed = 0; seed < 2000; seed++) {
            Solver sCNF = new Solver();
            BoolVar[] rCNF = VariableFactory.boolArray("b", 3, sCNF);
            ALogicTree tree = Node.ifOnlyIf(
                    Literal.pos(rCNF[0]),
                    Node.and(Literal.pos(rCNF[1]), Literal.pos(rCNF[2]))
            );
            sCNF.post(IntConstraintFactory.clauses(tree, sCNF));
            sCNF.set(IntStrategyFactory.random(rCNF, seed));

            //SearchMonitorFactory.log(sCNF, true, true);
            sCNF.findAllSolutions();
            Assert.assertEquals(sCNF.getMeasures().getSolutionCount(), nbSol);
        }
    }

}
