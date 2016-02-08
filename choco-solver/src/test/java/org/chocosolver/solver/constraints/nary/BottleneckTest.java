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
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.ResolutionPolicy.MAXIMIZE;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.minDomLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/01/11
 */
public class BottleneckTest {


    @Test(groups="5m", timeOut=300000)
    public void testStynes1() {
        for (int n = 100; n < 201; n += 50) {
            Model model = new Model();

            IntVar[] nexts = new IntVar[n];
            IntVar[] exps = new IntVar[n];
            IntVar[] bws = new IntVar[n];
            for (int i = 0; i < n; i++) {
                nexts[i] = model.intVar("n_" + i, 0, 200, false);
                exps[i] = model.intVar("e_" + i, 0, 200, false);
                bws[i] = model.intVar("b_" + i, 0, 2000, false);
                model.scalar(new IntVar[]{bws[i], exps[i]}, new int[]{1, 1}, "=", nexts[i]).post();
            }

            IntVar sum = model.intVar("sum", 0, 2000 * n, true);
            model.sum(bws, "=", sum).post();

            IntVar[] allvars = append(nexts, exps, bws, new IntVar[]{sum});


            model.getResolver().set(minDomLBSearch(allvars));
            model.setObjectives(MAXIMIZE, sum);
            model.solve();
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void testStynes2() {
        int n = 10000;
        {
            Model model = new Model();

            IntVar[] nexts = new IntVar[n];
            IntVar[] exps = new IntVar[n];
            IntVar[] bws = new IntVar[n];
            for (int i = 0; i < n; i++) {
                nexts[i] = model.intVar("n_" + i, 0, 200, false);
                exps[i] = model.intVar("e_" + i, 0, 200, false);
                bws[i] = model.intVar("b_" + i, 0, 2000, false);
                model.scalar(new IntVar[]{bws[i], exps[i]}, new int[]{1, 1}, "=", nexts[i]).post();
            }

            IntVar sum = model.intVar("sum", 0, 2000 * n, true);
            model.sum(bws, "=", sum).post();

            IntVar[] allvars = append(nexts, exps, bws, new IntVar[]{sum});

            // Heuristic val
            model.getResolver().set(minDomLBSearch(allvars));

            model.solve();
        }


    }


}
