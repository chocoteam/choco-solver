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
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.IntStrategyFactory.random_value;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/03/2014
 */
public class ConstraintTest {

    @Test(groups="1s", timeOut=60000)
    public void testBooleanChannelingJL() {
        //#issue 190
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("bs", 3);
        SetVar s1 = model.setVar("s1", new int[]{}, new int[]{-3, -2, -1, 0, 1, 2, 3});
        SetVar s2 = model.setVar("s2", new int[]{}, new int[]{-3, -2, -1, 0, 1, 2, 3});
        model.or(model.allEqual(new SetVar[]{s1, s2}), model.setBoolsChanneling(bs, s1, 0)).post();
        model.solveAll();
        assertEquals(2040, model.getMeasures().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testDependencyConditions() {
        Model model = new Model();
        IntVar[] ivs = model.intVarArray("X", 4, 0, 10, false);
        model.allDifferent(ivs, "BC").post(); // boundAndInst()
        model.arithm(ivs[0], "+", ivs[1], "=", 4).post(); // all()
        model.arithm(ivs[0], ">=", ivs[2]).post(); // INST + UB or INST + LB
        model.arithm(ivs[0], "!=", ivs[3]).post(); // instantiation()

        model.set(random_value(ivs, 0));
        model.solveAll();
        assertEquals(model.getMeasures().getSolutionCount(), 48);
        assertEquals(model.getMeasures().getNodeCount(), 100);
    }

    @Test(groups="1s", timeOut=60000)
    public void testDependencyConditions2() {
        Model model = new Model();
        IntVar[] ivs = model.intVarArray("X", 4, 0, 10, false);
        model.allDifferent(ivs, "BC").post(); // boundAndInst()
        model.arithm(ivs[0], "+", ivs[1], "=", 4).post(); // all()
        Constraint cr = model.arithm(ivs[0], ">=", ivs[2]);
        cr.post(); // INST + UB or INST + LB
        model.arithm(ivs[0], "!=", ivs[3]).post(); // instantiation()
        model.unpost(cr);
    }

}
