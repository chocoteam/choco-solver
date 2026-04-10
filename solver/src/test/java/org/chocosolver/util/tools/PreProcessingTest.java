/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/10/2020
 */
public class PreProcessingTest {

    @Test(groups = "1s", timeOut = 60000)
    public void test1() {
        Model model = ProblemMaker.makeContrived();
        PreProcessing.detectIntEqualities(model);
        Assert.assertEquals(model.getNbCstrs(), 6);
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDetectIntEqualities() {
        Model model = ProblemMaker.makeNQueenWithBinaryConstraints(8);
        Assert.assertEquals(model.getNbCstrs(), 84);
        PreProcessing.detectIntInequalities(model);
        Assert.assertEquals(model.getNbCstrs(), 59); // 57 + 2 implied constraints
        model.getSolver().findAllSolutions();
        model.getSolver().printShortStatistics();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 92);
    }


    @Test(groups = "1s")
    public void testSACNQ() throws ContradictionException {
        Model model = ProblemMaker.makeNQueenWithBinaryConstraints(4);
        Solver solver = model.getSolver();
        long before = Arrays.stream(model.retrieveIntVars(true))
                .mapToLong(VariableUtils::domainCardinality)
                .sum();
        Assert.assertEquals(before, 16);
        solver.propagate();
        PreProcessing.sac(model, 2000);
        long after = Arrays.stream(model.retrieveIntVars(true))
                .mapToLong(VariableUtils::domainCardinality)
                .sum();
        Assert.assertEquals(after, 8);
    }

    @Test(groups = "1s")
    public void testSACGR() throws ContradictionException {
        Model model = ProblemMaker.makeGolombRuler(6);
        Solver solver = model.getSolver();
        long before = Arrays.stream(model.retrieveIntVars(true))
                .mapToLong(VariableUtils::domainCardinality)
                .sum();
        Assert.assertEquals(before, 2688);
        solver.propagate();
        PreProcessing.sac(model, 2000);
        long after = Arrays.stream(model.retrieveIntVars(true))
                .mapToLong(VariableUtils::domainCardinality)
                .sum();
        Assert.assertEquals(after, 2222);
    }

    @Test(groups = "1s")
    public void testSACBound() throws ContradictionException {
        Model model = ProblemMaker.makeGolombRuler(6);
        Solver solver = model.getSolver();
        long before = Arrays.stream(model.retrieveIntVars(true))
                .mapToLong(VariableUtils::domainCardinality)
                .sum();
        Assert.assertEquals(before, 2688);
        solver.propagate();
        PreProcessing.sacBound(model, 2000);
        long after = Arrays.stream(model.retrieveIntVars(true))
                .mapToLong(VariableUtils::domainCardinality)
                .sum();
        Assert.assertEquals(after, 2340);
    }
}