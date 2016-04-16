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
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class BoolChannelTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        BoolVar[] boolVars = model.boolVarArray(5);
        SetVar setVar = model.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4});
        model.setBoolsChanneling(boolVars, setVar).post();

        checkSolutions(model, setVar, boolVars);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testBooleansFixed() {
        Model model = new Model();
        BoolVar[] boolVars = new BoolVar[] {
            model.boolVar(false),
            model.boolVar(true),
            model.boolVar(false),
            model.boolVar(true),
            model.boolVar(false)
        };
        SetVar setVar = model.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
        model.setBoolsChanneling(boolVars, setVar).post();

        checkSolutions(model, setVar, boolVars);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSetFixed() {
        Model model = new Model();
        BoolVar[] boolVars = model.boolVarArray(5);
        // the booleans of index {1, 3, 4} must be set to 1, the others to 0
        SetVar setVar = model.setVar(new int[]{1, 3, 4});
        model.setBoolsChanneling(boolVars, setVar).post();

        checkSolutions(model, setVar, boolVars);
    }

    /**
     * Case of a minizinc model, with index starting at 1 instead of 0
     */
    @Test(groups = "1s", timeOut=60000)
    public void testNominalMZN() {
        Model model = new Model();

        BoolVar[] boolVars = model.boolVarArray(5);
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5});
        model.setBoolsChanneling(boolVars, setVar, 1).post();

        checkSolutions(model, setVar, boolVars, 1);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoChannelingPossible() {
        Model model = new Model();

        BoolVar[] boolVars = model.boolVarArray(5);
        SetVar setVar = model.setVar(new int[]{5}, new int[]{5});

        model.setBoolsChanneling(boolVars, setVar).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.solve());
    }

    private void checkSolutions(Model model, SetVar setVar, BoolVar[] boolVars) {
        checkSolutions(model, setVar, boolVars, 0);
    }

    private void checkSolutions(Model model, SetVar setVar, BoolVar[] boolVars, int offset) {
        int nbSol = 0;
        while (model.solve()) {
            nbSol++;
            for (Integer value : setVar.getValue()) {
                assertEquals(boolVars[value - offset].getBooleanValue(), ESat.TRUE);
            }
            for (int i = 0; i < boolVars.length; i++) {
                assertEquals(boolVars[i].getBooleanValue() == ESat.TRUE, setVar.getValue().contain(i + offset));
            }
        }
        assertTrue(nbSol > 0);
    }

}
