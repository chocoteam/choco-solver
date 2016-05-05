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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class BitsIntChannelingTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        BoolVar[] bits = model.boolVarArray(7);
        IntVar intVar = model.intVar(10);
        model.bitsIntChanneling(bits, intVar).post();
        checkSolutions(model, bits, intVar);
    }

    @Test(groups = "1s", timeOut=6000)
    public void testNominalReverse() {
        Model model = new Model();
        BoolVar[] bits = new BoolVar[] {
                model.boolVar(false),
                model.boolVar(true),
                model.boolVar(false),
                model.boolVar(true),
                model.boolVar(false)
        };
        IntVar intVar = model.intVar(0, 100);
        model.bitsIntChanneling(bits, intVar).post();
        assertTrue(model.getSolver().solve());
        assertEquals(intVar.getValue(), 10);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testOneUnknownBit() {
        Model model = new Model();
        IntVar var = model.intVar(10, 16);
        BoolVar[] bits = new BoolVar[] {
            model.boolVar(false),
            model.boolVar(false),
            model.boolVar(false),
            model.boolVar(false),
            model.boolVar(),
        };
        model.bitsIntChanneling(bits, var).post();
        checkSolutions(model, bits, var);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNotEnoughDigits() {
        Model model = new Model();
        BoolVar[] bits = model.boolVarArray(7);
        IntVar intVar = model.intVar(128, 500);
        model.bitsIntChanneling(bits, intVar).post();
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTooSmallBound() {
        Model model = new Model();
        BoolVar[] bits = new BoolVar[] {
            model.boolVar(false),
            model.boolVar(false),
            model.boolVar(true),
            model.boolVar(false),
            model.boolVar(false),
            model.boolVar(true),
            model.boolVar(true),
        };
        IntVar intVar = model.intVar(0, 99);
        model.bitsIntChanneling(bits, intVar).post();
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEmptyArray() {
        Model model = new Model();
        BoolVar[] bits = new BoolVar[0];
        IntVar intVar = model.intVar(0, 100);
        model.bitsIntChanneling(bits, intVar).post();
        assertTrue(model.getSolver().solve());
        assertEquals(intVar.getValue(), 0);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNegativeValue() {
        Model model = new Model();
        BoolVar[] bits = model.boolVarArray(8);
        IntVar var = model.intVar(-5, -1);
        model.bitsIntChanneling(bits, var).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testFree() {
        Model model = new Model();
        BoolVar[] bits = model.boolVarArray(10);
        IntVar var = model.intVar(0, 1000);
        model.bitsIntChanneling(bits, var).post();

        checkSolutions(model, bits, var);
    }

    private void checkSolutions(Model model, BoolVar[] bits, IntVar var) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            int exp = 1;
            int number = 0;
            for (BoolVar bit : bits) {
                number += bit.getValue() * exp;
                exp *= 2;
            }
            assertEquals(number, var.getValue());
        }
        assertTrue(nbSol > 0);
    }

}
