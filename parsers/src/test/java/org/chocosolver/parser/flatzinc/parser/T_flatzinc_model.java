/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.parser;

import org.chocosolver.parser.flatzinc.Flatzinc4Parser;
import org.chocosolver.parser.flatzinc.FznSettings;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.solver.Model;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/10/12
 */
public class T_flatzinc_model extends GrammarTest {

    Model mSolver;
    Datas datas;
    StringBuilder st;

    @BeforeMethod(alwaysRun = true)
    public void before() {
        mSolver = new Model(new FznSettings());
        datas = new Datas();
        st = new StringBuilder();
    }

    @AfterMethod
    public void after() {
        Flatzinc4Parser fp = null;
        try {
            fp = parser(st.toString(), mSolver, datas);
        } catch (IOException e) {
            Assert.fail();
        }
        fp.flatzinc_model(mSolver, datas);
        mSolver.getSolver().solve();
    }

    @Test(groups = "1s")
    public void testRegularKO() {
        st.append(
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
    }

    @Test(groups = "1s")
    public void testRegularOK() {
        st.append(
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
    }

    @Test(groups = "1s")
    public void testSatisfy5() {
        st.append("constraint array_bool_and([], true);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void testSatisfy6() {
        st.append("var bool: x1 :: output_var;");
        st.append("var bool: x2 :: output_var;");
        st.append("constraint bool_eq(x1, x2);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_array_bool_and() {

        st.append("array[1 .. 3] of var bool : as;");
        st.append("var bool:r;");
        st.append("constraint array_bool_and(as, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_array_bool_element() {

        st.append("var 1 .. 3: b;");
        st.append("var bool:c;");
        st.append("constraint array_bool_element(b, [0, 1, 1], c);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_array_bool_or() {

        st.append("array[1 .. 3] of var bool : as;");
        st.append("var bool:r;");
        st.append("constraint array_bool_or(as, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_array_int_element() {

        st.append("var 1 .. 3: b;");
        st.append("var -1 .. 5 :c;");
        st.append("constraint array_int_element(b, [2, -1, 5], c);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_array_var_bool_element() {

        st.append("var 1 .. 3: b;");
        st.append("array[1 .. 3] of var bool : as;");
        st.append("var bool :c;");
        st.append("constraint array_var_bool_element(b, as, c);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_array_var_int_element() {

        st.append("var 1 .. 3: b;");
        st.append("array[1 .. 3] of var 1 .. 4 : as;");
        st.append("var 1 .. 4 :c;");
        st.append("constraint array_var_int_element(b, as, c);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool2int() {

        st.append("var bool : a;");
        st.append("var -1 .. 2 :b;");
        st.append("constraint bool2int(a, b);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool_and() {

        st.append("var bool : a;");
        st.append("var bool : b;");
        st.append("var bool : r;");
        st.append("constraint bool_and(a, b, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool_clause() {

        st.append("array [1 .. 3] of var bool : as;");
        st.append("array [1 .. 4] of var bool : bs;");
        st.append("constraint bool_clause(as, bs);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool_eq() {

        st.append("var bool : a;");
        st.append("var bool : b;");
        st.append("constraint bool_eq(a, b);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool_eq_reif() {

        st.append("var bool : a;");
        st.append("var bool : b;");
        st.append("var bool : r;");
        st.append("constraint bool_eq_reif(a, b, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool_le() {

        st.append("var bool : a;");
        st.append("var bool : b;");
        st.append("constraint bool_le(a, b);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool_le_reif() {

        st.append("var bool : a;");
        st.append("var bool : b;");
        st.append("var bool : r;");
        st.append("constraint bool_le_reif(a, b, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool_lt() {

        st.append("var bool : a;");
        st.append("var bool : b;");
        st.append("constraint bool_lt(a, b);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool_lt_reif() {

        st.append("var bool : a;");
        st.append("var bool : b;");
        st.append("var bool : r;");
        st.append("constraint bool_lt_reif(a, b, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool_not() {

        st.append("var bool : a;");
        st.append("var bool : b;");
        st.append("constraint bool_not(a, b);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool_or() {

        st.append("var bool : a;");
        st.append("var bool : b;");
        st.append("var bool : r;");
        st.append("constraint bool_or(a, b, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_bool_xor() {

        st.append("var bool : a;");
        st.append("var bool : b;");
        st.append("var bool : r;");
        st.append("constraint bool_xor(a, b, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_abs() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("constraint int_abs(a, b);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_div() {

        st.append("var 1 .. 3 : a;");
        st.append("var 1 .. 2 : b;");
        st.append("var 0 .. 3 : c;");
        st.append("constraint int_div(a, b, c);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_eq() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("constraint int_eq(a, b);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_eq_reif() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("var bool : r;");
        st.append("constraint int_eq_reif(a, b, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_le() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("constraint int_le(a, b);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_le_reif() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("var bool : r;");
        st.append("constraint int_le_reif(a, b, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_lin_eq() {

        st.append("array [1 .. 3] of var 1 .. 3 : bs;");
        st.append("constraint int_lin_eq([1, 2, 3], bs, 7);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_lin_eq_reif() {

        st.append("array [1 ..3] of var 1 .. 3 : bs;");
        st.append("var bool : r;");
        st.append("constraint int_lin_eq_reif([1, 2, 3], bs, 7, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_lin_le() {

        st.append("array [1 ..3] of var 1 .. 3 : bs;");
        st.append("constraint int_lin_le([1, 2, 3], bs, 8);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_lin_le_reif() {

        st.append("array [1 ..3] of var 1 .. 3 : bs;");
        st.append("var bool : r;");
        st.append("constraint int_lin_le_reif([1, 2, 3], bs, 8, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_lin_ne() {

        st.append("array [1 ..3] of var 1 .. 3 : bs;");
        st.append("constraint int_lin_ne([1, 2, 3], bs, 6);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_lin_ne_reif() {

        st.append("array [1 ..3] of var 1 .. 3 : bs;");
        st.append("var bool : r;");
        st.append("constraint int_lin_ne_reif([1, 2, 3], bs, 6, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_lt() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("constraint int_lt(a, b);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_lt_reif() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("var bool : r;");
        st.append("constraint int_lt_reif(a, b, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_max() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("var -3 .. 3 : c;");
        st.append("constraint int_max(a, b, c);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_min() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("var -3 .. 3 : c;");
        st.append("constraint int_min(a, b, c);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_mod() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("var -3 .. 3 : c;");
        st.append("constraint int_mod(a, b, c);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_ne() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("constraint int_ne(a, b);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_ne_reif() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("var bool : r;");
        st.append("constraint int_ne_reif(a, b, r);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_plus() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("var int : c;");
        st.append("constraint int_plus(a, b, c);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_int_times() {

        st.append("var 1 .. 3 : a;");
        st.append("var -3 .. 2 : b;");
        st.append("var int : c;");
        st.append("constraint int_times(a, b, c);");
        st.append("solve satisfy;");
    }

    @Test(groups = "1s")
    public void test_regular() {

        st.append("predicate regularChoco(array [int] of var int: x, int: Q, int: S, array [int, int] of int: d, int: q0, set of int: F);");
        st.append("array [1..20] of var 1..6: board;");
        st.append("constraint regularChoco(board, 4, 6, [3, 3, 3, 4, 3, 3, 2, 2, 2, 0, 2, 2, 3, 3, 3, 4, 3, 3, 2, 2, 2, 0, 2, 2], 1, 1..2);");
        st.append("solve satisfy;");

    }
}
