/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

        Flatzinc fzn = new Flatzinc(false, false, 1, -1);
        fzn.addListener(new BaseFlatzincListener(fzn));
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

        Flatzinc fzn = new Flatzinc(false, false, 1, -1);
        fzn.addListener(new BaseFlatzincListener(fzn));
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

        Flatzinc fzn = new Flatzinc(false, false, 1, -1);
        fzn.addListener(new BaseFlatzincListener(fzn));
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

        Flatzinc fzn = new Flatzinc(false, false, 1, -1);
        fzn.addListener(new BaseFlatzincListener(fzn));
        fzn.createSolver();
        fzn.parse(fzn.getModel(), fzn.datas[0], in);
        Model model = fzn.getModel();

        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

}
