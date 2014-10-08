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
package parser.flatzinc.parser;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import parser.flatzinc.Flatzinc4Parser;
import parser.flatzinc.ast.Datas;
import solver.Solver;
import solver.constraints.Arithmetic;
import solver.constraints.Constraint;
import solver.variables.VariableFactory;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_constraint extends GrammarTest {

    Solver mSolver;
    Datas map;

    @BeforeMethod
    public void before() {
        mSolver = new Solver();
        map = new Datas();
    }

    @Test(groups = "1s")
    public void test1() throws IOException {
        map.register("x", VariableFactory.bounded("x", 0, 2, mSolver));
        Flatzinc4Parser fp = parser("constraint int_le(0,x); % 0<= x\n", mSolver, map);
        fp.constraint();
        Assert.assertEquals(mSolver.getCstrs().length, 1);
        Constraint c = mSolver.getCstrs()[0];
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups = "1s")
    public void test2() throws IOException {
        map.register("x", VariableFactory.bounded("x", 0, 2, mSolver));
        map.register("y", VariableFactory.bounded("y", 0, 2, mSolver));
        Flatzinc4Parser fp = parser("constraint int_lt(x,y); % x <= y\n", mSolver, map);
        fp.constraint();
        Assert.assertEquals(mSolver.getCstrs().length, 1);
        Constraint c = mSolver.getCstrs()[0];
        Assert.assertTrue(c instanceof Arithmetic);
    }


    @Test(groups = "1s")
    public void test3() throws IOException {
        map.register("x", VariableFactory.bounded("x", 0, 2, mSolver));
        map.register("y", VariableFactory.bounded("y", 0, 2, mSolver));
        Flatzinc4Parser fp = parser("constraint int_lin_eq([2,3],[x,y],10); % 0<= x\n", mSolver, map);
        fp.constraint();
        Assert.assertEquals(mSolver.getCstrs().length, 1);
        Constraint c = mSolver.getCstrs()[0];
//		// not even true (can be Arithmetic or Scalar)
//        Assert.assertTrue(c instanceof Sum);
    }

    @Test(groups = "1s")
    public void test4() throws IOException {
        map.register("q", VariableFactory.boundedArray("q", 2, 0, 2, mSolver));
        Flatzinc4Parser fp = parser("constraint int_lin_eq([ 1, -1 ], [ q[1], q[2] ], -1);", mSolver, map);
        fp.constraint();
        Assert.assertEquals(mSolver.getCstrs().length, 1);
        Constraint c = mSolver.getCstrs()[0];
//		// not even true (can be Arithmetic or Scalar)
//        Assert.assertTrue(c instanceof Sum);
    }


}
