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
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class ElementTest {


    private Model model;
    private SetVar[] sets;
    private SetVar element;
    private IntVar index;

    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        model = new Model();
        sets = model.setVarArray(3, new int[]{}, new int[]{1, 2, 3});
        element = model.setVar(new int[]{1, 3});
        index = model.intVar(0, 100);
        model.element(index, sets, element).post();

        checkSolutions();
    }


    @Test(groups = "1s", timeOut=60000)
    public void testFixedValues() {
        Model model = new Model();
        SetVar[] sets = new SetVar[3];
        sets[0] = model.setVar(new int[]{1, 3});
        sets[1] = model.setVar(new int[]{3, 4});
        sets[2] = model.setVar(new int[]{4, 5});
        element = model.setVar(new int[]{4, 5});
        index = model.intVar(0, sets.length - 1);
        model.element(index, sets, element).post();

        assertTrue(model.getSolver().solve());
        assertEquals(index.getValue(), 2);
        assertFalse(model.getSolver().solve());
    }


    private int checkSolutions() {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            for (Integer val : sets[index.getValue()].getValue()) {
                assertTrue(element.getValue().contain(val));
            }
            for (Integer val : element.getValue()) {
                assertTrue(sets[index.getValue()].getValue().contain(val));
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }
}
