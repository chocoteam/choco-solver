/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
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
