/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgement:
 * This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;
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
        while (model.solve()) ;
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
        r.set(randomSearch(ivs, 0));
        while (model.solve()) ;
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

    @Test(groups="1s", timeOut=60000)
    public void testPostRemove2() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c = m.arithm(v, ">", 1);
        c.post();
        m.unpost(c);
    }

    @Test(groups="1s", timeOut=60000)
    public void testPostRemove3() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c1 = m.arithm(v, ">", 1);
        Constraint c2 = m.arithm(v, ">", 0);
        c1.post();
        c2.post();
            m.unpost(c1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testPostRemove4() {
        Model m = new Model();
        IntVar v = m.intVar(0, 2);
        Constraint c1 = m.arithm(v, ">", 1);
        Constraint c2 = m.arithm(v, ">", 0);
        c1.post();
        c2.post();
            m.unpost(c2);
    }

}
