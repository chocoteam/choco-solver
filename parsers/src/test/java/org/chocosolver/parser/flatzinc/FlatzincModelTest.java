/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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

}
