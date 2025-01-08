/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc;

import org.chocosolver.solver.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/*
 * User : CPRUDHOM
 * Mail : cprudhom(a)emn.fr
 * Date : 13 janv. 2010
 * Since : Choco 2.1.1
 *
 */
public class FlatzincModelTest {


    @Test(groups = "1s")
    public void test1() {

        InputStream in = new ByteArrayInputStream(("var 1 .. 2: a::output_var;" + "constraint int_ne(a, 1);" + "solve satisfy;").getBytes());

        Flatzinc fzn = new Flatzinc(false, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test2() {
        InputStream in = new ByteArrayInputStream(("var 1 .. 2: a::output_var;\n" +
                "constraint int_ne(a, 1);\n" +
                "solve satisfy;").getBytes());

        Flatzinc fzn = new Flatzinc(false, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test3() {

        InputStream in = new ByteArrayInputStream(("array[1 .. 2] of var 1 .. \n" +
                "2: q;\n" +
                "constraint int_ne(q[1], q[2]);\n" +
                "solve satisfy;").getBytes());

        Flatzinc fzn = new Flatzinc(false, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test11() {

        InputStream in = new ByteArrayInputStream(("array[1 .. 3] of int: covers = [1,5,8];\n" +
                "array[1 .. 3] of int: lbound = [0,1,0];\n" +
                "array[1 .. 3] of int: ubound = [1,1,1];\n" +
                "array[1 .. 3] of var 1 .. 10: vars;\n" +
                "constraint globalCardinalityLowUpChoco(vars, covers, lbound, ubound,false);\n" +
                "solve satisfy;").getBytes());

        Flatzinc fzn = new Flatzinc(false, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test12() {

        InputStream in = new ByteArrayInputStream(("var set of 1..10: x:: output_var;\n" +
                "var 0..10: X_INTRODUCED_1_ ::var_is_introduced ;\n" +
                "constraint set_card(x,X_INTRODUCED_1_);\n" +
                "solve  maximize X_INTRODUCED_1_;").getBytes());

        Flatzinc fzn = new Flatzinc(false, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void testWarmStart() {
        InputStream in = new ByteArrayInputStream(("array [1..10] of int: X_INTRODUCED_11_ = [1,1,1,1,1,1,1,1,1,1];\n" +
                "array [1..10] of int: X_INTRODUCED_12_ = [1,0,0,1,0,0,1,0,0,0];\n" +
                "var 0..1: X_INTRODUCED_0_;\n" +
                "var 0..1: X_INTRODUCED_1_;\n" +
                "var 0..1: X_INTRODUCED_2_;\n" +
                "var 0..1: X_INTRODUCED_3_;\n" +
                "var 0..1: X_INTRODUCED_4_;\n" +
                "var 0..1: X_INTRODUCED_5_;\n" +
                "var 0..1: X_INTRODUCED_6_;\n" +
                "var 0..1: X_INTRODUCED_7_;\n" +
                "var 0..1: X_INTRODUCED_8_;\n" +
                "var 0..1: X_INTRODUCED_9_;\n" +
                "array [1..10] of var int: x:: output_array([1..10]) = [X_INTRODUCED_0_,X_INTRODUCED_1_,X_INTRODUCED_2_,X_INTRODUCED_3_,X_INTRODUCED_4_,X_INTRODUCED_5_,X_INTRODUCED_6_,X_INTRODUCED_7_,X_INTRODUCED_8_,X_INTRODUCED_9_];\n" +
                "constraint int_lin_le(X_INTRODUCED_11_,[X_INTRODUCED_0_,X_INTRODUCED_1_,X_INTRODUCED_2_,X_INTRODUCED_3_,X_INTRODUCED_4_,X_INTRODUCED_5_,X_INTRODUCED_6_,X_INTRODUCED_7_,X_INTRODUCED_8_,X_INTRODUCED_9_],3);\n" +
                "solve :: warm_start_int(x,X_INTRODUCED_12_) satisfy;").getBytes());
        Flatzinc fzn = new Flatzinc(false, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        fzn.configureSearch();
        Model model = fzn.getModel();
        //model.getSolver().showDecisions();
        //model.getSolver().limitSolution(3);
        while (model.getSolver().solve()) {
            fzn.datas[0].onSolution();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 176);
    }

    @Test(groups = "1s")
    public void testEmptyListOfVariablesInSearch() {
        InputStream in = new ByteArrayInputStream(("solve :: int_search([], largest, indomain_random, complete) satisfy;").getBytes());
        Flatzinc fzn = new Flatzinc(false, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        fzn.configureSearch();
        Model model = fzn.getModel();
        model.getSolver().solve();
        // expecting no error
    }

    @Test(groups = "1s")
    public void testAllEquals() {
        InputStream in = new ByteArrayInputStream(("predicate fzn_all_equal_int_reif(array [int] of var int: x,var bool: b);\n" +
                "constraint fzn_all_equal_int_reif([2,2],false);\n" +
                "solve  satisfy;\n").getBytes());
        Flatzinc fzn = new Flatzinc(false, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        fzn.configureSearch();
        Model model = fzn.getModel();
        Assert.assertFalse(model.getSolver().solve());
    }


    @Test(groups = "1s")
    public void testMats1() {
        InputStream in = new ByteArrayInputStream((
                "predicate fzn_maximum_arg_int(array [int] of var int: x,var int: z);\n" +
                        "var 1..2: A:: output_var;\n" +
                        "var 3..4: B:: output_var;\n" +
                        "var 3..4: E:: output_var;\n" +
                        "array [1..4] of var int: X_INTRODUCED_0_ ::var_is_introduced  = [E,B,1,4];\n" +
                        "constraint fzn_maximum_arg_int(X_INTRODUCED_0_,A);\n" +
                        "solve  satisfy;\n").getBytes());

        Flatzinc fzn = new Flatzinc(true, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        while (model.getSolver().solve()) {
            fzn.datas[0].onSolution();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 3);
    }

    @Test(groups = "1s")
    public void testMats2() {
        InputStream in = new ByteArrayInputStream((
                "predicate fzn_minimum_arg_int(array [int] of var int: x,var int: z);\n" +
                        "var 1..2: A:: output_var;\n" +
                        "var {1,6}: B:: output_var;\n" +
                        "var {1,6}: D:: output_var;\n" +
                        "array [1..4] of var int: X_INTRODUCED_0_ ::var_is_introduced  = [D,B,7,5];\n" +
                        "constraint fzn_minimum_arg_int(X_INTRODUCED_0_,A);\n" +
                        "solve  satisfy;\n").getBytes());

        Flatzinc fzn = new Flatzinc(true, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        while (model.getSolver().solve()) {
            fzn.datas[0].onSolution();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 3);
    }

    @Test(groups = "1s")
    public void testMats3() {
        InputStream in = new ByteArrayInputStream((
                "predicate fzn_minimum_arg_int(array [int] of var int: x,var int: z);\n" +
                        "var {1,7}: B:: output_var;\n" +
                        "var 1..4: C:: output_var;\n" +
                        "var {1,7}: E:: output_var;\n" +
                        "array [1..5] of var int: X_INTRODUCED_0_ ::var_is_introduced  = [B,E,B,1,7];\n" +
                        "constraint fzn_minimum_arg_int(X_INTRODUCED_0_,C);\n" +
                        "solve :: int_search([B,C,E],occurrence,indomain_reverse_split,complete) satisfy;" +
                        "\n").getBytes());

        Flatzinc fzn = new Flatzinc(true, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        while (model.getSolver().solve()) {
            fzn.datas[0].onSolution();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testMats4() {
        InputStream in = new ByteArrayInputStream((
                "predicate fzn_maximum_arg_int(array [int] of var int: x,var int: z);\n" +
                        "var {4,5,7}: C:: output_var;\n" +
                        "array [1..5] of var int: X_INTRODUCED_0_ ::var_is_introduced  = [1,1,1,C,C];\n" +
                        "constraint fzn_maximum_arg_int(X_INTRODUCED_0_,5);\n" +
                        "solve  satisfy;\n" +
                        "\n").getBytes());

        Flatzinc fzn = new Flatzinc(true, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        while (model.getSolver().solve()) {
            fzn.datas[0].onSolution();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups = "1s")
    public void testMats5() {
        InputStream in = new ByteArrayInputStream((
                "var -1..-1: A;\n" +
                        "var {-8,-7,-2}: B:: output_var;\n" +
                        "constraint int_mod(B,B,-1);\n" +
                        "solve  satisfy;" +
                        "\n").getBytes());

        Flatzinc fzn = new Flatzinc(true, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        while (model.getSolver().solve()) {
            fzn.datas[0].onSolution();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups = "1s")
    public void testMats6() {
        InputStream in = new ByteArrayInputStream((
                "predicate choco_fzn_lex_less(array [int] of var int: x,array [int] of var int: y,bool: strict);\n" +
                        "var 1..3: A:: output_var;\n" +
                        "array [1..2] of var int: X_INTRODUCED_0_ ::var_is_introduced :: promise_ctx_antitone = [A,0];\n" +
                        "constraint choco_fzn_lex_less(X_INTRODUCED_0_,X_INTRODUCED_0_,true);\n" +
                        "solve  satisfy;" +
                        "\n").getBytes());

        Flatzinc fzn = new Flatzinc(true, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        while (model.getSolver().solve()) {
            fzn.datas[0].onSolution();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups = "1s")
    public void testMats7() {
        InputStream in = new ByteArrayInputStream((
                "predicate choco_fzn_table(array [int] of var int: x,array [int] of int: t);\n" +
                        "array [1..12] of int: X_INTRODUCED_2_ = [1,4,2,1,4,7,7,2,7,7,18,18];\n" +
                        "var {1,2,4,7,18}: A:: output_var;\n" +
                        "array [1..2] of var int: X_INTRODUCED_1_ ::var_is_introduced  = [A,A];\n" +
                        "constraint choco_fzn_table(X_INTRODUCED_1_,X_INTRODUCED_2_);\n" +
                        "solve :: int_search([A],largest,indomain_interval,complete) satisfy;" +
                        "\n").getBytes());

        Flatzinc fzn = new Flatzinc(true, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        while (model.getSolver().solve()) {
            fzn.datas[0].onSolution();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups = "1s")
    public void testMats8() {
        InputStream in = new ByteArrayInputStream((
                "predicate fzn_count_eq_reif(array [int] of var int: x,var int: y,var int: c,var bool: b);\n" +
                        "var {1,3}: A:: output_var;\n" +
                        "constraint fzn_count_eq_reif([1],A,4,false);\n" +
                        "solve  satisfy;" +
                        "\n").getBytes());

        Flatzinc fzn = new Flatzinc(true, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        while (model.getSolver().solve()) {
            fzn.datas[0].onSolution();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups = "1s")
    public void testMats9() {
        InputStream in = new ByteArrayInputStream((
                "array [1..2] of int: X_INTRODUCED_3_ = [1,3];\n" +
                        "var {-2,0}: A:: output_var;\n" +
                        "var -1..1: x1:: is_defined_var:: output_var;\n" +
                        "var -1..5: x2:: output_var:: is_defined_var;\n" +
                        "var 0..32: X_INTRODUCED_4_ ::var_is_introduced :: is_defined_var;\n" +
                        "constraint int_pow(-1,A,x1):: defines_var(x1);\n" +
                        "constraint int_lin_eq(X_INTRODUCED_3_,[x2,x1],2):: defines_var(x2);\n" +
                        "constraint int_pow(2,x2,X_INTRODUCED_4_):: defines_var(X_INTRODUCED_4_);\n" +
                        "solve  satisfy;" +
                        "\n").getBytes());

        Flatzinc fzn = new Flatzinc(true, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        while (model.getSolver().solve()) {
            fzn.datas[0].onSolution();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups = "1s")
    public void testMats10() {
        InputStream in = new ByteArrayInputStream((
                "predicate fzn_count_eq(array [int] of var int: x,var int: y,var int: c);\n" +
                        "var 1..2: B:: output_var;\n" +
                        "var {0,3}: E:: output_var;\n" +
                        "var {1,3}: F:: output_var;\n" +
                        "array [1..5] of var int: X_INTRODUCED_17_ ::var_is_introduced  = [0,1,F,E,1];\n" +
                        "constraint fzn_count_eq(X_INTRODUCED_17_,B,F);\n" +
                        "solve  satisfy;" +
                        "\n").getBytes());

        Flatzinc fzn = new Flatzinc(true, false, 1);
        fzn.createSettings();
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        while (model.getSolver().solve()) {
            fzn.datas[0].onSolution();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }
}
