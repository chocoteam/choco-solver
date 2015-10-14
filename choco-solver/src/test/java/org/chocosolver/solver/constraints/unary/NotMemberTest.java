/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.unary;

import org.chocosolver.choco.checker.DomainBuilder;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class NotMemberTest {

    private int intersectionSize(int[] dom, int[] values) {
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
        return dom.length - u;
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
        return ub - lb +1 - u;
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

                Constraint[] cstrs = new Constraint[]{IntConstraintFactory.not_member(vars[0], values[1])};

                s.post(cstrs);
                s.set(IntStrategyFactory.lexico_LB(vars));

                s.findAllSolutions();
                long sol = s.getMeasures().getSolutionCount();
                long nod = s.getMeasures().getNodeCount();
                Assert.assertEquals(sol, intersectionSize(values[0], values[1]), "nb sol incorrect");
                Assert.assertEquals(nod, sol == 0 ? 0 : sol * 2 - 1, "nb sol incorrect");
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
                int ub = values[0][values[0].length - 1];

                vars[0] = VariableFactory.bounded("v", lb, ub, s);

                Constraint[] cstrs = new Constraint[]{IntConstraintFactory.not_member(vars[0], values[1])};

                s.post(cstrs);
                s.set(IntStrategyFactory.lexico_LB(vars));

                s.findAllSolutions();
                long sol = s.getMeasures().getSolutionCount();
                long nod = s.getMeasures().getNodeCount();
                Assert.assertEquals(sol, unionSize(lb, ub, values[1]), "nb sol incorrect");
                Assert.assertEquals(nod, sol == 0 ? 0 : sol * 2 - 1, "nb nod incorrect");
            }
        }
    }

    @Test(groups = "1s")
    public void test_alxpgr() {
        Solver s = new Solver();
        IntVar vars = VariableFactory.bounded("v", 0, 10, s);
        int[] values = new int[]{0, 2, 8, 9, 10, 5, 6};

        s.post(ICF.not_member(vars, values));
        s.set(IntStrategyFactory.lexico_LB(vars));

        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 4);

    }

}
