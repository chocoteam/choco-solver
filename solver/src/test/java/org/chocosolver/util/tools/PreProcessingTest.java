/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import org.chocosolver.solver.Model;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

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
}