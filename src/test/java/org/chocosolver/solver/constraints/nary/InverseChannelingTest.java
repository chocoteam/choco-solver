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
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class InverseChannelingTest {

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testNominal(boolean bounded, Settings settings) {
        Model model = new Model();
        model.set(settings);
        IntVar[] intVars1 = makeArray(model, 5, 0, 4, bounded);
        IntVar[] intVars2 = makeArray(model, 5, 0, 4, bounded);
        model.inverseChanneling(intVars1, intVars2).post();

        checkSolutions(model, intVars1, intVars2);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testNoSolution(boolean bounded, Settings settings) {
        Model model = new Model();
        model.set(settings);
        IntVar[] intVars1 = new IntVar[] {
                makeVariable(model, 1, 1, bounded),
                makeVariable(model, 0, 1, bounded)
        };
        IntVar[] intVars2 = new IntVar[] {
                makeVariable(model, 1, 1, bounded),
                makeVariable(model, 1, 1, bounded)
        };
        model.inverseChanneling(intVars1, intVars2).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }


    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testDomainsFiltering(boolean bounded, Settings settings) {
        Model model = new Model();
        model.set(settings);
        IntVar[] intVars1 = makeArray(model, 2, 0, 6, bounded);
        IntVar[] intVars2 = makeArray(model, 2, 0, 6, bounded);
        model.inverseChanneling(intVars1, intVars2).post();
        assertEquals(checkSolutions(model, intVars1, intVars2), 2);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = UnsupportedOperationException.class,
            dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testLengthsDiffer(boolean bounded, Settings settings) {
        Model model = new Model();
        model.set(settings);
        IntVar[] intVars1 = makeArray(model, 3, 0, 4, bounded);
        IntVar[] intVars2 = makeArray(model, 4, 0, 4, bounded);
        model.inverseChanneling(intVars1, intVars2).post();
    }


    private int checkSolutions(Model model, IntVar[] intVars1, IntVar[] intVars2) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            for (int i = 0; i < intVars1.length; i++) {
                assertEquals(intVars2[intVars1[i].getValue()].getValue(), i);
            }
            for (int i = 0; i < intVars2.length; i++) {
                assertEquals(intVars1[intVars2[i].getValue()].getValue(), i);
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }


    private IntVar makeVariable(Model model, int lb, int ub, boolean bounded) {
        IntVar var = model.intVar(lb, ub, bounded);
        if(model.getSettings().enableViews()) {
            IntVar first = model.intOffsetView(var, 1);
            return model.intOffsetView(first, -1);
        } else {
            return var;
        }
    }

    private IntVar[] makeArray(Model model, int n, int lb, int ub, boolean bounded) {
        IntVar[] var = model.intVarArray(n, lb, ub, bounded);
        if(model.getSettings().enableViews()) {
            IntVar[] view1 = new IntVar[n];
            for (int i = 0; i < n; i++) {
                view1[i] = model.intOffsetView(var[i], 1);
            }
            IntVar[] view2 = new IntVar[n];
            for (int i = 0; i < n; i++) {
                view2[i] = model.intOffsetView(view1[i], -1);
            }
            return view2;
        } else {
            return var;
        }
    }

}
