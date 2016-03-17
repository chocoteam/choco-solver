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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexandre LEBRUN
 */
public class NotMemberTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        IntVar var = model.intVar(0, 5);
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 6});

        model.notMember(var, setVar).post();

        checkSolutions(model, setVar, var);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testFixedValue() {
        Model model = new Model();

        IntVar var = model.intVar(10);
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        model.notMember(var, setVar).post();

        checkSolutions(model, setVar, var);
    }

    @Test(groups = "1s", timeOut=60000)
    public void trivialFalse() {
        Model model = new Model();

        IntVar var = model.intVar(1, 3);
        SetVar setVar = model.setVar(new int[]{1, 2, 3}, new int[]{1, 2, 3, 4, 5, 6});
        model.notMember(var, setVar).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTrivialTrue() {
        Model model = new Model();

        IntVar var = model.intVar(1);
        // different domains
        SetVar setVar = model.setVar(new int[]{}, new int[]{4, 5, 6, 7, 8, 9});
        model.notMember(var, setVar).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        checkSolutions(model, setVar, var);
    }

    @Test(groups = "1s", timeOut=60000)
    public void emptySet() {
        Model model = new Model();

        IntVar var = model.intVar(1, 10);
        SetVar setVar = model.setVar(new int[]{});
        model.notMember(var, setVar).post();

       checkSolutions(model, setVar, var);
    }

    private void checkSolutions(Model model, SetVar setVar, IntVar var) {
        boolean solutionFound = false;
        while(model.solve()) {
            solutionFound = true;
            assertFalse(setVar.getValue().contain(var.getValue()));
        }
        assertTrue(solutionFound);
    }


}
