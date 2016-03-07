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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.nary.cnf.LogOp.implies;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/04/12
 */
public class CNFTest {

    @Test(groups="1s", timeOut=60000)
    public void testJGF() {
        for (int i = 0; i < 2; i++) {

            Model model = new Model();
            BoolVar a = model.boolVar("a");
            BoolVar b = model.boolVar("b");
            IntVar x = model.intVar("x", 0, 24, true);
            IntVar y = model.intVar("y", 0, 24, true);

            if (i == 0) {
                model.addClauses(implies(
                        a,
                        b
                ));
            } else {
                model.addClauses(implies(
                        b.not(),
                        a.not()
                ));
            }
            model.ifThenElse(b, model.arithm(x, ">=", y), model.arithm(x, "<", y));
//            SearchMonitorFactory.log(solver, true, true);
            while (model.solve()) ;
//            System.out.printf("%d\n", solver.getSolutionCount());
        }
    }
}
