/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.ternary.PropTimesNaiveWithLong;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Tests for numerical precision issues in propagators.
 * Focuses on overflow, underflow, and floating-point rounding errors.
 *
 * <p>
 * IMPORTANT: Tests avoid singleton domains to trigger the actual propagation
 * that may reveal precision bugs (cf. issue #1214).
 * </p>
 *
 * Related to: <a href="https://github.com/chocoteam/choco-solver/issues/1214">#1214</a>
 *
 * @since 14/08/2026
 */
public class NumericalPrecisionTest {

    // ========================================================================
    // PROP TIMES NAIVE & PROP TIMES NAIVE WITH LONG (CRITICAL)
    // ========================================================================

    /**
     * Test case from issue #1214: float rounding in div() causes unsound pruning.
     * Value 20_381_625 (odd, > 2^24) is not exactly representable as float.
     * This should return true (1525 * 13365 = 20381625), but fails due to float rounding.
     * 
     * Uses non-singleton domains to trigger propagation bug.
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testTimesNaiveFloatRounding() {
        Model m = new Model();
        IntVar x = m.intVar("x", 0, 7_625);
        IntVar y = m.intVar("y", 7_335, 13_365);
        IntVar z = m.intVar("z", 0, 20_381_625);
        
        m.times(x, y, z).post();
        m.arithm(x, "=", 1_525).post();
        m.arithm(y, "=", 13_365).post();
        
        assertTrue(m.getSolver().solve(), 
            "Times constraint should be satisfied: 1525 * 13365 = 20381625");
    }

    /**
     * Test with values just above 2^24 (first odd integer).
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testTimesNaiveAbove2Pow24() {
        Model m = new Model();
        IntVar x = m.intVar("x", 0, 2_000);
        IntVar y = m.intVar("y", 15_550, 15_560);
        IntVar z = m.intVar("z", 0, 27_641_235);
        
        m.times(x, y, z).post();
        m.arithm(x, "=", 1_777).post();
        m.arithm(y, "=", 15_555).post();
        
        assertTrue(m.getSolver().solve(), 
            "Should handle values above 2^24");
    }

    /**
     * Test case from issue #1214: float rounding in div() causes unsound pruning.
     * Value 20_381_625 (odd, > 2^24) is not exactly representable as float.
     * This should return true (1525 * 13365 = 20381625), but fails due to float rounding.
     *
     * Uses non-singleton domains to trigger propagation bug.
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testTimesNaiveWithLongFloatRounding() {
        Model m = new Model();
        IntVar x = m.intVar("x", 0, 7_625);
        IntVar y = m.intVar("y", 7_335, 13_365);
        IntVar z = m.intVar("z", 0, 20_381_625);

        new Constraint("times with long", new PropTimesNaiveWithLong(x, y, z)).post();
        m.arithm(x, "=", 1_525).post();
        m.arithm(y, "=", 13_365).post();

        assertTrue(m.getSolver().solve(),
                "Times constraint should be satisfied: 1525 * 13365 = 20381625");
    }

    /**
     * Test with values just above 2^24 (first odd integer).
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testTimesNaiveWithLongAbove2Pow24() {
        Model m = new Model();
        IntVar x = m.intVar("x", 0, 2_000);
        IntVar y = m.intVar("y", 15_550, 15_560);
        IntVar z = m.intVar("z", 0, 27_641_235);

        new Constraint("times with long", new PropTimesNaiveWithLong(x, y, z)).post();
        m.arithm(x, "=", 1_777).post();
        m.arithm(y, "=", 15_555).post();

        assertTrue(m.getSolver().solve(),
                "Should handle values above 2^24");
    }

}
