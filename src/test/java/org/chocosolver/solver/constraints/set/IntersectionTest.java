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
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class IntersectionTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{}, new int[]{0, 1, 2, 3, 4, 5});
        SetVar intersect = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        model.intersection(setVars, intersect).post();

        checkSolutions(model, setVars, intersect);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testVarsToIntersect() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        SetVar intersect = model.setVar(new int[]{1, 2, 3, 4});
        model.intersection(setVars, intersect).post();

        checkSolutions(model, setVars, intersect);
    }

    @Test
    public void testIntersectToVars() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[3];
        setVars[0] = model.setVar(new int[]{0, 1, 2});
        setVars[1] = model.setVar(new int[]{1, 2, 3});
        setVars[2] = model.setVar(new int[]{2, 4, 5});
        SetVar intersect = model.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        model.intersection(setVars, intersect).post();

        checkSolutions(model, setVars, intersect);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testDifferentDomains() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[3];
        setVars[0] = model.setVar(new int[]{}, new int[]{1, 2, 4, 5});
        setVars[1] = model.setVar(new int[]{}, new int[]{3, 5});
        setVars[2] = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5});
        SetVar intersect = model.setVar(new int[]{1, 2, 3, 4, 5});
        model.intersection(setVars, intersect).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.solve());
    }


    private void checkSolutions(Model model, SetVar[] setVars, SetVar intersect) {
        int nbSol = 0;
        while (model.solve()) {
            nbSol++;
            ISet computed = SetFactory.makeBipartiteSet(0);
            for (SetVar setVar : setVars) {
                for (Integer value : setVar.getValue()) {
                    computed.add(value);
                }
            }
            for (Integer inIntersect : intersect.getValue()) {
                assertTrue(computed.contain(inIntersect));
            }
        }
        assertTrue(nbSol > 0);
    }


}
