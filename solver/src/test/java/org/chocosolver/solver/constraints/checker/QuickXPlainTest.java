/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.checker;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class QuickXPlainTest {

    private static final Integer SHORT_TERM = 1;
    private static final Integer MEDIUM_TERM = 2;
    private static final Integer LONG_TERM = 3;

    private static final Integer LOW = 1;
    private static final Integer MEDIUM = 2;
    private static final Integer HIGH = 3;

    private static final Integer EQUITY_FUND = 1;
    private static final Integer INVESTMENT_FUND = 2;
    private static final Integer BANK_BOOK = 3;

    @Test(groups = "10s", timeOut = 60000)
    public void testMinimumConflictingSet() {
        Model model = new Model("Financial Services Problem");
        Solver solver = model.getSolver();

        // Variables
        // wr: Willingness to take risks
        IntVar wr = model.intVar("wr", new int[]{SHORT_TERM, MEDIUM_TERM, LONG_TERM});
        // di: Duration of investment
        IntVar di = model.intVar("di", new int[]{LOW, MEDIUM, HIGH});
        // rr: Expected return rate
        IntVar rr = model.intVar("rr", new int[]{LOW, MEDIUM, HIGH});
        // in: Item name
        IntVar in = model.intVar("in", new int[]{EQUITY_FUND, INVESTMENT_FUND, BANK_BOOK});

        //Constraints
        //C1: wr = low --> itemname = bankbook
        model.ifThen(model.arithm(wr, "=", LOW), model.arithm(in, "=", BANK_BOOK));
        //C2: wr = medium --> itemname != equityfund
        model.ifThen(model.arithm(wr, "=", MEDIUM), model.arithm(in, "!=", EQUITY_FUND));
        //C3: di = shortterm --> itemname = bankbook
        model.ifThen(model.arithm(di, "=", SHORT_TERM), model.arithm(in, "=", BANK_BOOK));
        //c4: di = mediumterm --> itemname != equityfund
        model.ifThen(model.arithm(di, "=", MEDIUM_TERM), model.arithm(in, "!=", EQUITY_FUND));
        //C5: rr = high or rr = medium --> itemname != bankbook
        model.ifThen(model.or(model.arithm(rr, "=", HIGH), model.arithm(rr, "=", MEDIUM)), model.arithm(in, "!=", BANK_BOOK));
        //C6: not (wr =low and rr = high)
        model.not(model.and(model.arithm(wr, "=", LOW), model.arithm(rr, "=", HIGH))).post();
        //C7: not (di = shortterm and rr = high)
        model.not(model.and(model.arithm(di, "=", SHORT_TERM), model.arithm(rr, "=", HIGH))).post();
        //C8: not (wr = high and rr = low)
        model.not(model.and(model.arithm(wr, "=", HIGH), model.arithm(rr, "=", LOW))).post();

        /*REQ = {r1: wr = low, r2: di = shortterm, r3: rr = high} - Conflict*/
        List<Constraint> userRequirements = new ArrayList<Constraint>();
        userRequirements.add(model.arithm(wr, "=", LOW));
        userRequirements.add(model.arithm(di, "=", SHORT_TERM));
        userRequirements.add(model.arithm(rr, "=", HIGH));
        for (int i = 0; i < userRequirements.size(); i++) {
            model.post(userRequirements.get(i));
        }

        assertEquals(solver.solve(), false);
        solver.reset();
        List<Constraint> minConflictSet = solver.findMinimumConflictingSet(userRequirements);
        assertEquals(minConflictSet.size(), 2);
        assertEquals(minConflictSet.get(0).toString(), "ARITHM ([rr = 3])");
        assertEquals(minConflictSet.get(1).toString(), "ARITHM ([wr = 1])");
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testErrorConditions() {
        Model model = new Model("Financial Services Problem");
        model.intVar(0, 10);
        model.getSolver().solve();
        // MCS can't be called during solving
        assertEquals(model.getSolver().isSolving(), true);
        assertThrows(SolverException.class, () -> model.getSolver().findMinimumConflictingSet(Collections.emptyList()));
    }

}
