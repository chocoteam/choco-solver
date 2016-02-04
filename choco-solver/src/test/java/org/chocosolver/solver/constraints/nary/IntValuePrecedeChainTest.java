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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by cprudhom on 07/07/15.
 * Project: choco.
 */
public class IntValuePrecedeChainTest {

    public static void int_value_precede_chain_dec(IntVar[] X, int S, int T) {
        Solver solver = X[0].getSolver();
        solver.post(ICF.arithm(X[0], "!=", T));
        for (int j = 1; j < X.length; j++) {
            BoolVar bj = ICF.arithm(X[j], "=", T).reif();
            BoolVar[] bis = new BoolVar[j];
            for (int i = 0; i < j; i++) {
                bis[i] = ICF.arithm(X[i], "=", S).reif();
            }
            solver.ifThen(bj, solver.or(bis));
        }
    }


    @Test(groups="10s", timeOut=60000)
    public void test1() {
        for (int i = 0; i < 200; i++) {
            long s1, s2;
            {
                Solver solver = new Solver();
                IntVar[] vars = solver.intVarArray("X", 5, 0, 5, false);
                solver.post(ICF.int_value_precede_chain(vars, 1, 2));
                solver.set(ISF.random(vars, i));
                solver.findAllSolutions();
                s1 = solver.getMeasures().getSolutionCount();
            }
            {
                Solver solver = new Solver();
                IntVar[] vars = solver.intVarArray("X", 5, 0, 5, false);
                int_value_precede_chain_dec(vars, 1, 2);
                solver.set(ISF.random(vars, i));
                solver.findAllSolutions();
                s2 = solver.getMeasures().getSolutionCount();
            }
            Assert.assertEquals(s1, s2);

        }
    }


}
