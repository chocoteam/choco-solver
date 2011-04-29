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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.binary.NotEqualX_YC;
import solver.constraints.nary.IntLinComb;
import solver.search.strategy.StrategyFactory;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
* 
*/
public class ConstraintTest {

    FZNParser fzn;
    Solver solver;

    @BeforeMethod
    public void before() {
        fzn = new FZNParser();
        solver = fzn.solver;
    }

    private void preset() {
        solver.set(StrategyFactory.forceInputOrderInDomainMin(solver.getVars(), solver.getEnvironment()));
    }

    @Test
    public void testIntNe() {
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 10: a::output_var;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 10: b::output_var;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_ne(a, b);");

        Assert.assertEquals(solver.getCstrs().length, 1);
        Assert.assertEquals(solver.getCstrs()[0].getClass(), NotEqualX_YC.class);
        preset();
        solver.findAllSolutions();
        Assert.assertEquals(90, solver.getMeasures().getSolutionCount());
    }

    @Test
    public void testIntLinNe(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 26: a::output_var;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 26: b::output_var;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lin_eq([ 1, -1 ], [ a, b ], -1);");

        Assert.assertEquals(solver.getCstrs().length, 1);
        Assert.assertEquals(solver.getCstrs()[0].getClass(), IntLinComb.class);
        preset();
        solver.findAllSolutions();
        Assert.assertEquals(25, solver.getMeasures().getSolutionCount());
    }

    @Test
    public void testIntLinNe2(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array[1 .. 2] of var 1 .. 2: q;");
        TerminalParser.parse(fzn.CONSTRAINT, "constraint int_lin_eq([ 1, -1 ], [ q[1], q[2] ], -1);");

        Assert.assertEquals(solver.getCstrs().length, 1);
        Assert.assertEquals(solver.getCstrs()[0].getClass(), IntLinComb.class);
        preset();
        solver.findAllSolutions();
        Assert.assertEquals(1, solver.getMeasures().getSolutionCount());
    }


}
