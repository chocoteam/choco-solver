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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import solver.Solver;
import solver.search.strategy.StrategyFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
public class PredicateTests {

    FZNParser fzn;
    Solver solver;

    @BeforeMethod
    public void before() {
        fzn = new FZNParser();
        solver = fzn.solver;
    }

    @AfterMethod
    public void after(){
        solver.set(StrategyFactory.forceInputOrderMinVal(solver.getVars(), solver.getEnvironment()));
        solver.findSolution();
    }

    @Test
    public void test_array_bool_and(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array[1 .. 3] of var bool : as;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool:r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint array_bool_and(as, r);");
    }

    @Test
    public void test_array_bool_element(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3: b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool:c;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint array_bool_element(b, [0, 1, 1], c);");
    }

    @Test
    public void test_array_bool_or(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array[1 .. 3] of var bool : as;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool:r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint array_bool_or(as, r);");
    }

    @Test
    public void test_array_int_element(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3: b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -1 .. 5 :c;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint array_int_element(b, [2, -1, 5], c);");
    }

    @Test
    public void test_array_var_bool_element(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3: b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array[1 .. 3] of var bool : as;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool :c;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint array_var_bool_element(b, as, c);");
    }

    @Test
    public void test_array_var_int_element(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3: b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array[1 .. 3] of var 1 .. 4 : as;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 4 :c;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint array_var_int_element(b, as, c);");
    }

    @Test
    public void test_bool2int(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -1 .. 2 :b;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool2int(a, b);");
    }

    @Test
    public void test_bool_and(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool_and(a, b, r);");
    }

    @Test
    public void test_bool_clause(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array [1 .. 3] of var bool : as;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array [1 .. 4] of var bool : bs;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool_clause(as, bs);");
    }

    @Test
    public void test_bool_eq(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : b;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool_eq(a, b);");
    }

    @Test
    public void test_bool_eq_reif(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool_eq_reif(a, b, r);");
    }

    @Test
    public void test_bool_le(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : b;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool_le(a, b);");
    }

    @Test
    public void test_bool_le_reif(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool_le_reif(a, b, r);");
    }

    @Test
    public void test_bool_lt(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : b;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool_lt(a, b);");
    }

    @Test
    public void test_bool_lt_reif(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool_lt_reif(a, b, r);");
    }

    @Test
    public void test_bool_not(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : b;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool_not(a, b);");
    }

    @Test
    public void test_bool_or(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool_or(a, b, r);");
    }

    @Test
    public void test_bool_xor(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint bool_xor(a, b, r);");
    }

    @Test
    public void test_int_abs(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_abs(a, b);");
    }

    @Test
    public void test_int_div(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 2 : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 0 .. 3 : c;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_div(a, b, c);");
    }

    @Test
    public void test_int_eq(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_eq(a, b);");
    }

    @Test
    public void test_int_eq_reif(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_eq_reif(a, b, r);");
    }

    @Test
    public void test_int_le(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_le(a, b);");
    }

    @Test
    public void test_int_le_reif(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_le_reif(a, b, r);");
    }

    @Test
    public void test_int_lin_eq(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array [1 .. 3] of var 1 .. 3 : bs;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lin_eq([1, 2, 3], bs, 7);");
    }

    @Test
    public void test_int_lin_eq_reif(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array [1 ..3] of var 1 .. 3 : bs;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lin_eq_reif([1, 2, 3], bs, 7, r);");
    }

    @Test
    public void test_int_lin_le(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array [1 ..3] of var 1 .. 3 : bs;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lin_le([1, 2, 3], bs, 8);");
    }

    @Test
    public void test_int_lin_le_reif(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array [1 ..3] of var 1 .. 3 : bs;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lin_le_reif([1, 2, 3], bs, 8, r);");
    }

    @Test
    public void test_int_lin_ne(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array [1 ..3] of var 1 .. 3 : bs;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lin_ne([1, 2, 3], bs, 6);");
    }

    @Test
    public void test_int_lin_ne_reif(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array [1 ..3] of var 1 .. 3 : bs;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lin_ne_reif([1, 2, 3], bs, 6, r);");
    }

    @Test
    public void test_int_lt(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lt(a, b);");
    }

    @Test
    public void test_int_lt_reif(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lt_reif(a, b, r);");
    }

    @Test
    public void test_int_max(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 3 : c;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_max(a, b, c);");
    }

    @Test
    public void test_int_min(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 3 : c;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_min(a, b, c);");
    }

    @Test
    public void test_int_mod(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 3 : c;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_mod(a, b, c);");
    }

    @Test
    public void test_int_ne(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_ne(a, b);");
    }

    @Test
    public void test_int_ne_reif(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var bool : r;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_ne_reif(a, b, r);");
    }

    @Test
    public void test_int_plus(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var int : c;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_plus(a, b, c);");
    }

    @Test
    public void test_int_times(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 3 : a;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var -3 .. 2 : b;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var int : c;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_times(a, b, c);");
    }
}
