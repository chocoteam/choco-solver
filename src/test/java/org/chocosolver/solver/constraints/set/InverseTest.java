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
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class InverseTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        SetVar[] setVars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4});
        SetVar[] inverseSetVars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4});
        model.inverseSet(setVars, inverseSetVars, 0, 0).post();

        boolean solutionFound = false;
        while(model.getSolver().solve()) {
            solutionFound = true;
            checkSolution(setVars, inverseSetVars);
        }
        assertTrue(solutionFound);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEnumerateLength1() {
        Model model = new Model();

        SetVar[] setVars = model.setVarArray(1, new int[]{}, new int[]{0, 1});
        SetVar[] inverseSetVars = model.setVarArray(1, new int[]{}, new int[]{0, 1});
        model.inverseSet(setVars, inverseSetVars, 0, 0).post();

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            checkSolution(setVars, inverseSetVars);
        }
        assertEquals(nbSol, 2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEnumerateLength2() {
        Model model = new Model();

        SetVar[] setVars = model.setVarArray(2, new int[]{}, new int[]{0, 1});
        SetVar[] inverseSetVars = model.setVarArray(2, new int[]{}, new int[]{0, 1});
        model.inverseSet(setVars, inverseSetVars, 0, 0).post();

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            checkSolution(setVars, inverseSetVars);
        }
        assertEquals(nbSol, 16);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBuildInverse() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[2];
        setVars[0] = model.setVar(new int[]{0, 1});
        setVars[1] = model.setVar(new int[]{0});
        SetVar[] inverseSetVars = model.setVarArray(2, new int[]{}, new int[]{0, 1});

        model.inverseSet(setVars, inverseSetVars, 0, 0).post();

        assertTrue(model.getSolver().solve());

        assertEquals(inverseSetVars[0].getValue().toArray(), new int[]{0, 1});
        assertEquals(inverseSetVars[1].getValue().toArray(), new int[]{0});

        assertFalse(model.getSolver().solve());
    }

    private void checkSolution(SetVar[] setVars, SetVar[] inverseSetVars) {
        for (int i = 0; i < setVars.length; i++) {
            for (Integer val : setVars[i].getValue()) {
                assertTrue(inverseSetVars[val].getValue().contains(i));
            }
        }
        for (int i = 0; i < inverseSetVars.length; i++) {
            for (Integer val : inverseSetVars[i].getValue()) {
                assertTrue(setVars[val].getValue().contains(i));
            }
        }
    }
}
