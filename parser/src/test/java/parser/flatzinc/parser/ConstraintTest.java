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

package parser.flatzinc.parser;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Arithmetic;
import solver.constraints.nary.Sum;
import solver.propagation.hardcoded.ConstraintEngine;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
* 
*/
public class ConstraintTest {

    FZNParser fzn;
    Solver solver;

    @BeforeMethod
    public void before() {
        fzn = new FZNParser();
        solver = fzn.solver;
    }

    private void preset() {
        solver.set(StrategyFactory.forceInputOrderMinVal(solver.getVars(), solver.getEnvironment()));
    }

    @Test
    public void testIntNe() {
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 10: a::output_var;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 10: b::output_var;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_ne(a, b);");

        Assert.assertEquals(solver.getCstrs().length, 1);
        Assert.assertEquals(solver.getCstrs()[0].getClass(), Arithmetic.class);
        preset();
        solver.findAllSolutions();
        Assert.assertEquals(90, solver.getMeasures().getSolutionCount());
    }

    @Test
    public void testIntLinEq() {
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 26: a::output_var;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 26: b::output_var;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lin_eq([ 1, -1 ], [ a, b ], -1);");

        Assert.assertEquals(solver.getCstrs().length, 1);
        Assert.assertEquals(solver.getCstrs()[0].getClass(), Sum.class);
        preset();
        solver.findAllSolutions();
        Assert.assertEquals(25, solver.getMeasures().getSolutionCount());
    }

    @Test
    public void testIntLinEq2() {
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array[1 .. 2] of var 1 .. 2: q;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lin_eq([ 1, -1 ], [ q[1], q[2] ], -1);");

        Assert.assertEquals(solver.getCstrs().length, 1);
        Assert.assertEquals(solver.getCstrs()[0].getClass(), Sum.class);
        preset();
        solver.findAllSolutions();
        Assert.assertEquals(1, solver.getMeasures().getSolutionCount());
    }

