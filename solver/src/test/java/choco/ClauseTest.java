/**
 * Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package choco;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.cnf.ALogicTree;
import solver.constraints.nary.cnf.ConjunctiveNormalForm;
import solver.constraints.nary.cnf.Literal;
import solver.constraints.nary.cnf.Node;
import solver.search.strategy.StrategyFactory;
import solver.variables.BoolVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22 nov. 2010
 */
public class ClauseTest {

    Logger log = LoggerFactory.getLogger("test");

    @Test(groups = "1s")
    public void test1() {
        int nSol = 1;
        for (int n = 1; n < 12; n++) {
            for (int i = 0; i <= n; i++) {
                Solver s = new Solver();

                BoolVar[] bs = new BoolVar[n];

                Literal[] lits = new Literal[n];
                for (int j = 0; j < n; j++) {
                    bs[j] = VariableFactory.bool("b" + j, s);
                    if(j < i){
                        lits[j] = Literal.pos(bs[j]);
                    }else{
                        lits[j] = Literal.neg(bs[j]);
                    }
                }

                ALogicTree or = Node.or(lits);

                log.info(or.toString());
                Constraint cons = new ConjunctiveNormalForm(or, s);

                Constraint[] cstrs = new Constraint[]{cons};

                s.post(cstrs);
                s.set(StrategyFactory.preset(bs, s.getEnvironment()));
                s.findAllSolutions();
                long sol = s.getMeasures().getSolutionCount();
                Assert.assertEquals(sol, nSol);
            }
            nSol = nSol * 2 + 1;
        }
    }

    @Test(groups = "1s")
    public void testBothAnd() {
        Solver s = new Solver();

        BoolVar[] bs = new BoolVar[1];
        bs[0] = VariableFactory.bool("to be", s);

        ALogicTree and = Node.and(Literal.pos(bs[0]), Literal.neg(bs[0]));

        Constraint cons = new ConjunctiveNormalForm(and, s);
        System.out.printf("%s\n", cons.toString());
        Constraint[] cstrs = new Constraint[]{cons};

        s.post(cstrs);
        s.set(StrategyFactory.preset(bs, s.getEnvironment()));
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 0);
    }

    @Test(groups = "1s")
    public void testBothOr() {
        Solver s = new Solver();

        BoolVar b = VariableFactory.bool("to be", s);

        ALogicTree or = Node.or(Literal.pos(b), Literal.neg(b));

        Constraint cons = new ConjunctiveNormalForm(or, s);

        Constraint[] cstrs = new Constraint[]{cons};

        BoolVar[] bs = new BoolVar[]{b};

        s.post(cstrs);
        s.set(StrategyFactory.preset(bs, s.getEnvironment()));
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 2);
    }

    

}
