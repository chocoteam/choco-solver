/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.Model;
import org.chocosolver.util.ProblemMaker;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 13/09/2016.
 */
public class GephiNetworkTest {

    @Test(groups = "1s", timeOut = 60000)
    public void test1() throws IOException {
        Model s1 = ProblemMaker.makeCostasArrays(7);
        File temp = File.createTempFile("tmp", ".gexf");
        s1.getSolver().constraintNetworkToGephi(temp.getAbsolutePath());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() throws IOException {
        Model s1 = ProblemMaker.makeGolombRuler(11);
        File temp = File.createTempFile("tmp", ".gexf");
        s1.getSolver().constraintNetworkToGephi(temp.getAbsolutePath());
    }

}