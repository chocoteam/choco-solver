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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class CardinalityTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        setVar.setCard(model.intVar(4));

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertEquals(setVar.getValue().size(), 4);
        }
        assertEquals(nbSol, 126); // binomial coefficient, 4 in 9
    }


    @Test(groups = "1s", timeOut=60000)
    public void testTwoVariables() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5});
        IntVar intVar = model.intVar(0, 100);
        setVar.setCard(intVar);

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertEquals(setVar.getValue().size(), intVar.getValue());
        }
        assertTrue(nbSol > 0);
        assertEquals(nbSol, 32); // (1,5) + (2,5) + (3,5) + (4, 5) + (5,5)
    }


    @Test(groups = "1s", timeOut=60000)
    public void testEmpty() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5});
        setVar.setCard(model.intVar(0));

        assertTrue(model.getSolver().solve());
        assertTrue(setVar.getValue().isEmpty());
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testUnfeasibleLB() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{1, 2, 3}, new int[]{1, 2, 3, 4, 5});
        IntVar intVar = model.intVar(0, 2);
        setVar.setCard(intVar);

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testUnfeasibleUB() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3});
        IntVar intVar = model.intVar(4, 10);
        setVar.setCard(intVar);

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
    }

}
