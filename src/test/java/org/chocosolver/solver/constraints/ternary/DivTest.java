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
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;
import static org.chocosolver.util.ESat.TRUE;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/07/12
 */
public class DivTest extends AbstractTernaryTest {

    @Override
    protected int validTuple(int vx, int vy, int vz) {
        return vy != 0 && vz == vx / vy ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Model model) {
        return model.div(vars[0], vars[1], vars[2]);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL() {
        Model model = new Model();
        IntVar i = model.intVar("i", 0, 2, false);
        model.div(i, model.ONE(), model.ZERO()).getOpposite().post();
//        SMF.log(solver, true, false);
        while (model.getSolver().solve()) ;
    }

    @Test(groups="10s", timeOut=60000)
    public void testJL2() {
        for (int i = 0; i < 100000; i++) {
            final Model s = new Model();
            IntVar a = s.intVar("a", new int[]{0, 2, 3, 4});
            IntVar b = s.intVar("b", new int[]{-1, 1, 3, 4});
            IntVar c = s.intVar("c", new int[]{-3, 1, 4});
            s.div(a, b, c).post();
            Solver r = s.getSolver();
            r.set(randomSearch(new IntVar[]{a, b, c}, i));
            //SMF.log(s, true, true);
            r.plugMonitor((IMonitorSolution) () -> {
                if (!TRUE.equals(r.isSatisfied())) {
                    throw new Error(s.toString());
                }
            });
            while (s.getSolver().solve()) ;
        }
    }
}
