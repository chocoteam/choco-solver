/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/03/2014
 */
public class ConstraintTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testBooleanChannelingJL() {
        //#issue 190
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("bs", 3);
        SetVar s1 = model.setVar("s1", new int[]{}, new int[]{-3, -2, -1, 0, 1, 2, 3});
        SetVar s2 = model.setVar("s2", new int[]{}, new int[]{-3, -2, -1, 0, 1, 2, 3});
        model.or(model.allEqual(new SetVar[]{s1, s2}), model.setBoolsChanneling(bs, s1, 0)).post();
        while (model.getSolver().solve()) ;
        assertEquals(2040, model.getSolver().getSolutionCount());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDependencyConditions() {
        Model model = new Model();
        IntVar[] ivs = model.intVarArray("X", 4, 0, 10, false);
        model.allDifferent(ivs, "BC").post(); // boundAndInst()
        model.arithm(ivs[0], "+", ivs[1], "=", 4).post(); // all()
        model.arithm(ivs[0], ">=", ivs[2]).post(); // INST + UB or INST + LB
        model.arithm(ivs[0], "!=", ivs[3]).post(); // instantiation()

        Solver r = model.getSolver();
        r.setSearch(randomSearch(ivs, 0));
        while (model.getSolver().solve()) ;
        assertEquals(r.getMeasures().getSolutionCount(), 48);
        assertEquals(r.getMeasures().getNodeCount(), 100);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDependencyConditions2() {
        Model model = new Model();
        IntVar[] ivs = model.intVarArray("X", 4, 0, 10, false);
        model.allDifferent(ivs, "BC").post(); // boundAndInst()
        model.arithm(ivs[0], "+", ivs[1], "=", 4).post(); // all()
        Constraint cr = model.arithm(ivs[0], ">=", ivs[2]);
        cr.post(); // INST + UB or INST + LB
        model.arithm(ivs[0], "!=", ivs[3]).post(); // instantiation()
        model.unpost(cr);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testOpposite1() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        Constraint co = c.getOpposite();
        Assert.assertEquals(c.getOpposite(), co);
        Assert.assertEquals(co.getOpposite(), c);
        Assert.assertFalse(c.isReified());
        Assert.assertFalse(co.isReified());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testOpposite2() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        BoolVar b = c.reify();
        Constraint co = c.getOpposite();
        Assert.assertEquals(c.getOpposite(), co);
        Assert.assertEquals(co.getOpposite(), c);
        Assert.assertTrue(c.isReified());
        Assert.assertTrue(co.isReified());
        Assert.assertEquals(b.not(), co.reify());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testOpposite3() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        Constraint co = c.getOpposite();
        BoolVar b = co.reify();
        Assert.assertEquals(c.getOpposite(), co);
        Assert.assertEquals(co.getOpposite(), c);
        Assert.assertTrue(c.isReified());
        Assert.assertTrue(co.isReified());
        Assert.assertEquals(b.not(), c.reify());
    }


    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testPostAndReif1() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        c.post();
        try {
            c.reify();
            fail();
        } catch (SolverException se) {
            se.printStackTrace(m.getSolver().getOut());
            throw se;
        }
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testPostAndReif2() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        c.reify();
        try {
            c.post();
            fail();
        } catch (SolverException se) {
            se.printStackTrace(m.getSolver().getOut());
            throw se;
        }
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testPostTwice1() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        c.post();
        try {
            c.post();
            fail();
        } catch (SolverException se) {
            se.printStackTrace(m.getSolver().getOut());
            throw se;
        }
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testPostTwice2() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        c.post();
        try {
            new Constraint("copycat", c.getPropagators());
            fail();
        } catch (SolverException se) {
            se.printStackTrace(m.getSolver().getOut());
            throw se;
        }
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testPostRemove1() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        try {
            m.unpost(c);
            fail();
        } catch (SolverException se) {
            se.printStackTrace(m.getSolver().getOut());
            throw se;
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPostRemove2() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        c.post();
        m.unpost(c);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPostRemove3() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c1 = m.arithm(v, ">", 1);
        Constraint c2 = m.arithm(v, ">", 0);
        c1.post();
        c2.post();
        m.unpost(c1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPostRemove4() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c1 = m.arithm(v, ">", 1);
        Constraint c2 = m.arithm(v, ">", 0);
        c1.post();
        c2.post();
        m.unpost(c2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPostUnpostPost1() {
        Model m = new Model();
        IntVar v = m.intVar(1, 3);
        IntVar w = m.intVar(0, 2);
        Constraint c1 = m.arithm(v, ">", w);
        c1.post();
        while (m.getSolver().solve()) ;
        Assert.assertEquals(m.getSolver().getMeasures().getSolutionCount(), 6);
        m.getSolver().reset();
        m.unpost(c1);
        while (m.getSolver().solve()) ;
        Assert.assertEquals(m.getSolver().getMeasures().getSolutionCount(), 9);
        m.getSolver().reset();
        c1.post();
        while (m.getSolver().solve()) ;
        Assert.assertEquals(m.getSolver().getMeasures().getSolutionCount(), 6);
    }

    @DataProvider(name = "unpost")
    public Object[][] providUP() {
        return new Object[][]{
                {"="}, {"!="}, {"<"}, {">"}};
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "unpost")
    public void testUnpost(String op) {
        Model m = new Model();
        {
            IntVar[] M = m.intVarArray("M", 3, 0, 2, false);
            BoolVar[][] choice = m.boolVarMatrix("choice", 3, 3);
            for (int i = 0; i < 3; i++) {
                m.boolsIntChanneling(choice[i], M[i], 0).post();
            }
            m.arithm(M[0], "=", 0).post();
            m.arithm(M[1], "=", 0).post();
            m.getSolver().setSearch(Search.inputOrderLBSearch(M));
            m.getSolver().findAllSolutions();
        }
        Model m2 = new Model();
        {
            IntVar[] M = m2.intVarArray("M", 3, 0, 2, false);
            BoolVar[][] choice = m2.boolVarMatrix("choice", 3, 3);
            for (int i = 0; i < 3; i++) {
                m2.boolsIntChanneling(choice[i], M[i], 0).post();
            }
            m2.arithm(M[0], "=", 0).post();
            m2.arithm(M[1], "=", 0).post();
            // begin diff
            Constraint c2 = m2.arithm(M[2], op, M[1]);
            c2.post();
            m2.unpost(c2);
            // end diff
            m2.getSolver().setSearch(Search.inputOrderLBSearch(M));
            m2.getSolver().findAllSolutions();
        }
        assertEquals(m.getSolver().getSolutionCount(), m2.getSolver().getSolutionCount());
    }

    @Test(groups = "1s", timeOut = 6000000)
    public void testUnlink1() {
        Model model = new Model("unlink");
        IntVar[] vars = model.intVarArray("X", 3, 0, 4);
        vars[0].eq(1).post();
        vars[0].ne(0).post();
        model.post(
                model.sum(new IntVar[]{vars[0], vars[1]}, ">", 1),
                model.sum(new IntVar[]{vars[0], vars[1], vars[2]}, ">", 2)
        );
        Propagator[] propagators = vars[0].getPropagators();

        Assert.assertEquals(vars[0].getPIndices(), new int[]{0,1,0,0,0,0,0,0});
        Assert.assertEquals(vars[0].getPropagators(), propagators);

        Assert.assertEquals(vars[1].getPIndices(), new int[]{1,0,0,0,0,0,0,0});
        Assert.assertEquals(vars[1].getPropagators(), new Propagator[]{propagators[0], propagators[1], null, null, null, null, null, null});

        Assert.assertEquals(vars[2].getPIndices(), new int[]{2,0,0,0,0,0,0,0});
        Assert.assertEquals(vars[2].getPropagators(), new Propagator[]{propagators[0], null, null, null, null, null, null, null});

        Assert.assertEquals(propagators[0].getVIndices(), new int[]{0,0,0});
        Assert.assertEquals(propagators[1].getVIndices(), new int[]{1,1});
        Assert.assertEquals(propagators[2].getVIndices(), new int[]{2});
        Assert.assertEquals(propagators[3].getVIndices(), new int[]{3});

    }

}
