/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.nary;


import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.ResolutionPolicy.MAXIMIZE;
import static org.chocosolver.solver.constraints.SatFactory.*;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.lexico_LB;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/13
 */
public class SatTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Model model = new Model();
        BoolVar b1, b2;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        SatFactory.addBoolEq(b1, b2);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Model model = new Model();
        BoolVar b1, b2;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        SatFactory.addBoolNot(b1, b2);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Model model = new Model();
        BoolVar b1, b2;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        SatFactory.addBoolLe(b1, b2);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 3);
    }


    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Model model = new Model();
        BoolVar b1, b2, r;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        r = model.boolVar("r");
        SatFactory.addBoolIsEqVar(b1, b2, r);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void test5() {
        Model model = new Model();
        BoolVar b1, b2, r;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        r = model.boolVar("r");
        SatFactory.addBoolAndEqVar(b1, b2, r);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void test6() {
        Model model = new Model();
        BoolVar b1, b2, r;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        r = model.boolVar("r");
        SatFactory.addBoolOrEqVar(b1, b2, r);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void test7() {
        Model model = new Model();
        BoolVar b1, b2;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        SatFactory.addBoolLt(b1, b2);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void test8() {
        Model model = new Model();
        BoolVar b1, b2, r;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        r = model.boolVar("r");
        SatFactory.addBoolIsLeVar(b1, b2, r);
//        SMF.log(solver, true, true);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void test9() {
        Model model = new Model();
        BoolVar b1, b2, r;
        b1 = model.boolVar("b1");
        b2 = model.boolVar("b2");
        r = model.boolVar("r");
        SatFactory.addBoolIsLtVar(b1, b2, r);
//        SMF.log(solver, true, true);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 4);
    }


    @Test(groups="1s", timeOut=60000)
    public void test10() {
        Model model = new Model();
        BoolVar b1;
        b1 = model.boolVar("b1");
        SatFactory.addTrue(b1);
        //        SMF.log(solver, true, true);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(b1.getBooleanValue(), ESat.TRUE);
    }

    @Test(groups="1s", timeOut=60000)
    public void test11() {
        Model model = new Model();
        BoolVar b1;
        b1 = model.boolVar("b1");
        SatFactory.addFalse(b1);
        //        SMF.log(solver, true, true);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(b1.getBooleanValue(), ESat.FALSE);
    }

    @Test(groups="1s", timeOut=60000)
    public void test12() {
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 3);
        SatFactory.addBoolOrArrayEqualTrue(bs);
        SatFactory.addFalse(bs[0]);
        SatFactory.addFalse(bs[1]);
        SatFactory.addFalse(bs[2]);
        //        SMF.log(solver, true, true);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAlexLoboda() throws ContradictionException {
        Model model = new Model();
        // VARS
        IntVar var = model.intVar("var", new int[]{0, 2});
        BoolVar eq2 = model.boolVar("eq2");
        BoolVar bvar = model.boolVar("already");
        BoolVar bvar2 = model.boolVar("bvar2");
        BoolVar cond = model.boolVar("cond");
        // CSTRS
        addFalse(bvar);
        model.arithm(var, "=", 2).reifyWith(eq2);
        addBoolAndArrayEqVar(new BoolVar[]{eq2, bvar.not()}, cond);
        addBoolOrArrayEqualTrue(new BoolVar[]{eq2.not(), cond});
        addBoolOrArrayEqVar(new BoolVar[]{bvar, cond}, bvar2);
        // SEARCH
        model.set(lexico_LB(var));

        model.setObjectives(MAXIMIZE, var);
        model.solve();
        assertEquals(model.getSolutionRecorder().getLastSolution().getIntVal(var).intValue(), 2);

    }
}
