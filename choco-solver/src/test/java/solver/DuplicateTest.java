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
package solver;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.constraints.ICF;
import solver.constraints.extension.TuplesFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VF;
import util.objects.setDataStructures.SetType;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 29/08/2014
 */
public class DuplicateTest {

    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver("ocohc");
        Solver copy = solver.duplicate();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
    }

    @Test(groups = "1s")
    public void test2() {
        Solver solver = new Solver("Choco");
        VF.fixed(-2, solver);
        VF.fixed(0, solver);
        VF.fixed("my cste 3", 3, solver);

        VF.bool("bool", solver);
        VF.bounded("bounded", 2, 4, solver);
        VF.enumerated("enum1", 1, 3, solver);
        VF.enumerated("enum2", new int[]{3, 4, 5}, solver);

        VF.set("set1", 2, 4, solver);
        VF.set("set2", new int[]{1, 2}, solver);
        VF.set("set3", new int[]{1, 2, 3, 4}, new int[]{2, 3}, solver);
        VF.set("set4", new int[]{3, 4, 5, 6}, SetType.BITSET, new int[]{5, 6}, SetType.BOOL_ARRAY, solver);

        VF.real("real", 1.1, 2.2, .001, solver);
        VF.real(VF.bounded("bounded", 2, 4, solver), 0.01);

        Solver copy = solver.duplicate();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
    }

    @Test(groups = "1s")
    public void test3() {
        Solver solver = new Solver("Choco");
        BoolVar b = VF.bool("b", solver);
        VF.not(b);
        b.not();
        VF.eq(b);
        IntVar e = VF.enumerated("e", 1, 3, solver);
        VF.offset(e, -2);
        VF.scale(e, 3);
        VF.minus(e);
        VF.eq(e);
        VF.task(e, solver.ONE, VF.offset(e, 1));

        Solver copy = solver.duplicate();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
    }

    @Test(groups = "1s")
    public void test4() {
        Solver solver = new Solver("Choco");
        solver.post(ICF.TRUE(solver));
        solver.post(ICF.FALSE(solver));
        Solver copy = solver.duplicate();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
    }

    @Test(groups = "1s")
    public void test5() {
        for (String op : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            solver.post(ICF.arithm(v, op, 3));

            Solver copy = solver.duplicate();

            solver.findAllSolutions();
            copy.findAllSolutions();
            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test6() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        ICF.arithm(v, "=", 3).reif();

        Solver copy = solver.duplicate();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test7() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);

        solver.post(ICF.member(v, 2, 3));
        solver.post(ICF.member(v, new int[]{2}));
        solver.post(ICF.not_member(v, 0, 1));
        solver.post(ICF.not_member(v, new int[]{7}));


        Solver copy = solver.duplicate();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test8() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", -6, 4, solver);
        solver.post(ICF.absolute(v, w));

        Solver copy = solver.duplicate();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test9() {
        for (String op : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            IntVar w = VF.enumerated("v", 1, 4, solver);
            solver.post(ICF.arithm(v, op, w));

            Solver copy = solver.duplicate();

            solver.findAllSolutions();
            copy.findAllSolutions();
            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test10() {
        for (String op1 : new String[]{"+", "-"}) {
            for (String op2 : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
                Solver solver = new Solver("Choco");
                IntVar v = VF.enumerated("v", 1, 4, solver);
                IntVar w = VF.enumerated("v", 1, 4, solver);
                solver.post(ICF.arithm(v, op1, w, op2, 1));

                Solver copy = solver.duplicate();

                solver.findAllSolutions();
                copy.findAllSolutions();
                Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
                Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
                Assert.assertEquals(copy.toString(), solver.toString());
                Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
            }
        }
    }

    @Test(groups = "1s")
    public void test11() {
        for (String op1 : new String[]{"+", "-"}) {
            for (String op2 : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
                Solver solver = new Solver("Choco");
                IntVar v = VF.enumerated("v", 1, 4, solver);
                IntVar w = VF.enumerated("v", 1, 4, solver);
                solver.post(ICF.arithm(v, op2, w, op1, 1));

                Solver copy = solver.duplicate();

                solver.findAllSolutions();
                copy.findAllSolutions();
                Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
                Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
                Assert.assertEquals(copy.toString(), solver.toString());
                Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
            }
        }
    }

    @Test(groups = "1s")
    public void test12() {
        for (String op : new String[]{"=", "!=", ">", "<"}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            IntVar w = VF.enumerated("v", -6, 4, solver);
            solver.post(ICF.distance(v, w, op, 1));

            Solver copy = solver.duplicate();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test13() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 0, 3, solver);
        solver.post(ICF.element(v, new int[]{4, 3, 2, 1}, w));

        Solver copy = solver.duplicate();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test14() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 16, solver);
        solver.post(ICF.square(w, v));

        Solver copy = solver.duplicate();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test15() {
        for (String op : new String[]{"AC3", "AC3rm", "AC3bit+rm", "AC2001", "FC"}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            IntVar w = VF.enumerated("v", -6, 4, solver);
            solver.post(ICF.table(v, w, TuplesFactory.allEquals(v, w), op));

            Solver copy = solver.duplicate();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

}
