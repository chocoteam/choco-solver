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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * @author Alexandre LEBRUN
 */
public class NotEmptyTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        // Set which could be empty
        SetVar var = model.setVar(new int[]{}, new int[]{1, 2, 3});
        model.post(notEmpty(var));

        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertFalse(var.getValue().isEmpty());
        }
        assertEquals(nbSol, 7);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTrivialTrue() {
        Model model = new Model();

        // Set which can't be empty
        SetVar var = model.setVar(new int[]{5}, new int[]{5, 6, 7, 8});
        model.post(notEmpty(var));

        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertFalse(var.getValue().isEmpty());
        }
        assertEquals(8, nbSol);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTrivialFalse() {
        Model model = new Model();

        // Set which must be empty
        SetVar var = model.setVar(new int[]{}, new int[]{});
        model.post(notEmpty(var));

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "10s", timeOut=60000)
    public void testComparedToNbEmpty() {
        Model model = new Model();
        int[] ub = new int[20];
        for (int i = 0; i < 20; i++) {
            ub[i] = i;
        }
        SetVar var = model.setVar(new int[]{}, ub);
        model.post(notEmpty(var));

        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        int nbSol1 = 0;
        while(model.getSolver().solve()) {
            assertFalse(var.getValue().isEmpty());
            nbSol1++;
        }
        System.out.println("step 1");

        model = new Model();
        var = model.setVar(new int[]{}, ub);
        model.nbEmpty(new SetVar[]{var}, 0).post();
        int nbSol2 = 0;
        while(model.getSolver().solve()) {
            assertFalse(var.getValue().isEmpty());
            nbSol2++;
        }
        System.out.println("step 2");

        assertEquals(nbSol1, nbSol2);
    }


    private Constraint notEmpty(SetVar var) {
        return new Constraint("NotEmpty", new PropNotEmpty(var));
    }

}