    @Test
    public void testRegularKO() {
        fzn.loadInstance(
                "array [1..25] of int: tiles = [63, 6, 1, 2, 0, 9, 6, 1, 2, 378, 54, 6, 1, 2, 432, 4, 6, 1, 2, 756, 14, 6, 1, 2, 780];\n" +
                        "array [1..20] of var 1..6: board :: output_array([1..20]);\n" +
                        "constraint int_eq(board[5], 6);\n" +
                        "constraint int_eq(board[10], 6);\n" +
                        "constraint int_eq(board[15], 6);\n" +
                        "constraint int_eq(board[20], 6);\n" +
                        "constraint int_ne(board[1], 6);\n" +
                        "constraint int_ne(board[2], 6);\n" +
                        "constraint int_ne(board[3], 6);\n" +
                        "constraint int_ne(board[4], 6);\n" +
                        "constraint int_ne(board[6], 6);\n" +
                        "constraint int_ne(board[7], 6);\n" +
                        "constraint int_ne(board[8], 6);\n" +
                        "constraint int_ne(board[9], 6);\n" +
                        "constraint int_ne(board[11], 6);\n" +
                        "constraint int_ne(board[12], 6);\n" +
                        "constraint int_ne(board[13], 6);\n" +
                        "constraint int_ne(board[14], 6);\n" +
                        "constraint int_ne(board[16], 6);\n" +
                        "constraint int_ne(board[17], 6);\n" +
                        "constraint int_ne(board[18], 6);\n" +
                        "constraint int_ne(board[19], 6);\n" +
                        "constraint regularChoco(board, 4, 6, [3, 3, 3, 4, 3, 3, 2, 2, 2, 0, 2, 2, 3, 3, 3, 4, 3, 3, 2, 2, 2, 0, 2, 2], 1, 1..2);\n" +
                        "constraint regularChoco(board, 9, 6, [3, 4, 3, 3, 3, 3, 2, 0, 2, 2, 2, 2, 3, 4, 3, 3, 3, 3, 5, 9, 5, 5, 5, 5, 6, 0, 6, 6, 6, 6, 7, 0, 7, 7, 7, 7, 8, 0, 8, 8, 8, 8, 0, 9, 0, 0, 0, 0, 2, 0, 2, 2, 2, 2], 1, 1..2);\n" +
                        "constraint regularChoco(board, 14, 6, [3, 3, 3, 3, 8, 3, 2, 2, 2, 2, 0, 2, 3, 3, 3, 3, 8, 3, 5, 5, 5, 5, 0, 5, 6, 6, 6, 6, 0, 6, 7, 7, 7, 7, 0, 7, 0, 0, 0, 0, 9, 0, 4, 4, 4, 4, 13, 4, 10, 10, 10, 10, 0, 10, 11, 11, 11, 11, 0, 11, 12, 12, 12, 12, 0, 12, 13, 13, 13, 13, 0, 13, 0, 0, 0, 0, 14, 0, 2, 2, 2, 2, 0, 2], 1, 1..2);\n" +
                        "constraint regularChoco(board, 54, 6, [4, 4, 14, 4, 4, 5, 2, 2, 0, 2, 2, 2, 3, 3, 10, 3, 3, 5, 3, 3, 12, 3, 3, 5, 4, 4, 14, 4, 4, 5, 8, 8, 0, 8, 8, 0, 9, 9, 0, 9, 9, 13, 11, 11, 0, 11, 11, 11, 11, 11, 22, 11, 11, 11, 7, 7, 15, 7, 7, 11, 13, 13, 0, 13, 13, 13, 6, 6, 15, 6, 6, 0, 0, 0, 22, 0, 0, 0, 6, 6, 25, 6, 6, 0, 17, 17, 29, 17, 17, 16, 19, 19, 0, 19, 19, 19, 20, 20, 0, 20, 20, 20, 21, 21, 0, 21, 21, 21, 22, 22, 0, 22, 22, 0, 23, 23, 0, 23, 23, 24, 24, 24, 0, 24, 24, 24, 26, 26, 0, 26, 26, 0, 26, 26, 27, 26, 26, 0, 0, 0, 27, 0, 0, 0, 18, 18, 29, 18, 18, 0, 0, 0, 30, 0, 0, 0, 28, 28, 0, 28, 28, 0, 30, 30, 0, 30, 30, 0, 32, 32, 0, 32, 32, 32, 33, 33, 0, 33, 33, 33, 34, 34, 0, 34, 34, 0, 35, 35, 0, 35, 35, 35, 36, 36, 0, 36, 36, 36, 0, 0, 37, 0, 0, 0, 31, 31, 40, 31, 31, 0, 0, 0, 45, 0, 0, 0, 39, 39, 0, 39, 39, 39, 41, 41, 0, 41, 41, 41, 42, 42, 0, 42, 42, 42, 43, 43, 0, 43, 43, 0, 44, 44, 0, 44, 44, 44, 45, 45, 0, 45, 45, 0, 38, 38, 46, 38, 38, 0, 0, 0, 50, 0, 0, 0, 0, 0, 51, 0, 0, 0, 47, 47, 0, 47, 47, 47, 49, 49, 0, 49, 49, 49, 51, 51, 0, 51, 51, 0, 48, 48, 52, 48, 48, 0, 0, 0, 53, 0, 0, 0, 0, 0, 54, 0, 0, 0, 53, 53, 0, 53, 53, 0, 54, 54, 0, 54, 54, 0, 2, 2, 0, 2, 2, 2], 1, 1..2);\n" +
                        "constraint regularChoco(board, 63, 6, [7, 5, 5, 5, 5, 3, 0, 2, 2, 2, 2, 2, 7, 5, 5, 5, 5, 3, 19, 4, 4, 4, 4, 3, 30, 4, 4, 4, 4, 3, 0, 10, 10, 10, 10, 10, 46, 8, 8, 8, 8, 0, 0, 12, 12, 12, 12, 13, 0, 15, 15, 15, 15, 14, 0, 16, 16, 16, 16, 16, 0, 18, 18, 18, 18, 17, 0, 20, 20, 20, 20, 20, 0, 21, 21, 21, 21, 21, 0, 22, 22, 22, 22, 22, 0, 23, 23, 23, 23, 23, 0, 28, 28, 28, 28, 0, 47, 22, 22, 22, 22, 22, 47, 23, 23, 23, 23, 23, 46, 11, 11, 11, 11, 24, 0, 26, 26, 26, 26, 26, 0, 25, 25, 25, 25, 25, 0, 27, 27, 27, 27, 25, 0, 29, 29, 29, 29, 26, 0, 31, 31, 31, 31, 31, 32, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 34, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 46, 9, 9, 9, 9, 6, 47, 16, 16, 16, 16, 16, 0, 35, 35, 35, 35, 0, 60, 35, 35, 35, 35, 0, 0, 37, 37, 37, 37, 39, 0, 39, 39, 39, 39, 39, 60, 37, 37, 37, 37, 39, 0, 40, 40, 40, 40, 40, 0, 41, 41, 41, 41, 41, 0, 42, 42, 42, 42, 42, 0, 43, 43, 43, 43, 43, 0, 45, 45, 45, 45, 45, 0, 47, 47, 47, 47, 47, 60, 47, 47, 47, 47, 47, 48, 0, 0, 0, 0, 0, 49, 44, 44, 44, 44, 0, 53, 38, 38, 38, 38, 38, 60, 0, 0, 0, 0, 0, 0, 50, 50, 50, 50, 50, 0, 51, 51, 51, 51, 0, 0, 52, 52, 52, 52, 52, 0, 54, 54, 54, 54, 54, 0, 55, 55, 55, 55, 55, 0, 56, 56, 56, 56, 56, 0, 57, 57, 57, 57, 57, 0, 60, 60, 60, 60, 0, 0, 58, 58, 58, 58, 58, 0, 59, 59, 59, 59, 59, 61, 55, 55, 55, 55, 0, 62, 0, 0, 0, 0, 0, 63, 0, 0, 0, 0, 0, 0, 62, 62, 62, 62, 0, 0, 63, 63, 63, 63, 0, 0, 2, 2, 2, 2, 2], 1, 1..2);\n" +
                        "solve  :: int_search(board, input_order, indomain_min, complete) satisfy;\n");
        fzn.parse();
        SearchMonitorFactory.log(fzn.solver, true, true);
        SearchMonitorFactory.limitNode(fzn.solver, 10);
        fzn.solver.set(new ConstraintEngine(fzn.solver));
        fzn.solver.findSolution();
    }

