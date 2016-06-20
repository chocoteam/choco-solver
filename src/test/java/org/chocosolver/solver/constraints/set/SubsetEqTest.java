/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class SubsetEqTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        SetVar[] vars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4});

        model.subsetEq(vars).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        checkSolution(model, vars);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testOneToFill() {
        Model model = new Model();
        SetVar[] vars = new SetVar[3];
        vars[0] = model.setVar(new int[]{0});
        vars[1] = model.setVar(new int[]{0, 1});
        vars[2] = model.setVar(new int[]{}, new int[]{0, 1, 2});
        model.subsetEq(vars).post();

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertTrue(vars[2].getValue().contain(0));
            assertTrue(vars[2].getValue().contain(1));
        }
        assertEquals(nbSol, 2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoSolution() {
        Model model = new Model();
        SetVar[] vars = new SetVar[3];
        vars[0] = model.setVar(new int[]{0});
        vars[1] = model.setVar(new int[]{0, 1});
        vars[2] = model.setVar(new int[]{}, new int[]{0, 2});
        model.subsetEq(vars).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoSolutionBadData() {
        Model model = new Model();
        SetVar[] vars = new SetVar[3];
        vars[0] = model.setVar(new int[]{0});
        vars[1] = model.setVar(new int[]{1});
        vars[2] = model.setVar(new int[]{}, new int[]{0, 1, 2});

        model.subsetEq(vars).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }




    private void checkSolution(Model model, SetVar[] vars) {
        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            for (int i = 0; i < vars.length - 1; i++) {
                for (Integer value : vars[i].getValue()) {
                    assertTrue(vars[i + 1].getValue().contain(value));
                }
            }
        }
        assertTrue(nbSol > 0);
    }

}
