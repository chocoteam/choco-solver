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
public class OffsetTest {

    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{1, 2}, new int[]{1, 2, 3, 4, 5, 6});
        SetVar offsetted = model.setVar(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        model.offSet(setVar, offsetted, 1).post();

        checkSolutions(model, setVar, offsetted, 1);
    }

    @Test(groups = "1s", timeOut=60000) 
    public void testNominalNegative() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{2, 3}, new int[]{1, 2, 3, 4, 5, 6, 7});
        SetVar offsetted = model.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
        model.offSet(setVar, offsetted, -1).post();

        checkSolutions(model, setVar, offsetted, -1);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominalInverse() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        SetVar offsetted = model.setVar(new int[]{2, 3}, new int[]{2, 3, 5});
        model.offSet(setVar, offsetted, 2).post();

        checkSolutions(model, setVar, offsetted, 2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEqualityFalse() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{1, 2, 3});
        SetVar offsetted = model.setVar(new int[]{0, 2, 3});
        model.offSet(setVar, offsetted, 0).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEqualityTrue() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{1, 2, 3});
        SetVar offsetted = model.setVar(new int[]{1, 2, 3});
        model.offSet(setVar, offsetted, 0).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        checkSolutions(model, setVar, offsetted, 0);
    }

    @Test(groups = "1s", timeOut=60000)
    public void wrongLowerBound() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{2, 3, 4}, new int[]{1, 2, 3, 4, 5, 6, 7});
        SetVar offsetted = model.setVar(new int[]{}, new int[]{2, 3, 4, 5, 7, 8});
        model.offSet(setVar, offsetted, -1).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void wrongUpperBound() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{2, 3, 4});
        SetVar offsetted = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 6, 7});
        model.offSet(setVar, offsetted, 1).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    private void checkSolutions(Model model, SetVar set, SetVar offseted, int offset) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            assertEquals(set.getValue().size(), offseted.getValue().size());
            for (Integer value : set.getValue()) {
                assertTrue(offseted.getValue().contains(value + offset));
            }
        }
    }

}
