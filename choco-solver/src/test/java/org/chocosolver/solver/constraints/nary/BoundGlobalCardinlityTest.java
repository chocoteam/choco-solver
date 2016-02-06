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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.constraints.nary.globalcardinality.GlobalCardinality.reformulate;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.lexico_LB;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/11
 */
public class BoundGlobalCardinlityTest {

    @Test(groups="1s", timeOut=60000)
    public void test0() throws ContradictionException {
        Model model = new Model();

        IntVar[] vars = model.intVarArray("vars", 6, 0, 3, true);
        IntVar[] card = model.intVarArray("card", 4, 0, 6, true);

        int[] values = new int[4];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }
        model.globalCardinality(vars, values, card, false).post();

        vars[0].instantiateTo(0, Null);
        vars[1].instantiateTo(1, Null);
        vars[2].instantiateTo(3, Null);
        vars[3].instantiateTo(2, Null);
        vars[4].instantiateTo(0, Null);
        vars[5].instantiateTo(0, Null);

        model.set(lexico_LB(append(vars, card)));
        while (model.solve()) ;
        assertTrue(model.getMeasures().getSolutionCount() > 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void testRandom() {
        Random random = new Random();
//        int seed= 108;{
        for (int seed = 0; seed < 200; seed++) {
//            System.out.println(seed);
            random.setSeed(seed);
            int n = 1 + random.nextInt(6);
            int m = 1 + random.nextInt(4);
            //solver 1
            Model model = new Model();
            int[] values = new int[m];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            {
                IntVar[] vars = model.intVarArray("vars", n, 0, m - 1, true);
                IntVar[] cards = model.intVarArray("cards", m, 0, n, true);
                model.globalCardinality(vars, values, cards, false).post();
//              solver.set(StrategyFactory.random(ArrayUtils.append(vars, cards), solver.getEnvironment(), seed));
                model.set(lexico_LB(append(vars, cards)));
            }
            // reformulation
            Model ref = new Model();
            {
                IntVar[] vars = ref.intVarArray("vars", n, 0, m - 1, true);
                IntVar[] cards = ref.intVarArray("cards", m, 0, n, true);
                reformulate(vars, cards, ref).post();
                ref.set(lexico_LB(append(vars, cards)));
            }
//            SearchMonitorFactory.log(solver, false, true);
            while (model.solve()) ;
            while (ref.solve()) ;
            assertEquals(model.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount());

        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testRandom2() {
        Random random = new Random();
        for (int seed = 0; seed < 200; seed++) {
//            System.out.println(seed);
            random.setSeed(seed);
            int n = 1 + random.nextInt(6);
            int m = 1 + random.nextInt(4);
            //solver 1
            Model model = new Model();
            int[] values = new int[m];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            {
                IntVar[] vars = model.intVarArray("vars", n, 0, m - 1, true);
                IntVar[] cards = model.intVarArray("cards", m, 0, n, true);
                model.globalCardinality(vars, values, cards, false).post();
//                solver.set(StrategyFactory.random(ArrayUtils.append(vars, cards), solver.getEnvironment(), seed));
                model.set(lexico_LB(vars));
            }
            // reformulation
            Model ref = new Model();
            {
                IntVar[] cards = ref.intVarArray("cards", m, 0, n, true);
                IntVar[] vars = ref.intVarArray("vars", n, 0, m - 1, true);
                reformulate(vars, cards, ref).post();
                ref.set(lexico_LB(vars));
            }
//            SearchMonitorFactory.log(solver, false, true);
            while (model.solve()) ;
            while (ref.solve()) ;
            assertEquals(model.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount());

        }
    }
}
