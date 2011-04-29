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
package solver.constraints.unary;

import choco.checker.DomainBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class MemberTest {

    private int unionSize(int[] dom, int[] values) {
        int u = 0;
        la:
        for (int i = 0; i < dom.length; i++) {
            for (int j = 0; j < values.length; j++) {
                if (dom[i] == values[j]) {
                    u++;
                    continue la;
                }
            }
        }
        return u;
    }


    private int unionSize(int lb, int ub, int[] values) {
        int u = 0;
        la:
        for (int i = lb; i <= ub; i++) {
            for (int j = 0; j < values.length; j++) {
                if (i == values[j]) {
                    u++;
                    continue la;
                }
            }
        }
        return u;
    }

    @Test(groups = "1s")
    public void test1() {
        for (int i = 1; i < 99; i++) {
            Random r = new Random(i);
            for (double d = 0.0; d <= 1.0; d += 0.125) {

                Solver s = new Solver();

                IntVar[] vars = new IntVar[1];
                int[][] values = DomainBuilder.buildFullDomains(2, 0, i, r, d, false);
                vars[0] = VariableFactory.enumerated("v", values[0], s);

                Constraint[] cstrs = new Constraint[]{new Member(vars[0], values[1], s)};

                s.post(cstrs);
                s.set(StrategyFactory.preset(vars, s.getEnvironment()));

                s.findAllSolutions();
                long sol = s.getMeasures().getSolutionCount();
                long nod = s.getMeasures().getNodeCount();
                Assert.assertEquals(sol, unionSize(values[0], values[1]), "nb sol incorrect");
                Assert.assertEquals(nod, sol == 0?0:sol * 2 - 1, "nb sol incorrect");
            }
        }
    }

    @Test(groups = "1s")
    public void test2() {
        for (int i = 1; i < 99; i++) {
            Random r = new Random(i);
            for (double d = 0.0; d <= 1.0; d += 0.125) {

                Solver s = new Solver();
                IntVar[] vars = new IntVar[1];
                int[][] values = DomainBuilder.buildFullDomains(2, 0, i, r, d, false);
                int lb = values[0][0];
                int ub = values[0][values[0].length-1];

                vars[0] = VariableFactory.bounded("v", lb, ub, s);

                Constraint[] cstrs = new Constraint[]{new Member(vars[0], values[1], s)};

                s.post(cstrs);
                s.set(StrategyFactory.preset(vars, s.getEnvironment()));

                s.findAllSolutions();
                long sol = s.getMeasures().getSolutionCount();
                long nod = s.getMeasures().getNodeCount();
                Assert.assertEquals(sol, unionSize(lb, ub, values[1]), "nb sol incorrect");
                Assert.assertEquals(nod, sol == 0?0:sol * 2 - 1, "nb nod incorrect");
            }
        }
    }

}
