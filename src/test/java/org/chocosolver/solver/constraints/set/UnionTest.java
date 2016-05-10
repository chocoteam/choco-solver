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
public class UnionTest {


    @Test(groups = "1s", timeOut=60000)
    public void testUnionFixed() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        SetVar union = model.setVar(new int[]{1, 2, 3, 4, 5});
        model.union(setVars, union).post();

        checkSolutions(model, setVars, union);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSetVarsFixed() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[3];
        setVars[0] = model.setVar(new int[]{1, 2});
        setVars[1] = model.setVar(new int[]{}, new int[]{3});
        setVars[2] = model.setVar(new int[]{4, 5});
        SetVar union = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7});
        model.union(setVars, union).post();

        assertEquals(checkSolutions(model, setVars, union), 2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testImpossible() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{1}, new int[]{1, 2, 3, 4});
        SetVar union = model.setVar(new int[]{2, 3, 4}); // different domains
        model.union(setVars, union).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }


    private int checkSolutions(Model model, SetVar[] setVars, SetVar union) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            ISet computed = SetFactory.makeLinkedList();
            for (SetVar setVar : setVars) {
                for (Integer value : setVar.getValue()) {
                    assertTrue(union.getValue().contain(value));
                    computed.add(value);
                }
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }

}