    @Test
    public void testRegularOK() {
        fzn.loadInstance(
                "array [1..25] of int: tiles = [63, 6, 1, 2, 0, 9, 6, 1, 2, 378, 54, 6, 1, 2, 432, 4, 6, 1, 2, 756, 14, 6, 1, 2, 780];\n" +
                        "array [1..20] of var 1..6: board :: output_array([1..20]);\n" +
                        "constraint regularChoco(board, 4, 6, [3, 3, 3, 4, 3, 3, 2, 2, 2, 0, 2, 2, 3, 3, 3, 4, 3, 3, 2, 2, 2, 0, 2, 2], 1, 1..2);\n" +
                        "constraint regularChoco(board, 9, 6, [3, 4, 3, 3, 3, 3, 2, 0, 2, 2, 2, 2, 3, 4, 3, 3, 3, 3, 5, 9, 5, 5, 5, 5, 6, 0, 6, 6, 6, 6, 7, 0, 7, 7, 7, 7, 8, 0, 8, 8, 8, 8, 0, 9, 0, 0, 0, 0, 2, 0, 2, 2, 2, 2], 1, 1..2);\n" +
                        "constraint regularChoco(board, 14, 6, [3, 3, 3, 3, 8, 3, 2, 2, 2, 2, 0, 2, 3, 3, 3, 3, 8, 3, 5, 5, 5, 5, 0, 5, 6, 6, 6, 6, 0, 6, 7, 7, 7, 7, 0, 7, 0, 0, 0, 0, 9, 0, 4, 4, 4, 4, 13, 4, 10, 10, 10, 10, 0, 10, 11, 11, 11, 11, 0, 11, 12, 12, 12, 12, 0, 12, 13, 13, 13, 13, 0, 13, 0, 0, 0, 0, 14, 0, 2, 2, 2, 2, 0, 2], 1, 1..2);\n" +
                        "constraint regularChoco(board, 54, 6, [4, 4, 14, 4, 4, 5, 2, 2, 0, 2, 2, 2, 3, 3, 10, 3, 3, 5, 3, 3, 12, 3, 3, 5, 4, 4, 14, 4, 4, 5, 8, 8, 0, 8, 8, 0, 9, 9, 0, 9, 9, 13, 11, 11, 0, 11, 11, 11, 11, 11, 22, 11, 11, 11, 7, 7, 15, 7, 7, 11, 13, 13, 0, 13, 13, 13, 6, 6, 15, 6, 6, 0, 0, 0, 22, 0, 0, 0, 6, 6, 25, 6, 6, 0, 17, 17, 29, 17, 17, 16, 19, 19, 0, 19, 19, 19, 20, 20, 0, 20, 20, 20, 21, 21, 0, 21, 21, 21, 22, 22, 0, 22, 22, 0, 23, 23, 0, 23, 23, 24, 24, 24, 0, 24, 24, 24, 26, 26, 0, 26, 26, 0, 26, 26, 27, 26, 26, 0, 0, 0, 27, 0, 0, 0, 18, 18, 29, 18, 18, 0, 0, 0, 30, 0, 0, 0, 28, 28, 0, 28, 28, 0, 30, 30, 0, 30, 30, 0, 32, 32, 0, 32, 32, 32, 33, 33, 0, 33, 33, 33, 34, 34, 0, 34, 34, 0, 35, 35, 0, 35, 35, 35, 36, 36, 0, 36, 36, 36, 0, 0, 37, 0, 0, 0, 31, 31, 40, 31, 31, 0, 0, 0, 45, 0, 0, 0, 39, 39, 0, 39, 39, 39, 41, 41, 0, 41, 41, 41, 42, 42, 0, 42, 42, 42, 43, 43, 0, 43, 43, 0, 44, 44, 0, 44, 44, 44, 45, 45, 0, 45, 45, 0, 38, 38, 46, 38, 38, 0, 0, 0, 50, 0, 0, 0, 0, 0, 51, 0, 0, 0, 47, 47, 0, 47, 47, 47, 49, 49, 0, 49, 49, 49, 51, 51, 0, 51, 51, 0, 48, 48, 52, 48, 48, 0, 0, 0, 53, 0, 0, 0, 0, 0, 54, 0, 0, 0, 53, 53, 0, 53, 53, 0, 54, 54, 0, 54, 54, 0, 2, 2, 0, 2, 2, 2], 1, 1..2);\n" +
                        "constraint regularChoco(board, 63, 6, [7, 5, 5, 5, 5, 3, 0, 2, 2, 2, 2, 2, 7, 5, 5, 5, 5, 3, 19, 4, 4, 4, 4, 3, 30, 4, 4, 4, 4, 3, 0, 10, 10, 10, 10, 10, 46, 8, 8, 8, 8, 0, 0, 12, 12, 12, 12, 13, 0, 15, 15, 15, 15, 14, 0, 16, 16, 16, 16, 16, 0, 18, 18, 18, 18, 17, 0, 20, 20, 20, 20, 20, 0, 21, 21, 21, 21, 21, 0, 22, 22, 22, 22, 22, 0, 23, 23, 23, 23, 23, 0, 28, 28, 28, 28, 0, 47, 22, 22, 22, 22, 22, 47, 23, 23, 23, 23, 23, 46, 11, 11, 11, 11, 24, 0, 26, 26, 26, 26, 26, 0, 25, 25, 25, 25, 25, 0, 27, 27, 27, 27, 25, 0, 29, 29, 29, 29, 26, 0, 31, 31, 31, 31, 31, 32, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 34, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 46, 9, 9, 9, 9, 6, 47, 16, 16, 16, 16, 16, 0, 35, 35, 35, 35, 0, 60, 35, 35, 35, 35, 0, 0, 37, 37, 37, 37, 39, 0, 39, 39, 39, 39, 39, 60, 37, 37, 37, 37, 39, 0, 40, 40, 40, 40, 40, 0, 41, 41, 41, 41, 41, 0, 42, 42, 42, 42, 42, 0, 43, 43, 43, 43, 43, 0, 45, 45, 45, 45, 45, 0, 47, 47, 47, 47, 47, 60, 47, 47, 47, 47, 47, 48, 0, 0, 0, 0, 0, 49, 44, 44, 44, 44, 0, 53, 38, 38, 38, 38, 38, 60, 0, 0, 0, 0, 0, 0, 50, 50, 50, 50, 50, 0, 51, 51, 51, 51, 0, 0, 52, 52, 52, 52, 52, 0, 54, 54, 54, 54, 54, 0, 55, 55, 55, 55, 55, 0, 56, 56, 56, 56, 56, 0, 57, 57, 57, 57, 57, 0, 60, 60, 60, 60, 0, 0, 58, 58, 58, 58, 58, 0, 59, 59, 59, 59, 59, 61, 55, 55, 55, 55, 0, 62, 0, 0, 0, 0, 0, 63, 0, 0, 0, 0, 0, 0, 62, 62, 62, 62, 0, 0, 63, 63, 63, 63, 0, 0, 2, 2, 2, 2, 2], 1, 1..2);\n" +
                        "constraint int_eq(board[5], 6);\n" +
                        "constraint int_eq(board[10], 6);\n" +
                        "constraint int_eq(board[15], 6);\n" +
                        "constraint int_eq(board[20], 6);\n" +
                        "constraint int_ne(board[1], 6);\n" +
                        "constraint int_ne(board[2], 6);\n" +
                        "constraint int_ne(board[3], 6);\n" +
                        "constraint int_ne(board[4], 6);\n" +
                        "constraint int_ne(board[6], 6);\n" +
                        "constraint int_ne(board[7], 6);\n" +
                        "constraint int_ne(board[8], 6);\n" +
                        "constraint int_ne(board[9], 6);\n" +
                        "constraint int_ne(board[11], 6);\n" +
                        "constraint int_ne(board[12], 6);\n" +
                        "constraint int_ne(board[13], 6);\n" +
                        "constraint int_ne(board[14], 6);\n" +
                        "constraint int_ne(board[16], 6);\n" +
                        "constraint int_ne(board[17], 6);\n" +
                        "constraint int_ne(board[18], 6);\n" +
                        "constraint int_ne(board[19], 6);\n" +
                        "solve  :: int_search(board, input_order, indomain_min, complete) satisfy;\n");
        fzn.parse();
        SearchMonitorFactory.log(fzn.solver, true, true);
        SearchMonitorFactory.limitNode(fzn.solver, 10);
        fzn.solver.set(new ConstraintEngine(fzn.solver));
        fzn.solver.findSolution();
    }


}
